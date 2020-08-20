/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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

/**
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class WeatherStationProviderFactory {
    public final static int YR = 1;
    public final static int FMI = 2;

    public static WeatherForecastProvider getWeatherForecastProvider(Integer weatherForecastProviderId)
    {
        switch(weatherForecastProviderId)
        {
            case WeatherStationProviderFactory.YR:
                return new YrWeatherForecastProvider();
            case WeatherStationProviderFactory.FMI:
                return new FMIWeatherForecastprovider();
            default:
                return null;
        }
    }
}
