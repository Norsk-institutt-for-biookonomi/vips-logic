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
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "forecast_summary")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ForecastSummary.findAll", query = "SELECT f FROM ForecastSummary f"),
    @NamedQuery(name = "ForecastSummary.findByForecastConfigurationId", query = "SELECT f FROM ForecastSummary f WHERE f.forecastSummaryPK.forecastConfigurationId = :forecastConfigurationId"),
    @NamedQuery(name = "ForecastSummary.findByForecastConfigurationIds", query = "SELECT f FROM ForecastSummary f WHERE f.forecastSummaryPK.forecastConfigurationId IN(:forecastConfigurationIds)"),
    @NamedQuery(name = "ForecastSummary.findByOrganizationId", query = "SELECT f FROM ForecastSummary f WHERE f.forecastSummaryPK.forecastConfigurationId IN(SELECT f1.forecastConfigurationId FROM ForecastConfiguration f1 WHERE f1.vipsLogicUserId IN (SELECT v.userId FROM VipsLogicUser v WHERE v.organizationId=:organizationId))"),    
    @NamedQuery(name = "ForecastSummary.findBySummaryForDate", query = "SELECT f FROM ForecastSummary f WHERE f.forecastSummaryPK.summaryForDate = :summaryForDate"),
    @NamedQuery(name = "ForecastSummary.findBySummaryCreatedTime", query = "SELECT f FROM ForecastSummary f WHERE f.summaryCreatedTime = :summaryCreatedTime"),
    @NamedQuery(name = "ForecastSummary.findByWarningStatus", query = "SELECT f FROM ForecastSummary f WHERE f.warningStatus = :warningStatus")})
public class ForecastSummary implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected ForecastSummaryPK forecastSummaryPK;
    @Column(name = "summary_created_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date summaryCreatedTime;
    @Column(name = "warning_status")
    private Integer warningStatus;

    public ForecastSummary() {
    }

    public ForecastSummary(ForecastSummaryPK forecastSummaryPK) {
        this.forecastSummaryPK = forecastSummaryPK;
    }

    public ForecastSummary(Long forecastConfigurationId, Date summaryForDate) {
        this.forecastSummaryPK = new ForecastSummaryPK(forecastConfigurationId, summaryForDate);
    }

    public ForecastSummaryPK getForecastSummaryPK() {
        return forecastSummaryPK;
    }

    public void setForecastSummaryPK(ForecastSummaryPK forecastSummaryPK) {
        this.forecastSummaryPK = forecastSummaryPK;
    }

    public Date getSummaryCreatedTime() {
        return summaryCreatedTime;
    }

    public void setSummaryCreatedTime(Date summaryCreatedTime) {
        this.summaryCreatedTime = summaryCreatedTime;
    }

    public Integer getWarningStatus() {
        return warningStatus;
    }

    public void setWarningStatus(Integer warningStatus) {
        this.warningStatus = warningStatus;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (forecastSummaryPK != null ? forecastSummaryPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ForecastSummary)) {
            return false;
        }
        ForecastSummary other = (ForecastSummary) object;
        if ((this.forecastSummaryPK == null && other.forecastSummaryPK != null) || (this.forecastSummaryPK != null && !this.forecastSummaryPK.equals(other.forecastSummaryPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.ForecastSummaryCache[ forecastSummaryCachePK=" + forecastSummaryPK + " ]";
    }

}
