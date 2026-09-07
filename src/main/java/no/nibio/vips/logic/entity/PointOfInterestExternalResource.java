/*
 * Copyright (c) 2018 NIBIO <http://www.nibio.no/>. 
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
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 * @copyright 2018 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "point_of_interest_external_resource")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PointOfInterestExternalResource.findAll", query = "SELECT p FROM PointOfInterestExternalResource p"),
    @NamedQuery(name = "PointOfInterestExternalResource.findByPointOfInterestId", query = "SELECT p FROM PointOfInterestExternalResource p WHERE p.pointOfInterestExternalResourcePK.pointOfInterestId = :pointOfInterestId"),
    @NamedQuery(name = "PointOfInterestExternalResource.findByExternalResourceId", query = "SELECT p FROM PointOfInterestExternalResource p WHERE p.pointOfInterestExternalResourcePK.externalResourceId = :externalResourceId"),
    @NamedQuery(name = "PointOfInterestExternalResource.findByResourceIdentifier", query = "SELECT p FROM PointOfInterestExternalResource p WHERE p.resourceIdentifier = :resourceIdentifier")})
public class PointOfInterestExternalResource implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected PointOfInterestExternalResourcePK pointOfInterestExternalResourcePK;
    @Size(max = 255)
    @Column(name = "resource_identifier")
    private String resourceIdentifier;
    
    @JoinColumn(name = "point_of_interest_id", referencedColumnName = "point_of_interest_id", insertable = false, updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private PointOfInterest pointOfInterest;
    @JoinColumn(name = "external_resource_id", referencedColumnName = "external_resource_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private ExternalResource externalResource;

    public PointOfInterestExternalResource() {
    }

    public PointOfInterestExternalResource(PointOfInterestExternalResourcePK pointOfInterestExternalResourcePK) {
        this.pointOfInterestExternalResourcePK = pointOfInterestExternalResourcePK;
    }

    public PointOfInterestExternalResource(int pointOfInterestId, int externalResourceId) {
        this.pointOfInterestExternalResourcePK = new PointOfInterestExternalResourcePK(pointOfInterestId, externalResourceId);
    }

    public PointOfInterestExternalResourcePK getPointOfInterestExternalResourcePK() {
        return pointOfInterestExternalResourcePK;
    }

    public void setPointOfInterestExternalResourcePK(PointOfInterestExternalResourcePK pointOfInterestExternalResourcePK) {
        this.pointOfInterestExternalResourcePK = pointOfInterestExternalResourcePK;
    }

    public String getResourceIdentifier() {
        return resourceIdentifier;
    }

    public void setResourceIdentifier(String resourceIdentifier) {
        this.resourceIdentifier = resourceIdentifier;
    }

    @XmlTransient
    @JsonIgnore
    public PointOfInterest getPointOfInterest() {
        return pointOfInterest;
    }

    public void setPointOfInterest(PointOfInterest pointOfInterest) {
        this.pointOfInterest = pointOfInterest;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (pointOfInterestExternalResourcePK != null ? pointOfInterestExternalResourcePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PointOfInterestExternalResource)) {
            return false;
        }
        PointOfInterestExternalResource other = (PointOfInterestExternalResource) object;
        if ((this.pointOfInterestExternalResourcePK == null && other.pointOfInterestExternalResourcePK != null) || (this.pointOfInterestExternalResourcePK != null && !this.pointOfInterestExternalResourcePK.equals(other.pointOfInterestExternalResourcePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.PointOfInterestExternalResource[ pointOfInterestExternalResourcePK=" + pointOfInterestExternalResourcePK + " ]";
    }

    /**
     * @return the externalResource
     */
    public ExternalResource getExternalResource() {
        return externalResource;
    }

    /**
     * @param externalResource the externalResource to set
     */
    public void setExternalResource(ExternalResource externalResource) {
        this.externalResource = externalResource;
    }
    
    @Transient
    public String getExternalResourceUrl()
    {
        return this.getExternalResource().getUri() + this.getExternalResource().getIdentifierTemplate().replace("%s", this.getResourceIdentifier());
    }

}
