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

package no.nibio.vips.logic.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "weather_forecast_provider")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "WeatherForecastProvider.findAll", query = "SELECT w FROM WeatherForecastProvider w"),
    @NamedQuery(name = "WeatherForecastProvider.findByWeatherForecastProviderId", query = "SELECT w FROM WeatherForecastProvider w WHERE w.weatherForecastProviderId = :weatherForecastProviderId"),
    @NamedQuery(name = "WeatherForecastProvider.findByName", query = "SELECT w FROM WeatherForecastProvider w WHERE w.name = :name"),
    @NamedQuery(name = "WeatherForecastProvider.findByDescription", query = "SELECT w FROM WeatherForecastProvider w WHERE w.description = :description")})
public class WeatherForecastProvider implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "weather_forecast_provider_id")
    private Integer weatherForecastProviderId;
    @Size(max = 255)
    @Column(name = "name")
    private String name;
    @Size(max = 511)
    @Column(name = "info_url")
    private String infoUrl;
    @Size(max = 2147483647)
    @Column(name = "description")
    private String description;

    public WeatherForecastProvider() {
    }

    public WeatherForecastProvider(Integer weatherForecastProviderId) {
        this.weatherForecastProviderId = weatherForecastProviderId;
    }

    public Integer getWeatherForecastProviderId() {
        return weatherForecastProviderId;
    }

    public void setWeatherForecastProviderId(Integer weatherForecastProviderId) {
        this.weatherForecastProviderId = weatherForecastProviderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (weatherForecastProviderId != null ? weatherForecastProviderId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof WeatherForecastProvider)) {
            return false;
        }
        WeatherForecastProvider other = (WeatherForecastProvider) object;
        if ((this.weatherForecastProviderId == null && other.weatherForecastProviderId != null) || (this.weatherForecastProviderId != null && !this.weatherForecastProviderId.equals(other.weatherForecastProviderId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.WeatherForecastProvider[ weatherForecastProviderId=" + weatherForecastProviderId + " ]";
    }

    /**
     * @return the infoUrl
     */
    public String getInfoUrl() {
        return infoUrl;
    }

    /**
     * @param infoUrl the infoUrl to set
     */
    public void setInfoUrl(String infoUrl) {
        this.infoUrl = infoUrl;
    }

}
