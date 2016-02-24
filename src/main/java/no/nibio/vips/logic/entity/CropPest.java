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

package no.nibio.vips.logic.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import no.nibio.vips.logic.util.IntegerArrayUserType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "crop_pest")
@TypeDefs( {@TypeDef( name= "IntegerArray", typeClass = IntegerArrayUserType.class)})
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CropPest.findAll", query = "SELECT c FROM CropPest c"),
    @NamedQuery(name = "CropPest.findByCropOrganismId", query = "SELECT c FROM CropPest c WHERE c.cropOrganismId = :cropOrganismId"),
    @NamedQuery(name = "CropPest.findByPestOrganismIds", query = "SELECT c FROM CropPest c WHERE c.pestOrganismIds = :pestOrganismIds"),
    @NamedQuery(name = "CropPest.findByIncludeAllChildCrops", query = "SELECT c FROM CropPest c WHERE c.includeAllChildCrops = :includeAllChildCrops")})
public class CropPest implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "crop_organism_id")
    private Integer cropOrganismId;
    @Type(type = "IntegerArray")
    @Column(name = "pest_organism_ids")
    private Integer[] pestOrganismIds;
    @Column(name = "include_all_child_crops")
    private Boolean includeAllChildCrops;

    public CropPest() {
    }

    public CropPest(Integer cropOrganismId) {
        this.cropOrganismId = cropOrganismId;
    }

    public Integer getCropOrganismId() {
        return cropOrganismId;
    }

    public void setCropOrganismId(Integer cropOrganismId) {
        this.cropOrganismId = cropOrganismId;
    }

    public Integer[] getPestOrganismIds() {
        return pestOrganismIds;
    }

    public void setPestOrganismIds(Integer[] pestOrganismIds) {
        this.pestOrganismIds = pestOrganismIds;
    }

    public Boolean getIncludeAllChildCrops() {
        return includeAllChildCrops;
    }

    public void setIncludeAllChildCrops(Boolean includeAllChildCrops) {
        this.includeAllChildCrops = includeAllChildCrops;
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
        if (!(object instanceof CropPest)) {
            return false;
        }
        CropPest other = (CropPest) object;
        if ((this.cropOrganismId == null && other.cropOrganismId != null) || (this.cropOrganismId != null && !this.cropOrganismId.equals(other.cropOrganismId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.CropPest[ cropOrganismId=" + cropOrganismId + " ]";
    }

}
