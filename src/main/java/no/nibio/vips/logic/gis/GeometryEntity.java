/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.gis;

import org.locationtech.jts.geom.Geometry;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.hibernate.annotations.Type;

/**
 * This entity enables native querying of PostGIS Geometry elements in the database, 
 * mapping to Java Topology Suite Geometry object. Example of usage:
 * <pre>Query q = em.createNativeQuery("SELECT 1 as id, ST_Centroid(ST_Union(gis_geom)) AS geometry_value FROM public.gis WHERE gis_id IN ("
                    + "     SELECT gis_id "
                    + "     FROM applefruitmoth.observation_site_point "
                    + "     WHERE observation_site_id = :observationSiteId"
                    + ")", GeometryEntity.class);
            
            GeometryEntity o = (GeometryEntity) q.setParameter("observationSiteId", observationSite.getObservationSiteId())
                    .getSingleResult();
            
            return o.getGeometryValue();
   </pre>
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
public class GeometryEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    //@Type(type = "org.hibernate.spatial.GeometryType")
    @Column(name = "geometry_value", columnDefinition = "Geometry")
    private Geometry geometryValue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof GeometryEntity)) {
            return false;
        }
        GeometryEntity other = (GeometryEntity) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.gis.GeometryEntity[ id=" + id + " ]";
    }

    /**
     * @return the geometryValue
     */
    public Geometry getGeometryValue() {
        return geometryValue;
    }

    /**
     * @param geometryValue the geometryValue to set
     */
    public void setGeometryValue(Geometry geometryValue) {
        this.geometryValue = geometryValue;
    }

}
