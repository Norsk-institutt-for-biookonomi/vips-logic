package no.bioforsk.vips.logic.scheduling.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
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
        return "no.bioforsk.vips.logic.scheduling.model.ForecastModelConfigurationPK[ forecastConfigurationId=" + forecastConfigurationId + ", modelConfigParameter=" + modelConfigParameter + " ]";
    }

}
