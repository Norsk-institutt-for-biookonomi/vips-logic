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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.weather.WeatherDataSourceException;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;

/**
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class CydiaPomonellaModelPreprocessor extends ModelRunPreprocessor{
    
    public final static String CYDIAPOMON_OBSERVED_DATE_OF_FIRST_CATCH = "CYDIAPOMON_OBSERVED_DATE_OF_FIRST_CATCH";

    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException {
        PointOfInterestWeatherStation weatherStation = (PointOfInterestWeatherStation) configuration.getWeatherStationPointOfInterestId();
        // What timezone is the calculation for
        TimeZone timeZone = TimeZone.getTimeZone(weatherStation.getTimeZone());
        Date observedDateOfFirstCatch = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try
        {
            observedDateOfFirstCatch = format.parse(configuration.getForecastModelConfigurationValue(CYDIAPOMON_OBSERVED_DATE_OF_FIRST_CATCH));
        }
        catch(ParseException ex)
        {
            throw new PreprocessorException(ex.getMessage());
        }
        
        // This season is either deduced from the date of catch or from current system time
        // TODO: Be able to determine season based on location (e.g. Australia)
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(SystemTime.getSystemTime());
        int systemYear = cal.get(Calendar.YEAR);
        int modelRunYear = systemYear;
        // If we have the observedDateOfFirstCatch, we know which year we're in
        // Otherwise assume we're in the year of current system time
        if(observedDateOfFirstCatch != null)
        {
            cal.setTime(observedDateOfFirstCatch);
            modelRunYear = cal.get(Calendar.YEAR);
        }
        
        // If we're in system year, assume we're in the now situation
        // Else estimate end date for that previous season
       
        cal.setTime(observedDateOfFirstCatch != null ? observedDateOfFirstCatch : SystemTime.getSystemTime());
        
        // Getting 1st of January
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE,1);
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND, 0);
        Date seasonStart = cal.getTime();
        
        // Season end is either date of first catch + 3 months or systemTime + 9 days
        if(modelRunYear == systemYear)
        {
            cal.setTime(SystemTime.getSystemTime());
            cal.add(Calendar.DATE, 9);
        }
        else
        {
            cal.setTime(seasonStart);
            cal.add(Calendar.MONTH, 9);
        }
        Date seasonEnd = cal.getTime();
        
        WeatherDataSourceUtil wdsUtil = new WeatherDataSourceUtil();
        List<WeatherObservation> observations;
        try {
            observations = wdsUtil.getWeatherObservations(
                    weatherStation,
                    WeatherObservation.LOG_INTERVAL_ID_1D,
                    new String[]{
                        WeatherElements.TEMPERATURE_MINIMUM,
                        WeatherElements.TEMPERATURE_MAXIMUM
                    },
                    seasonStart,
                    seasonEnd
            );
            observations.addAll(wdsUtil.getWeatherObservations(
                    weatherStation,
                    WeatherObservation.LOG_INTERVAL_ID_1H,
                    new String[]{
                        WeatherElements.TEMPERATURE_MEAN
                    },
                    seasonStart,
                    seasonEnd)
            );
        } catch (WeatherDataSourceException ex) {
            throw new PreprocessorException(ex.getMessage());
        }
        
        // Create the complete model configuration object
        ModelConfiguration retVal = new ModelConfiguration();
        retVal.setModelId(this.getModelId());
        retVal.setConfigParameter("timeZone", timeZone.getID());
        retVal.setConfigParameter("observations", observations);
        retVal.setConfigParameter("observedDateOfFirstCatch", observedDateOfFirstCatch);
        retVal.setConfigParameter("latitude", configuration.getLocationPointOfInterestId().getLatitude());
        retVal.setConfigParameter("longitude", configuration.getLocationPointOfInterestId().getLongitude());
        retVal.setConfigParameter("altitude", configuration.getLocationPointOfInterestId().getAltitude());
        return retVal;
    }

    @Override
    public String getModelId() {
        return "CYDIAPOMON";
    }

}
