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

import java.io.Serializable;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
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
    @Column(name = "point_of_interest_type_id", insertable = false, updatable = false)
    private Integer pointOfInterestTypeId;
    @Size(max = 255)
    @Column(name = "default_name")
    private String defaultName;
    
    // Hard coded stuff
    public static final int POINT_OF_INTEREST_TYPE_WEATHER_STATION = 1;
    public static final int POINT_OF_INTEREST_TYPE_FARM = 2;
    public static final int POINT_OF_INTEREST_TYPE_FIELD = 3;
    public static final int POINT_OF_INTEREST_TYPE_REGION = 4;
    public static final int POINT_OF_INTEREST_TYPE_TRAP = 5;
    public static final int POINT_OF_INTEREST_TYPE_NURSERY = 7;

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
        return "no.nibio.vips.logic.entity.PointOfInterestType[ pointOfInterestTypeId=" + pointOfInterestTypeId + " ]";
    }

    
    

}
