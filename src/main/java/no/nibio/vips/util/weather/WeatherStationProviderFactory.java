/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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

/**
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class WeatherStationProviderFactory {
    public final static int YR = 1;
    public final static int FMI = 2;
    public final static int OPEN_METEO = 3;

    public static WeatherForecastProvider getWeatherForecastProvider(Integer weatherForecastProviderId)
    {
        switch(weatherForecastProviderId)
        {
            case WeatherStationProviderFactory.YR:
                return new YrWeatherForecastProvider();
            case WeatherStationProviderFactory.FMI:
                return new FMIWeatherForecastprovider();
            case WeatherStationProviderFactory.OPEN_METEO:
                return new OpenMeteoForecastProvider();
            default:
                return null;
        }
    }
}
