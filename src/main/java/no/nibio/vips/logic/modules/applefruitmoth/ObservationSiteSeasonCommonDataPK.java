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
public class ObservationSiteSeasonCommonDataPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "observation_site_id")
    private int observationSiteId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "season")
    private int season;

    public ObservationSiteSeasonCommonDataPK() {
    }

    public ObservationSiteSeasonCommonDataPK(int observationSiteId, int season) {
        this.observationSiteId = observationSiteId;
        this.season = season;
    }

    public int getObservationSiteId() {
        return observationSiteId;
    }

    public void setObservationSiteId(int observationSiteId) {
        this.observationSiteId = observationSiteId;
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
        hash += (int) observationSiteId;
        hash += (int) season;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ObservationSiteSeasonCommonDataPK)) {
            return false;
        }
        ObservationSiteSeasonCommonDataPK other = (ObservationSiteSeasonCommonDataPK) object;
        if (this.observationSiteId != other.observationSiteId) {
            return false;
        }
        if (this.season != other.season) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.modules.applefruitmoth.ObservationSiteSeasonCommonDataPK[ observationSiteId=" + observationSiteId + ", season=" + season + " ]";
    }

}
