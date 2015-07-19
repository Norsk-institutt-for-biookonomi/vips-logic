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
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

/**
 * @copyright 2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Embeddable
public class ForecastSummaryPK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "forecast_configuration_id")
    private Long forecastConfigurationId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "summary_for_date")
    @Temporal(TemporalType.DATE)
    private Date summaryForDate;

    public ForecastSummaryPK() {
    }

    public ForecastSummaryPK(Long forecastConfigurationId, Date summaryForDate) {
        this.forecastConfigurationId = forecastConfigurationId;
        this.summaryForDate = summaryForDate;
    }

    public Long getForecastConfigurationId() {
        return forecastConfigurationId;
    }

    public void setForecastConfigurationId(Long forecastConfigurationId) {
        this.forecastConfigurationId = forecastConfigurationId;
    }

    public Date getSummaryForDate() {
        return summaryForDate;
    }

    public void setSummaryForDate(Date summaryForDate) {
        this.summaryForDate = summaryForDate;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += forecastConfigurationId;
        hash += (summaryForDate != null ? summaryForDate.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ForecastSummaryPK)) {
            return false;
        }
        ForecastSummaryPK other = (ForecastSummaryPK) object;
        if (this.forecastConfigurationId != other.forecastConfigurationId) {
            return false;
        }
        if ((this.summaryForDate == null && other.summaryForDate != null) || (this.summaryForDate != null && !this.summaryForDate.equals(other.summaryForDate))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.ForecastSummaryPK[ forecastConfigurationId=" + forecastConfigurationId + ", summaryForDate=" + summaryForDate + " ]";
    }

}
