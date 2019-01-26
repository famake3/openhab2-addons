/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.02.17 at 06:25:15 PM CET 
//


package org.openhab.ui.cometvisu.internal.config.beans;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for gauge complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="gauge"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="layout" type="{}layout" minOccurs="0"/&gt;
 *         &lt;element name="label" type="{}label" minOccurs="0"/&gt;
 *         &lt;element name="address" type="{}address" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="type"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="Radial"/&gt;
 *             &lt;enumeration value="RadialBargraph"/&gt;
 *             &lt;enumeration value="RadialVertical"/&gt;
 *             &lt;enumeration value="Linear"/&gt;
 *             &lt;enumeration value="LinearBargraph"/&gt;
 *             &lt;enumeration value="DisplaySingle"/&gt;
 *             &lt;enumeration value="DisplayMulti"/&gt;
 *             &lt;enumeration value="Level"/&gt;
 *             &lt;enumeration value="Compass"/&gt;
 *             &lt;enumeration value="WindDirection"/&gt;
 *             &lt;enumeration value="Horizon"/&gt;
 *             &lt;enumeration value="Led"/&gt;
 *             &lt;enumeration value="Clock"/&gt;
 *             &lt;enumeration value="Battery"/&gt;
 *             &lt;enumeration value="StopWatch"/&gt;
 *             &lt;enumeration value="Altimeter"/&gt;
 *             &lt;enumeration value="TrafficLight"/&gt;
 *             &lt;enumeration value="LightBulb"/&gt;
 *             &lt;enumeration value="Odometer"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="subtype"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="type1"/&gt;
 *             &lt;enumeration value="type2"/&gt;
 *             &lt;enumeration value="type3"/&gt;
 *             &lt;enumeration value="type4"/&gt;
 *             &lt;enumeration value="type5"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="titleString" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="unitString" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="unitStringVisible" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="minValue" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="maxValue" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="size" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="threshold" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *       &lt;attribute name="thresholdRising" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="ledVisible" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="lcdVisible" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="trendVisible" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="autoScroll" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="valuesNumeric" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="lcdDecimals" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" /&gt;
 *       &lt;attribute name="background"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="DARK_GRAY"/&gt;
 *             &lt;enumeration value="SATIN_GRAY"/&gt;
 *             &lt;enumeration value="LIGHT_GRAY"/&gt;
 *             &lt;enumeration value="WHITE"/&gt;
 *             &lt;enumeration value="BLACK"/&gt;
 *             &lt;enumeration value="BEIGE"/&gt;
 *             &lt;enumeration value="BROWN"/&gt;
 *             &lt;enumeration value="RED"/&gt;
 *             &lt;enumeration value="GREEN"/&gt;
 *             &lt;enumeration value="BLUE"/&gt;
 *             &lt;enumeration value="ANTHRACITE"/&gt;
 *             &lt;enumeration value="MUD"/&gt;
 *             &lt;enumeration value="PUNCHED_SHEET"/&gt;
 *             &lt;enumeration value="CARBON"/&gt;
 *             &lt;enumeration value="STAINLESS"/&gt;
 *             &lt;enumeration value="BRUSHED_METAL"/&gt;
 *             &lt;enumeration value="BRUSHED_STAINLESS"/&gt;
 *             &lt;enumeration value="TURNED"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="framedesign"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="BLACK_METAL"/&gt;
 *             &lt;enumeration value="METAL"/&gt;
 *             &lt;enumeration value="SHINY_METAL"/&gt;
 *             &lt;enumeration value="BRASS"/&gt;
 *             &lt;enumeration value="STEEL"/&gt;
 *             &lt;enumeration value="CHROME"/&gt;
 *             &lt;enumeration value="GOLD"/&gt;
 *             &lt;enumeration value="ANTHRACITE"/&gt;
 *             &lt;enumeration value="TILTED_GRAY"/&gt;
 *             &lt;enumeration value="TILTED_BLACK"/&gt;
 *             &lt;enumeration value="GLOSSY_METAL"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="width" type="{}dimension" /&gt;
 *       &lt;attribute name="height" type="{}dimension" /&gt;
 *       &lt;attribute name="valueColor"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="RED"/&gt;
 *             &lt;enumeration value="GREEN"/&gt;
 *             &lt;enumeration value="BLUE"/&gt;
 *             &lt;enumeration value="ORANGE"/&gt;
 *             &lt;enumeration value="YELLOW"/&gt;
 *             &lt;enumeration value="CYAN"/&gt;
 *             &lt;enumeration value="MAGENTA"/&gt;
 *             &lt;enumeration value="WHITE"/&gt;
 *             &lt;enumeration value="GRAY"/&gt;
 *             &lt;enumeration value="BLACK"/&gt;
 *             &lt;enumeration value="RAITH"/&gt;
 *             &lt;enumeration value="GREEN_LCD"/&gt;
 *             &lt;enumeration value="JUG_GREEN"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute ref="{}mapping"/&gt;
 *       &lt;attribute ref="{}target"/&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "gauge", propOrder = {
    "layout",
    "label",
    "address"
})
public class Gauge {

