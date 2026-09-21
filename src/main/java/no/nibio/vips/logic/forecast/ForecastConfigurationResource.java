package no.nibio.vips.logic.forecast;

import com.webcohesion.enunciate.metadata.Facet;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.dto.ForecastConfigurationDto;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.util.SystemTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
@Path("forecastconfigurations")
public class ForecastConfigurationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForecastConfigurationResource.class);

    private final ForecastService forecastService;
    private final UserBean userBean;

    @Inject
    public ForecastConfigurationResource(ForecastService forecastService, UserBean userBean) {
        this.forecastService = forecastService;
        this.userBean = userBean;
    }

    @GET
    @Path("{forecastConfigurationId}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(ForecastConfiguration.class)
    public Response getForecastConfiguration(
            @PathParam("forecastConfigurationId") Long forecastConfigurationId,
            @QueryParam("userUUID") String userUUID) {
        if (forecastService.isUserAuthorizedForForecastConfiguration(forecastConfigurationId, userUUID)) {
            return Response.ok().entity(forecastService.getForecastConfiguration(forecastConfigurationId)).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @GET
    @Path("{forecastConfigurationId}/dto")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(ForecastConfigurationDto.class)
    public Response getForecastConfigurationDto(
            @PathParam("forecastConfigurationId") Long forecastConfigurationId,
            @QueryParam("userUUID") String userUUID) {
        LOGGER.debug("Authorizing user for access to forecast configuration DTO for id " + forecastConfigurationId + " at " + SystemTime.getSystemTime());
        if (forecastService.isUserAuthorizedForForecastConfiguration(forecastConfigurationId, userUUID)) {
            return Response.ok().entity(forecastService.getForecastConfigurationDto(forecastConfigurationId)).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @GET
    @Path("model/{modelId}/{year}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(ForecastConfiguration[].class)
    public Response getForecastConfigurationsForModel(
            @PathParam("modelId") String modelId,
            @PathParam("year") Integer year) {
        return Response.ok().entity(forecastService.getForecastConfigurationsForModel(modelId, year)).build();
    }

    @GET
    @Path("private/{userUUID}")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(ForecastConfiguration[].class)
    public Response getPrivateForecastConfigurations(@PathParam("userUUID") String userUUID) {
        try {
            UUID uUUID = UUID.fromString(userUUID);
            VipsLogicUser user = userBean.findVipsLogicUser(uUUID);
            if (user != null) {
                List<ForecastConfiguration> retVal = forecastService.getPrivateForecastConfigurationsForUser(user.getUserId());
                return Response.ok().entity(retVal).build();
            }
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } catch (NullPointerException npe) {
            return Response.noContent().build();
        }
    }
}
