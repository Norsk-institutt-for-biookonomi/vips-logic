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
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@Table(name = "forecast_model_configuration")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ForecastModelConfiguration.findAll", query = "SELECT f FROM ForecastModelConfiguration f"),
    @NamedQuery(name = "ForecastModelConfiguration.findByForecastConfigurationId", query = "SELECT f FROM ForecastModelConfiguration f WHERE f.forecastModelConfigurationPK.forecastConfigurationId = :forecastConfigurationId"),
    @NamedQuery(name = "ForecastModelConfiguration.findByModelConfigParameter", query = "SELECT f FROM ForecastModelConfiguration f WHERE f.forecastModelConfigurationPK.modelConfigParameter = :modelConfigParameter"),
    @NamedQuery(name = "ForecastModelConfiguration.findByParameterValue", query = "SELECT f FROM ForecastModelConfiguration f WHERE f.parameterValue = :parameterValue")})
public class ForecastModelConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected ForecastModelConfigurationPK forecastModelConfigurationPK;
    @Size(max = 1024)
    @Column(name = "parameter_value")
    private String parameterValue;
    @JoinColumn(name = "forecast_configuration_id", referencedColumnName = "forecast_configuration_id", insertable = false, updatable = false)
    @ManyToOne(optional = false, fetch=FetchType.LAZY)
    //@ManyToOne(optional = false)
    private ForecastConfiguration forecastConfiguration;

    public ForecastModelConfiguration() {
    }

    public ForecastModelConfiguration(ForecastModelConfigurationPK forecastModelConfigurationPK) {
        this.forecastModelConfigurationPK = forecastModelConfigurationPK;
    }

    public ForecastModelConfiguration(long forecastConfigurationId, String modelConfigParameter) {
        this.forecastModelConfigurationPK = new ForecastModelConfigurationPK(forecastConfigurationId, modelConfigParameter);
    }

    public ForecastModelConfigurationPK getForecastModelConfigurationPK() {
        return forecastModelConfigurationPK;
    }

    public void setForecastModelConfigurationPK(ForecastModelConfigurationPK forecastModelConfigurationPK) {
        this.forecastModelConfigurationPK = forecastModelConfigurationPK;
    }

    public String getParameterValue() {
        return parameterValue;
    }

    public void setParameterValue(String parameterValue) {
        this.parameterValue = parameterValue;
    }

    @JsonIgnore
    public ForecastConfiguration getForecastConfiguration() {
        return forecastConfiguration;
    }

    public void setForecastConfiguration(ForecastConfiguration forecastConfiguration) {
        this.forecastConfiguration = forecastConfiguration;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (forecastModelConfigurationPK != null ? forecastModelConfigurationPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ForecastModelConfiguration)) {
            return false;
        }
        ForecastModelConfiguration other = (ForecastModelConfiguration) object;
        if ((this.forecastModelConfigurationPK == null && other.forecastModelConfigurationPK != null) || (this.forecastModelConfigurationPK != null && !this.forecastModelConfigurationPK.equals(other.forecastModelConfigurationPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.scheduling.model.ForecastModelConfiguration[ forecastModelConfigurationPK=" + forecastModelConfigurationPK + " ]";
    }

}
