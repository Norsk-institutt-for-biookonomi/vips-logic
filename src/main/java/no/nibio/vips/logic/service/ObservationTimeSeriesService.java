package no.nibio.vips.logic.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import no.nibio.vips.logic.controller.session.ObservationTimeSeriesBean;
import no.nibio.vips.logic.controller.session.OrganismBean;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.Gis;
import no.nibio.vips.logic.entity.ObservationTimeSeries;
import no.nibio.vips.logic.entity.PolygonService;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.entity.rest.PointMappingResponse;
import no.nibio.vips.logic.entity.rest.ReferencedPoint;
import no.nibio.vips.logic.util.GISEntityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wololo.geojson.Feature;

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
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Path("rest/observationtimeseries")
public class ObservationTimeSeriesService {

    public static final String APPLICATION_JSON = "application/json;charset=UTF-8";
    private final static Logger LOGGER = LoggerFactory.getLogger(ObservationTimeSeriesService.class);
    private static final String DELETED = "deleted";
    private static final String OBSERVATION_TIME_SERIES_ID = "observationTimeSeriesId";
    private static final String ORGANISM_ID = "organismId";
    private static final String CROP_ORGANISM_ID = "cropOrganismId";
    private static final String USER_ID = "userId";
    private static final String LOCATION_POINT_OF_INTEREST_ID = "locationPointOfInterestId";
    private static final String YEAR = "year";
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String LOCATION_IS_PRIVATE = "locationIsPrivate";
    private static final String POLYGON_SERVICE = "polygonService";
    @EJB
    UserBean userBean;
    @EJB
    ObservationTimeSeriesBean observationTimeSeriesBean;
    @EJB
    OrganismBean organismBean;

    @Context
    private HttpServletRequest httpServletRequest;

