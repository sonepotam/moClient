import org.w3c.dom.*;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.IOException;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;




/**
 * Created by pavel2107 on 14.09.15.
 */
public class moServiceSOAPHandler implements SOAPHandler<SOAPMessageContext> {
    private Signer fileSigner;


    private static final Logger logger = LogManager.getLogger( moServiceSOAPHandler.class.getName());

    moServiceSOAPHandler( Signer fileSigner){
        this.fileSigner = fileSigner;
    }


    public Set<QName> getHeaders() {
        return new TreeSet();
    }

    public boolean handleMessage(SOAPMessageContext context) {
        SOAPMessage message = context.getMessage();
        logger.debug("moServiceSOAPHandler started");
        Boolean outboundProperty =
                (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        try {
        if (outboundProperty.booleanValue()) {
            logger.debug("outbound message" + message);

                Document doc = message.getSOAPPart().getEnvelope().getOwnerDocument();
                logger.info( "Сообщение до подписи");
                Utils.prettyPrint( doc);
                logger.info( "Сообщение после подписи");
                fileSigner.signMessageNew(message);
                doc = message.getSOAPPart().getEnvelope().getOwnerDocument();
                Utils.prettyPrint( doc);
                logger.info( "========================================");
        } else {
            logger.info( "inbound message" + message);
            Document doc = message.getSOAPPart().getEnvelope().getOwnerDocument();
            Utils.prettyPrint( doc);
        }
        if( logger.isDebugEnabled()) message.writeTo( System.out);
        } catch (Exception e) {
            logger.error("Exception in handler: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    public boolean handleFault(SOAPMessageContext context) {
        logger.error(context.toString());
        SOAPMessage message = context.getMessage();
        logger.error("");
        logger.error("============ FAULT BEGIN ==================");
        try {
            message.writeTo(System.out);
        } catch (SOAPException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.error( "");
        logger.error("============ FAULT END ==================");
        return true;
    }

    public void close(MessageContext context) {
        //
    }
}
