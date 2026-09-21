package no.nibio.vips.logic.forecast;

import com.webcohesion.enunciate.metadata.Facet;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import no.nibio.vips.logic.dto.ForecastResultDto;
import no.nibio.vips.logic.entity.ForecastResult;

@ApplicationScoped
@Path("forecastresults")
public class ForecastResultResource {

    private final ForecastService forecastService;

    @Inject
    public ForecastResultResource(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @GET
    @Path("{forecastConfigurationId}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(ForecastResultDto[].class)
    public Response getForecastResults(
            @PathParam("forecastConfigurationId") Long forecastConfigurationId,
            @QueryParam("userUUID") String userUUID) {
        if (forecastService.isUserAuthorizedForForecastConfiguration(forecastConfigurationId, userUUID)) {
            return Response.ok().entity(forecastService.getForecastResultDtos(forecastConfigurationId)).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @GET
    @Path("{forecastConfigurationId}/csv")
    @Produces("text/csv;charset=UTF-8")
    public Response getForecastResultsCSV(
            @PathParam("forecastConfigurationId") Long forecastConfigurationId,
            @QueryParam("userUUID") String userUUID) {
        if (forecastService.isUserAuthorizedForForecastConfiguration(forecastConfigurationId, userUUID)) {
            return Response.ok().entity(forecastService.getForecastResultsCsv(forecastConfigurationId)).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @GET
    @Path("{forecastConfigurationId}/{latestDays}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(ForecastResult[].class)
    public Response getForecastResults(
            @PathParam("forecastConfigurationId") Long forecastConfigurationId,
            @PathParam("latestDays") Integer latestDays,
            @QueryParam("userUUID") String userUUID) {
        if (forecastService.isUserAuthorizedForForecastConfiguration(forecastConfigurationId, userUUID)) {
            return Response.ok().entity(forecastService.getForecastResultDtos(forecastConfigurationId, latestDays)).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @GET
    @Path("{forecastConfigurationId}/{dateStart}/{dateEnd}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(ForecastResult[].class)
    public Response getForecastResults(
            @PathParam("forecastConfigurationId") Long forecastConfigurationId,
            @PathParam("dateStart") String dateStartStr,
            @PathParam("dateEnd") String dateEndStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date dateStart = format.parse(dateStartStr);
            Date dateEnd = format.parse(dateEndStr);
            return Response.ok().entity(forecastService.getForecastResults(forecastConfigurationId, dateStart, dateEnd)).build();
        } catch (ParseException ex) {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }

    @GET
    @Path("aggregate/{organizationId}")
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    @Facet("restricted")
    public Response getForecastResultsAggregate(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("cropCategoryId") List<Integer> cropCategoryIds,
            @QueryParam("userUUID") String userUUID) {
        Kml retVal = forecastService.getForecastsAggregateKml(organizationId, cropCategoryIds, userUUID);
        if (retVal == null) {
            return Response.noContent().build();
        }
        return Response.ok().entity(retVal).build();
    }

    @GET
    @Path("aggregate/orgspan")
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    @Facet("restricted")
    public Response getForecastResultsAggregate(
            @QueryParam("organizationId") List<Integer> organizationIds,
            @QueryParam("cropCategoryId") List<Integer> cropCategoryIds,
            @QueryParam("userUUID") String userUUID) {
        Kml retVal = forecastService.getForecastsAggregateKmlForOrganizations(organizationIds, cropCategoryIds, userUUID);
        if (retVal == null) {
            return Response.noContent().build();
        }
        return Response.ok().entity(retVal).build();
    }

    @GET
    @Path("latest/poi/{poiId}")
    @Produces("application/json;charset=UTF-8")
    public Response getLatestForecastResultsForPoi(@PathParam("poiId") Integer poiId) {
        return Response.ok().entity(forecastService.getLatestForecastResultsForPoi(poiId)).build();
    }
}
