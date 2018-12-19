/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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
import java.math.BigDecimal;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2018 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "resistance_factor", schema = "wheatleafb")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ResistanceFactor.findAll", query = "SELECT f FROM Factors f"),
    @NamedQuery(name = "ResistanceFactor.findByOrganizationId", query = "SELECT f FROM ResistanceFactor f where f.organizationId IN (SELECT o.organizationId FROM Organization o WHERE o = :organizationId OR o.parentOrganizationId = :organizationId)"),
    @NamedQuery(name = "ResistanceFactor.findByCropOrganismId", query = "SELECT f FROM ResistanceFactor f WHERE f.cropOrganismId = :cropOrganismId"),
    @NamedQuery(name = "ResistanceFactor.findByResistanceFactor", query = "SELECT f FROM ResistanceFactor f WHERE f.resistanceFactor = :resistanceFactor")
})
public class ResistanceFactor implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "crop_organism_id")
    private Integer cropOrganismId;
    @Column(name = "organization_id")
    private Integer organizationId;
    @Column(name = "resistance_factor")
    private BigDecimal resistanceFactor;

    public ResistanceFactor() {
    }

    public ResistanceFactor(Integer cropOrganismId) {
        this.cropOrganismId = cropOrganismId;
    }

    public Integer getCropOrganismId() {
        return cropOrganismId;
    }

    public void setCropOrganismId(Integer cropOrganismId) {
        this.cropOrganismId = cropOrganismId;
    }

    public BigDecimal getResistanceFactor() {
        return resistanceFactor;
    }

    public void setResistanceFactor(BigDecimal resistanceFactor) {
        this.resistanceFactor = resistanceFactor;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cropOrganismId != null ? cropOrganismId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ResistanceFactor)) {
            return false;
        }
        ResistanceFactor other = (ResistanceFactor) object;
        if ((this.cropOrganismId == null && other.cropOrganismId != null) || (this.cropOrganismId != null && !this.cropOrganismId.equals(other.cropOrganismId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.modules.barleynetblotch.Factors[ cropOrganismId=" + cropOrganismId + " ]";
    }

    /**
     * @return the organizationId
     */
    public Integer getOrganizationId() {
        return organizationId;
    }

    /**
     * @param organizationId the organizationId to set
     */
    public void setOrganizationId(Integer organizationId) {
        this.organizationId = organizationId;
    }

}
