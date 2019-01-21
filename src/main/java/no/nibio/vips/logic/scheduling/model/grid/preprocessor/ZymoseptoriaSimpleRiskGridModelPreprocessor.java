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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
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
import no.nibio.vips.logic.util.SystemTime;
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
        List<PointOfInterestWeatherStation> stations = Stream.of(configuration.getGridWeatherStationPointOfInterestIds()).map(
                stationId -> (PointOfInterestWeatherStation) SessionControllerGetter.getPointOfInterestBean().getPointOfInterest(stationId)
            ).collect(Collectors.toList());
        
        List<PointWeatherObservationList> allObs = new ArrayList<>();
        WeatherUtil wUtil = new WeatherUtil();
        for(PointOfInterestWeatherStation station:stations)
        {
            Coordinate coordinate = new Coordinate(station.getPointOfInterest().getLongitude(), station.getPointOfInterest().getLatitude());
            List<WeatherObservation> stationObs = getStationObs(station, configuration);
            try
            {
                // We need TM, UM and RR. BT is optional
                List<WeatherObservation> mandatory = wUtil.filterWeatherObservationsByParameter(stationObs, new HashSet<>(Arrays.asList("TM","UM","RR")));
                stationObs = wUtil.checkForAndFixHourlyTimeSeriesHolesMultiParameter(mandatory, 6);
            }
            catch(WeatherObservationListException ex)
            {
                //throw new PreprocessorException();
                System.out.println("Problem with station " + station.getPointOfInterest().getName() + " (#" + station.getPointOfInterestId() + "): " + ex.getMessage());
                continue;
            }
            // Checking for BT
            try
            {
                List<WeatherObservation> BT = wUtil.filterWeatherObservationsByParameter(stationObs, new HashSet<>(Arrays.asList("BT")));
                stationObs.addAll(wUtil.checkForAndFixHourlyTimeSeriesHoles(BT, 6));
            }
            catch(WeatherObservationListException ex)
            {
                
            }
            
            PointWeatherObservationList pointObs = new PointWeatherObservationList(coordinate, stationObs, station.getTimeZone());
            allObs.add(pointObs);
        }
        
        modelConfig.setConfigParameter("multiPointWeatherObservations", allObs);
        modelConfig.setModelId(this.getModelId());
        modelConfig.setConfigParameter("timeZone", configuration.getTimeZone());
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
        
        // We run this for max 1 week back in time to make sure it doesn't crash
        ZonedDateTime aWeekAgo = ZonedDateTime.ofInstant(SystemTime.getSystemTime().toInstant(), ZoneId.of("Europe/Oslo")).minus(1, ChronoUnit.WEEKS);
        ZonedDateTime aWeekAhead = ZonedDateTime.ofInstant(SystemTime.getSystemTime().toInstant(), ZoneId.of("Europe/Oslo")).plus(1, ChronoUnit.WEEKS);
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
                    Date.from(aWeekAgo.toInstant()),
                    Date.from(aWeekAhead.toInstant()),
                    true,
                    new HashSet<>(Arrays.asList(WeatherObservation.LOG_INTERVAL_ID_1H,WeatherObservation.LOG_INTERVAL_ID_3H, WeatherObservation.LOG_INTERVAL_ID_6H))
            );
            //stationObs = wUtil.checkForAndFixHourlyTimeSeriesHoles(stationObs,6);
            return stationObs;
        } catch (WeatherDataSourceException  ex ) {
            throw new PreprocessorException(ex.getMessage());
        }
    }
}
