/*
 * Copyright (c) 2013 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with VIPSLogic.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package no.bioforsk.vips.logic.scheduling.model.preprocessor;

import no.bioforsk.vips.logic.entity.ForecastConfiguration;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import no.bioforsk.vips.entity.ModelConfiguration;
import no.bioforsk.vips.entity.WeatherObservation;
import no.bioforsk.vips.logic.entity.PointOfInterestWeatherStation;
import no.bioforsk.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.bioforsk.vips.logic.scheduling.model.PreprocessorException;
import no.bioforsk.vips.logic.util.SystemTime;
import no.bioforsk.vips.util.WeatherElements;

/**
 * Gathers necessary data to run the Apple Scab Model (APPLESCABM).
 * Also responsible for providing forecast configuration form with the correct input parameters
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class AppleScabModelPreprocessor extends ModelRunPreprocessor{
    
    public final static String APPLESCABM_START_DATE_ASCOSPORE_MATURITY = "APPLESCABM_START_DATE_ASCOSPORE_MATURITY";

    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException
    {
        //configuration.getDateStart();
        PointOfInterestWeatherStation weatherStation = (PointOfInterestWeatherStation) configuration.getWeatherStationPointOfInterestId();
        // What timezone is the calculation for
        TimeZone timeZone = TimeZone.getTimeZone(weatherStation.getTimeZone());
        //System.out.println("timeZone ID in preprocessor=" + timeZone.getID());
        // When do we start calculation ascospore maturity
        // Normally the date for "green tip"
        Date startDateAscosporeMaturity = null;
        // As long as forecast has not ended, try to get weather data 3 days into the future
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(SystemTime.getSystemTime());
        cal.add(Calendar.DATE, 3);
        Date nearFuture = cal.getTime();
        Date dateEndWeatherData = configuration.getDateEnd().compareTo(nearFuture) < 0 ? configuration.getDateEnd() : nearFuture;
        
        // We need data until hour 23 of this date, need to fix that
        cal.setTime(dateEndWeatherData);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        dateEndWeatherData = cal.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(timeZone);
        try
        {   
            
            startDateAscosporeMaturity = format.parse(configuration.getForecastModelConfigurationValue(APPLESCABM_START_DATE_ASCOSPORE_MATURITY));
        }catch (ParseException ex)
        {
            throw new PreprocessorException("Could not parse date for ascospore maturation date. Given string was: " + configuration.getForecastModelConfigurationValue(APPLESCABM_START_DATE_ASCOSPORE_MATURITY));
        }

        
        // Use Jackson to parse JSON from server
        // Weather data collections
        List<WeatherObservation> observations = getWeatherObservations(
                weatherStation.getDataFetchUri(), 
                WeatherObservation.LOG_INTERVAL_ID_1H, 
                new String[]{
                    WeatherElements.TEMPERATURE_MEAN, 
                    WeatherElements.PRECIPITATION,
                    WeatherElements.LEAF_WETNESS,
                    WeatherElements.RELATIVE_HUMIDITY,
                    WeatherElements.GLOBAL_RADIATION,
                    WeatherElements.WIND_SPEED_2M
                }, 
                startDateAscosporeMaturity, 
                dateEndWeatherData, 
                timeZone
        );
        
        //System.out.println("Observations=" + observations.toString());
        
        // Create the complete model configuration object
        ModelConfiguration retVal = new ModelConfiguration();
        retVal.setModelId(this.getModelId());
        
        retVal.setConfigParameter("startDateAscosporeMaturity", format.format(startDateAscosporeMaturity));
        retVal.setConfigParameter("timeZone", timeZone.getID());
        retVal.setConfigParameter("observations", observations);
        return retVal;
    }

    @Override
    public String getModelId() {
        return "APPLESCABM";
    }

}
