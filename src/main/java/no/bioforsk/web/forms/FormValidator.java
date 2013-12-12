package no.bioforsk.web.forms;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import no.bioforsk.vips.logic.authenticate.PasswordValidationException;
import no.bioforsk.vips.logic.i18n.SessionLocaleUtil;
import no.bioforsk.vips.logic.util.SessionControllerGetter;
import org.apache.commons.validator.EmailValidator;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 * Uses form configuration set in JSON files in [WARFILE]/formdefinitions/
 * This validator has its JavaScript counterpart in /js/validateForm.js (for 
 * client side validation). They use the same JSON file for form validation. 
 * Changes in logic server side should be reflected client side.
 * 
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class FormValidator {
    
    public static String RELATION_TYPE_EQUALS = "EQUALS";
    public static String RELATION_TYPE_AFTER = "AFTER";

    public static FormValidation validateForm(String formName, HttpServletRequest request, ServletContext servletContext) throws IOException {
       
        JsonNode formDefinition = getFormDefinition(formName, servletContext);
        List<FormField> fields =  new ObjectMapper().convertValue(formDefinition.findValue("fields"), new TypeReference<List<FormField>>(){});
        FormValidation retVal = new FormValidation();
        
        for(FormField field: fields)
        {
            retVal.addFormField(field);
            field.setLabel(SessionLocaleUtil.getI18nText(request, field.getName()));
            field.setWebValue(request.getParameterValues(field.getName()));
            if(field.getWebValue() == null || field.getWebValue().isEmpty())
            {
                if(field.isRequired())
                {
                    field.setValid(false);
                    field.setValidationMessage(SessionLocaleUtil.getI18nText(request, "fieldIsRequired"));
                }
                continue;
            }
            // STRINGS
            // Strings are checked for length
            if(field.getType().equals(FormField.FORM_FIELD_TYPE_STRING))
            {
                if(field.getMaxLength() != null && field.getMaxLength() < field.getWebValue().length())
                {
                    field.setValid(false);
                    field.setValidationMessage(MessageFormat.format(SessionLocaleUtil.getI18nText(request, "exceedsMaxLengthOf"), field.getMaxLength()));
                }
            }
            
            // Dates
            // We try to convert to date using the given format
            if(field.getType().equals(FormField.FORM_FIELD_TYPE_DATE))
            {
                SimpleDateFormat format = new SimpleDateFormat(field.getDateFormat());
                try
                {
                    Date date = format.parse(field.getWebValue());
                }
                catch(ParseException ex)
                {
                    field.setValid(false);
                    field.setValidationMessage(MessageFormat.format(SessionLocaleUtil.getI18nText(request, "doesNotMatchFormatX"), field.getDateFormat()));
                }
                
            }
            
            // Timestamps
            // We try to convert to date using the given format
            if(field.getType().equals(FormField.FORM_FIELD_TYPE_TIMESTAMP))
            {
                SimpleDateFormat format = new SimpleDateFormat(field.getTimestampFormat());
                try
                {
                    Date date = format.parse(field.getWebValue());
                }
                catch(ParseException ex)
                {
                    field.setValid(false);
                    field.setValidationMessage(MessageFormat.format(SessionLocaleUtil.getI18nText(request, "doesNotMatchFormatX"), field.getTimestampFormat()));
                }
                
            }
            
            if(field.getType().equals(FormField.FORM_FIELD_TYPE_PASSWORD))
            {
                try
                {
                    Locale currentLocale = SessionLocaleUtil.getCurrentLocale(request);
                    SessionControllerGetter.getUserBean().isPasswordValid(field.getWebValue(), currentLocale);
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
            if(field.getType().equals(FormField.FORM_FIELD_TYPE_EMAIL))
            {
                if(!EmailValidator.getInstance().isValid(field.getWebValue()))
                {
                    field.setValid(false);
                    field.setValidationMessage(SessionLocaleUtil.getI18nText(request, "invalidFormat"));
                }
               
            }
            
            // NUMBERS
            // Check if we can convert to double. In that case it's a number
            // TODO: What about doubles with comma separator? (e.g. 9,4 and not 9.4)??
            // TODO: INTEGERS should not have decimal values
            // If we have min or max constraints, check them
            if(
                field.getType().equals(FormField.FORM_FIELD_TYPE_INTEGER)
                || field.getType().equals(FormField.FORM_FIELD_TYPE_DOUBLE)
                )
            {
                try
                {
                    Double test = Double.valueOf(field.getWebValue());
                    if(field.getMinValue() != null && test < field.getMinValue())
                    {
                        field.setValid(false);
                        field.setValidationMessage(MessageFormat.format(SessionLocaleUtil.getI18nText(request, "lowerThanMinimum"), field.getMinValue()));
                    }
                    else if(field.getMaxValue() != null && test > field.getMaxValue())
                    {
                        field.setValid(false);
                        field.setValidationMessage(MessageFormat.format(SessionLocaleUtil.getI18nText(request, "higherThanMaximum"), field.getMaxValue()));
                    }
                }
                catch(NumberFormatException ex)
                {
                    field.setValid(false);
                    field.setValidationMessage(SessionLocaleUtil.getI18nText(request, "numberRequired"));
                }
            }
            
            // SELECT FIELDS
            // SINGLE SELECT
            // Check that selected option does not equal "nullValue"
            if(field.getType().equals(FormField.FORM_FIELD_TYPE_SELECT_SINGLE))
            {
                if(field.getWebValue().equals(field.getNullValue()))
                {
                    field.setValid(false);
                    field.setValidationMessage(SessionLocaleUtil.getI18nText(request, "fieldIsRequired"));
                }
            }
            
            // MULTIPLE SELECT
            // If required, check that at least one option is selected
            if(field.getType().equals(FormField.FORM_FIELD_TYPE_SELECT_SINGLE))
            {
                
            }
            
            // TODO: Add dates (and others??)
            
            

        }
        
        // Check repeats
        JsonNode relations = formDefinition.findValue("relations");
        if(relations != null)
        {
            for(JsonNode item: relations)
            {
                String relationType = item.findValue("relationType").getTextValue();
                String primaryFieldName = item.findValue("primaryField").getTextValue();
                String secondaryFieldName = item.findValue("secondaryField").getTextValue();
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
                                MessageFormat.format(SessionLocaleUtil.getI18nText(request, "xIsNotEqualToY"),
                                        SessionLocaleUtil.getI18nText(request, primaryFieldName),
                                        SessionLocaleUtil.getI18nText(request, secondaryFieldName)
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
                                MessageFormat.format(SessionLocaleUtil.getI18nText(request, "xIsNotAfterY"),
                                        SessionLocaleUtil.getI18nText(request, primaryFieldName),
                                        SessionLocaleUtil.getI18nText(request, secondaryFieldName)
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
        JsonFactory factory = mapper.getJsonFactory();
        InputStream in = servletContext.getResourceAsStream("/formdefinitions/" + formName + ".json");

        JsonParser parser = factory.createJsonParser(in);
        JsonNode formDefinition = mapper.readTree(parser);
        return formDefinition;
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
