/*
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.modules.barkbeetle;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.webcohesion.enunciate.metadata.Facet;

import no.nibio.vips.util.ServletUtil;

/**
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest/barkbeetle")
@Facet("restricted")
public class BarkbeetleService {
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    @Context
    private HttpServletRequest httpServletRequest;
    
    @GET
    @Path("seasontrapsites/{season}/kml")
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    public Response getSeasonTrapsites(
            @PathParam("season") Integer season,
            @QueryParam("excludeSeasonTrapsiteId") Integer excludeSeasonTrapsiteId)
    {
        return Response.ok().entity(BarkbeetleBean.getInstance().getSeasonTrapsitesKml(season, excludeSeasonTrapsiteId, ServletUtil.getServerName(httpServletRequest))).build();
    }
    
    @GET
    @Path("seasontrapsites/{season}/kml/status")
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    public Response getSeasonTrapsitesWithStatus(
            @PathParam("season") Integer season
            )
    {
        return Response.ok().entity(BarkbeetleBean.getInstance().getSeasonTrapsitesStatusKml(season, ServletUtil.getServerName(httpServletRequest))).build();
    }
    
    @GET
    @Path("report/seasontrapsites/{season}")
    @Produces("text/csv")
    public Response getSeasonTrapsitesReport(@PathParam("season") Integer season)
    {
    	String csv = BarkbeetleBean.getInstance().getSeasonTrapsitesReportCsv(season);
    	return Response.ok().entity(csv).build();
    }
    
    @GET
    @Path("report/countyaverage/{season}")
    @Produces("text/csv")
    public Response getSeasonCountyAverage(@PathParam("season") Integer season)
    {
    	String csv = BarkbeetleBean.getInstance().getCountyAverageCsv(season);
    	return Response.ok().entity(csv).build();
    }
    
    @GET
    @Path("report/bivolt/{season}")
    @Produces("text/csv")
    public Response getLatestBivoltCalculations(@PathParam("season") Integer season)
    {
    	String csv = BarkbeetleBean.getInstance().getLatestBivoltCalculations(season);
    	return Response.ok().entity(csv).build();
    }
    
    /**
     * 
     * @param seasonTrapsiteId
     * @param isMaintenanceCompleted
     * @return 200 (OK) or 204 (No Content). 404 (Not Found), if ID not found or invalid.
     */
    @PATCH
    @Path("seasontrapsite/{seasonTrapsiteId}/maintenancecompleted/{isMaintenanceCompleted}")
    public Response setMaintenanceCompleted(
    		@PathParam("seasonTrapsiteId") Integer seasonTrapsiteId,
    		@PathParam("isMaintenanceCompleted") String isMaintenanceCompleted
    		)
    {
    	BarkbeetleBean.getInstance().setMaintenanceCompleted(seasonTrapsiteId, isMaintenanceCompleted != null && isMaintenanceCompleted.equals("true"));
    	return Response.ok().build();
    }
}
