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

import com.fasterxml.jackson.annotation.JsonProperty;
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
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "map_layer")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "MapLayer.findAll", query = "SELECT m FROM MapLayer m"),
    @NamedQuery(name = "MapLayer.findByMapLayerId", query = "SELECT m FROM MapLayer m WHERE m.mapLayerId = :mapLayerId"),
    @NamedQuery(name = "MapLayer.findByMapLayerTitle", query = "SELECT m FROM MapLayer m WHERE m.mapLayerTitle = :mapLayerTitle"),
    @NamedQuery(name = "MapLayer.findByMapLayerType", query = "SELECT m FROM MapLayer m WHERE m.mapLayerType = :mapLayerType"),
    @NamedQuery(name = "MapLayer.findByDefaultVisible", query = "SELECT m FROM MapLayer m WHERE m.defaultVisible = :defaultVisible"),
    @NamedQuery(name = "MapLayer.findByHoverAttribute", query = "SELECT m FROM MapLayer m WHERE m.hoverAttribute = :hoverAttribute"),
    @NamedQuery(name = "MapLayer.findByUrl", query = "SELECT m FROM MapLayer m WHERE m.url = :url")})
public class MapLayer implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "map_layer_id")
    private Integer mapLayerId;
    @Size(max = 255)
    @Column(name = "map_layer_title")
    private String mapLayerTitle;
    @Column(name = "description")
    private String description;
    @Size(max = 255)
    @Column(name = "map_layer_type")
    private String mapLayerType;
    @Column(name = "default_visible")
    private Boolean defaultVisible;
    @Size(max = 63)
    @Column(name = "hover_attribute")
    private String hoverAttribute;
    @Size(max = 1023)
    @Column(name = "url")
    private String url;

    public MapLayer() {
    }

    public MapLayer(Integer mapLayerId) {
        this.mapLayerId = mapLayerId;
    }

    public Integer getMapLayerId() {
        return mapLayerId;
    }

    public void setMapLayerId(Integer mapLayerId) {
        this.mapLayerId = mapLayerId;
    }

    public String getMapLayerTitle() {
        return mapLayerTitle;
    }

    public void setMapLayerTitle(String mapLayerTitle) {
        this.mapLayerTitle = mapLayerTitle;
    }

    public String getMapLayerType() {
        return mapLayerType;
    }

    public void setMapLayerType(String mapLayerType) {
        this.mapLayerType = mapLayerType;
    }

    public Boolean getDefaultVisible() {
        return defaultVisible;
    }

    public void setDefaultVisible(Boolean defaultVisible) {
        this.defaultVisible = defaultVisible;
    }

    public String getHoverAttribute() {
        return hoverAttribute;
    }

    public void setHoverAttribute(String hoverAttribute) {
        this.hoverAttribute = hoverAttribute;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (mapLayerId != null ? mapLayerId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MapLayer)) {
            return false;
        }
        MapLayer other = (MapLayer) object;
        if ((this.mapLayerId == null && other.mapLayerId != null) || (this.mapLayerId != null && !this.mapLayerId.equals(other.mapLayerId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.MapLayer[ mapLayerId=" + mapLayerId + " ]";
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

}
