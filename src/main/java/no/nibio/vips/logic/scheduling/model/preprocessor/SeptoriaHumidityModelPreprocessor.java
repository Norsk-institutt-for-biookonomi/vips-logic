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

import java.util.Collections;
import java.util.List;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.weather.WeatherDataSourceException;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;

/**
 * @copyright 2019 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class SeptoriaHumidityModelPreprocessor  extends ModelRunPreprocessor{
    
    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException
    {
        ModelConfiguration retVal = new ModelConfiguration();
        retVal.setModelId(this.getModelId());
        retVal.setConfigParameter("timeZone", configuration.getTimeZone());
        WeatherDataSourceUtil wdsUtil = new WeatherDataSourceUtil();
        try
        {
            List<WeatherObservation> observations = wdsUtil.getWeatherObservations(
                        (PointOfInterestWeatherStation) configuration.getWeatherStationPointOfInterestId(), 
                        WeatherObservation.LOG_INTERVAL_ID_1H, 
                        new String[]{
                            WeatherElements.TEMPERATURE_MEAN,
                            WeatherElements.PRECIPITATION,
                            WeatherElements.RELATIVE_HUMIDITY_MEAN,
                            WeatherElements.LEAF_WETNESS_DURATION
                        }, 
                        configuration.getDateStart(), 
                        configuration.getDateEnd()
                );
            Collections.sort(observations);
            retVal.setConfigParameter("observations", observations);
        }
        catch(WeatherDataSourceException ex)
        {
            throw new PreprocessorException(ex.getMessage());
        }
        configuration.getForecastModelConfigurationSet().forEach(c->
                retVal.setConfigParameter(c.getForecastModelConfigurationPK().getModelConfigParameter(), c.getParameterValue())
        );
        return retVal;
    }



    @Override
    public String getModelId() {
        return "SEPTORIAHU";
    }

}
