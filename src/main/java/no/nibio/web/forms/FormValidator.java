/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package no.nibio.web.forms;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.util.ULocale;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import no.nibio.vips.logic.authenticate.PasswordValidationException;
import no.nibio.vips.logic.controller.session.SessionControllerGetter;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.LoggerFactory;

/**
 * Uses form configuration set in JSON files in [WARFILE]/formdefinitions/, or 
 * form configurations sent directly as strings 
 * 
 * This validator has its JavaScript counterpart in /js/validateForm.js (for 
 * client side validation). They use the same JSON file for form validation. 
 * Changes in logic server side should be reflected client side.
 * 
 * @copyright 2013-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class FormValidator {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FormValidator.class);
    
    @EJB
    UserBean userBean;
    
    public static String RELATION_TYPE_EQUALS = "EQUALS";
    public static String RELATION_TYPE_AFTER = "AFTER";

    /**
     * 
     * @param formName
     * @param request
     * @param servletContext
     * @return
     * @throws IOException
     * @throws FormValidationException 
     */
    public static FormValidation validateForm(String formName, HttpServletRequest request, ServletContext servletContext) throws IOException, FormValidationException {
        JsonNode formDefinition = getFormDefinition(formName, servletContext);
        return validateForm(formDefinition,  request.getParameterMap(), SessionLocaleUtil.getI18nBundle(request));
    }
    
    /**
     * 
     * @param formName
     * @param servletContext
     * @param parameterMap
     * @return
     * @throws IOException
     * @throws FormValidationException 
     */
    public static FormValidation validateForm(String formName, Map<String,String[]> parameterMap, ResourceBundle resourceBundle, ServletContext servletContext) throws IOException, FormValidationException {
        //System.out.println("Parametermap=" + parameterMap.toString());
        //System.out.println("Number of messageTagIds submitted:" + parameterMap.get("messageTagIds").length);
        JsonNode formDefinition = getFormDefinition(formName, servletContext);
        return validateForm(formDefinition, parameterMap, resourceBundle);
        
    }
    
    
    /**
     * 
     * @param formDefinitionStr
     * @param request
     * @return
     * @throws IOException
     * @throws FormValidationException 
     */
    public static FormValidation validateForm(String formDefinitionStr, Map<String, String[]> parameterMap, ResourceBundle resourceBundle) throws IOException, FormValidationException
    {
        JsonNode formDefinition = getFormDefinition(formDefinitionStr);
        return validateForm(formDefinition, parameterMap, resourceBundle);
    }
    
    /**
     * 
     * @param formDefinition
     * @param request
     * @param parameterMap
     * @return
     * @throws FormValidationException 
     */
    public static FormValidation validateForm(JsonNode formDefinition, Map<String, String[]> parameterMap, ResourceBundle resourceBundle) throws FormValidationException
    {
        List<FormField> fields =  new ObjectMapper().convertValue(formDefinition.findValue("fields"), new TypeReference<List<FormField>>(){});
        FormValidation retVal = new FormValidation();
        
        // If one of the fields is a multiple map, we must get them from the form and add them
        List<FormField> multipleMapFields = new ArrayList<>();
        for(FormField field: fields)
        {
            if(field.getFieldType().equals(FormField.FIELD_TYPE_MULTIPLE_MAP))
            {
                // By convention, these fields always end with "_*", where * denotes the key.
                // So we loop through the requests and stores all that matches (begins with) fieldName
                
                for(String parameterName: parameterMap.keySet())
                {
                    if(parameterName.startsWith(field.getName()))
                    {
                        FormField newField = new FormField();
                        newField.setName(parameterName);
                        newField.setDataType(field.getDataType());
                        newField.setRequired(field.isRequired());
                        newField.setWebValue(parameterMap.get(parameterName));
                        multipleMapFields.add(newField);
                        retVal.addMultipleMapFormField(field.getName(), parameterName.substring(field.getName().length() + 1), newField);
                    }
                }
            }
        }
        fields.addAll(multipleMapFields);
        
        for(FormField field: fields)
        {
            // Check for NULL fieldType
            if(field.getDataType() == null)
            {
                throw new FormValidationException("Missing datatype definition for field " + field.getName());
            }
            
            // Skip multiple map fields, as they've been exploded in the loop above.
            if(field.getFieldType().equals(FormField.FIELD_TYPE_MULTIPLE_MAP))
            {
                continue;
            }
 
            retVal.addFormField(field);
            try
            {
                field.setLabel(resourceBundle.getString(field.getName()));
            }
            catch(MissingResourceException ex)
            {
                field.setLabel(field.getName());
            }
            
            field.setWebValue(parameterMap.get(field.getName()));
            
            if(field.getWebValue() == null || field.getWebValue().isEmpty())
            {
                if(field.isRequired())
                {
                    field.setValid(false);
                    field.setValidationMessage(resourceBundle.getString("fieldIsRequired"));
                }
                continue;
            }
            // STRINGS
            // Strings are checked for length
            if(field.getDataType().equals(FormField.DATA_TYPE_STRING))
            {
                if(field.getMaxLength() != null && field.getMaxLength() < field.getWebValue().length())
                {
                    field.setValid(false);
                    field.setValidationMessage(MessageFormat.format(resourceBundle.getString("exceedsMaxLengthOf"), field.getMaxLength()));
                }
            }
            
            // Dates
            // We try to convert to date using the given format
            if(field.getDataType().equals(FormField.DATA_TYPE_DATE))
            {
                SimpleDateFormat format = new SimpleDateFormat(field.getDateFormat());
                try
                {
                    Date date = format.parse(field.getWebValue());
                }
                catch(ParseException ex)
                {
                    field.setValid(false);
                    field.setValidationMessage(MessageFormat.format(resourceBundle.getString("doesNotMatchFormatX"), field.getDateFormat()));
                }
                
            }
            
            // Timestamps
            // We try to convert to date using the given format
            if(field.getDataType().equals(FormField.DATA_TYPE_TIMESTAMP))
            {
                SimpleDateFormat format = new SimpleDateFormat(field.getTimestampFormat());
                try
                {
                    Date date = format.parse(field.getWebValue());
                }
                catch(ParseException ex)
                {
                    field.setValid(false);
                    field.setValidationMessage(MessageFormat.format(resourceBundle.getString("doesNotMatchFormatX"), field.getTimestampFormat()));
                }
                
            }
            
            if(field.getDataType().equals(FormField.DATA_TYPE_PASSWORD))
            {
                try
                {
                    SessionControllerGetter.getUserBean().isPasswordValid(field.getWebValue(), ULocale.forLocale(resourceBundle.getLocale()));
                }
                catch(PasswordValidationException ex)
                {
                    field.setValid(false);
                    field.setValidationMessage(ex.getMessage());
                }
            }
            
            // EMAILS
            // Check email format
            // Using Apache Commons Validator
            if(field.getDataType().equals(FormField.DATA_TYPE_EMAIL))
            {
                if(!EmailValidator.getInstance().isValid(field.getWebValue()))
                {
                    field.setValid(false);
                    field.setValidationMessage(resourceBundle.getString("invalidFormat"));
                }
               
            }
            
            // NUMBERS
            // Check if we can convert to double. In that case it's a number
            // TODO: What about doubles with comma separator? (e.g. 9,4 and not 9.4)??
            // TODO: INTEGERS should not have decimal values
            // If we have min or max constraints, check them
            if(
                field.getDataType().equals(FormField.DATA_TYPE_INTEGER)
                || field.getDataType().equals(FormField.DATA_TYPE_DOUBLE)
                )
            {
                try
                {
                    Double test = Double.valueOf(field.getWebValue());
                    if(field.getMinValue() != null && test < field.getMinValue())
                    {
                        field.setValid(false);
                        field.setValidationMessage(MessageFormat.format(resourceBundle.getString("lowerThanMinimum"), field.getMinValue()));
                    }
                    else if(field.getMaxValue() != null && test > field.getMaxValue())
                    {
                        field.setValid(false);
                        field.setValidationMessage(MessageFormat.format(resourceBundle.getString("higherThanMaximum"), field.getMaxValue()));
                    }
                }
                catch(NumberFormatException ex)
                {
                    field.setValid(false);
                    field.setValidationMessage(resourceBundle.getString("numberRequired"));
                }
            }
            
            // GIS
            // Point in WGS84 format
            // Format [Double], [Double]
            // TODO Check for minimum and maximum values
            if(field.getDataType().equals(FormField.DATA_TYPE_POINT_WGS84))
            {
                String[] possibleDoubles = field.getWebValue().split(",");
                if(possibleDoubles.length != 2)
                {
                    field.setValid(false);
                    if(possibleDoubles.length < 2)
                    {
                        field.setValidationMessage(resourceBundle.getString("missingSeparatorComma"));
                    }
                    else
                    {
                        field.setValidationMessage(resourceBundle.getString("tooManySeparatorCommas"));
                    }
                }
                else
                {       
                    try
                    {
                        for(String possibleDouble:possibleDoubles)
                        {
                            Double actualDouble = Double.valueOf(possibleDouble);
                        }
                    }
                    catch(NumberFormatException ex)
                    {
                        field.setValid(false);
                        field.setValidationMessage("invalidFormat");
                    }
                }
            }
            // GEOJSON
            if(field.getDataType().equals(FormField.DATA_TYPE_GEOJSON))
            {
                if((field.getWebValue().equals("{}") || field.getWebValue().equals(field.getNullValue())) && field.isRequired())
                {
                    field.setValid(false);
                    field.setValidationMessage(resourceBundle.getString("fieldIsRequired"));
                }
            }
            
            // SELECT FIELDS
            // SINGLE SELECT
            // Check that selected option does not equal "nullValue"
            if(field.getFieldType().equals(FormField.FIELD_TYPE_SELECT_SINGLE))
            {
                if(field.getWebValue().equals(field.getNullValue()) && field.isRequired())
                {
                    field.setValid(false);
                    field.setValidationMessage(resourceBundle.getString("fieldIsRequired"));
                    LOGGER.debug(field.getName() + " with a value of " + field.getWebValue() + " is considered to be NULL");
                }
            }
            
            // MULTIPLE SELECT
            // TODO: Implement check!!
            // If required, check that at least one option is selected
            if(field.getFieldType().equals(FormField.FIELD_TYPE_SELECT_SINGLE))
            {
                
            }
            
            
            
            

        }
        
        // Check repeats
        JsonNode relations = formDefinition.findValue("relations");
        if(relations != null)
        {
            for(JsonNode item: relations)
            {
                String relationType = item.findValue("relationType").asText();
                String primaryFieldName = item.findValue("primaryField").asText();
                String secondaryFieldName = item.findValue("secondaryField").asText();
                FormField primaryField = retVal.getFormField(primaryFieldName);
                FormField secondaryField = retVal.getFormField(secondaryFieldName);
                if(primaryField == null || secondaryField == null)
                {
                    continue;
                }
                
                // Repetition of strings
                if(relationType.equals(RELATION_TYPE_EQUALS))
                {
                    if(!primaryField.getWebValue().equals(secondaryField.getWebValue()))
                    {
                        primaryField.setValid(false);
                        primaryField.setValidationMessage(
                                MessageFormat.format(resourceBundle.getString("xIsNotEqualToY"),
                                        resourceBundle.getString(primaryFieldName),
                                        resourceBundle.getString(secondaryFieldName)
                                        )
                        );
                    }
                }
                // Ordering of dates
                else if(relationType.equals(RELATION_TYPE_AFTER))
                {
                    if(primaryField.getValueAsDate().compareTo(secondaryField.getValueAsDate()) < 0)
                    {
                        primaryField.setValid(false);
                        primaryField.setValidationMessage(
                                MessageFormat.format(resourceBundle.getString("xIsNotAfterY"),
                                        resourceBundle.getString(primaryFieldName),
                                        resourceBundle.getString(secondaryFieldName)
                                        )
                        );
                    }
                }
            }
        }
        
        return retVal;
    }
    
    /**
     * Fetches all fields and their constrants from form definition
     * @param formName name of JSON file with form definition
     * @param servletContext
     * @return
     * @throws IOException 
     */
    public static JsonNode getFormDefinition(String formName, ServletContext servletContext) throws IOException
    {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();
        InputStream in = servletContext.getResourceAsStream("/formdefinitions/" + formName + ".json");

        JsonParser parser = factory.createParser(in);
        JsonNode formDefinition = mapper.readTree(parser);
        return formDefinition;
    }
    
    public static JsonNode getFormDefinition(String formDefinitionStr) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(formDefinitionStr);
    }
    
    /**
     * 
     * @param formName
     * @param servletContext
     * @return all form fields form the form definition, indexed by their name
     * @throws IOException 
     */
    public static Map<String, FormField> getFormFields(String formName, ServletContext servletContext) throws IOException
    {
        JsonNode formDefinition = getFormDefinition(formName, servletContext);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode fields = formDefinition.findValue("fields");
        Map<String, FormField> retVal = new HashMap<>();
        for(JsonNode fieldNode:fields)
        {
            FormField formField = mapper.convertValue(fieldNode, FormField.class);
            retVal.put(formField.getName(), formField);
        }
        
        return retVal;
    }

    

}
