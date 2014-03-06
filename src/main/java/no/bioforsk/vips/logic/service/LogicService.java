/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with VIPSLogic.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.service;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import no.bioforsk.vips.coremanager.service.ManagerResource;
import no.bioforsk.vips.logic.authenticate.PasswordValidationException;
import no.bioforsk.vips.logic.entity.ForecastResult;
import no.bioforsk.vips.logic.i18n.SessionLocaleUtil;
import no.bioforsk.vips.logic.entity.ForecastConfiguration;
import no.bioforsk.vips.logic.entity.ForecastModelConfiguration;
import no.bioforsk.vips.logic.entity.Message;
import no.bioforsk.vips.logic.entity.MessageTag;
import no.bioforsk.vips.logic.entity.Observation;
import no.bioforsk.vips.logic.entity.Organism;
import no.bioforsk.vips.logic.entity.VipsLogicUser;
import no.bioforsk.vips.logic.util.SessionControllerGetter;
import no.bioforsk.vips.logic.util.SystemTime;
import no.bioforsk.vips.util.ServletUtil;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.spi.HttpRequest;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Path("rest")
public class LogicService {
    private final static String VIPSCOREMANAGER_URL = System.getProperty("no.bioforsk.vips.logic.VIPSCOREMANAGER_URL");
    
    @Context
    private HttpRequest httpRequest;
    @Context
    private HttpServletRequest httpServletRequest;

    @GET
    @Path("testforecastresults")
    @Produces("application/json;charset=UTF-8")
    public Response testForecastResults()
    {
        List<ForecastResult> results = SessionControllerGetter.getForecastBean().getForecastResults();
        return Response.ok().entity(results).build();
    }
    
