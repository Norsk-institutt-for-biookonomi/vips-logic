/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.service;

import com.webcohesion.enunciate.metadata.Facet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TimeZone;
import jakarta.ejb.EJB;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import no.nibio.vips.entity.Result;
import no.nibio.vips.logic.controller.session.ForecastBean;
import no.nibio.vips.logic.controller.session.MessageBean;
import no.nibio.vips.logic.controller.session.ObservationBean;
import no.nibio.vips.logic.controller.session.OrganismBean;
import no.nibio.vips.logic.controller.session.PointOfInterestBean;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.CropCategory;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ForecastSummary;
import no.nibio.vips.logic.entity.Message;
import no.nibio.vips.logic.entity.MessageIllustration;
import no.nibio.vips.logic.entity.MessageLocale;
import no.nibio.vips.logic.entity.ModelInformation;
import no.nibio.vips.logic.entity.Observation;
import no.nibio.vips.logic.entity.Organism;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.entity.vipsmobile.VIPSMobileCropCategory;
import no.nibio.vips.logic.entity.vipsmobile.VIPSMobileForecast;
import no.nibio.vips.logic.entity.vipsmobile.VIPSMobileMessage;
import no.nibio.vips.logic.entity.vipsmobile.VIPSMobilePresentation;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.WeatherUtil;
import org.jboss.resteasy.spi.HttpRequest;

/**
 * The purpose of this service is to mimic the services for VIPSMobile that
 * were implemented in the old VIPS. It's assumed that it's easier to adapt the
 * service layer than the client layer in Sencha Touch...
 * 
 * @copyright 2017 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Facet("restricted")
@Path("rest/vipsmobile")
public class VIPSMobileService {
    
    @Context
    private HttpRequest httpRequest;
    @Context
    private HttpServletRequest httpServletRequest;
    
    @EJB
    MessageBean messageBean;
    @EJB
    ObservationBean observationBean;
    @EJB
    OrganismBean organismBean;
    @EJB
    ForecastBean forecastBean;
    @EJB
    PointOfInterestBean pointOfInterestBean;
    @EJB
    UserBean userBean;
    
    // TODO: Add observation messages
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
            List<Message> messageList = messageBean.getMessageList(organizationId, datePublishedFrom, datePublishedTo);
            
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
                Integer messageTagId = 2; // Default: Advisory system message
                try
                {
                    MessageIllustration illustration = message.getMessageIllustrationSet().iterator().next();
                    imageURL = String.format("/static/images/messages/%s/%s", pragmaticBestLocale.getCreatedBy().getOrganizationId().getOrganizationId(), illustration.getMessageIllustrationPK().getFileName());
                    messageTagId = message.getMessageTagSet().iterator().next().getMessageTagId();
                }catch(NoSuchElementException | NullPointerException ex){}
                
                VIPSMobileMessage vmMessage = new VIPSMobileMessage(
                        message.getMessageId(),
                        pragmaticBestLocale.getHeading(),
                        pragmaticBestLocale.getLeadParagraph(),
                        pragmaticBestLocale.getBody(),
                        message.getDatePub(),
                        message.getDateValidTo(),
                        messageTagId,
                        imageURL,
                        "Test"
                );
                
                retVal.add(vmMessage);
            }
            
            // TODO Add observation messages
            // Using code specific to current VIPSMobile app
            Calendar cal = Calendar.getInstance();
            cal.setTime(datePublishedFrom);
            Integer observationMessageTagId = 1;
            List<Observation> broadcastObservations = observationBean.getBroadcastObservations(organizationId, cal.get(Calendar.YEAR));
            // Making an observation message valid for 3 months by default
            
            broadcastObservations.stream().filter(
                    obs -> obs.getBroadcastMessage() && !obs.getObservationHeading().trim().isEmpty()
            ).forEach(
                    obs -> {
                        cal.setTime(obs.getTimeOfObservation());
                        cal.add(Calendar.MONTH, 3);
                        Date msgValidTo = cal.getTime();
                        VIPSMobileMessage vmMessage = new VIPSMobileMessage(
                            obs.getObservationId() * 1000000,
                            obs.getObservationHeading(),
                            "",
                            obs.getObservationText(),
                            obs.getTimeOfObservation(),
                            msgValidTo,
                            observationMessageTagId,
                            "",
                            "Test"
                        );
                        retVal.add(vmMessage);
                    }
            );
            Collections.sort(retVal, Collections.reverseOrder());
            return Response.ok().entity(retVal).build();
    }
    
    @GET
    @Path("varselkulturkategorier")
    @Produces("application/json;charset=UTF-8")
    public Response getCropCategories(
            @QueryParam("organizationId") Integer organizationId,
            @QueryParam("locale") String locale
        )
    {
        List<Organism> cropList = organismBean.getAllCrops();
        Map<Integer, Organism> allCrops = new HashMap<>();
        cropList.forEach((crop) -> {
            allCrops.put(crop.getOrganismId(), crop);
        });
        List<CropCategory> cropCategories = organismBean.getCropCategories(organizationId);
        List<VIPSMobileCropCategory> retVal = new ArrayList<>();
        cropCategories.stream().forEach(
                cc -> {
                    List<Organism> ccCropList = new ArrayList<>();
                    Arrays.asList(cc.getCropOrganismIds()).stream().forEach(
                            cropId -> {
                                ccCropList.add(allCrops.get(cropId));
                            }
                    );
                    retVal.add(new VIPSMobileCropCategory(cc, ccCropList, "nb"));
                }
        );
        
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
        Map<String, ModelInformation> modelInformationMap = forecastBean.getIndexedModelInformation();
        
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
            List<ForecastConfiguration> configsWithSummaries = forecastBean.getForecastConfigurationWithSummaries(parsedIds);
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
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        Date from = cal.getTime();
        
        PointOfInterestWeatherStation station = (PointOfInterestWeatherStation) pointOfInterestBean.getPointOfInterest(klimastasjonId);
        List<ForecastConfiguration> forecasts = forecastBean.getForecastConfigurations(station, from, to);
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
        List<VipsLogicUser> organizationUsers = userBean.getUsersByOrganization(organizationId);
        // Then: Get forecasts for these users, collate and return
        List<ForecastConfiguration> forecasts = new ArrayList<>();
        
        for(VipsLogicUser user:organizationUsers)
        {
            Integer userId = user.getUserId();
            List<ForecastConfiguration> result = cropOrganismIds != null && ! cropOrganismIds.isEmpty() ?
                    forecastBean.getForecastConfigurationsForUserAndCropsAndDate(userId,cropOrganismIds, from, to)
                    : forecastBean.getForecastConfigurationsForUserAndDate(userId, from, to);
            if(forecasts == null)
                forecasts = result;
            else
                forecasts.addAll(result);
        }
        
        return Response.ok().entity(forecasts).build();
    }
}
