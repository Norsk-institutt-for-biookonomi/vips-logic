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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.util.ULocale;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import no.nibio.vips.gis.GISUtil;
import no.nibio.vips.logic.controller.session.ObservationBean;
import no.nibio.vips.logic.controller.session.ObservationTimeSeriesBean;
import no.nibio.vips.logic.controller.session.OrganismBean;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.*;
import no.nibio.vips.logic.entity.rest.ObservationListItem;
import no.nibio.vips.logic.entity.rest.PointMappingResponse;
import no.nibio.vips.logic.entity.rest.ReferencedPoint;
import no.nibio.vips.logic.messaging.MessagingBean;
import no.nibio.vips.logic.util.GISEntityUtil;
import no.nibio.vips.logic.util.Globals;
import org.jboss.resteasy.annotations.GZIP;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wololo.geojson.Feature;
import org.wololo.geojson.GeoJSON;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 * @copyright 2016-2022 <a href="http://www.nibio.no/">NIBIO</a>
 */
@Path("rest/observation")
public class ObservationService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ObservationService.class);
    @EJB
    UserBean userBean;
    @EJB
    ObservationBean observationBean;
    @EJB
    OrganismBean organismBean;

    @EJB
    ObservationTimeSeriesBean observationTimeSeriesBean;
    @EJB
    MessagingBean messagingBean;
    @Context
    private HttpServletRequest httpServletRequest;

    /*
     * PostGIS tip:
     * How to query for observations within a bounding box
     * Select * from gis where ST_Intersects(
     *   ST_SetSRID(ST_MakeBox2D(ST_MakePoint(2.9004, 57.7511), ST_MakePoint(32.4316, 71.3851)),4326),
     *   gis_geom);
     * First point is SW, last is NE (but could be anything?)
     */

    /**
     * @param organizationId Database ID of the organization
     * @param pestId         Database ID of the pest
     * @param cropId         Database ID of the crop
     * @param cropCategoryId Database IDs of the crop category/categories
     * @param fromStr        format "yyyy-MM-dd"
     * @param toStr          format "yyyy-MM-dd"
     * @return Observation objects with all data (full tree)
     */
    @GET
    @Path("filter/{organizationId}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    @TypeHint(Observation[].class)
    public Response getFilteredObservations(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("observationTimeSeriesId") Integer observationTimeSeriesId,
            @QueryParam("pestId") Integer pestId,
            @QueryParam("cropId") Integer cropId,
            @QueryParam("cropCategoryId") List<Integer> cropCategoryId,
            @QueryParam("from") String fromStr,
            @QueryParam("to") String toStr,
            @QueryParam("isPositive") Boolean isPositive
    ) {
        return Response.ok().entity(getFilteredObservationsFromBackend(
                organizationId,
                observationTimeSeriesId,
                pestId,
                cropId,
                cropCategoryId,
                fromStr,
                toStr,
                isPositive
        )).build();
    }

    /**
     * @param organizationId Database ID of the organization
     * @param pestId         Database ID of the pest
     * @param cropId         Database ID of the crop
     * @param cropCategoryId cropCategoryId Database IDs of the crop category/categories
     * @param fromStr        format "yyyy-MM-dd"
     * @param toStr          format "yyyy-MM-dd"
     * @return Observation objects for which the user is authorized to observe with properties relevant for lists
     */
    @GET
    @Path("list/filter/{organizationId}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    @TypeHint(ObservationListItem.class)
    public Response getFilteredObservationListItemsAsJson(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("observationTimeSeriesId") Integer observationTimeSeriesId,
            @QueryParam("pestId") Integer pestId,
            @QueryParam("cropId") Integer cropId,
            @QueryParam("cropCategoryId") List<Integer> cropCategoryId,
            @QueryParam("from") String fromStr,
            @QueryParam("to") String toStr,
            @QueryParam("userUUID") String userUUID,
            @QueryParam("locale") String localeStr,
            @QueryParam("isPositive") Boolean isPositive
    ) {
        return Response.ok().entity(this.getFilteredObservationListItems(organizationId, observationTimeSeriesId, pestId, cropId, cropCategoryId, fromStr, toStr, userUUID, localeStr, isPositive)).build();
    }

    private List<ObservationListItem> getFilteredObservationListItems(
            Integer organizationId,
            Integer observationTimeSeriesId,
            Integer pestId,
            Integer cropId,
            List<Integer> cropCategoryId,
            String fromStr,
            String toStr,
            String userUUID,
            String localeStr,
            Boolean isPositive) {
        VipsLogicUser user = (VipsLogicUser) httpServletRequest.getSession().getAttribute("user");

        if (user == null && userUUID != null) {
            user = userBean.findVipsLogicUser(UUID.fromString(userUUID));
        }
        ULocale locale = new ULocale(localeStr != null ? localeStr :
                user != null ? user.getOrganizationId().getDefaultLocale() :
                        userBean.getOrganization(organizationId).getDefaultLocale());

        LOGGER.info("Get filtered observations for user {}", user != null ? user.getUserId() : "<no user>");
        List<ObservationListItem> observations = getFilteredObservationsFromBackend(
                organizationId,
                observationTimeSeriesId,
                pestId,
                cropId,
                cropCategoryId,
                fromStr,
                toStr,
                isPositive,
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
                LOGGER.error("Exception when getting localized observation data schema for observation " + obs.getObservationId(), e);
                return null;
            }
        }).collect(Collectors.toList());
        //o.setObservationDataSchema(observationBean.getObservationDataSchema(observer.getOrganizationId().getOrganizationId(), o.getOrganismId()));
        return observations;
    }

    /**
     * @param organizationId Database ID of the organization
     * @param pestId         Database ID of the pest
     * @param cropId         Database ID of the crop
     * @param cropCategoryId cropCategoryId Database IDs of the crop category/categories
     * @param fromStr        format "yyyy-MM-dd"
     * @param toStr          format "yyyy-MM-dd"
     * @return Observation objects for which the user is authorized to observe with properties relevant for lists
     */
    @GET
    @Path("list/filter/{organizationId}/csv")
    @GZIP
    @Produces("text/csv;charset=UTF-8")
    @TypeHint(ObservationListItem.class)
    public Response getFilteredObservationListItemsAsCSV(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("observationTimeSeriesId") Integer observationTimeSeriesId,
            @QueryParam("pestId") Integer pestId,
            @QueryParam("cropId") Integer cropId,
            @QueryParam("cropCategoryId") List<Integer> cropCategoryId,
            @QueryParam("from") String fromStr,
            @QueryParam("to") String toStr,
            @QueryParam("userUUID") String userUUID,
            @QueryParam("locale") String localeStr,
            @QueryParam("isPositive") Boolean isPositive
    ) {
        List<ObservationListItem> observations = this.getFilteredObservationListItems(organizationId, observationTimeSeriesId, pestId, cropId, cropCategoryId, fromStr, toStr, userUUID, localeStr, isPositive);
        Collections.sort(observations);
        String retVal = "ObservationID;organismName;cropOrganismName;timeOfObservation;lat/lon;observationHeading;observationData";
        GISUtil gisUtil = new GISUtil();
        for (ObservationListItem obs : observations) {
            // Get latlon from geoInfo
            List<Geometry> geometries = gisUtil.getGeometriesFromGeoJSON(obs.getGeoInfo());
            Coordinate c = null;
            if (geometries.size() == 1 && geometries.get(0) instanceof Point) {
                c = ((Point) geometries.get(0)).getCoordinate();
            }
            retVal += "\n" + obs.getObservationId()
                    + ";" + obs.getOrganismName()
                    + ";" + obs.getCropOrganismName()
                    + ";" + obs.getTimeOfObservation()
                    + ";" + (c != null ? c.getY() + "," + c.getX() : "")
                    + ";" + obs.getObservationHeading()
                    + ";" + obs.getObservationData();
        }
        return Response.ok().entity(retVal).build();
    }

    /**
     * @param organizationId          Database ID of the organization
     * @param observationTimeSeriesId Database ID of the observation time series
     * @param pestId                  Database ID of the pest
     * @param cropId                  Database ID of the crop
     * @param cropCategoryId          cropCategoryId Database IDs of the crop category/categories
     * @param fromStr                 format "yyyy-MM-dd"
     * @param toStr                   format "yyyy-MM-dd"
     * @return Observation objects for which the user is authorized to observe with properties relevant for lists
     */
    private List<Observation> getFilteredObservationsFromBackend(
            Integer organizationId,
            Integer observationTimeSeriesId,
            Integer pestId,
            Integer cropId,
            List<Integer> cropCategoryId,
            String fromStr,
            String toStr,
            Boolean isPositive) {
        SimpleDateFormat format = new SimpleDateFormat(Globals.defaultDateFormat);
        //TODO Set correct timeZone!!!
        Date from = null;
        Date to = null;
        try {
            from = fromStr != null ? format.parse(fromStr) : null;
            to = toStr != null ? format.parse(toStr) : null;
        } catch (ParseException ex) {
            LOGGER.error("Error when parsing dates", ex);
        }

        return observationBean.getFilteredObservations(
                organizationId,
                observationTimeSeriesId,
                pestId,
                cropId,
                cropCategoryId,
                from,
                to,
                isPositive
        );

    }

    /**
     * @param organizationId
     * @param pestId
     * @param cropId
     * @param cropCategoryId
     * @param fromStr
     * @param toStr
     * @return
     * @responseExample application/json
     * {
     * "type": "FeatureCollection",
     * "features": [
     * {
     * "type": "Feature",
     * "id": 18423,
     * "geometry": {
     * "type": "Point",
     * "coordinates": [
     * 10.782463095356452,
     * 59.35794998304658,
     * 0.0
     * ]
     * },
     * "properties": {
     * "observationId": 18446,
     * "observationHeading": "NLR Øst, Huggenes: Det er funnet potettørråte i Råde",
     * "organism": {
     * "organismId": 14,
     * "latinName": "Phytophthora infestans",
     * "tradeName": "",
     * "logicallyDeleted": false,
     * "isPest": true,
     * "isCrop": false,
     * "parentOrganismId": 124,
     * "hierarchyCategoryId": 120,
     * "organismLocaleSet": [
     * {
     * "organismLocalePK": {
     * "organismId": 14,
     * "locale": "nb"
     * },
     * "localName": "Potettørråte"
     * },
     * {
     * "organismLocalePK": {
     * "organismId": 14,
     * "locale": "en"
     * },
     * "localName": "Late blight"
     * }
     * ],
     * "organismExternalResourceSet": [],
     * "childOrganisms": null,
     * "extraProperties": {},
     * "observationDataSchema": null
     * },
     * "cropOrganism": {
     * "organismId": 5,
     * "latinName": "Solanum tuberosum",
     * "tradeName": "",
     * "logicallyDeleted": false,
     * "isPest": false,
     * "isCrop": true,
     * "parentOrganismId": 4,
     * "hierarchyCategoryId": 120,
     * "organismLocaleSet": [
     * {
     * "organismLocalePK": {
     * "organismId": 5,
     * "locale": "bs"
     * },
     * "localName": "Potato"
     * },
     * {
     * "organismLocalePK": {
     * "organismId": 5,
     * "locale": "nb"
     * },
     * "localName": "Potet"
     * },
     * {
     * "organismLocalePK": {
     * "organismId": 5,
     * "locale": "en"
     * },
     * "localName": "Potato"
     * },
     * {
     * "organismLocalePK": {
     * "organismId": 5,
     * "locale": "hr"
     * },
     * "localName": "Krompir"
     * }
     * ],
     * "organismExternalResourceSet": [],
     * "childOrganisms": null,
     * "extraProperties": {},
     * "observationDataSchema": null
     * },
     * "observationText": "Fredag 2.juli ble det gjort første funn av potettørråte i Råde. Det er noen flekker på bladene på øvre del av planten. Smitten ser ut til å ha kommet som sekundærsmitte med vinden.",
     * "timeOfObservation": "2021-07-02T11:00:00+02:00"
     * }
     * }
     * ]
     * }
     */
    @GET
    @Path("filter/{organizationId}/geoJSON")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    @TypeHint(GeoJSON.class)
    public Response getFilteredObservationsAsGeoJSON(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("observationTimeSeriesId") Integer observationTimeSeriesId,
            @QueryParam("pestId") Integer pestId,
            @QueryParam("cropId") Integer cropId,
            @QueryParam("cropCategoryId") List<Integer> cropCategoryId,
            @QueryParam("from") String fromStr,
            @QueryParam("to") String toStr,
            @QueryParam("isPositive") Boolean isPositive

    ) {
        SimpleDateFormat format = new SimpleDateFormat(Globals.defaultDateFormat);
        //TODO Set correct timeZone!!!
        Date from = null;
        Date to = null;
        try {
            from = fromStr != null ? format.parse(fromStr) : null;
            to = toStr != null ? format.parse(toStr) : null;
        } catch (ParseException ex) {
            LOGGER.error("Error when parsing dates", ex);
        }

        List<Observation> filteredObservations = this.getFilteredObservationsFromBackend(
                organizationId,
                observationTimeSeriesId,
                pestId,
                cropId,
                cropCategoryId,
                fromStr,
                toStr,
                isPositive
        );

        GISEntityUtil gisUtil = new GISEntityUtil();
        return Response.ok().entity(gisUtil.getGeoJSONFromObservations(filteredObservations)).build();
    }

    /**
     * Get a list of all observed pests for one organization
     * Practical for building effective select lists
     *
     * @param organizationId Database ID of organization
     * @return list of all observed pests for one organization
     */
    @GET
    @Path("pest/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(Organism[].class)
    public Response getObservedPests(@PathParam("organizationId") Integer organizationId) {
        return Response.ok().entity(observationBean.getObservedPests(organizationId)).build();
    }

    /**
     * Get a list of all crop cultures where observations have been made for one organization
     * Practical for building effective select lists
     * TODO: Should be cached??
     *
     * @param organizationId Database ID of organization
     * @return
     */
    @GET
    @Path("crop/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(Organism[].class)
    public Response getObservedCrops(@PathParam("organizationId") Integer organizationId) {
        return Response.ok().entity(observationBean.getObservedCrops(organizationId)).build();
    }

    /**
     * Publicly available observations per organization
     *
     * @param organizationId Database ID of organization
     * @return APPROVED observations
     */
    @GET
    @Path("list/{organizationId}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    @TypeHint(Observation[].class)
    public Response getObservations(@PathParam("organizationId") Integer organizationId) {
        return Response.ok().entity(observationBean.getObservations(organizationId, Observation.STATUS_TYPE_ID_APPROVED)).build();
    }

    /**
     * Get observations for a user
     * Requires a valid UUID to be provided in the Authorization header
     *
     * @param observationIds Comma separated list of Observation Ids
     * @return Filtering by observation ids
     */
    @GET
    @Path("list/user")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(Observation[].class)
    public Response getObservationsForUser(
            @QueryParam("observationIds") String observationIds
    ) {
        LOGGER.info("getObservationsForUser for observationIds={}", observationIds);
        try {
            VipsLogicUser user = userBean.getUserFromUUID(httpServletRequest);
            if (user != null) {
                LOGGER.info("Get observations for user={}", user.getUserId());
                List<Observation> allObs = observationBean.getObservationsForUser(user);
                LOGGER.info("Found {} observations for user {}", allObs.size(), user.getUserId());
                if (observationIds != null) {
                    Set<Integer> observationIdSet = Arrays.asList(observationIds.split(",")).stream()
                            .map(s -> Integer.valueOf(s))
                            .collect(Collectors.toSet());
                    return Response.ok().entity(
                                    allObs.stream()
                                            .filter(obs -> observationIdSet.contains(obs.getObservationId()))
                                            .collect(Collectors.toList())
                            )
                            .build();
                }
                return Response.ok().entity(allObs).build();
            } else {
                return Response.status(Status.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            LOGGER.error("Exception when getting observations for user", e);
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    /**
     * Get minimized (only synchronization info) observations for a user
     * Used by the Observation app
     * Requires a valid UUID to be provided in the Authorization header
     *
     * @return
     */
    @GET
    @Path("list/minimized/user")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(ObservationSyncInfo[].class)
    public Response getMinimizedObservationsForUser() {
        VipsLogicUser user = userBean.getUserFromUUID(httpServletRequest);
        if (user != null) {
            return Response.ok().entity(observationBean.getObservationsForUser(user).stream()
                    .map(obs -> new ObservationSyncInfo(obs)).collect(Collectors.toList())).build();
        } else {
            return Response.status(Status.UNAUTHORIZED).build();
        }
    }

    /**
     * Publicly available observations per organization
     *
     * @param organizationId Database id of the organization
     * @return APPROVED observations
     */
    @GET
    @Path("broadcast/list/{organizationId}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    @TypeHint(Observation[].class)
    public Response getBroadcastObservations(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("season") Integer season,
            @QueryParam("timeOfObservationFrom") String timeOfObservationFrom,
            @QueryParam("timeOfObservationTo") String timeOfObservationTo
    ) {
        if ((timeOfObservationFrom != null && !timeOfObservationFrom.isEmpty())
                || (timeOfObservationTo != null && !timeOfObservationTo.isEmpty())) {
            Date from = null;
            Date to = null;
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                if (timeOfObservationFrom != null && !timeOfObservationFrom.isEmpty()) {
                    from = format.parse(timeOfObservationFrom);
                }
                if (timeOfObservationTo != null && !timeOfObservationTo.isEmpty()) {
                    to = format.parse(timeOfObservationTo);
                }
                return Response.ok().entity(observationBean.getBroadcastObservations(organizationId, from, to)).build();
            } catch (ParseException ex) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid date format").build();
            }
        } else {
            return Response.ok().entity(observationBean.getBroadcastObservations(organizationId, season)).build();
        }
    }

    /**
     * Get one observation
     *
     * @param observationId Database ID of the observation
     * @param userUUID      UUID that identifies the user (e.g. from VIPSWeb)
     * @return
     */
    @GET
    @Path("{observationId}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(Observation.class)
    public Response getObservation(
            @PathParam("observationId") Integer observationId,
            @QueryParam("userUUID") String userUUID
    ) {
        // Observation needs to be masked here as well, or does it create trouble for VIPSLogic observation admin?
        Observation o = observationBean.getObservation(observationId);
        if (o == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        // Which organization does this observation belong to?
        VipsLogicUser observer = userBean.getVipsLogicUser(o.getUserId());
        o.setObservationDataSchema(observationBean.getObservationDataSchema(observer.getOrganizationId().getOrganizationId(), o.getOrganismId()));

        VipsLogicUser user = (VipsLogicUser) httpServletRequest.getSession().getAttribute("user");
        if (user == null && userUUID != null) {
            user = userBean.findVipsLogicUser(UUID.fromString(userUUID));
        }
        // Modification of location information:
        // 1) If location is private, only the owner or super users/org admins may view them
        // slett all geoInfo
        if (user == null || (!user.isSuperUser() && !user.isOrganizationAdmin())) {
            // Hide completely for all users except super and orgadmin
            if (o.getLocationIsPrivate() && (user == null || !o.getUserId().equals(user.getUserId()))) {
                o.setGeoinfos(null);
            }
            // This means the user wants to hide the exact location,
            // so mask for all users except super and orgadmin
            else if (o.getPolygonService() != null) {
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

    /**
     * Polygon services are used to mask observations (privacy concerns)
     * They may vary greatly between organizations (different countries)
     *
     * @param organizationId Database id of the organization
     * @return A list of available polygon services for the requested organization
     */
    @GET
    @Path("polygonservices/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(PolygonService[].class)
    public Response getPolygonServicesForOrganization(
            @PathParam("organizationId") Integer organizationId
    ) {
        return Response.ok().entity(observationBean.getPolygonServicesForOrganization(organizationId)).build();
    }

    /**
     * Deletes a gis entity and its corresponding observation
     *
     * @param gisId Database id of the gis entity
     * @return
     */
    @DELETE
    @Path("gisobservation/{gisId}")
    public Response deleteGisObservation(@PathParam("gisId") Integer gisId) {
        VipsLogicUser user = (VipsLogicUser) httpServletRequest.getSession().getAttribute("user");
        // If no user, send error message back to client
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        if (!userBean.authorizeUser(user,
                VipsLogicRole.OBSERVER,
                VipsLogicRole.OBSERVATION_AUTHORITY,
                VipsLogicRole.ORGANIZATION_ADMINISTRATOR,
                VipsLogicRole.SUPERUSER
        )
        ) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        observationBean.deleteGisObservationByGis(gisId);
        return Response.noContent().build();
    }

    /**
     * Stores an observation from geoJson
     *
     * @param geoJSON
     * @return the Url of the created entity (observation)
     */
    // TODO Authentication
    @POST
    @Path("gisobservation")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public Response storeGisObservation(String geoJSON) {
        try {
            // Create the Observation
            Observation observation = observationBean.getObservationFromGeoJSON(geoJSON);
            VipsLogicUser user = (VipsLogicUser) httpServletRequest.getSession().getAttribute("user");
            // If no user, send error message back to client
            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            if (!userBean.authorizeUser(user,
                    VipsLogicRole.OBSERVER,
                    VipsLogicRole.OBSERVATION_AUTHORITY,
                    VipsLogicRole.ORGANIZATION_ADMINISTRATOR,
                    VipsLogicRole.SUPERUSER
            )
            ) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
            observation.setUserId(user.getUserId());
            observation.setStatusChangedByUserId(user.getUserId());
            observation.setStatusChangedTime(new Date());
            observation = observationBean.storeObservation(observation);
            GISEntityUtil gisUtil = new GISEntityUtil();
            return Response.created(URI.create("/observation/" + observation.getObservationId())).entity(gisUtil.getGeoJSONFromObservation(observation)).build();
        } catch (IOException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex).build();
        }
    }

    /**
     * Returns the time of the first observation in the system of the pest with given Id
     *
     * @param organismId Database id of the given organism
     * @return the time of the first observation in the system of the pest with given Id
     */
    @GET
    @Path("first/{organismId}")
    @Produces("text/plain;charset=UTF-8")
    @TypeHint(Date.class)
    public Response getFirstObservation(@PathParam("organismId") Integer organismId) {
        Date firstObsTime = observationBean.getFirstObservationTime(organismId);
        return firstObsTime != null ? Response.ok().entity(firstObsTime).build()
                : Response.status(404).entity("No observations of organism with id=" + organismId).build();
    }

    /**
     * When was the last time a change was made in cropCategories or organisms? Used for sync with the Field observation
     * app.
     *
     * @return last time a change was made in cropCategories or organisms
     * @responseExample application/json {"lastUpdated": "2021-02-08T00:00:00Z"}
     */
    @GET
    @Path("organismsystemupdated")
    @Produces(MediaType.APPLICATION_JSON)
    @TypeHint(Date.class)
    public Response getDateOfLastOrganismSystemUpdate() {
        HashMap<String, Object> result = new HashMap<>();
        Instant lastUpdated = organismBean.getLatestUpdateOfOrganisms();
        result.put("lastUpdated", lastUpdated != null ? lastUpdated : "1970-01-01T00:00:00Z");
        return Response.ok().entity(result).build();
    }

    /**
     * @param organizationId Database id of the organization
     * @param observationTimeSeriesId Database id of the observation time series
     * @param pestId         Database id of the pest
     * @param cropId         Database id of the crop
     * @param cropCategoryId Database ids of the crop categories
     * @param fromStr        format "yyyy-MM-dd"
     * @param toStr          format "yyyy-MM-dd"
     * @param user           The user that requests this (used for authorization)
     * @return A list of observations that meets the filter criteria
     */
    private List<Observation> getFilteredObservationsFromBackend(
            Integer organizationId,
            Integer observationTimeSeriesId,
            Integer pestId,
            Integer cropId,
            List<Integer> cropCategoryId,
            String fromStr,
            String toStr,
            Boolean isPositive,
            VipsLogicUser user
    ) {
        List<Observation> filteredObservations = this.getFilteredObservationsFromBackend(organizationId, observationTimeSeriesId, pestId, cropId, cropCategoryId, fromStr, toStr, isPositive);

        // If superuser or orgadmin: Return everything, unchanged, uncensored
        if (user != null && (user.isSuperUser() || user.isOrganizationAdmin())) {
            LOGGER.info("Return uncensored list of {} observations to {}", filteredObservations.size(), (user.isSuperUser() ? "super" : "admin") + " user");
            return sortObservationsByDateAndId(filteredObservations);
        }
        List<Observation> retVal = filteredObservations.stream().filter(obs -> obs.getBroadcastMessage() || (isPositive == null || !isPositive)).collect(Collectors.toList());
        //retVal.forEach(o->System.out.println(o.getObservationId()));
        retVal = this.maskObservations(retVal);
        //retVal.forEach(o->System.out.println(o.getObservationId()));
        // If user is not logged in, return only the publicly available observations
        if (user == null) {
            LOGGER.info("Return {} masked public observations for unregistered user", retVal.size());
            return sortObservationsByDateAndId(retVal);
        }
         // Else: This is a registered user without special privileges. Show public observations + user's own
        // Making sure we don't add duplicates
        Set<Integer> obsIds = retVal.stream().map(o->o.getObservationId()).collect(Collectors.toSet());
        retVal.addAll(observationBean.getObservationsForUser(user).stream().filter(o->!obsIds.contains(o.getObservationId())).collect(Collectors.toList()));
        LOGGER.info("Return {} masked public observations and user's own observations for registered user {}", retVal.size(), user.getUserId());
        return sortObservationsByDateAndId(retVal);
    }

    /**
     * Runs through the observations, check each to see if it should be masked
     * (for privacy reasons) through a polygon service
     *
     * @param observations The list of observations to check
     * @return The list of observations, with the locations masked with the selected polygon service
     */
    private List<Observation> maskObservations(List<Observation> observations) {
        //System.out.println("maskObservations(List<Observation> observations)");

        // Placing all observations with a polygon service in the correct bucket.
        Map<PolygonService, List<Observation>> registeredPolygonServicesInObservationList = new HashMap<>();
        observations.stream().filter((obs) -> {
            return (!obs.getLocationIsPrivate() && obs.getPolygonService() != null);
        }).forEachOrdered((obs) -> {
            List<Observation> obsWithPolyServ = registeredPolygonServicesInObservationList.getOrDefault(obs.getPolygonService(), new ArrayList<>());
            obsWithPolyServ.add(obs);
            registeredPolygonServicesInObservationList.put(obs.getPolygonService(), obsWithPolyServ);
        });

        // No buckets filled = No masking needed, return list unmodified
        if (registeredPolygonServicesInObservationList.isEmpty()) {
            return observations;
        } else {
            // Loop through, mask
            Map<Integer, Observation> maskedObservations = new HashMap<>();
            registeredPolygonServicesInObservationList.keySet().forEach((pService) -> {
                this.maskObservations(pService, registeredPolygonServicesInObservationList.get(pService))
                        .forEach(o -> maskedObservations.put(o.getObservationId(), o));
            });

            // Adding the rest of the observations (the ones that don't need masking)
            observations.stream().filter(o -> maskedObservations.get(o.getObservationId()) == null).forEach(o -> maskedObservations.put(o.getObservationId(), o));
            return new ArrayList<>(maskedObservations.values());
        }
    }

    /**
     * @param polygonService The polygon service that should be used for masking
     * @param observations   The list of observations to mask
     * @return The list of observations, with the locations masked with the selected polygon service
     */
    private List<Observation> maskObservations(PolygonService polygonService, List<Observation> observations) {
        //observations.forEach(o->System.out.println(o.getObservationId()));
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(polygonService.getGisSearchUrlTemplate());
        List<ReferencedPoint> points = observations.stream()
                .filter(obs -> (obs.getGeoinfos() != null && !obs.getGeoinfos().isEmpty()) || obs.getLocation() != null)
                .map(obs -> {
                    ReferencedPoint rp = new ReferencedPoint();
                    rp.setId(String.valueOf(obs.getObservationId()));
                    if (obs.getGeoinfos() != null) {
                        rp.setLon(obs.getGeoinfos().get(0).getGisGeom().getCoordinate().x);
                        rp.setLat(obs.getGeoinfos().get(0).getGisGeom().getCoordinate().y);
                    } else {
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
        for (Feature feature : response.getFeatureCollection().getFeatures()) {
            indexedPolygons.put((Integer) feature.getProperties().get("id"), feature);
        }
        GISEntityUtil gisEntityUtil = new GISEntityUtil();
        for (Map mapping : response.getMapping()) {
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

    /**
     * This service is used by the VIPS Field observation app to sync data stored locally on the smartphone with the
     * state of an (potentially non-existent) observation in the VIPSLogic database
     *
     * @param observationJson Json representation of the observation(s)
     * @return The observation(s) in their merged state, serialized to Json
     */
    @POST
    @Path("syncobservationfromapp")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(Observation.class)
    public Response syncObservationFromApp(
            String observationJson
    ) {
        LOGGER.info("In syncObservationFromApp");

        try {
            VipsLogicUser user = userBean.getUserFromUUID(httpServletRequest);
            if (user == null) {
                LOGGER.warn("Unable to get user from UUID");
                return Response.status(Status.UNAUTHORIZED).build();
            }
            ObjectMapper oM = new ObjectMapper();
            SimpleDateFormat df = new SimpleDateFormat(Globals.defaultTimestampFormat);
            try {
                Map<Object, Object> mapFromApp = oM.readValue(observationJson, new TypeReference<HashMap<Object, Object>>() {
                });
                LOGGER.info("Syncing for user {} with roles {}. mapFromApp.get(\"userId\")={}", user.getUserId(), user.getVipsLogicRoles(), mapFromApp.get("userId"));

                // Check if it is marked as deleted or not
                if (mapFromApp.get("deleted") != null && ((Boolean) mapFromApp.get("deleted").equals(true))) {
                    if (observationBean.getObservation((Integer) mapFromApp.get("observationId")) != null) {
                        observationBean.deleteObservation((Integer) mapFromApp.get("observationId"));
                        return Response.ok().build();
                    } else {
                        return Response.status(Status.NOT_FOUND).build();
                    }
                } else {
                    Integer observationId = (Integer) mapFromApp.get("observationId");
                    Date now = new Date(); // For setting timestamps

                    Observation mergeObs;
                    if (observationId > 0) { // Observation already in database
                        mergeObs = observationBean.getObservation(observationId);
                        if (mergeObs == null) {
                            LOGGER.warn("Observation with id {} not found", observationId);
                            return Response.status(Status.NOT_FOUND).build();
                        }
                    } else { // New observation!
                        mergeObs = new Observation("APP");
                    }

                    // Observation time series
                    if (mapFromApp.get("observationTimeSeriesId") != null) {
                        Integer observationTimeSeriesId = (Integer) mapFromApp.get("observationTimeSeriesId");
                        mergeObs.setObservationTimeSeries(observationTimeSeriesBean.getObservationTimeSeries(observationTimeSeriesId));
                    }
                    // Pest organism
                    mergeObs.setOrganism(organismBean.getOrganism((Integer) mapFromApp.get("organismId")));
                    // Crop organism
                    mergeObs.setCropOrganism(organismBean.getOrganism((Integer) mapFromApp.get("cropOrganismId")));
                    // Other properties
                    mergeObs.setTimeOfObservation(oM.convertValue(mapFromApp.get("timeOfObservation"), new TypeReference<Date>() {
                    }));
                    mergeObs.setIsPositive(mapFromApp.get("isPositive") != null ? (Boolean) mapFromApp.get("isPositive") : false);
                    mergeObs.setUserId(mapFromApp.get("userId") != null ? (Integer) mapFromApp.get("userId") : user.getUserId());
                    mergeObs.setGeoinfo((String) mapFromApp.get("geoinfo"));
                    mergeObs.setLocationPointOfInterestId(mapFromApp.get("locationPointOfInterestId") != null ? (Integer) mapFromApp.get("locationPointOfInterestId") : null);

                    mergeObs.setObservationHeading(getStringPropertyOrNull(mapFromApp, "observationHeading"));
                    mergeObs.setObservationText(getStringPropertyOrNull(mapFromApp, "observationText"));
                    mergeObs.setBroadcastMessage(mapFromApp.get("broadcastMessage") != null ? (Boolean) mapFromApp.get("broadcastMessage") : false);

                    mergeObs.setIsQuantified(mapFromApp.get("isQuantified") != null ? (Boolean) mapFromApp.get("isQuantified") : false);
                    mergeObs.setLocationIsPrivate(mapFromApp.get("locationIsPrivate") != null ? (Boolean) mapFromApp.get("locationIsPrivate") : false);
                    Object polygonServiceValue = mapFromApp.get("polygonService");
                    if (polygonServiceValue != null && !polygonServiceValue.toString().isBlank()) {
                        PolygonService polygonService = oM.convertValue(mapFromApp.get("polygonService"), PolygonService.class);
                        mergeObs.setPolygonService(polygonService);
                    }

                    mergeObs.setObservationDataSchema(observationBean.getObservationDataSchema(user.getOrganization_id(), mergeObs.getOrganismId()));

                    mergeObs.setObservationData(getStringPropertyOrNull(mapFromApp, "observationData"));
                    mergeObs.setLastEditedBy(user.getUserId());
                    mergeObs.setLastEditedTime(now);

                    boolean sendNotification = false; // Notification should be sent if status is set to approved
                    boolean newRegistration = mergeObs.getObservationId() == null || mergeObs.getObservationId() <= 0;
                    boolean automaticApproval = userBean.authorizeUser(user, VipsLogicRole.OBSERVATION_AUTHORITY, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER);
                    if (newRegistration) {
                        if (automaticApproval) {
                            LOGGER.info("Set status to approved for new observation registered by user {}", user.getUserId());
                            mergeObs.setStatusChangedByUserId(user.getUserId());
                            mergeObs.setStatusChangedTime(now);
                            mergeObs.setStatusTypeId(ObservationStatusType.STATUS_APPROVED);
                            sendNotification = mergeObs.getBroadcastMessage(); // Only send for approved observations
                        } else {
                            LOGGER.info("Set status to pending for new observation registered by user {}", user.getUserId());
                            mergeObs.setStatusTypeId(Observation.STATUS_TYPE_ID_PENDING);
                        }
                    } else {
                        // Existing observation
                        Integer newStatusTypeId = (Integer) mapFromApp.get("statusTypeId");
                        Integer originalStatusTypeId = mergeObs.getStatusTypeId();
                        if (automaticApproval && ObservationStatusType.STATUS_PENDING.equals(newStatusTypeId) && ObservationStatusType.STATUS_PENDING.equals(originalStatusTypeId)) {
                            LOGGER.info("Set status to approved for existing observation {} and user {}", mergeObs.getObservationId(), user.getUserId());
                            mergeObs.setStatusChangedByUserId(user.getUserId());
                            mergeObs.setStatusChangedTime(now);
                            mergeObs.setStatusTypeId(ObservationStatusType.STATUS_APPROVED);
                            sendNotification = mergeObs.getBroadcastMessage(); // Only send for approved observations
                        }
                        // No option for changing the status in registration form in app
                    }

                    // Input check before storing, location must be set
                    if ((mergeObs.getGeoinfo() == null || mergeObs.getGeoinfo().trim().isEmpty()) && mergeObs.getLocationPointOfInterestId() == null) {
                        LOGGER.error("The observation is missing location data, return bad request.");
                        return Response.status(Status.BAD_REQUEST).entity("{\"error\": \"The observation is missing location data.\"}").type(MediaType.APPLICATION_JSON).build();
                    }

                    // We need to get an observation Id before storing the illustrations!
                    mergeObs = observationBean.storeObservation(mergeObs);

                    // ObservationIllustrationSet
                    // Including data that may need to be stored
                    if (mapFromApp.get("observationIllustrationSet") != null) {
                        List<Map<Object, Object>> illusMaps = (List<Map<Object, Object>>) mapFromApp.get("observationIllustrationSet");
                        for (Map<Object, Object> illusMap : illusMaps) {
                            ObservationIllustrationPK pk = oM.convertValue(illusMap.get("observationIllustrationPK"), new TypeReference<ObservationIllustrationPK>() {
                            });

                            if (illusMap.get("deleted") != null && ((Boolean) illusMap.get("deleted")) == true) {
                                observationBean.deleteObservationIllustration(mergeObs, new String[]{pk.getFileName()});
                            } else if (illusMap.get("uploaded") != null && ((Boolean) illusMap.get("uploaded")) == false && illusMap.get("imageTextData") != null) {
                                mergeObs = observationBean.storeObservationIllustration(mergeObs, pk.getFileName(), (String) illusMap.get("imageTextData"));
                            }
                        }
                    }
                    String disableMessagingSystemProperty = System.getProperty("no.nibio.vips.logic.DISABLE_MESSAGING_SYSTEM");
                    boolean messagingSystemDisabled = disableMessagingSystemProperty != null && disableMessagingSystemProperty.equals("true");

                    LOGGER.info("Send notification? " + sendNotification);
                    LOGGER.info("Messaging system enabled? " + !messagingSystemDisabled);

                    // All transactions finished, we can send notifications
                    // if conditions are met
                    if (sendNotification && !messagingSystemDisabled) {
                        LOGGER.info("Send message");
                        messagingBean.sendUniversalMessage(mergeObs);
                    }

                    return Response.ok().entity(mergeObs).build();
                }
            } catch (IOException e) {
                return Response.serverError().entity(e).build();
            }
        } catch (Exception e) {
            LOGGER.error("Exception occurred while syncing observations from app", e);
            return Response.serverError().entity(e).build();
        }
    }

    /**
     * Sort observations by date in descending order, and by id in descending order if dates are identical
     *
     * @param observations The original list of unordered observations
     * @return a sorted list of observations
     */
    private List<Observation> sortObservationsByDateAndId(List<Observation> observations) {
        return observations.stream()
                .sorted((o1, o2) -> {
                    int timeCompare = o2.getTimeOfObservation().compareTo(o1.getTimeOfObservation());
                    if (timeCompare != 0) {
                        return timeCompare;
                    } else {
                        return Integer.compare(o2.getObservationId(), o1.getObservationId());
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Utility method for getting the string value of a given property in a given map.
     *
     * @param map          The map from which to retrieve the value
     * @param propertyName The name of the property
     * @return The value of the given property, null if not existing or empty
     */
    private String getStringPropertyOrNull(Map<Object, Object> map, String propertyName) {
        Object mapValue = map.get(propertyName);
        if (mapValue == null) {
            return null;
        }
        String strMapValue = (String) mapValue;
        return !strMapValue.isBlank() ? strMapValue : null;
    }

}
