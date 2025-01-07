/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
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
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "organism_external_resource")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "OrganismExternalResource.findAll", query = "SELECT o FROM OrganismExternalResource o"),
    @NamedQuery(name = "OrganismExternalResource.findByOrganismId", query = "SELECT o FROM OrganismExternalResource o WHERE o.organismExternalResourcePK.organismId = :organismId"),
    @NamedQuery(name = "OrganismExternalResource.findByExternalResourceId", query = "SELECT o FROM OrganismExternalResource o WHERE o.organismExternalResourcePK.externalResourceId = :externalResourceId"),
    @NamedQuery(name = "OrganismExternalResource.findByResourceIdentifier", query = "SELECT o FROM OrganismExternalResource o WHERE o.resourceIdentifier = :resourceIdentifier")})
public class OrganismExternalResource implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected OrganismExternalResourcePK organismExternalResourcePK;
    @Size(max = 255)
    @Column(name = "resource_identifier")
    private String resourceIdentifier;
    @JoinColumn(name = "organism_id", referencedColumnName = "organism_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    @JsonIgnore
    private Organism organism;
    @JoinColumn(name = "external_resource_id", referencedColumnName = "external_resource_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private ExternalResource externalResource;

    public OrganismExternalResource() {
    }

    public OrganismExternalResource(OrganismExternalResourcePK organismExternalResourcePK) {
        this.organismExternalResourcePK = organismExternalResourcePK;
    }

    public OrganismExternalResource(int organismId, int externalResourceId) {
        this.organismExternalResourcePK = new OrganismExternalResourcePK(organismId, externalResourceId);
    }

    public OrganismExternalResourcePK getOrganismExternalResourcePK() {
        return organismExternalResourcePK;
    }

    public void setOrganismExternalResourcePK(OrganismExternalResourcePK organismExternalResourcePK) {
        this.organismExternalResourcePK = organismExternalResourcePK;
    }

    public String getResourceIdentifier() {
        return resourceIdentifier;
    }

    public void setResourceIdentifier(String resourceIdentifier) {
        this.resourceIdentifier = resourceIdentifier;
    }

    @XmlTransient
    public Organism getOrganism() {
        return organism;
    }

    public void setOrganism(Organism organism) {
        this.organism = organism;
    }

    public ExternalResource getExternalResource() {
        return externalResource;
    }

    public void setExternalResource(ExternalResource externalResource) {
        this.externalResource = externalResource;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (organismExternalResourcePK != null ? organismExternalResourcePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof OrganismExternalResource)) {
            return false;
        }
        OrganismExternalResource other = (OrganismExternalResource) object;
        if ((this.organismExternalResourcePK == null && other.organismExternalResourcePK != null) || (this.organismExternalResourcePK != null && !this.organismExternalResourcePK.equals(other.organismExternalResourcePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.OrganismExternalResource[ organismExternalResourcePK=" + organismExternalResourcePK + " ]";
    }

    @Transient
    public String getResourceUrl()
    {
        StringBuilder retVal = new StringBuilder()
                .append(this.getExternalResource().getUri())
                .append(this.getExternalResource().getIdentifierTemplate());
        return String.format(retVal.toString(), this.getResourceIdentifier());
    }
}
