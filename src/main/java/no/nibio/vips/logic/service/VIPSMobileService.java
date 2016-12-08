/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import no.nibio.vips.entity.Result;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ForecastSummary;
import no.nibio.vips.logic.entity.Message;
import no.nibio.vips.logic.entity.MessageIllustration;
import no.nibio.vips.logic.entity.MessageLocale;
import no.nibio.vips.logic.entity.ModelInformation;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.entity.vipsmobile.VIPSMobileForecast;
import no.nibio.vips.logic.entity.vipsmobile.VIPSMobileMessage;
import no.nibio.vips.logic.entity.vipsmobile.VIPSMobilePresentation;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.WeatherUtil;
import org.jboss.resteasy.spi.HttpRequest;

/**
 * The purpose of this service is to mimic the services for VIPSMobile that
 * were implemented in the old VIPS. It's assumed that it's easier to adapt the
 * service layer than the client layer in Sencha Touch...
 * 
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest/vipsmobile")
public class VIPSMobileService {
    
    @Context
    private HttpRequest httpRequest;
    @Context
    private HttpServletRequest httpServletRequest;
    
    @GET
    @Path("melding")
    @Produces("application/json;charset=UTF-8")
    public Response getMessages(
            @QueryParam("organizationId") Integer organizationId,
            @QueryParam("locale") String locale
    )
    {
            Date datePublishedFrom = SystemTime.getSystemTime();
            Date datePublishedTo = datePublishedFrom;
            List<Message> messageList = SessionControllerGetter.getMessageBean().getMessageList(organizationId, datePublishedFrom, datePublishedTo);
            
            List<VIPSMobileMessage> retVal = new ArrayList<>();
            for(Message message: messageList)
            {
                // Determine language
                // Priority: Preferred language, English, first available locale
                Set<MessageLocale> messageLocales = message.getMessageLocaleSet();
                MessageLocale first = null;
                MessageLocale english = null;
                MessageLocale optimal = null;
                for(MessageLocale mLocale:messageLocales)
                {
                    if(mLocale.getMessageLocalePK().getLocale().equals(locale))
                    {
                        optimal = mLocale;
                        break;
                    }
                    if(first == null)
                    {
                        first = mLocale;
                    }
                    if(mLocale.getMessageLocalePK().getLocale().equals("en"))
                    {
                        english = mLocale;
                    }
                }
                
                MessageLocale pragmaticBestLocale = optimal != null ? optimal
                                                : english != null ? english
                                                : first;
                
                String imageURL = "";
                String imageCaption = "";
                try
                {
                    MessageIllustration illustration = message.getMessageIllustrationSet().iterator().next();
                    imageURL = String.format("/static/images/messages/%s/%s", pragmaticBestLocale.getCreatedBy().getOrganizationId().getOrganizationId(), illustration.getMessageIllustrationPK().getFileName());
                }catch(NoSuchElementException | NullPointerException ex){}
                
                VIPSMobileMessage vmMessage = new VIPSMobileMessage(
                        message.getMessageId(),
                        pragmaticBestLocale.getHeading(),
                        pragmaticBestLocale.getLeadParagraph(),
                        pragmaticBestLocale.getBody(),
                        message.getDatePub(),
                        message.getDateValidTo(),
                        message.getMessageTagSet().iterator().next().getMessageTagId(),
                        imageURL,
                        "Test"
                );
                
                retVal.add(vmMessage);
            }
            
            return Response.ok().entity(retVal).build();
    }
    
    @GET
    @Path("presentasjoner")
    @Produces("application/json;charset=UTF-8")
    public Response getPresentations(
            @QueryParam("varselIder") String forecastConfigurationIds,
            @QueryParam("timeZone") String timeZoneStr,
            @QueryParam("locale") String locale
    ){
        // Default locale is English
        if(locale == null)
        {
            locale = "en";
        }
        
        // Get model info
        Map<String, ModelInformation> modelInformationMap = SessionControllerGetter.getForecastBean().getIndexedModelInformation();
        
        TimeZone timeZone = timeZoneStr != null ? TimeZone.getTimeZone(timeZoneStr)
                : TimeZone.getDefault();
        List<Long> parsedIds = new ArrayList<>();
        for(String idCandidate:forecastConfigurationIds.split(","))
        {
            try
            {
                parsedIds.add(Long.valueOf(idCandidate));
            }
            catch(NumberFormatException ex){}
        }
        List<VIPSMobilePresentation> retVal = new ArrayList<>();
        if(!parsedIds.isEmpty())
        {
            List<ForecastConfiguration> configsWithSummaries = SessionControllerGetter.getForecastBean().getForecastConfigurationWithSummaries(parsedIds);
            // Initializing the array of summaries
            Date today = new WeatherUtil().normalizeToExactDate(SystemTime.getSystemTime(), timeZone);
            List<Date> datesList = new ArrayList<>();
            Integer[] statusList = new Integer[11];
            Calendar cal = Calendar.getInstance(timeZone);
            cal.setTime(today);
            cal.add(Calendar.DATE, -4);
            for(int i=0;i<11;i++)
            {
                datesList.add(cal.getTime());
                statusList[i] = Result.WARNING_STATUS_NO_WARNING_MISSING_DATA;
                cal.add(Calendar.DATE, 1);
            }
            
            for(ForecastConfiguration config:configsWithSummaries)
            {
                List<ForecastSummary> summaries = config.getForecastSummaries();
                if(summaries == null || summaries.isEmpty())
                {
                    continue;
                }
                for(ForecastSummary summary:summaries)
                {
                    for(int i=0;i<11;i++)
                    {
                        if(datesList.get(i).compareTo(summary.getForecastSummaryPK().getSummaryForDate()) == 0)
                        {
                            statusList[i] = summary.getWarningStatus();
                        }
                    }
                }
                
                VIPSMobilePresentation pres = new VIPSMobilePresentation(
                        config.getForecastConfigurationId(), 
                        today, 
                        2, 
                        statusList[0], 
                        statusList[1], 
                        statusList[2], 
                        statusList[3], 
                        statusList[4], 
                        statusList[5], 
                        statusList[6], 
                        statusList[7], 
                        statusList[8], 
                        statusList[9], 
                        statusList[10], 
                        "", 
                        "", 
                        "", 
                        "", 
                        "", 
                        "", 
                        "", 
                        "", 
                        "", 
                        "", 
                        "", 
                        config.getWeatherStationPointOfInterestId().getPointOfInterestId(), 
                        config.getPestOrganismId().getOrganismId(), 
                        config.getCropOrganismId().getOrganismId(), 
                        config.getLocationPointOfInterestId().getPointOfInterestId(), 
                        config.getModelId(), 
                        config.getWeatherStationPointOfInterestId().getName(), 
                        config.getPestOrganismId().getLocalName(locale), 
                        config.getCropOrganismId().getLocalName(locale), 
                        config.getLocationPointOfInterestId().getName(), 
                        SessionLocaleUtil.getI18nText(httpServletRequest, config.getModelId()),
                        "", 
                        ""
                );
                pres.setVarselId(config.getForecastConfigurationId());
                retVal.add(pres);
                // Reset statusList
                for(int i=0;i<11;i++)
                {
                    statusList[i] = Result.WARNING_STATUS_NO_WARNING_MISSING_DATA;
                    cal.add(Calendar.DATE, 1);
                }
            }
        }
        return Response.ok().entity(retVal).build(); 
    }
    
    @GET
    @Path("varsler")
    @Produces("application/json;charset=UTF-8")
    public Response getForecastConfigurations(
            @QueryParam("klimastasjonId") Integer klimastasjonId,
            @QueryParam("kulturId") List<Integer> kulturIder,
            @QueryParam("locale") String locale
    )
    {
        // Default locale is English
        if(locale == null)
        {
            locale = "en";
        }
        
        Date to = SystemTime.getSystemTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(to);
        cal.add(Calendar.MONTH, -2);
        Date from = cal.getTime();
        
        PointOfInterestWeatherStation station = (PointOfInterestWeatherStation) SessionControllerGetter.getPointOfInterestBean().getPointOfInterest(klimastasjonId);
        List<ForecastConfiguration> forecasts = SessionControllerGetter.getForecastBean().getForecastConfigurations(station, from, to);
        List<VIPSMobileForecast> retVal = new ArrayList<>();
        for(ForecastConfiguration forecast:forecasts)
        {
            for(Integer cropOrganismId:kulturIder)
            {
                if(cropOrganismId.equals(forecast.getCropOrganismId().getOrganismId()))
                {
                    // Create old style VIPS forecast object, add to returned list
                    VIPSMobileForecast varsel = new VIPSMobileForecast(
                            forecast.getForecastConfigurationId(), 
                            -1, 
                            forecast.getDateStart(), 
                            forecast.getDateEnd(), 
                            forecast.getWeatherStationPointOfInterestId().getPointOfInterestId(),
                            forecast.getWeatherStationPointOfInterestId().getName(), 
                            forecast.getLocationPointOfInterestId().getPointOfInterestId(), 
                            forecast.getLocationPointOfInterestId().getName(), 
                            false, 
                            false, 
                            -1, 
                            null, 
                            false, 
                            forecast.getPestOrganismId() != null ? forecast.getPestOrganismId().getOrganismId() : -1l, 
                            forecast.getPestOrganismId() != null ? 
                                    forecast.getPestOrganismId().getLocalName(locale) != null && ! forecast.getPestOrganismId().getLocalName(locale).isEmpty() ? 
                                            forecast.getPestOrganismId().getLocalName(locale)
                                            : forecast.getPestOrganismId().getLatinName()
                                    : "", 
                            forecast.getCropOrganismId().getOrganismId(), 
                            forecast.getCropOrganismId().getLocalName(locale), 
                            forecast.getModelId(), 
                            SessionLocaleUtil.getI18nText(httpServletRequest, forecast.getModelId())
                    );
                    retVal.add(varsel);
                    break;
                }
            }
        }
        return Response.ok().entity(retVal).build();   
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
}
