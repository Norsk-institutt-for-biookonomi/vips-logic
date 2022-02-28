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

import com.ibm.icu.util.ULocale;
import com.webcohesion.enunciate.metadata.Facet;
import java.io.IOException;
import javax.ejb.EJB;
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
import no.nibio.vips.logic.controller.session.ObservationBean;
import no.nibio.vips.logic.controller.session.UserBean;

/**
 * @copyright 2016-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Facet("restricted")
@Path("rest/observationdata")
public class ObservationDataService {
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    @EJB
    UserBean userBean;
    @EJB
    ObservationBean observationBean;
    @EJB
    ObservationDataBean observationDataBean;
    
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
        
        try
        {
            // Try to find schema for given organism/organization
            ObservationDataSchema ods = observationBean.getObservationDataSchema(organizationId, organismId);
            ULocale locale = new ULocale(userBean.getOrganization(organizationId).getDefaultLocale());
            ods = observationBean.getLocalizedObservationDataSchema(ods, httpServletRequest, locale);
            return Response.ok().entity(ods.getDataSchema()).build();
            
        }
        catch(IOException ex)
        {
            return Response.serverError().entity(ex).build();
        }
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
        return Response.ok().entity(ods != null ? ods.getDataModel() : observationDataBean.getStandardModel()).build();
    }
    
    
}
