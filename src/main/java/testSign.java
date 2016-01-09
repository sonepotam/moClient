import java.io.FileInputStream;
import java.io.FileReader;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;

public class testSign {
    private static final Logger logger = LogManager.getLogger(testSign.class.getName());


    public static void main(String[] args) throws Exception {
        //
        // читаем настройки
        //
        logger.info("Запускается интеграционное ПО для связи с министерством обороны");
        logger.info("Читаем настройкм");

        Properties configProperties = new Properties();
        configProperties.load(new FileReader("config.properties"));


        /*
        SOAPMessage message = MessageFactory.
                                newInstance().
                                createMessage(null, new FileInputStream("C:\\work\\testSign\\message.xml"));
        Document doc = message.getSOAPPart().getEnvelope().getOwnerDocument();
        System.out.println( "==================== before ================================");
        Utils.prettyPrint( doc);
        final Signer fileSigner = Signer.getInstance( configProperties);

        System.out.println( "===================== auto ===============================");
        fileSigner.signMessageNew( message);
        doc = message.getSOAPPart().getEnvelope().getOwnerDocument();
        Utils.prettyPrint( doc);


        */

        List paramList = new ArrayList<>();
        if( args != null) paramList = Arrays.asList( args);
        //
        // проверяем входящие файлы на синтаксическую правильность
        //
        FileValidator fileValidator = new FileValidator( configProperties);
        boolean validated = fileValidator.validateOutFiles();
        if( paramList.contains( "validateonly") || !validated) return;
        //
        // создаем объекты
        //
        final Signer fileSigner = Signer.getInstance( configProperties);
        moServiceClient serviceClient = moServiceClient.getInstance( configProperties, fileSigner);
        //
        // запуск движка
        //
        boolean worked = serviceClient.integrator();
        logger.info( "Обмен файлами с министерством обороны" +
                ( worked ? "" : " НЕ " ) +  " успешно завершен");
        if( !worked) {
            System.out.println( "Отправка не успешна. Сформировать опись[y/n] ?");
            char ch = (char) System.in.read();
            //
            // формируем опись...
            //
            if( ch == 'y'){
                DirectoryScanner scanner = new FileDirectoryScanner( configProperties, fileSigner);
                scanner.processOutDirectory();
                Utils.createDisc(configProperties);
                scanner.clearDirectories();
            }
        }

        logger.info( "Работа программы завершена");
        System.exit(0);
    }
}

