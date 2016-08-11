
package statutews;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import codesparser.SectionNumber;


/**
 * <p>Java class for statuteView complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="statuteView"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="refCount" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="sectionNumber" type="{http://codesparser}sectionNumber" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "statuteView", propOrder = {
    "refCount",
    "sectionNumber"
})
public class StatuteView {

    protected int refCount;
    protected SectionNumber sectionNumber;

    /**
     * Gets the value of the refCount property.
     * 
     */
    public int getRefCount() {
        return refCount;
    }

    /**
     * Sets the value of the refCount property.
     * 
     */
    public void setRefCount(int value) {
        this.refCount = value;
    }

    /**
     * Gets the value of the sectionNumber property.
     * 
     * @return
     *     possible object is
     *     {@link SectionNumber }
     *     
     */
    public SectionNumber getSectionNumber() {
        return sectionNumber;
    }

    /**
     * Sets the value of the sectionNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link SectionNumber }
     *     
     */
    public void setSectionNumber(SectionNumber value) {
        this.sectionNumber = value;
    }

}
