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

package no.nibio.vips.logic.entity;

import java.io.Serializable;
import java.util.List;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import org.hibernate.annotations.Type;
import io.hypersistence.utils.hibernate.type.array.ListArrayType;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "crop_pest")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CropPest.findAll", query = "SELECT c FROM CropPest c"),
    @NamedQuery(name = "CropPest.findByCropOrganismId", query = "SELECT c FROM CropPest c WHERE c.cropOrganismId = :cropOrganismId"),
    @NamedQuery(name = "CropPest.findByPestOrganismIds", query = "SELECT c FROM CropPest c WHERE c.pestOrganismIds = :pestOrganismIds"),
    @NamedQuery(name = "CropPest.findByPestOrganismId", query = "SELECT c FROM CropPest c WHERE :pestOrganismId IN (c.pestOrganismIds)"),
    @NamedQuery(name = "CropPest.findByIncludeAllChildCrops", query = "SELECT c FROM CropPest c WHERE c.includeAllChildCrops = :includeAllChildCrops")})
public class CropPest implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "crop_organism_id")
    private Integer cropOrganismId;
    @Type(ListArrayType.class)
    @Column(name = "pest_organism_ids")
    private List<Integer> pestOrganismIds;
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

    public List<Integer> getPestOrganismIds() {
        return pestOrganismIds;
    }

    public void setPestOrganismIds(List<Integer> pestOrganismIds) {
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
