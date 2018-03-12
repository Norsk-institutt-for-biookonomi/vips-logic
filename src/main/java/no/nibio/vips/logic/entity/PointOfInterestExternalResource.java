/*
 * Copyright (c) 2018 NIBIO <http://www.nibio.no/>. 
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
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

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
