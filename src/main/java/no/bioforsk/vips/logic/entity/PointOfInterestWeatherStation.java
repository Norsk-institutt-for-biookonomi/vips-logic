/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the Bioforsk Open Source License as published by 
 * Bioforsk, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Bioforsk Open Source License for more details.
 * 
 * You should have received a copy of the Bioforsk Open Source License
 * along with VIPSLogic.  If not, see <http://www.bioforsk.no/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Extension of {@see PointOfInterest}, with additional info for weather stations
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@DiscriminatorValue("1")
@Table(name = "point_of_interest_weather_station")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PointOfInterestWeatherStation.findAll", query = "SELECT p FROM PointOfInterestWeatherStation p"),
    @NamedQuery(name = "PointOfInterestWeatherStation.findAllByActivity", query = "SELECT p FROM PointOfInterestWeatherStation p WHERE p.active = :active"),
    @NamedQuery(name = "PointOfInterestWeatherStation.findByPointOfInterestId", query = "SELECT p FROM PointOfInterestWeatherStation p WHERE p.pointOfInterestId = :pointOfInterestId"),
    @NamedQuery(name = "PointOfInterestWeatherStation.findByOrganizationId", query = "SELECT p FROM PointOfInterestWeatherStation p WHERE p.userId IN(SELECT u.userId FROM VipsLogicUser u WHERE u.organizationId=:organizationId)"),
    @NamedQuery(name = "PointOfInterestWeatherStation.findByActivityAndOrganizationId", query = "SELECT p FROM PointOfInterestWeatherStation p WHERE p.active = :active AND p.userId IN(SELECT u.userId FROM VipsLogicUser u WHERE u.organizationId=:organizationId)"),
    @NamedQuery(name = "PointOfInterestWeatherStation.findByUserId", query = "SELECT p FROM PointOfInterestWeatherStation p WHERE p.userId = :userId")
})
public class PointOfInterestWeatherStation extends PointOfInterest implements Serializable {
    @Size(max = 255)
    @Column(name = "weather_station_remote_id")
    private String weatherStationRemoteId;
    @JoinColumn(name = "weather_station_data_source_id", referencedColumnName = "weather_station_data_source_id")
    @ManyToOne
    private WeatherStationDataSource weatherStationDataSourceId;
    @JsonIgnore
    @JoinColumn(name = "point_of_interest_id", referencedColumnName = "point_of_interest_id", insertable = false, updatable = false)
    @OneToOne(optional = false)
    private PointOfInterest pointOfInterest;
    @Column(name="active")
    private Boolean active;

    public PointOfInterestWeatherStation() {
    }


    public String getWeatherStationRemoteId() {
        return weatherStationRemoteId;
    }

    public void setWeatherStationRemoteId(String weatherStationRemoteId) {
        this.weatherStationRemoteId = weatherStationRemoteId;
    }

    public WeatherStationDataSource getWeatherStationDataSourceId() {
        return weatherStationDataSourceId;
    }

    public void setWeatherStationDataSourceId(WeatherStationDataSource weatherStationDataSourceId) {
        this.weatherStationDataSourceId = weatherStationDataSourceId;
    }

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
        return "no.bioforsk.vips.logic.entity.PointOfInterestWeatherStation[ pointOfInterestId=" + this.getPointOfInterestId() + " ]";
    }

    /**
     * Returns the complete URI for fetching weather data from the given
     * weather data source and weather station
     * @return 
     */
    public String getDataFetchUri()
    {
        String dataRequestUri = String.format(this.getWeatherStationDataSourceId().getDatafetchUriExpression(),this.getWeatherStationRemoteId());
        return dataRequestUri;
    }

    /**
     * @return the active
     */
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
