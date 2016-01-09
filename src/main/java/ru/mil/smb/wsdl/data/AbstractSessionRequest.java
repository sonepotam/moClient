
package ru.mil.smb.wsdl.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * Запрос с кодом сессии
 * 
 * <p>Java class for AbstractSessionRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AbstractSessionRequest"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="sessionUid" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AbstractSessionRequest", propOrder = {
    "sessionUid"
})
@XmlSeeAlso({
    CancelSessionRequest.class,
    SendRequestConfirmationRequest.class,
    SendFileConfirmationRequest.class
})
public abstract class AbstractSessionRequest {

    @XmlElement(required = true)
    protected String sessionUid;

    /**
     * Gets the value of the sessionUid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSessionUid() {
        return sessionUid;
    }

    /**
     * Sets the value of the sessionUid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSessionUid(String value) {
        this.sessionUid = value;
    }

}
