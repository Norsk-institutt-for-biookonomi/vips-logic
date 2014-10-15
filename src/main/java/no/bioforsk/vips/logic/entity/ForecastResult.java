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
import no.bioforsk.vips.entity.Result;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@Table(name = "forecast_result")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ForecastResult.findAll", query = "SELECT f FROM ForecastResult f ORDER BY f.resultValidTime ASC"),
    @NamedQuery(name = "ForecastResult.findByForecastResultId", query = "SELECT f FROM ForecastResult f WHERE f.forecastResultId = :forecastResultId ORDER BY f.resultValidTime ASC"),
    @NamedQuery(name = "ForecastResult.findByForecastConfigurationId", query = "SELECT f FROM ForecastResult f WHERE f.forecastConfigurationId = :forecastConfigurationId ORDER BY f.resultValidTime ASC"),
    @NamedQuery(name = "ForecastResult.findByResultValidTime", query = "SELECT f FROM ForecastResult f WHERE f.resultValidTime = :resultValidTime ORDER BY f.resultValidTime ASC"),
    @NamedQuery(name = "ForecastResult.findByWarningStatus", query = "SELECT f FROM ForecastResult f WHERE f.warningStatus = :warningStatus ORDER BY f.resultValidTime ASC")})
public class ForecastResult implements Serializable, Comparable{
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "forecast_result_id")
    private Long forecastResultId;
    @Column(name = "result_valid_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date resultValidTime;
    @Column(name = "warning_status")
    private Integer warningStatus;
    @Column(name = "all_values")
    private String allValues;
    //@ManyToOne(fetch = FetchType.LAZY,optional=true)
    //@JoinColumn(name="forecast_configuration_id",referencedColumnName="forecast_configuration_id")    
    @Column(name = "forecast_configuration_id")
    private Long forecastConfigurationId;

    
    public ForecastResult() {
    }
    
    public ForecastResult(Long forecastConfigurationId, Result result){
        try {
            this.forecastConfigurationId = forecastConfigurationId;
            this.allValues = new ObjectMapper().writeValueAsString(result.getAllValues());
            this.resultValidTime = result.getResultValidTime();
            this.warningStatus = result.getWarningStatus();
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

    public Date getResultValidTime() {
        return resultValidTime;
    }

    public void setResultValidTime(Date resultValidTime) {
        this.resultValidTime = resultValidTime;
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
        return "no.bioforsk.vips.logic.entity.ForecastResult[ forecastResultId=" + forecastResultId + " ]";
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
        return this.getResultValidTime().compareTo(other.getResultValidTime());
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
    
}
