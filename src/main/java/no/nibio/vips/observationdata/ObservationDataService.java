/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.observationdata;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.webcohesion.enunciate.metadata.Facet;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Facet("restricted")
@Path("rest/observationdata")
public class ObservationDataService {
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    @Context
    private HttpServletRequest httpServletRequest;
    


    /**
     * TODO Integrate with ObservationBean.getObservationDataSchema(...)
     * @param organizationId
     * @param organismId
     * @return 
     */
    @GET
    @Path("schema/{organizationId}/{organismId}")
    @Produces("application/json;charset=UTF-8")
    public Response getSchema(@PathParam("organizationId") Integer organizationId,@PathParam("organismId") Integer organismId){
        // Try to find schema for given organism/organization
        ObservationDataSchema ods = null;
        try
        {
            ods = em.createNamedQuery("ObservationDataSchema.findByPK", ObservationDataSchema.class)
                .setParameter("organizationId", organizationId)
                .setParameter("organismId", organismId)
                .getSingleResult();
            
            ResourceBundle bundle = SessionLocaleUtil.getI18nBundle(httpServletRequest);
            
            // We iterate the schema, replacing default field labels with
            // translated ones
            // First: Convert to Jackson JsonNode tree
            ObjectMapper m = new ObjectMapper();
            JsonNode rootNode = m.readTree(ods.getDataSchema());
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
            return Response.ok().entity(rootNode).build();
            
        }catch(IOException | NoResultException ex){}
        
        // If not found, return standard nominator/denominator (unit) form 
        return Response.ok().entity(ods != null ? ods.getDataSchema() : this.getStandardSchema()).build();
    }
    
    @GET
    @Path("model/{organizationId}/{organismId}")
    @Produces("application/json;charset=UTF-8")
    public Response getModel(@PathParam("organizationId") Integer organizationId,@PathParam("organismId") Integer organismId){
        
        // Try to find schema for given organism/organization
        ObservationDataSchema ods = null;
        try
        {
            ods = em.createNamedQuery("ObservationDataSchema.findByPK", ObservationDataSchema.class)
                .setParameter("organizationId", organizationId)
                .setParameter("organismId", organismId)
                .getSingleResult();
        }catch(NoResultException nre){}
        // If not found, return standard nominator/denominator (unit) form 
        return Response.ok().entity(ods != null ? ods.getDataModel() : this.getStandardModel()).build();
    }
    
    private String getStandardSchema(){
        return "{"
                + "\"number\":{\"title\":\"Number\"},"
                + "\"unit\":{\"title\":\"Unit\"}"
                + "}";
    }
    
    private String getStandardModel(){
        return "{"
                + "\"number\":0,"
                + "\"unit\":\"Number\""
                + "}";
    }
}
