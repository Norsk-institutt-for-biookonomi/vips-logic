/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="point_of_interest_type_id")
@Table(name = "point_of_interest_type")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PointOfInterestType.findAll", query = "SELECT p FROM PointOfInterestType p"),
    @NamedQuery(name = "PointOfInterestType.findByPointOfInterestTypeId", query = "SELECT p FROM PointOfInterestType p WHERE p.pointOfInterestTypeId = :pointOfInterestTypeId"),
    @NamedQuery(name = "PointOfInterestType.findByDefaultName", query = "SELECT p FROM PointOfInterestType p WHERE p.defaultName = :defaultName")})
public class PointOfInterestType implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "point_of_interest_type_id")
    private Integer pointOfInterestTypeId;
    @Size(max = 255)
    @Column(name = "default_name")
    private String defaultName;

    public PointOfInterestType() {
    }

    public PointOfInterestType(Integer pointOfInterestTypeId) {
        this.pointOfInterestTypeId = pointOfInterestTypeId;
    }

    public Integer getPointOfInterestTypeId() {
        return pointOfInterestTypeId;
    }

    public void setPointOfInterestTypeId(Integer pointOfInterestTypeId) {
        this.pointOfInterestTypeId = pointOfInterestTypeId;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (pointOfInterestTypeId != null ? pointOfInterestTypeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PointOfInterestType)) {
            return false;
        }
        PointOfInterestType other = (PointOfInterestType) object;
        if ((this.pointOfInterestTypeId == null && other.pointOfInterestTypeId != null) || (this.pointOfInterestTypeId != null && !this.pointOfInterestTypeId.equals(other.pointOfInterestTypeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.PointOfInterestType[ pointOfInterestTypeId=" + pointOfInterestTypeId + " ]";
    }

    
    

}
