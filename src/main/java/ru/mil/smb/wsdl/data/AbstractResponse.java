
package ru.mil.smb.wsdl.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * Ответ, содержащий код ошибки
 * 
 * <p>Java class for AbstractResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AbstractResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="operationResult" type="{http://smb.mil.ru/wsdl/data}ResultCodeEnum"/&gt;
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractResponse", propOrder = {
    "operationResult",
    "message"
})
@XmlSeeAlso({
    CancelSessionResponse.class,
    SendRequestConfirmationResponse.class,
    SendFileConfirmationResponse.class,
    GetRSAKeyResponse.class,
    StartSessionResponse.class
})
public abstract class AbstractResponse {

    protected int operationResult;
    protected String message;

    /**
     * Gets the value of the operationResult property.
     * 
     */
    public int getOperationResult() {
        return operationResult;
    }

    /**
     * Sets the value of the operationResult property.
     * 
     */
    public void setOperationResult(int value) {
        this.operationResult = value;
    }

    /**
     * Gets the value of the message property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessage(String value) {
        this.message = value;
    }

}
