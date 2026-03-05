/*
 * Copyright (c) 2026 NIBIO <http://www.nibio.no/>. 
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.model.ConfigValidationException;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.WeatherObservationListException;
import no.nibio.vips.util.WeatherUtil;
import no.nibio.vips.util.weather.WeatherDataSourceException;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;

/**
 * Gathers necessary data to run the Mycosphaerella ribis Model (MYCOSRIBIS).
 * Also responsible for providing forecast configuration form with the correct input parameters
 * @copyright 2026 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class MycosphaerellaRibisModelPreprocessor extends ModelRunPreprocessor{
    private static Logger LOGGER = LoggerFactory.getLogger(MycosphaerellaRibisModelPreprocessor.class);
    public final static String MYCOSRIBIS_START_DATE_SPORE_MATURATION = "MYCOSRIBIS_START_DATE_SPORE_MATURATION";

    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException
    {
        LOGGER.debug("MycosphaerellaRibisModelPreprocessor.getModelConfiguration() called");
        //configuration.getDateStart();
        PointOfInterestWeatherStation weatherStation = (PointOfInterestWeatherStation) configuration.getWeatherStationPointOfInterestId();
        // What timezone is the calculation for
        TimeZone timeZone = TimeZone.getTimeZone(weatherStation.getTimeZone());
        //System.out.println("timeZone ID in preprocessor=" + timeZone.getID());
        // When do we start calculation ascospore maturity
        // Normally the date for bud break
        Date startDateSporeMaturation = null;
        // As long as forecast has not ended, try to get weather data 3 days into the future
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(SystemTime.getSystemTime());
        cal.add(Calendar.DATE, 3);
        Date nearFuture = cal.getTime();
        Date dateEndWeatherData = configuration.getDateEndInTimeZone().compareTo(nearFuture) < 0 ? configuration.getDateEndInTimeZone(): nearFuture;
        
        // We need data until hour 23 of this date, need to fix that
        cal.setTime(dateEndWeatherData);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        dateEndWeatherData = cal.getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(timeZone);
        try
        {   
            
            startDateSporeMaturation = format.parse(configuration.getForecastModelConfigurationValue(MYCOSRIBIS_START_DATE_SPORE_MATURATION));
        }catch (ParseException ex)
        {
            throw new PreprocessorException("Could not parse date for spore maturation start date. Given string was: " + configuration.getForecastModelConfigurationValue(MYCOSRIBIS_START_DATE_SPORE_MATURATION));
        }

        
        // Use Jackson to parse JSON from server
        // Weather data collections
        LOGGER.debug("Getting weather data at " + new Date().toString());

        WeatherDataSourceUtil wdsUtil = new WeatherDataSourceUtil();
        List<WeatherObservation> observations;
        try {
            observations = wdsUtil.getWeatherObservations(
                    weatherStation,
                    WeatherObservation.LOG_INTERVAL_ID_1H,
                    new String[]{
                        WeatherElements.TEMPERATURE_MEAN,
                        WeatherElements.PRECIPITATION,
                        WeatherElements.LEAF_WETNESS_DURATION,
                        WeatherElements.RELATIVE_HUMIDITY_MEAN
                    },
                    startDateSporeMaturation,
                    dateEndWeatherData
            );
        } catch (WeatherDataSourceException ex) {
            throw new PreprocessorException(ex.getMessage());
        }
                
        Collections.sort(observations);

        // These weather data are for leaf wetness calculation only. We could
        // possibly do without them, using the simple LW algorithm
        try {
            List<WeatherObservation> calcObs = wdsUtil.getWeatherObservations(
                    weatherStation,
                    WeatherObservation.LOG_INTERVAL_ID_1H,
                    new String[]{
                        WeatherElements.GLOBAL_RADIATION,
                        WeatherElements.WIND_SPEED_2M
                    },
                    startDateSporeMaturation,
                    dateEndWeatherData
            );
            observations.addAll(calcObs);
        } catch (WeatherDataSourceException ex) {
            // We do nothing
        }
        
        LOGGER.debug("Finished getting weather data at " + new Date().toString());

        try {
            observations = validateAndSanitizeObservations(observations, startDateSporeMaturation);
        } catch (ConfigValidationException | WeatherObservationListException ex) {
            //ex.printStackTrace();
            throw new PreprocessorException(ex.getMessage());
        } 

        //Collections.sort(observations);
        LOGGER.debug("Observations=" + observations.toString());
        
        
        // Create the complete model configuration object
        ModelConfiguration retVal = new ModelConfiguration();
        retVal.setModelId(this.getModelId());
        
        retVal.setConfigParameter("startDateSporeMaturation", format.format(startDateSporeMaturation));
        retVal.setConfigParameter("timeZone", timeZone.getID());
        retVal.setConfigParameter("observations", observations);
        return retVal;
    }

    @Override
    public String getModelId() {
        return "MYCOSRIBIS";
    }

    /**
     * Returns list of TM, RR and BT (possibly calculated, either in whole or partially)
     * Should be complete series. 
     * @param observations
     * @return
     * @throws ConfigValidationException
     * @throws WeatherObservationListException 
     */
    private List<WeatherObservation> validateAndSanitizeObservations(List<WeatherObservation> observations, Date firstTimeStamp) throws ConfigValidationException, WeatherObservationListException {

        WeatherUtil wUtil = new WeatherUtil();
        
        // First we remove all duplicates
        observations = wUtil.removeDuplicateWeatherObservations(observations, null);
        
        // Fix weather data
        List<WeatherObservation> fixedObservations = wUtil.fixHourlyValuesForParameters(
                observations, 
                new HashSet<>(Arrays.asList("TM","RR")), 
                firstTimeStamp, 
                null
        );
        
        
        // Now we need to validate and possibly try to fix the weather data
        List<WeatherObservation> TM = new ArrayList<>();
        List<WeatherObservation> RR = new ArrayList<>();
        
        for(WeatherObservation o:fixedObservations)
        {
            switch(o.getElementMeasurementTypeId())
            {
                case WeatherElements.TEMPERATURE_MEAN:
                    TM.add(o); 
                    break;
                case WeatherElements.PRECIPITATION:
                    RR.add(o); 
                    break;
                    
            }
        }
        
        List<WeatherObservation> BT = new ArrayList<>();
        List<WeatherObservation> UM = new ArrayList<>();
        List<WeatherObservation> Q0 = new ArrayList<>();
        List<WeatherObservation> FM2 = new ArrayList<>();
        for(WeatherObservation o:observations)
        {
            switch(o.getElementMeasurementTypeId())
            {
                case WeatherElements.LEAF_WETNESS_DURATION:
                    BT.add(o); 
                    break;
                case WeatherElements.RELATIVE_HUMIDITY_MEAN:
                    UM.add(o); 
                    break;
                case WeatherElements.GLOBAL_RADIATION:
                    Q0.add(o);
                    break;
                case WeatherElements.WIND_SPEED_2M:
                    FM2.add(o);
                    break;
                default:
                    // Let it pass in silence 
                    break;
            }
        }
        
        // Problems with weather observations

        
        // Unequal length of lists
        if  (        
                RR.size() != TM.size()
                || BT.size() != TM.size()
                || RR.size() != TM.size()
            )
        {
            LOGGER.debug("Unequal lists lengt: RR=" + RR.size() + ", TM=" + TM.size() + ", BT=" + BT.size());
            UM = wUtil.fixHourlyValuesForParameters(
                    UM, 
                    new HashSet<>(Arrays.asList("UM")), 
                    firstTimeStamp, 
                    null
            );
            // Fallback if missing leaf wetness: If we have relative humidity. leaf wetness may be calculated
            if(BT.size() != TM.size() && UM.size() == TM.size())
            {
                BT = wUtil.calculateLeafWetnessHourSeriesBestEffort(BT,TM, RR, Q0, FM2, UM);
                
                if(BT.size() != TM.size())
                {
                   throw new ConfigValidationException("Missing leaf wetness data. Also, attempting to calculate leaf wetness from other weather parameters failed.");
                }
            }
            else
            {
                System.out.println("TM starts " + TM.get(0).getTimeMeasured() + ", ends " + TM.get(TM.size()-1).getTimeMeasured());
                System.out.println("UM starts " + UM.get(0).getTimeMeasured() + ", ends " + UM.get(UM.size()-1).getTimeMeasured());
                throw new ConfigValidationException("Incorrect number of weather data. TM.size() = " + TM.size() + ", BT.size()=" + BT.size() +  ", RR.size()=" + RR.size() + ", UM.size()=" + UM.size() );
            }
        }
        List<WeatherObservation> retVal = new ArrayList<>();
        retVal.addAll(TM);
        retVal.addAll(RR);
        retVal.addAll(BT);
        return retVal;
    }

    

}
