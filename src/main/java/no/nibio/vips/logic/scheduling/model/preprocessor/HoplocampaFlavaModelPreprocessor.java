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

package no.nibio.vips.logic.scheduling.model.preprocessor;

import java.util.ArrayList;
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
public class HoplocampaFlavaModelPreprocessor extends ModelRunPreprocessor {

    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException {
        PointOfInterestWeatherStation weatherStation = (PointOfInterestWeatherStation) configuration.getWeatherStationPointOfInterestId();
        // What timezone is the calculation for
        TimeZone timeZone = TimeZone.getTimeZone(weatherStation.getTimeZone());

        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(SystemTime.getSystemTime());
        cal.add(Calendar.DATE, 3);
        WeatherUtil wUtil = new WeatherUtil();
        Date endDate = wUtil.normalizeToExactDate(cal.getTime(), timeZone);
        
        Date startDate = wUtil.normalizeToExactDate(configuration.getDateStart(), timeZone);
        
        ModelConfiguration retVal = new ModelConfiguration();
        
        WeatherDataSourceUtil wdsUtil = new WeatherDataSourceUtil();
        
        // Getting the parameters one by one
        // TM first
        List<WeatherObservation> TM;
        try {
            TM = wdsUtil.getWeatherObservations( 
                    weatherStation,
                    WeatherObservation.LOG_INTERVAL_ID_1D,
                    new String[]{
                        WeatherElements.TEMPERATURE_MEAN
                    },
                    startDate, 
                    endDate);
        
        
        
            // If daily observations are not available, try hourly
            if(TM == null || TM.isEmpty())
            {
                TM = wdsUtil.getWeatherObservations(
                        weatherStation, 
                        WeatherObservation.LOG_INTERVAL_ID_1H, 
                        new String[]{
                            WeatherElements.TEMPERATURE_MEAN
                        }, 
                        startDate, 
                        endDate);
            }
        } catch (WeatherDataSourceException ex) {
            throw new PreprocessorException(ex.getMessage());
        }
        
        // TJM5 (soil temp at 5 cm)
        List<WeatherObservation> TJM5;
        try {
            TJM5 = wdsUtil.getWeatherObservations( 
                    weatherStation,
                    WeatherObservation.LOG_INTERVAL_ID_1D,
                    new String[]{
                        WeatherElements.SOIL_TEMPERATURE_5CM_MEAN
                    },
                    startDate, 
                    endDate);
        
        
            // If daily observations are not available, try hourly
            if(TJM5 == null || TJM5.isEmpty())
            {
                TJM5 = wdsUtil.getWeatherObservations(
                        weatherStation, 
                        WeatherObservation.LOG_INTERVAL_ID_1H, 
                        new String[]{
                            WeatherElements.SOIL_TEMPERATURE_5CM_MEAN
                        }, 
                        startDate, 
                        endDate);
            }
        } catch (WeatherDataSourceException ex) {
            throw new PreprocessorException(ex.getMessage());
        }
        
        
        String weatherdataFixErrorMessage = "";
        List<WeatherObservation> fixedObservations = new ArrayList<>();
        try
        {
            TM = wUtil.fixHourlyValuesForParameters(TM, new HashSet(Arrays.asList(WeatherElements.TEMPERATURE_MEAN)), startDate, null);
            fixedObservations.addAll(TM);
        }
        catch(WeatherObservationListException ex)
        {
            weatherdataFixErrorMessage += ex.getMessage();
        }
        
        try
        {
            TJM5 = wUtil.fixHourlyValuesForParameters(TJM5, new HashSet(Arrays.asList(WeatherElements.SOIL_TEMPERATURE_5CM_MEAN)), startDate, null);
            fixedObservations.addAll(TJM5);
        }
        catch(WeatherObservationListException ex)
        {
            weatherdataFixErrorMessage += ex.getMessage();
        }
        
        if((    TM == null && TJM5 == null) 
                || (TM == null && TJM5.isEmpty())
                || (TM.isEmpty() && TJM5 == null)
                )
        {
            throw new PreprocessorException(this.getClass().getName() + ": Missing weather data. Error message was :\n" + weatherdataFixErrorMessage);
        }
        
        
        // TODO:  weather data validation
        //System.out.println("Number of values in fixedObservations: " + fixedObservations.size());
        retVal.setModelId(this.getModelId());
        retVal.setConfigParameter("timeZone", timeZone.getID());
        //retVal.setConfigParameter("observations", fixedObservations);
        retVal.setConfigParameter("observations", fixedObservations);
            
        return retVal;
    }

    @Override
    public String getModelId() {
        return "HOPLOCAMPA";
    }

}
