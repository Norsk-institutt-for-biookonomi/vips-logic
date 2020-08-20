/*
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.util.weather;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.entity.PointOfInterest;

/**
 * Provides forecasts from 
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class FMIWeatherForecastprovider implements WeatherForecastProvider {
    
    private final static String FMI_VIPS_WEATHER_PROXY_URL = System.getProperty("no.nibio.vips.logic.weather.VIPS_WEATHER_PROXY_BASE_URL") + "/rest/forecasts/fmi?longitude={0}&latitude={1}&altitude={2}";
    
    @Override
    public List<WeatherObservation> getWeatherForecasts(PointOfInterest location) throws ParseWeatherDataException {
        if(location.getGisGeom() != null)
        {
            return this.getWeatherForecasts(location.getGisGeom().getCoordinate().x, location.getGisGeom().getCoordinate().y, location.getGisGeom().getCoordinate().z);
        }
        else
        {
            return this.getWeatherForecasts(location.getLongitude(), location.getLatitude(), location.getAltitude());
        }
    }

    public List<WeatherObservation> getWeatherForecasts(Double longitude, Double latitude, Double altitude) throws ParseWeatherDataException 
    {
        try
        {
            URL FMIVIPSWeatherProxyURL = new URL(MessageFormat.format(
                    FMIWeatherForecastprovider.FMI_VIPS_WEATHER_PROXY_URL,
                    longitude, latitude, 0
            ));
            //String unserializedResponse = new Scanner(FMIVIPSWeatherProxyURL.openStream(),"UTF-8")
            //        .useDelimiter("\\A").next();
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(FMIVIPSWeatherProxyURL.openStream(),new TypeReference<List<WeatherObservation>>(){});
        }
        catch(IOException ex)
        {
            throw new ParseWeatherDataException(ex.getMessage());
        }
                
    }
}
