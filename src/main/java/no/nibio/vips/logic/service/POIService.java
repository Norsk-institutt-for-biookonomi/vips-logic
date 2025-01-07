/*
 * Copyright (c) 2022 NIBIO <http://www.nibio.no/>. 
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
package no.nibio.vips.logic.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.JsonProcessingException;
import no.nibio.vips.logic.controller.session.PointOfInterestBean;
import no.nibio.vips.logic.entity.helpers.PointOfInterestFactory;
import org.jboss.resteasy.spi.HttpRequest;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webcohesion.enunciate.metadata.Facet;
import com.webcohesion.enunciate.metadata.rs.TypeHint;

import java.util.stream.Collectors;

import no.nibio.vips.gis.GISUtil;
import no.nibio.vips.logic.entity.Country;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.PointOfInterest;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.controller.session.SessionControllerGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @copyright 2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest/poi")
public class POIService {
    private final static Logger LOGGER = LoggerFactory.getLogger(POIService.class);

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

    @POST
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public Response postPoi(String poiJson) {
        ObjectMapper oM = new ObjectMapper();
        Map<Object, Object> poiMap;
        try {
            poiMap = oM.readValue(poiJson, new TypeReference<HashMap<Object, Object>>() {});
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage(), e);
            return Response.status(Status.BAD_REQUEST).entity("Unable to parse input").build();
        }

        VipsLogicUser user = SessionControllerGetter.getUserBean().getUserFromUUID(httpServletRequest);
        if (user == null) {
            LOGGER.error("No user found for UUID in Authorization");
            return Response.status(Status.UNAUTHORIZED).build();
        }
        LOGGER.error("Remember to check for roles as well, if necessary!");

        PointOfInterestBean poiBean = SessionControllerGetter.getPointOfInterestBean();
        Integer poiTypeId = poiMap.get("pointOfInterestTypeId") != null ? Integer.parseInt(poiMap.get("pointOfInterestTypeId").toString()) : null;
        if(poiTypeId == null) {
            return Response.status(Status.BAD_REQUEST).entity("pointOfInterestTypeId is required").build();
        }
        String poiName = poiMap.get("name") != null ? poiMap.get("name").toString() : null;
        Double poiLongitude = poiMap.get("longitude") != null ? Double.valueOf(poiMap.get("longitude").toString()): null;
        Double poiLatitude = poiMap.get("latitude") != null ? Double.valueOf(poiMap.get("latitude").toString()): null;
        Double poiAltitude = 0.0;

        PointOfInterest poiToSave = PointOfInterestFactory.getPointOfInterest(poiTypeId);
        poiToSave.setName(poiName);
        poiToSave.setLongitude(poiLongitude);
        poiToSave.setLatitude(poiLatitude);
        poiToSave.setAltitude(poiAltitude);
        poiToSave.setLastEditedTime(new Date());
        poiToSave.setUser(user);
        poiToSave.setCountryCode(user.getOrganizationId().getCountryCode());
        poiToSave.setTimeZone(user.getOrganizationId().getDefaultTimeZone());
        poiToSave.setIsForecastLocation(Boolean.FALSE);
        poiToSave.setIsPrivate(Boolean.TRUE);

        if (poiLongitude != null && poiLatitude != null) {
            GISUtil gisUtil = new GISUtil();
            Coordinate coordinate = new Coordinate(poiLongitude, poiLatitude, poiAltitude);
            Point p3d = gisUtil.createPointWGS84(coordinate);
            poiToSave.setGisGeom(p3d);
        }
        poiToSave = poiBean.storePoi(poiToSave);

        if (poiToSave != null) {
            return Response.status(Response.Status.CREATED)
                    .entity(poiToSave)
                    .build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to create PointOfInterest " + poiName)
                    .build();
        }
    }

    /**
     * @param organizationId Id of the organization in question
     * @param poiTypesStr Comma separated list of poiTypes
     * @return GeoJson with pois filtered by organization and type
     */
    @GET
    @Path("organization/{organizationId}/type/geojson")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(PointOfInterest.class)
    public Response getPoisByPoiTypesAsGeoJson(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("poiTypes") String poiTypesStr
    )
    {
        List<Integer> poiTypes = Arrays.asList(poiTypesStr.split(",")).stream().map(str->Integer.valueOf(str)).collect(Collectors.toList());
        Organization organization = SessionControllerGetter.getUserBean().getOrganization(organizationId);
        List<PointOfInterest> pois = SessionControllerGetter.getPointOfInterestBean().getPoisForOrganizationAndOfTypes(organization, poiTypes); 
        
        return Response.ok().entity(SessionControllerGetter.getPointOfInterestBean().getPoisAsGeoJson(pois)).build();
    }

    @POST
    @Path("geojson")
    @Produces("application/json;charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getPoisGeoJson(Set<Integer> ids) {
        // Retrieve POIs from data source
        List<PointOfInterest> pois = SessionControllerGetter.getPointOfInterestBean().getPois(ids);
        if (pois == null || pois.isEmpty()) {
            return Response.noContent().build();
        }
        return Response.ok(SessionControllerGetter.getPointOfInterestBean().getPoisAsGeoJson(pois)).build();
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
        try {
            Map<Object, Object> mapFromApp = oM.readValue(poiJson, new TypeReference<HashMap<Object, Object>>() {
            });
            Integer pointOfInterestId = (Integer) mapFromApp.get("pointOfInterestId");
            PointOfInterestBean pointOfInterestBean = SessionControllerGetter.getPointOfInterestBean();
            // Check if it is marked as deleted or not
            if (mapFromApp.get("deleted") != null && (mapFromApp.get("deleted").equals(true))) {
                if (pointOfInterestBean.getPointOfInterest(pointOfInterestId) != null) {
                    pointOfInterestBean.deletePoi(pointOfInterestId);
                    LOGGER.info("POI with id={} deleted", pointOfInterestId);
                    return Response.ok().build();
                } else {
                    LOGGER.error("POI with id={} not found, nothing deleted", pointOfInterestId);
                    return Response.status(Status.NOT_FOUND).build();
                }
            } else {
                PointOfInterest mergePoi = pointOfInterestId > 0 ? pointOfInterestBean.getPointOfInterest(pointOfInterestId) : PointOfInterest.getInstance((Integer) mapFromApp.get("pointOfInterestTypeId"));
                // Trying to sync a non-existing POI
                if (mergePoi == null) {
                    LOGGER.error("POI with id={} not found, nothing updated", pointOfInterestId);
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

                mergePoi = pointOfInterestBean.getPointOfInterest(pointOfInterestBean.storePoi(mergePoi).getPointOfInterestId());
                return Response.ok().entity(mergePoi).build();
            }
        } catch (IOException e) {
            LOGGER.error("Exception when syncing POI", e);
            return Response.serverError().entity(e).build();
        }

    }

}
