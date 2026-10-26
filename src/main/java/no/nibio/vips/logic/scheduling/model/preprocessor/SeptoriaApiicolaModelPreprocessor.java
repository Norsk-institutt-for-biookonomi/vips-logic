/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.controller.session.ObservationBean;
import no.nibio.vips.logic.controller.session.OrganismBean;
import no.nibio.vips.logic.controller.session.SessionControllerGetter;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.Organism;
import no.nibio.vips.logic.entity.PointOfInterest;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.model.ConfigValidationException;
import no.nibio.vips.observation.Observation;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.WeatherObservationListException;
import no.nibio.vips.util.WeatherUtil;
import no.nibio.vips.util.weather.WeatherDataSourceException;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;

/**
 * @copyright 2016-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class SeptoriaApiicolaModelPreprocessor extends ModelRunPreprocessor {
    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException {
        ModelConfiguration config;
        PointOfInterest location = configuration.getLocationPointOfInterestId();
        PointOfInterestWeatherStation weatherStation =  (PointOfInterestWeatherStation) configuration.getWeatherStationPointOfInterestId();
        config = new ModelConfiguration();
        
        Organism organism = SessionControllerGetter.getOrganismBean().getOrganismByLatinName("Septoria apiicola");
        //System.out.println(organism);
        List<Observation> pestObservations = SessionControllerGetter.getObservationBean().getObservations(organism.getOrganismId(), location.getPointOfInterestId(),configuration.getDateStart(),SystemTime.getSystemTime());
        
        //System.out.println("Found " + pestObservations + " observations");
        WeatherDataSourceUtil wdsUtil = new WeatherDataSourceUtil();
        WeatherUtil wUtil = new WeatherUtil();
        
        List<WeatherObservation> observations;
            
   
        try {
            observations = wdsUtil.getWeatherObservations(
                    weatherStation,
                    WeatherObservation.LOG_INTERVAL_ID_1H,
                    new String[]{
                        WeatherElements.LEAF_WETNESS_DURATION,
                        WeatherElements.TEMPERATURE_MEAN,
                        WeatherElements.PRECIPITATION,
                        WeatherElements.RELATIVE_HUMIDITY_MEAN
                    },
                    configuration.getDateStartInTimeZone(),
                    configuration.getDateEndInTimeZone());
            observations = wUtil.checkForAndFixHourlyTimeSeriesHolesMultiParameter(observations);
            List<WeatherObservation> BT = wUtil.filterWeatherObservationsByParameter(observations, new HashSet(Arrays.asList(WeatherElements.LEAF_WETNESS_DURATION)));
            List<WeatherObservation> TM = wUtil.filterWeatherObservationsByParameter(observations, new HashSet(Arrays.asList(WeatherElements.TEMPERATURE_MEAN)));
            List<WeatherObservation> RR = wUtil.filterWeatherObservationsByParameter(observations, new HashSet(Arrays.asList(WeatherElements.PRECIPITATION)));
            List<WeatherObservation> UM = wUtil.filterWeatherObservationsByParameter(observations, new HashSet(Arrays.asList(WeatherElements.RELATIVE_HUMIDITY_MEAN)));
            if(BT.size() != TM.size())
            {
            	// Start calculating BT after last occurence of BT
            	Collections.sort(BT);
            	Date lastBTTime = BT.get(BT.size()-1).getTimeMeasured();
            	
                BT.addAll(wUtil.calculateLeafWetnessHourSeriesSimple(
                		TM.stream().filter(o->o.getTimeMeasured().after(lastBTTime)).collect(Collectors.toList()), 
                		RR.stream().filter(o->o.getTimeMeasured().after(lastBTTime)).collect(Collectors.toList()), 
                		UM.stream().filter(o->o.getTimeMeasured().after(lastBTTime)).collect(Collectors.toList())
                		)
                );
            }
            observations = new ArrayList<>();
            observations.addAll(BT);
            observations.addAll(TM);
            observations.addAll(RR);
            observations.addAll(UM);
            observations = wUtil.checkForAndFixHourlyTimeSeriesHolesMultiParameter(observations);
        } catch (WeatherDataSourceException | WeatherObservationListException | ConfigValidationException ex ) {
            throw new PreprocessorException(ex.getMessage());
        }
        
        config.setModelId(this.getModelId());
        config.setConfigParameter("timeZone", weatherStation.getTimeZone());
        config.setConfigParameter("observations", observations);
        config.setConfigParameter("pestObservations", pestObservations);
        return config;
    }

    @Override
    public String getModelId() {
        return "SEPAPIICOL";
    }

}
