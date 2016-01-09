
package ru.mil.smb.wsdl.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://smb.mil.ru/wsdl/data}AbstractResponse"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="sessionUid" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sessionUid"
})
@XmlRootElement(name = "StartSessionResponse")
public class StartSessionResponse
    extends AbstractResponse
{

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
