/*
 * Copyright (c) 2019 NIBIO <http://www.nibio.no/>. 
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
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.controller.session.ForecastBean;
import no.nibio.vips.logic.controller.session.SessionControllerGetter;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.weather.WeatherDataSourceException;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;

/**
 * @copyright 2019-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class SeptoriaHumidityModelPreprocessor  extends ModelRunPreprocessor{
    
    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException
    {
        ModelConfiguration retVal = new ModelConfiguration();
        retVal.setModelId(this.getModelId());
        retVal.setConfigParameter("timeZone", configuration.getTimeZone());
        String[] modelConfigurationValueNames = {"dateSpraying1","dateSpraying2","dateGs31","date3rdUpperLeafEmerging",
        "date2ndUpperLeafEmerging","dateUpperLeafEmerging","dateGs75","thresholdRelativeHumidity","thresholdLeafWetness",
        "thresholdPrecipitation","slidingHoursPast","slidingHoursAhead","thresholdHumidPeriodHours","sprayingProtectionDays",
        "leafLifeTime"};
        for(String modelConfigurationValueName: modelConfigurationValueNames)
        {
            retVal.setConfigParameter(modelConfigurationValueName, configuration.getForecastModelConfigurationValue(SessionControllerGetter.getForecastBean().getDeCamelizedFieldName(this.getModelId(),modelConfigurationValueName)));
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone(configuration.getTimeZone()));
        WeatherDataSourceUtil wdsUtil = new WeatherDataSourceUtil();
        try
        {
            Date gs31 = format.parse((String)retVal.getConfigParameter("dateGs31"));
            // Pull data from one day before, to make sure we don't have any "startup issues" in the weather data
            Date dateStart = Date.from(gs31.toInstant().minus(1, ChronoUnit.DAYS));
            List<WeatherObservation> observations = wdsUtil.getWeatherObservations(
                        (PointOfInterestWeatherStation) configuration.getWeatherStationPointOfInterestId(), 
                        WeatherObservation.LOG_INTERVAL_ID_1H, 
                        new String[]{
                            WeatherElements.TEMPERATURE_MEAN,
                            WeatherElements.PRECIPITATION,
                            WeatherElements.RELATIVE_HUMIDITY_MEAN,
                            WeatherElements.LEAF_WETNESS_DURATION
                        }, 
                        dateStart, 
                        configuration.getDateEnd()
                );
            Collections.sort(observations);
            retVal.setConfigParameter("observations", observations);
        }
        catch(WeatherDataSourceException | ParseException ex)
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
