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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.bioforsk.vips.i18n.I18nImpl;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class HTMLFormGenerator {
    
    private final I18nImpl i18n;
    
    // Template strings
    private static final String FIELD_PREFIX_TEMPLATE = "<div class=\"form-group\">\n<label for=\"{0}\">{1}</label>\n";
    private static final String FIELD_SUFFIX_TEMPLATE = "<span class=\"help-block\" id=\"{0}_{1}_validation\"></span>\n</div>\n";
    private static final String INPUT_FIELD_TEMPLATE = "<input class=\"form-control\" type=\"{0}\" onblur=\"validateField(this);\" placeholder=\"{1}\" name=\"{2}\" value=\"{3}\"/>\n";
    private static final String HIDDEN_FIELD_TEMPLATE = "<input type=\"hidden\" id=\"{0}\" name=\"{0}\" value=\"{1}\"/>\n";
    private static final String SELECT_FIELD_OPENTAG_TEMPLATE = "<select class=\"form-control\" name=\"{0}\" onblur=\"validateField(this);{1}\">\n";
    private static final String SELECT_FIELD_CLOSETAG_TEMPLATE = "</select>\n";
    private static final String OPTION_TEMPLATE = "<option value=\"{0}\"{1}>{2}</option>\n";
    
    public HTMLFormGenerator()
    {
        this.i18n = new I18nImpl("no.bioforsk.vips.logic.i18n.vipslogictexts");
    }
    
    /**
     * 
     * @param formDefintion
     * @param formId
     * @param language
     * @return 
     */
    public String getHTMLFormFields(String formDefintion, String formId, String language)
    {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return this.getHTMLFormFields(mapper.readTree(formDefintion), formId, language);
        } catch (IOException ex) {
            Logger.getLogger(HTMLFormGenerator.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * 
     * @param formDefintion
     * @param formId
     * @param language
     * @return 
     */
    public String getHTMLFormFields(JsonNode formDefinition, String formId, String language)
    {
        StringBuilder HTMLFormFields = new StringBuilder();
        List<FormField> fields =  new ObjectMapper().convertValue(formDefinition.findValue("fields"), new TypeReference<List<FormField>>(){});
        for(FormField field : fields)
        {
            HTMLFormFields.append(this.createFieldHTML(field, formId, language));
        }
        
        return HTMLFormFields.toString();
    }

    private Object createFieldHTML(FormField field, String formId, String language) {
        if(field.getFieldType().equals(FormField.FIELD_TYPE_HIDDEN))
        {
            return MessageFormat.format(HIDDEN_FIELD_TEMPLATE, field.getName(), field.getWebValue());
        }
        StringBuilder fieldHTML = new StringBuilder();
        fieldHTML.append(MessageFormat.format(FIELD_PREFIX_TEMPLATE, field.getName(),this.i18n.getText(field.getName(), language)));
        if(field.getFieldType().equals(FormField.FIELD_TYPE_SELECT_SINGLE))
        {
            fieldHTML.append(MessageFormat.format(SELECT_FIELD_OPENTAG_TEMPLATE,field.getName(),""));
            if(field.getOptions() != null){
                for(FormSelectOption option:field.getOptions())
                {
                    fieldHTML.append(MessageFormat.format(OPTION_TEMPLATE,option.getValue(),(option.isSelected() ? " selected=\"selected\"" : ""),option.getLabel()));
                }
            }
            fieldHTML.append(SELECT_FIELD_CLOSETAG_TEMPLATE);
        }
        else if(field.getFieldType().equals(FormField.FIELD_TYPE_SELECT_MULTIPLE))
        {
            fieldHTML.append(MessageFormat.format(SELECT_FIELD_OPENTAG_TEMPLATE,field.getName()," multiple=\"multiple\""));
            fieldHTML.append(MessageFormat.format(OPTION_TEMPLATE,"-1","Test"));
            fieldHTML.append(SELECT_FIELD_CLOSETAG_TEMPLATE);
        }
        else 
        {
            fieldHTML.append(MessageFormat.format(INPUT_FIELD_TEMPLATE, getHTMLInputType(field.getDataType()), this.i18n.getText(field.getName(), language), field.getName(), (field.getWebValue() != null ? field.getWebValue(): "")));
        }
        fieldHTML.append(MessageFormat.format(FIELD_SUFFIX_TEMPLATE, formId, field.getName()));
        return fieldHTML.toString();
    }

    private Object getHTMLInputType(String fieldType) {
        switch(fieldType)
        {
            case FormField.DATA_TYPE_STRING:
                return "text";
            case FormField.DATA_TYPE_INTEGER:
                return "number";
            case FormField.DATA_TYPE_DOUBLE:
                return "number";
            case FormField.DATA_TYPE_DATE:
                return "date";
            case FormField.DATA_TYPE_TIMESTAMP:
                return "datetime";
            case FormField.DATA_TYPE_PASSWORD:
                return "password";
            case FormField.DATA_TYPE_EMAIL:
                return "email";
            default:
                return "text";
        }
    }
}
