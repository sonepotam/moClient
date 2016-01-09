/**
 * Created by pavel2107 on 25.09.15.
 */
import ru.mil.smb.wsdl.data.*;

import javax.jws.WebParam;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//
// центр сертификации у МО дохлый, периодически вываливается по ошибке,
// если немного подождать и запустить метод снова, то все работает
// этот класс повторяет вызовы несколько раз... :)
//
public class ServicePortDecorator implements MoSoapGateway {

    private static final Logger logger = LogManager.getLogger( ServicePortDecorator.class.getName());


    private MoSoapGateway port;
    private int timeout;
    private int retries;


    public ServicePortDecorator( MoSoapGateway port, int timeout, int retries){
        this.port    = port;
        this.timeout = timeout;
        this.retries = retries;
    }

    private String getMethodPoint(){
        return Thread.currentThread().getStackTrace()[2].getClassName() + '.' + Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    public GetRSAKeyResponse getRSAKey(@WebParam(partName = "GetRSAKeyRequest", name = "GetRSAKeyRequest", targetNamespace = "http://smb.mil.ru/wsdl/data")
                                       GetRSAKeyRequest getRSAKeyRequest) {
        GetRSAKeyResponse getRSAKeyResponse = null;
        for( int i = 0; i < retries; i++){
            logger.info( "Запускаем " + getMethodPoint());
            getRSAKeyResponse = port.getRSAKey(getRSAKeyRequest);
            logResponse( getRSAKeyResponse);
            if( getRSAKeyResponse.getKey() != null) break;
            if( !waitAMinute( "Ключ RSA получить не удалось.")) break;
        }
        logger.info( "Завершен вызов " + getMethodPoint());
        return getRSAKeyResponse;
    }



    public SendRequestConfirmationResponse sendRequestConfirmation(@WebParam(partName = "SendRequestConfirmationRequest", name = "SendRequestConfirmationRequest", targetNamespace = "http://smb.mil.ru/wsdl/data")
                                                                   SendRequestConfirmationRequest sendRequestConfirmationRequest) {
        SendRequestConfirmationResponse sendRequestConfirmationResponse = null;
        for( int i = 0; i < retries; i++){
            logger.info( "Запускаем " + getMethodPoint());
            sendRequestConfirmationResponse = port.sendRequestConfirmation( sendRequestConfirmationRequest);
            logResponse(sendRequestConfirmationResponse);
            if( sendRequestConfirmationResponse.getOperationResult() == 0) break;
            if( !waitAMinute( "Подтверждение отправки получить не удалось.")) break;
        }
        logger.info( "Завершен вызов " + getMethodPoint());
        return sendRequestConfirmationResponse;
    }

    public StartSessionResponse startSession(@WebParam(partName = "StartSessionRequest", name = "StartSessionRequest", targetNamespace = "http://smb.mil.ru/wsdl/data")
                                             StartSessionRequest startSessionRequest) {
        StartSessionResponse startSessionResponse = null;
        for( int i = 0; i < retries; i++){
            logger.info( "Запускаем " + getMethodPoint());
            startSessionResponse = port.startSession( startSessionRequest);
            logResponse( startSessionResponse);
            if( startSessionResponse.getSessionUid() != null) break;
            if( !waitAMinute( "Идентификатор сессии получить не удалось.")) break;
        }
        logger.info( "Завершен вызов " + getMethodPoint());
        return startSessionResponse;
    }

    public CancelSessionResponse cancelSession(@WebParam(partName = "CancelSessionRequest", name = "CancelSessionRequest", targetNamespace = "http://smb.mil.ru/wsdl/data")
                                               CancelSessionRequest cancelSessionRequest) {
        CancelSessionResponse cancelSessionResponse = null;

        for( int i = 0; i < retries; i++){
            logger.info( "Запускаем " + getMethodPoint());
            cancelSessionResponse = port.cancelSession(cancelSessionRequest);
            logResponse( cancelSessionResponse);
            if( cancelSessionResponse.getOperationResult() == 0) break;
            if( !waitAMinute( "Корректный ответ получить не удалось.")) break;
        }
        logger.info( "Завершен вызов " + getMethodPoint());

        return cancelSessionResponse;
    }

    public SendFileConfirmationResponse sendFileConfirmation(@WebParam(partName = "SendFileConfirmationRequest", name = "SendFileConfirmationRequest", targetNamespace = "http://smb.mil.ru/wsdl/data")
                                                             SendFileConfirmationRequest sendFileConfirmationRequest) {
        SendFileConfirmationResponse  sendFileConfirmationResponse = null;
        for( int i = 0; i < retries; i++){
            logger.info( "Запускаем " + getMethodPoint());
            sendFileConfirmationResponse = port.sendFileConfirmation(sendFileConfirmationRequest);
            logResponse( sendFileConfirmationResponse);
            if( sendFileConfirmationResponse.getOperationResult() == 0) break;
            if( !waitAMinute( "Корректный ответ получить не удалось.")) break;
        }
        logger.info( "Завершен вызов " + getMethodPoint());
        return sendFileConfirmationResponse;
    }

    //
    // запись в лог результат вызова
    //
    private void logResponse( AbstractResponse response){
        logger.info( "");
        logger.info( "message = " + response.getMessage());
        logger.info("code="       + response.getOperationResult());
        if( response instanceof StartSessionResponse ) {
            logger.info("uid=" + ((StartSessionResponse) response).getSessionUid());
        }
    }

    //
    // ожидание ответа в цикле
    //
    private boolean waitAMinute( String str){
        boolean result = true;
        try{
            logger.info( str + " Ждем " + timeout + " секунд...");
            Thread.sleep( timeout * 1000);
        }
        catch ( Exception e){
            result = false;
            logger.info( e.getMessage());
            e.printStackTrace();
        }
        return result;
    }


}
