
package ru.mil.smb.wsdl.data;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ru.mil.smb.wsdl.data package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ru.mil.smb.wsdl.data
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link StartSessionRequest }
     * 
     */
    public StartSessionRequest createStartSessionRequest() {
        return new StartSessionRequest();
    }

    /**
     * Create an instance of {@link StartSessionResponse }
     * 
     */
    public StartSessionResponse createStartSessionResponse() {
        return new StartSessionResponse();
    }

    /**
     * Create an instance of {@link GetRSAKeyRequest }
     * 
     */
    public GetRSAKeyRequest createGetRSAKeyRequest() {
        return new GetRSAKeyRequest();
    }

    /**
     * Create an instance of {@link GetRSAKeyResponse }
     * 
     */
    public GetRSAKeyResponse createGetRSAKeyResponse() {
        return new GetRSAKeyResponse();
    }

    /**
     * Create an instance of {@link SendFileConfirmationRequest }
     * 
     */
    public SendFileConfirmationRequest createSendFileConfirmationRequest() {
        return new SendFileConfirmationRequest();
    }

    /**
     * Create an instance of {@link FileMetaInfo }
     * 
     */
    public FileMetaInfo createFileMetaInfo() {
        return new FileMetaInfo();
    }

    /**
     * Create an instance of {@link SendFileConfirmationResponse }
     * 
     */
    public SendFileConfirmationResponse createSendFileConfirmationResponse() {
        return new SendFileConfirmationResponse();
    }

    /**
     * Create an instance of {@link SendRequestConfirmationRequest }
     * 
     */
    public SendRequestConfirmationRequest createSendRequestConfirmationRequest() {
        return new SendRequestConfirmationRequest();
    }

    /**
     * Create an instance of {@link SendRequestConfirmationResponse }
     * 
     */
    public SendRequestConfirmationResponse createSendRequestConfirmationResponse() {
        return new SendRequestConfirmationResponse();
    }

    /**
     * Create an instance of {@link CancelSessionRequest }
     * 
     */
    public CancelSessionRequest createCancelSessionRequest() {
        return new CancelSessionRequest();
    }

    /**
     * Create an instance of {@link CancelSessionResponse }
     * 
     */
    public CancelSessionResponse createCancelSessionResponse() {
        return new CancelSessionResponse();
    }

}
