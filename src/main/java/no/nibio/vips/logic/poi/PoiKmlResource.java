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
@Path("pois")
public class PoiKmlResource {

    private final PointOfInterestService pointOfInterestService;

    @Inject
    public PoiKmlResource(PointOfInterestService pointOfInterestService) {
        this.pointOfInterestService = pointOfInterestService;
    }

    /**
     * Get a KML list of locations (pois) for a given organization
     * @param excludePoiId
     * @param highlightPoiId use this if you want to highlight a specific POI. Should be
     * used in conjunction with excludePoiId
     * @param organizationId
     * @return KML
     */
    @GET
    @Path("kml/{organizationId}")
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    public Response getPois(
            @QueryParam("excludePoiId") Integer excludePoiId,
            @QueryParam("highlightPoiId") Integer highlightPoiId,
            @PathParam("organizationId") Integer organizationId,
            @Context HttpServletRequest httpServletRequest
    )
    {
        Kml retVal = pointOfInterestService.getPoisKml(
                organizationId,
                excludePoiId,
                highlightPoiId,
                Globals.SERVER_NAME,
                SessionLocaleUtil.getI18nBundle(httpServletRequest));
        return Response.ok().entity(retVal).build();
    }
}
