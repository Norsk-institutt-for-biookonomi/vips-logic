/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the Bioforsk Open Source License as published by 
 * Bioforsk, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Bioforsk Open Source License for more details.
 * 
 * You should have received a copy of the Bioforsk Open Source License
 * along with VIPSLogic.  If not, see <http://www.bioforsk.no/licenses/>.
 * 
 */

package no.bioforsk.web.forms;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * Represents a (web) form field
 * Life cycle:
 * <ol>
 *  <li>Parsed from JSON form definition file in [WARFILE]/formdefinitions</li>
 *  <li>Validated with FormValidator, added validation result values</li>
 *  <li>Sent back to web client in a FormValidation object</li>
 * </ol>
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class FormField {
    public final static String DATA_TYPE_STRING = "STRING";
    public final static String DATA_TYPE_INTEGER = "INTEGER";
    public final static String DATA_TYPE_DOUBLE = "DOUBLE";
    public final static String DATA_TYPE_DATE = "DATE";
    public final static String DATA_TYPE_TIMESTAMP = "TIMESTAMP";
    public final static String DATA_TYPE_PASSWORD = "PASSWORD";
    public final static String DATA_TYPE_EMAIL = "EMAIL";
    public final static String DATA_TYPE_POINT_WGS84 = "POINT_WGS84";
    
    public final static String FIELD_TYPE_INPUT = "INPUT";
    public final static String FIELD_TYPE_HIDDEN = "HIDDEN";
    public final static String FIELD_TYPE_SELECT_SINGLE = "SELECT_SINGLE";
    public final static String FIELD_TYPE_SELECT_MULTIPLE = "SELECT_MULTIPLE";
    // Multiple inputs for map. (e.g. <input type="text" name="relatedItem_25" value="Foo"/>,<input type="text" name="relatedItem_3" value="Bar"/>
    public final static String FIELD_TYPE_MULTIPLE_MAP = "MULTIPLE_MAP";

    // Standard format for dates is the ISO-8861-format
    public final static String DATE_DEFAULT_FORMAT = "yyyy-MM-dd";
    
    private String name;
    private String label;
    private String repeatField;
    private String dataType;
    private String fieldType;
    private boolean required;
    private String[] webValue;
    private Integer maxLength;
    private boolean valid;
    private String validationMessage;
    private Integer minValue;
    private Integer maxValue;
    private String nullValue;
    private String dateFormat;
    private String timestampFormat;
    private String timestampFormatMomentJS;
    private List<FormSelectOption> options;
    
    @JsonIgnore
    public Integer getValueAsInteger()
    {
        return Integer.valueOf(this.webValue[0]);
    }
    
    @JsonIgnore
    public Long getValueAsLong()
    {
        return Long.valueOf(this.webValue[0]);
    }
    
    @JsonIgnore
    public Double getValueAsDouble()
    {
        return Double.valueOf(this.webValue[0]);
    }
    
    @JsonIgnore
    public Date getValueAsDate()
    {
        try
        {
            return new SimpleDateFormat(this.getDateFormat()).parse(this.getWebValue());
        }
        catch(ParseException ex)
        {
            return null;
        }
    }
    
    @JsonIgnore
    public Date getValueAsTimestamp()
    {
        try
        {
            return new SimpleDateFormat(this.getTimestampFormat()).parse(this.getWebValue());
        }
        catch(ParseException ex)
        {
            return null;
        }
    }
    
    @JsonIgnore
    public Point getValueAsPointWGS84()
    {
        String[] xy = this.getWebValue().split(",");
        Coordinate coordinate = new Coordinate(Double.valueOf(xy[0]), Double.valueOf(xy[1]));
        GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);
        return gf.createPoint(coordinate);
    }
    
    
    /**
     * Default state is valid
     */
    public FormField()
    {
        this.valid = true;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the dataType
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * @param type the dataType to set
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * @return the required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * @param required the required to set
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * @return the webValue
     */
    public String getWebValue() {
        return webValue != null ? webValue[0] : null;
    }
    
    @JsonIgnore
    public String[] getWebValues()
    {
        return webValue;
    }

    /**
     * @param webValue the webValue to set
     */
    public void setWebValue(String... webValue) {
        this.webValue = webValue;
    }
    
    /**
     * @return the maxLength
     */
    @JsonIgnore
    public Integer getMaxLength() {
        return maxLength;
    }

    /**
     * @param maxLength the maxLength to set
     */
    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * @return the repeatField
     */
    @JsonIgnore
    public String getRepeatField() {
        return repeatField;
    }

    /**
     * @param repeatField the repeatField to set
     */
    public void setRepeatField(String repeatField) {
        this.repeatField = repeatField;
    }

    /**
     * @return the valid
     */
    @JsonIgnore
    public boolean isValid() {
        return valid;
    }

    /**
     * @param valid the valid to set
     */
    @JsonIgnore
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    /**
     * @return the validationMessage
     */
    @JsonIgnore
    public String getValidationMessage() {
        return validationMessage;
    }

    /**
     * @param validationMessage the validationMessage to set
     */
    @JsonIgnore
    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }

    /**
     * @return the label
     */
    @JsonIgnore
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    @JsonIgnore
    public void setLabel(String label) {
        this.label = label;
    }
    
    @Override
    public String toString()
    {
        StringBuilder retVal = new StringBuilder()
                .append(this.getClass().getSimpleName()).append(":\n")
                .append("Name=").append(this.getName()).append("\n")
                .append("WebValue=").append(this.getWebValue());
        
        return retVal.toString();
    }

    /**
     * @return the minValue
     */
    public Integer getMinValue() {
        return minValue;
    }

    /**
     * @param minValue the minValue to set
     */
    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    /**
     * @return the maxValue
     */
    public Integer getMaxValue() {
        return maxValue;
    }

    /**
     * @param maxValue the maxValue to set
     */
    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
    }

    /**
     * @return the nullValue
     */
    public String getNullValue() {
        return nullValue;
    }

    /**
     * @param nullValue the nullValue to set
     */
    public void setNullValue(String nullValue) {
        this.nullValue = nullValue;
    }

    /**
     * @return the dateFormat (if not set, it returns FormField.DATE_DEFAULT_FORMAT
     */
    public String getDateFormat() {
        if(this.dateFormat != null)
        {
            return dateFormat;
        }
        else
        {
            return DATE_DEFAULT_FORMAT;
        }
    }
    
    /**
     * 
     * @return Timestampformat (adds hour,minute,second and timezone to getDateFormat())
     */
    public String getTimestampFormat()
    {
        return this.timestampFormat != null ? this.timestampFormat: this.getDateFormat() + "'T'HH:mm:ssX";
    }
    
    public void setTimestampFormat(String timestampFormat)
    {
        this.timestampFormat = timestampFormat;
    }

    /**
     * @param dateFormat the dateFormat to set
     */
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * @return the options
     */
    public List<FormSelectOption> getOptions() {
        return options;
    }

    /**
     * @param options the options to set
     */
    public void setOptions(List<FormSelectOption> options) {
        this.options = options;
    }

    /**
     * @return the fieldType (default == FIELD_TYPE_INPUT)
     */
    public String getFieldType() {
        return fieldType != null ? fieldType : FIELD_TYPE_INPUT;
    }

    /**
     * @param fieldType the fieldType to set
     */
    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    /**
     * 
     * @return true if the value for this field is not set or is an empty string
     */
    public boolean isEmpty()
    {
        return this.getWebValue() == null || this.getWebValue().trim().isEmpty();
    }

    /**
     * @return the timestampFormatMomentJS
     */
    @JsonIgnore
    public String getTimestampFormatMomentJS() {
        return timestampFormatMomentJS;
    }

    /**
     * @param timestampFormatMomentJS the timestampFormatMomentJS to set
     */
    @JsonIgnore
    public void setTimestampFormatMomentJS(String timestampFormatMomentJS) {
        this.timestampFormatMomentJS = timestampFormatMomentJS;
    }

}

