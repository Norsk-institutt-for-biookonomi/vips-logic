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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
@Path("observationtimeseries")
public class ObservationTimeSeriesService {
    private final static Logger LOGGER = LoggerFactory.getLogger(ObservationTimeSeriesService.class);

    public static final String APPLICATION_JSON = "application/json;charset=UTF-8";
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
    @Inject
    UserBean userBean;
    @Inject
    ObservationTimeSeriesBean observationTimeSeriesBean;
    @Inject
    OrganismBean organismBean;

    @GET
    @Path("list/user")
    @Produces(APPLICATION_JSON)
    @TypeHint(ObservationTimeSeries[].class)
    public Response getObservationsTimeSeriesForUser(
            @QueryParam("observationTimeSeriesIds") String otsIds,
            @Context HttpServletRequest httpServletRequest,
            @Context HttpHeaders httpHeaders) {
        try {
            VipsLogicUser user = userBean.resolveCurrentUser(httpServletRequest, httpHeaders);
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
            LOGGER.error("Exception when getting observation time series for user", e);
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
    public Response getObservationTimeSeries(
            @PathParam("observationTimeSeriesId") Integer observationTimeSeriesId,
            @QueryParam("userUUID") String userUUID,
            @Context HttpServletRequest httpServletRequest,
            @Context HttpHeaders httpHeaders) {
        ObservationTimeSeries ots = observationTimeSeriesBean.getObservationTimeSeries(observationTimeSeriesId);
        if (ots == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        VipsLogicUser requester = userBean.resolveCurrentUser(httpServletRequest, httpHeaders);
        if (requester == null && userUUID != null) {
            requester = userBean.findVipsLogicUser(UUID.fromString(userUUID));
        }
        observationTimeSeriesBean.enrichObservationTimeSeriesWithPointOfInterest(ots);

        boolean requesterNotValidUser = requester == null;
        boolean requesterRegularUser = requester != null && !requester.isSuperUser() && !requester.isOrganizationAdmin();
        boolean requesterNotCreator = requester != null && !ots.getUserId().equals(requester.getUserId());

        if (requesterNotValidUser || requesterRegularUser) {
            // Hide completely for all users except creator, super and orgadmin
            if (ots.getLocationIsPrivate() && (requesterNotValidUser || requesterNotCreator)) {
                ots.setGeoInfoList(new ArrayList<>());
            }
            // Mask for all users except creator, super and orgadmin
            else if (ots.getPolygonService() != null) {
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
        try(Client client = ClientBuilder.newClient()){
            WebTarget target = client.target(polygonService.getGisSearchUrlTemplate());
            ReferencedPoint rp = new ReferencedPoint();
            rp.setId(String.valueOf(observationTimeSeries.getObservationTimeSeriesId()));
            rp.setLon(observationTimeSeries.getLocationPointOfInterest().getLongitude());
            rp.setLat(observationTimeSeries.getLocationPointOfInterest().getLatitude());

            ReferencedPoint[] pointArray = {rp};
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                try (Response httpResponse = target.request(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(objectMapper.writeValueAsString(pointArray), MediaType.APPLICATION_JSON))) {
                    PointMappingResponse response = objectMapper.readValue(
                            httpResponse.readEntity(String.class), PointMappingResponse.class);
                    // We need to loop through the observations and find corresponding featurecollections and replace those
                    Map<Integer, Feature> indexedPolygons = new HashMap<>();
                    for (Feature feature : response.getFeatureCollection().getFeatures()) {
                        indexedPolygons.put((Integer) feature.getProperties().get("id"), feature);
                    }
                    GISEntityUtil gisEntityUtil = new GISEntityUtil();
                    for (Map mapping : response.getMapping()) {
                        Integer observationTimeSeriesId = Integer.valueOf((String) mapping.get("id"));
                        if (observationTimeSeries.getObservationTimeSeriesId().equals(observationTimeSeriesId)) {
                            Integer borderId = (Integer) mapping.get("borderid");
                            Gis polygon = gisEntityUtil.getGisFromFeature(indexedPolygons.get(borderId));
                            List<Gis> gis = new ArrayList<>();
                            gis.add(polygon);
                            observationTimeSeries.setGeoInfoList(gis);
                            observationTimeSeries.setLocationPointOfInterest(null);
                            observationTimeSeries.setLocationPointOfInterestId(null);
                        }
                    }
                }
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                throw new RuntimeException(e);
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
    public Response syncObservationTimeSeriesFromApp(
            String jsonOts,
            @Context HttpServletRequest httpServletRequest,
            @Context HttpHeaders httpHeaders) {
        LOGGER.info(jsonOts);
        VipsLogicUser user = userBean.resolveCurrentUser(httpServletRequest, httpHeaders);
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
