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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
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
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "crop_category")
@XmlRootElement
@TypeDefs( {@TypeDef( name= "IntegerArray", typeClass = IntegerArrayUserType.class)})
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
    @Type(type = "IntegerArray")
    @Column(name = "crop_organism_ids")
    private Integer[] cropOrganismIds;
    @Column(name = "organization_id")
    private Integer organizationId;

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
        return null;
    }
}
