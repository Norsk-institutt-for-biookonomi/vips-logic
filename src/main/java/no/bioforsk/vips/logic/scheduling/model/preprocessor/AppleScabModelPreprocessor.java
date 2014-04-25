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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import no.bioforsk.vips.model.ConfigValidationException;
import no.bioforsk.vips.util.WeatherElements;
import no.bioforsk.vips.util.WeatherObservationListException;
import no.bioforsk.vips.util.WeatherUtil;

/**
 * Gathers necessary data to run the Apple Scab Model (APPLESCABM).
 * Also responsible for providing forecast configuration form with the correct input parameters
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class AppleScabModelPreprocessor extends ModelRunPreprocessor{
    
    private final boolean DEBUG = false;
    public final static String APPLESCABM_START_DATE_ASCOSPORE_MATURITY = "APPLESCABM_START_DATE_ASCOSPORE_MATURITY";

    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException
    {
        if(DEBUG)
        {
            System.out.println("getModelConfiguration");
        }
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
        if(DEBUG)
        {
            System.out.println("Getting weather data at " + new Date().toString());
        }
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
        
        if(DEBUG)
        {
            System.out.println("Finished getting weather data at " + new Date().toString());
        }
        try {
            observations = validateAndSanitizeObservations(observations);
        } catch (ConfigValidationException | WeatherObservationListException ex) {
            ex.printStackTrace();
            throw new PreprocessorException(ex.getMessage());
        } 
        if(DEBUG)
        {        
            System.out.println("Observations=" + observations.toString());
        }

        
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

    /**
     * Returns list of TM, RR and BT (possibly calculated, either in whole or partially)
     * Should be complete series. 
     * @param observations
     * @return
     * @throws ConfigValidationException
     * @throws WeatherObservationListException 
     */
    private List<WeatherObservation> validateAndSanitizeObservations(List<WeatherObservation> observations) throws ConfigValidationException, WeatherObservationListException {
        if(DEBUG)
        {
            System.out.println("validateAndSanitizeObservations");
        }
        // Now we need to validate and possibly try to fix the weather data
        List<WeatherObservation> TM = new ArrayList<>();
        List<WeatherObservation> RR = new ArrayList<>();
        List<WeatherObservation> BT = new ArrayList<>();
        List<WeatherObservation> UM = new ArrayList<>();
        List<WeatherObservation> Q0 = new ArrayList<>();
        List<WeatherObservation> FM2 = new ArrayList<>();
        for(WeatherObservation o:observations)
        {
            switch(o.getElementMeasurementTypeId())
            {
                case WeatherElements.TEMPERATURE_MEAN:
                    TM.add(o); 
                    break;
                case WeatherElements.PRECIPITATION:
                    RR.add(o); 
                    break;
                case WeatherElements.LEAF_WETNESS:
                    BT.add(o); 
                    break;
                case WeatherElements.RELATIVE_HUMIDITY:
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
        WeatherUtil wUtil = new WeatherUtil();
        // Problems with weather observations
        
        // Holes in series
        if(DEBUG)
        {
            System.out.println("checkForAndFixHourlyTimeSeriesHoles");
            //System.out.println(wUtil.dumpWeatherObservationList(RR));
        }
        TM = wUtil.checkForAndFixHourlyTimeSeriesHoles(TM);
        RR = wUtil.checkForAndFixHourlyTimeSeriesHoles(RR);
        BT = wUtil.checkForAndFixHourlyTimeSeriesHoles(BT);
        
        // Unequal length of lists
        if  (        
                RR.size() != TM.size()
                || BT.size() != TM.size()
                || RR.size() != TM.size()
            )
        {
            UM = wUtil.checkForAndFixHourlyTimeSeriesHoles(UM);
            // Fallback if missing leaf wetness: If we have relative humidity. leaf wetness may be calculated
            if(BT.size() != TM.size() && UM.size() == TM.size())
            {
                Q0 = wUtil.checkForAndFixHourlyTimeSeriesHoles(Q0);
                FM2 = wUtil.checkForAndFixHourlyTimeSeriesHoles(FM2);
                BT = wUtil.calculateLeafWetnessHourSeriesBestEffort(BT,TM, RR, Q0, FM2, UM);
                
                if(BT.size() != TM.size())
                {
                   throw new ConfigValidationException("Missing leaf wetness data. Also, attempting to calculate leaf wetness from other weather parameters failed.");
                }
            }
            else
            {
                throw new ConfigValidationException("Incorrect number of weather data. TM.size() = " + TM.size() + ", BT.size()=" + BT.size() +  ", RR.size()=" + RR.size() );
            }
        }
        List<WeatherObservation> retVal = new ArrayList<>();
        retVal.addAll(TM);
        retVal.addAll(RR);
        retVal.addAll(BT);
        return retVal;
    }

    

}
