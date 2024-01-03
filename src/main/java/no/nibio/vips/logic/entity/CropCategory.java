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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import no.nibio.vips.logic.util.IntegerArrayUserType;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "crop_category")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CropCategory.findAll", query = "SELECT c FROM CropCategory c"),
    @NamedQuery(name = "CropCategory.findByCropCategoryId", query = "SELECT c FROM CropCategory c WHERE c.cropCategoryId = :cropCategoryId"),
    @NamedQuery(name = "CropCategory.findByCropCategoryIds", query = "SELECT c FROM CropCategory c WHERE c.cropCategoryId IN :cropCategoryIds"),
    @NamedQuery(name = "CropCategory.findByDefaultName", query = "SELECT c FROM CropCategory c WHERE c.defaultName = :defaultName"),
    @NamedQuery(name = "CropCategory.findByCropOrganismIds", query = "SELECT c FROM CropCategory c WHERE c.cropOrganismIds = :cropOrganismIds"),
    @NamedQuery(name = "CropCategory.findByOrganizationId", query = "SELECT c FROM CropCategory c WHERE c.organizationId = :organizationId")
})
public class CropCategory implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "crop_category_id")
    private Integer cropCategoryId;
    @Size(max = 255)
    @Column(name = "default_name")
    private String defaultName;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "cropCategoryId", fetch = FetchType.EAGER)
    private Set<CropCategoryLocal> cropCategoryLocalSet;
    @Convert(converter = IntegerArrayUserType.class)
    @Column(name = "crop_organism_ids")
    private Integer[] cropOrganismIds;
    @Column(name = "organization_id")
    private Integer organizationId;
    @Column(name = "max_hierarchy_category_id")
    private Integer maxHierarchyCategoryId;

    public CropCategory() {
    }

    public CropCategory(Integer cropCategoryId) {
        this.cropCategoryId = cropCategoryId;
    }

    public Integer getCropCategoryId() {
        return cropCategoryId;
    }

    public void setCropCategoryId(Integer cropCategoryId) {
        this.cropCategoryId = cropCategoryId;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    public Integer[] getCropOrganismIds() {
        return cropOrganismIds;
    }

    public void setCropOrganismIds(Integer[] cropOrganismIds) {
        this.cropOrganismIds = cropOrganismIds;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cropCategoryId != null ? cropCategoryId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CropCategory)) {
            return false;
        }
        CropCategory other = (CropCategory) object;
        if ((this.cropCategoryId == null && other.cropCategoryId != null) || (this.cropCategoryId != null && !this.cropCategoryId.equals(other.cropCategoryId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.CropCategory[ cropCategoryId=" + cropCategoryId + " ]";
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

    /**
     * @return the cropCategoryLocalSet
     */
    public Set<CropCategoryLocal> getCropCategoryLocalSet() {
        return cropCategoryLocalSet;
    }

    /**
     * @param cropCategoryLocalSet the cropCategoryLocalSet to set
     */
    public void setCropCategoryLocalSet(Set<CropCategoryLocal> cropCategoryLocalSet) {
        this.cropCategoryLocalSet = cropCategoryLocalSet;
    }

    @JsonIgnore
    public String getLocalName(String language)
    {
        if(this.cropCategoryLocalSet != null && ! this.getCropCategoryLocalSet().isEmpty())
        {
            for(CropCategoryLocal ccl:this.getCropCategoryLocalSet())
            {
                if(ccl.getCropCategoryLocalPK().getLocale().equals(language))
                {
                    return ccl.getLocalName();
                }
            }
        }
        return this.getDefaultName();
    }
    
    @JsonIgnore
    public boolean containsCropOrganismId(Integer organismId)
    {
        if(this.getCropOrganismIds() == null)
        {
            return false;
        }
        for(Integer oId:this.getCropOrganismIds())
        {
            if(oId.equals(organismId))
            {
                return true;
            }
        }
        return false;
    }

    public void addCropOrganismId(Integer organismId) {
        Set<Integer> cropSet;
        if(this.getCropOrganismIds() == null)
        {
            cropSet = new HashSet<>();
        }
        else
        {
            cropSet = new HashSet<>(Arrays.asList(this.getCropOrganismIds()));
        }
        cropSet.add(organismId);
        this.setCropOrganismIds(cropSet.toArray(new Integer[cropSet.size()]));
    }

    public void removeCropOrganismId(Integer organismId) {
        if(this.getCropOrganismIds() != null)
        {
            Set<Integer> cropSet = new HashSet<>(Arrays.asList(this.getCropOrganismIds()));
            cropSet.remove(organismId);
            this.setCropOrganismIds(cropSet.toArray(new Integer[cropSet.size()]));
        }
    }

    /**
     * @return the maxHierarchyCategoryId
     */
    public Integer getMaxHierarchyCategoryId() {
        return maxHierarchyCategoryId;
    }

    /**
     * @param maxHierarchyCategoryId the maxHierarchyCategoryId to set
     */
    public void setMaxHierarchyCategoryId(Integer maxHierarchyCategoryId) {
        this.maxHierarchyCategoryId = maxHierarchyCategoryId;
    }
}