    protected Layout layout;
    protected Label label;
    @XmlElement(required = true)
    protected List<Address> address;
    @XmlAttribute(name = "type")
    protected String type;
    @XmlAttribute(name = "subtype")
    protected String subtype;
    @XmlAttribute(name = "titleString")
    protected String titleString;
    @XmlAttribute(name = "unitString")
    protected String unitString;
    @XmlAttribute(name = "unitStringVisible")
    protected Boolean unitStringVisible;
    @XmlAttribute(name = "minValue")
    protected BigDecimal minValue;
    @XmlAttribute(name = "maxValue")
    protected BigDecimal maxValue;
    @XmlAttribute(name = "size")
    protected BigDecimal size;
    @XmlAttribute(name = "threshold")
    protected BigDecimal threshold;
    @XmlAttribute(name = "thresholdRising")
    protected Boolean thresholdRising;
    @XmlAttribute(name = "ledVisible")
    protected Boolean ledVisible;
    @XmlAttribute(name = "lcdVisible")
    protected Boolean lcdVisible;
    @XmlAttribute(name = "trendVisible")
    protected Boolean trendVisible;
    @XmlAttribute(name = "autoScroll")
    protected Boolean autoScroll;
    @XmlAttribute(name = "valuesNumeric")
    protected Boolean valuesNumeric;
    @XmlAttribute(name = "lcdDecimals")
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger lcdDecimals;
    @XmlAttribute(name = "background")
    protected String background;
    @XmlAttribute(name = "framedesign")
    protected String framedesign;
    @XmlAttribute(name = "width")
    protected String width;
    @XmlAttribute(name = "height")
    protected String height;
    @XmlAttribute(name = "valueColor")
    protected String valueColor;
    @XmlAttribute(name = "mapping")
    protected String mapping;
    @XmlAttribute(name = "target")
    protected String target;

    /**
     * Gets the value of the layout property.
     * 
     * @return
     *     possible object is
     *     {@link Layout }
     *     
     */
    public Layout getLayout() {
        return layout;
    }

    /**
     * Sets the value of the layout property.
     * 
     * @param value
     *     allowed object is
     *     {@link Layout }
     *     
     */
    public void setLayout(Layout value) {
        this.layout = value;
    }

    /**
     * Gets the value of the label property.
     * 
     * @return
     *     possible object is
     *     {@link Label }
     *     
     */
    public Label getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     * 
     * @param value
     *     allowed object is
     *     {@link Label }
     *     
     */
    public void setLabel(Label value) {
        this.label = value;
    }

