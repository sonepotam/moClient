import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.zip.CRC32;

/**
 * Created by pavel2107 on 25.09.15.
 */
public class Utils {

    private static final Logger logger = LogManager.getLogger( Utils.class.getName());

    public static void prettyPrint(Document xml) throws Exception {
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        Writer out = new StringWriter();
        tf.transform(new DOMSource(xml), new StreamResult(out));
        logger.info( out.toString());
    }


    public static long calculateCRC32( File file) throws Exception{
        CRC32 crc32 = new CRC32();
        long result = 0;
        FileInputStream fis = new FileInputStream( file);
        byte[] buffer = new byte[ fis.available()];
        fis.read(buffer);
        fis.close();
        crc32.update(buffer);
        result  = crc32.getValue();
        crc32.reset();
        return result;

    }

    //
    // обработка исходящего каталога АБС
    //
    public static  boolean createDisc( Properties properties) throws Exception{
        final String ABS_OUTPUT_DIR   = properties.getProperty("directory.ABS_OUTPUT_DIR");
        boolean result = true;
        BufferedWriter writer = new BufferedWriter( new FileWriter( "opis.txt"));
        SimpleDateFormat df = new SimpleDateFormat( "dd.MM.yyyy");


        writer.write("СПЕЦИФИКАЦИЯ НА КОНТЕЙНЕРЫ\n");
        writer.write("БАНК ОАО \"АБ\" РОСCИЯ\n");
        writer.newLine();
        writer.write("от " + df.format(new Date()));
        writer.newLine();
        writer.write("тип носителя               _________________________________");
        writer.newLine();
        writer.write("учетные реквизиты носителя _________________________________");
        writer.newLine();
        writer.newLine();
        writer.write("ПЕРЕЧЕНЬ ФАЙЛОВ\n\n\n");
        writer.newLine();
        writer.newLine();
        writer.write("+--------------------------------------------------------------------------+\n");
        writer.write( "| Наименование файла   | Размер     | Дата создания | Контрольная сумма    |\n");
        writer.write( "+--------------------------------------------------------------------------+\n");




        // ищем каталоги с контейнерами
        File[] directoryList = ( new File( ABS_OUTPUT_DIR)).listFiles( pathname -> pathname.isDirectory());
        //
        // просматриваем каталоги типа FILES/out/....
        //
        for( File curDirList: directoryList){
            //
            // в каталоге должен быть файл data.zip и файлы-вложения
            // файл data.zip нужно подписать и зазиповать
            //
            logger.info("Обрабатываем каталог " + curDirList.getAbsolutePath());
            //
            // просматриваем содержимое каталога с контейнером
            //
            File[] currentDir = curDirList.listFiles( pathname -> pathname.isFile() && pathname.getName().startsWith("data") && pathname.getName().endsWith(".zip")) ;

            for( File curFile: currentDir){
                logger.info("обрабатываем файл " + curFile.getName() + " -> " + curFile.getAbsolutePath());
                writer.write( getFormattedStr( curFile));
            }
        }
        //
        // допишем контрольный файл
        //
        writer.write( getFormattedStr( new File( ABS_OUTPUT_DIR + "/control.zip" )));

        writer.write( "+--------------------------------------------------------------------------+\n");

        writer.newLine();
        writer.newLine();
        writer.newLine();
        writer.write("Наименование подразделения ведущего учет носителя _________________________________");

        writer.close();

        return result;

    }


    private static String getFormattedStr( File file) throws Exception{
        Path path = Paths.get(file.getAbsolutePath());
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
        FileInputStream fis = new FileInputStream( file);
        int fileSize = fis.available();
        byte[] buffer = new byte[ fileSize];
        CRC32 crc32 = new CRC32();

        fis.read(buffer);
        crc32.update(buffer);

        String str = String.format( "| %1$20s | %2$10s | %3$13s | %4$20s |\n", file.getName(), fileSize, attrs.creationTime().toString().substring( 0, 10), crc32.getValue());
        crc32.reset();
        fis.close();
        return str;
    }


}