    @GET
    @Path("list/user")
    @Produces(APPLICATION_JSON)
    @TypeHint(ObservationTimeSeries[].class)
    public Response getObservationsTimeSeriesForUser(@QueryParam("observationTimeSeriesIds") String otsIds) {
        LOGGER.info("In getObservationsTimeSeriesForUser observationTimeSeriesIds=" + otsIds);
        try {
            VipsLogicUser user = userBean.getUserFromUUID(httpServletRequest);
            if (user != null) {
                List<ObservationTimeSeries> allObs = observationTimeSeriesBean.getObservationTimeSeriesListForUser(user);
                if (otsIds != null) {
                    Set<Integer> observationIdSet = Arrays.stream(otsIds.split(","))
                            .map(Integer::valueOf)
                            .collect(Collectors.toSet());
                    return Response.ok().entity(
                                    allObs.stream()
                                            .filter(obs -> observationIdSet.contains(obs.getObservationTimeSeriesId()))
                                            .collect(Collectors.toList())
                            )
                            .build();
                }
                return Response.ok().entity(allObs).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    /**
     * Get one observation time series
     *
     * @param observationTimeSeriesId Database ID of the observation time series
     * @param userUUID                UUID that identifies the user (e.g. from VIPSWeb)
     * @return
     */
    @GET
    @Path("{observationTimeSeriesId}")
    @Produces(APPLICATION_JSON)
    @TypeHint(ObservationTimeSeries.class)
    public Response getObservationTimeSeries(@PathParam("observationTimeSeriesId") Integer observationTimeSeriesId, @QueryParam("userUUID") String userUUID) {
        ObservationTimeSeries ots = observationTimeSeriesBean.getObservationTimeSeries(observationTimeSeriesId);
        if (ots == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        VipsLogicUser requester = (VipsLogicUser) httpServletRequest.getSession().getAttribute("user");
        if (requester == null && userUUID != null) {
            requester = userBean.findVipsLogicUser(UUID.fromString(userUUID));
        }

        boolean requesterNotValidUser = requester == null;
        boolean requesterRegularUser = requester != null && !requester.isSuperUser() && !requester.isOrganizationAdmin();
        boolean requesterNotCreator = requester != null && !ots.getUserId().equals(requester.getUserId());

        if (requesterNotValidUser || requesterRegularUser) {
            // Mask for all users except creator, super and orgadmin
            if (!(ots.getLocationIsPrivate() && (requesterNotValidUser || requesterNotCreator)) && ots.getPolygonService() != null) {
                observationTimeSeriesBean.enrichObservationTimeSeriesWithPointOfInterest(ots);
                this.maskLocation(ots.getPolygonService(), ots);
            }
        }
        return Response.ok().entity(ots).build();
    }

    /**
     * The location (point of interest) of the given observation time series is masked using the provided polygon service.
     *
     * @param polygonService        The polygon service that should be used for masking
     * @param observationTimeSeries The observation time series to mask location for
     * @return The observation time series, with location masked with the provided polygon service
     */
    private void maskLocation(PolygonService polygonService, ObservationTimeSeries observationTimeSeries) {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(polygonService.getGisSearchUrlTemplate());
        ReferencedPoint rp = new ReferencedPoint();
        rp.setId(String.valueOf(observationTimeSeries.getObservationTimeSeriesId()));
        rp.setLon(observationTimeSeries.getLocationPointOfInterest().getLongitude());
        rp.setLat(observationTimeSeries.getLocationPointOfInterest().getLatitude());

        ReferencedPoint[] pointArray = {rp};
        PointMappingResponse response = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(pointArray, MediaType.APPLICATION_JSON), PointMappingResponse.class);
        // We need to loop through the observations and find corresponding featurecollections and replace those
        Map<Integer, Feature> indexedPolygons = new HashMap<>();
        for (Feature feature : response.getFeatureCollection().getFeatures()) {
            indexedPolygons.put((Integer) feature.getProperties().get(OBSERVATION_TIME_SERIES_ID), feature);
        }
        GISEntityUtil gisEntityUtil = new GISEntityUtil();
        for (Map mapping : response.getMapping()) {
            Integer observationTimeSeriesId = Integer.valueOf((String) mapping.get(OBSERVATION_TIME_SERIES_ID));
            if (observationTimeSeries.getObservationTimeSeriesId().equals(observationTimeSeriesId)) {
                Integer borderId = (Integer) mapping.get("borderid");
                Gis polygon = gisEntityUtil.getGisFromFeature(indexedPolygons.get(borderId));
                List<Gis> gis = new ArrayList<>();
                gis.add(polygon);
                observationTimeSeries.setLocationPointOfInterest(null);
                observationTimeSeries.setLocationPointOfInterestId(null);
            }
        }
    }

    /**
     * This service is used by the VIPS Field observation app to sync data stored locally on the smartphone with the
     * state of a (potentially non-existent) observation time series in the VIPSLogic database
     *
     * @param jsonOts Json representation of the observation time series
     * @return The observation time series in its merged state, serialized to Json
     */
    @POST
    @Path("syncfromapp")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @TypeHint(ObservationTimeSeries.class)
    public Response syncObservationTimeSeriesFromApp(String jsonOts) {
        LOGGER.info("In syncObservationTimeSeriesFromApp");
        LOGGER.info(jsonOts);
        VipsLogicUser user = userBean.getUserFromUUID(httpServletRequest);
        if (user == null) {
            LOGGER.warn("Unable to get user from UUID");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        LOGGER.info("Syncing for user {} with roles {}", user.getUserId(), user.getVipsLogicRoles());
        ObjectMapper oM = new ObjectMapper();
        try {
            Map<Object, Object> mapFromApp = oM.readValue(jsonOts, new TypeReference<HashMap<Object, Object>>() {
            });

            Integer otsId = (Integer) mapFromApp.get(OBSERVATION_TIME_SERIES_ID);

            // Check if the observation time series is marked as deleted
            Boolean isDeleted = (Boolean) mapFromApp.getOrDefault(DELETED, false);
            if (isDeleted) {
                // If marked as deleted, delete the observation time series if it exists
                // Observations in time series are also deleted, but the app currently prevents
                // deletion of time series with content
                if (observationTimeSeriesBean.getObservationTimeSeries(otsId) != null) {
                    observationTimeSeriesBean.deleteObservationTimeSeries(otsId);
                    LOGGER.info("ObservationTimeSeries with id={} deleted", otsId);
                    return Response.ok().build();
                } else {
                    LOGGER.warn("ObservationTimeSeries with id={} not found, nothing deleted", otsId);
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
            }

            Date currentDate = new Date();
            ObservationTimeSeries otsToSave;
            if (otsId != null && otsId > 0) {
                otsToSave = observationTimeSeriesBean.getObservationTimeSeries(otsId);
                if (otsToSave == null) {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
            } else {
                otsToSave = new ObservationTimeSeries("APP");
                otsToSave.setUserId(user.getUserId());
                otsToSave.setCreated(currentDate);
            }
            otsToSave.setCropOrganism(organismBean.getOrganism(getValueFromMap(mapFromApp, CROP_ORGANISM_ID, Integer.class)));
            otsToSave.setOrganism(organismBean.getOrganism(getValueFromMap(mapFromApp, ORGANISM_ID, Integer.class)));
            otsToSave.setLocationPointOfInterestId(getValueFromMap(mapFromApp, LOCATION_POINT_OF_INTEREST_ID, Integer.class));
            otsToSave.setYear(getValueFromMap(mapFromApp, YEAR, Integer.class));
            otsToSave.setName(getValueFromMap(mapFromApp, NAME, String.class));
            otsToSave.setDescription(getValueFromMap(mapFromApp, DESCRIPTION, String.class));
            otsToSave.setLocationIsPrivate(getValueFromMap(mapFromApp, LOCATION_IS_PRIVATE, Boolean.class));

            Object polygonServiceValue = mapFromApp.get(POLYGON_SERVICE);
            if (polygonServiceValue != null && !polygonServiceValue.toString().isBlank()) {
                PolygonService polygonService = oM.convertValue(polygonServiceValue, PolygonService.class);
                otsToSave.setPolygonService(polygonService);
            }

            otsToSave.setLastModified(currentDate);
            otsToSave.setLastModifiedBy(user.getUserId());

            // Input check before storing, location must be set
            if (otsToSave.getLocationPointOfInterestId() == null) {
                LOGGER.error("The observation time series is missing location data");
                return Response.status(Response.Status.BAD_REQUEST).entity("The observation time series is missing location data").build();
            }
            LOGGER.info("otsToSave before storing: " + otsToSave);
            otsToSave = observationTimeSeriesBean.storeObservationTimeSeries(otsToSave);
            return Response.ok().entity(otsToSave).build();
        } catch (IOException e) {
            LOGGER.error("IOException on save ObservationTimeSeries", e);
            return Response.serverError().entity(e).build();
        }
    }

    private <T> T getValueFromMap(Map<Object, Object> map, String key, Class<T> clazz) {
        Object value = map.get(key);
        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        } else if (clazz == Integer.class && value instanceof String) {
            try {
                return clazz.cast(Integer.parseInt((String) value));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return clazz == Boolean.class ? clazz.cast(false) : null;
    }
}
