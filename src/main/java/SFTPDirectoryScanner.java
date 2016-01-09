/**
 * Created by pavel2107 on 14.09.15.
 */

import java.io.IOException;
import java.util.*;

import ru.mil.smb.wsdl.data.MoSoapGateway;
import ru.mil.smb.wsdl.data.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcraft.jsch.*;


public class SFTPDirectoryScanner  implements DirectoryScanner{
    private static final Logger logger = LogManager.getLogger( SFTPDirectoryScanner.class.getName());

    private Properties properties;
    private JSch jsch;
    private Session session;

    private String  sessionUID;
    private String  rsaFileName;
    private final MoSoapGateway port;


    final String FTP_USER_NAME;
    final String FTP_SERVER_NAME;
    final int    FTP_SERVER_PORT;
    final String FTP_HOME_DIR;
    final String BANK_UID;
    final int    FTP_MAX_RETRIES;


    public SFTPDirectoryScanner( Properties properties, MoSoapGateway port, String rsaFileName,  String sessionUID){
        this.properties  = properties;
        this.port        = port;
        this.rsaFileName = rsaFileName;
        this.sessionUID  = sessionUID;
        //
        // устнавливаем учетные данные
        //
        FTP_USER_NAME   = properties.getProperty("ftp.server.username");
        FTP_SERVER_NAME = properties.getProperty( "ftp.server.servername");
        FTP_SERVER_PORT = Integer.parseInt(properties.getProperty("ftp.server.port"));
        FTP_HOME_DIR    = properties.getProperty("ftp.server.homedir");
        BANK_UID        = properties.getProperty("BANK_UID");
        FTP_MAX_RETRIES = Integer.parseInt(properties.getProperty("ftp.server.retry"));;
    }

    private boolean initSession(){
       boolean result = true;
       if( session == null || jsch == null){
        logger.info("Открываем соединение с sftp-сервером");
        //
        // инициализируем сессию
        //
        jsch=new JSch();
           try{
               //
               // добавляем сеансовый ключ
               //
               jsch.addIdentity(rsaFileName);
               //
               // открываем сессию
               //
               session = jsch.getSession(FTP_USER_NAME, FTP_SERVER_NAME, FTP_SERVER_PORT);
               // проверку сертификата не делаем
               Properties config = new Properties();
               config.put("StrictHostKeyChecking", "no");
               // начинаем sftp сессию
               session.setConfig(config);
               session.connect();
               logger.info("Открыто соединение с sftp-сервером");

           }
           catch ( Exception e){
               result = false;
               logger.error( e.getMessage());
               e.printStackTrace();
           }
        }
        return result;
    }


    public boolean processOutDirectory(){
        boolean result = true;

        if( !( new File(properties.getProperty("directory.ABS_OUTPUT_DIR") + "/control.zip")).exists()){
            logger.error( "Нет файла " + properties.getProperty("directory.ABS_OUTPUT_DIR") + "/control.zip");
            return false;
        }

        logger.info( "Начинаем отправку файлов");
        try{
            //
            // формируем список каталогов, в которых лежат zip-файлы
            //
            File directory = new File(properties.getProperty("directory.ABS_OUTPUT_DIR"));
            File[] directoryList = (directory).listFiles(pathname -> pathname.isDirectory());
            ArrayList<File> filesToSend = new ArrayList<File>();
            //
            // в каждом каталоге ищем файлы типа data_xxx.zip
            //
            for (File curDir : directoryList) {
                File[] fileList = curDir.listFiles((File dir, String name) -> (name.toLowerCase().startsWith("data_") && name.toLowerCase().endsWith(".zip")));
                // и добавляем их в список для отправки...
                Collections.addAll(filesToSend, fileList);
            }
            //
            // добавляем control.zip файл
            //
            filesToSend.add(new File(properties.getProperty("directory.ABS_OUTPUT_DIR") + "/control.zip"));
            //
            // начинаем передачу файлов
            //
            ChannelSftp sftpChannel = null;
            File fileToSend = null;
            int fileSize = 0;
            for (int i = 0; i < filesToSend.size(); i++) {
                int retries = 0;
                //
                // файл передаем в цикле, если не получилось - пробуем еще 3 раза
                //
                while( retries < FTP_MAX_RETRIES){
                    //
                    // ицициализируем сессию
                    //
                    if( !initSession()) return false;
                    try{
                        //
                        // открываем канал
                        //
                        if(sftpChannel == null) {
                            sftpChannel = (ChannelSftp) session.openChannel("sftp");
                            sftpChannel.connect();
                            //
                            // переходим в каталагог сессии
                            //
                            String sessinonPath = FTP_HOME_DIR + "/data/" + sessionUID + "/in";
                            logger.info("Переходим в каталог " + sessinonPath);
                            sftpChannel.cd(sessinonPath);
                        }
                        //
                        //начинаем передачу файла
                        //
                        fileToSend = filesToSend.get(i);
                        logger.info("Загружаем файл " + fileToSend.getName());
                        FileInputStream fis = new FileInputStream(fileToSend);
                        fileSize = fis.available();
                        sftpChannel.put(fis, fileToSend.getName());
                        fis.close();
                        //
                        // передача файла завершена
                        //
                        break;
                    }
                    catch(Exception e){
                        logger.error( e.getMessage());
                        e.printStackTrace();
                        retries--;
                        //
                        // сбрасываем информацию о сессии
                        //
                        session = null; jsch = null; sftpChannel = null;
                    }
                }
                //
                // подготавливаем описатель файла
                //
                FileMetaInfo fileMetaInfo = new FileMetaInfo();
                fileMetaInfo.setBankUid(BANK_UID);
                fileMetaInfo.setFileName(fileToSend.getName());
                fileMetaInfo.setSize(fileSize);
                //
                // отправляем серверу подтверждение передачи
                //
                if (i == (filesToSend.size() - 1)) {
                    // это контрольный файл
                    SendRequestConfirmationRequest sendRequestConfirmationRequest = new SendRequestConfirmationRequest();
                    sendRequestConfirmationRequest.setSessionUid(sessionUID);
                    sendRequestConfirmationRequest.setFileLink(fileMetaInfo);
                    SendRequestConfirmationResponse sendRequestConfirmationResponse = port.sendRequestConfirmation(sendRequestConfirmationRequest);
                    if (sendRequestConfirmationResponse.getOperationResult() != 0) {
                        logger.error("Ошибка отправки файла");
                        result = false;
                    }
                } else {
                    SendFileConfirmationRequest sendFileConfirmationRequest = new SendFileConfirmationRequest();
                    sendFileConfirmationRequest.setSessionUid(sessionUID);
                    sendFileConfirmationRequest.setFileLink(fileMetaInfo);
                    //
                    // получаем первичное подтверждение передачи
                    //
                    SendFileConfirmationResponse sendFileConfirmationResponse = port.sendFileConfirmation(sendFileConfirmationRequest);
                    if (sendFileConfirmationResponse.getOperationResult() != 0) {
                        logger.error("Ошибка отправки файла");
                        result = false;
                    }
                }

            }
        }
        catch (Exception e){
            logger.error( e.getMessage());
            e.printStackTrace();
            result = false;
        }

        logger.info( "Отправка файлов завершена");
        return result;
    }

