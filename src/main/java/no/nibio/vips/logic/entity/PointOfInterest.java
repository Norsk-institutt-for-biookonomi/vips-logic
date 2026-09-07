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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.locationtech.jts.geom.Geometry;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import no.nibio.vips.gis.GISUtil;
import org.wololo.geojson.Feature;

/**
 * @copyright 2017 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "point_of_interest")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="point_of_interest_type_id", discriminatorType=DiscriminatorType.INTEGER)
@DiscriminatorValue("0")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PointOfInterest.findAll", query = "SELECT p FROM PointOfInterest p ORDER BY p.name ASC"),
    @NamedQuery(name = "PointOfInterest.findByPointOfInterestId", query = "SELECT p FROM PointOfInterest p WHERE p.pointOfInterestId = :pointOfInterestId"),
    @NamedQuery(name = "PointOfInterest.findByPointOfInterestIds", query = "SELECT p FROM PointOfInterest p WHERE p.pointOfInterestId IN :pointOfInterestIds"),
    @NamedQuery(name = "PointOfInterest.findByOrganizationId", query = "SELECT p FROM PointOfInterest p WHERE p.user.userId IN(SELECT u.userId FROM VipsLogicUser u WHERE u.organizationId = :organizationId OR u.organizationId.organizationId IN (SELECT o.organizationId FROM Organization o WHERE o.parentOrganizationId = :organizationId))  ORDER BY p.name ASC"),
    @NamedQuery(name = "PointOfInterest.findByOrganizationIdAndPoiTypes", query = "SELECT p FROM PointOfInterest p WHERE p.pointOfInterestTypeId in :pointOfInterestTypes AND p.user.userId IN(SELECT u.userId FROM VipsLogicUser u WHERE u.organizationId = :organizationId OR u.organizationId.organizationId IN (SELECT o.organizationId FROM Organization o WHERE o.parentOrganizationId = :organizationId))  ORDER BY p.name ASC"),
    @NamedQuery(name = "PointOfInterest.findForecastLocationsByOrganizationId", query = "SELECT p FROM PointOfInterest p WHERE p.isForecastLocation IS TRUE AND p.user.userId IN(SELECT u.userId FROM VipsLogicUser u WHERE u.organizationId = :organizationId OR u.organizationId.organizationId IN (SELECT o.organizationId FROM Organization o WHERE o.parentOrganizationId = :organizationId))  ORDER BY p.name ASC"),
    @NamedQuery(name = "PointOfInterest.findByName", query = "SELECT p FROM PointOfInterest p WHERE p.name = :name"),
    @NamedQuery(name = "PointOfInterest.findByNameCaseInsensitive", query = "SELECT p FROM PointOfInterest p WHERE lower(p.name) = lower(:name)"),
    @NamedQuery(name = "PointOfInterest.findByUserId", query = "SELECT p FROM PointOfInterest p WHERE p.user = :userId ORDER BY p.name ASC")
})
public class PointOfInterest implements Serializable, Comparable {
    private Set<PointOfInterestExternalResource> pointOfInterestExternalResourceSet;

    private static final long serialVersionUID = 1L;
    private Integer pointOfInterestId;
    @Size(max = 255)
    private String name;
    @Size(max = 255)
    private String timeZone;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    private Double longitude;
    private Double latitude;
    private Double altitude;
    @JsonIgnore
    private Geometry gisGeom;
    private VipsLogicUser user;
    private Country countryCode;
    private WeatherForecastProvider weatherForecastProviderId;
    private Boolean isForecastLocation;
    private Integer pointOfInterestTypeId;
    private Date lastEditedTime;
    private Boolean isPrivate;
   
    @Column(name = "is_private")
    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    // For attaching ad-hoc properties
    // e.g. Worst warning for map
    @Transient
    private Map<String, Object> properties;
    
    private GISUtil gisUtil;
    
    public PointOfInterest() {
        this.gisUtil = new GISUtil();
    }

    public PointOfInterest(Integer pointOfInterestId) {
        this.pointOfInterestId = pointOfInterestId;
    }
    
    public static PointOfInterest getInstance(Integer typeId)
    {
    	PointOfInterest instance;
    	
    	switch(typeId)
    	{
    		case 1: instance = new PointOfInterestWeatherStation();
    				break;
    		case 2: instance = new PointOfInterestTypeFarm();
    				break;
    		case 3: instance = new PointOfInterestTypeField();
    				break;
    		case 4: instance = new PointOfInterestTypeRegion();
    				break;
            case 5: instance = new PointOfInterestTypeTrap();
                break;
    		default: instance = null;
    				break;
    	}
    	return instance;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "point_of_interest_id", insertable = false, updatable = false)
    public Integer getPointOfInterestId() {
        return pointOfInterestId;
    }

    public void setPointOfInterestId(Integer pointOfInterestId) {
        this.pointOfInterestId = pointOfInterestId;
    }

    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "longitude")
    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Column(name = "latitude")
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @Column(name = "altitude")
    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    
    @JoinColumn(name = "country_code", referencedColumnName = "country_code")
    @ManyToOne
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
    @Column(name = "time_zone")
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
    @Transient
    public Map<String,Object> getProperties() {
        if(this.properties == null)
        {
            this.properties = new HashMap<>();
        }
        if(this.properties.get("pointOfInterestId") == null)
        {
            this.properties.put("pointOfInterestId", this.getPointOfInterestId());
        }
        if(this.properties.get("pointOfInterestTypeId") == null)
        {
            this.properties.put("pointOfInterestTypeId", this.getPointOfInterestTypeId());
        }
        if(this.properties.get("pointOfInterestName") == null)
        {
            this.properties.put("pointOfInterestName", this.getName());
        }
        return this.properties;
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
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ManyToOne
    @JsonIgnore
    public VipsLogicUser getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(VipsLogicUser user) {
        this.user = user;
    }
    
    /**
     * For serialization
     * @return
     */
    @Transient
    public Integer getUserId()
    {
    	return this.getUser().getUserId();
    }

    /**
     * @return the weatherForecastProviderId
     */
    @JoinColumn(name = "weather_forecast_provider_id", referencedColumnName = "weather_forecast_provider_id")
    @ManyToOne
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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "pointOfInterest", fetch = FetchType.EAGER)
    public Set<PointOfInterestExternalResource> getPointOfInterestExternalResourceSet() {
        return pointOfInterestExternalResourceSet;
    }

    public void setPointOfInterestExternalResourceSet(Set<PointOfInterestExternalResource> pointOfInterestExternalResourceSet) {
        this.pointOfInterestExternalResourceSet = pointOfInterestExternalResourceSet;
    }

    /**
     * @return the pointOfInterestTypeId
     */
    @Column(name = "point_of_interest_type_id", insertable = false, updatable = false)
    public Integer getPointOfInterestTypeId() {
        return pointOfInterestTypeId;
    }

    /**
     * @param pointOfInterestTypeId the pointOfInterestTypeId to set
     */
    public void setPointOfInterestTypeId(Integer pointOfInterestTypeId) {
        this.pointOfInterestTypeId = pointOfInterestTypeId;
    }

    /**
     * @return the gisGeom
     */
    @Column(name = "gis_geom", columnDefinition = "Geometry")
    public Geometry getGisGeom() {
        return gisGeom;
    }

    @Transient
    public String getGeoJSON()
    {
        return this.gisUtil.getGeoJSONFromGeometry(this.getGisGeom(), this.getProperties());
    }
    
    public void addProperty(String key, Object value)
    {
        if(this.properties == null)
        {
            this.properties = new HashMap<>();
        }
        this.properties.put(key, value);
    }
    
    /**
     * @param gisGeom the gisGeom to set
     */
    public void setGisGeom(Geometry gisGeom) {
        this.gisGeom = gisGeom;
    }

    /**
     * @return the isForecastLocation
     */
    @Column(name = "is_forecast_location")
    public Boolean getIsForecastLocation() {
        return isForecastLocation;
    }

    /**
     * @param isForecastLocation the isForecastLocation to set
     */
    public void setIsForecastLocation(Boolean isForecastLocation) {
        this.isForecastLocation = isForecastLocation;
    }

    @Column(name = "last_edited_time")
	public Date getLastEditedTime() {
		return lastEditedTime;
	}

	public void setLastEditedTime(Date lastEditedTime) {
		this.lastEditedTime = lastEditedTime;
	}
}
