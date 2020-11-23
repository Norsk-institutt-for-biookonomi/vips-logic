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
package no.nibio.vips.logic.scheduling.model.preprocessor;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.logic.util.Plant;
import no.nibio.vips.logic.util.Plantation;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.model.ConfigValidationException;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.WeatherObservationListException;
import no.nibio.vips.util.WeatherUtil;
import no.nibio.vips.util.weather.WeatherDataSourceException;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;

/**
 *
 * @author wildfly
 */


public class PhenologyModelPreprocessor extends ModelRunPreprocessor{
     public final           Logger      LOGGER                  =   Logger.getLogger(this.getClass().getName());
    private final           boolean     DEBUG                   =   false;
    private final   static  String      MODEL_ID                =   "PHENOLOGYM";
    private final   static  String      SPECIES_NAME            =   "speciesName";
    private final   static  String      TYPE_NAME               =   "typeName";
    private final   static  String      SOWING_DATE             =   "sowingDate";
    
    private final   static  String      PLANTATION_DATA         =   "plantationData";
    private final   static  String      FILE_PLANTATION         =   "/dataset/plantationData.json";
    
    private final   static  String      NODE_SPECIES            =   "species";
    private final   static  String      NODE_SPECIES_LATIN_NAME =   "speciesLatinName";
    private final   static  String      NODE_BASE_TEMP          =   "baseTemp";
    private final   static  String      NODE_LOG_INTERVAL_ID    =   "weatherLogIntervalId";
    private final   static  String      NODE_TYPE_NAMES         =   "typeNames";
    private final   static  String      NODE_TYPE_NAME          =   "typeName";
    private final   static  String      NODE_PHASE_INFO         =   "phaseInfo";
    private final   static  String      NODE_PHASE_NAME         =   "phaseName";
    private final   static  String      NODE_HEAT_REQ           =   "heatReq";
    private final   static  String      NODE_WEATHER_PARAMS     =   "weatherParameters";
    private final   static  String      NODE_DD_ALGORITHM_ID    =   "dayDegreeAlgorithmId";
    
    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException {
        String      PHENOLOGY_SPECIES_NAME                  = SessionControllerGetter.getForecastBean().getDeCamelizedFieldName(MODEL_ID, SPECIES_NAME);  
        String      PHENOLOGY_TYPE_NAME                     = SessionControllerGetter.getForecastBean().getDeCamelizedFieldName(MODEL_ID, TYPE_NAME);
        String      PHENOLOGY_SOWING_DATE                   = SessionControllerGetter.getForecastBean().getDeCamelizedFieldName(MODEL_ID, SOWING_DATE);
        
        String      paramPhenologySpeciesName               = null;          
        String      paramPhenologyTypeName                  = null;          
        Date        paramSowingDate                         = null;
        
        
        String                         paramConfigValue     =   null;
        
        ModelConfiguration              retVal              =   new ModelConfiguration();
        PointOfInterestWeatherStation   weatherStation      =   (PointOfInterestWeatherStation)  configuration.getWeatherStationPointOfInterestId();
        WeatherDataSourceUtil           wdsUtil             =   new WeatherDataSourceUtil();
        WeatherUtil                     wUtil               =   new WeatherUtil();
        TimeZone                        timeZone            =   TimeZone.getTimeZone(weatherStation.getTimeZone());
        SimpleDateFormat                format              =   new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(timeZone);
        // Getting date three days after "today"
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(SystemTime.getSystemTime());
        cal.add(Calendar.DATE, 3);
        Date endDate = wUtil.normalizeToExactDate(configuration.getDateEnd(), timeZone);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        
        Plant         plant  = new Plant();
        
        try
        {
        paramConfigValue    = configuration.getForecastModelConfigurationValue(PHENOLOGY_SOWING_DATE);
        paramSowingDate     = format.parse(paramConfigValue);
        }
        catch(ParseException ex)
        {
            throw new PreprocessorException("Could not parse date for Sowing Date for "+MODEL_ID+". Given string was: "+ paramConfigValue);
        }
        
        Date                            startDate       =   wUtil.normalizeToExactDate(
                                                                        paramSowingDate
                                                                    ,   timeZone
                                                            );
        
        List<WeatherObservation>        observations    =   null;
        
        paramPhenologySpeciesName   = configuration.getForecastModelConfigurationValue(PHENOLOGY_SPECIES_NAME);
        paramPhenologyTypeName      = configuration.getForecastModelConfigurationValue(PHENOLOGY_TYPE_NAME);
        Plant   plantDBData   =   getConfigurationPlantation(FILE_PLANTATION,paramPhenologySpeciesName,paramPhenologyTypeName);
        
        try {
            observations    = wdsUtil.getWeatherObservations(
                                                              weatherStation
                                                            , WeatherObservation.LOG_INTERVAL_ID_1H
                                                            , plantDBData.getWeatherParams()
                                                            , startDate
                                                            , endDate);
        }
        catch(WeatherDataSourceException ex)
        {
            throw new PreprocessorException(ex.getMessage());
        }
 
        Collections.sort(observations);

        
       if(DEBUG)
        {
            LOGGER.log(Level.CONFIG, "Finished getting weather data at  "+new Date().toString());
        }
        try {
            observations = validateAndSanitizeObservations(observations,plantDBData.getWeatherParams(), startDate);
        } catch (ConfigValidationException | WeatherObservationListException ex) {
            //ex.printStackTrace();
            throw new PreprocessorException(ex.getMessage());
        } 
        if(DEBUG)
        {        
            LOGGER.log(Level.CONFIG, "Observations=" + observations.toString());
        }
    
        

        if(!plantDBData.getPlantations().isEmpty())
        {
            plant = plantDBData;
            plant.setStartDate(paramSowingDate);
        }

        retVal.setModelId(this.getModelId());
        retVal.setConfigParameter("timeZone", timeZone.getID());
        retVal.setConfigParameter("observations", observations);
        retVal.setConfigParameter(PLANTATION_DATA, plant);
        
        return retVal;
    }

    
    
    
    @Override
    public String getModelId() {
        return MODEL_ID;
    }
    
    
       private List<WeatherObservation> validateAndSanitizeObservations(List<WeatherObservation> observations,String[] paramsWeather, Date firstTimeStamp) throws ConfigValidationException, WeatherObservationListException 
       {
       
         if(DEBUG)
        {
            LOGGER.log(Level.CONFIG, "validateAndSanitizeObservations");
        }
        
        WeatherUtil wUtil = new WeatherUtil();
        
        // First we remove all duplicates
        observations = wUtil.removeDuplicateWeatherObservations(observations, null);
        
        // Fix weather data
        List<WeatherObservation> fixedObservations = wUtil.fixHourlyValuesForParameters(observations, 
                new HashSet(Arrays.asList(paramsWeather)), 
                firstTimeStamp, 
                null
        );
        
        
        // Now we need to validate and possibly try to fix the weather data
        List<WeatherObservation> TM = new ArrayList<>();
        List<WeatherObservation> TN = new ArrayList<>();
        List<WeatherObservation> TX = new ArrayList<>();
        
        for(WeatherObservation o:fixedObservations)
        {
            switch(o.getElementMeasurementTypeId())
            {
                case WeatherElements.TEMPERATURE_MEAN:
                    TM.add(o); 
                    break;
                case WeatherElements.TEMPERATURE_MINIMUM:
                    TN.add(o); 
                    break;
                case WeatherElements.TEMPERATURE_MAXIMUM:
                    TX.add(o); 
                    break;                    
                    
            }
        }
        

        List<WeatherObservation> retVal = new ArrayList<>();
        if(!TM.isEmpty()) retVal.addAll(TM);
        if(!TN.isEmpty()) retVal.addAll(TN);
        if(!TX.isEmpty()) retVal.addAll(TX);
       Collections.sort(retVal);
        return retVal;
    }
    
