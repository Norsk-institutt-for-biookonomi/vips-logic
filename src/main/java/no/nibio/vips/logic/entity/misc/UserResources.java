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

package no.nibio.vips.logic.entity.misc;

import java.util.ArrayList;
import java.util.List;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.MessageLocale;
import no.nibio.vips.logic.entity.Observation;
import no.nibio.vips.logic.entity.PointOfInterest;

/**
 * Utility class for collecting user's resources in a uniform way
 * @copyright 2019 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class UserResources {
    private List<PointOfInterest> pois;
    private List<MessageLocale> messageLocales;
    private List<ForecastConfiguration> forecastConfigurations;
    private List<Observation> observations;
    
    public boolean isEmpty(){
        return (this.getPois() == null || this.getPois().isEmpty())
                && (this.getMessageLocales() == null || this.getMessageLocales().isEmpty())
                && (this.getForecastConfigurations() == null || this.getForecastConfigurations().isEmpty())
                && (this.getObservations() == null || this.getObservations().isEmpty());
    }

    /**
     * @return the weatherStations
     */
    public List<PointOfInterest> getPois() {
        if(this.pois == null)
        {
            this.pois = new ArrayList<>();
        }
        return pois;
    }

    /**
     * @param pois the weatherStations to set
     */
    public void setPois(List<PointOfInterest> pois) {
        this.pois = pois;
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

    /**
     * @return the observations
     */
    public List<Observation> getObservations() {
        return observations;
    }

    /**
     * @param observations the observations to set
     */
    public void setObservations(List<Observation> observations) {
        this.observations = observations;
    }
    

}
