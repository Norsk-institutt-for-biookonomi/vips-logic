/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.messaging;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "forecast_event", schema = "messaging")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ForecastEvent.findAll", query = "SELECT f FROM ForecastEvent f"),
    @NamedQuery(name = "ForecastEvent.findByForecastEventId", query = "SELECT f FROM ForecastEvent f WHERE f.forecastEventId = :forecastEventId"),
    @NamedQuery(name = "ForecastEvent.findByEventName", query = "SELECT f FROM ForecastEvent f WHERE f.eventName = :eventName"),
    @NamedQuery(name = "ForecastEvent.findByEventDescription", query = "SELECT f FROM ForecastEvent f WHERE f.eventDescription = :eventDescription")})
public class ForecastEvent implements Serializable {
    
    public final static Integer TO_RED = 1;
    public final static Integer GREEN_TO_YELLOW = 2;

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "forecast_event_id")
    private Integer forecastEventId;
    @Size(max = 255)
    @Column(name = "event_name")
    private String eventName;
    @Size(max = 2147483647)
    @Column(name = "event_description")
    private String eventDescription;
   

    public ForecastEvent() {
    }

    public ForecastEvent(Integer forecastEventId) {
        this.forecastEventId = forecastEventId;
    }

    public Integer getForecastEventId() {
        return forecastEventId;
    }

    public void setForecastEventId(Integer forecastEventId) {
        this.forecastEventId = forecastEventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (forecastEventId != null ? forecastEventId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ForecastEvent)) {
            return false;
        }
        ForecastEvent other = (ForecastEvent) object;
        if ((this.forecastEventId == null && other.forecastEventId != null) || (this.forecastEventId != null && !this.forecastEventId.equals(other.forecastEventId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.messaging.ForecastEvent[ forecastEventId=" + forecastEventId + " ]";
    }

}
