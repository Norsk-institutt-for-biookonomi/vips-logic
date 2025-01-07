/*
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.modules.barkbeetle;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.annotations.GZIP;

import com.ibm.icu.util.Calendar;
import com.webcohesion.enunciate.metadata.Facet;

import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.ejb.EJB;

import no.nibio.vips.logic.util.SystemTime;
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
    
    @EJB
    BarkbeetleBean barkbeetleBean;
    
    @GET
    @Path("seasontrapsites/{season}/kml")
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    public Response getSeasonTrapsites(
            @PathParam("season") Integer season,
            @QueryParam("excludeSeasonTrapsiteId") Integer excludeSeasonTrapsiteId)
    {
        return Response.ok().entity(barkbeetleBean.getSeasonTrapsitesKml(season, excludeSeasonTrapsiteId, ServletUtil.getServerName(httpServletRequest))).build();
    }
    
    @GET
    @Path("seasontrapsites/{season}/kml/status")
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    public Response getSeasonTrapsitesWithStatus(
            @PathParam("season") Integer season
            )
    {
        return Response.ok().entity(barkbeetleBean.getSeasonTrapsitesStatusKml(season, ServletUtil.getServerName(httpServletRequest))).build();
    }
    
    @GET
    @Path("report/seasontrapsites/{season}")
    @Produces("text/csv")
    public Response getSeasonTrapsitesReport(@PathParam("season") Integer season)
    {
    	String csv = barkbeetleBean.getSeasonTrapsitesReportCsv(season);
    	return Response.ok().entity(csv).build();
    }
    
    @GET
    @Path("report/countyaverage/{season}")
    @Produces("text/csv")
    //@Produces("text/plain")
    public Response getSeasonCountyAverage(@PathParam("season") Integer season)
    {
    	String csv = barkbeetleBean.getCountyAverageCsv(season);
    	return Response.ok().entity(csv).build();
    }
    
    @GET
    @Path("report/bivolt/{season}")
    @Produces("text/csv")
    public Response getLatestBivoltCalculations(@PathParam("season") Integer season)
    {
    	String csv = barkbeetleBean.getLatestBivoltCalculations(season);
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
    	barkbeetleBean.setMaintenanceCompleted(seasonTrapsiteId, isMaintenanceCompleted != null && isMaintenanceCompleted.equals("true"));
    	return Response.ok().build();
    }
    
    
    @GET
    @Path("batch/checkandsendtrapemptyingreminder")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    public Response checkAndSendTrapEmptyingReminder(
    		@QueryParam("currentDate") String currentDateStr
    		)
    {
        try
        {
        	Date currentDate = currentDateStr == null ? SystemTime.getSystemTime() :  new SimpleDateFormat("yyyy-MM-dd").parse(currentDateStr);
			barkbeetleBean.checkAndSendTrapEmptyingReminder(currentDate);
			return Response.ok().build();
        }
        catch(Exception ex)
        {
        	return Response.serverError().entity(ex).build();
        }
    }
}
