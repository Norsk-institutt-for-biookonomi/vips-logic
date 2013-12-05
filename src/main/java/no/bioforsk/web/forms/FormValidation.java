package no.bioforsk.web.forms;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class FormValidation {

    private Map<String,FormField> formFields;
    

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
    
}