    /**
     * Gets the value of the address property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the address property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAddress().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Address }
     * 
     * 
     */
    public List<Address> getAddress() {
        if (address == null) {
            address = new ArrayList<Address>();
        }
        return this.address;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the subtype property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSubtype() {
        return subtype;
    }

    /**
     * Sets the value of the subtype property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSubtype(String value) {
        this.subtype = value;
    }

    /**
     * Gets the value of the titleString property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTitleString() {
        return titleString;
    }

    /**
     * Sets the value of the titleString property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTitleString(String value) {
        this.titleString = value;
    }

    /**
     * Gets the value of the unitString property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnitString() {
        return unitString;
    }

    /**
     * Sets the value of the unitString property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnitString(String value) {
        this.unitString = value;
    }

    /**
     * Gets the value of the unitStringVisible property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isUnitStringVisible() {
        return unitStringVisible;
    }

    /**
     * Sets the value of the unitStringVisible property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUnitStringVisible(Boolean value) {
        this.unitStringVisible = value;
    }

    /**
     * Gets the value of the minValue property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMinValue() {
        return minValue;
    }

    /**
     * Sets the value of the minValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMinValue(BigDecimal value) {
        this.minValue = value;
    }

    /**
     * Gets the value of the maxValue property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMaxValue() {
        return maxValue;
    }

    /**
     * Sets the value of the maxValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMaxValue(BigDecimal value) {
        this.maxValue = value;
    }

    /**
     * Gets the value of the size property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getSize() {
        return size;
    }

    /**
     * Sets the value of the size property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setSize(BigDecimal value) {
        this.size = value;
    }

    /**
     * Gets the value of the threshold property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getThreshold() {
        return threshold;
    }

    /**
     * Sets the value of the threshold property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setThreshold(BigDecimal value) {
        this.threshold = value;
    }

    /**
     * Gets the value of the thresholdRising property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isThresholdRising() {
        return thresholdRising;
    }

    /**
     * Sets the value of the thresholdRising property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setThresholdRising(Boolean value) {
        this.thresholdRising = value;
    }

    /**
     * Gets the value of the ledVisible property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isLedVisible() {
        return ledVisible;
    }

    /**
     * Sets the value of the ledVisible property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLedVisible(Boolean value) {
        this.ledVisible = value;
    }

    /**
     * Gets the value of the lcdVisible property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isLcdVisible() {
        return lcdVisible;
    }

    /**
     * Sets the value of the lcdVisible property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLcdVisible(Boolean value) {
        this.lcdVisible = value;
    }

    /**
     * Gets the value of the trendVisible property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isTrendVisible() {
        return trendVisible;
    }

    /**
     * Sets the value of the trendVisible property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTrendVisible(Boolean value) {
        this.trendVisible = value;
    }

    /**
     * Gets the value of the autoScroll property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAutoScroll() {
        return autoScroll;
    }

    /**
     * Sets the value of the autoScroll property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAutoScroll(Boolean value) {
        this.autoScroll = value;
    }

    /**
     * Gets the value of the valuesNumeric property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isValuesNumeric() {
        return valuesNumeric;
    }

    /**
     * Sets the value of the valuesNumeric property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setValuesNumeric(Boolean value) {
        this.valuesNumeric = value;
    }

    /**
     * Gets the value of the lcdDecimals property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getLcdDecimals() {
        return lcdDecimals;
    }

    /**
     * Sets the value of the lcdDecimals property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setLcdDecimals(BigInteger value) {
        this.lcdDecimals = value;
    }

    /**
     * Gets the value of the background property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBackground() {
        return background;
    }

    /**
     * Sets the value of the background property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBackground(String value) {
        this.background = value;
    }

    /**
     * Gets the value of the framedesign property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFramedesign() {
        return framedesign;
    }

    /**
     * Sets the value of the framedesign property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFramedesign(String value) {
        this.framedesign = value;
    }

    /**
     * Gets the value of the width property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWidth(String value) {
        this.width = value;
    }

    /**
     * Gets the value of the height property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHeight() {
        return height;
    }

    /**
     * Sets the value of the height property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHeight(String value) {
        this.height = value;
    }

    /**
     * Gets the value of the valueColor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValueColor() {
        return valueColor;
    }

    /**
     * Sets the value of the valueColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValueColor(String value) {
        this.valueColor = value;
    }

    /**
     * Gets the value of the mapping property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMapping() {
        return mapping;
    }

    /**
     * Sets the value of the mapping property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMapping(String value) {
        this.mapping = value;
    }

    /**
     * Gets the value of the target property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTarget() {
        return target;
    }

    /**
     * Sets the value of the target property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTarget(String value) {
        this.target = value;
    }

}
