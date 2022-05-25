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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.util.ULocale;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import no.nibio.vips.logic.controller.session.ObservationBean;
import no.nibio.vips.logic.controller.session.OrganismBean;
import no.nibio.vips.logic.controller.session.UserBean;

import no.nibio.vips.logic.entity.Gis;
import no.nibio.vips.logic.entity.Observation;
import no.nibio.vips.logic.entity.ObservationIllustrationPK;
import no.nibio.vips.logic.entity.ObservationStatusType;
import no.nibio.vips.logic.entity.ObservationSyncInfo;
import no.nibio.vips.logic.entity.PolygonService;
import no.nibio.vips.logic.entity.VipsLogicRole;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.entity.rest.ObservationListItem;
import no.nibio.vips.logic.entity.rest.PointMappingResponse;
import no.nibio.vips.logic.entity.rest.ReferencedPoint;
import no.nibio.vips.logic.messaging.MessagingBean;
import no.nibio.vips.logic.util.GISEntityUtil;
import no.nibio.vips.logic.util.Globals;
import org.jboss.resteasy.annotations.GZIP;
import org.wololo.geojson.Feature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @copyright 2016-2021 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest/observation")
public class ObservationService {
    
    private static Logger LOGGER = LoggerFactory.getLogger(ObservationService.class);
    
    @Context
    private HttpServletRequest httpServletRequest;
    
    @EJB
    UserBean userBean;
    @EJB
    ObservationBean observationBean;
    @EJB
    OrganismBean organismBean;
    @EJB
    MessagingBean messagingBean;
    
    /*
     * NOTE TO SELF
     * How to query for observations within a bounding box
     * Select * from gis where ST_Intersects(
     *   ST_SetSRID(ST_MakeBox2D(ST_MakePoint(2.9004, 57.7511), ST_MakePoint(32.4316, 71.3851)),4326),
     *   gis_geom);
     * First point is SW, last is NE (but could be anything?)
    */
    
    /**
     * 
     * @param organizationId
     * @param pestId
     * @param cropId
     * @param cropCategoryId
     * @param from
     * @param to
     * @return Observation objects with all data (full tree)
     */
    @GET
    @Path("filter/{organizationId}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    public Response getFilteredObservations(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("pestId") Integer pestId,
            @QueryParam("cropId") Integer cropId,
            @QueryParam("cropCategoryId") List<Integer> cropCategoryId,
            @QueryParam("from") String fromStr,
            @QueryParam("to") String toStr
            
    )
    {
        return Response.ok().entity(getFilteredObservationsFromBackend(
                organizationId,
                pestId,
                cropId,
                cropCategoryId,
                fromStr,
                toStr
        )).build();
    }
    
    /**
     * 
     * @param organizationId
     * @param pestId
     * @param cropId
     * @param cropCategoryId
     * @param from
     * @param to
     * @return Observation objects for which the user is authorized to observe with properties relevant for lists
     */
    @GET
    @Path("list/filter/{organizationId}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    public Response getFilteredObservationListItems(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("pestId") Integer pestId,
            @QueryParam("cropId") Integer cropId,
            @QueryParam("cropCategoryId") List<Integer> cropCategoryId,
            @QueryParam("from") String fromStr,
            @QueryParam("to") String toStr,
            @QueryParam("userUUID") String userUUID,
            @QueryParam("locale") String localeStr
    )
    {
        VipsLogicUser user = (VipsLogicUser) httpServletRequest.getSession().getAttribute("user");
        
        if(user == null && userUUID != null)
        {
            user = userBean.findVipsLogicUser(UUID.fromString(userUUID));
        }
        ULocale locale = new ULocale(localeStr != null ? localeStr : 
        	user != null ? user.getOrganizationId().getDefaultLocale() : 
        		userBean.getOrganization(organizationId).getDefaultLocale());
        
        List<ObservationListItem> observations = getFilteredObservationsFromBackend(
                        organizationId,
                        pestId,
                        cropId,
                        cropCategoryId,
                        fromStr,
                        toStr,
                        user
                ).stream().map(obs -> { 
                    try {
						return obs.getListItem(locale.getLanguage(), 
								observationBean.getLocalizedObservationDataSchema(
										observationBean.getObservationDataSchema(organizationId, obs.getOrganismId()),
										httpServletRequest,
										locale
										)
								);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					}
                }).collect(Collectors.toList());
        
        //o.setObservationDataSchema(observationBean.getObservationDataSchema(observer.getOrganizationId().getOrganizationId(), o.getOrganismId()));
        return Response.ok().entity(observations).build();
    }
            
            
    private List<Observation> getFilteredObservationsFromBackend(
            Integer organizationId,
            Integer pestId,
            Integer cropId,
            List<Integer> cropCategoryId,
            String fromStr,
            String toStr
    )
    {
        //System.out.println("getFilteredObservationsFromBackend");
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
                
        return observationBean.getFilteredObservations(
            organizationId,
            pestId,
            cropId,
            cropCategoryId,
            from,
            to
        );
        
    }
    
