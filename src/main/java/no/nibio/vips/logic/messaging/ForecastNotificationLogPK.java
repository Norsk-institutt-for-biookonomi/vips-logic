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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Embeddable
public class ForecastNotificationLogPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "forecast_configuration_id")
    private Long forecastConfigurationId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "forecast_event_id")
    private int forecastEventId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "event_date")
    @Temporal(TemporalType.DATE)
    private Date eventDate;

    public ForecastNotificationLogPK() {
    }

    public ForecastNotificationLogPK(Long forecastConfigurationId, int forecastEventId, Date eventDate) {
        this.forecastConfigurationId = forecastConfigurationId;
        this.forecastEventId = forecastEventId;
        this.eventDate = eventDate;
    }

    public Long getForecastConfigurationId() {
        return forecastConfigurationId;
    }

    public void setForecastConfigurationId(Long forecastConfigurationId) {
        this.forecastConfigurationId = forecastConfigurationId;
    }

    public int getForecastEventId() {
        return forecastEventId;
    }

    public void setForecastEventId(int forecastEventId) {
        this.forecastEventId = forecastEventId;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (long) forecastConfigurationId;
        hash += (int) forecastEventId;
        hash += (eventDate != null ? eventDate.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ForecastNotificationLogPK)) {
            return false;
        }
        ForecastNotificationLogPK other = (ForecastNotificationLogPK) object;
        if (this.forecastConfigurationId != other.forecastConfigurationId) {
            return false;
        }
        if (this.forecastEventId != other.forecastEventId) {
            return false;
        }
        if ((this.eventDate == null && other.eventDate != null) || (this.eventDate != null && !this.eventDate.equals(other.eventDate))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.messaging.ForecastNotificationLogPK[ forecastConfigurationId=" + forecastConfigurationId + ", forecastEvent=" + forecastEventId + ", eventDate=" + eventDate + " ]";
    }

}
