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
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.WeatherObservationListException;
import no.nibio.vips.util.WeatherUtil;
import no.nibio.vips.util.weather.WeatherDataSourceException;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class NegativePrognosisModelPreprocessor extends ModelRunPreprocessor{

    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException {
        PointOfInterestWeatherStation weatherStation = (PointOfInterestWeatherStation) configuration.getWeatherStationPointOfInterestId();
        TimeZone timeZone = TimeZone.getTimeZone(weatherStation.getTimeZone());
        
        WeatherDataSourceUtil wdsUtil = new WeatherDataSourceUtil();
        WeatherUtil wUtil = new WeatherUtil();
            
        List<WeatherObservation> observations;
            
   
        try {
            observations = wdsUtil.getWeatherObservations(
                    weatherStation,
                    WeatherObservation.LOG_INTERVAL_ID_1H,
                    new String[]{
                        WeatherElements.TEMPERATURE_MEAN,
                        WeatherElements.PRECIPITATION,
                        WeatherElements.RELATIVE_HUMIDITY_MEAN
                    },
                    configuration.getDateStartInTimeZone(),
                    configuration.getDateEndInTimeZone());
            observations = wUtil.checkForAndFixHourlyTimeSeriesHolesMultiParameter(observations);
        } catch (WeatherDataSourceException | WeatherObservationListException ex) {
            throw new PreprocessorException(ex.getMessage());
        }
        
        ModelConfiguration retVal = new ModelConfiguration();
        retVal.setModelId(this.getModelId());
        retVal.setConfigParameter("timeZone", weatherStation.getTimeZone());
        retVal.setConfigParameter("observations", observations);
        return retVal;
    }

    @Override
    public String getModelId() {
        return "NEGPROGMOD";
    }

}