    @GET
    @Path("filter/{organizationId}/geoJSON")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    public Response getFilteredObservationsAsGeoJSON(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("pestId") Integer pestId,
            @QueryParam("cropId") Integer cropId,
            @QueryParam("cropCategoryId") List<Integer> cropCategoryId,
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
                
        List<Observation> filteredObservations = observationBean.getFilteredObservations(
            organizationId,
            pestId,
            cropId,
            cropCategoryId,
            from,
            to
        );
        
        GISEntityUtil gisUtil = new GISEntityUtil();
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
        return Response.ok().entity(observationBean.getObservedPests(organizationId)).build();
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
        return Response.ok().entity(observationBean.getObservedCrops(organizationId)).build();
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
        return Response.ok().entity(observationBean.getObservations(organizationId, Observation.STATUS_TYPE_ID_APPROVED)).build();
    }
    
    /**
     * Get observations for a user
     * Requires a valid UUID to be provided in the Authorization header
     * @return
     */
    @GET
    @Path("list/user")
    @Produces("application/json;charset=UTF-8")
    public Response getObservationsForUser(
    		@QueryParam("observationIds") String observationIds
    		)
    {
    	try
	    	{
			VipsLogicUser user = userBean.getUserFromUUID(httpServletRequest);
			if(user != null)
			{
				List<Observation> allObs = observationBean.getObservationsForUser(user);
				if(observationIds != null)
				{
					Set<Integer> observationIdSet = Arrays.asList(observationIds.split(",")).stream()
							.map(s->Integer.valueOf(s))
							.collect(Collectors.toSet());
					return Response.ok().entity(
							allObs.stream()
							.filter(obs->observationIdSet.contains(obs.getObservationId()))
							.collect(Collectors.toList())
							)
							.build();
				}
				return Response.ok().entity(allObs).build();
			}
			else
			{
				return Response.status(Status.UNAUTHORIZED).build();
			}
    	}
    	catch(Exception e)
    	{
    		return Response.serverError().entity(e.getMessage()).build();
    	}
    }
    
