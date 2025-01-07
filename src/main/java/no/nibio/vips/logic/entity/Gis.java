/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import java.io.Serializable;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
//import org.hibernate.annotations.Type;

/**
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "gis")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Gis.findAll", query = "SELECT g FROM Gis g"),
    @NamedQuery(name = "Gis.findByGisId", query = "SELECT g FROM Gis g WHERE g.gisId = :gisId"),
    @NamedQuery(name = "Gis.findByGisIds", query = "SELECT g FROM Gis g WHERE g.gisId IN(:gisIds)")
})
public class Gis implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "gis_id")
    private Integer gisId;
    @JsonIgnore
    //@Type(type = "org.hibernate.spatial.GeometryType")
    @Column(name = "gis_geom", columnDefinition = "Geometry")
    private Geometry gisGeom;

    public Gis() {
    }

    public Gis(Integer gisId) {
        this.gisId = gisId;
    }

    public Integer getGisId() {
        return gisId;
    }

    public void setGisId(Integer gisId) {
        this.gisId = gisId;
    }

    
    public Geometry getGisGeom() {
        return gisGeom;
    }

    public void setGisGeom(Geometry gisGeom) {
        for(Coordinate c:gisGeom.getCoordinates())
        {
            if(Double.isNaN(c.z))
            {
                c.setCoordinate(new Coordinate(c.x,c.y,0.0));
            }
        }
        this.gisGeom = gisGeom;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (gisId != null ? gisId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Gis)) {
            return false;
        }
        Gis other = (Gis) object;
        if ((this.gisId == null && other.gisId != null) || (this.gisId != null && !this.gisId.equals(other.gisId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.Gis[ gisId=" + gisId + " ]";
    }

}
