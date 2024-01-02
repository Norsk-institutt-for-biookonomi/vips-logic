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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
import no.nibio.vips.util.WeatherElements;
import no.nibio.vips.util.WeatherUtil;
import no.nibio.vips.util.weather.WeatherDataSourceException;
import no.nibio.vips.util.weather.WeatherDataSourceUtil;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class PsilaRosaeTempModelPreprocessor extends ModelRunPreprocessor {
    
    public final static String PSILARTEMP_START_DATE_DAY_DEGREE_ACCUMULATION = "PSILARTEMP_START_DATE_DAY_DEGREE_ACCUMULATION";

    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException {
        PointOfInterestWeatherStation weatherStation = (PointOfInterestWeatherStation) configuration.getWeatherStationPointOfInterestId();
        // What timezone is the calculation for
        TimeZone timeZone = TimeZone.getTimeZone(weatherStation.getTimeZone());
        // Getting date three days after "today"
        Calendar cal = Calendar.getInstance(timeZone);
        cal.setTime(SystemTime.getSystemTime());
        cal.add(Calendar.DATE, 3);
        WeatherUtil wUtil = new WeatherUtil();
        Date endDate = wUtil.normalizeToExactDate(cal.getTime(), timeZone);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(timeZone);
        Date startDate = cal.getTime(); // Fallback
        try {
            startDate = format.parse(configuration.getForecastModelConfigurationValue(PSILARTEMP_START_DATE_DAY_DEGREE_ACCUMULATION));
        } catch (ParseException ex) {
            Logger.getLogger(PsilaRosaeTempModelPreprocessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        ModelConfiguration retVal = new ModelConfiguration();
        WeatherDataSourceUtil wdsUtil = new WeatherDataSourceUtil();
        List<WeatherObservation> observations;
        try {
            observations = wdsUtil.getWeatherObservations( 
                    weatherStation,
                    WeatherObservation.LOG_INTERVAL_ID_1D,
                    new String[]{
                        WeatherElements.TEMPERATURE_MEAN
                    },
                    startDate, 
                    endDate);
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
                            WeatherElements.TEMPERATURE_MEAN
                        },
                        startDate,
                        endDate);
            } catch (WeatherDataSourceException ex) {
                throw new PreprocessorException(ex.getMessage());
            }
        }
        
        // TODO:  weather data validation
        retVal.setModelId(this.getModelId());
        retVal.setConfigParameter("timeZone", timeZone.getID());
        retVal.setConfigParameter("observations", observations);
            
        return retVal;
    }

    @Override
    public String getModelId() {
        return "PSILARTEMP";
    }

}
