/*
 * Copyright (c) 2022 NIBIO <http://www.nibio.no/>. 
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.InvalidAggregationTypeException;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.WeatherObservationListException;
import no.nibio.vips.util.WeatherUtil;
import no.nibio.vips.util.weather.WeatherDataSourceException;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;

/**
 * @copyright 2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class YellowStemborerTempModelPreprocessor extends ModelRunPreprocessor {
	
	public final static String YSTEMBTEMP_BIOFIX_DATE = "YSTEMBTEMP_BIOFIX_DATE";
	public final static String YSTEMBTEMP_DD_LOWER = "YSTEMBTEMP_DD_LOWER";
	public final static String YSTEMBTEMP_DD_UPPER = "YSTEMBTEMP_DD_UPPER";
	public final static String YSTEMBTEMP_HEAT_REQUIREMENTS = "YSTEMBTEMP_HEAT_REQUIREMENTS";
	public final static String YSTEMBTEMP_OBSERVED_PHASE = "YSTEMBTEMP_OBSERVED_PHASE";

	@Override
	public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException {
		PointOfInterestWeatherStation weatherStation = (PointOfInterestWeatherStation) configuration.getWeatherStationPointOfInterestId();
        // What timezone is the calculation for
        TimeZone timeZone = TimeZone.getTimeZone(weatherStation.getTimeZone());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(timeZone);
        
        
        ModelConfiguration retVal = new ModelConfiguration();
        retVal.setModelId(this.getModelId());
        retVal.setConfigParameter("timeZone", timeZone.getID());
        
        // Getting input parameters from database
        Double[] dd_lower = null, dd_upper = null, heatRequirements = null;
        Date biofixDate;
        String observedPhase;
        Set<String> allowedPhases = Set.of("EGG","LARVA","PUPA","ADULT");
        
        try
        {
        	biofixDate = format.parse(configuration.getForecastModelConfigurationValue(YSTEMBTEMP_BIOFIX_DATE));
        	String ddl_interm = configuration.getForecastModelConfigurationValue(YSTEMBTEMP_DD_LOWER);
        	if(ddl_interm.split(",").length == 4)
        	{
        		dd_lower = Arrays.asList(ddl_interm.split(",")).stream().toArray(Double[]::new);
        	}
        	String ddu_interm = configuration.getForecastModelConfigurationValue(YSTEMBTEMP_DD_UPPER);
        	if(ddu_interm.split(",").length == 4)
        	{
        		dd_lower = Arrays.asList(ddu_interm.split(",")).stream().toArray(Double[]::new);
        	}
        	String hr_interm = configuration.getForecastModelConfigurationValue(YSTEMBTEMP_HEAT_REQUIREMENTS);
        	if(hr_interm.split(",").length == 4)
        	{
        		heatRequirements = Arrays.asList(hr_interm.split(",")).stream().toArray(Double[]::new);
        	}
        	
        	
        	observedPhase = configuration.getForecastModelConfigurationValue(YSTEMBTEMP_OBSERVED_PHASE);
        	observedPhase = observedPhase != null && allowedPhases.contains(observedPhase) ? observedPhase : null;
        	
        	
        	retVal.setConfigParameter("biofixDate", format.format(biofixDate));
            retVal.setConfigParameter("heatRequirements", heatRequirements);
            retVal.setConfigParameter("dd_lower", dd_lower);
            retVal.setConfigParameter("dd_upper", dd_upper);
            retVal.setConfigParameter("observedPhase", observedPhase);
            
            // Getting weather data
            WeatherDataSourceUtil wdsUtil = new WeatherDataSourceUtil();
            List<WeatherObservation> observations;
            // Setting the end date for the data - 9 days ahead (of systemtime)
            Calendar cal = Calendar.getInstance(timeZone);
            cal.setTime(SystemTime.getSystemTime());
            cal.add(Calendar.DATE, 9);
            
            WeatherUtil wUtil = new WeatherUtil();
            Date nineDaysFromNow = wUtil.normalizeToExactDate(cal.getTime(), timeZone);
            try {
                observations = wdsUtil.getWeatherObservations( 
                        weatherStation,
                        WeatherObservation.LOG_INTERVAL_ID_1D,
                        new String[]{
                            WeatherElements.TEMPERATURE_MINIMUM,
                            WeatherElements.TEMPERATURE_MAXIMUM
                        },
                        biofixDate, 
                        nineDaysFromNow);
            } catch (WeatherDataSourceException ex) {
                throw new PreprocessorException(ex.getMessage());
            }
            
            // If daily observations are not available, try hourly
            if(observations == null || observations.isEmpty())
            {
                try {
                    observations = wdsUtil.getWeatherObservations(
                            weatherStation,
                            WeatherObservation.LOG_INTERVAL_ID_1H,
                            new String[]{
                            		WeatherElements.TEMPERATURE_MINIMUM,
                                    WeatherElements.TEMPERATURE_MAXIMUM
                            },
                            biofixDate,
                            nineDaysFromNow);
                    // Aggregate
                    List<WeatherObservation> dailyMins = wUtil.getAggregatedDailyValues(wUtil.filterWeatherObservationsByParameter(observations, "TN"),timeZone, 15, WeatherUtil.AGGREGATION_TYPE_MINIMUM);
                    List<WeatherObservation> dailyMaxes = wUtil.getAggregatedDailyValues(wUtil.filterWeatherObservationsByParameter(observations, "TX"),timeZone, 15, WeatherUtil.AGGREGATION_TYPE_MAXIMUM);
                    observations = dailyMins;
                    observations.addAll(dailyMaxes);
                } catch (WeatherDataSourceException | WeatherObservationListException | InvalidAggregationTypeException ex) {
                    throw new PreprocessorException(ex.getMessage());
                }
            }
            
            retVal.setConfigParameter("observations", observations);
        }
        catch(ParseException | NumberFormatException ex)
        {
        	throw new PreprocessorException(ex.getMessage());
        }

        return retVal;
	}

	@Override
	public String getModelId() {
		return "YSTEMBTEMP";
	}

}
