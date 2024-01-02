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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Embeddable
public class ForecastModelConfigurationPK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "forecast_configuration_id")
    private long forecastConfigurationId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "model_config_parameter")
    private String modelConfigParameter;

    public ForecastModelConfigurationPK() {
    }

    public ForecastModelConfigurationPK(long forecastConfigurationId, String modelConfigParameter) {
        this.forecastConfigurationId = forecastConfigurationId;
        this.modelConfigParameter = modelConfigParameter;
    }

    public long getForecastConfigurationId() {
        return forecastConfigurationId;
    }

    public void setForecastConfigurationId(long forecastConfigurationId) {
        this.forecastConfigurationId = forecastConfigurationId;
    }

    public String getModelConfigParameter() {
        return modelConfigParameter;
    }

    public void setModelConfigParameter(String modelConfigParameter) {
        this.modelConfigParameter = modelConfigParameter;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) forecastConfigurationId;
        hash += (modelConfigParameter != null ? modelConfigParameter.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ForecastModelConfigurationPK)) {
            return false;
        }
        ForecastModelConfigurationPK other = (ForecastModelConfigurationPK) object;
        if (this.forecastConfigurationId != other.forecastConfigurationId) {
            return false;
        }
        return (this.modelConfigParameter != null || other.modelConfigParameter == null) && (this.modelConfigParameter == null || this.modelConfigParameter.equals(other.modelConfigParameter));
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.scheduling.model.ForecastModelConfigurationPK[ forecastConfigurationId=" + forecastConfigurationId + ", modelConfigParameter=" + modelConfigParameter + " ]";
    }

}
