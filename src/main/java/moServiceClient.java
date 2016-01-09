/**
 * Created by pavel2107 on 28.09.15.
 */


/**
 * Created by pavel2107 on 03.09.15.
 * использованные ресурсы
 *
 * http://fm-test.lanit.ru/mo/integration/soapGateway/webService.wsdl
 *
 * http://www.jcraft.com/jsch/ - домашняя страница jsch
 * http://stackoverflow.com/questions/15108923/file-transfer-using-java-jsch-sftp - пример работы с JSch
 *
 * http://www.computerhope.com/unix/sftp.htm - описание параметров sftp
 *
 * очень интересно
 * http://oldcouncil.blogspot.ru/search?updated-min=2013-01-01T00:00:00-08:00&updated-max=2014-01-01T00:00:00-08:00&max-results=2 - пример подписи СМЭВа
 * http://stackoverflow.com/questions/12528667/xml-digital-signature-java - пример подписи XMLDsig
 * https://gist.github.com/asilchev - еще примеры подписи
 *
 * cryptopro
 * http://www.cryptopro.ru/forum2/default.aspx?g=posts&t=5635 - подпись части сообщений
 * пример СМЭВ от криптопро
 * http://www.cryptopro.ru/blog/2012/07/02/podpis-soobshchenii-soap-dlya-smev-s-ispolzovaniem-kriptopro-jcp
 * http://www.cryptopro.ru/forum2/default.aspx?g=posts&t=9123 пример деаттач подписи pkcs7
 *
 * подпись на уровне SOAPMessage
 * http://www.stackprinter.com/export?service=stackoverflow&question=830691&printer=false&linktohome=true
 *
 * какие поля и как прописывать в сообщение...
 *
 * https://www.cryptopro.ru/forum2/default.aspx?g=posts&m=61356
 */


import javax.xml.namespace.QName;

import java.io.*;
import java.util.*;
import java.net.*;

import ru.mil.smb.wsdl.data.MoSoapGateway;
import ru.mil.smb.wsdl.data.MoSoapGatewayService;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;


import ru.mil.smb.wsdl.data.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class moServiceClient {



    private static final Logger logger = LogManager.getLogger( moServiceClient.class.getName());

    int DIRECTORY_SCAN_TIMEOUT = 5;
    String ABS_OUTPUT_DIR = "FILES/out";


    Properties configProperties;
    Signer fileSigner;

    private QName SERVICE_NAME = new QName("http://smb.mil.ru/wsdl/data", "moSoapGatewayService");
    private URL WSDL_LOCATION;


    private static moServiceClient serviceClient = null;

    private moServiceClient( Properties p, Signer fs){

        this.configProperties = p;
        this.fileSigner       = fs;
    }


    public static moServiceClient getInstance( Properties p, Signer fs){
        if ( serviceClient == null){
            serviceClient = new moServiceClient( p, fs);
        }
        return serviceClient;
    }

    //
    // обмен с веб-сервисом
    //
    public boolean integrator()  {
        boolean result = false;
        try{
            //
            // создаем объект для связи веб-сервисом МО
            //
            logger.info("Устанавливаем связь с веб-сервисом");
            SERVICE_NAME = new QName(configProperties.getProperty("service.endpoint.namespace.uri"), configProperties.getProperty("service.ednpoint.namespace.localpart"));
            WSDL_LOCATION = new URL(configProperties.getProperty("service.WSDL_LOCATION"));

            MoSoapGatewayService moSoapGatewayService = new MoSoapGatewayService(WSDL_LOCATION, SERVICE_NAME);
            moSoapGatewayService.setHandlerResolver(new HandlerResolver() {
                public List<Handler> getHandlerChain(PortInfo portInfo) {
                    List<Handler> handlerList = new ArrayList<Handler>();
                    handlerList.add(new moServiceSOAPHandler( fileSigner));
                    return handlerList;
                }
            });
            MoSoapGateway port = moSoapGatewayService.getMoSoapGatewaySoap11();
            ServicePortDecorator servicePortDecorator = new ServicePortDecorator( port, 30, 3);
            //
            // получаем таймаут для опроса каталогов
            //
            DIRECTORY_SCAN_TIMEOUT = Integer.parseInt(configProperties.getProperty("directory.scan.timeout")) * 1000;
            ABS_OUTPUT_DIR = configProperties.getProperty("directory.ABS_OUTPUT_DIR");
            //
            // начинаем сессию с МО
            //
            StartSessionRequest startSessionRequest = new StartSessionRequest();
            StartSessionResponse startSessionResponse = servicePortDecorator.startSession(startSessionRequest);
            //
            // если сессия не начата, то на выход
            //
            if (startSessionResponse.getSessionUid() == null) {
                logger.error("Сессия НЕ начата");
                return  false;
            };
            //
            // получаем сеансовый ключ
            //
            GetRSAKeyRequest getRSAKeyRequest = new GetRSAKeyRequest();
            GetRSAKeyResponse getRSAKeyResponse = servicePortDecorator.getRSAKey( getRSAKeyRequest);
            logger.debug("");
            logger.debug("==================== RSA KEY BEGIN ========================");
            logger.debug(getRSAKeyResponse.getKey());
            logger.debug("==================== RSA KEY END ==========================");
            if( ( result = getRSAKeyResponse.getKey() != null)){
                FileOutputStream fos = new FileOutputStream("rsa_key.txt");
                fos.write(getRSAKeyResponse.getKey().getBytes());
                fos.close();
                logger.info("Ключ сохранен в файл rsa_key.txt ");
                //
                // готовимся к обработке файлов
                //
                DirectoryScanner fileDirectoryScanner = new FileDirectoryScanner( configProperties, fileSigner);
                //
                // начинаем просмотр каталогов ABS_OUTPUT_DIR
                //
                if( fileDirectoryScanner.processOutDirectory()) {
                    //
                    // начинаем процесс обмена по sftp
                    //
                    DirectoryScanner sftpDirectoryScanner =
                            new SFTPDirectoryScanner(configProperties, servicePortDecorator,
                                    "rsa_key.txt", startSessionResponse.getSessionUid());
                    //
                    // отправляем файлы
                    //
                    result = sftpDirectoryScanner.processOutDirectory();
                    //
                    // принимаем файлы
                    //
                    sftpDirectoryScanner.processInDirectory();
                }
                //
                // обрабатываем входной каталог
                //
                fileDirectoryScanner.processInDirectory();
                fileDirectoryScanner.clearDirectories();
            } // rsa
            //result = true;
        }
        catch ( Exception e){
            logger.error( e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

}

