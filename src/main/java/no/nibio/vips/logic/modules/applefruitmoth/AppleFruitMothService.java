/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the NIBIO Open Source License as published by 
 * NIBIO, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * NIBIO Open Source License for more details.
 * 
 * You should have received a copy of the NIBIO Open Source License
 * along with VIPSLogic.  If not, see <http://www.nibio.no/licenses/>.
 * 
 */

package no.nibio.vips.logic.modules.applefruitmoth;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import java.util.Calendar;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.ServletUtil;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest/applefruitmoth")
public class AppleFruitMothService {
    
    @Context
    private HttpServletRequest httpServletRequest;
    
    @GET
    @Path("observationsites/kml/")
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    public Response getObservationSitesKML()
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(SystemTime.getSystemTime());
        Integer season = cal.get(Calendar.YEAR);
        return this.getObservationSitesKML(season);
    }
    
    @GET
    @Path("observationsites/kml/{season}")
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    public Response getObservationSitesKML(@PathParam("season") Integer season)
    {
        
        Kml retVal = SessionControllerGetter.getAppleFruitMothBean().getObservationSitesKml(season, ServletUtil.getServerName(httpServletRequest));
        return Response.ok().entity(retVal).build();
    }
}
