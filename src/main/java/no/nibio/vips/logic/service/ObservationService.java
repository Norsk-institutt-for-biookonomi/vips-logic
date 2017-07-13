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

package no.nibio.vips.logic.service;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import no.nibio.vips.logic.entity.Observation;
import no.nibio.vips.logic.entity.VipsLogicRole;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.util.GISUtil;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.logic.util.SessionControllerGetter;
import org.jboss.resteasy.annotations.GZIP;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest/observation")
public class ObservationService {
    
    @Context
    private HttpServletRequest httpServletRequest;
    
    /**
     * NOTE TO SELF
     * How to query for observations within a bounding box
     * Select * from gis where ST_Intersects(
     *   ST_SetSRID(ST_MakeBox2D(ST_MakePoint(2.9004, 57.7511), ST_MakePoint(32.4316, 71.3851)),4326),
     *   gis_geom);
     * First point is SW, last is NE (but could be anything?)
     * @param organizationId
     * @param pestId
     * @param cropId
     * @param cropCategoryId
     * @param from
     * @param to
     * @return 
     */
    @GET
    @Path("filter/{organizationId}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    public Response getFilteredObservations(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("pestId") Integer pestId,
            @QueryParam("cropId") Integer cropId,
            @QueryParam("cropCategoryId") Integer cropCategoryId,
            @QueryParam("from") String fromStr,
            @QueryParam("to") String toStr
            
    )
    {
        SimpleDateFormat format = new SimpleDateFormat(Globals.defaultDateFormat);
        //TODO Set correct timeZone!!!
        Date from = null;
        Date to = null;
        try
        {
            from = fromStr != null ? format.parse(fromStr) : null;
            to = toStr != null ? format.parse(toStr) : null;
        }
        catch(ParseException ex){ System.out.println("ERROR");}
                
        List<Observation> filteredObservations = SessionControllerGetter.getObservationBean().getFilteredObservations(
            organizationId,
            pestId,
            cropId,
            cropCategoryId,
            from,
            to
        );
        return Response.ok().entity(filteredObservations).build();
    }
    
    @GET
    @Path("filter/{organizationId}/geoJSON")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    public Response getFilteredObservationsAsGeoJSON(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("pestId") Integer pestId,
            @QueryParam("cropId") Integer cropId,
            @QueryParam("cropCategoryId") Integer cropCategoryId,
            @QueryParam("from") String fromStr,
            @QueryParam("to") String toStr
            
    )
    {
        SimpleDateFormat format = new SimpleDateFormat(Globals.defaultDateFormat);
        //TODO Set correct timeZone!!!
        Date from = null;
        Date to = null;
        try
        {
            from = fromStr != null ? format.parse(fromStr) : null;
            to = toStr != null ? format.parse(toStr) : null;
        }
        catch(ParseException ex){ System.out.println("ERROR");}
                
        List<Observation> filteredObservations = SessionControllerGetter.getObservationBean().getFilteredObservations(
            organizationId,
            pestId,
            cropId,
            cropCategoryId,
            from,
            to
        );
        
        GISUtil gisUtil = new GISUtil();
        return Response.ok().entity(gisUtil.getGeoJSONFromObservations(filteredObservations)).build();
    }
    
