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

import java.util.Arrays;
import java.util.Calendar;
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
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.WeatherObservationListException;
import no.nibio.vips.util.WeatherUtil;
import no.nibio.vips.util.weather.WeatherDataSourceException;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;

/**
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class PlasmoparaViticolaModelPreprocessor extends ModelRunPreprocessor{

    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException {
        PointOfInterestWeatherStation weatherStation = (PointOfInterestWeatherStation) configuration.getWeatherStationPointOfInterestId();
        // What timezone is the calculation for
        TimeZone timeZone = TimeZone.getTimeZone(weatherStation.getTimeZone());
        // Getting January 1st
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(SystemTime.getSystemTime());
        cal.add(Calendar.DATE, 3);
        WeatherUtil wUtil = new WeatherUtil();
        Date endDate = wUtil.normalizeToExactDate(cal.getTime(), timeZone);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        Date startDate = wUtil.normalizeToExactDate(cal.getTime(), timeZone);
        
        ModelConfiguration retVal = new ModelConfiguration();
        WeatherDataSourceUtil wdsUtil = new WeatherDataSourceUtil();
        List<WeatherObservation> observations;
        try {
            observations = wdsUtil.getWeatherObservations( 
                    weatherStation,
                    WeatherObservation.LOG_INTERVAL_ID_1H,
                    new String[]{
                        WeatherElements.TEMPERATURE_MEAN, 
                        WeatherElements.PRECIPITATION
                    },
                    startDate, 
                    endDate);
        
        
            /*Collections.sort(observations);
            cal.set(2015, Calendar.APRIL, 10, 0, 0, 0);
            Date tenth = cal.getTime();
            cal.set(2015, Calendar.APRIL, 11, 0, 0, 0);
            Date eleventh = cal.getTime();
            for(WeatherObservation obs: observations)
            {
                if(obs.getElementMeasurementTypeId().equals("TM") 
                        && obs.getTimeMeasured().compareTo(tenth) > 0
                        && obs.getTimeMeasured().compareTo(eleventh) < 0
                        )
                {
                    System.out.println(obs.toString());
                }
            }*/

            // Weather data validation/sanitation
      
            observations = wUtil.fixHourlyValuesForParameters(observations, new HashSet(Arrays.asList(WeatherElements.TEMPERATURE_MEAN, WeatherElements.PRECIPITATION)), startDate, null);
        } catch (WeatherDataSourceException | WeatherObservationListException ex) {
            throw new PreprocessorException(this.getClass().getName() + ": Missing weather data. Error message was :\n" + ex.getMessage());
        }

        retVal.setModelId(this.getModelId());
        retVal.setConfigParameter("timeZone", timeZone.getID());
        retVal.setConfigParameter("observations", observations);
            
        return retVal;
    }

    @Override
    public String getModelId() {
        return "PLASMOVITI";
    }

}
