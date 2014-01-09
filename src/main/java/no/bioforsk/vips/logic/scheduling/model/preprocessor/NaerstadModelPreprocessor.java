/*
 * Copyright (c) 2013 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with VIPSLogic.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.scheduling.model.preprocessor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.bioforsk.vips.entity.ModelConfiguration;
import no.bioforsk.vips.entity.WeatherObservation;
import no.bioforsk.vips.logic.entity.ForecastConfiguration;
import no.bioforsk.vips.logic.entity.PointOfInterestWeatherStation;
import no.bioforsk.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.bioforsk.vips.logic.scheduling.model.PreprocessorException;
import no.bioforsk.vips.logic.util.SystemTime;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class NaerstadModelPreprocessor extends ModelRunPreprocessor{
    public final static String NAERSTADMO_CALCULATION_START = "NAERSTADMO_CALCULATION_START";
    public final static String NAERSTADMO_CALCULATION_END = "NAERSTADMO_CALCULATION_END";

    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException
    {
        try {
            PointOfInterestWeatherStation weatherStation = (PointOfInterestWeatherStation) configuration.getLocationPointOfInterestId();
            TimeZone timeZone = TimeZone.getTimeZone(weatherStation.getTimeZone());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
            Date calculationStart = format.parse(configuration.getForecastModelConfigurationValue(NAERSTADMO_CALCULATION_START));
            Date calculationEnd = format.parse(configuration.getForecastModelConfigurationValue(NAERSTADMO_CALCULATION_END));
            Date dateEndWeatherData = configuration.getDateEnd().compareTo(SystemTime.getSystemTime()) < 0 ? configuration.getDateEnd() : SystemTime.getSystemTime();
            
            List<WeatherObservation> observations = getWeatherObservations(weatherStation.getDataFetchUri(), WeatherObservation.LOG_INTERVAL_ID_1H, new String[]{"TM", "RR", "BT","Q0","UM"}, configuration.getDateStart(), dateEndWeatherData, timeZone);
            
            ModelConfiguration retVal = new ModelConfiguration();
            retVal.setModelId(this.getModelId());
            retVal.setConfigParameter("calculationStart", calculationStart);
            retVal.setConfigParameter("calculationEnd", calculationEnd);
            retVal.setConfigParameter("observations", observations);
            
            return retVal;
        } catch (ParseException ex) 
        {
            Logger.getLogger(NaerstadModelPreprocessor.class.getName()).log(Level.SEVERE, null, ex);
            throw new PreprocessorException("Could not parse date. " + ex.getMessage());
        }
    }

    @Override
    public String getModelId() {
        return "NAERSTADMO";
    }

}
