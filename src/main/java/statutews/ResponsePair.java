
package statutews;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for responsePair complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="responsePair"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="statuteKey" type="{http://statutews/}statuteKey" minOccurs="0"/&gt;
 *         &lt;element name="statutesBaseClass" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "responsePair", propOrder = {
    "statuteKey",
    "statutesBaseClass"
})
public class ResponsePair {

    protected StatuteKey statuteKey;
    protected Object statutesBaseClass;

    /**
     * Gets the value of the statuteKey property.
     * 
     * @return
     *     possible object is
     *     {@link StatuteKey }
     *     
     */
    public StatuteKey getStatuteKey() {
        return statuteKey;
    }

    /**
     * Sets the value of the statuteKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatuteKey }
     *     
     */
    public void setStatuteKey(StatuteKey value) {
        this.statuteKey = value;
    }

    /**
     * Gets the value of the statutesBaseClass property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getStatutesBaseClass() {
        return statutesBaseClass;
    }

    /**
     * Sets the value of the statutesBaseClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setStatutesBaseClass(Object value) {
        this.statutesBaseClass = value;
    }

}
