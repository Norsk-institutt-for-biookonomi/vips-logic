/*
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
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

/**
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "growth_stage_location_date", schema = "wheatleafb")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "GrowthStageLocationDate.findAll", query = "SELECT g FROM GrowthStageLocationDate g"),
    @NamedQuery(name = "GrowthStageLocationDate.findByPointOfInterestId", query = "SELECT g FROM GrowthStageLocationDate g WHERE g.growthStageLocationDatePK.pointOfInterestId = :pointOfInterestId"),
    @NamedQuery(name = "GrowthStageLocationDate.findByCropOrganismId", query = "SELECT g FROM GrowthStageLocationDate g WHERE g.growthStageLocationDatePK.cropOrganismId = :cropOrganismId"),
    @NamedQuery(name = "GrowthStageLocationDate.findByCropOrganismIdAndPointOfInterestId", query = "SELECT g FROM GrowthStageLocationDate g WHERE g.growthStageLocationDatePK.cropOrganismId = :cropOrganismId AND g.growthStageLocationDatePK.pointOfInterestId = :pointOfInterestId")
})
public class GrowthStageLocationDate implements Serializable, Comparable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    private GrowthStageLocationDatePK growthStageLocationDatePK; 
    @Column(name = "day_number")
    private Integer dayNumber;
    

    public GrowthStageLocationDate() {
    }

    public GrowthStageLocationDate(GrowthStageLocationDatePK growthStateLocationPK) {
        this.growthStageLocationDatePK = growthStateLocationPK;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (getGrowthStageLocationDatePK() != null ? getGrowthStageLocationDatePK().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof GrowthStageLocationDate)) {
            return false;
        }
        GrowthStageLocationDate other = (GrowthStageLocationDate) object;
        if ((this.getGrowthStageLocationDatePK() == null && other.getGrowthStageLocationDatePK() != null) || (this.getGrowthStageLocationDatePK() != null && !this.growthStageLocationDatePK.equals(other.growthStageLocationDatePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.modules.wheatleafblotch.GrowthStageLocationDate[ growthStageLocationDatePK=" + getGrowthStageLocationDatePK() + " ]";
    }

    /**
     * @return the dayNumber
     */
    public Integer getDayNumber() {
        return dayNumber;
    }

    /**
     * @param dayNumber the dayNumber to set
     */
    public void setDayNumber(Integer dayNumber) {
        this.dayNumber = dayNumber;
    }

    @Override
    public int compareTo(Object t) {
        return this.getGrowthStageLocationDatePK().getGrowthStage().compareTo(((GrowthStageLocationDate) t).getGrowthStageLocationDatePK().getGrowthStage()
        );
    }

    /**
     * @return the growthStageLocationDatePK
     */
    public GrowthStageLocationDatePK getGrowthStageLocationDatePK() {
        return growthStageLocationDatePK;
    }

    /**
     * @param growthStageLocationDatePK the growthStageLocationDatePK to set
     */
    public void setGrowthStageLocationDatePK(GrowthStageLocationDatePK growthStageLocationDatePK) {
        this.growthStageLocationDatePK = growthStageLocationDatePK;
    }

}
