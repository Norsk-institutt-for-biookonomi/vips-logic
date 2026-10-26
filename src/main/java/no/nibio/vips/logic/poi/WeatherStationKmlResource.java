package no.nibio.vips.logic.poi;

import de.micromata.opengis.kml.v_2_2_0.Kml;
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
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.util.Globals;

@ApplicationScoped
@Path("weatherstations")
public class WeatherStationKmlResource {

    private final PointOfInterestService pointOfInterestService;

    @Inject
    public WeatherStationKmlResource(PointOfInterestService pointOfInterestService) {
        this.pointOfInterestService = pointOfInterestService;
    }

    /**
     * Get a list of weather stations for a given organization
     * @param excludeWeatherStationId Exclude this weather station from the KML
     * @param highlightWeatherStationId Show highlight icon for this weather station
     * @param organizationId
     * @return a KML with weather stations for an organization
     */
    @GET
    @Path("kml/{organizationId}")
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    public Response getWeatherStations(
            @QueryParam("excludeWeatherStationId") Integer excludeWeatherStationId,
            @QueryParam("highlightWeatherStationId") Integer highlightWeatherStationId,
            @PathParam("organizationId") Integer organizationId,
            @Context HttpServletRequest httpServletRequest
    )
    {
        Kml retVal = pointOfInterestService.getWeatherStationsKml(
                organizationId,
                excludeWeatherStationId,
                highlightWeatherStationId,
                Globals.SERVER_NAME,
                SessionLocaleUtil.getI18nBundle(httpServletRequest));
        return Response.ok().entity(retVal).build();
    }
}