    //
    // ждем ответа после копирования файлов на ftp-сервер
    //
    public boolean waitForReply() {
        boolean result = false;
        int replys  = Integer.parseInt( properties.getProperty( "ftp.server.retry"));
        int timeout = Integer.parseInt( properties.getProperty( "ftp.server.timeout")) * 1000 * 60;
        String checkingPath = FTP_HOME_DIR + "/data/" + sessionUID + "/out";
        SftpATTRS attrs;
        logger.info( "Ожидание ответа");

        //
        // создаем канал связи
        //
        ChannelSftp sftpChannel = null;
        try {
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            for( int i = 0; i < replys; i++){
                logger.info( "Ожидание " + timeout/ 1000 + " секунд");
                Thread.sleep(timeout);
                // проверяем наличие каталога out на сервере sftp. если каталог есть - ответ получен
                try {
                    attrs = sftpChannel.stat( checkingPath);
                    if( attrs != null){
                        result = true;
                        logger.info( "Каталог out создан");
                        break;
                    }
                } catch (Exception e) {
                    logger.info( "Путь " + checkingPath + " не найден");
                }
            }
        } catch (JSchException e) {
            logger.error( e.getMessage());
            e.printStackTrace();
        }
        catch ( InterruptedException e){}
        finally {
            if( sftpChannel != null && sftpChannel.isConnected())    sftpChannel.disconnect();
        }
        return result;
    }

    //
    // копируем файлы с сервера
    //
    public void processInDirectory(){
        String outputDir = properties.getProperty( "directory.ABS_INPUT_DIR");
        ChannelSftp sftpChannel = null;
        try {
            if( !initSession()) return;
            //
            // ожидаем появления каталога out
            //
            if( waitForReply()) {
                //
                // открываем канал
                //
                sftpChannel = (ChannelSftp) session.openChannel("sftp");
                sftpChannel.connect();
                //
                // переходим в каталог out
                //
                String sessionPath = FTP_HOME_DIR + "/data/" + sessionUID;
                sftpChannel.cd(sessionPath);
                Vector<ChannelSftp.LsEntry> fileList = sftpChannel.ls("out");
                for (ChannelSftp.LsEntry currentFile : fileList) {
                    if (currentFile.getAttrs().isDir()) {
                        continue; // skip directories . and ..
                    }
                    //
                    // копируем файл на локальный диск
                    //
                    logger.info("Копируем файл  " + "out/" + currentFile.getFilename() + " -> " + outputDir + "/" + currentFile.getFilename());
                    sftpChannel.get("out/" + currentFile.getFilename(), outputDir + "/" + currentFile.getFilename());
                    //
                    // копируем файл в каталог out_processed
                    //
                    logger.info("Копируем файл " + "out/" + currentFile.getFilename() + " -> " + "out_processed/" + currentFile.getFilename());
                    sftpChannel.rename("out/" + currentFile.getFilename(), "out_processed/" + currentFile.getFilename());
                    logger.info("Файл " + "out/" + currentFile.getFilename() + " обработан");
                }
            } // waitforreply
        }
        catch (Exception e){
            logger.error( e.getMessage());
            e.printStackTrace();
        }
        finally {
            if( sftpChannel != null && sftpChannel.isConnected())    sftpChannel.disconnect();
        }
    }

    public void clearDirectories(){
       if( session != null){
           //
           // закрываем сессию
           //
           session.disconnect();
           session = null;
           jsch    = null;
       }
    }


}
