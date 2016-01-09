import CMS_samples.CMS;
import CMS_samples.CMSSign;
import CMS_samples.CMSVerify;
import CMS_samples.CMStools;
import com.sun.org.apache.xml.internal.security.transforms.Transforms;
import org.w3c.dom.Document;

import org.w3c.dom.NodeList;
import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.JCPxml.xmldsig.JCPXMLDSigInit;

import java.io.File;
import java.io.FileOutputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import java.io.FileInputStream;
import java.security.PrivateKey;
import java.util.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Created by pavel2107 on 11.09.15.
 *
 * из спецификации поля MUST !!!
 *
 * OID = 1.2.840.113549.1.9.3           тип содержимого
 * OID = 1.2.840.113549.1.9.16.2.47     сертификат подписанта
 * OID = 1.2.840.113549.1.9.4           хеш сообщения
 *
 * их надо добавлять в блок signedAttrs точно производитель не в курсе :)
 *
 */
public class Signer {
    //
    // logger
    //
    private static final Logger logger = LogManager.getLogger( Signer.class.getName());
    //
    // свойства для работы
    //
    Properties properties;
    //
    // для обычного провайдера
    //
    KeyStore keyStore;
    PrivateKey privateKey;
    X509Certificate cert;
    String ALIAS;
    String PASSWORD;

    private XMLSignatureFactory fac;
    Provider xmlDSigProvider;



    private static Signer signer = null;

    public static Signer getInstance( Properties p){
        if( signer == null){
            signer = new Signer( p);
        }
        return signer;
    }


    private Signer( Properties configProperties) {
        this.properties = configProperties;
        //
        // инициализация крипто-провайдера
        //
        logger.info("Загружаем криптопровайдер");
        // Инициализация Transforms.
        com.sun.org.apache.xml.internal.security.Init.init();
        // Инициализация сервис-провайдера.
        if (!JCPXMLDSigInit.isInitialized()) {
            JCPXMLDSigInit.init();
        }
        logger.info("Криптопровайдер загружен");


        ALIAS = properties.getProperty("crypto.key.alias");
        PASSWORD = properties.getProperty("crypto.key.password");


        logger.info("Загружаем ключевую информацию");
        try {
            //
            // Инициализация ключевого контейнера и получение сертификата и закрытого ключа.
            //
            keyStore = KeyStore.getInstance(properties.getProperty("crypto.key.imagestore"));
            keyStore.load(null, null);
            //
            // загружаем ключи
            //
            privateKey = (PrivateKey) keyStore.getKey(ALIAS, PASSWORD.toCharArray());
            cert = (X509Certificate) keyStore.getCertificate(ALIAS);
            //
            //  выводим ключи в протокол
            //
            logger.debug(privateKey.toString());
            logger.debug(cert.toString());

            xmlDSigProvider = new ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI();
            fac = XMLSignatureFactory.getInstance("DOM", xmlDSigProvider);


        }
        catch ( Exception e){
            logger.error( e.getMessage());
            e.printStackTrace();
            System.exit( 1); // приплыли
        }
    }

