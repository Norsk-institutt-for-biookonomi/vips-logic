package no.nibio.vips.logic.forecast;

import com.webcohesion.enunciate.metadata.Facet;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ForecastModelConfiguration;
import no.nibio.vips.logic.entity.ModelInformation;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.ServletUtil;

@ApplicationScoped
@Path("")
public class ForecastResource {

    private final ForecastService forecastService;
    private final UserBean userBean;

    @Inject
    public ForecastResource(ForecastService forecastService, UserBean userBean) {
        this.forecastService = forecastService;
        this.userBean = userBean;
    }

    @GET
    @Path("forecastconfigurationsummaries/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(ForecastConfiguration[].class)
    public Response getForecastSummaries(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("cropOrganismId") List<Integer> cropOrganismIds,
            @QueryParam("includeOrganizationIds") String includeOrganizationIds,
            @QueryParam("userUUID") String userUUID) {
        return Response.ok()
                .entity(forecastService.getForecastConfigurationSummaries(organizationId, includeOrganizationIds, userUUID))
                .build();
    }

    @GET
    @Path("forecastconfigurationsummaries/private/{userUUID}")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    public Response getForecastSummaries(@PathParam("userUUID") String userUUID) {
        UUID uUUID = UUID.fromString(userUUID);
        VipsLogicUser user = userBean.findVipsLogicUser(uUUID);
        if (user != null) {
            return Response.ok().entity(forecastService.getPrivateForecastConfigurationSummaries(user)).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @GET
    @Path("forecastconfigurationsincludeorgs/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(ForecastConfiguration[].class)
    public Response getActiveForecastConfigurationsWithIncludeOrganizations(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("includeOrganizationIds") String includeOrganizationIds,
            @QueryParam("from") String fromStr,
            @QueryParam("to") String toStr) {
        Date from = null;
        Date to = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            from = format.parse(fromStr);
            to = format.parse(toStr);
        } catch (ParseException | NullPointerException ex) {
        }
        return Response.ok()
                .entity(forecastService.getActiveForecastConfigurationsWithIncludeOrganizations(
                        organizationId, includeOrganizationIds, from, to))
                .build();
    }

    @GET
    @Path("organizationforecastconfigurations/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(ForecastConfiguration[].class)
    public Response getForecastConfigurationsForOrganization(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("cropOrganismId") List<Integer> cropOrganismIds,
            @QueryParam("from") String fromStr,
            @QueryParam("to") String toStr) {
        Date from = null;
        Date to = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            from = format.parse(fromStr);
            to = format.parse(toStr);
        } catch (ParseException | NullPointerException ex) {
        }
        return Response.ok()
                .entity(forecastService.getForecastConfigurationsForOrganization(
                        organizationId, cropOrganismIds, from, to))
                .build();
    }

    @GET
    @Path("forecastmodelconfiguration/{forecastConfigurationId}")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(ForecastModelConfiguration.class)
    public Response getForecastModelConfiguration(
            @PathParam("forecastConfigurationId") Long forecastConfigurationId) {
        return Response.ok()
                .entity(forecastService.getForecastModelConfigurations(forecastConfigurationId))
                .build();
    }

    @GET
    @Path("batch/updateforecastcaches")
    @Produces("text/plain;charset=UTF-8")
    @Facet("restricted")
    public Response updateForecastCaches(@Context HttpServletRequest httpServletRequest) {
        if (!ServletUtil.getClientIP(httpServletRequest).equals("127.0.0.1")) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        Date start = new Date();
        forecastService.updateForecastCaches();
        Long timeLapsed = new Date().getTime() - start.getTime();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return Response.ok().entity("Forecast caches were successfully updated with data from today ("
                + format.format(SystemTime.getSystemTime())
                + "). Time spent=" + timeLapsed + " milliseconds.\n").build();
    }

    @GET
    @Path("model/{modelId}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(ModelInformation.class)
    public Response getModelInformation(@PathParam("modelId") String modelId) {
        ModelInformation retVal = forecastService.getModelInformation(modelId);
        return retVal != null
                ? Response.ok().entity(retVal).build()
                : Response.status(Response.Status.NOT_FOUND).entity("ERROR: Could not find model with id=" + modelId).build();
    }
}
