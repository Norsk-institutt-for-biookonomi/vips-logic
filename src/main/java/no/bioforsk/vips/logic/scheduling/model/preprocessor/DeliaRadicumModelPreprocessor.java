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

package no.bioforsk.vips.logic.scheduling.model.preprocessor;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import no.bioforsk.vips.entity.ModelConfiguration;
import no.bioforsk.vips.entity.WeatherObservation;
import no.bioforsk.vips.logic.entity.ForecastConfiguration;
import no.bioforsk.vips.logic.entity.PointOfInterestWeatherStation;
import no.bioforsk.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.bioforsk.vips.logic.scheduling.model.PreprocessorException;
import no.bioforsk.vips.logic.util.SystemTime;
import no.bioforsk.vips.util.WeatherElements;
import no.bioforsk.vips.util.WeatherUtil;

/**
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class DeliaRadicumModelPreprocessor extends ModelRunPreprocessor {

    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException {
        PointOfInterestWeatherStation weatherStation = (PointOfInterestWeatherStation) configuration.getWeatherStationPointOfInterestId();
        // What timezone is the calculation for
        TimeZone timeZone = TimeZone.getTimeZone(weatherStation.getTimeZone());
        // Getting date three days after "today"
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(SystemTime.getSystemTime());
        cal.add(Calendar.DATE, 3);
        WeatherUtil wUtil = new WeatherUtil();
        Date endDate = wUtil.normalizeToExactDate(cal.getTime(), timeZone);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        Date startDate = wUtil.normalizeToExactDate(configuration.getDateStart(), timeZone);
        
        ModelConfiguration retVal = new ModelConfiguration();
        List<WeatherObservation> observations = getWeatherObservations(
                    weatherStation, 
                    WeatherObservation.LOG_INTERVAL_ID_1D, 
                    new String[]{
                        WeatherElements.TEMPERATURE_MEAN, 
                        WeatherElements.SOIL_TEMPERATURE_10CM_MEAN
                    }, 
                    startDate, 
                    endDate);
        
        // If daily observations are not available, try hourly
        if(observations == null || observations.isEmpty())
        {
            observations = getWeatherObservations(
                    weatherStation, 
                    WeatherObservation.LOG_INTERVAL_ID_1H, 
                    new String[]{
                        WeatherElements.TEMPERATURE_MEAN, 
                        WeatherElements.SOIL_TEMPERATURE_10CM_MEAN
                    }, 
                    startDate, 
                    endDate);
        }
        
        // TODO:  weather data validation
        retVal.setModelId(this.getModelId());
        retVal.setConfigParameter("timeZone", timeZone.getID());
        retVal.setConfigParameter("observations", observations);
            
        return retVal;
    }

    @Override
    public String getModelId() {
        return "DELIARADIC";
    }

}
