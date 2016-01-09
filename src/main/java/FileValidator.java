import java.io.*;
import java.util.Properties;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;



public class FileValidator {

    private Properties properties;
    private static final Logger logger = LogManager.getLogger();

    public FileValidator( Properties properties){
        this.properties = properties;
    }

    public boolean validateOutFiles(){
        boolean result = true;
        final String ABS_OUTPUT_DIR = properties.getProperty("directory.ABS_OUTPUT_DIR");

        if( properties.getProperty("xsd.skipValidation").equalsIgnoreCase("true")){
            logger.info("Пропускаем синтаксическую проверку файлов");
            return true;
        }

        // ищем каталоги с контейнерами
        File[] directoryList = ( new File( ABS_OUTPUT_DIR)).listFiles( pathname -> pathname.isDirectory());
        //
        // в каждом каталоге с контейнером берем файл data.xml и проверяем его по схеме
        //
        String xsdIntegrationDataModel = properties.getProperty( "xsd.IntegrationDataModel");
        logger.info("Начинаем синтаксическую проверку файлов");
        String str = "";
        for( File currentDir: directoryList){
            String dataXML = currentDir + "/data.xml";
            logger.info( "Анализируем файл " + dataXML);
            str = validateFile( xsdIntegrationDataModel, dataXML);
            processCheckedFile( dataXML, str);
            if( !"".equals( str)) {
                result = false;
            }
        }
        //
        // проверяем control.xml
        //
        String xsdIntegrationControlModel = properties.getProperty( "xsd.IntegrationControlModel");
        str = validateFile( xsdIntegrationControlModel, ABS_OUTPUT_DIR + "/control.xml");
        processCheckedFile( ABS_OUTPUT_DIR + "/control.xml", str);
        if( !"".equals( str)) {
            result = false;
        }
        logger.info( "Проверка завершена");
        return result;
    }

    //
    // переименовываем файл с ошибками, чтобы избежать его отправки
    // если все ОК - выводим в протокол
    //
    private void processCheckedFile( String fileName, String messages){
        if( "".equals( messages)) {
            logger.info( "Файл " + fileName + " - ОК");
        }
        else{
            logger.error("Файл " + fileName + " содержит ошибки");
            logger.error( messages);
            //
            // переименовываем файл с ошибками
            //

            File errFile = new File( fileName);
            errFile.renameTo( new File( fileName + ".err"));
        }
    }

    //
    // проверка файла на соответствие xsd-схеме
    //
    public String validateFile( String schemaName, String fileName) {

        if( !( new File( fileName)).exists()){
            logger.error( "Нет файла " + fileName);
            return "";
        }

        SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        try {

            Schema schema = factory.newSchema(new File(schemaName));

            Validator validator = schema.newValidator();

            SoftErrorHandler softErrorHandler = new SoftErrorHandler();
            validator.setErrorHandler(softErrorHandler);

            Source source = new StreamSource(fileName);
            validator.validate(source);

            return softErrorHandler.getErrors();
        }
        catch ( Exception e){
            return e.getMessage();
        }
    }


}

class SoftErrorHandler implements ErrorHandler {
    private StringBuilder stringBuffer;
    SoftErrorHandler(){
        stringBuffer = new StringBuilder ();
    }

    boolean isValid(){
       return stringBuffer.length() == 0;
    }
    public String getErrors(){
        return stringBuffer.toString();
    }

    private String convertStr( String str){
        int ptr = str.indexOf( "lineNumber");
        if( ptr > 0) {
            str = str.substring( ptr);
        }
        System.out.println( str);
        return str;
    }
    public void fatalError( SAXParseException e ) throws SAXException {
        stringBuffer.append(convertStr(e.toString())).append( "\n");
    }
    public void error( SAXParseException e ) throws SAXException {
        stringBuffer.append(convertStr(e.toString())).append("\n");
    }
    public void warning( SAXParseException e ) throws SAXException {
        stringBuffer.append(convertStr(e.toString())).append("\n");
    }
}