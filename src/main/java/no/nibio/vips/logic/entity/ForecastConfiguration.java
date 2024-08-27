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
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.TimeZone;
import javax.persistence.Transient;
import no.nibio.vips.logic.util.IntegerArrayUserType;
import no.nibio.vips.util.WeatherUtil;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

/**
 * @copyright 2014-2024 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "forecast_configuration")
@XmlRootElement
@TypeDefs( {@TypeDef( name= "IntegerArray", typeClass = IntegerArrayUserType.class)})
@NamedQueries({
    @NamedQuery(name = "ForecastConfiguration.findAll", query = "SELECT f FROM ForecastConfiguration f WHERE f.isPrivate = FALSE"),
    @NamedQuery(name = "ForecastConfiguration.findByForecastConfigurationId", query = "SELECT f FROM ForecastConfiguration f WHERE f.forecastConfigurationId = :forecastConfigurationId"),
    @NamedQuery(name = "ForecastConfiguration.findByForecastConfigurationIds", query = "SELECT f FROM ForecastConfiguration f WHERE f.forecastConfigurationId IN(:forecastConfigurationIds)"),
    @NamedQuery(name = "ForecastConfiguration.findByModelId", query = "SELECT f FROM ForecastConfiguration f WHERE f.modelId = :modelId AND f.isPrivate = FALSE"),
    @NamedQuery(name = "ForecastConfiguration.findByModelIds", query = "SELECT f FROM ForecastConfiguration f WHERE f.modelId IN (:modelIds) AND f.isPrivate = FALSE"),
    @NamedQuery(name = "ForecastConfiguration.findByDateStart", query = "SELECT f FROM ForecastConfiguration f WHERE f.dateStart = :dateStart AND f.isPrivate = FALSE"),
    @NamedQuery(name = "ForecastConfiguration.findByDateEnd", query = "SELECT f FROM ForecastConfiguration f WHERE f.dateEnd = :dateEnd AND f.isPrivate = FALSE"),
    @NamedQuery(name = "ForecastConfiguration.findActiveAtDate", query = "SELECT f FROM ForecastConfiguration f WHERE f.dateStart <= :currentDate AND f.dateEnd >= :currentDate AND f.isPrivate = FALSE"),
    @NamedQuery(name = "ForecastConfiguration.findAllActiveAtDate", query = "SELECT f FROM ForecastConfiguration f WHERE f.dateStart <= :currentDate AND f.dateEnd >= :currentDate"),
    @NamedQuery(name = "ForecastConfiguration.findByLocationPointOfInterestId", query = "SELECT f FROM ForecastConfiguration f WHERE f.locationPointOfInterestId = :locationPointOfInterestId AND f.isPrivate = FALSE"),
    @NamedQuery(name = "ForecastConfiguration.findByWeatherStationPointOfInterestId", query = "SELECT f FROM ForecastConfiguration f WHERE f.weatherStationPointOfInterestId = :weatherStationPointOfInterestId AND f.isPrivate = FALSE"),
    @NamedQuery(name = "ForecastConfiguration.findByWeatherStationPointOfInterestIdAndDate", query = "SELECT f FROM ForecastConfiguration f WHERE f.weatherStationPointOfInterestId = :weatherStationPointOfInterestId AND f.dateStart <= :to AND f.dateEnd >= :from AND f.isPrivate = FALSE"),
    @NamedQuery(name = "ForecastConfiguration.findByVipsLogicUserId", query = "SELECT f FROM ForecastConfiguration f WHERE f.vipsLogicUserId = :vipsLogicUserId AND f.isPrivate = FALSE"),
    @NamedQuery(name = "ForecastConfiguration.findByVipsLogicUserIdAndDate", query = "SELECT f FROM ForecastConfiguration f WHERE f.vipsLogicUserId = :vipsLogicUserId AND f.dateStart <= :to AND f.dateEnd >= :from AND f.isPrivate = FALSE" ),
    @NamedQuery(name = "ForecastConfiguration.findByVipsLogicUserIdAndCropOrganismId", query = "SELECT f FROM ForecastConfiguration f WHERE f.vipsLogicUserId = :vipsLogicUserId AND f.cropOrganismId.organismId IN (:cropOrganismIds) AND f.isPrivate = FALSE"),
    @NamedQuery(name = "ForecastConfiguration.findByVipsLogicUserIdAndCropOrganismIdsAndDate", query = "SELECT f FROM ForecastConfiguration f WHERE f.vipsLogicUserId = :vipsLogicUserId AND f.cropOrganismId.organismId IN (:cropOrganismIds) AND f.dateStart <= :to AND f.dateEnd >= :from AND f.isPrivate = FALSE"),
    @NamedQuery(name = "ForecastConfiguration.findPrivateByVipsLogicUserId", query = "SELECT f FROM ForecastConfiguration f WHERE f.vipsLogicUserId = :vipsLogicUserId AND f.isPrivate = TRUE"),
    @NamedQuery(name = "ForecastConfiguration.findPrivateByVipsLogicUserIdAndDate", query = "SELECT f FROM ForecastConfiguration f WHERE f.vipsLogicUserId = :vipsLogicUserId AND f.dateStart <= :to AND f.dateEnd >= :from AND f.isPrivate = TRUE" ),
    @NamedQuery(name = "ForecastConfiguration.findPrivateByVipsLogicUserIdAndCropOrganismId", query = "SELECT f FROM ForecastConfiguration f WHERE f.vipsLogicUserId = :vipsLogicUserId AND f.cropOrganismId.organismId IN (:cropOrganismIds) AND f.isPrivate = TRUE"),
    @NamedQuery(name = "ForecastConfiguration.findPrivateByVipsLogicUserIdAndCropOrganismIdsAndDate", query = "SELECT f FROM ForecastConfiguration f WHERE f.vipsLogicUserId = :vipsLogicUserId AND f.cropOrganismId.organismId IN (:cropOrganismIds) AND f.dateStart <= :to AND f.dateEnd >= :from AND f.isPrivate = TRUE"),
    @NamedQuery(name = "ForecastConfiguration.findByVipsLogicUserIds", query = "SELECT f FROM ForecastConfiguration f WHERE f.vipsLogicUserId IN (:vipsLogicUserIds) AND f.isPrivate = FALSE"),
    @NamedQuery(name = "ForecastConfiguration.findByVipsLogicUserIdsAndModelIds", query = "SELECT f FROM ForecastConfiguration f WHERE f.vipsLogicUserId IN (:vipsLogicUserIds) AND f.modelId IN (:modelIds) AND f.isPrivate = FALSE"),
    @NamedQuery(name = "ForecastConfiguration.findByVipsLogicUserIdsAndModelIdsAndDate", query = "SELECT f FROM ForecastConfiguration f WHERE f.vipsLogicUserId IN (:vipsLogicUserIds) AND f.modelId IN (:modelIds) AND f.dateStart <= :to AND f.dateEnd >= :from AND f.isPrivate = FALSE"),
    @NamedQuery(name = "ForecastConfiguration.findByVipsLogicUserIdsAndDate", query = "SELECT f FROM ForecastConfiguration f WHERE f.vipsLogicUserId IN (:vipsLogicUserIds) AND f.dateStart <= :to AND f.dateEnd >= :from AND f.isPrivate = FALSE"),
    @NamedQuery(name = "ForecastConfiguration.findByModelIdAndYear", query = "SELECT f FROM ForecastConfiguration f WHERE f.modelId = :modelId AND YEAR(f.dateStart) <= :year AND YEAR(f.dateEnd) >= :year AND f.isPrivate = FALSE")
})
public class ForecastConfiguration implements Serializable, Comparable {
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "forecastConfiguration", fetch = FetchType.EAGER)
    private Set<ForecastModelConfiguration> forecastModelConfigurationSet;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "forecast_configuration_id")
    private Long forecastConfigurationId;
    @Size(max = 10)
    @Column(name = "model_id")
    private String modelId;
    @Column(name = "date_start")
    @Temporal(TemporalType.DATE)
    private Date dateStart;
    @Column(name = "date_end")
    @Temporal(TemporalType.DATE)
    private Date dateEnd;
    @Size(max = 255)
    @Column(name = "time_zone")
    private String timeZone;
    @JsonIgnore
    @JoinColumn(name = "vips_logic_user_id", referencedColumnName = "user_id")
    @ManyToOne(optional = false)
    private VipsLogicUser vipsLogicUserId;
    @JoinColumn(name = "location_point_of_interest_id", referencedColumnName = "point_of_interest_id")
    @ManyToOne
    private PointOfInterest locationPointOfInterestId;
    @JoinColumn(name = "weather_station_point_of_interest_id", referencedColumnName = "point_of_interest_id")
    @ManyToOne()
    private PointOfInterest weatherStationPointOfInterestId;
    @JoinColumn(name = "crop_organism_id", referencedColumnName = "organism_id")
    @ManyToOne
    private Organism cropOrganismId;
    @JoinColumn(name = "pest_organism_id", referencedColumnName = "organism_id")
    @ManyToOne
    private Organism pestOrganismId;
    @Column(name = "is_private")
    private Boolean isPrivate;
    @Column(name = "use_grid_weather_data")
    private Boolean useGridWeatherData;
    
    public Boolean getUseGridWeatherData() {
        return useGridWeatherData;
    }

    public void setUseGridWeatherData(Boolean useGridWeatherData) {
        this.useGridWeatherData = useGridWeatherData;
    }

    @Type(type = "IntegerArray")
    @Column(name = "grid_weather_station_point_of_interest_ids")
    private Integer[] gridWeatherStationPointOfInterestIds;
    
    @Transient
    private WeatherUtil weatherUtil;
    
    // Relations connected manually
    @Transient
    private List<ForecastSummary> forecastSummaries;

    public ForecastConfiguration() {
        
    }

    public ForecastConfiguration(Long forecastConfigurationId) {
        this.forecastConfigurationId = forecastConfigurationId;
    }

    public Long getForecastConfigurationId() {
        return forecastConfigurationId;
    }

    public void setForecastConfigurationId(Long forecastConfigurationId) {
        this.forecastConfigurationId = forecastConfigurationId;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public void setDateStart(Date dateStart) {
        this.dateStart = dateStart;
    }

    public Date getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(Date dateEnd) {
        this.dateEnd = dateEnd;
    }

    public VipsLogicUser getVipsLogicUserId() {
        return vipsLogicUserId;
    }

    public void setVipsCoreUserId(VipsLogicUser vipsLogicUserId) {
        this.vipsLogicUserId = vipsLogicUserId;
    }

    public PointOfInterest getLocationPointOfInterestId() {
        return locationPointOfInterestId;
    }

    public void setLocationPointOfInterestId(PointOfInterest locationPointOfInterestId) {
        this.locationPointOfInterestId = locationPointOfInterestId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (forecastConfigurationId != null ? forecastConfigurationId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ForecastConfiguration)) {
            return false;
        }
        ForecastConfiguration other = (ForecastConfiguration) object;
        if ((this.forecastConfigurationId == null && other.forecastConfigurationId != null) || (this.forecastConfigurationId != null && !this.forecastConfigurationId.equals(other.forecastConfigurationId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.scheduling.model.ForecastConfiguration[ forecastConfigurationId=" + forecastConfigurationId + " ]";
    }

    @XmlTransient
    @JsonIgnore
    public Set<ForecastModelConfiguration> getForecastModelConfigurationSet() {
        return forecastModelConfigurationSet;
    }

    public void setForecastModelConfigurationSet(Set<ForecastModelConfiguration> forecastModelConfigurationSet) {
        this.forecastModelConfigurationSet = forecastModelConfigurationSet;
    }
    
    /**
     * Util method to get a specific parameter value
     * @param modelConfigParameter
     * @return 
     */
    public String getForecastModelConfigurationValue(String modelConfigParameter)
    {
        for(ForecastModelConfiguration conf:this.getForecastModelConfigurationSet())
        {
            if(conf.getForecastModelConfigurationPK().getModelConfigParameter().equals(modelConfigParameter))
            {
                return conf.getParameterValue();
            }
        }
        return null;
    }

    /**
     * @return the weatherStationPointOfInterestId
     */
    public PointOfInterest getWeatherStationPointOfInterestId() {
        return weatherStationPointOfInterestId;
    }

    /**
     * @param weatherStationPointOfInterestId the weatherStationPointOfInterestId to set
     */
    public void setWeatherStationPointOfInterestId(PointOfInterest weatherStationPointOfInterestId) {
        this.weatherStationPointOfInterestId = weatherStationPointOfInterestId;
    }

    @Override
    public int compareTo(Object t) {
        return this.compareTo((ForecastConfiguration) t);
    }

    /**
     * Sort by 1)ModelId, then 2) Location name
     * @param other the forecast to compare with
     * @return 
     */
    public int compareTo(ForecastConfiguration other)
    {
        if(this.getModelId().compareTo(other.getModelId()) != 0)
        {
            return this.getModelId().compareTo(other.getModelId());
        }
        else
        {
            return this.getLocationPointOfInterestId().getName().compareTo(other.getLocationPointOfInterestId().getName());
        }
    }

    /**
     * @return the organismiId
     */
    public Organism getCropOrganismId() {
        return cropOrganismId;
    }

    /**
     * @param organismiId the organismiId to set
     */
    public void setCropOrganismId(Organism cropOrganismId) {
        this.cropOrganismId = cropOrganismId;
    }

    /**
     * @return the forecastSummaries
     */
    public List<ForecastSummary> getForecastSummaries() {
        return forecastSummaries;
    }

    /**
     * @param forecastSummaries the forecastSummaries to set
     */
    public void setForecastSummaries(List<ForecastSummary> forecastSummaries) {
        this.forecastSummaries = forecastSummaries;
    }

    /**
     * @return the pestOrganismId
     */
    public Organism getPestOrganismId() {
        return pestOrganismId;
    }

    /**
     * @param pestOrganismId the pestOrganismId to set
     */
    public void setPestOrganismId(Organism pestOrganismId) {
        this.pestOrganismId = pestOrganismId;
    }
    
    @Transient
    private WeatherUtil getWeatherUtil()
    {
        if(this.weatherUtil == null)
        {
            this.weatherUtil = new WeatherUtil();
        }
        return this.weatherUtil;
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
    
    @Transient
    public Date getDateStartInTimeZone()
    {
        return this.getDateInTimeZone(this.getDateStart());
    }
    
    @Transient
    public Date getDateEndInTimeZone()
    {
        return this.getDateInTimeZone(this.getDateEnd());
    }
    
    @Transient
    public Date getDateInTimeZone(Date theDate)
    {
        return this.getWeatherUtil().changeDateTimeZone(
                theDate, 
                TimeZone.getDefault(), 
                this.getTimeZone() != null ? TimeZone.getTimeZone(this.getTimeZone()) : TimeZone.getDefault()
        );
    }
    
    /*
    
    @Transient
    public String getISOFormattedDateStart()
    {
        TimeZone timeZone1 = this.getTimeZone() != null ? TimeZone.getTimeZone(this.getTimeZone())
                : TimeZone.getDefault();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(timeZone1);
        return format.format(this.getDateStart());
    }
    
    @Transient
    public String getISOFormattedDateEnd()
    {
        TimeZone timeZone1 = this.getTimeZone() != null ? TimeZone.getTimeZone(this.getTimeZone())
                : TimeZone.getDefault();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(timeZone1);
        return format.format(this.getDateEnd());
    }*/

    /**
     * @return the isPrivate
     */
    public Boolean getIsPrivate() {
        return isPrivate != null ? isPrivate : false;
    }

    /**
     * @param isPrivate the isPrivate to set
     */
    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    /**
     * @return the gridWeatherStationPointOfInterestIds
     */
    public Integer[] getGridWeatherStationPointOfInterestIds() {
        return gridWeatherStationPointOfInterestIds;
    }

    /**
     * @param gridWeatherStationPointOfInterestIds the gridWeatherStationPointOfInterestIds to set
     */
    public void setGridWeatherStationPointOfInterestIds(Integer[] gridWeatherStationPointOfInterestIds) {
        this.gridWeatherStationPointOfInterestIds = gridWeatherStationPointOfInterestIds;
    }
}
