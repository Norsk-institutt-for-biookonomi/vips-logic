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

package no.nibio.vips.logic.entity;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "preparation_type")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PreparationType.findAll", query = "SELECT p FROM PreparationType p"),
    @NamedQuery(name = "PreparationType.findByPreparationTypeId", query = "SELECT p FROM PreparationType p WHERE p.preparationTypeId = :preparationTypeId"),
    @NamedQuery(name = "PreparationType.findByPreparationTypeName", query = "SELECT p FROM PreparationType p WHERE p.preparationTypeName = :preparationTypeName")})
public class PreparationType implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "preparation_type_id")
    private Integer preparationTypeId;
    @Size(max = 255)
    @Column(name = "preparation_type_name")
    private String preparationTypeName;
    @OneToMany(mappedBy = "preparationTypeId")
    private Collection<Preparation> preparationCollection;

    public PreparationType() {
    }

    public PreparationType(Integer preparationTypeId) {
        this.preparationTypeId = preparationTypeId;
    }

    public Integer getPreparationTypeId() {
        return preparationTypeId;
    }

    public void setPreparationTypeId(Integer preparationTypeId) {
        this.preparationTypeId = preparationTypeId;
    }

    public String getPreparationTypeName() {
        return preparationTypeName;
    }

    public void setPreparationTypeName(String preparationTypeName) {
        this.preparationTypeName = preparationTypeName;
    }

    @XmlTransient
    public Collection<Preparation> getPreparationCollection() {
        return preparationCollection;
    }

    public void setPreparationCollection(Collection<Preparation> preparationCollection) {
        this.preparationCollection = preparationCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (preparationTypeId != null ? preparationTypeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PreparationType)) {
            return false;
        }
        PreparationType other = (PreparationType) object;
        if ((this.preparationTypeId == null && other.preparationTypeId != null) || (this.preparationTypeId != null && !this.preparationTypeId.equals(other.preparationTypeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.PreparationType[ preparationTypeId=" + preparationTypeId + " ]";
    }

}