    /**
     * Get a list of all observed pests for one organization
     * Practical for building effective select lists
     * TODO: Should be cached??
     * @param organizationId
     * @return 
     */
    @GET
    @Path("pest/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getObservedPests(@PathParam("organizationId") Integer organizationId)
    {
        return Response.ok().entity(SessionControllerGetter.getObservationBean().getObservedPests(organizationId)).build();
    }
    
    /**
     * Get a list of all crop cultures where observations have been made for one organization
     * Practical for building effective select lists
     * TODO: Should be cached??
     * @param organizationId
     * @return 
     */
    @GET
    @Path("crop/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getObservedCrops(@PathParam("organizationId") Integer organizationId)
    {
        return Response.ok().entity(SessionControllerGetter.getObservationBean().getObservedCrops(organizationId)).build();
    }
    
    
    /**
     * Publicly available observations per organization
     * @param organizationId
     * @return APPROVED observations
     */
    @GET
    @Path("list/{organizationId}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    public Response getObservations(@PathParam("organizationId") Integer organizationId){
        return Response.ok().entity(SessionControllerGetter.getObservationBean().getObservations(organizationId, Observation.STATUS_TYPE_ID_APPROVED)).build();
    }
    
    /**
     * Publicly available observations per organization
     * @param organizationId
     * @return APPROVED observations
     */
    @GET
    @Path("broadcast/list/{organizationId}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    public Response getBroadcastObservations(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("season") Integer season,
            @QueryParam("timeOfObservationFrom") String timeOfObservationFrom,
            @QueryParam("timeOfObservationTo") String timeOfObservationTo
    )
    {
        if((timeOfObservationFrom != null && ! timeOfObservationFrom.isEmpty()) 
                || (timeOfObservationTo != null && ! timeOfObservationTo.isEmpty()))
        {
            Date from = null;
            Date to = null;
            try
            {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                if(timeOfObservationFrom != null && ! timeOfObservationFrom.isEmpty())
                {
                    from = format.parse(timeOfObservationFrom);
                }
                if(timeOfObservationTo != null && ! timeOfObservationTo.isEmpty())
                {
                    to = format.parse(timeOfObservationTo);
                }
                return Response.ok().entity(SessionControllerGetter.getObservationBean().getBroadcastObservations(organizationId, from, to)).build();
            }
            catch(ParseException ex)
            {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid date format").build();
            }
        }
        else
        {
            return Response.ok().entity(SessionControllerGetter.getObservationBean().getBroadcastObservations(organizationId, season)).build();
        }
    }
    
    @GET
    @Path("{observationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getObservation(@PathParam("observationId") Integer observationId){
        return Response.ok().entity(SessionControllerGetter.getObservationBean().getObservation(observationId)).build();
    }
    
    /**
     * Deletes a gis entity and its corresponding observation
     */
    @DELETE
    @Path("gisobservation/{gisId}")
    public Response deleteGisObservation(@PathParam("gisId") Integer gisId)
    {
        VipsLogicUser user = (VipsLogicUser) httpServletRequest.getSession().getAttribute("user");
        // If no user, send error message back to client
        if(user == null)
        {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        if(!SessionControllerGetter.getUserBean().authorizeUser(user, 
                    VipsLogicRole.OBSERVER, 
                    VipsLogicRole.OBSERVATION_AUTHORITY, 
                    VipsLogicRole.ORGANIZATION_ADMINISTRATOR,
                    VipsLogicRole.SUPERUSER
                )
            )
        {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        SessionControllerGetter.getObservationBean().deleteGisObservationByGis(gisId);
        return Response.noContent().build();
    }
    
    /**
     * TODO Authentication
     * @param geoJSON
     * @return 
     */
    @POST
    @Path("gisobservation")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public Response storeGisObservation(String geoJSON)
    {
        try
        {
            // Create the Observation
            Observation observation = SessionControllerGetter.getObservationBean().getObservationFromGeoJSON(geoJSON);
            VipsLogicUser user = (VipsLogicUser) httpServletRequest.getSession().getAttribute("user");
            // If no user, send error message back to client
            if(user == null)
            {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            if(!SessionControllerGetter.getUserBean().authorizeUser(user, 
                    VipsLogicRole.OBSERVER, 
                    VipsLogicRole.OBSERVATION_AUTHORITY, 
                    VipsLogicRole.ORGANIZATION_ADMINISTRATOR,
                    VipsLogicRole.SUPERUSER
                )
            )
            {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
            observation.setUserId(user.getUserId());
            observation.setStatusChangedByUserId(user.getUserId());
            observation.setStatusChangedTime(new Date());
            observation = SessionControllerGetter.getObservationBean().storeObservation(observation);
            GISUtil gisUtil = new GISUtil();
            return Response.created(URI.create("/observation/" + observation.getObservationId())).entity(gisUtil.getGeoJSONFromObservation(observation)).build();
        }catch (IOException ex)
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex).build();
        }
    }

    
    
}
