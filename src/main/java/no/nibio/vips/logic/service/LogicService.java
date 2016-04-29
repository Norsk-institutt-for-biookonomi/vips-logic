/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.service;

import com.ibm.icu.util.ULocale;
import java.util.TimeZone;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
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
import no.nibio.vips.coremanager.service.ManagerResource;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.authenticate.PasswordValidationException;
import no.nibio.vips.logic.entity.CropPest;
import no.nibio.vips.logic.entity.ForecastResult;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ForecastModelConfiguration;
import no.nibio.vips.logic.entity.Message;
import no.nibio.vips.logic.entity.MessageTag;
import no.nibio.vips.logic.entity.Observation;
import no.nibio.vips.logic.entity.Organism;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.PointOfInterest;
import no.nibio.vips.logic.entity.PointOfInterestType;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.CSVPrintUtil;
import no.nibio.vips.util.ServletUtil;
import no.nibio.vips.util.SolarRadiationUtil;
import no.nibio.vips.util.weather.ALabDataParser;
import no.nibio.vips.util.weather.FruitWebDavisDataParser;
import no.nibio.vips.util.weather.MetosDataParser;
import no.nibio.vips.util.weather.ParseWeatherDataException;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.spi.HttpRequest;

/**
 * @copyright 2013-2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest")
public class LogicService {
    private final static String VIPSCOREMANAGER_URL = System.getProperty("no.nibio.vips.logic.VIPSCOREMANAGER_URL");
    
    @Context
    private HttpRequest httpRequest;
    @Context
    private HttpServletRequest httpServletRequest;
    
    @GET
    @Path("forecastresults/{forecastConfigurationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getForecastResults(
            @PathParam("forecastConfigurationId") Long forecastConfigurationId,
            @QueryParam("userUUID") String userUUID
    )
    {
        if(SessionControllerGetter.getForecastBean().isUserAuthorizedForForecastConfiguration(forecastConfigurationId, userUUID))
        {
            List<ForecastResult> results = SessionControllerGetter.getForecastBean().getForecastResults(forecastConfigurationId);
            if(results == null)
            {
                results = new ArrayList<>();
            }
            return Response.ok().entity(results).build();
        }
        else
        {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
    
    @GET
    @Path("forecastresults/{forecastConfigurationId}/{latestDays}")
    @Produces("application/json;charset=UTF-8")
    public Response getForecastResults(
            @PathParam("forecastConfigurationId") Long forecastConfigurationId,
            @PathParam("latestDays") Integer latestDays,
            @QueryParam("userUUID") String userUUID
    )
    {
        if(SessionControllerGetter.getForecastBean().isUserAuthorizedForForecastConfiguration(forecastConfigurationId, userUUID))
        {
            List<ForecastResult> results = SessionControllerGetter.getForecastBean().getForecastResults(forecastConfigurationId, latestDays);
            if(results == null)
            {
                results = new ArrayList<>();
            }
            return Response.ok().entity(results).build();
        }
        else
        {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
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
    
    @GET
    @Path("forecastconfigurationsummaries/private/{userUUID}")
    @Produces("application/json;charset=UTF-8")
    public Response getForecastSummaries(
            @PathParam("userUUID") String userUUID
    )
    {
        UUID uUUID = UUID.fromString(userUUID);
        VipsLogicUser user = SessionControllerGetter.getUserBean().findVipsLogicUser(uUUID);
        if(user != null)
        {
            List<ForecastConfiguration> summaries = SessionControllerGetter.getForecastBean().getPrivateForecastConfigurationSummaries(user);
            return Response.ok().entity(summaries).build();
        }
        else
        {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
    }
    
    /**
     * Returns the requested forecast configuration
     * @param forecastConfigurationId
     * @return 
     */
    @GET
    @Path("forecastconfigurations/{forecastConfigurationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getForecastConfiguration(@PathParam("forecastConfigurationId") Long forecastConfigurationId,@QueryParam("userUUID") String userUUID)
    {
        if(SessionControllerGetter.getForecastBean().isUserAuthorizedForForecastConfiguration(forecastConfigurationId, userUUID))
        {
            ForecastConfiguration forecastConfiguration = SessionControllerGetter.getForecastBean().getForecastConfiguration(forecastConfigurationId);
            return Response.ok().entity(forecastConfiguration).build();
        }
        else
        {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
    
    @GET
    @Path("forecastconfigurations/private/{userUUID}")
    @Produces("application/json;charset=UTF-8")
    public Response getPrivateForecastConfigurations(@PathParam("userUUID") String userUUID)
    {
        try
        {
            UUID uUUID = UUID.fromString(userUUID);
            VipsLogicUser user = SessionControllerGetter.getUserBean().findVipsLogicUser(uUUID);
            if(user != null)
            {
                List<ForecastConfiguration> retVal = SessionControllerGetter.getForecastBean().getPrivateForecastConfigurationsForUser(user.getUserId());
                return Response.ok().entity(retVal).build();
            }
            else
            {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        }
        catch(NullPointerException npe)
        {
            return Response.noContent().build();
        }
        
    }
    
    /**
     * Returns a list of forecasts for given organization
     * @param organizationId
     * @param cropOrganismIds
     * @param from
     * @param to
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
            @QueryParam("cropCategoryId") List<Integer> cropCategoryIds)
    {
        if(cropCategoryIds == null || cropCategoryIds.isEmpty())
        {
            return Response.noContent().build();
        }
        else
        {
            List<Integer> cropOrganismIds = SessionControllerGetter.getOrganismBean().getCropCategoryOrganismIds(cropCategoryIds);
            Kml retVal = SessionControllerGetter.getForecastBean().getForecastsAggregateKml(organizationId, cropOrganismIds, SystemTime.getSystemTime(), ServletUtil.getServerName(httpServletRequest));
            return Response.ok().entity(retVal).build();
        }
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
        Kml retVal = SessionControllerGetter.getPointOfInterestBean().getPoisForOrganization(organizationId, excludeWeatherStationId, highlightWeatherStationId, ServletUtil.getServerName(httpServletRequest), SessionLocaleUtil.getI18nBundle(httpServletRequest), PointOfInterestType.POINT_OF_INTEREST_TYPE_WEATHER_STATION);
        return Response.ok().entity(retVal).build();
    }
    
    @GET
    @Path("pois/kml/{organizationId}")
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    public Response getPois(@QueryParam("excludePoiId") Integer excludePoiId, @QueryParam("highlightPoiId") Integer highlightPoiId, @PathParam("organizationId") Integer organizationId)
    {
        Kml retVal = SessionControllerGetter.getPointOfInterestBean().getPoisForOrganization(organizationId, excludePoiId, highlightPoiId, ServletUtil.getServerName(httpServletRequest), SessionLocaleUtil.getI18nBundle(httpServletRequest), null);
        return Response.ok().entity(retVal).build();
    }
    
    
    
    @GET
    @Path("poi/organization/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getPoisForOrganization(@PathParam("organizationId") Integer organizationId)
    {
        Organization organization = SessionControllerGetter.getUserBean().getOrganization(organizationId);
        List<PointOfInterestWeatherStation> retVal = SessionControllerGetter.getPointOfInterestBean().getWeatherstationsForOrganization(organization, Boolean.TRUE);
        return Response.ok().entity(retVal).build();
    }
    
    @GET
    @Path("poi/{pointOfInterestId}")
    @Produces("application/json;charset=UTF-8")
    public Response getPoi(@PathParam("pointOfInterestId") Integer pointOfInterestId)
    {
        PointOfInterest retVal = SessionControllerGetter.getPointOfInterestBean().getPointOfInterest(pointOfInterestId);
        return Response.ok().entity(retVal).build();
    }
    
    @GET
    @Path("poi/user/")
    @Produces("application/json;charset=UTF-8")
    public Response getPoisForCurrentUser()
    {
        VipsLogicUser user = (VipsLogicUser) httpServletRequest.getSession().getAttribute("user");
        List<PointOfInterest> retVal = SessionControllerGetter.getPointOfInterestBean().getRelevantPointOfInterestsForUser(user);
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
    @Path("organism/crop/list")
    @Produces("application/json;charset=UTF-8")
    public Response getCropOrganismList()
    {
        List<Organism> organismList = SessionControllerGetter.getOrganismBean().getAllCrops();
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
            @QueryParam("locale") String locale,
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
    @Path("weather/calculation/solarradiation/json")
    @Produces("application/json;charset=UTF-8")
    public Response getCalculatedSolarRadiationAtLocationAndTimeJSON(
            @QueryParam("latitude") Double latitude, 
            @QueryParam("longitude") Double longitude, 
            @QueryParam("startTime") String startTimeStr, 
            @QueryParam("endTime") String endTimeStr, 
            @QueryParam("timeZone") String timeZoneStr,
            @QueryParam("logIntervalId") Integer logIntervalId
    )
    {
        try
        {

            List<WeatherObservation> radiationValues = getCalculatedSolarRadiationAtLocationAndTime (
                    latitude,
                    longitude,
                    startTimeStr,
                    endTimeStr,
                    timeZoneStr,
                    logIntervalId
            );
            return Response.ok().entity(radiationValues).build();
        }
        catch(ParseException ex)
        {
            return Response.serverError().entity(ex).build();
        }
    }
    
    @GET
    @Path("weather/calculation/solarradiation/csv")
    @Produces("text/csv;charset=UTF-8")
    public Response getCalculatedSolarRadiationAtLocationAndTimeCSV(
            @QueryParam("latitude") Double latitude, 
            @QueryParam("longitude") Double longitude, 
            @QueryParam("startTime") String startTimeStr, 
            @QueryParam("endTime") String endTimeStr, 
            @QueryParam("timeZone") String timeZoneStr,
            @QueryParam("logIntervalId") Integer logIntervalId
    )
    {
        try
        {

            List<WeatherObservation> radiationValues = getCalculatedSolarRadiationAtLocationAndTime (
                    latitude,
                    longitude,
                    startTimeStr,
                    endTimeStr,
                    timeZoneStr,
                    logIntervalId
            );
            //return Response.ok().entity(radiationValues).build();
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
            return Response.ok(new CSVPrintUtil().printWeatherObservations(radiationValues, timeZone, "\t")).build();
        }
        catch(ParseException ex)
        {
            return Response.serverError().entity(ex).build();
        }
    }
    
    private List<WeatherObservation> getCalculatedSolarRadiationAtLocationAndTime (
            Double latitude,
            Double longitude,
            String startTimeStr,
            String endTimeStr,
            String timeZoneStr,
            Integer logIntervalId
    ) throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
            Date startTime = format.parse(startTimeStr);
            Date endTime = format.parse(endTimeStr);
            logIntervalId = logIntervalId == null ? WeatherObservation.LOG_INTERVAL_ID_1H : logIntervalId;
            SolarRadiationUtil srUtil = new SolarRadiationUtil();
            return srUtil.getCalculatedSolarRadiation(latitude, longitude, startTime, endTime, timeZone, logIntervalId);
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
    
    @GET
    @POST
    @Path("weather/proxy/fruitwebdavis/{stationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getFruitWebDavisWeatherData(
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
            observations = new FruitWebDavisDataParser().getWeatherObservations(URLDecoder.decode(stationId, "UTF-8"), timeZone, startDate1);
        } catch (ParseException | ParseWeatherDataException | NullPointerException | UnsupportedEncodingException ex) {
            return Response.serverError().entity(ex).build();
        }
        return Response.ok().entity(observations).build();
    }
    
    
    @GET
    @POST
    @Path("weather/proxy/alab/{stationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getALabWeatherData(
            @PathParam("stationId") String stationId, 
            @FormParam("timeZone") String timeZonePOST, 
            @QueryParam("timeZone") String timeZoneGET, 
            @FormParam("startDate") String startDatePOST,
            @QueryParam("startDate") String startDateGET,
            @FormParam("userName") String userNamePOST,
            @QueryParam("userName") String userNameGET,
            @FormParam("password") String passwordPOST,
            @QueryParam("password") String passwordGET
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
            String userName = userNamePOST != null ? userNamePOST:userNameGET;
            String password = passwordPOST != null ? passwordPOST:passwordGET;
            observations = new ALabDataParser().getWeatherObservations(stationId, timeZone, startDate1, userName, password);
        } catch (ParseException | ParseWeatherDataException | NullPointerException ex) {
            return Response.serverError().entity(ex).build();
        }
        return Response.ok().entity(observations).build();
    }
    
    @GET
    @Path("observation/{observationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getObservation(@PathParam("observationId") Integer observationId){
        return Response.ok().entity(SessionControllerGetter.getObservationBean().getObservation(observationId)).build();
    }
    
    /**
     * Publicly available observations per organization
     * @param organizationId
     * @return APPROVED observations
     */
    @GET
    @Path("observation/list/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getObservations(@PathParam("organizationId") Integer organizationId){
        return Response.ok().entity(SessionControllerGetter.getObservationBean().getObservations(organizationId, Observation.STATUS_TYPE_ID_APPROVED)).build();
    }
    
    /**
     * Publicly available observations per organization
     * @param organizationId
     * @return APPROVED observations
     */
    @GET
    @Path("observation/broadcast/list/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getBroadcastObservations(@PathParam("organizationId") Integer organizationId){
        return Response.ok().entity(SessionControllerGetter.getObservationBean().getBroadcastObservations(organizationId)).build();
    }
    
    /**
     * Service available locally for cron jobs. Most useful on test servers
     * @return 
     */
    @GET
    @Path("batch/updateforecastcaches")
    @Produces("text/plain;charset=UTF-8")
    public Response updateForecastCaches()
    {
        //System.out.println(httpServletRequest.getHeader("X-Forwarded-For"));
        if(!ServletUtil.getClientIP(httpServletRequest).equals("127.0.0.1"))
        {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        Date start = new Date();
        SessionControllerGetter.getForecastBean().updateForecastResultCacheTable();
        SessionControllerGetter.getForecastBean().updateForecastSummaryTable(SystemTime.getSystemTime());
        Long timeLapsed = new Date().getTime() - start.getTime();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return Response.ok().entity("Forecast caches were successfully updated with data from today (" 
                + format.format(SystemTime.getSystemTime()) 
                + "). Time spent=" + timeLapsed + " milliseconds.\n").build();
    }
    
    /**
     * TODO: Should only be available for trusted clients (like VIPSWeb)
     * @param userUUID
     * @return 
     */
    @GET
    @Path("user/uuid/{userUUID}")
    @Produces("application/json;charset=UTF-8")
    public Response getVipsLogicUserByUUID(@PathParam("userUUID") String userUUID)
    {
        try
        {
            UUID uUUID = UUID.fromString(userUUID);
            VipsLogicUser user = SessionControllerGetter.getUserBean().findVipsLogicUser(uUUID);
            if(user != null)
            {
                return Response.ok().entity(user).build();
            }
            else
            {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
        catch(IllegalArgumentException ex)
        {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }
    
    @DELETE
    @Path("user/uuid/{userUUID}")
    @Produces("application/json;charset=UTF-8")
    public Response deleteVipsLogicUserUUID(@PathParam("userUUID") String userUUID)
    {
        try
        {
            UUID uUUID = UUID.fromString(userUUID);
            SessionControllerGetter.getUserBean().deleteUserUuid(uUUID);
            return Response.ok().build();
        }
        catch(IllegalArgumentException ex)
        {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }
    
    @GET
    @Path("organism/croppest/{cropOrganismId}")
    @Produces("application/json;charset=UTF-8")
    public Response getCropPest(@PathParam("cropOrganismId") Integer cropOrganismId)
    {
        CropPest retVal = SessionControllerGetter.getOrganismBean().getCropPestRecursive(cropOrganismId,true);
        if(retVal != null)
        {
            return Response.ok().entity(retVal).build();
        }
        else
        {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }
    
    @GET
    @Path("organism/cropcategory/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getCropCategories(@PathParam("organizationId") Integer organizationId)
    {
        if(organizationId != null)
        {
            return Response.ok().entity(SessionControllerGetter.getOrganismBean().getCropCategories(organizationId)).build();
        }
        else
        {
            return Response.noContent().build();
        }
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
