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
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "vips_logic_role")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "VipsLogicRole.findAll", query = "SELECT v FROM VipsLogicRole v"),
    @NamedQuery(name = "VipsLogicRole.findByVipsLogicRoleId", query = "SELECT v FROM VipsLogicRole v WHERE v.vipsLogicRoleId = :vipsLogicRoleId"),
    @NamedQuery(name = "VipsLogicRole.findByDefaultTitle", query = "SELECT v FROM VipsLogicRole v WHERE v.defaultTitle = :defaultTitle"),
    @NamedQuery(name = "VipsLogicRole.findByDefaultDescription", query = "SELECT v FROM VipsLogicRole v WHERE v.defaultDescription = :defaultDescription")})
public class VipsLogicRole implements Serializable {
    
    /** Static role definitions */
    public static Integer SUPERUSER = 1;
    public static Integer ORGANIZATION_ADMINISTRATOR = 2;
    public static Integer OBSERVER = 3;
    public static Integer OBSERVATION_AUTHORITY = 4;
    public static Integer MESSAGE_AUTHOR = 5;
    public static Integer APPLE_FRUIT_MOTH_ADMINISTRATOR = 6; // Norway only
    public static Integer ORGANISM_EDITOR = 7;
    public static Integer APPLE_FRUIT_MOTH_BERRY_CLUSTER_COUNTER = 8; // Norway only
    public static Integer BARKBEETLE_REGISTRATOR = 9; // Norway only
    public static Integer BARKBEETLE_ADMIN = 10; // Norway only
    public static Integer BARKBEETLE_COUNTY_ADMIN = 11; // Norway only


    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "vips_logic_role_id")
    private Integer vipsLogicRoleId;
    @Size(max = 255)
    @Column(name = "default_title")
    private String defaultTitle;
    @Size(max = 2147483647)
    @Column(name = "default_description")
    private String defaultDescription;
   

    public VipsLogicRole() {
    }

    public VipsLogicRole(Integer vipsLogicRoleId) {
        this.vipsLogicRoleId = vipsLogicRoleId;
    }

    public Integer getVipsLogicRoleId() {
        return vipsLogicRoleId;
    }

    public void setVipsLogicRoleId(Integer vipsLogicRoleId) {
        this.vipsLogicRoleId = vipsLogicRoleId;
    }

    public String getDefaultTitle() {
        return defaultTitle;
    }

    public void setDefaultTitle(String defaultTitle) {
        this.defaultTitle = defaultTitle;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }

    public void setDefaultDescription(String defaultDescription) {
        this.defaultDescription = defaultDescription;
    }

    

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (vipsLogicRoleId != null ? vipsLogicRoleId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof VipsLogicRole)) {
            return false;
        }
        VipsLogicRole other = (VipsLogicRole) object;
        if ((this.vipsLogicRoleId == null && other.vipsLogicRoleId != null) || (this.vipsLogicRoleId != null && !this.vipsLogicRoleId.equals(other.vipsLogicRoleId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "VipsLogicRole{" +
                "vipsLogicRoleId=" + vipsLogicRoleId +
                ", defaultTitle='" + defaultTitle + '\'' +
                '}';
    }
}
