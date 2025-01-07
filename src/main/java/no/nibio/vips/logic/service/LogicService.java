/*
 * Copyright (c) 2022 NIBIO <http://www.nibio.no/>. 
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

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;
import jakarta.ejb.EJB;
import jakarta.persistence.NonUniqueResultException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.apache.commons.validator.routines.EmailValidator;
import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.spi.HttpRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.util.ULocale;
import com.webcohesion.enunciate.metadata.Facet;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import no.nibio.vips.coremanager.service.ManagerResource;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.authenticate.PasswordValidationException;
import no.nibio.vips.logic.controller.session.DeleteUserException;
import no.nibio.vips.logic.controller.session.ForecastBean;
import no.nibio.vips.logic.controller.session.MessageBean;
import no.nibio.vips.logic.controller.session.OrganismBean;
import no.nibio.vips.logic.controller.session.PointOfInterestBean;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.CropCategory;
import no.nibio.vips.logic.entity.CropPest;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ForecastModelConfiguration;
import no.nibio.vips.logic.entity.ForecastResult;
import no.nibio.vips.logic.entity.Message;
import no.nibio.vips.logic.entity.MessageTag;
import no.nibio.vips.logic.entity.ModelInformation;
import no.nibio.vips.logic.entity.Organism;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.PointOfInterest;
import no.nibio.vips.logic.entity.PointOfInterestType;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.entity.UserAuthentication;
import no.nibio.vips.logic.entity.UserAuthenticationType;
import no.nibio.vips.logic.entity.VipsLogicRole;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.observationdata.ObservationDataBean;
import no.nibio.vips.util.CSVPrintUtil;
import no.nibio.vips.util.ServletUtil;
import no.nibio.vips.util.SolarRadiationUtil;
import no.nibio.web.forms.FormValidationException;

/**
 * @copyright 2013-2023 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest")
public class LogicService {
    private final static String VIPSCOREMANAGER_URL = System.getProperty("no.nibio.vips.logic.VIPSCOREMANAGER_URL");
    
    @Context
    private HttpRequest httpRequest;
    @Context
    private HttpServletRequest httpServletRequest;
    
    @EJB
    ForecastBean forecastBean;
    @EJB
    UserBean userBean;
    @EJB
    OrganismBean organismBean;
    @EJB
    PointOfInterestBean pointOfInterestBean;
    @EJB
    MessageBean messageBean;
    @EJB
    ObservationDataBean observationDataBean;
    
    /**
     * Get all results for one pest prediction
     * @param forecastConfigurationId Database id of the configured forecast
     * @param userUUID if the forecast is private, the correct userUUID must be supplied. 
     * @return JSON with result data. A list of ForecastResult objects. 
     * @responseExample application/json
     * {
        "forecastResultId": 5710137,
        "validTimeStart": "2019-01-22T23:00:00.000+0000",
        "validTimeEnd": null,
        "warningStatus": 0,
        "forecastConfigurationId": -1000,
        "validGeometry": { 
            "type": "Point",
            "coordinates": [
                10.333252,
                57.179002
            ]
        },
        "keys": [ 
            "GRIDZYMOSE.WHS"
        ],
        "allValues": { 
            "GRIDZYMOSE.WHS": "0"
        }
    }
     */
    @GET
    @Path("forecastresults/{forecastConfigurationId}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    @TypeHint(ForecastResult[].class)
    public Response getForecastResults(
            @PathParam("forecastConfigurationId") Long forecastConfigurationId,
            @QueryParam("userUUID") String userUUID
    )
    {
        if(forecastBean.isUserAuthorizedForForecastConfiguration(forecastConfigurationId, userUUID))
        {
            List<ForecastResult> results = forecastBean.getForecastResults(forecastConfigurationId);
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
    
    /**
     * Get all results for one pest prediction
     * @param forecastConfigurationId
     * @param userUUID if the forecast is private, the correct userUUID must be supplied. 
     * @return 
     * @responseExample text/csv
     * Valid time start,Valid time end,Warning status,WEATHER.BT,NAERSTADMO.SPH,FORECAST.THRESHOLD_LOW,NAERSTADMO.VAS,NAERSTADMO.TSHH,NAERSTADMO.VRS,FORECAST.THRESHOLD_HIGH,NAERSTADMO.WD,WEATHER.RR,NAERSTADMO.IR,WEATHER.Q0,NAERSTADMO.RISK,WEATHER.UM,NAERSTADMO.WHS,NAERSTADMO.WH,WEATHER.TM
     * 2022-05-21 00:00:00.0,null,2,0,0,1.0,0,0,0,2.5,0,0,0,0,0,77.86,0,0,12.61
     * 2022-05-21 01:00:00.0,null,2,0,0,1.0,0,0,0,2.5,0,0,0,0,0,81.1,0,0,12.29
     * 2022-05-21 02:00:00.0,null,2,0,0,1.0,0,0,0,2.5,0,0,0,0,0,84.1,0,0,11.85
     * 2022-05-21 03:00:00.0,null,2,0,0,1.0,0,11.49,0,2.5,0,0,0,0,0,86.6,0,0,11.49
     * 2022-05-21 04:00:00.0,null,2,0,0,1.0,0,22.42,0,2.5,0,0,0,1.17,0,90.5,0,0,10.93
     * 2022-05-21 05:00:00.0,null,2,28,0,1.0,0,33.29,0,2.5,1,0,1,11.08,0,92.1,1,1,10.87
     * 2022-05-21 06:00:00.0,null,2,60,0,1.0,0,44.32,0,2.5,2,0.2,1,19.02,0,92.3,1,1,11.03
     * 2022-05-21 07:00:00.0,null,2,60,0,1.0,0,55.39,0,2.5,3,1,1,28.13,0,95,1,1,11.07
     * 2022-05-21 08:00:00.0,null,2,60,0,1.0,0,66.54,0,2.5,4,1.4,1,49.35,0,97.6,1,1,11.15
     * 2022-05-21 09:00:00.0,null,2,60,0,1.0,0,78.15,0,2.5,5,1.2,1,89.6,0,95.3,1,1,11.61
     */
    @GET
    @Path("forecastresults/{forecastConfigurationId}/csv")
    @GZIP
    @Produces("text/csv;charset=UTF-8")
    public Response getForecastResultsCSV(
            @PathParam("forecastConfigurationId") Long forecastConfigurationId,
            @QueryParam("userUUID") String userUUID
    )
    {
        if(forecastBean.isUserAuthorizedForForecastConfiguration(forecastConfigurationId, userUUID))
        {
            String CSVOutput = "";
            List<ForecastResult> results = forecastBean.getForecastResults(forecastConfigurationId);
            if(results != null && ! results.isEmpty())
            {
                List<String> parameters = new ArrayList<>();//new String[results.get(0).getKeys().size() + 3];
                parameters.add("Valid time start");
                parameters.add("Valid time end");
                parameters.add("Warning status");
                results.get(0).getKeys().stream().forEach(key->{
                    parameters.add(key);
                });
                
                CSVOutput += String.join(",", parameters) + "\n";
                
                CSVOutput += results.stream().map(result->{
                    String line = result.getValidTimeStart() + "," + result.getValidTimeEnd() + "," + result.getWarningStatus();
                    Map<String, String> valueMap = result.getAllValuesAsMap();
                    for(int i=3;i<parameters.size();i++)
                    {
                        line += "," + valueMap.get(parameters.get(i));
                    }
                    return line;
                }).collect(Collectors.joining("\n"));
                
                return Response.ok().entity(CSVOutput).build();
                
            }
            
            return Response.ok().entity("").build();
        }
        else
        {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
    
    /**
     * Get the latestDays results for one pest prediction
     * @param forecastConfigurationId
     * @param latestDays
     * @param userUUID if the forecast is private, the correct userUUID must be supplied. 
     * @return 
     * @responseExample application/json
     * {
        "forecastResultId": 5710137,
        "validTimeStart": "2019-01-22T23:00:00.000+0000",
        "validTimeEnd": null,
        "warningStatus": 0,
        "forecastConfigurationId": -1000,
        "validGeometry": { 
            "type": "Point",
            "coordinates": [
                10.333252,
                57.179002
            ]
        },
        "keys": [ 
            "GRIDZYMOSE.WHS"
        ],
        "allValues": { 
            "GRIDZYMOSE.WHS": "0"
        }
    }
     */
    @GET
    @Path("forecastresults/{forecastConfigurationId}/{latestDays}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    @TypeHint(ForecastResult[].class)
    public Response getForecastResults(
            @PathParam("forecastConfigurationId") Long forecastConfigurationId,
            @PathParam("latestDays") Integer latestDays,
            @QueryParam("userUUID") String userUUID
    )
    {
        if(forecastBean.isUserAuthorizedForForecastConfiguration(forecastConfigurationId, userUUID))
        {
            List<ForecastResult> results = forecastBean.getForecastResults(forecastConfigurationId, latestDays);
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
    
    /**
     * Get the forecast results for a particular forecast configuration in a given period
     * @param forecastConfigurationId
     * @param dateStartStr format "yyyy-MM-dd"
     * @param dateEndStr format "yyyy-MM-dd"
     * @return The forecast results for a particular forecast configuration in a given period
     * @responseExample application/json
     * {
        "forecastResultId": 5710137,
        "validTimeStart": "2019-01-22T23:00:00.000+0000",
        "validTimeEnd": null,
        "warningStatus": 0,
        "forecastConfigurationId": -1000,
        "validGeometry": { 
            "type": "Point",
            "coordinates": [
                10.333252,
                57.179002
            ]
        },
        "keys": [ 
            "GRIDZYMOSE.WHS"
        ],
        "allValues": { 
            "GRIDZYMOSE.WHS": "0"
        }
    }
    */
    @GET
    @Path("forecastresults/{forecastConfigurationId}/{dateStart}/{dateEnd}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    @TypeHint(ForecastResult[].class)
    public Response getForecastResults(
            @PathParam("forecastConfigurationId") Long forecastConfigurationId,
            @PathParam("dateStart") String dateStartStr,
            @PathParam("dateEnd") String dateEndStr
    )
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try
        {
            Date dateStart = format.parse(dateStartStr);
            Date dateEnd = format.parse(dateEndStr);
            return Response.ok().entity(forecastBean.getForecastResults(forecastConfigurationId, dateStart, dateEnd)).build();
        }
        catch(ParseException ex)
        {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }
    
    /**
     * @param organizationId Id of the organization
     * @param cropOrganismIds Integer list of crop ids
     * @param includeOrganizationIds Optional additional organization ids - include summaries from these organizations as well
     * @param userUUID unique login token (optional, used to authenticate user logged in via VIPSWeb)
     * @return A list of forecast configurations (for (a) given organization(s)) with forecast summaries attached
     */
    @GET
    @Path("forecastconfigurationsummaries/{organizationId}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(ForecastConfiguration[].class)
    public Response getForecastSummaries(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("cropOrganismId") List<Integer> cropOrganismIds,
            @QueryParam("includeOrganizationIds") String includeOrganizationIds,
            @QueryParam("userUUID") String userUUID
    )
    {
        
        VipsLogicUser user = null;
        try
        {
            UUID uUUID = UUID.fromString(userUUID);
            user = userBean.findVipsLogicUser(uUUID);
        }
        catch(NullPointerException | IllegalArgumentException ex) {}
        
        List<ForecastConfiguration> summaries = forecastBean.getForecastConfigurationSummaries(organizationId, user);
        
        if(includeOrganizationIds != null)
        {
            String[] includeOrgIdStrs = includeOrganizationIds.split(",");
            for(String orgId:includeOrgIdStrs)
            {
                try
                {
                    Integer includeOrgId = Integer.valueOf(orgId);
                    if(includeOrgId.equals(organizationId))
                    {
                        continue;
                    }
                    summaries.addAll(forecastBean.getForecastConfigurationSummaries(includeOrgId, user));
                }
                catch(NumberFormatException ex){}
            }
        }
        return Response.ok().entity(summaries).build();
    }
    
    /**
     * 
     * @param userUUID unique login token (optional, used to authenticate user logged in via VIPSWeb)
     * @return A list of forecast configurations for the user's organization with forecast summaries attached
     * @ignore
     */
    @GET
    @Path("forecastconfigurationsummaries/private/{userUUID}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    public Response getForecastSummaries(
            @PathParam("userUUID") String userUUID
    )
    {
        UUID uUUID = UUID.fromString(userUUID);
        VipsLogicUser user = userBean.findVipsLogicUser(uUUID);
        if(user != null)
        {
            List<ForecastConfiguration> summaries = forecastBean.getPrivateForecastConfigurationSummaries(user);
            return Response.ok().entity(summaries).build();
        }
        else
        {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
    }
        
    /**
     * Get the configuration of the specified forecast
     * @param forecastConfigurationId The ID of the requested configuration (crop, pest, model, location, period, owner etc.)
     * @param userUUID if the forecast is private, the correct userUUID must be supplied. 
     * @return the configuration (crop, pest, model, location, period, owner etc.) of the specified forecast
     */
    @GET
    @Path("forecastconfigurations/{forecastConfigurationId}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(ForecastConfiguration.class)
    public Response getForecastConfiguration(
            @PathParam("forecastConfigurationId") Long forecastConfigurationId,
            @QueryParam("userUUID") String userUUID
    )
    {
        if(forecastBean.isUserAuthorizedForForecastConfiguration(forecastConfigurationId, userUUID))
        {
            ForecastConfiguration forecastConfiguration = forecastBean.getForecastConfiguration(forecastConfigurationId);
            return Response.ok().entity(forecastConfiguration).build();
        }
        else
        {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }
    
    /**
     * Returns public forecast configurations for the given model and season
     * @param modelId The ID of the model. 10 character string. E.g. PSILARTEMP
     * @param year The year for which to find the configured forecasts
     * @return 
     */
    @GET
    @Path("forecastconfigurations/model/{modelId}/{year}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(ForecastConfiguration[].class)
    public Response getForecastConfigurationsForModel(@PathParam("modelId") String modelId, @PathParam("year") Integer year)
    {
        return Response.ok().entity(forecastBean.getForecastConfigurationsForModel(modelId, year)).build();
    }
    
    
    /**
     * Returns private forecast configurations for the given user
     * @param userUUID unique login token (optional, used to authenticate user logged in via VIPSWeb)
     * @return 
     */
    @GET
    @Path("forecastconfigurations/private/{userUUID}")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(ForecastConfiguration[].class)
    public Response getPrivateForecastConfigurations(@PathParam("userUUID") String userUUID)
    {
        try
        {
            UUID uUUID = UUID.fromString(userUUID);
            VipsLogicUser user = userBean.findVipsLogicUser(uUUID);
            if(user != null)
            {
                List<ForecastConfiguration> retVal = forecastBean.getPrivateForecastConfigurationsForUser(user.getUserId());
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
     * 
     * @param organizationId The primary organization to get forecast configurations from
     * @param includeOrganizationIds Additional organizations to get forecast configurations from
     * @param fromStr Dateformat = "yyyy-MM-dd"
     * @param toStr Dateformat = "yyyy-MM-dd"
     * @return A list of forecast configurations (for (a) given organization(s))
     */
    @GET
    @Path("forecastconfigurationsincludeorgs/{organizationId}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    @TypeHint(ForecastConfiguration[].class)
    public Response getActiveForecastConfigurationsWithIncludeOrganizations(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("includeOrganizationIds") String includeOrganizationIds,
            @QueryParam("from") String fromStr,
            @QueryParam("to") String toStr
    )
    {
        Date from, to;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try{
            from = format.parse(fromStr);
            to = format.parse(toStr);
        }
        catch(ParseException | NullPointerException ex)
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime(SystemTime.getSystemTime());
            cal.set(cal.get(Calendar.YEAR), Calendar.JANUARY, 1, 0, 0, 0);
            from = cal.getTime();
            cal.set(cal.get(Calendar.YEAR), Calendar.DECEMBER, 31, 23, 0, 0);
            to = cal.getTime();
        }
        
        
        List<Integer> orgIds = new ArrayList<>();
        orgIds.add(organizationId);
        if(includeOrganizationIds != null)
        {
            String[] includeOrgIdStrs = includeOrganizationIds.split(",");
            for(String orgIdStr:includeOrgIdStrs)
            {
                try
                {
                    Integer includeOrgId = Integer.valueOf(orgIdStr.trim());
                    if(includeOrgId.equals(organizationId))
                    {
                        continue;
                    }
                    orgIds.add(includeOrgId);
                }
                catch(NumberFormatException ex){}
            }
        }
        List<ForecastConfiguration> forecastConfigs = forecastBean.getForecastConfigurations(orgIds, from, to);
        return Response.ok().entity(forecastConfigs).build();
    }
    
    /**
     * Returns a list of forecasts for given organization
     * @param organizationId
     * @param cropOrganismIds
     * @param fromStr format="yyyy-MM-dd"
     * @param toStr format="yyyy-MM-dd"
     * @return 
     */
    @GET
    @Path("organizationforecastconfigurations/{organizationId}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    @TypeHint(ForecastConfiguration[].class)
    public Response getForecastConfigurationsForOrganization(
            @PathParam("organizationId") Integer organizationId, 
            @QueryParam("cropOrganismId") List<Integer> cropOrganismIds,
            @QueryParam("from") String fromStr,
            @QueryParam("to") String toStr
            )
    {
        Date from, to;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try{
            from = format.parse(fromStr);
            to = format.parse(toStr);
        }
        catch(ParseException | NullPointerException ex)
        {
            to = SystemTime.getSystemTime();
            Calendar cal = Calendar.getInstance();
            cal.setTime(to);
            cal.add(Calendar.MONTH, -4);
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
        
        // We filter out all specialized forecasts (id < 0)
        forecasts = forecasts.stream().filter(forecast->forecast.getForecastConfigurationId() > 0).collect(Collectors.toList());
        
        return Response.ok().entity(forecasts).build();
    }
    
    
    /**
     * Check if a proposed password meets the requirements configured by Passay
     * @param password
     * @return 
     */
    @GET
    @Path("evaluatepassword/{password}")
    @Produces("text/plain;charset=UTF-8")
    @Facet("restricted")
    public Response evaluatePassord(@PathParam("password") String password)
    {
        ULocale currentLocale = SessionLocaleUtil.getCurrentLocale(httpServletRequest);
        try
        {
            // Invalid passwords always cause a PasswordValidationException to be thrown
            Boolean isPasswordValid = userBean.isPasswordValid(password, currentLocale);
            return Response.ok().entity(isPasswordValid).build();
            
        }
        catch(PasswordValidationException ex)
        {
            return Response.status(Status.BAD_REQUEST).entity(ex.getMessage()).build();
        }
    }
    
    /**
     * The model configuration (model specific parameters and their values) for the given forecast configuration
     * @param forecastConfigurationId
     * @return 
     */
    @GET
    @Path("forecastmodelconfiguration/{forecastConfigurationId}")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(ForecastModelConfiguration.class)
    public Response getForecastModelConfiguration(@PathParam("forecastConfigurationId") Long forecastConfigurationId)
    {
        List<ForecastModelConfiguration> forecastModelConfigurations = forecastBean.getForecastModelConfigurations(forecastConfigurationId);
        return Response.ok().entity(forecastModelConfigurations).build();
    }
    
    /**
     * 
     * @param organizationId Get POIs for this organization
     * @param cropCategoryIds Optionally filter by crop category ids (comma separated)
     * @param userUUID unique login token (optional, used to authenticate user logged in via VIPSWeb)
     * @return a KML file with the "worst" warning status for each POI
     */
    @GET
    @Path("forecastresults/aggregate/{organizationId}")
    @GZIP
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    @Facet("restricted")
    public Response getForecastResultsAggregate(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("cropCategoryId") List<Integer> cropCategoryIds,
            @QueryParam("userUUID") String userUUID)
    {
        VipsLogicUser viewUser = null;
        if(userUUID != null)
        {
            try
            {
                UUID uUUID = UUID.fromString(userUUID);
                viewUser = userBean.findVipsLogicUser(uUUID);
            }
            catch(IllegalArgumentException ex)
            {
                // Skip this
            }
        }
        List<Integer> organizationIds = new ArrayList<>();
        organizationIds.add(organizationId);
        if(cropCategoryIds == null || cropCategoryIds.isEmpty())
        {
            return Response.noContent().build();
        }
        else
        {
            List<Integer> cropOrganismIds = organismBean.getCropCategoryOrganismIds(cropCategoryIds);
            Kml retVal = forecastBean.getForecastsAggregateKml(organizationIds, cropOrganismIds, SystemTime.getSystemTime(), ServletUtil.getServerName(httpServletRequest), viewUser);
            return Response.ok().entity(retVal).build();
        }
    }
    /**
     * 
     * @param organizationIds
     * @param cropCategoryIds
     * @param userUUID unique login token (optional, used to authenticate user logged in via VIPSWeb)
     * @return a KML file with the "worst" warning status for each POI
     */
    @GET
    @Path("forecastresults/aggregate/orgspan")
    @GZIP
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    @Facet("restricted")
    public Response getForecastResultsAggregate(
            @QueryParam("organizationId") List<Integer> organizationIds,
            @QueryParam("cropCategoryId") List<Integer> cropCategoryIds,
            @QueryParam("userUUID") String userUUID)
    {
        if(cropCategoryIds == null || cropCategoryIds.isEmpty())
        {
            return Response.noContent().build();
        }
        else
        {
            VipsLogicUser viewUser = null;
            if(userUUID != null)
            {
                try
                {
                    UUID uUUID = UUID.fromString(userUUID);
                    viewUser = userBean.findVipsLogicUser(uUUID);
                }
                catch(IllegalArgumentException ex)
                {
                    // Skip this
                }
            }
            List<Integer> cropOrganismIds = organismBean.getCropCategoryOrganismIds(cropCategoryIds);
            Kml retVal = forecastBean.getForecastsAggregateKml(organizationIds, cropOrganismIds, SystemTime.getSystemTime(), ServletUtil.getServerName(httpServletRequest), viewUser);
            return Response.ok().entity(retVal).build();
        }
    }
    
    /**
     * 
     * @param poiId
     * @return The latest forecast results (within the current season) for a given Point Of Interest (poi)
     */
    @GET
    @Path("forecastresults/latest/poi/{poiId}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    public Response getLatestForecastResultsForPoi(@PathParam("poiId") Integer poiId)
    {
        Map<String, Object> latestResults = forecastBean.getLatestForecastResultsForPoi(poiId);
        return Response.ok().entity(latestResults).build();
    }
    
    /**
     * Get a list of weather stations for a given organization
     * @param excludeWeatherStationId Exclude this weather station from the KML
     * @param highlightWeatherStationId Show highlight icon for this weather station
     * @param organizationId
     * @return a KML with weather stations for an organization
     */
    @GET
    @Path("weatherstations/kml/{organizationId}")
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    public Response getWeatherStations(
            @QueryParam("excludeWeatherStationId") Integer excludeWeatherStationId, 
            @QueryParam("highlightWeatherStationId") Integer highlightWeatherStationId, 
            @PathParam("organizationId") Integer organizationId
    )
    {
        Kml retVal = pointOfInterestBean.getPoisForOrganization(organizationId, excludeWeatherStationId, highlightWeatherStationId, ServletUtil.getServerName(httpServletRequest), SessionLocaleUtil.getI18nBundle(httpServletRequest), PointOfInterestType.POINT_OF_INTEREST_TYPE_WEATHER_STATION);
        return Response.ok().entity(retVal).build();
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
    @Path("pois/kml/{organizationId}")
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    public Response getPois(
            @QueryParam("excludePoiId") Integer excludePoiId, 
            @QueryParam("highlightPoiId") Integer highlightPoiId, 
            @PathParam("organizationId") Integer organizationId
    )
    {
        Kml retVal = pointOfInterestBean.getPoisForOrganization(organizationId, excludePoiId, highlightPoiId, ServletUtil.getServerName(httpServletRequest), SessionLocaleUtil.getI18nBundle(httpServletRequest), null);
        return Response.ok().entity(retVal).build();
    }
    

    
    /**
     * Get a list of locations (pois) for a given organization
     * @param organizationId
     * @return 
     */
    @GET
    @Path("poi/organization/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(PointOfInterestWeatherStation[].class)
    public Response getPoisForOrganization(@PathParam("organizationId") Integer organizationId)
    {
        Organization organization = userBean.getOrganization(organizationId);
        List<PointOfInterestWeatherStation> retVal = pointOfInterestBean.getWeatherstationsForOrganization(organization, Boolean.TRUE);
        return Response.ok().entity(retVal).build();
    }
    
    /**
     * 
     * @param pointOfInterestId
     * @return a particular POI (Point of interest)
     */
    @GET
    @Path("poi/{pointOfInterestId}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(PointOfInterest.class)
    public Response getPoi(@PathParam("pointOfInterestId") Integer pointOfInterestId)
    {
        PointOfInterest retVal = pointOfInterestBean.getPointOfInterest(pointOfInterestId);
        return Response.ok().entity(retVal).build();
    }
    
    /**
     * Find a POI (Point of interest) by name
     * @param poiName
     * @return 
     */
    @GET
    @Path("poi/name/{poiName}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(PointOfInterest.class)
    public Response getPoiByName(@PathParam("poiName") String poiName)
    {
        PointOfInterest retVal = pointOfInterestBean.getPointOfInterest(poiName);
        return retVal != null ? Response.ok().entity(retVal).build() : Response.noContent().build();
    }
    
    /**
     * If used outside of VIPSLogic: Requires a valid UUID to be provided in the Authorization header
     * @return a list of POIs for the user logged in in this session
     */
    @GET
    @Path("poi/user")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(PointOfInterest[].class)
    public Response getPoisForCurrentUser()
    {
        VipsLogicUser user = (VipsLogicUser) httpServletRequest.getSession().getAttribute("user");
        // Could be the VIPS obs app or some other client using UUID
        if(user == null)
        {
        	String uuidStr = httpServletRequest.getHeader(HttpHeaders.AUTHORIZATION);
    		UUID uuid = UUID.fromString(uuidStr);
    		user = userBean.findVipsLogicUser(uuid);
        }
        List<PointOfInterest> retVal = pointOfInterestBean.getRelevantPointOfInterestsForUser(user);
        return Response.ok().entity(retVal).build();
    }
    
    /**
     * 
     * @return A list of all organisms (pests and crops)
     */
    @GET
    @Path("organism/list")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(Organism[].class)
    public Response getOrganismList()
    {
        List<Organism> organismList = organismBean.getOrganismSubTree(null);
        return Response.ok().entity(organismList).build();
    }
    
    /**
     * Look up (an) organism(s) by its/their latin name(s)
     * @param keywords comma separated list of latin names
     * @return List of matching organisms (pests and crops)
     */
    @GET
    @Path("organism/search/latinnames")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(Organism[].class)
    public Response findOrganismsByLatinNames(@QueryParam("keywords") String keywords)
    {
        List<String> latinNames = Arrays.asList(keywords.split(","));
        List<Organism> organismList = organismBean.findOrganismsByLatinNames(latinNames);
        return Response.ok().entity(organismList).build();
    }
    
    /**
     * Look up organisms by local names
     * @param locale two-letter language code
     * @param keywords Comma separated list of local name
     * @return List of matching organisms (pests and crops)
     */
    @GET
    @Path("organism/search/localnames/{locale}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(Organism[].class)
    public Response findOrganismsByLocalNames(
            @PathParam("locale") String locale,
            @QueryParam("keywords") String keywords
    )
    {
        List<String> localNames = Arrays.asList(keywords.split(",")).stream().map(String::trim).collect(Collectors.toList());
        List<Organism> organismList = organismBean.findOrganismsByLocalNames(localNames, locale);
        return Response.ok().entity(organismList).build();
    }
    
    /**
     * Get a list of all crops
     * @return A list of all crops
     */
    @GET
    @Path("organism/crop/list")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(Organism[].class)
    public Response getCropOrganismList()
    {
        List<Organism> organismList = organismBean.getAllCrops();
        return Response.ok().entity(organismList).build();
    }
    
    /**
     * Get a list of all pests, OR if cropOrganismId is specified,
     * get a list of all pests that are connected with this crop
     * @param cropOrganismId optional if set, only pests for this crop are returned
     * @param organizationId optional if set, observation data schemas are added
     * @return 
     */
    @GET
    @Path("organism/pest/list")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(Organism[].class)
    public Response getPestOrganismList(
    		@QueryParam("cropOrganismId") Integer cropOrganismId,
    		@QueryParam("organizationId") Integer organizationId
    		)
    {
    	List<Organism> organismList;
    	if(cropOrganismId == null)
    	{
    		organismList = organismBean.getAllPests();
    	}
    	else
    	{
    		organismList = organismBean.getCropPests(cropOrganismId);
    	}
    	if(organizationId != null)
    	{
    		VipsLogicUser user = userBean.getUserFromUUID(httpServletRequest);
            
            
    		ULocale locale = new ULocale(
            	user != null ? user.getOrganizationId().getDefaultLocale() : 
            		userBean.getOrganization(organizationId).getDefaultLocale());
    		organismList = observationDataBean.decoratePestsWithOrganismDataSchema(organismList, organizationId, httpServletRequest, locale);
    	}
        return Response.ok().entity(organismList).build();
    }
    
    /**
     * Returns a list of all Crop ids with connected
     * pest ids as arrays.
     * @return 
     */
    @GET
    @Path("organism/crop/pest/list")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    public Response getCropPestList(
    		)
    {
        return Response.ok().entity(organismBean.getCropPestsMapped()).build();
    }
    
    /**
     * 
     * @param messageId the ID of the news message
     * @return a news message
     */
    @GET
    @Path("message/{messageId}")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(Message.class)
    public Response getMessage(@PathParam("messageId") Integer messageId)
    {
        Message message = messageBean.getMessage(messageId);
        if(message != null)
        {
            return Response.ok().entity(message).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }
    
    /**
     * 
     * @param publishedFrom Format "yyyy-MM-dd"
     * @param publishedTo Format "yyyy-MM-dd"
     * @param locale two letter language code for preferred language version (if it exists)
     * @param organizationId The organization for which to get messages
     * @return a list of news messages, filtered by the parameters given
     */
    @GET
    @Path("message/list/{organizationId}")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(Message[].class)
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
            
            List<Message> messageList = messageBean.getMessageList(organizationId, datePublishedFrom, datePublishedTo);
            return Response.ok().entity(messageList).build();
        }
        catch(ParseException ex){
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }
    
    /**
     * 
     * @param tagIds comma separated list of tagIds to filter the news messages
     * Use the messagetag/list endpoint to see what tags are available
     * @param organizationId The organization for which to get messages
     * @return a list of news messages, filtered by the parameters given
     */
    @GET
    @Path("message/list/{organizationId}/tagfilter")
    @GZIP
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(Message[].class)
    public Response getMessageListWithTags(
            @QueryParam("tagId") List<Integer> tagIds, 
            @PathParam("organizationId") Integer organizationId
    )
    {
        List<Message> messageListWithTags = messageBean.getCurrentFilteredMessagesForOrganization(tagIds, organizationId);
        return Response.ok().entity(messageListWithTags).build();
    }
    
    /**
     * 
     * @return a list of available message tags (for filtering messages)
     */
    @GET
    @Path("messagetag/list")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(MessageTag[].class)
    public Response getMessageTagList()
    {
        List<MessageTag> messageTags = messageBean.getMessageTagList();
        return Response.ok().entity(messageTags).build();
    }
    
    /**
     * Not ready for production use!
     * @param latitude
     * @param longitude
     * @param startTimeStr
     * @param endTimeStr
     * @param timeZoneStr
     * @param logIntervalId
     * @return 
     */
    @GET
    @Path("weather/calculation/solarradiation/json")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
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
    
    /**
     * Not ready for production use!
     * @param latitude
     * @param longitude
     * @param startTimeStr
     * @param endTimeStr
     * @param timeZoneStr
     * @param logIntervalId
     * @return 
     */
    @GET
    @Path("weather/calculation/solarradiation/csv")
    @Produces("text/csv;charset=UTF-8")
    @Facet("restricted")
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
    
    /**
     * Not ready for production use!
     * @param latitude
     * @param longitude
     * @param startTimeStr
     * @param endTimeStr
     * @param timeZoneStr
     * @param logIntervalId
     * @return
     * @throws ParseException 
     */
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
    

    /**
     * Service available locally for cron jobs. Most useful on test servers
     * @return 
     */
    @GET
    @Path("batch/updateforecastcaches")
    @Produces("text/plain;charset=UTF-8")
    @Facet("restricted")
    public Response updateForecastCaches()
    {
        //System.out.println(httpServletRequest.getHeader("X-Forwarded-For"));
        if(!ServletUtil.getClientIP(httpServletRequest).equals("127.0.0.1"))
        {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        Date start = new Date();
        forecastBean.updateForecastResultCacheTable();
        forecastBean.updateForecastSummaryTable(SystemTime.getSystemTime());
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
     * @ignore
     */
    @GET
    @Path("user/uuid/{userUUID}")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    public Response getVipsLogicUserByUUID(@PathParam("userUUID") String userUUID)
    {
        try
        {
            UUID uUUID = UUID.fromString(userUUID);
            VipsLogicUser user = userBean.findVipsLogicUser(uUUID);
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
    
    /**
     * TODO: Must be authenticated or not??
     * @param userUUID
     * @return 
     * @ignore
     */
    @DELETE
    @Path("user/uuid/{userUUID}")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    public Response deleteVipsLogicUserUUID(@PathParam("userUUID") String userUUID)
    {
        try
        {
            UUID uUUID = UUID.fromString(userUUID);
            userBean.deleteUserUuid(uUUID);
            return Response.ok().build();
        }
        catch(IllegalArgumentException ex)
        {
            return Response.serverError().entity(ex.getMessage()).build();
        }
    }
    
    /**
     * 
     * @param cropOrganismId ID of the crop
     * @return list of pests associated with the given crop
     */
    @GET
    @Path("organism/croppest/{cropOrganismId}")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(CropPest.class)
    public Response getCropPest(@PathParam("cropOrganismId") Integer cropOrganismId)
    {
        CropPest retVal = organismBean.getCropPestRecursive(cropOrganismId,true);
        if(retVal != null)
        {
            return Response.ok().entity(retVal).build();
        }
        else
        {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }
    
    /**
     * 
     * @param organizationId
     * @return List of crop categories for a given organization
     */
    @GET
    @Path("organism/cropcategory/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    @Facet("restricted")
    @TypeHint(CropCategory[].class)
    public Response getCropCategories(@PathParam("organizationId") Integer organizationId)
    {
        if(organizationId != null)
        {
            return Response.ok().entity(organismBean.getCropCategories(organizationId)).build();
        }
        else
        {
            return Response.noContent().build();
        }
    }
    
    @GET
    @Path("organization")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(Organization[].class)
    public Response getOrganizations()
    {
        return Response.ok().entity(userBean.getOrganizations()).build();
    }

    @GET
    @Path("organization/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(Organization[].class)
    public Response getOrganizations(@PathParam("organizationId") Integer organizationId)
    {
        return Response.ok().entity(userBean.getOrganization(organizationId)).build();
    }
    
    @GET
    @Path("model/{modelId}")
    @Produces("application/json;charset=UTF-8")
    @TypeHint(ModelInformation.class)
    public Response getModelInformation(@PathParam("modelId") String modelId)
    {
        ModelInformation retVal = forecastBean.getModelInformation(modelId);
        return retVal != null ? Response.ok().entity(retVal).build() 
                : Response.status(Response.Status.NOT_FOUND).entity("ERROR: Could not find model with id=" + modelId).build();
    }


    /**
     * Registers a user and grants limited access to certain functionalities in the VIPSLogic system:
     * <ul>
     *     <li>Adding observations - default not approved</li>
     *     <li>Adding POIs (Points Of Interest)</li>
     * </ul>
     * The user must be approved
     * @param userInfoBody
     * @return
     */
    @POST
    @Path("user/register")
    @Consumes("application/json;charset=UTF-8")
    @Produces("application/json;charset=UTF-8")
    public Response registerNewLimitedUser(String userInfoBody)
    {

        try {
            HashMap<String, Object> userInfo = new ObjectMapper().readValue(userInfoBody, new TypeReference<HashMap<String, Object>>() {
            });
            // Input control
            List<String> errorMessages = new ArrayList<>();
            // Email

            String email = ((String) userInfo.get("email")).toLowerCase();
            // Set?
            if(email == null || email.isBlank())
            {
                errorMessages.add("Email must be set");
            }
            // Must be valid email
            else if(!EmailValidator.getInstance().isValid(email))
            {
                errorMessages.add(email + " is not a valid email address");
            }
            else
            {
                // Must be unique
                Boolean emailAlreadyInUse = false;
                try {
                    VipsLogicUser foundUser = userBean.getUserByEmail(email);
                    emailAlreadyInUse = (foundUser != null);
                } catch (NonUniqueResultException ex) {
                    emailAlreadyInUse = true;
                }
                if (emailAlreadyInUse) {
                    errorMessages.add("Email " + email + " is already in use");
                }
            }

            // Username
            String username = (String) userInfo.get("username");
            // Set?
            if(username == null || username.isBlank())
            {
                errorMessages.add("Username must be set");
            }
            else
            {
                // Existing username?
                Boolean usernameExists = false;
                try
                {
                    VipsLogicUser foundUser = userBean.getUser(username, UserAuthenticationType.TYPE_PASSWORD);
                    usernameExists = (foundUser != null);
                }
                catch(NonUniqueResultException ex)
                {
                    usernameExists = true;
                }
                if(usernameExists)
                {
                    errorMessages.add("Username " + username  + " already exists");
                }
            }

            // First name
            String firstName = (String) userInfo.get("firstName");
            if(firstName == null || firstName.isBlank())
            {
                errorMessages.add("First name must be set");
            }

            // Last name
            String lastName = (String) userInfo.get("lastName");
            if(lastName == null || lastName.isBlank())
            {
                errorMessages.add("Last name must be set");
            }


            // Password
            String password = (String) userInfo.get("password");
            if(password == null || password.isBlank())
            {
                errorMessages.add("Password must be set");
            }

            if(errorMessages.size() > 0)
            {
                Map<String, List<String>> errorMsg = Map.of("errorMessages",errorMessages);
                return Response.status(Status.BAD_REQUEST).entity(errorMsg).build();
            }

            VipsLogicUser user = new VipsLogicUser();
            user.setFirstName(firstName.trim());
            user.setLastName(lastName.trim());
            user.setEmail(email.trim());
            user.setPhoneCountryCode((String) userInfo.get("phoneCountryCode"));
            user.setPreferredLocale((String) userInfo.get("preferredLocale"));
            user.setOrganizationId(userBean.getOrganization((Integer) userInfo.get("organizationId")));
            user.setApprovalApplication("Registered in app");
            user.setUserStatusId(Globals.USER_STATUS_AWAITING_EMAIL_VERIFICATION);
            // Add observer role
            user.setVipsLogicRoles(Set.of(userBean.getVipsLogicRole(VipsLogicRole.OBSERVER)));
            // Set user authentication
            UserAuthenticationType uat = userBean.createUserAuthenticationTypeInstance(UserAuthenticationType.TYPE_PASSWORD);
            UserAuthentication ua = new UserAuthentication();
            ua.setUserAuthenticationType(uat);
            ua.setUsername(username.trim());
            ua.setPassword(userBean.getMD5EncryptedString(password.trim()));
            userBean.storeUserFirstTime(user, ua);
            userBean.sendUserEmailVerification(user, SessionLocaleUtil.getI18nBundle(httpServletRequest), ServletUtil.getServerName(httpServletRequest));

            return Response.status(Status.OK).entity(user).build();
        }
        catch(FormValidationException | IOException ex)
        {
            return Response.status(Status.BAD_REQUEST).entity("INPUT ERROR: " + ex.getMessage()).build();
        }
    }

    /**
     * Allows a user to delete their account
     * @param keepData if true, move all data to default user.
     * @return
     */
    @DELETE
    @Path("user/deleteme")
    public Response deleteMe(@QueryParam("keepData") Boolean keepData) {
        // Authentication
        // Either valid UUID or session
        VipsLogicUser user = userBean.getUserFromUUID(httpServletRequest);
        if(user == null)
        {
            user = (VipsLogicUser) httpServletRequest.getSession().getAttribute("user");
        }
        if(user == null)
        {
            return Response.status(Status.UNAUTHORIZED).entity("You are not authorized to perform this operation").build();
        }

        // If it's an archive user, do NOT delete it!
        if(user.getOrganizationId().getArchiveUser() != null && user.getOrganizationId().getArchiveUser().getUserId().equals(user.getUserId()))
        {
            return Response.status(Status.BAD_REQUEST).entity("User is an archive user for organization " + user.getOrganizationId().getOrganizationName() + ". Can't delete it").build();
        }
        try {
            if (keepData != null && keepData) {
                // Get default user for organization
                VipsLogicUser archiveUser = user.getOrganizationId().getArchiveUser();
                if (archiveUser == null) {
                    return Response.status(Status.BAD_REQUEST).entity("Your organization " + user.getOrganizationId().getOrganizationName() + " has not defined a default user for archiving your data. Please contact your systems administrator to fix this.").build();
                }
                userBean.transferUserResources(user, archiveUser);
            } else {
                userBean.deleteUserResources(user);
            }
            // Delete the user
            userBean.deleteUser(user);
            return Response.status(Status.NO_CONTENT).build();
        }
        catch(DeleteUserException ex)
        {
            return Response.serverError().entity(ex.getMessage()).build();
        }

    }
    
    /**
     * Get the client to use for calling VIPSCoreManager REST services programmatically
     * @return 
     */
    private ManagerResource getManagerResource()
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(VIPSCOREMANAGER_URL);
        ResteasyWebTarget rTarget = (ResteasyWebTarget) target;
        ManagerResource resource = rTarget.proxy(ManagerResource.class);
        return resource;
    }
    
    
    
}
