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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vividsolutions.jts.geom.Geometry;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
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
