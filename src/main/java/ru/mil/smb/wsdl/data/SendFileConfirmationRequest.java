
package ru.mil.smb.wsdl.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Подтверждение посылки файла данных
 * 
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://smb.mil.ru/wsdl/data}AbstractSessionRequest"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="fileLink" type="{http://smb.mil.ru/wsdl/data}fileMetaInfo"/&gt;
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
    "fileLink"
})
@XmlRootElement(name = "SendFileConfirmationRequest")
public class SendFileConfirmationRequest
    extends AbstractSessionRequest
{

    @XmlElement(required = true)
    protected FileMetaInfo fileLink;

    /**
     * Gets the value of the fileLink property.
     * 
     * @return
     *     possible object is
     *     {@link FileMetaInfo }
     *     
     */
    public FileMetaInfo getFileLink() {
        return fileLink;
    }

    /**
     * Sets the value of the fileLink property.
     * 
     * @param value
     *     allowed object is
     *     {@link FileMetaInfo }
     *     
     */
    public void setFileLink(FileMetaInfo value) {
        this.fileLink = value;
    }

}