    /**
     * Get observations for a user
     * Requires a valid UUID to be provided in the Authorization header
     * @return
     */
    @GET
    @Path("list/minimized/user")
    @Produces("application/json;charset=UTF-8")
    public Response getMinimizedObservationsForUser()
    {
		VipsLogicUser user = userBean.getUserFromUUID(httpServletRequest);
		if(user != null)
		{
			return Response.ok().entity(observationBean.getObservationsForUser(user).stream()
					.map(obs->new ObservationSyncInfo(obs)).collect(Collectors.toList())).build();
		}
		else
		{
			return Response.status(Status.UNAUTHORIZED).build();
		}
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
                return Response.ok().entity(observationBean.getBroadcastObservations(organizationId, from, to)).build();
            }
            catch(ParseException ex)
            {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid date format").build();
            }
        }
        else
        {
            return Response.ok().entity(observationBean.getBroadcastObservations(organizationId, season)).build();
        }
    }
    
    @GET
    @Path("{observationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getObservation(
            @PathParam("observationId") Integer observationId,
            @QueryParam("userUUID") String userUUID
    ){
        // Observation needs to be masked here as well, or does it create trouble for VIPSLogic observation admin?
        Observation o = observationBean.getObservation(observationId);
        if(o == null)
        {
            return Response.status(Status.NOT_FOUND).build();
        }
        // Which organization does this observation belong to?
        VipsLogicUser observer = userBean.getVipsLogicUser(o.getUserId());
        o.setObservationDataSchema(observationBean.getObservationDataSchema(observer.getOrganizationId().getOrganizationId(), o.getOrganismId()));
      
        VipsLogicUser user = (VipsLogicUser) httpServletRequest.getSession().getAttribute("user");
        if(user == null && userUUID != null)
        {
            user = userBean.findVipsLogicUser(UUID.fromString(userUUID));
        }
        // Modification of location information:
        // 1) If location is private, only the owner or super users/org admins may view them
        // slett all geoInfo
        if(user == null || (! user.isSuperUser() && ! user.isOrganizationAdmin()))
        {
            // Hide completely for all users except super and orgadmin
            if(o.getLocationIsPrivate() && (user == null || ! o.getUserId().equals(user.getUserId()))) 
            {
                o.setGeoinfos(null);
            }
            // This means the user wants to hide the exact location,
            // so mask for all users except super and orgadmin
            else if(o.getPolygonService() != null) 
            {
                //System.out.println("Masking observation");
                List<Observation> intermediary = new ArrayList<>();
                intermediary.add(o);
                intermediary = this.maskObservations(o.getPolygonService(), 
                        observationBean.getObservationsWithLocations(
                                observationBean.getObservationsWithGeoInfo(intermediary)
                        )
                    );
                o = intermediary.get(0);
            }
        }
        
        return Response.ok().entity(o).build();
    }
    
    @GET
    @Path("polygonservices/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getPolygonServicesForOrganization(
    		@PathParam("organizationId") Integer organizationId
    		)
    {
    	return Response.ok().entity(observationBean.getPolygonServicesForOrganization(organizationId)).build();
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
        if(!userBean.authorizeUser(user, 
                    VipsLogicRole.OBSERVER, 
                    VipsLogicRole.OBSERVATION_AUTHORITY, 
                    VipsLogicRole.ORGANIZATION_ADMINISTRATOR,
                    VipsLogicRole.SUPERUSER
                )
            )
        {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        observationBean.deleteGisObservationByGis(gisId);
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
            Observation observation = observationBean.getObservationFromGeoJSON(geoJSON);
            VipsLogicUser user = (VipsLogicUser) httpServletRequest.getSession().getAttribute("user");
            // If no user, send error message back to client
            if(user == null)
            {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            if(!userBean.authorizeUser(user, 
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
            observation = observationBean.storeObservation(observation);
            GISEntityUtil gisUtil = new GISEntityUtil();
            return Response.created(URI.create("/observation/" + observation.getObservationId())).entity(gisUtil.getGeoJSONFromObservation(observation)).build();
        }catch (IOException ex)
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex).build();
        }
    }

    /**
     * Returns the time of the first observation in the system of the pest with given Id
     * @param organismId
     * @return 
     */
    @GET
    @Path("first/{organismId}")
    @Produces("text/plain;charset=UTF-8")
    public Response getFirstObservation(@PathParam("organismId") Integer organismId)
    {
        Date firstObsTime = observationBean.getFirstObservationTime(organismId);
        return firstObsTime != null ? Response.ok().entity(firstObsTime).build()
                : Response.status(404).entity("No observations of organism with id=" + organismId).build();
    }
    
    /**
     * When was the last time a change was made in cropCategories or organisms?
     * 
     * @responseExample application/json {"lastUpdated": "2021-02-08T00:00:00Z"}
     */
    @GET
    @Path("organismsystemupdated")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDateOfLastOrganismSystemUpdate()
    {
    	HashMap<String, Object> result = new HashMap<>();
    	Instant lastUpdated = organismBean.getLatestUpdateOfOrganisms();
    	result.put("lastUpdated", lastUpdated != null ? lastUpdated: "1970-01-01T00:00:00Z");
    	return Response.ok().entity(result).build();
    }
    

    private List<Observation> getFilteredObservationsFromBackend(Integer organizationId, Integer pestId, Integer cropId, List<Integer> cropCategoryId, String fromStr, String toStr, VipsLogicUser user) {
        List<Observation> filteredObservations = this.getFilteredObservationsFromBackend(organizationId, pestId, cropId, cropCategoryId, fromStr, toStr);
        //filteredObservations.forEach(o->System.out.println(o.getObservationId()));
        // If superuser or orgadmin: Return everything, unchanged, uncensored
        if(user != null && (user.isSuperUser() || user.isOrganizationAdmin()))
        {
            return filteredObservations;
        }
        List<Observation> retVal = filteredObservations.stream().filter(obs->obs.getBroadcastMessage()).collect(Collectors.toList());
        //retVal.forEach(o->System.out.println(o.getObservationId()));
        retVal = this.maskObservations(retVal);
        //retVal.forEach(o->System.out.println(o.getObservationId()));
        // If user is not logged in, return only the publicly available observations
        if(user == null)
        {
            return retVal;
        }
        // Else: This is a registered user without special privileges. Show public observations + user's own
        retVal.addAll(observationBean.getObservationsForUser(user));
        return retVal;
    }

    /**
     * Runs through the observations, check each to see if it should be masked
     * (for privacy reasons) through a polygon service
     * @param observations
     * @return 
     */
    private List<Observation> maskObservations(List<Observation> observations) {
        //System.out.println("maskObservations(List<Observation> observations)");
        
        // Placing all observations with a polygon service in the correct bucket.
        Map<PolygonService, List<Observation>> registeredPolygonServicesInObservationList = new HashMap<>();
        observations.stream().filter((obs) -> (!obs.getLocationIsPrivate() && obs.getPolygonService() != null)).forEachOrdered((obs) -> {
            List<Observation> obsWithPolyServ = registeredPolygonServicesInObservationList.getOrDefault(obs.getPolygonService(), new ArrayList<>());
            obsWithPolyServ.add(obs);
            registeredPolygonServicesInObservationList.put(obs.getPolygonService(), obsWithPolyServ);
        });
        
        // No buckets filled = No masking needed, return list unmodified
        if(registeredPolygonServicesInObservationList.isEmpty())
        {
            return observations; 
        }
        else
        {
            // Loop through, mask
            Map<Integer,Observation> maskedObservations = new HashMap<>();
            registeredPolygonServicesInObservationList.keySet().forEach((pService) -> {
                this.maskObservations(pService, registeredPolygonServicesInObservationList.get(pService))
                        .forEach(o->maskedObservations.put(o.getObservationId(), o));
            });
            
            // Adding the rest of the observations (the ones that don't need masking)
            observations.stream().filter(o->maskedObservations.get(o.getObservationId())==null).forEach(o->maskedObservations.put(o.getObservationId(), o));
            return new ArrayList(maskedObservations.values());
        }
    }
    
    private List<Observation> maskObservations(PolygonService polygonService, List<Observation> observations)
    {
        //observations.forEach(o->System.out.println(o.getObservationId()));
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(polygonService.getGisSearchUrlTemplate());
        List<ReferencedPoint> points = observations.stream()
                .filter(obs -> (obs.getGeoinfos()!=null && !obs.getGeoinfos().isEmpty()) || obs.getLocation() != null)
                .map(obs->{
                    ReferencedPoint rp = new ReferencedPoint();
                    rp.setId(String.valueOf(obs.getObservationId()));
                    if(obs.getGeoinfos() != null)
                    {
                        rp.setLon(obs.getGeoinfos().get(0).getGisGeom().getCoordinate().x);
                        rp.setLat(obs.getGeoinfos().get(0).getGisGeom().getCoordinate().y);
                    }
                    else
                    {
                        rp.setLon(obs.getLocation().getLongitude());
                        rp.setLat(obs.getLocation().getLatitude());
                    }
                    return rp;
        }).collect(Collectors.toList());
        /*System.out.println("maskobservations - target.request() about to be called");
        ObjectMapper oMapper = new ObjectMapper();
        try {
            System.out.println(oMapper.writeValueAsString(Entity.entity(points.toArray(new ReferencedPoint[points.size()]), MediaType.APPLICATION_JSON)));
        } catch (JsonProcessingException ex) {
            Logger.getLogger(ObservationService.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        PointMappingResponse response = target.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(points.toArray(new ReferencedPoint[points.size()]), MediaType.APPLICATION_JSON), PointMappingResponse.class);
        // We need to loop through the observations and find corresponding featurecollections and replace those
        Map<Integer, Feature> indexedPolygons = new HashMap<>();
        for(Feature feature:response.getFeatureCollection().getFeatures())
        {
            indexedPolygons.put((Integer)feature.getProperties().get("id"), feature);
        }
        GISEntityUtil gisEntityUtil = new GISEntityUtil();
        for(Map mapping:response.getMapping())
        {
            Integer observationId = Integer.valueOf((String) mapping.get("id"));
            Integer borderId = (Integer) mapping.get("borderid");
            observations.stream().filter((o) -> (o.getObservationId().equals(observationId))).forEachOrdered((o) -> {
                Gis polygon = gisEntityUtil.getGisFromFeature(indexedPolygons.get(borderId));
                List<Gis> gis = new ArrayList<>();
                gis.add(polygon);
                o.setGeoinfos(gis);
                o.setLocation(null);
                o.setLocationPointOfInterestId(null);
            });
        }
        
        return observations;
    }
    
    @POST
    @Path("syncobservationfromapp")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public Response syncObservationFromApp(
    		String observationJson
    		)
    {
    	try
    	{
	    	VipsLogicUser user = userBean.getUserFromUUID(httpServletRequest);
	    	if(user == null)
	    	{
	    		return Response.status(Status.UNAUTHORIZED).build();
	    	}
	    	ObjectMapper oM = new ObjectMapper();
	    	SimpleDateFormat df = new SimpleDateFormat(Globals.defaultTimestampFormat);
	    	try {
	    		Map<Object,Object> mapFromApp = oM.readValue(observationJson, new TypeReference<HashMap<Object,Object>>(){});
				// Check if it is marked as deleted or not
				if(mapFromApp.get("deleted") != null && ((Boolean)mapFromApp.get("deleted").equals(true)))
				{
					if(observationBean.getObservation((Integer)mapFromApp.get("observationId")) != null)
					{
						observationBean.deleteObservation((Integer)mapFromApp.get("observationId"));
						return Response.ok().build();
					}
					else
					{
						return Response.status(Status.NOT_FOUND).build();
					}
				}
				else
				{
					Observation mergeObs = ((Integer)mapFromApp.get("observationId")) > 0 ? observationBean.getObservation((Integer)mapFromApp.get("observationId")): new Observation();
					// Trying to sync a non-existing observation
					if(mergeObs == null)
					{
						return Response.status(Status.NOT_FOUND).build();
					}
					// Pest organism
					mergeObs.setOrganism(organismBean.getOrganism((Integer)mapFromApp.get("organismId")));
					// Crop organism
					mergeObs.setCropOrganism(organismBean.getOrganism((Integer)mapFromApp.get("cropOrganismId")));
					// Other properties
					mergeObs.setTimeOfObservation(oM.convertValue(mapFromApp.get("timeOfObservation"), new TypeReference<Date>(){}));
					mergeObs.setUserId(mapFromApp.get("userId") != null ? Integer.valueOf((Integer)mapFromApp.get("userId")): user.getUserId());
					mergeObs.setGeoinfo((String)mapFromApp.get("geoinfo"));
					mergeObs.setLocationPointOfInterestId(mapFromApp.get("locationPointOfInterestId") != null ? (Integer) mapFromApp.get("locationPointOfInterestId") : null);
					mergeObs.setObservationHeading(mapFromApp.get("observationHeading") != null ? (String) mapFromApp.get("observationHeading") : null);
					mergeObs.setObservationText(mapFromApp.get("observationText") != null ? (String) mapFromApp.get("observationText") : null);
					mergeObs.setBroadcastMessage(mapFromApp.get("broadcastMessage") != null ? (Boolean) mapFromApp.get("broadcastMessage") : false);
					mergeObs.setStatusTypeId(Integer.valueOf((Integer)mapFromApp.get("statusTypeId")));
					// If the user has the role of observation approver, change to approved if set to pending
					if(mergeObs.getStatusTypeId().equals(ObservationStatusType.STATUS_PENDING) && user.isObservationAuthority())
					{
						mergeObs.setStatusTypeId(ObservationStatusType.STATUS_APPROVED);
					}
					mergeObs.setStatusChangedByUserId(mapFromApp.get("statusChangedByUserId") != null ? (Integer) mapFromApp.get("statusChangedByUserId") : null);
					mergeObs.setStatusChangedTime(mapFromApp.get("timeOfObservation") != null ? oM.convertValue(mapFromApp.get("timeOfObservation"), new TypeReference<Date>(){}) : null);
					mergeObs.setStatusRemarks(mapFromApp.get("statusRemarks") != null ? (String) mapFromApp.get("statusRemarks") : null);
					mergeObs.setIsQuantified(mapFromApp.get("isQuantified") != null ? (Boolean) mapFromApp.get("isQuantified") : false);
					mergeObs.setLocationIsPrivate(mapFromApp.get("locationIsPrivate") != null ? (Boolean) mapFromApp.get("locationIsPrivate") : false);
					mergeObs.setPolygonService(mapFromApp.get("polygonService") != null && ! ((String)mapFromApp.get("polygonService")).isEmpty() ? oM.convertValue(mapFromApp.get("polygonService"), new TypeReference<PolygonService>(){}) : null);
					mergeObs.setObservationDataSchema(observationBean.getObservationDataSchema(user.getOrganization_id(), mergeObs.getOrganismId()));
					mergeObs.setObservationData(mapFromApp.get("observationData") != null ? mapFromApp.get("observationData").toString() : null);
					mergeObs.setLastEditedBy(user.getUserId());
					mergeObs.setLastEditedTime(new Date());
					
					// Input check before storing
					// Location must be set
					if((mergeObs.getGeoinfo() == null || mergeObs.getGeoinfo().trim().isEmpty()) && mergeObs.getLocationPointOfInterestId() == null)
					{
						return Response.status(Status.BAD_REQUEST).entity("The observation is missing location data.").build();
					}
                                        
                                        boolean sendNotification = false;    
                                        // Storing approval status
                                        // If superusers or user with correct authorization: Set as approved and send message
                                        if(userBean.authorizeUser(user, VipsLogicRole.OBSERVATION_AUTHORITY, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                                        {
                                            LOGGER.debug("user properly authorized to register observations");
                                            LOGGER.debug("observation Id=" + mergeObs.getObservationId());
                                            LOGGER.debug("broadcast this message? " + mergeObs.getBroadcastMessage());
                                            if(mergeObs.getObservationId() == null || mergeObs.getObservationId() <= 0)
                                            {
                                                mergeObs.setStatusTypeId(Observation.STATUS_TYPE_ID_APPROVED);
                                                sendNotification = mergeObs.getBroadcastMessage(); // Only send the ones intended for sending
                                            }
                                        }
                                        else if(mergeObs.getObservationId() == null || mergeObs.getObservationId() <= 0)
                                        {
                                            mergeObs.setStatusTypeId(Observation.STATUS_TYPE_ID_PENDING);
                                        }
					
					// We need to get an observation Id before storing the illustrations!
					mergeObs = observationBean.storeObservation(mergeObs);
                                        
                                        
					
					// ObservationIllustrationSet
					// Including data that may need to be stored
					if(mapFromApp.get("observationIllustrationSet") != null)
					{
						List<Map<Object,Object>> illusMaps = (List<Map<Object,Object>>) mapFromApp.get("observationIllustrationSet");
						for(Map<Object,Object> illusMap:illusMaps)
						{
							ObservationIllustrationPK pk = oM.convertValue(illusMap.get("observationIllustrationPK"), new TypeReference<ObservationIllustrationPK>(){});
							
							if(illusMap.get("deleted") != null && ((Boolean) illusMap.get("deleted")) == true)
							{
								observationBean.deleteObservationIllustration(mergeObs, new String[] {pk.getFileName()});
							}
							else if(illusMap.get("uploaded") != null && ((Boolean) illusMap.get("uploaded")) == false && illusMap.get("imageTextData") != null)
							{
								mergeObs = observationBean.storeObservationIllustration(mergeObs, pk.getFileName(), (String)illusMap.get("imageTextData"));
							}
						}
					}
                                        LOGGER.debug("sendNotification? " + sendNotification);
                                        
                                        // All transactions finished, we can send notifications
                                        // if conditions are met
                                        if(sendNotification && !
                                                (System.getProperty("DISABLE_MESSAGING_SYSTEM") != null && System.getProperty("DISABLE_MESSAGING_SYSTEM").equals("true"))
                                                )
                                        {
                                            LOGGER.debug("Sending the message!");
                                            messagingBean.sendUniversalMessage(mergeObs);
                                        }
                                        
					return Response.ok().entity(mergeObs).build();
				}
			} catch (IOException e) {
				return Response.serverError().entity(e).build();
			}
    	}
    	catch(Exception e)
    	{
            e.printStackTrace();
    		return Response.serverError().entity(e).build();
    	}
    	
    }
}
