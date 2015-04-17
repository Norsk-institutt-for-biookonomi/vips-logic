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

package no.bioforsk.vips.logic.entity.misc;

import java.util.ArrayList;
import java.util.List;
import no.bioforsk.vips.logic.entity.ForecastConfiguration;
import no.bioforsk.vips.logic.entity.MessageLocale;
import no.bioforsk.vips.logic.entity.PointOfInterestWeatherStation;

/**
 * Utility class for collecting user's resources in a uniform way
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class UserResources {
    private List<PointOfInterestWeatherStation> weatherStations;
    private List<MessageLocale> messageLocales;
    private List<ForecastConfiguration> forecastConfigurations;
    
    public boolean isEmpty(){
        return (this.getWeatherStations() == null || this.getWeatherStations().isEmpty())
                && (this.getMessageLocales() == null || this.getMessageLocales().isEmpty())
                && (this.getForecastConfigurations() == null || this.getForecastConfigurations().isEmpty());
    }

    /**
     * @return the weatherStations
     */
    public List<PointOfInterestWeatherStation> getWeatherStations() {
        if(this.weatherStations == null)
        {
            this.weatherStations = new ArrayList<>();
        }
        return weatherStations;
    }

    /**
     * @param weatherStations the weatherStations to set
     */
    public void setWeatherStations(List<PointOfInterestWeatherStation> weatherStations) {
        this.weatherStations = weatherStations;
    }

    /**
     * @return the messageLocales
     */
    public List<MessageLocale> getMessageLocales() {
        if(this.messageLocales == null)
        {
            this.messageLocales = new ArrayList<>();
        }
        return messageLocales;
    }

    /**
     * @param messageLocales the messageLocales to set
     */
    public void setMessageLocales(List<MessageLocale> messageLocales) {
        this.messageLocales = messageLocales;
    }

    /**
     * @return the forecastConfigurations
     */
    public List<ForecastConfiguration> getForecastConfigurations() {
        if(this.forecastConfigurations == null)
        {
            this.forecastConfigurations = new ArrayList<>();
        }
        return forecastConfigurations;
    }

    /**
     * @param forecastConfigurations the forecastConfigurations to set
     */
    public void setForecastConfigurations(List<ForecastConfiguration> forecastConfigurations) {
        this.forecastConfigurations = forecastConfigurations;
    }
    

}
