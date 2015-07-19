/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
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
package no.nibio.vips.logic.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorType;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

/**
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "point_of_interest")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="point_of_interest_type_id", discriminatorType=DiscriminatorType.INTEGER)
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PointOfInterest.findAll", query = "SELECT p FROM PointOfInterest p"),
    @NamedQuery(name = "PointOfInterest.findByPointOfInterestId", query = "SELECT p FROM PointOfInterest p WHERE p.pointOfInterestId = :pointOfInterestId"),
    //@NamedQuery(name = "PointOfInterest.findByPointOfInterestTypeId", query = "SELECT p FROM PointOfInterest p WHERE p.pointOfInterestType = :pointOfInterestType"),
    @NamedQuery(name = "PointOfInterest.findByOrganizationId", query = "SELECT p FROM PointOfInterest p WHERE p.userId IN(SELECT u.userId FROM VipsLogicUser u WHERE u.organizationId=:organizationId OR u.organizationId IN (SELECT o.organizationId FROM Organization o WHERE o.parentOrganizationId = :organizationId))"),
    @NamedQuery(name = "PointOfInterest.findByName", query = "SELECT p FROM PointOfInterest p WHERE p.name = :name"),
    @NamedQuery(name = "PointOfInterest.findByLongitude", query = "SELECT p FROM PointOfInterest p WHERE p.longitude = :longitude"),
    @NamedQuery(name = "PointOfInterest.findByLatitude", query = "SELECT p FROM PointOfInterest p WHERE p.latitude = :latitude"),
    @NamedQuery(name = "PointOfInterest.findByAltitude", query = "SELECT p FROM PointOfInterest p WHERE p.altitude = :altitude"),
    @NamedQuery(name = "PointOfInterest.findByUserId", query = "SELECT p FROM PointOfInterest p WHERE p.userId = :userId")
})
public class PointOfInterest implements Serializable, Comparable {
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "pointOfInterest", fetch = FetchType.EAGER)
    private Set<PointOfInterestExternalResource> pointOfInterestExternalResourceSet;

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "point_of_interest_id")
    private Integer pointOfInterestId;
    @Size(max = 255)
    @Column(name = "name")
    private String name;
    @Size(max = 255)
    @Column(name = "time_zone")
    private String timeZone;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "longitude")
    private Double longitude;
    @Column(name = "latitude")
    private Double latitude;
    @Column(name = "altitude")
    private Double altitude;
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ManyToOne
    private VipsLogicUser userId;
    @JoinColumn(name = "country_code", referencedColumnName = "country_code")
    @ManyToOne
    private Country countryCode;
    @JoinColumn(name = "weather_forecast_provider_id", referencedColumnName = "weather_forecast_provider_id")
    @ManyToOne
    private WeatherForecastProvider weatherForecastProviderId;
   
    
    // For attaching ad-hoc properties
    // e.g. Worst warning for map
    @Transient
    private Map<String, Object> properties;
    
    public PointOfInterest() {
    }

    public PointOfInterest(Integer pointOfInterestId) {
        this.pointOfInterestId = pointOfInterestId;
    }

    public Integer getPointOfInterestId() {
        return pointOfInterestId;
    }

    public void setPointOfInterestId(Integer pointOfInterestId) {
        this.pointOfInterestId = pointOfInterestId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    
    public Country getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(Country countryCode) {
        this.countryCode = countryCode;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (pointOfInterestId != null ? pointOfInterestId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PointOfInterest)) {
            return false;
        }
        PointOfInterest other = (PointOfInterest) object;
        if ((this.pointOfInterestId == null && other.pointOfInterestId != null) || (this.pointOfInterestId != null && !this.pointOfInterestId.equals(other.pointOfInterestId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.PointOfInterest[ pointOfInterestId=" + pointOfInterestId + " ]";
    }

    /**
     * @return the pointOfInterestType
     *
    public PointOfInterestType getPointOfInterestType() {
        return pointOfInterestType;
    }

    /**
     * @param pointOfInterestType the pointOfInterestType to set
     *
    public void setPointOfInterestType(PointOfInterestType pointOfInterestType) {
        this.pointOfInterestType = pointOfInterestType;
    }*/

    @Override
    public int compareTo(Object t) {
        return this.compareTo((PointOfInterest) t);
    }
    
    /**
     * We sort alphabetically by name
     * @param other
     * @return 
     */
    public int compareTo(PointOfInterest other)
    {
        return this.getName().compareToIgnoreCase(other.getName());
    }

    

    /**
     * @return the timeZone
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * @param timeZone the timeZone to set
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * @return the properties
     */
    public Map<String,Object> getProperties() {
        if(this.properties == null)
        {
            this.properties = new HashMap<>();
        }
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(Map<String,Object> properties) {
        this.properties = properties;
    }

    /**
     * @return the userId
     */
    @JsonIgnore
    public VipsLogicUser getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(VipsLogicUser userId) {
        this.userId = userId;
    }

    /**
     * @return the weatherForecastProviderId
     */
    public WeatherForecastProvider getWeatherForecastProviderId() {
        return weatherForecastProviderId;
    }

    /**
     * @param weatherForecastProviderId the weatherForecastProviderId to set
     */
    public void setWeatherForecastProviderId(WeatherForecastProvider weatherForecastProviderId) {
        this.weatherForecastProviderId = weatherForecastProviderId;
    }

    //@XmlTransient
    public Set<PointOfInterestExternalResource> getPointOfInterestExternalResourceSet() {
        return pointOfInterestExternalResourceSet;
    }

    public void setPointOfInterestExternalResourceSet(Set<PointOfInterestExternalResource> pointOfInterestExternalResourceSet) {
        this.pointOfInterestExternalResourceSet = pointOfInterestExternalResourceSet;
    }

    
}
