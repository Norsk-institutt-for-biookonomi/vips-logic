/*
 * Copyright (c) 2018 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.scheduling.model.grid.preprocessor;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.PointWeatherObservationList;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.WeatherObservationListException;
import no.nibio.vips.util.WeatherUtil;
import no.nibio.vips.util.weather.WeatherDataSourceException;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;

/**
 * @copyright 2018 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class ZymoseptoriaSimpleRiskGridModelPreprocessor extends ModelRunPreprocessor{

    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException {
        ModelConfiguration modelConfig = new ModelConfiguration();
        // Which weather stations??
        // Ilseng and Apelsvoll to start with ;-)
        List<PointOfInterestWeatherStation> stations = Stream.of(new Integer[]{50,91,87}).map(
                stationId -> (PointOfInterestWeatherStation) SessionControllerGetter.getPointOfInterestBean().getPointOfInterest(stationId)
            ).collect(Collectors.toList());
        
        List<PointWeatherObservationList> allObs = new ArrayList<>();
        
        for(PointOfInterestWeatherStation station:stations)
        {
            Coordinate coordinate = new Coordinate(station.getPointOfInterest().getLongitude(), station.getPointOfInterest().getLatitude());
            List<WeatherObservation> stationObs = getStationObs(station, configuration);
            PointWeatherObservationList pointObs = new PointWeatherObservationList(coordinate, stationObs);
            allObs.add(pointObs);
        }
        
        modelConfig.setConfigParameter("multiPointWeatherObservations", allObs);
        modelConfig.setModelId(this.getModelId());
        modelConfig.setConfigParameter("timeZone", stations.get(0).getTimeZone());
        return modelConfig;
    }

    @Override
    public String getModelId() {
        return "GRIDZYMOSE";
    }
    
    private List<WeatherObservation> getStationObs(PointOfInterestWeatherStation station, ForecastConfiguration configuration) throws PreprocessorException
    {
        List<WeatherObservation> stationObs;// = new ArrayList<>();
        WeatherDataSourceUtil wdsUtil = new WeatherDataSourceUtil();
        WeatherUtil wUtil = new WeatherUtil();
        try {

            stationObs = wdsUtil.getWeatherObservations(
                    station, 
                    WeatherObservation.LOG_INTERVAL_ID_1H, 
                    new String[]{
                        WeatherElements.LEAF_WETNESS,
                        WeatherElements.RELATIVE_HUMIDITY_MEAN,
                        WeatherElements.PRECIPITATION, 
                        WeatherElements.TEMPERATURE_MEAN
                    }, 
                    configuration.getDateStartInTimeZone(),
                    configuration.getDateEndInTimeZone(),
                    true
            );
            stationObs = wUtil.checkForAndFixHourlyTimeSeriesHoles(stationObs);
            return stationObs;
        } catch (WeatherDataSourceException | WeatherObservationListException ex ) {
            throw new PreprocessorException(ex.getMessage());
        }
    }
}
