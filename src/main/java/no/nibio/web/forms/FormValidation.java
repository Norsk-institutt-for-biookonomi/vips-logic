/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the NIBIO Open Source License as published by 
 * NIBIO, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * NIBIO Open Source License for more details.
 * 
 * You should have received a copy of the NIBIO Open Source License
 * along with VIPSLogic.  If not, see <http://www.nibio.no/licenses/>.
 * 
 */

package no.nibio.web.forms;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class FormValidation {

    private Map<String,FormField> formFields;
    
    private Map<String,Map<String,FormField>> multipleMapFormFields;

    /**
     * @return the valid If at least one field is not valid
     */
    public Boolean isValid() {
        for(FormField field: this.getFormFields().values())
        {
            if(!field.isValid())
            {
                return false;
            }
        }
        return true;
    }
    
    public String getValidationMessages()
    {
        StringBuilder retVal = new StringBuilder();
        for(FormField field: this.getFormFields().values())
        {
            if(!field.isValid())
            {
                retVal.append(field.getLabel()).append(": ").append(field.getValidationMessage()).append("\n");
            }
        }
        return retVal.toString();
    }

    public void addFormField(FormField field)
    {
        
        this.getFormFields().put(field.getName(), field);
    }

    public FormField getFormField(String fieldName)
    {
        return this.getFormFields().get(fieldName);
    }
    /**
     * @return the formFields
     */
    public Map<String,FormField> getFormFields() {
        if(this.formFields == null)
        {
            this.formFields = new HashMap<>();
        }
        return formFields;
    }

    /**
     * @param formFields the formFields to set
     */
    public void setFormFields(Map<String,FormField> formFields) {
        this.formFields = formFields;
    }

    /**
     * @return the multipleMapFormFields
     */
    public Map<String,Map<String,FormField>> getMultipleMapFormFields() {
        if(this.multipleMapFormFields == null)
        {
            this.multipleMapFormFields = new HashMap<>();
        }
        return multipleMapFormFields;
    }

    /**
     * @param multipleMapFormFields the multipleMapFormFields to set
     */
    public void setMultipleMapFormFields(Map<String,Map<String,FormField>> multipleMapFormFields) {
        this.multipleMapFormFields = multipleMapFormFields;
    }
    
    public void addMultipleMapFormField(String fieldName, String key, FormField formField)
    {
        if(this.multipleMapFormFields == null)
        {
            this.multipleMapFormFields = new HashMap<>();
        }
        Map<String, FormField> fieldMap = this.multipleMapFormFields.get(fieldName);
        if(fieldMap == null)
        {
            fieldMap = new HashMap<>();
            this.multipleMapFormFields.put(fieldName, fieldMap);
        }
        fieldMap.put(key, formField);
    }
}
