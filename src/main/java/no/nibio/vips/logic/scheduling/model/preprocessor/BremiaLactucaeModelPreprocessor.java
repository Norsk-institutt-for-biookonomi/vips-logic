/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.model.ConfigValidationException;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.WeatherObservationListException;
import no.nibio.vips.util.WeatherUtil;
import no.nibio.vips.util.weather.WeatherDataSourceException;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class BremiaLactucaeModelPreprocessor extends ModelRunPreprocessor {

    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException {
        ModelConfiguration config = new ModelConfiguration();
        PointOfInterestWeatherStation weatherStation =  (PointOfInterestWeatherStation) configuration.getWeatherStationPointOfInterestId();
        WeatherDataSourceUtil wdsUtil = new WeatherDataSourceUtil();
        WeatherUtil wUtil = new WeatherUtil();
        
        List<WeatherObservation> observations;
            
        
        try {
            observations = wdsUtil.getWeatherObservations(
                    weatherStation,
                    WeatherObservation.LOG_INTERVAL_ID_1H,
                    new String[]{
                        WeatherElements.LEAF_WETNESS_DURATION,
                        WeatherElements.LEAF_WETNESS_DURATION_GROUND_LEVEL,
                        WeatherElements.TEMPERATURE_MAXIMUM,
                        WeatherElements.GLOBAL_RADIATION,
                        WeatherElements.RELATIVE_HUMIDITY_MEAN,
                        WeatherElements.PRECIPITATION, // Backup for leaf wetness calculation
                        WeatherElements.TEMPERATURE_MEAN // Backup for leaf wetness calculation
                    },
                    configuration.getDateStartInTimeZone(),
                    configuration.getDateEndInTimeZone());
            observations = wUtil.checkForAndFixHourlyTimeSeriesHolesMultiParameter(observations);
            List<WeatherObservation> BTg = wUtil.filterWeatherObservationsByParameter(observations, new HashSet(Arrays.asList(WeatherElements.LEAF_WETNESS_DURATION_GROUND_LEVEL)));
            List<WeatherObservation> BT = wUtil.filterWeatherObservationsByParameter(observations, new HashSet(Arrays.asList(WeatherElements.LEAF_WETNESS_DURATION)));
            List<WeatherObservation> TX = wUtil.filterWeatherObservationsByParameter(observations, new HashSet(Arrays.asList(WeatherElements.TEMPERATURE_MAXIMUM)));
            List<WeatherObservation> Q0 = wUtil.filterWeatherObservationsByParameter(observations, new HashSet(Arrays.asList(WeatherElements.GLOBAL_RADIATION)));
            List<WeatherObservation> UM = wUtil.filterWeatherObservationsByParameter(observations, new HashSet(Arrays.asList(WeatherElements.RELATIVE_HUMIDITY_MEAN)));
            List<WeatherObservation> RR = wUtil.filterWeatherObservationsByParameter(observations, new HashSet(Arrays.asList(WeatherElements.PRECIPITATION)));
            List<WeatherObservation> TM = wUtil.filterWeatherObservationsByParameter(observations, new HashSet(Arrays.asList(WeatherElements.TEMPERATURE_MEAN)));
            if(BTg.isEmpty())
            {
                BTg = BT;
            }
            if(BTg.size() != TM.size())
            {
                BTg = wUtil.calculateLeafWetnessHourSeriesBestEffort(BTg,TM, RR, null,null, UM);
            }
            wUtil.renameParameter(BTg, WeatherElements.LEAF_WETNESS_DURATION, WeatherElements.LEAF_WETNESS_DURATION_GROUND_LEVEL);
            
            observations = new ArrayList<>();
            observations.addAll(BTg);
            observations.addAll(TX);
            observations.addAll(UM);
            observations.addAll(Q0);
            observations.addAll(RR);
            observations.addAll(TM);
            
            observations = wUtil.checkForAndFixHourlyTimeSeriesHolesMultiParameter(observations);
        } catch (WeatherDataSourceException | WeatherObservationListException | ConfigValidationException ex ) {
            throw new PreprocessorException(ex.getMessage());
        }
        
        config.setModelId(this.getModelId());
        config.setConfigParameter("timeZone", weatherStation.getTimeZone());
        config.setConfigParameter("longitude", weatherStation.getPointOfInterest().getLongitude());
        config.setConfigParameter("latitude", weatherStation.getPointOfInterest().getLatitude());
        config.setConfigParameter("observations", observations);
        return config;
    }

    @Override
    public String getModelId() {
        return "BREMIALACT";
    }

}
