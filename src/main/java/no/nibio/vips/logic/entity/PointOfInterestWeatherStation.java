/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
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
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Transient;

/**
 * Extension of {@see PointOfInterest}, with additional info for weather stations
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@DiscriminatorValue("1")
@Table(name = "point_of_interest_weather_station")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PointOfInterestWeatherStation.findAll", query = "SELECT p FROM PointOfInterestWeatherStation p"),
    @NamedQuery(name = "PointOfInterestWeatherStation.findAllByActivity", query = "SELECT p FROM PointOfInterestWeatherStation p WHERE p.active = :active"),
    @NamedQuery(name = "PointOfInterestWeatherStation.findByPointOfInterestId", query = "SELECT p FROM PointOfInterestWeatherStation p WHERE p.pointOfInterestId = :pointOfInterestId"),
    @NamedQuery(name = "PointOfInterestWeatherStation.findByOrganizationId", query = "SELECT p FROM PointOfInterestWeatherStation p WHERE p.user.userId IN(SELECT u.userId FROM VipsLogicUser u WHERE u.organizationId.organizationId = :organizationId)"),
    @NamedQuery(name = "PointOfInterestWeatherStation.findByActivityAndOrganizationId", query = "SELECT p FROM PointOfInterestWeatherStation p WHERE p.active = :active AND p.user.userId IN(SELECT u.userId FROM VipsLogicUser u WHERE u.organizationId = :organizationId)"),
    @NamedQuery(name = "PointOfInterestWeatherStation.findByUserId", query = "SELECT p FROM PointOfInterestWeatherStation p WHERE p.user = :userId"),
    @NamedQuery(name = "PointOfInterestWeatherStation.findByNames", query = "SELECT p FROM PointOfInterestWeatherStation p WHERE p.name IN :names")
})
public class PointOfInterestWeatherStation extends PointOfInterest {
    @Size(max = 255)
    private String weatherStationRemoteId;
    private WeatherStationDataSource weatherStationDataSourceId;
    @JsonIgnore
    private PointOfInterest pointOfInterest;
    private Boolean active;

    public PointOfInterestWeatherStation() {
    }


    @Column(name = "weather_station_remote_id")
    public String getWeatherStationRemoteId() {
        return weatherStationRemoteId;
    }

    public void setWeatherStationRemoteId(String weatherStationRemoteId) {
        this.weatherStationRemoteId = weatherStationRemoteId;
    }

    @JoinColumn(name = "weather_station_data_source_id", referencedColumnName = "weather_station_data_source_id")
    @ManyToOne
    public WeatherStationDataSource getWeatherStationDataSourceId() {
        return weatherStationDataSourceId;
    }

    public void setWeatherStationDataSourceId(WeatherStationDataSource weatherStationDataSourceId) {
        this.weatherStationDataSourceId = weatherStationDataSourceId;
    }

    @JoinColumn(name = "point_of_interest_id", referencedColumnName = "point_of_interest_id", insertable = false, updatable = false)
    @OneToOne(optional = false)
    public PointOfInterest getPointOfInterest() {
        return pointOfInterest;
    }

    public void setPointOfInterest(PointOfInterest pointOfInterest) {
        this.pointOfInterest = pointOfInterest;
    }
    
    @Override
    public Integer getPointOfInterestId()
    {
        return super.getPointOfInterestId();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.getPointOfInterestId() != null ? this.getPointOfInterestId().hashCode() : 0);
        return hash;
    }

    
    
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PointOfInterestWeatherStation)) {
            return false;
        }
        PointOfInterestWeatherStation other = (PointOfInterestWeatherStation) object;
        if ((this.getPointOfInterestId() == null && other.getPointOfInterestId() != null) || (this.getPointOfInterestId() != null && !this.getPointOfInterestId().equals(other.getPointOfInterestId()))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.PointOfInterestWeatherStation[ pointOfInterestId=" + this.getPointOfInterestId() + " ]";
    }

    /**
     * Returns the complete URI for fetching weather data from the given
     * weather data source and weather station
     * @return 
     */
    @Transient
    public String getDataFetchUri()
    {
        if(this.getWeatherStationDataSourceId() != null)
        {
            String dataRequestUri = String.format(this.getWeatherStationDataSourceId().getDatafetchUriExpression(),this.getWeatherStationRemoteId());
            return dataRequestUri;
        }
        return "";
    }

    /**
     * @return the active
     */
    @Column(name="active")
    public Boolean getActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(Boolean active) {
        this.active = active;
    }
    
}
