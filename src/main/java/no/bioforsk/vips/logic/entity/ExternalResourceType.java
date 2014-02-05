/*
 * Copyright (c) 2013-2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with VIPSLogic.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package no.bioforsk.vips.logic.entity;

import java.io.Serializable;
import java.util.Set;
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
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@Table(name = "external_resource_type")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ExternalResourceType.findAll", query = "SELECT e FROM ExternalResourceType e"),
    @NamedQuery(name = "ExternalResourceType.findByExternalResourceTypeId", query = "SELECT e FROM ExternalResourceType e WHERE e.externalResourceTypeId = :externalResourceTypeId"),
    @NamedQuery(name = "ExternalResourceType.findByDefaultName", query = "SELECT e FROM ExternalResourceType e WHERE e.defaultName = :defaultName")})
public class ExternalResourceType implements Serializable {
    
    public static final int ORGANISM_DATABASE = 2;
    
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "external_resource_type_id")
    private Integer externalResourceTypeId;
    @Size(max = 255)
    @Column(name = "default_name")
    private String defaultName;
    @OneToMany(mappedBy = "externalResourceTypeId")
    private Set<ExternalResource> externalResourceSet;

    public ExternalResourceType() {
    }

    public ExternalResourceType(Integer externalResourceTypeId) {
        this.externalResourceTypeId = externalResourceTypeId;
    }

    public Integer getExternalResourceTypeId() {
        return externalResourceTypeId;
    }

    public void setExternalResourceTypeId(Integer externalResourceTypeId) {
        this.externalResourceTypeId = externalResourceTypeId;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    @XmlTransient
    @JsonIgnore
    public Set<ExternalResource> getExternalResourceSet() {
        return externalResourceSet;
    }

    public void setExternalResourceSet(Set<ExternalResource> externalResourceSet) {
        this.externalResourceSet = externalResourceSet;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (externalResourceTypeId != null ? externalResourceTypeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ExternalResourceType)) {
            return false;
        }
        ExternalResourceType other = (ExternalResourceType) object;
        if ((this.externalResourceTypeId == null && other.externalResourceTypeId != null) || (this.externalResourceTypeId != null && !this.externalResourceTypeId.equals(other.externalResourceTypeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.ExternalResourceType[ externalResourceTypeId=" + externalResourceTypeId + " ]";
    }

}
