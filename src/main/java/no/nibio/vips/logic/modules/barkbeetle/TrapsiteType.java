/*
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.modules.barkbeetle;

import java.io.Serializable;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "trapsite_type", schema="barkbeetle")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TrapsiteType.findAll", query = "SELECT l FROM TrapsiteType l"),
    @NamedQuery(name = "TrapsiteType.findByLocationTypeId", query = "SELECT l FROM TrapsiteType l WHERE l.trapsiteTypeId = :trapsiteTypeId"),
    @NamedQuery(name = "TrapsiteType.findByTitle", query = "SELECT l FROM TrapsiteType l WHERE l.title = :title"),
    @NamedQuery(name = "TrapsiteType.findByDescription", query = "SELECT l FROM TrapsiteType l WHERE l.description = :description")})
public class TrapsiteType implements Serializable {
    
    public static Integer TRAPSITE_TYPE_STANDARD = 1;
    public static Integer TRAPSITE_TYPE_EXTENDED = 2;


    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "trapsite_type_id")
    private Integer trapsiteTypeId;
    @Size(max = 255)
    @Column(name = "title")
    private String title;
    @Size(max = 2147483647)
    @Column(name = "description")
    private String description;
    

    public TrapsiteType() {
    }

    public TrapsiteType(Integer trapsiteTypeId) {
        this.trapsiteTypeId = trapsiteTypeId;
    }

    public Integer getTrapsiteTypeId() {
        return trapsiteTypeId;
    }

    public void setTrapsiteTypeId(Integer trapsiteTypeId) {
        this.trapsiteTypeId = trapsiteTypeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (trapsiteTypeId != null ? trapsiteTypeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TrapsiteType)) {
            return false;
        }
        TrapsiteType other = (TrapsiteType) object;
        if ((this.trapsiteTypeId == null && other.trapsiteTypeId != null) || (this.trapsiteTypeId != null && !this.trapsiteTypeId.equals(other.trapsiteTypeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.modules.barkbeetle.TrapsiteType[ trapsiteTypeId=" + trapsiteTypeId + " ]";
    }

}
