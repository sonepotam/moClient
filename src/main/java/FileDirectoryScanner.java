import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by lenovo on 13.09.2015.
 */
public class FileDirectoryScanner implements DirectoryScanner{

    Properties properties;
    final String ABS_OUTPUT_DIR  ;
    final String ABS_ARCH_OUT_DIR;
    final String ABS_INPUT_DIR;
    final String ABS_ARCH_IN_DIR;

    private static final Logger logger = LogManager.getLogger( FileDirectoryScanner.class.getName());
    private Signer fileSigner;

    public FileDirectoryScanner ( Properties properties,  Signer fileSigner){
        this.properties = properties;
        this.fileSigner = fileSigner;

        ABS_OUTPUT_DIR   = properties.getProperty( "directory.ABS_OUTPUT_DIR");
        ABS_ARCH_OUT_DIR = properties.getProperty( "directory.ABS_ARCH_OUT");
        ABS_INPUT_DIR    = properties.getProperty("directory.ABS_INPUT_DIR");
        ABS_ARCH_IN_DIR   = properties.getProperty( "directory.ABS_ARCH_IN");
    }

    //
    // обработка исходящего каталога АБС
    //
    public boolean processOutDirectory(){
        boolean result = true;
        try{

        //
        // читаем control.xml
        //
        File controlFile = new File( ABS_OUTPUT_DIR + "/control.xml");
        if( !controlFile.exists()){
            logger.error( "Нет файла " + controlFile.getName());
            if( new File( ABS_OUTPUT_DIR + "/control.zip").exists()) {
                logger.info("Обнаружен архив " + ABS_OUTPUT_DIR + "/control.zip");
                return true;
            }
            return false;
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document controlDoc = builder.parse(controlFile);
        // готовимся изменять файл
        XPath searchPath = XPathFactory.newInstance().newXPath();

        // ищем каталоги с контейнерами
        File[] directoryList = ( new File( ABS_OUTPUT_DIR)).listFiles( p -> p.isDirectory());


        //
        // просматриваем каталоги типа FILES/out/....
        //
        for( int i = 0; i < directoryList.length; i++){
            //
            // в каталоге должен быть файл data.zip и файлы-вложения
            // файл data.zip нужно подписать и зазиповать
            //
            logger.info("Обрабатываем каталог " + directoryList[i].getAbsolutePath());
            //
            // просматриваем содержимое каталога с контейнером
            //
            File[] currentDir = ( directoryList[i]).listFiles( p -> p.isFile());
            //
            // создаем zip-file
            //
            File zip = new File( directoryList[ i].getAbsolutePath() + "/data_" + i + ".zip");
            logger.info( "Создаем архив " + zip.getAbsolutePath());
            FileOutputStream zipStream = new FileOutputStream( zip );
            ZipOutputStream dataZip= new ZipOutputStream( zipStream);
            for( File curFile: currentDir){
                if( "data.xml.sig".equals( curFile.getName())) continue;
                appendZipFile( dataZip, curFile);
                //
                // для файла data.zip создаем цифровую подпись и записываем файл с подписью в архив
                //
                if( "data.xml".equals( curFile.getName())) {
                    fileSigner.cades(curFile.getAbsolutePath());
                    String fName  = curFile.getAbsolutePath();
                    File signFile = new File( fName.substring( 0, fName.length() - 3) + "sign");
                    appendZipFile( dataZip, signFile);
                    signFile.delete();
                }
                //
                // удаляем файл после включения в архив
                //
                curFile.delete();
            }
            //
            // закрываем архив и его поток
            //
            dataZip.close();
            zipStream.close();
            logger.info( "Запись в архив " + zip.getAbsolutePath() + " завершена");
            //
            // вычисляем crc32
            //
            long crc32 = Utils.calculateCRC32( zip);
            long zipSize = zip.length();
            //
            // в файле control.xml ищем соответствующий узел контейнера и меняем его атрибуты
            //
            String xpathQuery = "//Containers[@ReqUID='" + directoryList[ i].getName() + "']";
            Node containerNode = (Node)searchPath.evaluate( xpathQuery, controlDoc, XPathConstants.NODE);
            if( containerNode == null){
                logger.error( "В файле control.xml  не найдено описание для контейнера " + directoryList[ i].getName());
                System.exit(1);
            }
            Element containerElement = (Element)containerNode;
            containerElement.setAttribute( "name", zip.getName());
            containerElement.setAttribute( "size", zipSize + "" );
            containerElement.setAttribute("CRC", crc32 + "");
        }
        // сохраняем control.xml
        DOMSource domSource = new DOMSource( controlDoc);
        StreamResult streamResult = new StreamResult( controlFile);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(domSource, streamResult);
        transformer.reset();
        //
        // подписываем файл
        //
        fileSigner.cades(controlFile.getAbsolutePath());
        //
        // записываем файл control.xml и подпись в архив
        //
        File zip = new File( ABS_OUTPUT_DIR + "/control.zip");
        logger.info( "Создаем архив " + zip.getAbsolutePath());
        FileOutputStream zipStream = new FileOutputStream( zip );
        ZipOutputStream dataZip= new ZipOutputStream( zipStream);
        //
        // дописываем управляющий файл
        //
        File cFile = new File(  ABS_OUTPUT_DIR + "/control.xml");
        appendZipFile( dataZip, cFile);
        cFile.delete();
        //
        // дописываем подпись
        //
        cFile = new File(  ABS_OUTPUT_DIR + "/control.sign");
        appendZipFile( dataZip, cFile);
        cFile.delete();
        logger.info( "Запись в архив " + zip.getAbsolutePath() + " завершена");
        //
        // закрываем архив и его поток
        //
        dataZip.close();
        zipStream.close();}
        catch ( Exception e){
            logger.error( e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    public void clearDirectories() {
        try {
            SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            final String sessionDate = df.format(new Date());

            Path startPath = Paths.get(ABS_OUTPUT_DIR);
            logger.info( "Начинаем очистку каталога " + ABS_OUTPUT_DIR);
            Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    logger.info( "Обнаружили файл" + file);
                    if (file.toString().endsWith(".zip")) {
                        Path fileTo = Paths.get(ABS_ARCH_OUT_DIR + "/" + sessionDate + "-" + file.getFileName());
                        Files.move(file, fileTo);
                        logger.info("Скопирован файл " + file + " -> " + fileTo);
                    } else {
                        logger.info("Удаляем файл " + file);
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
                    logger.info( "Обнаружили каталог" + dir);
                    if (!dir.toString().equals(ABS_OUTPUT_DIR)) {
                        logger.info("Удаляем каталог " + dir);
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            logger.info( "Закончили очистку каталога " + ABS_OUTPUT_DIR);
        }
        catch ( Exception e){
            logger.error( e.getMessage());
            e.printStackTrace();
        }
    }


    public void processInDirectory(){
        //
        // распакуем все файлы из каталага in
        //
        final int BUFFER_SIZE      = 4096;
        SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        final String sessionDate = df.format(new Date());
        //
        // ищем файлы с ответом
        logger.info( "Обрабатываем входной каталог " + ABS_INPUT_DIR);
        try {
            File[] directoryList = (new File(ABS_INPUT_DIR)).listFiles(
                    pathname -> !pathname.isDirectory() && pathname.getName().endsWith(".zip"));
            //
            // распаковываем каждый zip
            //
            for (File curFile : directoryList) {
                logger.info("Распаковываем " + curFile.getName());
                //
                // открываем архив
                //
                FileInputStream fis = new FileInputStream(curFile);
                ZipInputStream zis = new ZipInputStream(fis);
                ZipEntry zipEntry;
                while ((zipEntry = zis.getNextEntry()) != null) {
                    //
                    // пропускаем директории, т.к. их быть не должно
                    //
                    if (zipEntry.isDirectory()) continue;
                    //
                    // из архива извлекаем только xml !!!
                    //
                    if (zipEntry.getName().endsWith(".xml")) {
                        File unzippedFile = new File(ABS_INPUT_DIR + "/" + zipEntry.getName());
                        logger.info("Извлекаем файл " + zipEntry.getName());
                        FileOutputStream fos = new FileOutputStream(unzippedFile);
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int count = 0;
                        while ((count = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, count);
                        }
                        fos.close();
                    }
                    zis.closeEntry();
                }
                zis.close();
                fis.close();
                Files.move(curFile.toPath(), Paths.get(ABS_ARCH_IN_DIR + "/" + sessionDate + "-" + curFile.getName()));

            }
        }
        catch ( Exception e){
            logger.error( e.getMessage());
            e.printStackTrace();
        }

        logger.info("Обработан входной каталог " + ABS_INPUT_DIR);
    }



    private void appendZipFile( ZipOutputStream dataZip, File file)throws Exception{
        logger.info( "В архив записываем файл " + file.getName() + " -> " + file.getAbsolutePath());
        // открываем поток для чтения файла
        FileInputStream fis = new FileInputStream( file);
        byte[] buffer = new byte[ fis.available()];
        fis.read(buffer);
        fis.close();
        // создаем элемент архива
        ZipEntry zipEntry = new ZipEntry( file.getName());
        // записываем данные в элемент архива и закрываем элемент
        dataZip.putNextEntry( zipEntry);
        dataZip.write(buffer);
        dataZip.closeEntry();
    }

}
