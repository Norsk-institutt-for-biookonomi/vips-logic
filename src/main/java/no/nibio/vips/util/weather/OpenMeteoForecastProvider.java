/*
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
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
package no.nibio.vips.util.weather;

import java.io.IOException;
import java.util.List;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nibio.vips.entity.WeatherObservation;
import no.nibio.vips.logic.entity.PointOfInterest;

/** Provides forecasts from Open-Meteo */
public class OpenMeteoForecastProvider implements WeatherForecastProvider {
    private final static String OPEN_METEO_VIPS_WEATHER_PROXY_URL = System.getProperty("no.nibio.vips.logic.weather.VIPS_WEATHER_PROXY_BASE_URL") + "/rest/grid/openmeteo/?longitude={0}&latitude={1}&timeZone={2}&startDate={3}&endDate={4}&logIntervalId=1";

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
            // The OpenMeteo proxy in VIPSWeatherProxy covers not only forecasts, but also historical data. 
            // Need to set start (today at midnight) and end (4 days from now at midnight) dates for the forecast.
            Instant now = Instant.now();
            LocalDate startDate = now.atZone(ZoneId.of("UTC")).toLocalDate();
            LocalDate endDate = startDate.plusDays(4);
            String startDateStr = startDate.toString();
            String endDateStr = endDate.toString();
            URL openMeteoVIPSWeatherProxyURL = new URI(MessageFormat.format(
                    OpenMeteoForecastProvider.OPEN_METEO_VIPS_WEATHER_PROXY_URL,
                    longitude, latitude, "UTC", startDateStr, endDateStr
            )).toURL();
                   
            
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(openMeteoVIPSWeatherProxyURL.openStream(),new TypeReference<List<WeatherObservation>>(){});
        }
        catch(IOException | URISyntaxException ex)
        {
            throw new ParseWeatherDataException(ex.getMessage());
        }
    }
}
