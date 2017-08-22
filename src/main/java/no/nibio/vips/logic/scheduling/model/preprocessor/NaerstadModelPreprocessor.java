/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
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

import java.util.Calendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import no.nibio.vips.model.ConfigValidationException;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.WeatherObservationListException;
import no.nibio.vips.util.WeatherUtil;
import no.nibio.vips.util.weather.WeatherDataSourceException;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;

/**
 * @copyright 2013-2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class NaerstadModelPreprocessor extends ModelRunPreprocessor{
    public final static String NAERSTADMO_CALCULATION_START = "NAERSTADMO_CALCULATION_START";
    public final static String NAERSTADMO_CALCULATION_END = "NAERSTADMO_CALCULATION_END";

    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException
    {
        try {
            PointOfInterestWeatherStation weatherStation = (PointOfInterestWeatherStation) configuration.getWeatherStationPointOfInterestId();
            TimeZone timeZone = TimeZone.getTimeZone(weatherStation.getTimeZone());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
            Date calculationStart = null;
            Date calculationEnd = null;
            if(configuration.getForecastModelConfigurationValue(NAERSTADMO_CALCULATION_START)!=null
                    && ! configuration.getForecastModelConfigurationValue(NAERSTADMO_CALCULATION_START).isEmpty())
            {
                calculationStart = format.parse(configuration.getForecastModelConfigurationValue(NAERSTADMO_CALCULATION_START));
            }
            if(configuration.getForecastModelConfigurationValue(NAERSTADMO_CALCULATION_END) != null
                    && ! configuration.getForecastModelConfigurationValue(NAERSTADMO_CALCULATION_END).isEmpty())
            {
                calculationEnd = format.parse(configuration.getForecastModelConfigurationValue(NAERSTADMO_CALCULATION_END));
            }
            
            // We attempt to get data from near future
            Calendar cal = Calendar.getInstance(timeZone);
            cal.setTime(SystemTime.getSystemTime());
            cal.add(Calendar.DATE, 3);
            Date nearFuture = cal.getTime();
            Date dateEndWeatherData = configuration.getDateEndInTimeZone().compareTo(nearFuture) < 0 ? configuration.getDateEndInTimeZone(): nearFuture;
            WeatherDataSourceUtil wdsUtil = new WeatherDataSourceUtil();
            
            List<WeatherObservation> observations;
            
            try {
                observations = wdsUtil.getWeatherObservations(
                        weatherStation,
                        WeatherObservation.LOG_INTERVAL_ID_1H,
                        new String[]{
                            WeatherElements.TEMPERATURE_MEAN,
                            WeatherElements.PRECIPITATION,
                            WeatherElements.LEAF_WETNESS,
                            WeatherElements.RELATIVE_HUMIDITY_MEAN,
                            WeatherElements.GLOBAL_RADIATION,
                            WeatherElements.WIND_SPEED_2M
                        },
                        configuration.getDateStartInTimeZone(),
                        dateEndWeatherData);
                observations = validateAndSanitizeObservations(observations, configuration.getDateStartInTimeZone());    
                //System.out.println(configuration.getDateStartInTimeZone());
            } catch (ConfigValidationException | WeatherObservationListException | WeatherDataSourceException ex) {
                ex.printStackTrace();
                throw new PreprocessorException(ex.getMessage());
            } 
            
            ModelConfiguration retVal = new ModelConfiguration();
            retVal.setModelId(this.getModelId());
            if(calculationStart != null)
            {
                retVal.setConfigParameter("calculationStart", calculationStart);
            }
            if(calculationEnd != null)
            {
                retVal.setConfigParameter("calculationEnd", calculationEnd);
            }
            retVal.setConfigParameter("observations", observations);
            
            return retVal;
        } catch (ParseException ex) 
        {
            Logger.getLogger(NaerstadModelPreprocessor.class.getName()).log(Level.SEVERE, null, ex);
            throw new PreprocessorException("Could not parse date. " + ex.getMessage());
        }
    }

    @Override
    public String getModelId() {
        return "NAERSTADMO";
    }

    /**
     * Attempts to fix problems with weather data sets
     * @param observations
     * @return
     * @throws WeatherObservationListException
     * @throws ConfigValidationException 
     */
    private List<WeatherObservation> validateAndSanitizeObservations(List<WeatherObservation> observations, Date firstTimestamp) throws WeatherObservationListException, ConfigValidationException {
        
        
        WeatherUtil wUtil = new WeatherUtil();
        
        // First we truncate observations at end of the period, so that all
        // parameter series end at the same time
        // Then we fix holes
        List<WeatherObservation> fixedObservations = wUtil.fixHourlyValuesForParameters(
                wUtil.truncateToLastCommonObservation(
                    wUtil.filterWeatherObservationsByParameter(
                            observations, 
                            new HashSet(Arrays.asList("TM","RR","UM","Q0"))
                    )
                ), 
                new HashSet(Arrays.asList("TM","RR","UM","Q0")), 
                firstTimestamp, 
                null
        );
        
        // Now we need to validate and possibly try to fix the weather data
        List<WeatherObservation> TM = new ArrayList<>();
        List<WeatherObservation> RR = new ArrayList<>();
        List<WeatherObservation> UM = new ArrayList<>();
        List<WeatherObservation> Q0 = new ArrayList<>();
        
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
                case WeatherElements.RELATIVE_HUMIDITY_MEAN:
                    UM.add(o); 
                    break;
                case WeatherElements.GLOBAL_RADIATION:
                    Q0.add(o);
                    break;
                default:
                    // Let it pass in silence 
                    break;
            }
        }
        
        // BT (Leaf wetness) may be optionally calculated
        // FM2 (Mean wind speed at 2m above ground)
        List<WeatherObservation> BT = new ArrayList<>();
        List<WeatherObservation> FM2 = new ArrayList<>();
        for(WeatherObservation o:observations)
        {
            if(o.getTimeMeasured().compareTo(firstTimestamp) >=0)
            {
                switch(o.getElementMeasurementTypeId())
                {
                    case WeatherElements.LEAF_WETNESS:
                        BT.add(o); 
                        break;

                    case WeatherElements.WIND_SPEED_2M:
                        FM2.add(o);
                        break;
                    default:
                        // Let it pass in silence 
                        break;
                }
            }
        }
                
        // Problems with weather observations
        //System.out.println("BT=" + BT.size() + " [First=," + BT.get(0).getTimeMeasured() + " last=" + BT.get(BT.size()-1).getTimeMeasured() + "]");
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
                //Collections.sort(BT);
                //System.out.println("BT=" + BT.size() + " [First=," + BT.get(0).getTimeMeasured() + " last=" + BT.get(BT.size()-1).getTimeMeasured() + "]");
                FM2 = wUtil.checkForAndFixHourlyTimeSeriesHoles(FM2);
                BT = wUtil.calculateLeafWetnessHourSeriesBestEffort(BT,TM, RR, Q0, FM2, UM);
                
                if(BT.size() != TM.size())
                {
                    // Last attempt, trying to fix minor problems:
                    fixedObservations.addAll(BT);
                    fixedObservations = wUtil.fixHourlyValuesForParameters(
                            fixedObservations, 
                            new HashSet(Arrays.asList("TM","RR","UM","Q0","BT")), 
                            firstTimestamp, 
                            null
                    );
                    BT = wUtil.filterWeatherObservationsByParameter(fixedObservations, new HashSet(Arrays.asList("BT")));
                            
                    /* 
                    // DEBUG
                    Collections.sort(BT);Collections.sort(TM);
                            
                    System.out.println("BT=" + BT.size() + " [First=," + BT.get(0).getTimeMeasured() + " last=" + BT.get(BT.size()-1).getTimeMeasured() 
                            + "], TM=" + TM.size()+ " [First=" + TM.get(0).getTimeMeasured() + ", last=" + TM.get(TM.size()-1).getTimeMeasured() + "]");
                    */
                    if(BT.size() != TM.size())
                    {
                        throw new ConfigValidationException("Missing leaf wetness data. Also, attempting to calculate leaf wetness from other weather parameters failed.");
                    }
                }
            }
            else
            {
                throw new ConfigValidationException("Incorrect number of weather data. "
                        + "TM.size() = " + TM.size() + " (first/last at " + TM.get(0).getTimeMeasured() + "/" + TM.get(TM.size()-1).getTimeMeasured() + "), "
                        + "BT.size()=" + BT.size() +  " (first/last at " + BT.get(0).getTimeMeasured() + "/" +  BT.get(BT.size()-1).getTimeMeasured() + ", "
                        + "RR.size()=" + RR.size() + " (first/last at " + RR.get(0).getTimeMeasured() + "/" + RR.get(RR.size()-1).getTimeMeasured() + ")");
            }
        }
        List<WeatherObservation> retVal = new ArrayList<>();
        retVal.addAll(TM);
        retVal.addAll(RR);
        retVal.addAll(BT);
        retVal.addAll(UM);
        retVal.addAll(Q0);
        retVal = wUtil.truncateToLastCommonObservation(retVal);
        return retVal;
    }
}
