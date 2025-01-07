package no.nibio.vips.observationdata;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ibm.icu.util.ULocale;

import no.nibio.vips.logic.controller.session.ObservationBean;
import no.nibio.vips.logic.controller.session.SessionControllerGetter;
import no.nibio.vips.logic.entity.Organism;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;

@Stateless
public class ObservationDataBean {
	@PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
	
	ObservationBean observationBean = SessionControllerGetter.getObservationBean();
	
	public JsonNode getSchema(Integer organizationId, Integer organismId, ResourceBundle bundle){
    	ObservationDataSchema ods = null;
    	ObjectMapper m = new ObjectMapper();
        try
        {
        	JsonNode rootNode = null;
        	try
        	{
	        	//System.out.println("organizationId = " + organizationId + ", organismId = " + organismId);
	            ods = em.createNamedQuery("ObservationDataSchema.findByPK", ObservationDataSchema.class)
	                .setParameter("organizationId", organizationId)
	                .setParameter("organismId", organismId)
	                .getSingleResult();
	            rootNode = m.readTree(ods.getDataSchema());
        	}
        	catch(NoResultException ex)
        	{
        		rootNode = m.readTree(this.getStandardSchema());
        	}
            
            
            // We iterate the schema, replacing default field labels with
            // translated ones
            if(bundle != null)
            {
	            Iterator<Entry<String, JsonNode>> nodeIterator = rootNode.get("properties").fields();
	            
	            String fieldKeyPrefix = "observationDataField_";
	            // Loop through each field
	            while (nodeIterator.hasNext()) {
	                Map.Entry<String, JsonNode> schemaPropertyField = (Map.Entry<String, JsonNode>) nodeIterator.next();
	                // Get the property field key (e.g. "counting2")
	                String fieldKey = schemaPropertyField.getKey();
	                //System.out.println(fieldKeyPrefix + fieldKey);
	                // Find a translation. 
	                if(bundle.containsKey(fieldKeyPrefix + fieldKey))
	                {
	                    // If found, replace with translation
	                    // Get the property field (e.g. {"title":"Counting 2"} )
	                    JsonNode schemaProperty = schemaPropertyField.getValue();
	                    ((ObjectNode)schemaProperty).put("title", bundle.getString(fieldKeyPrefix + fieldKey));
	                }
	            }
            }
            return rootNode;
            
        }catch(IOException | NoResultException ex){}
        
        // If not found, return standard nominator/denominator (unit) form 
        try {
			return m.readTree(getStandardSchema());
		} catch (IOException e) {
			return m.createObjectNode();
		}
    }

	public JsonNode getDataModel(Integer organizationId, Integer organismId)
	{
		ObjectMapper m = new ObjectMapper();

		JsonNode rootNode = null;
		try {
			try {
				//System.out.println("organizationId = " + organizationId + ", organismId = " + organismId);
				ObservationDataSchema ods = em.createNamedQuery("ObservationDataSchema.findByPK", ObservationDataSchema.class)
						.setParameter("organizationId", organizationId)
						.setParameter("organismId", organismId)
						.getSingleResult();
				rootNode = m.readTree(ods.getDataModel());
			} catch (NoResultException ex) {
				rootNode = m.readTree(this.getStandardModel());
			}
		}catch (JsonProcessingException ex)
		{
			return null;
		}
		return rootNode;
	}
    
	
	public String getStandardSchema(){
        return "{\n"
        		+ "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n"
        		+ "  \"type\": \"object\",\n"
        		+ "  \"title\": \" \",\n"
        		+ "  \"properties\": {"
                + "\"number\":{\"title\":\"Number\", \"type\":\"string\"},"
                + "\"unit\":{\"title\":\"Unit\", \"type\":\"string\"}"
                + "}}";
    }
    
    public String getStandardModel(){
        return "{"
                + "\"number\":0,"
                + "\"unit\":\"Number\""
                + "}";
    }
    
    public List<Organism> decoratePestsWithOrganismDataSchema(List<Organism> pests, Integer organizationId, HttpServletRequest httpServletRequest, ULocale locale)
    {
    	try
    	{
    		if(locale != null)
            {
            	SessionLocaleUtil.setCurrentLocale(httpServletRequest, locale);
            }
            ResourceBundle bundle = SessionLocaleUtil.getI18nBundle(httpServletRequest);
	    	ObjectMapper om = new ObjectMapper();
	    	for(Organism pest:pests)
	    	{
	    		pest.setObservationDataSchema(om.writeValueAsString(this.getSchema(organizationId, pest.getOrganismId(), bundle)));
				pest.setObservationData(om.writeValueAsString(this.getDataModel(organizationId, pest.getOrganismId())));
	    	}
	    	return pests;
    	}
    	catch(JsonProcessingException ex)
    	{
    		ex.printStackTrace();
    		return pests;
    	}
    }
}