    public void signMessageNew( SOAPMessage message)throws Exception{

        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        SOAPFactory factory = SOAPFactory.newInstance();

        final String NAMESPACEURI_WSSECURITY_WSU=
                "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";


        Document doc = message.getSOAPPart().getEnvelope().getOwnerDocument();
        // добавление заголовков


        SOAPBody body = envelope.getBody();
        body.addAttribute(new QName(NAMESPACEURI_WSSECURITY_WSU, "Id", "wsu"), "body");



        String prefix = "wsse";
        String uri = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";


        SOAPElement securityElem =
                factory.createElement("Security", "wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");

        securityElem.addAttribute(new QName("xmlns:wsu"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");

        SOAPElement binarySecurityToken = factory.createElement("BinarySecurityToken", "wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
        binarySecurityToken.addAttribute(new QName( "EncodingType"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");
        binarySecurityToken.addAttribute(new QName("ValueType"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");
        securityElem.addChildElement(binarySecurityToken);
        SOAPHeader header = envelope.getHeader();
        header.addChildElement(securityElem);

        //.. Преобразования над узлом ds:SignedInfo:
        List<Transform> transformList = new ArrayList<Transform>();
        Transform transformC14N = fac.newTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS, (XMLStructure) null);
        transformList.add(transformC14N);


        // Ссылка на подписываемые данные с алгоритмом хеширования ГОСТ 34.11.
        Reference ref = fac.newReference("#body", fac.newDigestMethod("http://www.w3.org/2001/04/xmldsig-more#gostr3411", null), transformList, null, null);

        // задаем алгоритм подписи
        SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE,
                (C14NMethodParameterSpec) null), fac.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#gostr34102001-gostr3411", null), Collections.singletonList(ref));


        KeyInfoFactory kif = fac.getKeyInfoFactory();
        X509Data x509d = kif.newX509Data(Collections.singletonList((X509Certificate) cert));
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(x509d));

        // подписываем данные в элементе token
        XMLSignature sig = fac.newXMLSignature(si, ki);

        Element xmlEnvelope = getFirstChildElement( doc);
        Element xmlHeader = getFirstChildElement(envelope);

        // DOMSignContext signContext = new DOMSignContext((Key) privateKey, doc.getDocumentElement());
        DOMSignContext signContext = new DOMSignContext((Key) privateKey, xmlHeader);
        sig.sign(signContext);

        NodeList list = doc.getElementsByTagName("Signature");
        org.w3c.dom.Node signatureElement = list.item(0);

        list = doc.getElementsByTagName( "X509Certificate");
        org.w3c.dom.Node x509CertElement = list.item( 0);

        org.w3c.dom.Node securityToken = doc.getElementsByTagName("wsse:BinarySecurityToken").item(0);
        securityToken.setTextContent(x509CertElement.getTextContent());

        //testSign.prettyPrint( doc);
        // удаляем элемент Signature
      //  envelope.removeChild(signatureElement);

    }



    public Set<QName> getHeaders() {
        return new TreeSet();
    }



    public void signMessage( SOAPMessage message)throws Exception{
        //
        // получаем сообщение
        //
        SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
        SOAPFactory factory = SOAPFactory.newInstance();
        //
        // получаем документ как DOM - документ
        //
        Document doc = message.getSOAPPart().getEnvelope().getOwnerDocument();
        //
        // добавляем элемент Security
        //
        SOAPElement securityElem =
                factory.createElement("Security", "wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");

        securityElem.addAttribute(new QName("xmlns:wsu"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
        //
        // создаем binarySecurityToken
        //
        SOAPElement binarySecurityToken = factory.createElement("BinarySecurityToken", "wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
        binarySecurityToken.addAttribute(new QName( "EncodingType"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");
        binarySecurityToken.addAttribute(new QName("ValueType"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3");
        //
        // добавляем binarySecurityToken в заголовок сообщения
        //
        securityElem.addChildElement(binarySecurityToken);
        SOAPHeader header = envelope.getHeader();
        header.addChildElement(securityElem);
        //
        // подставляем сертификат в binarySecurityToken
        //
        org.w3c.dom.Node securityToken = doc.getElementsByTagName("wsse:BinarySecurityToken").item(0);
        securityToken.setTextContent( new String( Base64.getEncoder().encodeToString( cert.getEncoded()).getBytes()) );
    }


    public void cades( String fileName) throws Exception{
        //
        // не создаем файлы с подписями - за нас это сделал КРИПТО-АРМ
        //
        if( properties.getProperty( "crypto.skip").equals( "yes")) {
            //
            // проверяем есть ли файл, если нет - надо создавать КРИПТО_АРМом
            File sigFile = new File( fileName + ".sig");
            while( !sigFile.exists()){
                System.out.println("Создайте файл " + fileName + ".sig" + " и нажмите любую клавишу для продолжения...");
                 System.in.read();
            }
            sigFile.renameTo( new File( fileName.substring(0, fileName.length() - 3) + "sign"));
            sigFile = new File( fileName.substring(0, fileName.length() - 3) + "sign");
            //
            // читаем файл
            //
            FileInputStream fis = new FileInputStream( sigFile);
            byte[] buffer = new byte[ fis.available()];
            fis.read( buffer);
            fis.close();
            //
            // преобразуем в url-encoding без переносов строк и =
            //
            String str = urlEncoding( new String( buffer));
            //
            // записываем обратно в файл
            //
            sigFile.delete();
            FileOutputStream fos = new FileOutputStream( sigFile);
            fos.write( str.getBytes());
            fos.close();


            return;
        };
        //
        // читаем файл с данными
        //
        byte[] cms = Array.readFile( fileName);
        PrivateKey[] keys = new PrivateKey[]{ privateKey};
        Certificate[] certs = new Certificate[]{ cert};
        //
        // создаем подпись в формате CaDES-BES
        //
        String cadesFileName = fileName + ".cades";
        CMSSign.createHashCMSEx(CMStools.digestm(cms, CMStools.DIGEST_ALG_NAME), true, keys, certs, cadesFileName, true, true);
        //
        // Проверяем CAdES-BES подпись.
        //
        byte[] signdata = Array.readFile(cadesFileName);
        CMSVerify.CMSVerify(signdata, certs, cms);
        //
        // преобразуем в формат url-encoding
        //
        String str = Base64.getEncoder().encodeToString( signdata);
        signdata   = urlEncoding( str).getBytes();
        Array.writeFile( fileName.substring( 0, fileName.length() - 3) + "sign", signdata);
        //
        // удаляем cades
        //
        (new File( cadesFileName)).delete();
    }

    private static Signature readAndHash(Signature signature, String fileName) throws Exception {

        File file = new File(fileName);
        FileInputStream fData = new FileInputStream(file);
        byte[] buffer = new byte[ fData.available()];
        fData.read( buffer);
        signature.update( buffer);
        fData.close();
        return signature;
    }


    private static Element getFirstChildElement(Node node) {
        Node child = node.getFirstChild();
        while ((child != null) && (child.getNodeType() != Node.ELEMENT_NODE)) {
            child = child.getNextSibling();
        }
        return (Element) child;
    }


    public static String urlEncoding(String base64){
        StringBuffer sb = new StringBuffer( base64.length() - 2);
        for( char ch : base64.toCharArray()){
             if( ch == '+'){ ch = '-';}
             if( ch == '/') { ch = '_';}
             if( ch != '=' && ch != 13 && ch != 10)  sb.append( ch);
        };
        return sb.toString();
    }



}