    @GET
    @Path("forecastresults/{forecastConfigurationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getForecastResults(@PathParam("forecastConfigurationId") Long forecastConfigurationId)
    {
        List<ForecastResult> results = SessionControllerGetter.getForecastBean().getForecastResults(forecastConfigurationId);
        return Response.ok().entity(results).build();
    }
    
    /**
     * Returns the requested forecast configuration
     * @param forecastConfigurationId
     * @return 
     */
    @GET
    @Path("forecastconfigurations/{forecastConfigurationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getForecastConfiguration(@PathParam("forecastConfigurationId") Long forecastConfigurationId)
    {
        ForecastConfiguration forecastConfiguration = SessionControllerGetter.getForecastBean().getForecastConfiguration(forecastConfigurationId);
        return Response.ok().entity(forecastConfiguration).build();
    }
    
    /**
     * Returns a list of forecasts for given organization
     * @param organizationId
     * @return 
     */
    @GET
    @Path("organizationforecastconfigurations/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getForecastConfigurationsForOrganization(@PathParam("organizationId") Integer organizationId)
    {
        // First: Get all users for organization
        List<VipsLogicUser> organizationUsers = SessionControllerGetter.getUserBean().getUsersByOrganization(organizationId);
        // Then: Get forecasts for these users, collate and return
        List<ForecastConfiguration> forecasts = new ArrayList<>();
        for(VipsLogicUser user:organizationUsers)
        {
            Integer userId = (Integer) user.getUserId();
            if(forecasts == null)
                forecasts = SessionControllerGetter.getForecastBean().getForecastConfigurationsForUser(userId);
            else
                forecasts.addAll(SessionControllerGetter.getForecastBean().getForecastConfigurationsForUser(userId));
        }
        
        return Response.ok().entity(forecasts).build();
    }
    
    
    
    @GET
    @Path("evaluatepassword/{password}")
    @Produces("text/plain;charset=UTF-8")
    public Response evaluatePassord(@PathParam("password") String password)
    {
        Locale currentLocale = SessionLocaleUtil.getCurrentLocale(httpServletRequest);
        try
        {
            boolean isPasswordValid = SessionControllerGetter.getUserBean().isPasswordValid(password, currentLocale);
            return Response.ok().entity("true").build();
            
        }
        catch(PasswordValidationException ex)
        {
            return Response.ok().entity(ex.getMessage()).build();
        }
    }
    
    @GET
    @Path("forecastmodelconfiguration/{forecastConfigurationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getForecastModelConfiguration(@PathParam("forecastConfigurationId") Long forecastConfigurationId)
    {
        List<ForecastModelConfiguration> forecastModelConfigurations = SessionControllerGetter.getForecastBean().getForecastModelConfigurations(forecastConfigurationId);
        return Response.ok().entity(forecastModelConfigurations).build();
    }
    
    @GET
    @Path("forecastresults/aggregate")
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    public Response getForecastResultsAggregate()
    {
            Kml retVal = SessionControllerGetter.getForecastBean().getForecastsAggregateKml(SystemTime.getSystemTime(), ServletUtil.getServerName(httpServletRequest));
            return Response.ok().entity(retVal).build();
    }
    
    @GET
    @Path("forecastresults/latest/poi/{poiId}")
    @Produces("application/json;charset=UTF-8")
    public Response getLatestForecastResultsForPoi(@PathParam("poiId") Integer poiId)
    {
        Map<String, Object> latestResults = SessionControllerGetter.getForecastBean().getLatestForecastResultsForPoi(poiId);
        return Response.ok().entity(latestResults).build();
    }
    
    @GET
    @Path("weatherstations/kml/{organizationId}")
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    public Response getWeatherStations(@QueryParam("excludeWeatherStationId") Integer excludeWeatherStationId, @QueryParam("highlightWeatherStationId") Integer highlightWeatherStationId, @PathParam("organizationId") Integer organizationId)
    {
        Kml retVal = SessionControllerGetter.getPointOfInterestBean().getWeatherstationsForOrganization(organizationId, excludeWeatherStationId, highlightWeatherStationId, ServletUtil.getServerName(httpServletRequest), SessionLocaleUtil.getI18nBundle(httpServletRequest));
        return Response.ok().entity(retVal).build();
    }
    
    @GET
    @Path("organism/list")
    @Produces("application/json;charset=UTF-8")
    public Response getOrganismList()
    {
        List<Organism> organismList = SessionControllerGetter.getOrganismBean().getOrganismSubTree(null);
        return Response.ok().entity(organismList).build();
    }
    
    
    
    @GET
    @Path("message/{messageId}")
    @Produces("application/json;charset=UTF-8")
    public Response getMessage(@PathParam("messageId") Integer messageId)
    {
        Message message = SessionControllerGetter.getMessageBean().getMessage(messageId);
        return Response.ok().entity(message).build();
    }
    
    @GET
    @Path("message/list/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getMessageList(@PathParam("organizationId") Integer organizationId)
    {
        List<Message> messageList = SessionControllerGetter.getMessageBean().getMessageList(organizationId);
        return Response.ok().entity(messageList).build();
    }
    
    @GET
    @Path("message/list/{organizationId}/tagfilter")
    @Produces("application/json;charset=UTF-8")
    public Response getMessageListWithTags(@QueryParam("tagId") List<Integer> tagIds, @PathParam("organizationId") Integer organizationId)
    {
        List<Message> messageListWithTags = SessionControllerGetter.getMessageBean().getMessageListWithTags(organizationId, tagIds);
        return Response.ok().entity(messageListWithTags).build();
    }
    
    
    
    @GET
    @Path("messagetag/list")
    @Produces("application/json;charset=UTF-8")
    public Response getMessageTagList()
    {
        List<MessageTag> messageTags = SessionControllerGetter.getMessageBean().getMessageTagList();
        return Response.ok().entity(messageTags).build();
    }
    
    @GET
    @Path("observation")
    @Produces("application/json;charset=UTF-8")
    public Response getObservationList(@QueryParam("organizationId") Integer organizationId)
    {
        List<Observation> observations = SessionControllerGetter.getObservationBean().getObservations(organizationId);
        return Response.ok().entity(observations).build();
    }
    
    private ManagerResource getManagerResource()
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(VIPSCOREMANAGER_URL);
        ResteasyWebTarget rTarget = (ResteasyWebTarget) target;
        ManagerResource resource = rTarget.proxy(ManagerResource.class);
        return resource;
    }
    
    
    
}
