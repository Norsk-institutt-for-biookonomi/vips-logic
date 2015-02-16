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

package no.bioforsk.vips.logic.scheduling.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import no.bioforsk.vips.entity.ModelConfiguration;
import no.bioforsk.vips.entity.WeatherObservation;
import no.bioforsk.vips.logic.entity.ForecastConfiguration;
import no.bioforsk.vips.logic.entity.PointOfInterestWeatherStation;
import no.bioforsk.vips.util.weather.ParseWeatherDataException;
import no.bioforsk.vips.util.weather.WeatherForecastProvider;
import no.bioforsk.vips.util.weather.WeatherStationProviderFactory;
import org.apache.commons.io.IOUtils;


/**
 * All model preprocessors must implement this
 * @copyright 2013-2014 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public abstract class ModelRunPreprocessor {
    
    public abstract ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException;
    
    public abstract String getModelId();
    
    /**
     * Collects weather observations from a data source
     * @param fetchURI Base URI. E.g. http://lmt.bioforsk.no/agrometbase/export/getVIPS3JSONWeatherData.php?weatherStationId=13
     * @param logIntervalId
     * @param elementMeasurementTypes
     * @param startTime
     * @param endTime
     * @param timeZone
     * @return 
     */
    protected List<WeatherObservation> getWeatherObservations(String fetchURI, Integer logIntervalId, String[] elementMeasurementTypes, Date startTime, Date endTime, TimeZone timeZone) throws PreprocessorException
    {
        SimpleDateFormat dateOutput = new SimpleDateFormat("yyyy-MM-dd");
        dateOutput.setTimeZone(timeZone);
        SimpleDateFormat hourOutput = new SimpleDateFormat("H");
        hourOutput.setTimeZone(timeZone);
        
        StringBuilder URL = new StringBuilder(fetchURI)
                .append(fetchURI.contains("?") ? "&" : "?")
                .append("logIntervalId=").append(logIntervalId)
                .append("&timeZone=").append(timeZone.getID())
                .append("&startDate=").append(dateOutput.format(startTime))
                .append("&startTime=").append(hourOutput.format(startTime))
                .append("&endDate=").append(dateOutput.format(endTime))
                .append("&endTime=").append(hourOutput.format(endTime));
        for(String type: elementMeasurementTypes)
        {
                URL.append("&elementMeasurementTypes[]=").append(type);
        }
        //System.out.println("URL = " + URL.toString());
        URLConnection URLConn = null;
        InputStream URLStream = null;
        InputStream error = null;
        String URLOutput = "";
        try
        {
            URL weatherURL = new URL(URL.toString());
            URLConn = weatherURL.openConnection();
            URLStream = URLConn.getInputStream();
            URLOutput = IOUtils.toString(URLStream);
            return new ObjectMapper().readValue(URLOutput, new TypeReference<List<WeatherObservation>>(){});
        }catch(IOException ex)
        {
            // Create error message to logging system
            StringBuilder errorMessage = new StringBuilder()
                            .append("Could not fetch weather observations from URI ")
                            .append(URL.toString()).append(".\n");
            
            // Try to get error message from weather server and append it
            String errorOutput = "";
            try
            {
                error = ((HttpURLConnection) URLConn).getErrorStream();
                errorOutput = IOUtils.toString(error);
            }
            catch(IOException | NullPointerException ex2) {}
            
            if(errorOutput.isEmpty())
            {
                errorMessage.append("There was no output from weather data server to explain this.");
            }
            else
            {
                errorMessage.append("Output from weather data server was: \n").append(errorOutput);
            }
            // Getting the first parts of the error message from code
            errorMessage.append("\nFirst error output from code was: \n")
                    .append(ex.getMessage());
            throw new PreprocessorException(errorMessage.toString());
        }
        finally
        {
            if(URLStream != null)
            {
                IOUtils.closeQuietly(URLStream);
            }
            if(error != null)
            {
                IOUtils.closeQuietly(error);
            }
        }
        
    }
    
    protected List<WeatherObservation> getWeatherObservations(PointOfInterestWeatherStation station, Integer logIntervalId, String[] elementMeasurementTypes, Date startTime, Date endTime) throws PreprocessorException
    {
        List<WeatherObservation> observations = this.getWeatherObservations(station.getDataFetchUri(), logIntervalId, elementMeasurementTypes, startTime, endTime, TimeZone.getTimeZone(station.getTimeZone()));
        Collections.sort(observations);
        Date latestTimeOfMeasuredObservations = observations.isEmpty() ? null : observations.get(observations.size()-1).getTimeMeasured();
        // If the station has a weather forecast provider, add forecasts
        if(station.getWeatherForecastProviderId() != null)
        {
            try
            {
                WeatherForecastProvider forecastProvider = WeatherStationProviderFactory.getWeatherForecastProvider(station.getWeatherForecastProviderId().getWeatherForecastProviderId());
                List<WeatherObservation> forecasts = forecastProvider.getWeatherForecasts(station);
                // Make sure we have the same amount and timing of the different parameters
                // Create a map with one list for each parameter
                Map<String, List<WeatherObservation>> obsMap = new HashMap<>();
                for(String elementMeasurementType : elementMeasurementTypes)
                {
                    obsMap.put(elementMeasurementType, new ArrayList<WeatherObservation>());
                }
                // Populate the lists and see what we've got
                for(WeatherObservation obs:forecasts)
                {
                    // We don't add irrelevant parameters from the forecast
                    if(obs.getLogIntervalId().equals(logIntervalId) && obsMap.get(obs.getElementMeasurementTypeId()) != null)
                    {
                        obsMap.get(obs.getElementMeasurementTypeId()).add(obs);
                    }
                }
                
                // Find the latest date where all parameters are present
                Date latestCommonDate = null;
                for(String elementMeasurementType : elementMeasurementTypes)
                {
                    List<WeatherObservation> paramObs = obsMap.get(elementMeasurementType);
                    Collections.sort(paramObs);
                    latestCommonDate = latestCommonDate == null ?
                            paramObs.get(paramObs.size()-1).getTimeMeasured()
                            : new Date(Math.min(latestCommonDate.getTime(), paramObs.get(paramObs.size()-1).getTimeMeasured().getTime())
                    );
                }
                // We remove all observations after that date
                // And before the measured data ends
                Integer previousObsSize = null;
                for(String elementMeasurementType : elementMeasurementTypes)
                {
                    List<WeatherObservation> paramForecastObs = obsMap.get(elementMeasurementType);
                    List<WeatherObservation> forecastsOutsideScope = new ArrayList<>();
                    for(WeatherObservation obs:paramForecastObs)
                    {
                        if(obs.getTimeMeasured().compareTo(latestCommonDate) > 0
                                || (latestTimeOfMeasuredObservations != null && obs.getTimeMeasured().compareTo(latestTimeOfMeasuredObservations) <= 0)
                        )
                        {
                            forecastsOutsideScope.add(obs);
                        }
                    }
                    paramForecastObs.removeAll(forecastsOutsideScope);
                    // Have we got the same amount of observations for each parameter?
                    if(previousObsSize != null && previousObsSize != paramForecastObs.size())
                    {
                        throw new PreprocessorException("Weather forecast from " 
                                + station.getWeatherForecastProviderId().getName()
                                + " does not provide consistent data. Array length differ "
                                + "between parameters for same time span. First deviation "
                                + "was found for " + elementMeasurementType + ", it has " 
                                + paramForecastObs.size() + " elements, compared to " + previousObsSize
                                + " for the previous parameter. End of the period is " + latestCommonDate
                        );
                    }
                }
                
                // Everything seemingly OK, we add parameters to observation list
                for(String elementMeasurementType : elementMeasurementTypes)
                {
                    observations.addAll(obsMap.get(elementMeasurementType));
                }
                
            }
            catch(ParseWeatherDataException ex)
            {
                throw new PreprocessorException(ex.getMessage());
            }
        }
        return observations;
    }
    
}
