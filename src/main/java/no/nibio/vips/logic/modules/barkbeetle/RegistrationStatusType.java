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
@Table(name = "registration_status_type", schema="barkbeetle")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RegistrationStatusType.findAll", query = "SELECT r FROM RegistrationStatusType r"),
    @NamedQuery(name = "RegistrationStatusType.findByRegistrationStatusTypeId", query = "SELECT r FROM RegistrationStatusType r WHERE r.registrationStatusTypeId = :registrationStatusTypeId"),
    @NamedQuery(name = "RegistrationStatusType.findByTitle", query = "SELECT r FROM RegistrationStatusType r WHERE r.title = :title")})
public class RegistrationStatusType implements Serializable {
    
    public final static Integer NOT_EVALUATED = 1;
    public final static Integer APPROVED = 2;
    public final static Integer NOT_APPROVED = 3;

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "registration_status_type_id")
    private Integer registrationStatusTypeId;
    @Size(max = 255)
    @Column(name = "title")
    private String title;

    public RegistrationStatusType() {
    }

    public RegistrationStatusType(Integer registrationStatusId) {
        this.registrationStatusTypeId = registrationStatusId;
    }

    public Integer getRegistrationStatusTypeId() {
        return registrationStatusTypeId;
    }

    public void setRegistrationStatusTypeId(Integer registrationStatusTypeId) {
        this.registrationStatusTypeId = registrationStatusTypeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (registrationStatusTypeId != null ? registrationStatusTypeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RegistrationStatusType)) {
            return false;
        }
        RegistrationStatusType other = (RegistrationStatusType) object;
        if ((this.registrationStatusTypeId == null && other.registrationStatusTypeId != null) || (this.registrationStatusTypeId != null && !this.registrationStatusTypeId.equals(other.registrationStatusTypeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.modules.barkbeetle.RegistrationStatusType[ registrationStatusId=" + registrationStatusTypeId + " ]";
    }

}
