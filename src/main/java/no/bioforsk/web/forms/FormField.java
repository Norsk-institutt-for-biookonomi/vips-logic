/*
 * Copyright (c) 2013 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with VIPSLogic.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package no.bioforsk.web.forms;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.codehaus.jackson.annotate.JsonIgnore;

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
    public static String FORM_FIELD_TYPE_STRING = "STRING";
    public static String FORM_FIELD_TYPE_INTEGER = "INTEGER";
    public static String FORM_FIELD_TYPE_DOUBLE = "DOUBLE";
    public static String FORM_FIELD_TYPE_DATE = "DATE";
    public static String FORM_FIELD_TYPE_TIMESTAMP = "TIMESTAMP";
    public static String FORM_FIELD_TYPE_PASSWORD = "PASSWORD";
    public static String FORM_FIELD_TYPE_EMAIL = "EMAIL";
    public static String FORM_FIELD_TYPE_SELECT_SINGLE = "SELECT_SINGLE";
    public static String FORM_FIELD_TYPE_SELECT_MULTIPLE = "SELECT_MULTIPLE";

    // Standard format for dates is the ISO-8861-format
    public static String DATE_DEFAULT_FORMAT = "yyyy-MM-dd";
    
    private String name;
    private String label;
    private String repeatField;
    private String type;
    private boolean required;
    private String[] webValue;
    private Integer maxLength;
    private boolean valid;
    private String validationMessage;
    private Integer minValue;
    private Integer maxValue;
    private String nullValue;
    private String dateFormat;
    
    
    public Integer getValueAsInteger()
    {
        return Integer.valueOf(this.webValue[0]);
    }
    
    public Long getValueAsLong()
    {
        return Long.valueOf(this.webValue[0]);
    }
    
    public Double getValueAsDouble()
    {
        return Double.valueOf(this.webValue[0]);
    }
    
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
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
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
        return this.getDateFormat() + "'T'HH:mm:ssX";
    }

    /**
     * @param dateFormat the dateFormat to set
     */
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    
}