    private Plant getConfigurationPlantation(String fileName,String strParamSpeciesName, String strParamTypeName)
    {
        Plant   plant   = new Plant();
               
        ObjectMapper        mapper          = new ObjectMapper();
        List<Plantation>    plantations     = new ArrayList<Plantation>();
        try {
            BufferedInputStream inputStream = new BufferedInputStream(this.getClass().getResourceAsStream(fileName));
            JsonFactory f = new MappingJsonFactory();
            JsonParser jp = f.createParser(inputStream);
            
            JsonNode all = jp.readValueAsTree();
            
            JsonNode nodeAllSpecies = all.path(NODE_SPECIES);
            
            if(nodeAllSpecies.isArray())
            {
                for(JsonNode nodeSpecies : nodeAllSpecies)
                {
                    if(nodeSpecies.get(NODE_SPECIES_LATIN_NAME).asText().trim().equalsIgnoreCase(strParamSpeciesName))
                    {
                        plant.setSpeciesLatinName(nodeSpecies.get(NODE_SPECIES_LATIN_NAME).asText());
                        if(null != nodeSpecies.get(NODE_BASE_TEMP).asText())
                        {
                            plant.setBaseTemp(nodeSpecies.get(NODE_BASE_TEMP).asDouble());
                        }
                        if(null != nodeSpecies.get(NODE_LOG_INTERVAL_ID).asText())
                        {
                            plant.setLogIntervalId(nodeSpecies.get(NODE_LOG_INTERVAL_ID).asInt());
                        }
                        if(null != nodeSpecies.get(NODE_DD_ALGORITHM_ID).asText())
                        {
                            plant.setAlgorithmId(nodeSpecies.get(NODE_DD_ALGORITHM_ID).asInt());
                        }
                       JsonNode nodeSpeciesWeatherParams   = nodeSpecies.path(NODE_WEATHER_PARAMS);
                       if( null != nodeSpeciesWeatherParams && nodeSpeciesWeatherParams.isArray())
                       {
                           String[] strWeatherParams    = new String[nodeSpeciesWeatherParams.size()];
                           int i = 0;
                           for (JsonNode nodeWeather: nodeSpeciesWeatherParams)
                           {
                               strWeatherParams[i++] = mapper.convertValue(nodeWeather,  new TypeReference<String>(){});
                           }
                           plant.setWeatherParams(strWeatherParams);
                       }
                       
                       JsonNode nodeTypeNames       = nodeSpecies.path(NODE_TYPE_NAMES); 
                       
                        if(nodeTypeNames.isArray())
                        {
                             for(JsonNode nodeTypeName : nodeTypeNames)
                             {
                                 if(nodeTypeName.get(NODE_TYPE_NAME).asText().trim().equalsIgnoreCase(strParamTypeName))
                                 {
                                    plant.setTypeName(strParamTypeName);
                                    if(null != nodeTypeName.get(NODE_LOG_INTERVAL_ID).asText())
                                    {
                                        plant.setLogIntervalId(nodeTypeName.get(NODE_LOG_INTERVAL_ID).asInt());
                                    }
                                    if(null != nodeTypeName.get(NODE_DD_ALGORITHM_ID).asText())
                                    {
                                        plant.setAlgorithmId(nodeTypeName.get(NODE_DD_ALGORITHM_ID).asInt());
                                    }                                    
                                    if(null != nodeTypeName.get(NODE_BASE_TEMP).asText())
                                    {
                                        plant.setBaseTemp(nodeTypeName.get(NODE_BASE_TEMP).asDouble());
                                    }

                                    JsonNode nodeTypeNameWeatherParams   = nodeTypeName.path(NODE_WEATHER_PARAMS);
                                    if( null != nodeTypeNameWeatherParams && nodeTypeNameWeatherParams.isArray())
                                        {
                                            String[] strWeatherParams    = new String[nodeTypeNameWeatherParams.size()];
                                            int i = 0;
                                            for (JsonNode nodeWeather: nodeTypeNameWeatherParams)
                                            {
                                                strWeatherParams[i++] = mapper.convertValue(nodeWeather,  new TypeReference<String>(){});
                                            }
                                            plant.setWeatherParams(strWeatherParams);
                                        }
                                    
                                    JsonNode nodeAllPhaseInfo = nodeTypeName.path(NODE_PHASE_INFO);
                                    if(nodeAllPhaseInfo.isArray())
                                    {
                                         for(JsonNode nodePhaseInfo : nodeAllPhaseInfo)
                                         {
                                            Plantation plantation  = new Plantation();
                                            plantation.setTypeName(nodeTypeName.get(NODE_TYPE_NAME).asText());


                                             plantation.setPhaseName(nodePhaseInfo.get(NODE_PHASE_NAME).asText());
                                             plantation.setHeatReq(nodePhaseInfo.get(NODE_HEAT_REQ).asDouble());

                                             plantations.add(plantation);
                                         }

                                    }
                                 } 

                             }

                        }
                       
                       
                    }
                }

            }
     
            plant.setPlantations(plantations);
            
            
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        
        return plant;
    }
       
}
