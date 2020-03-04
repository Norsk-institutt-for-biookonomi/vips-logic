/*
 * Copyright (c) 2019 NIBIO <http://www.nibio.no/>. 
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.weather.WeatherDataSourceException;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class RainyDaysModelPreprocessor extends ModelRunPreprocessor {
    
    
    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException {
        PointOfInterestWeatherStation weatherStation = (PointOfInterestWeatherStation) configuration.getWeatherStationPointOfInterestId();
        // What timezone is the calculation for
        TimeZone timeZone = TimeZone.getTimeZone(weatherStation.getTimeZone());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date seasonStart = configuration.getDateStartInTimeZone();
        Date seasonEnd = configuration.getDateEndInTimeZone();
        try
        {
            if(configuration.getForecastModelConfigurationValue(this.getModelId() + "_SEASON_START") != null)
            {
                seasonStart = format.parse(configuration.getForecastModelConfigurationValue(this.getModelId() + "_SEASON_START"));
            }
            if(configuration.getForecastModelConfigurationValue(this.getModelId() + "_SEASON_END") != null)
            {
                seasonEnd = format.parse(configuration.getForecastModelConfigurationValue(this.getModelId() + "_SEASON_END"));
            }
        }
        catch(ParseException ex)
        {
            // Keep calm and carry on?
        }

        
        ModelConfiguration retVal = new ModelConfiguration();
        WeatherDataSourceUtil wdsUtil = new WeatherDataSourceUtil();
        List<WeatherObservation> observations;
        try {
            observations = wdsUtil.getWeatherObservations( 
                    weatherStation,
                    WeatherObservation.LOG_INTERVAL_ID_1D,
                    new String[]{
                        WeatherElements.PRECIPITATION
                    },
                    seasonStart,
                    seasonEnd);
        } catch (WeatherDataSourceException ex) {
            throw new PreprocessorException(ex.getMessage());
        }
        
        // If daily observations are not available, try hourly
        if(observations == null || observations.isEmpty())
        {
            //System.out.println("No daily obs from " + weatherStation.getName() +", trying hourly");
            try {
                observations = wdsUtil.getWeatherObservations(
                        weatherStation,
                        WeatherObservation.LOG_INTERVAL_ID_1H,
                        new String[]{
                            WeatherElements.PRECIPITATION
                        },
                        seasonStart,
                        seasonEnd
                );
                //System.out.println("Got hourly obs from " + weatherStation.getName() +", a total of " + observations.size()  + " items.");
            } catch (WeatherDataSourceException ex) {
                throw new PreprocessorException(ex.getMessage());
            }
        }
        
        // TODO:  weather data validation
        retVal.setModelId(this.getModelId());
        retVal.setConfigParameter("timeZone", timeZone.getID());
        retVal.setConfigParameter("observations", observations);
            
        return retVal;
    }

    @Override
    public String getModelId() {
        return "RAINYDAYSM";
    }

}
