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
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import no.nibio.vips.logic.entity.PointOfInterest;

/**
 * @copyright 2019 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "yield_loss", schema = "wheatleafb")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "YieldLoss.findAll", query = "SELECT y FROM YieldLoss y"),
    @NamedQuery(name = "YieldLoss.findBySeason", query = "SELECT y FROM YieldLoss y WHERE y.yieldLossPK.season = :season"),
    @NamedQuery(name = "YieldLoss.findByPointOfInterestId", query = "SELECT y FROM YieldLoss y WHERE y.yieldLossPK.pointOfInterestId = :pointOfInterestId"),
    @NamedQuery(name = "YieldLoss.findByYieldLoss", query = "SELECT y FROM YieldLoss y WHERE y.yieldLoss = :yieldLoss")
})
public class YieldLoss implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected YieldLossPK yieldLossPK;
    @Column(name = "yield_loss")
    private Double yieldLoss;

    public YieldLoss() {
    }

    public YieldLoss(YieldLossPK yieldLossPK) {
        this.yieldLossPK = yieldLossPK;
    }

    public YieldLoss(int season, PointOfInterest pointOfInterestId) {
        this.yieldLossPK = new YieldLossPK(season, pointOfInterestId);
    }

    public YieldLossPK getYieldLossPK() {
        return yieldLossPK;
    }

    public void setYieldLossPK(YieldLossPK yieldLossPK) {
        this.yieldLossPK = yieldLossPK;
    }

    public Double getYieldLoss() {
        return yieldLoss;
    }

    public void setYieldLoss(Double yieldLoss) {
        this.yieldLoss = yieldLoss;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (yieldLossPK != null ? yieldLossPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof YieldLoss)) {
            return false;
        }
        YieldLoss other = (YieldLoss) object;
        if ((this.yieldLossPK == null && other.yieldLossPK != null) || (this.yieldLossPK != null && !this.yieldLossPK.equals(other.yieldLossPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.modules.wheatleafblotch.YieldLoss[ yieldLossPK=" + yieldLossPK + " ]";
    }

}
