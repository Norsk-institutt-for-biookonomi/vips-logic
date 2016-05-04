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

package no.nibio.vips.logic.modules.roughage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.Result;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.util.RunModelException;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.InvalidAggregationTypeException;
import no.nibio.vips.util.ParseRESTParamUtil;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.WeatherObservationListException;
import no.nibio.vips.util.WeatherUtil;
import no.nibio.vips.util.weather.WeatherDataSourceException;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;

/**
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Path("rest/roughage")
public class RoughageService {
    private final static String VIPSCOREMANAGER_URL = System.getProperty("no.nibio.vips.logic.VIPSCOREMANAGER_URL");

    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    /**
     * Temporary hack to get correct normal data weather stations
     * @return 
     */
    @GET
    @Path("normaldataweatherstations")
    @Produces("application/json;charset=UTF-8")
    public Response getWeatherStations()
    {
        String[] stationNames = {"Apelsvoll","Fureneset","Holt","Kvithamar","Landvik","Løken","Særheim","Tjøtta", "Ås"};
        Query q = em.createNamedQuery("PointOfInterestWeatherStation.findByNames");
        q.setParameter("names", Arrays.asList(stationNames));
        List<PointOfInterestWeatherStation> stations = q.getResultList();
        return Response.ok().entity(stations).build();
    }
    
    @GET
    @Path("nutrition/runmodel/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    public Response runRoughageNutritionModel(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("timeZone") String timeZoneStr,
            @QueryParam("weatherStationId") Integer weatherStationId,
            @QueryParam("normalDataWeatherStationId") Integer normalDataWeatherStationId,
            @QueryParam("wateringAffectsNormalData") String wateringAffectsNormalData,
            @QueryParam("firstHarvest") String firstHarvestStr,
            @QueryParam("secondHarvest") String secondHarvestStr,
            @QueryParam("soilType") Integer soilType,
            @QueryParam("cloverShare") Integer cloverShare,
            @QueryParam("userWeatherScenarios") String useWeatherScenarios,
            @QueryParam("scenarioMeanTemp") Double scenarioMeanTemp,
            @QueryParam("scenarioPrecipitation") Double scenarioPrecipitation,
            @QueryParam("scenarioRadiation") Double scenarioRadiation,
            @QueryParam("optimizationInfo") List<String> optimizationInfo,
            @QueryParam("watering") List<String> waterings
    )
    {
        
        ParseRESTParamUtil parseUtil = new ParseRESTParamUtil();
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneStr);
        Date firstHarvest = parseUtil.parseISODate(firstHarvestStr, timeZone);
        Date secondHarvest = secondHarvestStr != null ? parseUtil.parseISODate(secondHarvestStr, timeZone) : null;
        // Calculating start and end date of weather data retrieval
        // Start is April 1st
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(firstHarvest);
        cal.set(Calendar.MONTH, Calendar.APRIL);
        cal.set(Calendar.DATE, 1);

        WeatherUtil wUtil = new WeatherUtil();
        Date aprilFirst = wUtil.normalizeToExactDate(cal.getTime(), timeZone);

        // End date for weather data depends on season
        // We try September 30th. If that's in the future,
        // We add 10 days to today
        Date dateOfLastWeatherData;
        cal.setTime(aprilFirst);
        cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
        cal.set(Calendar.DATE, 30);
        Date endOfSeptember = cal.getTime();
        if(endOfSeptember.after(SystemTime.getSystemTime()))
        {
            cal.setTime(SystemTime.getSystemTime());
            cal.add(Calendar.DATE, 10);
            dateOfLastWeatherData = cal.getTime();
        }
        else
        {
            dateOfLastWeatherData = endOfSeptember;
        }
        
        PointOfInterestWeatherStation weatherStation = em.find(PointOfInterestWeatherStation.class, weatherStationId);
        WeatherDataSourceUtil wsdUtil = new WeatherDataSourceUtil();
        
        // Getting the weather observations
        List<WeatherObservation> observations;
        try {
             observations = wsdUtil.getWeatherObservations(
                    weatherStation,
                    WeatherObservation.LOG_INTERVAL_ID_1D,
                    new String[]{
                        WeatherElements.TEMPERATURE_MEAN,
                        WeatherElements.PRECIPITATION,
                        WeatherElements.GLOBAL_RADIATION,
                        WeatherElements.SOIL_TEMPERATURE_10CM_MEAN,
                        WeatherElements.POTENTIAL_EVAPORATION
                    },
                    aprilFirst, 
                    dateOfLastWeatherData
            );
        } catch (WeatherDataSourceException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
        
        // If we're still in the season, add forecasts
        if(dateOfLastWeatherData.before(endOfSeptember) )
        {
            // We must do some special adjustmens to get weather forecasts from 3-9 days in 
            // the future
            // For EPP and Q0 we simply add the last value
            WeatherObservation lastEPP = wUtil.getLastObservations(observations, "EPP", 1).get(0);
            Date lastEPPDate = lastEPP.getTimeMeasured();
            while(lastEPPDate.before(dateOfLastWeatherData))
            {
                cal.setTime(lastEPPDate);
                cal.add(Calendar.DATE, 1);
                WeatherObservation newEPP = new WeatherObservation(lastEPP);
                newEPP.setTimeMeasured(cal.getTime());
                observations.add(newEPP);
                lastEPPDate = newEPP.getTimeMeasured();
            }
            WeatherObservation lastQ0 = wUtil.getLastObservations(observations, "Q0", 1).get(0);
            Date lastQ0Date = lastQ0.getTimeMeasured();
            while(lastQ0Date.before(dateOfLastWeatherData))
            {
                cal.setTime(lastQ0Date);
                cal.add(Calendar.DATE, 1);
                WeatherObservation newQ0 = new WeatherObservation(lastQ0);
                newQ0.setTimeMeasured(cal.getTime());
                observations.add(newQ0);
                lastQ0Date = newQ0.getTimeMeasured();
            }

            // For TM and RR we get long time forecasts from LMT (Norwegian service only)
            try {
                //System.out.println("Last TM dated " + wUtil.getLastObservations(observations, "TM", 1).get(0).getTimeMeasured());
                cal.setTime(wUtil.getLastObservations(observations, "TM", 1).get(0).getTimeMeasured());
                cal.add(Calendar.DATE, 1);
                Date firstLongTermForecastTime = cal.getTime();
                String[] TMel = {"TM"};
                List<WeatherObservation> TMLongTime = wsdUtil.getWeatherObservations(
                        "http://lmt.nibio.no/agrometbase/export/getLongTermForecastVIPSJSONWeatherData.php?weatherStationId=" + weatherStation.getWeatherStationRemoteId(), 
                        1, 
                        TMel, 
                        firstLongTermForecastTime, 
                        dateOfLastWeatherData, 
                        timeZone
                );
                Collections.sort(TMLongTime);
                TMLongTime = wUtil.fillHourlyHolesBruteForce(TMLongTime, WeatherUtil.AGGREGATION_TYPE_AVERAGE, firstLongTermForecastTime, dateOfLastWeatherData);

                /*for(WeatherObservation obs:TMLongTime)
                {
                    if(obs.getElementMeasurementTypeId().equals("TM"))
                    {
                        System.out.println(obs);
                    }
                }*/

                List<WeatherObservation> TMDLongTime = wUtil.getAggregatedDailyValues(TMLongTime, timeZone, 15, WeatherUtil.AGGREGATION_TYPE_AVERAGE);
                observations.addAll(TMDLongTime);



                String[] RRel = {"RR"};
                List<WeatherObservation> RRLongTime = wsdUtil.getWeatherObservations(
                        "http://lmt.nibio.no/agrometbase/export/getLongTermForecastVIPSJSONWeatherData.php?weatherStationId=" + weatherStation.getWeatherStationRemoteId(), 
                        1, 
                        RRel, 
                        firstLongTermForecastTime, 
                        dateOfLastWeatherData, 
                        timeZone
                );

                RRLongTime = wUtil.fillHourlyHolesBruteForce(RRLongTime, WeatherUtil.AGGREGATION_TYPE_SUM, firstLongTermForecastTime, dateOfLastWeatherData);

                List<WeatherObservation> RRDLongTime = wUtil.getAggregatedDailyValues(RRLongTime, timeZone, 15, WeatherUtil.AGGREGATION_TYPE_SUM);
                observations.addAll(RRDLongTime);
            }
            catch(WeatherDataSourceException | InvalidAggregationTypeException | WeatherObservationListException ex)
            {
                ex.printStackTrace();
                // ? DO WHAT ?
            }
        }
        Collections.sort(observations);
         /*for(WeatherObservation obs:observations)
            {
                if(obs.getElementMeasurementTypeId().equals("TM"))
                {
                    System.out.println(obs);
                }
            }
        */
        // Add weather scenarios??
        if(
                useWeatherScenarios != null && useWeatherScenarios.equals("true")
                && scenarioMeanTemp != null && scenarioPrecipitation != null && scenarioRadiation != null
                )
        {
            Double scenarioEvaporation = wUtil.getAverage(
                    wUtil.getObservationValues(
                            wUtil.getLastObservations(observations, "EPP", 5)
                    )
            );
            Map<String, Double> scenarios = new HashMap<>();
            scenarios.put(WeatherElements.POTENTIAL_EVAPORATION, scenarioEvaporation);
            scenarios.put(WeatherElements.TEMPERATURE_MEAN, scenarioMeanTemp);
            scenarios.put(WeatherElements.PRECIPITATION, scenarioPrecipitation);
            scenarios.put(WeatherElements.GLOBAL_RADIATION, scenarioRadiation);
            
            Date scenarioEndDate = secondHarvest != null ? secondHarvest : firstHarvest;
            
            
            WeatherObservation scenarioTpl = new WeatherObservation();
            scenarioTpl.setLogIntervalId(WeatherObservation.LOG_INTERVAL_ID_1D);
            
            
            for(String param:scenarios.keySet())
            {
                Date latestObsDate = wUtil.getLastObservations(observations, param, 1).get(0).getTimeMeasured();
                cal.setTime(latestObsDate);
                cal.add(Calendar.DATE, 1);
                Date currentDate = cal.getTime();
                
                while(currentDate.compareTo(scenarioEndDate) <= 0)
                {
                    scenarioTpl.setTimeMeasured(currentDate);
 
                    WeatherObservation scenarioObs = new WeatherObservation(scenarioTpl);
                    scenarioObs.setElementMeasurementTypeId(param);
                    scenarioObs.setValue(scenarios.get(param));
                    observations.add(scenarioObs);

                    cal.setTime(currentDate);
                    cal.add(Calendar.DATE, 1);
                    currentDate = cal.getTime();
                }
            }

        }
        

        // Add waterings??
        Map<Date,Double> wateringMap = null;
        if(waterings != null && ! waterings.isEmpty())
        {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setTimeZone(timeZone);
            wateringMap = new HashMap<>();
            for(String wateringStr:waterings)
            {
                String[] elems = wateringStr.split(",");
                try
                {
                    Date wateringDate = format.parse(elems[0]);
                    Double wateringAmount = Double.parseDouble(elems[1]);
                    wateringMap.put(wateringDate, wateringAmount);
                }
                catch(ParseException ex)
                {
                    // Skip to next
                }    
            }
            
            // Add waterings to observations
            for(WeatherObservation obs:observations)
            {
                if  (
                        obs.getElementMeasurementTypeId().equals("RR")
                        && wateringMap.containsKey(obs.getTimeMeasured())
                    )
                {
                    //System.out.println("Old: " + obs.getValue() + ", new: " + (obs.getValue() + wateringMap.get(obs.getTimeMeasured())));
                    obs.setValue(obs.getValue() + wateringMap.get(obs.getTimeMeasured()));
                }
            }
        }
        
        ModelConfiguration config = new ModelConfiguration();
        config.setModelId("ROUGHAGENU");
        config.setConfigParameter("observations", observations);
        config.setConfigParameter("timeZone", timeZone.getID());
        config.setConfigParameter("firstHarvest", firstHarvest);
        config.setConfigParameter("soilType", soilType);
        config.setConfigParameter("cloverShare", cloverShare);
        
        // Optional parameters
        if(secondHarvest != null)
        {
            config.setConfigParameter("secondHarvest", secondHarvest);
        }
        
        // Optimizations
        // We just pass the csv lines as-is. Otherwise we'd need to create
        // a common Class for Jackson (de)serialization
        if(optimizationInfo != null && !optimizationInfo.isEmpty())
        {
           config.setConfigParameter("optimizationInfo", optimizationInfo);
        }
        
        // Must get the VIPSCore user id for this organization
        Organization org = em.find(Organization.class, organizationId);
        Integer VIPSCoreUserId = org.getDefaultVipsCoreUserId();
        
        List<Result> results;
        try
        {
             results = SessionControllerGetter.getForecastBean().runForecast(config, VIPSCoreUserId);
        }
        catch(RunModelException ex)
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
        // NORMALDATA
        if(normalDataWeatherStationId != null && normalDataWeatherStationId > -1)
        {
            // Definere normalparametre
            // Hente normaldata
            try {
                    weatherStation = em.find(PointOfInterestWeatherStation.class, normalDataWeatherStationId);
                    observations = wsdUtil.getWeatherObservations(
                        weatherStation,
                        WeatherObservation.LOG_INTERVAL_ID_1D,
                        new String[]{
                            WeatherElements.getNormalDataParameter(WeatherElements.TEMPERATURE_MEAN),
                            WeatherElements.getNormalDataParameter(WeatherElements.PRECIPITATION),
                            WeatherElements.getNormalDataParameter(WeatherElements.GLOBAL_RADIATION),
                            WeatherElements.getNormalDataParameter(WeatherElements.SOIL_TEMPERATURE_10CM_MEAN),
                            WeatherElements.getNormalDataParameter(WeatherElements.POTENTIAL_EVAPORATION)
                        },
                        aprilFirst, 
                        dateOfLastWeatherData
                    );
                    
                    // Add waterings to normal data precipitation?
                    if(
                            wateringAffectsNormalData != null && wateringAffectsNormalData.equals("true")
                            && wateringMap != null
                    )
                    {
                        for(WeatherObservation obs:observations)
                        {
                            if(
                                    obs.getElementMeasurementTypeId().equals("RR")
                                    && wateringMap.containsKey(obs.getTimeMeasured())
                            )
                            {
                                //System.out.println("Old: " + obs.getValue() + ", new: " + (obs.getValue() + wateringMap.get(obs.getTimeMeasured())));
                                obs.setValue(obs.getValue() + wateringMap.get(obs.getTimeMeasured()));
                            }
                        }
                    }
                    /*System.out.println("Antall normaldata: " + observations.size());
                    for(WeatherObservation obs:observations)
                    {
                        System.out.println(obs.toString());
                    }*/
            } catch (WeatherDataSourceException ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
            }
            // Eventuelt korrigere for vanning
            // Kjør modell en gang til med disse observasjonene
            config.setConfigParameter("observations", observations);
            List<Result> normalDataResults;
            try
            {
                 normalDataResults = SessionControllerGetter.getForecastBean().runForecast(config, VIPSCoreUserId);
            }
            catch(RunModelException ex)
            {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
            }
            if(results == null)
            {
                results = normalDataResults;
            }
            else
            {
                // MERGE RESULTS
                String modelNS = "ROUGHAGENU";
                Map<Date, Result> resultBucket = new HashMap<>();
                for(Result result:results)
                {
                    resultBucket.put(result.getResultValidTime(), result);
                }
                for(Result normalDataResult:normalDataResults)
                {
                    Result result = resultBucket.get(normalDataResult.getResultValidTime());
                    if(result != null)
                    {
                        String[] resultKeys = {"Avling", "Beregnet MSC","NDF","INDF","R&aring;protein","FEm"};
                        for(String resultKey : resultKeys) 
                        {
                            result.setValue(modelNS, resultKey + " (Normalverdi)", normalDataResult.getValue(modelNS, resultKey));
                        }
                    }
                } 
            }
        }
        
        return Response.ok().entity(results).build();
    }
    
    @GET
    @Path("grassdrying/runmodel/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    public Response runGrassDryingModel(
            @PathParam("organizationId") Integer organizationId,
            @QueryParam("timeZone") String timeZoneStr,
            @QueryParam("weatherStationId") Integer weatherStationId,
            @QueryParam("timeOfHarvest") String timeOfHarvestStr,
            @QueryParam("seasonHarvestNumber") Integer seasonHarvestNumber,
            @QueryParam("stringWidth") Double stringWidth,
            @QueryParam("harvestWidth") Double harvestWidth,
            @QueryParam("conditioning") Integer conditioning,
            @QueryParam("yield") Double yield,
            @QueryParam("initialDryMatterFraction") Double initialDryMatterFraction,
            @QueryParam("cloverFraction") Integer cloverFraction,
            @QueryParam("precipitationLastDayBeforeHarvest") Double precipitationLastDayBeforeHarvest
    )
    {
        Calendar cal = javax.xml.bind.DatatypeConverter.parseDateTime(timeOfHarvestStr);
        Date timeOfHarvest = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY, 48);
        Date endTimeGrassDryingModel = cal.getTime();
        
        ModelConfiguration modelConfig = new ModelConfiguration();
        modelConfig.setModelId("GRASSDRYMO");
        modelConfig.setConfigParameter("timeZone", timeZoneStr);
        modelConfig.setConfigParameter("stringWidth", stringWidth);
        modelConfig.setConfigParameter("harvestWidth", harvestWidth);
        modelConfig.setConfigParameter("yield", yield);
        modelConfig.setConfigParameter("conditioning", conditioning);
        if(initialDryMatterFraction != null)
        {
            modelConfig.setConfigParameter("initialDryMatterFraction", initialDryMatterFraction);
        }
        modelConfig.setConfigParameter("seasonHarvestNumber", seasonHarvestNumber);
        modelConfig.setConfigParameter("cloverFraction", cloverFraction);
        modelConfig.setConfigParameter("precipitationLastDayBeforeHarvest", precipitationLastDayBeforeHarvest);
        Map<String,String> loginInfo = new HashMap<>();
        
        PointOfInterestWeatherStation weatherStation = em.find(PointOfInterestWeatherStation.class, weatherStationId);
        WeatherDataSourceUtil wsdUtil = new WeatherDataSourceUtil();
        String[] parameters = {"TM","RR","UM","Q0","FM2"};
        List<Result> results = null;
        try
        {
            List<WeatherObservation> observations = wsdUtil.getWeatherObservations(
                weatherStation, 
                WeatherObservation.LOG_INTERVAL_ID_1H, 
                parameters,
                timeOfHarvest, 
                endTimeGrassDryingModel
            );
            
            modelConfig.setConfigParameter("observations", observations);
            
            // Must get the VIPSCore user id for this organization
            Organization org = em.find(Organization.class, organizationId);
            Integer VIPSCoreUserId = org.getDefaultVipsCoreUserId();
            
            results = SessionControllerGetter.getForecastBean().runForecast(modelConfig, VIPSCoreUserId);
            
        } catch (WeatherDataSourceException | RunModelException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
        
        return Response.ok().entity(results).build();
    }
}
