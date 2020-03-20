/*
 * Copyright (c) 2019 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.modules.wheatleafblotch;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import no.nibio.vips.logic.entity.PointOfInterest;

/**
 * @copyright 2019 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Embeddable
public class YieldLossPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "season")
    private int season;
    @Basic(optional = false)
    @NotNull
    @JoinColumn(name = "point_of_interest_id", referencedColumnName = "point_of_interest_id")
    @ManyToOne
    private PointOfInterest pointOfInterestId;

    public YieldLossPK() {
    }

    public YieldLossPK(int season, PointOfInterest pointOfInterestId) {
        this.season = season;
        this.pointOfInterestId = pointOfInterestId;
    }

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }

    public PointOfInterest getPointOfInterestId() {
        return pointOfInterestId;
    }

    public void setPointOfInterestId(PointOfInterest pointOfInterestId) {
        this.pointOfInterestId = pointOfInterestId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) season;
        hash += (int) pointOfInterestId.getPointOfInterestId();
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof YieldLossPK)) {
            return false;
        }
        YieldLossPK other = (YieldLossPK) object;
        if (this.season != other.season) {
            return false;
        }
        if (this.pointOfInterestId != other.pointOfInterestId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.modules.wheatleafblotch.YieldLossPK[ season=" + season + ", pointOfInterestId=" + pointOfInterestId + " ]";
    }

}
