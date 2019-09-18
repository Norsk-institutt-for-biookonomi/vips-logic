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

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;
import no.nibio.vips.entity.Result;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import no.nibio.vips.logic.entity.helpers.ForecastResultSerializer;
import org.locationtech.jts.geom.Geometry;


/**
 * @copyright 2013-2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "forecast_result")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ForecastResult.findAll", query = "SELECT f FROM ForecastResult f ORDER BY f.validTimeStart ASC"),
    @NamedQuery(name = "ForecastResult.findByForecastResultId", query = "SELECT f FROM ForecastResult f WHERE f.forecastResultId = :forecastResultId ORDER BY f.validTimeStart ASC"),
    @NamedQuery(name = "ForecastResult.findByForecastConfigurationId", query = "SELECT f FROM ForecastResult f WHERE f.forecastConfigurationId = :forecastConfigurationId ORDER BY f.validTimeStart ASC"),
    @NamedQuery(name = "ForecastResult.findByForecastConfigurationIdAndPeriod", query = "SELECT f FROM ForecastResult f WHERE f.forecastConfigurationId = :forecastConfigurationId AND f.validTimeStart BETWEEN :timeStart AND :timeEnd ORDER BY f.validTimeStart ASC"),
    @NamedQuery(name = "ForecastResult.findByValidTimeStart", query = "SELECT f FROM ForecastResult f WHERE f.validTimeStart = :validTimeStart ORDER BY f.validTimeStart ASC"),
    @NamedQuery(name = "ForecastResult.findByWarningStatus", query = "SELECT f FROM ForecastResult f WHERE f.warningStatus = :warningStatus ORDER BY f.validTimeStart ASC")})
@JsonSerialize(using=ForecastResultSerializer.class)
public class ForecastResult implements Serializable, Comparable{
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "forecast_result_id")
    private Long forecastResultId;
    @Column(name = "valid_time_start")
    @Temporal(TemporalType.TIMESTAMP)
    private Date validTimeStart;
    @Column(name = "valid_time_end")
    @Temporal(TemporalType.TIMESTAMP)
    private Date validTimeEnd;
    @Column(name = "warning_status")
    private Integer warningStatus;
    @Column(name = "all_values")
    private String allValues;
    //@ManyToOne(fetch = FetchType.LAZY,optional=true)
    //@JoinColumn(name="forecast_configuration_id",referencedColumnName="forecast_configuration_id")    
    @Column(name = "forecast_configuration_id")
    private Long forecastConfigurationId;
    @Column(name = "valid_geometry", columnDefinition = "Geometry")
    private Geometry validGeometry;

    
    public ForecastResult() {
    }
    
    public ForecastResult(Long forecastConfigurationId, Result result){
        try {
            this.forecastConfigurationId = forecastConfigurationId;
            this.allValues = new ObjectMapper().writeValueAsString(result.getAllValues());
            this.validTimeStart = result.getValidTimeStart();
            this.warningStatus = result.getWarningStatus();
            this.validGeometry = result.getValidGeometry();
        } catch (IOException ex) {
            Logger.getLogger(ForecastResult.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ForecastResult(Long forecastResultId) {
        this.forecastResultId = forecastResultId;
    }

    public Long getForecastResultId() {
        return forecastResultId;
    }

    public void setForecastResultId(Long forecastResultId) {
        this.forecastResultId = forecastResultId;
    }

    public Date getValidTimeStart() {
        return validTimeStart;
    }

    public void setValidTimeStart(Date validTimeStart) {
        this.validTimeStart = validTimeStart;
    }

    public Integer getWarningStatus() {
        return warningStatus;
    }

    public void setWarningStatus(Integer warningStatus) {
        this.warningStatus = warningStatus;
    }

    @JsonIgnore
    public String getAllValues() {
        return allValues;
    }

    public void setAllValues(String allValues) {
        this.allValues = allValues;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (forecastResultId != null ? forecastResultId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ForecastResult)) {
            return false;
        }
        ForecastResult other = (ForecastResult) object;
        if ((this.forecastResultId == null && other.forecastResultId != null) || (this.forecastResultId != null && !this.forecastResultId.equals(other.forecastResultId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.ForecastResult[ forecastResultId=" + forecastResultId + " ]";
    }

    /**
     * Converting from JSON format
     * @return 
     */
    @Transient
    @JsonProperty("allValues")
    public Map<String,String> getAllValuesAsMap()
    {
        try {
            return new ObjectMapper().readValue(this.allValues, new TypeReference<HashMap<String,String>>(){});
        } catch (IOException ex) {
            Logger.getLogger(ForecastResult.class.getName()).log(Level.SEVERE, null, ex);
            return new HashMap<>();
        }
 
    }
    
    /**
     * Converting to JSON string
     * @param values 
     */
    @Transient 
    public void setAllValues(Map<String,String> values)
    {
        try {
            this.allValues = new ObjectMapper().writeValueAsString(values);
            System.out.println("this.allvalues=" + this.allValues);
        } catch (IOException ex) {
            Logger.getLogger(ForecastResult.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public int compareTo(Object t) {
        return this.compareTo((ForecastResult)t);
    }
    
    public int compareTo(ForecastResult other)
    {
        return this.getValidTimeStart().compareTo(other.getValidTimeStart());
    }

    /**
     * @return the forecastConfigurationId
     */
    public Long getForecastConfigurationId() {
        return forecastConfigurationId;
    }

    /**
     * @param forecastConfigurationId the forecastConfigurationId to set
     */
    public void setForecastConfigurationId(Long forecastConfigurationId) {
        this.forecastConfigurationId = forecastConfigurationId;
    }


    @Transient
    public Set<String> getKeys() {
        return this.getAllValuesAsMap().keySet();
    }

    /**
     * @return the validGeometry
     */
    public Geometry getValidGeometry() {
        return validGeometry;
    }

    /**
     * @param validGeometry the validGeometry to set
     */
    public void setValidGeometry(Geometry validGeometry) {
        this.validGeometry = validGeometry;
    }

    /**
     * @return the validTimeEnd
     */
    public Date getValidTimeEnd() {
        return validTimeEnd;
    }

    /**
     * @param validTimeEnd the validTimeEnd to set
     */
    public void setValidTimeEnd(Date validTimeEnd) {
        this.validTimeEnd = validTimeEnd;
    }
    
}
