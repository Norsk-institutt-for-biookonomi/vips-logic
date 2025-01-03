/*
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
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
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.ejb.EJB;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.controller.session.ForecastBean;
import no.nibio.vips.logic.controller.session.SessionControllerGetter;
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
 *
 * @author Bhabesh Mukhopadyay
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class AltenariaModelPreprocessor extends ModelRunPreprocessor {
    
    public final   Logger      LOGGER              =   Logger.getLogger(this.getClass().getName());
    private final boolean DEBUG = false;
    
    /**
     * method name : getModelConfiguration
     * @param      :
     * @return     : no.nibio.vips.entity.ModelConfiguration
     */
    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException
    {
        String      ALTERNARIA_SPRAYING_DATES_01    = SessionControllerGetter.getForecastBean().getDeCamelizedFieldName("ALTERNARIA", "sprayingDate01");
        String      ALTERNARIA_SPRAYING_DATES_02    = SessionControllerGetter.getForecastBean().getDeCamelizedFieldName("ALTERNARIA", "sprayingDate02");
        String      ALTERNARIA_SPRAYING_DATES_03    = SessionControllerGetter.getForecastBean().getDeCamelizedFieldName("ALTERNARIA", "sprayingDate03");
        String      ALTERNARIA_SPRAYING_DATES_04    = SessionControllerGetter.getForecastBean().getDeCamelizedFieldName("ALTERNARIA", "sprayingDate04");
        
        String      paramConfigValue                = null;
        List<Date>  lstSprayingDates                = new ArrayList(){};
        
        PointOfInterestWeatherStation weatherStation = (PointOfInterestWeatherStation) configuration.getWeatherStationPointOfInterestId();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        // What timezone is the calculation for
        TimeZone timeZone = TimeZone.getTimeZone(weatherStation.getTimeZone());
        format.setTimeZone(timeZone);
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
        WeatherDataSourceUtil wdsUtil = new WeatherDataSourceUtil();
        List<WeatherObservation> observations;
        
        Date    paramSprayingDate01   = null;
        Date    paramSprayingDate02   = null;
        Date    paramSprayingDate03   = null;
        Date    paramSprayingDate04   = null;
        
        try {
            observations = wdsUtil.getWeatherObservations( 
                    weatherStation,
                    //WeatherObservation.LOG_INTERVAL_ID_1D,
                    WeatherObservation.LOG_INTERVAL_ID_1H,
                    new String[]{
                        WeatherElements.TEMPERATURE_MEAN,
                        WeatherElements.PRECIPITATION,
                        WeatherElements.LEAF_WETNESS_DURATION,
                        WeatherElements.RELATIVE_HUMIDITY_MEAN
                    },
                    startDate, 
                    endDate);
        } catch (WeatherDataSourceException ex) {
            throw new PreprocessorException(ex.getMessage());
        }
        
           // If daily observations are not available, try hourly
/*
           if(observations == null || observations.isEmpty() )
        {
            try {
                observations = wdsUtil.getWeatherObservations(
                        weatherStation,
                        WeatherObservation.LOG_INTERVAL_ID_1H,
                        new String[]{
                            WeatherElements.TEMPERATURE_MEAN,
                            WeatherElements.LEAF_WETNESS_DURATION
                        },
                        startDate,
                        endDate);
            } catch (WeatherDataSourceException ex) {
                throw new PreprocessorException(ex.getMessage());
            }
        }
  */   
    
    

    try
    {
                if(
                            null != configuration.getForecastModelConfigurationValue(ALTERNARIA_SPRAYING_DATES_01)
                        &&  ! configuration.getForecastModelConfigurationValue(ALTERNARIA_SPRAYING_DATES_01).trim().equals("")
                   )
                {
                    paramConfigValue    = configuration.getForecastModelConfigurationValue(ALTERNARIA_SPRAYING_DATES_01);
                    paramSprayingDate01 = format.parse(paramConfigValue);
                    lstSprayingDates.add(paramSprayingDate01);
                }
        
                if(
                            null != configuration.getForecastModelConfigurationValue(ALTERNARIA_SPRAYING_DATES_02)
                        &&  ! configuration.getForecastModelConfigurationValue(ALTERNARIA_SPRAYING_DATES_02).trim().equals("")
                  )
                {
                    paramConfigValue    = configuration.getForecastModelConfigurationValue(ALTERNARIA_SPRAYING_DATES_02);
                    paramSprayingDate02 = format.parse(paramConfigValue);
                    lstSprayingDates.add(paramSprayingDate02);
                }

                if(
                            null != configuration.getForecastModelConfigurationValue(ALTERNARIA_SPRAYING_DATES_03)
                        &&  ! configuration.getForecastModelConfigurationValue(ALTERNARIA_SPRAYING_DATES_03).trim().equals("")
                  )
                {
                    paramConfigValue    = configuration.getForecastModelConfigurationValue(ALTERNARIA_SPRAYING_DATES_03);
                    paramSprayingDate03 = format.parse(paramConfigValue);
                    lstSprayingDates.add(paramSprayingDate03);
                }

                if(
                            null != configuration.getForecastModelConfigurationValue(ALTERNARIA_SPRAYING_DATES_04)
                        &&  ! configuration.getForecastModelConfigurationValue(ALTERNARIA_SPRAYING_DATES_04).trim().equals("")
                  )
                {
                    paramConfigValue    = configuration.getForecastModelConfigurationValue(ALTERNARIA_SPRAYING_DATES_04);
                    paramSprayingDate04 = format.parse(paramConfigValue);
                    lstSprayingDates.add(paramSprayingDate04);
                }                
                
                
                
    }
    catch(ParseException ex)
    {
       throw new PreprocessorException("Could not parse date for Spraying Date for Alternaria. Given string was: "+ paramConfigValue);
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
                    startDate,
                    endDate
            );
            observations.addAll(calcObs);
        } catch (WeatherDataSourceException ex) {
            // We do nothing
        }


        if(DEBUG)
        {
            LOGGER.log(Level.CONFIG, "Finished getting weather data at  "+new Date().toString());
        }
        try {
            observations = validateAndSanitizeObservations(observations, startDate);
        } catch (ConfigValidationException | WeatherObservationListException ex) {
            //ex.printStackTrace();
            throw new PreprocessorException(ex.getMessage());
        } 
        if(DEBUG)
        {        
            LOGGER.log(Level.CONFIG, "Observations=" + observations.toString());
        }

        
        
        
        // TODO:  weather data validation
        retVal.setModelId(this.getModelId());
        retVal.setConfigParameter("timeZone", timeZone.getID());
        retVal.setConfigParameter("observations", observations);
        
        //retVal.setConfigParameter("sprayingDates", format.format(paramSprayingDate));
        
        retVal.setConfigParameter("sprayingDates", lstSprayingDates);
        
        return retVal; 
        
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
        if(DEBUG)
        {
            LOGGER.log(Level.CONFIG, "validateAndSanitizeObservations");
        }
        
        WeatherUtil wUtil = new WeatherUtil();
        
        // First we remove all duplicates
        observations = wUtil.removeDuplicateWeatherObservations(observations, null);
        
        // Fix weather data
        List<WeatherObservation> fixedObservations = wUtil.fixHourlyValuesForParameters(
                observations, 
                new HashSet(Arrays.asList("TM","RR")), 
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
        
        // Holes in series
        if(DEBUG)
        {
            LOGGER.log(Level.CONFIG, "checkForAndFixHourlyTimeSeriesHoles");
        }

        
        
        // Unequal length of lists
        if  (        
                RR.size() != TM.size()
                || BT.size() != TM.size()
                || RR.size() != TM.size()
            )
        {
            UM = wUtil.fixHourlyValuesForParameters(
                    UM, 
                    new HashSet(Arrays.asList("UM")), 
                    firstTimeStamp, 
                    null
            );
            
            
            
            // Fallback if missing leaf wetness: If we have relative humidity. leaf wetness may be calculated
            if((BT.size() != TM.size()) && (UM.size() == TM.size()))
            {
                BT = wUtil.calculateLeafWetnessHourSeriesBestEffort(BT,TM, RR, Q0, FM2, UM);
                
                if(BT.size() != TM.size())
                {
                   throw new ConfigValidationException("Missing leaf wetness data. Also, attempting to calculate leaf wetness from other weather parameters failed.");
                }
            }
            else
            {
                LOGGER.log(Level.WARNING, "TM starts " + TM.get(0).getTimeMeasured() + ", ends " + TM.get(TM.size()-1).getTimeMeasured());
                throw new ConfigValidationException("Incorrect number of weather data. TM.size() = " + TM.size() + ", BT.size()=" + BT.size() +  ", RR.size()=" + RR.size() + ", UM.size()=" + UM.size() );
            }
        }
        List<WeatherObservation> retVal = new ArrayList<>();
        retVal.addAll(TM);
        retVal.addAll(RR);
        retVal.addAll(BT);
        return retVal;
    }

    
    
    /**
    * method name : getModelId
    * @param      :
    * @return     : java.lang.String
    *
    * purpose     :
    *
    * date        : Expression date is undefined on line 20, column 19 in Templates/Classes/Code/GeneratedMethodBody. Expression time is undefined on line 20, column 27 in Templates/Classes/Code/GeneratedMethodBody.
    */
    @Override
    public String getModelId()
    {

        return "ALTERNARIA";
    }

}
