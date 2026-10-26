/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.modules.applefruitmoth;

import com.webcohesion.enunciate.metadata.Facet;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import java.util.Calendar;
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
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.logic.util.SystemTime;

/**
 * @copyright 2016-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@ApplicationScoped
@Path("applefruitmoth")
@Facet("restricted")
public class AppleFruitMothService {
    
    @Inject
    AppleFruitMothBean appleFruitMothBean;
    
    @Context
    private HttpServletRequest httpServletRequest;
    
    @GET
    @Path("observationsites/kml/")
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    public Response getObservationSitesKML(
            @QueryParam("onlyPubliclyAvailable") Boolean onlyPubliclyAvailable
    )
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(SystemTime.getSystemTime());
        Integer season = cal.get(Calendar.YEAR);
        return this.getObservationSitesKML(season, null, onlyPubliclyAvailable);
    }
    
    @GET
    @Path("observationsites/kml/{season}/")
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    public Response getObservationSitesKML(
            @PathParam("season") Integer season,
            @QueryParam("selectedObservationSiteId") Integer selectedObservationSiteId,
            @QueryParam("onlyPubliclyAvailable") Boolean onlyPubliclyAvailable
    )
    {
        Kml retVal = appleFruitMothBean.getObservationSitesKml(season, selectedObservationSiteId, Globals.SERVER_NAME, onlyPubliclyAvailable);
        return Response.ok().entity(retVal).build();
    }
    
    /*@GET
    @Path("observationsite/{observationSiteId}/sitepoints/kml/")
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    public Response getObservationSitePointsKML(@PathParam("observationSiteId") Integer observationSiteId)
    {
        Kml retVal = appleFruitMothBean.getObservationSitePointsKml(observationSiteId, ServletUtil.getServerName(httpServletRequest));
        return Response.ok().entity(retVal).build();
    }*/
}
