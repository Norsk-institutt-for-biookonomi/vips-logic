/*
 * Copyright (c) 2019 NIBIO <http://www.nibio.no/>. 
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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.WeatherObservationListException;
import no.nibio.vips.util.WeatherUtil;
import no.nibio.vips.util.weather.WeatherDataSourceException;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;

/**
 * @copyright 2018 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class SeptoriaReferenceHumidityModelPreprocessor extends ModelRunPreprocessor{

    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException {
        
        ModelConfiguration modelConfig = new ModelConfiguration();
        // Which weather stations??
        // Ilseng and Apelsvoll to start with ;-)
        PointOfInterestWeatherStation station = (PointOfInterestWeatherStation) configuration.getWeatherStationPointOfInterestId();
  
        //List<WeatherObservation> stationObs = getStationObs(station, configuration);
        WeatherUtil wUtil = new WeatherUtil();
        WeatherDataSourceUtil wdsUtil = new WeatherDataSourceUtil();
        List<WeatherObservation> stationObs;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date seasonStart = configuration.getDateStartInTimeZone();
        Date seasonEnd = configuration.getDateEndInTimeZone();
        try
        {
            if(configuration.getForecastModelConfigurationValue(this.getModelId() + "_SEASON_START") != null)
            {
                seasonStart = format.parse(configuration.getForecastModelConfigurationValue(this.getModelId() + "_SEASON_START"));
            }
            if(configuration.getForecastModelConfigurationValue(this.getModelId() + "_SEASON_END") != null)
            {
                seasonEnd = format.parse(configuration.getForecastModelConfigurationValue(this.getModelId() + "_SEASON_END"));
            }
        }
        catch(ParseException ex)
        {
            // Keep calm and carry on?
        }
        try
        {
            stationObs = wdsUtil.getWeatherObservations(
                    station,
                    WeatherObservation.LOG_INTERVAL_ID_1H,
                    new String[]{
                        WeatherElements.LEAF_WETNESS_DURATION,
                        WeatherElements.RELATIVE_HUMIDITY_MEAN,
                        WeatherElements.PRECIPITATION, 
                        WeatherElements.TEMPERATURE_MEAN 
                    },
                    seasonStart,
                    seasonEnd);
            // We need TM, UM and RR. BT is optional
            //List<WeatherObservation> mandatory = wUtil.filterWeatherObservationsByParameter(stationObs, new HashSet<>(Arrays.asList("TM","UM","RR")));
            //stationObs = wUtil.checkForAndFixHourlyTimeSeriesHolesMultiParameter(mandatory, 6);
            stationObs = wUtil.filterWeatherObservationsByParameter(stationObs, new HashSet<>(Arrays.asList("TM","UM","RR")));
        }
        catch(WeatherDataSourceException  ex)
        {
            throw new PreprocessorException("Problem with station " + station.getPointOfInterest().getName() + " (#" + station.getPointOfInterestId() + "): " + ex.getMessage());
        }
        // Checking for BT
        //try
        //{
            List<WeatherObservation> BT = wUtil.filterWeatherObservationsByParameter(stationObs, new HashSet<>(Arrays.asList("BT")));
            //stationObs.addAll(wUtil.checkForAndFixHourlyTimeSeriesHoles(BT, 6));
            stationObs.addAll(BT);
        //}
        //catch(WeatherObservationListException ex)
        //{
        //    System.out.println("Problem with station " + station.getPointOfInterest().getName() + " (#" + station.getPointOfInterestId() + "): " + ex.getMessage());
        //}
            

        
        
        modelConfig.setConfigParameter("observations", stationObs);
        modelConfig.setModelId(this.getModelId());
        modelConfig.setConfigParameter("timeZone", configuration.getTimeZone());
        return modelConfig;
    }

    @Override
    public String getModelId() {
        return "SEPTREFHUM";
    }
}
