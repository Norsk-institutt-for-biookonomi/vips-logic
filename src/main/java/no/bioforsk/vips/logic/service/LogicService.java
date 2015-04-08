/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the Bioforsk Open Source License as published by 
 * Bioforsk, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Bioforsk Open Source License for more details.
 * 
 * You should have received a copy of the Bioforsk Open Source License
 * along with VIPSLogic.  If not, see <http://www.bioforsk.no/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.service;

import com.ibm.icu.util.ULocale;
import java.util.TimeZone;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import no.bioforsk.vips.entity.WeatherObservation;
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
import no.bioforsk.vips.util.weather.MetosDataParser;
import no.bioforsk.vips.util.weather.ParseWeatherDataException;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.spi.HttpRequest;

/**
 * @copyright 2013-2015 <a href="http://www.bioforsk.no/">Bioforsk</a>
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
    @Path("forecastresults/{forecastConfigurationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getForecastResults(@PathParam("forecastConfigurationId") Long forecastConfigurationId)
    {
        List<ForecastResult> results = SessionControllerGetter.getForecastBean().getForecastResults(forecastConfigurationId);
        return Response.ok().entity(results).build();
    }
    
    @GET
    @Path("forecastresults/{forecastConfigurationId}/{latestDays}")
    @Produces("application/json;charset=UTF-8")
    public Response getForecastResults(
            @PathParam("forecastConfigurationId") Long forecastConfigurationId,
            @PathParam("latestDays") Integer latestDays
    )
    {
        List<ForecastResult> results = SessionControllerGetter.getForecastBean().getForecastResults(forecastConfigurationId, latestDays);
        return Response.ok().entity(results).build();
    }
    
    @GET
    @Path("forecastconfigurationsummaries/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getForecastSummaries(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("cropOrganismId") List<Integer> cropOrganismIds
    )
    {
        List<ForecastConfiguration> summaries = SessionControllerGetter.getForecastBean().getForecastConfigurationSummaries(organizationId);
        return Response.ok().entity(summaries).build();
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
    public Response getForecastConfigurationsForOrganization(
            @PathParam("organizationId") Integer organizationId, 
            @QueryParam("cropOrganismId") List<Integer> cropOrganismIds,
            @QueryParam("from") Date from,
            @QueryParam("to") Date to
            )
    {
        
        if(from == null || to == null)
        {
            to = SystemTime.getSystemTime();
            Calendar cal = Calendar.getInstance();
            cal.setTime(to);
            cal.add(Calendar.MONTH, -2);
            from = cal.getTime();
        }

        // First: Get all users for organization
        List<VipsLogicUser> organizationUsers = SessionControllerGetter.getUserBean().getUsersByOrganization(organizationId);
        // Then: Get forecasts for these users, collate and return
        List<ForecastConfiguration> forecasts = new ArrayList<>();
        
        for(VipsLogicUser user:organizationUsers)
        {
            Integer userId = user.getUserId();
            List<ForecastConfiguration> result = cropOrganismIds != null && ! cropOrganismIds.isEmpty() ?
                    SessionControllerGetter.getForecastBean().getForecastConfigurationsForUserAndCropsAndDate(userId,cropOrganismIds, from, to)
                    : SessionControllerGetter.getForecastBean().getForecastConfigurationsForUserAndDate(userId, from, to);
            if(forecasts == null)
                forecasts = result;
            else
                forecasts.addAll(result);
        }
        
        return Response.ok().entity(forecasts).build();
    }
    
    
    
    @GET
    @Path("evaluatepassword/{password}")
    @Produces("text/plain;charset=UTF-8")
    public Response evaluatePassord(@PathParam("password") String password)
    {
        ULocale currentLocale = SessionLocaleUtil.getCurrentLocale(httpServletRequest);
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
    @Path("forecastresults/aggregate/{organizationId}")
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    public Response getForecastResultsAggregate(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("cropOrganismId") List<Integer> cropOrganismIds)
    {
            Kml retVal = SessionControllerGetter.getForecastBean().getForecastsAggregateKml(organizationId, cropOrganismIds, SystemTime.getSystemTime(), ServletUtil.getServerName(httpServletRequest));
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
    public Response getMessageList(
            @QueryParam("publishedFrom") String publishedFrom , @QueryParam("publishedTo") String publishedTo,
            @PathParam("organizationId") Integer organizationId
    )
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try
        {
            Date datePublishedFrom;// = publishedFrom == null ? SystemTime.getSystemTime() : format.parse(publishedFrom);
            Date datePublishedTo;// = publishedTo == null ? SystemTime.getSystemTime() : format.parse(publishedTo);
            if(publishedFrom == null && publishedTo == null)
            {
                datePublishedFrom = SystemTime.getSystemTime();
                datePublishedTo = datePublishedFrom;
            }
            else
            {
                datePublishedFrom = publishedFrom == null ? null : format.parse(publishedFrom);
                datePublishedTo = publishedTo == null ? null : format.parse(publishedTo);
            }
            
            List<Message> messageList = SessionControllerGetter.getMessageBean().getMessageList(organizationId, datePublishedFrom, datePublishedTo);

            return Response.ok().entity(messageList).build();
        }
        catch(ParseException ex){
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }
    
    @GET
    @Path("message/list/{organizationId}/tagfilter")
    @Produces("application/json;charset=UTF-8")
    public Response getMessageListWithTags(@QueryParam("tagId") List<Integer> tagIds, @PathParam("organizationId") Integer organizationId)
    {
        List<Message> messageListWithTags = SessionControllerGetter.getMessageBean().getCurrentFilteredMessagesForOrganization(tagIds, organizationId);
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
    
    @GET
    @POST
    @Path("weather/proxy/metos/{stationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getMetosWeatherData(
            @PathParam("stationId") String stationId, 
            @FormParam("timeZone") String timeZonePOST, 
            @QueryParam("timeZone") String timeZoneGET, 
            @FormParam("startDate") String startDatePOST,
            @QueryParam("startDate") String startDateGET
            )
    {
        List<WeatherObservation> observations;
        try 
        {
            String timeZoneParam = timeZonePOST != null ? timeZonePOST : timeZoneGET != null ? timeZoneGET : "UTC";
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneParam);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setTimeZone(timeZone);
            String startDateParam = startDatePOST != null ? startDatePOST : startDateGET;
            Date startDate1 = format.parse(startDateParam);
            observations = new MetosDataParser().getWeatherObservations(stationId, timeZone, startDate1);
        } catch (ParseException | ParseWeatherDataException | NullPointerException ex) {
            return Response.serverError().entity(ex).build();
        }
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
