/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.observationdata;

import com.ibm.icu.util.ULocale;
import com.webcohesion.enunciate.metadata.Facet;
import java.io.IOException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import no.nibio.vips.logic.controller.session.ObservationBean;
import no.nibio.vips.logic.controller.session.UserBean;

/**
 * @copyright 2016-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Facet("restricted")
@ApplicationScoped
@Path("observationdata")
public class ObservationDataService {
    @Inject
    EntityManager em;
    
    @Inject
    UserBean userBean;
    @Inject
    ObservationBean observationBean;
    @Inject
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
