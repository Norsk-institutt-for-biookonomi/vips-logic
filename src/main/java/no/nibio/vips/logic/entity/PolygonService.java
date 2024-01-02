/*
 * Copyright (c) 2019 NIBIO <http://www.nibio.no/>. 
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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2019 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "polygon_service")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PolygonService.findAll", query = "SELECT p FROM PolygonService p"),
    @NamedQuery(name = "PolygonService.findByPolygonServiceId", query = "SELECT p FROM PolygonService p WHERE p.polygonServiceId = :polygonServiceId"),
    @NamedQuery(name = "PolygonService.findByPolygonServiceName", query = "SELECT p FROM PolygonService p WHERE p.polygonServiceName = :polygonServiceName"),
    @NamedQuery(name = "PolygonService.findByDescription", query = "SELECT p FROM PolygonService p WHERE p.description = :description"),
    @NamedQuery(name = "PolygonService.findByGisSearchUrlTemplate", query = "SELECT p FROM PolygonService p WHERE p.gisSearchUrlTemplate = :gisSearchUrlTemplate"),
    @NamedQuery(name = "PolygonService.findByEpsg", query = "SELECT p FROM PolygonService p WHERE p.epsg = :epsg")})
public class PolygonService implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "polygon_service_id")
    private Integer polygonServiceId;
    @Size(max = 255)
    @Column(name = "polygon_service_name")
    private String polygonServiceName;
    @Size(max = 2147483647)
    @Column(name = "description")
    private String description;
    @Size(max = 1024)
    @Column(name = "gis_search_url_template")
    private String gisSearchUrlTemplate;
    @Column(name = "epsg")
    private Integer epsg;

    public PolygonService() {
    }

    public PolygonService(Integer polygonServiceId) {
        this.polygonServiceId = polygonServiceId;
    }

    public Integer getPolygonServiceId() {
        return polygonServiceId;
    }

    public void setPolygonServiceId(Integer polygonServiceId) {
        this.polygonServiceId = polygonServiceId;
    }

    public String getPolygonServiceName() {
        return polygonServiceName;
    }

    public void setPolygonServiceName(String polygonServiceName) {
        this.polygonServiceName = polygonServiceName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGisSearchUrlTemplate() {
        return gisSearchUrlTemplate;
    }

    public void setGisSearchUrlTemplate(String gisSearchUrlTemplate) {
        this.gisSearchUrlTemplate = gisSearchUrlTemplate;
    }

    public Integer getEpsg() {
        return epsg;
    }

    public void setEpsg(Integer epsg) {
        this.epsg = epsg;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (polygonServiceId != null ? polygonServiceId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PolygonService)) {
            return false;
        }
        PolygonService other = (PolygonService) object;
        if ((this.polygonServiceId == null && other.polygonServiceId != null) || (this.polygonServiceId != null && !this.polygonServiceId.equals(other.polygonServiceId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.PolygonService[ polygonServiceId=" + polygonServiceId + " ]";
    }

}
