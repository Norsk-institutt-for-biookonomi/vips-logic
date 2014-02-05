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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@Table(name = "external_resource")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ExternalResource.findAll", query = "SELECT e FROM ExternalResource e"),
    @NamedQuery(name = "ExternalResource.findByExternalResourceId", query = "SELECT e FROM ExternalResource e WHERE e.externalResourceId = :externalResourceId"),
    @NamedQuery(name = "ExternalResource.findByUri", query = "SELECT e FROM ExternalResource e WHERE e.uri = :uri"),
    @NamedQuery(name = "ExternalResource.findByIdentifierTemplate", query = "SELECT e FROM ExternalResource e WHERE e.identifierTemplate = :identifierTemplate"),
    @NamedQuery(name = "ExternalResource.findByName", query = "SELECT e FROM ExternalResource e WHERE e.name = :name")})
public class ExternalResource implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "external_resource_id")
    private Integer externalResourceId;
    @Size(max = 1023)
    @Column(name = "uri")
    private String uri;
    @Size(max = 255)
    @Column(name = "identifier_template")
    private String identifierTemplate;
    @Size(max = 255)
    @Column(name = "name")
    private String name;
    @ManyToMany(mappedBy = "externalResourceSet")
    private Set<Country> countrySet;
    @JoinColumn(name = "external_resource_type_id", referencedColumnName = "external_resource_type_id")
    @ManyToOne
    private ExternalResourceType externalResourceTypeId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "externalResource")
    private Set<OrganismExternalResource> organismExternalResourceSet;

    public ExternalResource() {
    }

    public ExternalResource(Integer externalResourceId) {
        this.externalResourceId = externalResourceId;
    }

    public Integer getExternalResourceId() {
        return externalResourceId;
    }

    public void setExternalResourceId(Integer externalResourceId) {
        this.externalResourceId = externalResourceId;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getIdentifierTemplate() {
        return identifierTemplate;
    }

    public void setIdentifierTemplate(String identifierTemplate) {
        this.identifierTemplate = identifierTemplate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlTransient
    @JsonIgnore
    public Set<Country> getCountrySet() {
        return countrySet;
    }

    public void setCountrySet(Set<Country> countrySet) {
        this.countrySet = countrySet;
    }

    public ExternalResourceType getExternalResourceTypeId() {
        return externalResourceTypeId;
    }

    public void setExternalResourceTypeId(ExternalResourceType externalResourceTypeId) {
        this.externalResourceTypeId = externalResourceTypeId;
    }

    @XmlTransient
    @JsonIgnore
    public Set<OrganismExternalResource> getOrganismExternalResourceSet() {
        return organismExternalResourceSet;
    }

    public void setOrganismExternalResourceSet(Set<OrganismExternalResource> organismExternalResourceSet) {
        this.organismExternalResourceSet = organismExternalResourceSet;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (externalResourceId != null ? externalResourceId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ExternalResource)) {
            return false;
        }
        ExternalResource other = (ExternalResource) object;
        if ((this.externalResourceId == null && other.externalResourceId != null) || (this.externalResourceId != null && !this.externalResourceId.equals(other.externalResourceId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.ExternalResource[ externalResourceId=" + externalResourceId + " ]";
    }

}
