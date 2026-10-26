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

package no.nibio.vips.logic.entity;

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
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "organization_group")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "OrganizationGroup.findAll", query = "SELECT o FROM OrganizationGroup o"),
    @NamedQuery(name = "OrganizationGroup.findByOrganizationGroupId", query = "SELECT o FROM OrganizationGroup o WHERE o.organizationGroupId = :organizationGroupId"),
    @NamedQuery(name = "OrganizationGroup.findByOrganizationId", query = "SELECT o FROM OrganizationGroup o WHERE o.organizationId = :organizationId"),
    @NamedQuery(name = "OrganizationGroup.findByGroupName", query = "SELECT o FROM OrganizationGroup o WHERE o.groupName = :groupName")})
public class OrganizationGroup implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "organization_group_id")
    private Integer organizationGroupId;
    @Size(max = 255)
    @Column(name = "group_name")
    private String groupName;
    @Column(name = "organization_id")
    private Integer organizationId;

    public OrganizationGroup() {
    }

    public OrganizationGroup(Integer organizationGroupId) {
        this.organizationGroupId = organizationGroupId;
    }

    public Integer getOrganizationGroupId() {
        return organizationGroupId;
    }

    public void setOrganizationGroupId(Integer organizationGroupId) {
        this.organizationGroupId = organizationGroupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (organizationGroupId != null ? organizationGroupId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof OrganizationGroup)) {
            return false;
        }
        OrganizationGroup other = (OrganizationGroup) object;
        if ((this.organizationGroupId == null && other.organizationGroupId != null) || (this.organizationGroupId != null && !this.organizationGroupId.equals(other.organizationGroupId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.OrganizationGroup[ organizationGroupId=" + organizationGroupId + " ]";
    }

    /**
     * @return the organizationId
     */
    public Integer getOrganizationId() {
        return organizationId;
    }

    /**
     * @param organizationId the organizationId to set
     */
    public void setOrganizationId(Integer organizationId) {
        this.organizationId = organizationId;
    }

}
