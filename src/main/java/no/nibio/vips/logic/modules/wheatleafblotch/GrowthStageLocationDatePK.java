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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import no.nibio.vips.logic.entity.Organism;
import no.nibio.vips.logic.entity.PointOfInterest;

/**
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Embeddable
public class GrowthStageLocationDatePK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @JoinColumn(name = "crop_organism_id", referencedColumnName = "organism_id")
    @ManyToOne
    private Organism cropOrganismId;
    @Basic(optional = false)
    @NotNull
    @JoinColumn(name = "point_of_interest_id", referencedColumnName = "point_of_interest_id")
    @ManyToOne
    private PointOfInterest pointOfInterestId;
    @Column(name = "growth_stage")
    private Integer growthStage;
    

    public GrowthStageLocationDatePK() {
    }

    public GrowthStageLocationDatePK(Organism organism, PointOfInterest pointOfInterestId) {
        this.cropOrganismId = organism;
        this.pointOfInterestId = pointOfInterestId;
    }

    public Organism getCropOrganismId() {
        return cropOrganismId;
    }

    public void setCropOrganismId(Organism cropOrganismId) {
        this.cropOrganismId = cropOrganismId;
    }

    public PointOfInterest getPointOfInterestId() {
        return pointOfInterestId;
    }

    public void setPointOfInterestId(PointOfInterest pointOfInterestId) {
        this.pointOfInterestId = pointOfInterestId;
    }
    
    public Integer getGrowthStage() {
        return growthStage;
    }

    public void setGrowthStage(Integer growthStage) {
        this.growthStage = growthStage;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) cropOrganismId.getOrganismId();
        hash += (int) pointOfInterestId.getPointOfInterestId();
        hash += (int) growthStage;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof GrowthStageLocationDatePK)) {
            return false;
        }
        GrowthStageLocationDatePK other = (GrowthStageLocationDatePK) object;
        if (this.cropOrganismId != other.cropOrganismId) {
            return false;
        }
        if (this.pointOfInterestId != other.pointOfInterestId) {
            return false;
        }
        if(! this.growthStage.equals(other.growthStage)){
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.modules.wheatleafblotch.YieldLossPK[ organism=" + cropOrganismId + ", pointOfInterestId=" + pointOfInterestId + " ]";
    }

}
