/*
 * Copyright (c) 2022 NIBIO <http://www.nibio.no/>. 
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.spi.HttpRequest;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webcohesion.enunciate.metadata.Facet;
import com.webcohesion.enunciate.metadata.rs.TypeHint;

import no.nibio.vips.gis.GISUtil;
import no.nibio.vips.logic.entity.Country;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.PointOfInterest;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.logic.controller.session.SessionControllerGetter;

/**
 * @copyright 2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest/poi")
public class POIService {

    @Context
    private HttpRequest httpRequest;
    @Context
    private HttpServletRequest httpServletRequest;

    /**
     * Get a list of locations (pois) for a given organization
     *
     * @param organizationId Database id for the organization
     * @return List of weather stations for the organization
     */
    @GET
    @Path("organization/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(PointOfInterestWeatherStation[].class)
    public Response getPoisForOrganization(@PathParam("organizationId") Integer organizationId) {
        Organization organization = SessionControllerGetter.getUserBean().getOrganization(organizationId);
        List<PointOfInterestWeatherStation> retVal = SessionControllerGetter.getPointOfInterestBean().getWeatherstationsForOrganization(organization, Boolean.TRUE);
        return Response.ok().entity(retVal).build();
    }

    /**
     *
     * @param pointOfInterestId Database id of the POI
     * @return a particular POI (Point of interest)
     */
    @GET
    @Path("{pointOfInterestId}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(PointOfInterest.class)
    public Response getPoi(@PathParam("pointOfInterestId") Integer pointOfInterestId) {
        PointOfInterest retVal = SessionControllerGetter.getPointOfInterestBean().getPointOfInterest(pointOfInterestId);
        return Response.ok().entity(retVal).build();
    }

    /**
     * Find a POI (Point of interest) by name
     *
     * @param poiName The name of the POI, e.g. "My FooBar Location"
     * @return a particular POI (Point of interest), or HTTP Status 204 (No content) if not found
     */
    @GET
    @Path("name/{poiName}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(PointOfInterest.class)
    public Response getPoiByName(@PathParam("poiName") String poiName) {
        PointOfInterest retVal = SessionControllerGetter.getPointOfInterestBean().getPointOfInterest(poiName);
        return retVal != null ? Response.ok().entity(retVal).build() : Response.noContent().build();
    }

    /**
     * If used outside of VIPSLogic: Requires a valid UUID to be provided in the
     * Authorization header
     *
     * @return a list of POIs belonging to the user currently logged in
     */
    @GET
    @Path("user")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(PointOfInterest[].class)
    public Response getPoisForCurrentUser() {
        VipsLogicUser user = (VipsLogicUser) httpServletRequest.getSession().getAttribute("user");
        // Could be the VIPS obs app or some other client using UUID
        if (user == null) {
            String uuidStr = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
            UUID uuid = UUID.fromString(uuidStr);
            user = SessionControllerGetter.getUserBean().findVipsLogicUser(uuid);
            if (user == null) {
                return Response.status(Status.UNAUTHORIZED).build();
            }
        }
        List<PointOfInterest> retVal = SessionControllerGetter.getPointOfInterestBean().getRelevantPointOfInterestsForUser(user);
        return Response.ok().entity(retVal).build();
    }
    
    /**
     * This service is used by the VIPS Field observation app to sync data stored locally on the smartphone with the
     * state of the (potentially non-existent) POI in the VIPSLogic database
     * TODO Add request example
     * @param poiJson Json representation of the POI(s)
     * @return  The POI(s) in their merged state, serialized to Json
     */
    @POST
    @Path("syncpoifromapp")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(PointOfInterest.class)
    public Response syncPOIFromApp(
            String poiJson
    ) {
        VipsLogicUser user = SessionControllerGetter.getUserBean().getUserFromUUID(httpServletRequest);
        if (user == null) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        ObjectMapper oM = new ObjectMapper();
        SimpleDateFormat df = new SimpleDateFormat(Globals.defaultTimestampFormat);
        try {
            Map<Object, Object> mapFromApp = oM.readValue(poiJson, new TypeReference<HashMap<Object, Object>>() {
            });
            // Check if it is marked as deleted or not
            if (mapFromApp.get("deleted") != null && ((Boolean) mapFromApp.get("deleted").equals(true))) {
                if (SessionControllerGetter.getPointOfInterestBean().getPointOfInterest((Integer) mapFromApp.get("pointOfInterestId")) != null) {
                    SessionControllerGetter.getPointOfInterestBean().deletePoi((Integer) mapFromApp.get("pointOfInterestId"));
                    return Response.ok().build();
                } else {
                    return Response.status(Status.NOT_FOUND).build();
                }
            } else {
                PointOfInterest mergePoi = ((Integer) mapFromApp.get("pointOfInterestId")) > 0 ? SessionControllerGetter.getPointOfInterestBean().getPointOfInterest((Integer) mapFromApp.get("pointOfInterestId")) : PointOfInterest.getInstance((Integer) mapFromApp.get("pointOfInterestTypeId"));
                // Trying to sync a non-existing observation
                if (mergePoi == null) {
                    return Response.status(Status.NOT_FOUND).build();
                }

                mergePoi.setName((String) mapFromApp.get("name"));
                mergePoi.setTimeZone(mapFromApp.get("timeZone") != null
                        ? (String) mapFromApp.get("timeZone")
                        : user.getOrganizationId().getDefaultTimeZone()
                );
                mergePoi.setLongitude((Double) mapFromApp.get("longitude"));
                mergePoi.setLatitude((Double) mapFromApp.get("latitude"));
                try {
                    Double altitude = mapFromApp.get("altitude") instanceof Integer
                            ? ((Integer) mapFromApp.get("altitude")).doubleValue()
                            : (Double) mapFromApp.get("altitude");
                    mergePoi.setAltitude(altitude);
                } catch (NullPointerException | ClassCastException ex) {
                    mergePoi.setAltitude(0.0);
                }
                mergePoi.setCountryCode(
                        mapFromApp.get("countryCode") != null
                        ? new Country((String) (((Map<Object, Object>) mapFromApp.get("countryCode")).get("countryCode")))
                        : user.getOrganizationId().getCountryCode()
                );
                mergePoi.setUser(user);
                mergePoi.setLastEditedTime(new Date());
                GISUtil gisUtil = new GISUtil();
                Coordinate coordinate = new Coordinate(mergePoi.getLongitude(), mergePoi.getLatitude(), mergePoi.getAltitude());
                Point p3d = gisUtil.createPointWGS84(coordinate);
                mergePoi.setGisGeom(p3d);

                mergePoi = SessionControllerGetter.getPointOfInterestBean().getPointOfInterest(SessionControllerGetter.getPointOfInterestBean().storePoi(mergePoi).getPointOfInterestId());

                return Response.ok().entity(mergePoi).build();
            }
        } catch (IOException e) {
            return Response.serverError().entity(e).build();
        }

    }

}
