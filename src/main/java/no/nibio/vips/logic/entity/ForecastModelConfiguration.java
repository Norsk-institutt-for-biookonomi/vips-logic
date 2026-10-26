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
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
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
        return "no.nibio.vips.logic.scheduling.model.ForecastModelConfiguration[ forecastModelConfigurationPK=" + forecastModelConfigurationPK + " ]";
    }

}
