package no.nibio.vips.observationdata;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import no.nibio.vips.logic.entity.Organism;

@Stateless
public class ObservationDataBean {
	@PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
	
	public JsonNode getSchema(Integer organizationId, Integer organismId, ResourceBundle bundle){
    	ObservationDataSchema ods = null;
    	ObjectMapper m = new ObjectMapper();
        try
        {
        	//System.out.println("organizationId = " + organizationId + ", organismId = " + organismId);
            ods = em.createNamedQuery("ObservationDataSchema.findByPK", ObservationDataSchema.class)
                .setParameter("organizationId", organizationId)
                .setParameter("organismId", organismId)
                .getSingleResult();
            
            
            
            // We iterate the schema, replacing default field labels with
            // translated ones
            // First: Convert to Jackson JsonNode tree
            JsonNode rootNode = m.readTree(ods.getDataSchema());
            if(bundle != null)
            {
	            Iterator<Entry<String, JsonNode>> nodeIterator = rootNode.fields();
	            
	            String fieldKeyPrefix = "observationDataField_";
	            // Loop through each field
	            while (nodeIterator.hasNext()) {
	                Map.Entry<String, JsonNode> schemaPropertyField = (Map.Entry<String, JsonNode>) nodeIterator.next();
	                // Get the property field key (e.g. "counting2")
	                String fieldKey = schemaPropertyField.getKey();
	                // Find a translation. 
	                if(bundle.containsKey(fieldKeyPrefix + fieldKey))
	                {
	                    // If found, replace with translation
	                    // Get the property field (e.g. {"title":"Counting 2"} )
	                    JsonNode schemaProperty = schemaPropertyField.getValue();
	                    ((ObjectNode)schemaProperty).put("title", bundle.getString(fieldKeyPrefix + fieldKey));
	                    ((ObjectNode)rootNode).replace(fieldKey, schemaProperty);
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
    
    public List<Organism> decoratePestsWithOrganismDataSchema(List<Organism> pests, Integer organizationId )
    {
    	try
    	{
	    	
	    	ObjectMapper om = new ObjectMapper();
	    	for(Organism pest:pests)
	    	{
	    		pest.setObservationDataSchema(om.writeValueAsString(this.getSchema(organizationId, pest.getOrganismId(), null)));
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
