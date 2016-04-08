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

package no.nibio.vips.logic.modules.applefruitmoth;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Embeddable
public class ObservationSitePointSeasonDataPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "observation_site_point_id")
    private int observationSitePointId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "season")
    private int season;

    public ObservationSitePointSeasonDataPK() {
    }

    public ObservationSitePointSeasonDataPK(int observationSitePointId, int season) {
        this.observationSitePointId = observationSitePointId;
        this.season = season;
    }

    public int getObservationSitePointId() {
        return observationSitePointId;
    }

    public void setObservationSitePointId(int observationSitePointId) {
        this.observationSitePointId = observationSitePointId;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) observationSitePointId;
        hash += (int) season;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ObservationSitePointSeasonDataPK)) {
            return false;
        }
        ObservationSitePointSeasonDataPK other = (ObservationSitePointSeasonDataPK) object;
        if (this.observationSitePointId != other.observationSitePointId) {
            return false;
        }
        if (this.season != other.season) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.modules.applefruitmoth.ObservationSitePointSeasonDataPK[ observationSitePointId=" + observationSitePointId + ", season=" + season + " ]";
    }

}
