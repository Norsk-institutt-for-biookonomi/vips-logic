/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.messaging;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "forecast_notification_log", schema = "messaging")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ForecastNotificationLog.findAll", query = "SELECT f FROM ForecastNotificationLog f"),
    @NamedQuery(name = "ForecastNotificationLog.findByForecastConfigurationId", query = "SELECT f FROM ForecastNotificationLog f WHERE f.forecastNotificationLogPK.forecastConfigurationId = :forecastConfigurationId"),
    @NamedQuery(name = "ForecastNotificationLog.findByForecastEventId", query = "SELECT f FROM ForecastNotificationLog f WHERE f.forecastNotificationLogPK.forecastEventId = :forecastEventId"),
    @NamedQuery(name = "ForecastNotificationLog.findByEventDate", query = "SELECT f FROM ForecastNotificationLog f WHERE f.forecastNotificationLogPK.eventDate = :eventDate"),
    @NamedQuery(name = "ForecastNotificationLog.findByPK", query = "SELECT f FROM ForecastNotificationLog f WHERE f.forecastNotificationLogPK.eventDate = :eventDate AND f.forecastNotificationLogPK.forecastEventId = :forecastEventId AND f.forecastNotificationLogPK.forecastConfigurationId = :forecastConfigurationId"),
    @NamedQuery(name = "ForecastNotificationLog.findByCreatedTime", query = "SELECT f FROM ForecastNotificationLog f WHERE f.createdTime = :createdTime")})
public class ForecastNotificationLog implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected ForecastNotificationLogPK forecastNotificationLogPK;
    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;
    @Column(name = "universal_message_id")
    private Integer universalMessageId;
    

    public ForecastNotificationLog() {
    }

    public ForecastNotificationLog(ForecastNotificationLogPK forecastNotificationLogPK) {
        this.forecastNotificationLogPK = forecastNotificationLogPK;
    }

    public ForecastNotificationLog(Long forecastConfigurationId, int forecastEvent, Date eventDate) {
        this.forecastNotificationLogPK = new ForecastNotificationLogPK(forecastConfigurationId, forecastEvent, eventDate);
    }

    public ForecastNotificationLogPK getForecastNotificationLogPK() {
        return forecastNotificationLogPK;
    }

    public void setForecastNotificationLogPK(ForecastNotificationLogPK forecastNotificationLogPK) {
        this.forecastNotificationLogPK = forecastNotificationLogPK;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (forecastNotificationLogPK != null ? forecastNotificationLogPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ForecastNotificationLog)) {
            return false;
        }
        ForecastNotificationLog other = (ForecastNotificationLog) object;
        if ((this.forecastNotificationLogPK == null && other.forecastNotificationLogPK != null) || (this.forecastNotificationLogPK != null && !this.forecastNotificationLogPK.equals(other.forecastNotificationLogPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.messaging.ForecastNotificationLog[ forecastNotificationLogPK=" + forecastNotificationLogPK + " ]";
    }

    /**
     * @return the universalMessageId
     */
    public Integer getUniversalMessageId() {
        return universalMessageId;
    }

    /**
     * @param universalMessageId the universalMessageId to set
     */
    public void setUniversalMessageId(Integer universalMessageId) {
        this.universalMessageId = universalMessageId;
    }

}
