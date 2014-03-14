/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
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

package no.bioforsk.vips.logic.scheduling.model;

import no.bioforsk.vips.logic.entity.ForecastConfiguration;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import no.bioforsk.vips.entity.ModelConfiguration;
import no.bioforsk.vips.entity.WeatherObservation;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 * All model preprocessors must implement this
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public abstract class ModelRunPreprocessor {
    
    public abstract ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException;
    
    public abstract String getModelId();
    
    /**
     * Collects weather observations from a data source
     * @param fetchURI Base URI. E.g. http://lmt.bioforsk.no/agrometbase/export/getVIPS3JSONWeatherData.php?weatherStationId=13
     * @param logIntervalId
     * @param elementMeasurementTypes
     * @param startTime
     * @param endTime
     * @param timeZone
     * @return 
     */
    protected List<WeatherObservation> getWeatherObservations(String fetchURI, Integer logIntervalId, String[] elementMeasurementTypes, Date startTime, Date endTime, TimeZone timeZone) throws PreprocessorException
    {
        SimpleDateFormat dateOutput = new SimpleDateFormat("yyyy-MM-dd");
        dateOutput.setTimeZone(timeZone);
        SimpleDateFormat hourOutput = new SimpleDateFormat("H");
        hourOutput.setTimeZone(timeZone);
        
        StringBuilder URL = new StringBuilder(fetchURI)
                .append(fetchURI.indexOf("?")>= 0 ? "&" : "?")
                .append("logIntervalId=").append(logIntervalId)
                .append("&timeZone=").append(timeZone.getID())
                .append("&startDate=").append(dateOutput.format(startTime))
                .append("&startTime=").append(hourOutput.format(startTime))
                .append("&endDate=").append(dateOutput.format(endTime))
                .append("&endTime=").append(hourOutput.format(endTime));
        for(String type: elementMeasurementTypes)
        {
                URL.append("&elementMeasurementTypes[]=").append(type);
        }
        //System.out.println("URL = " + URL.toString());
        try
        {
            return new ObjectMapper().readValue(new URL(URL.toString()), new TypeReference<List<WeatherObservation>>(){});
        }catch(IOException ex)
        {
            throw new PreprocessorException("Could not fetch weather observations from URI " + URL.toString());
        }
        
    }
    
}
