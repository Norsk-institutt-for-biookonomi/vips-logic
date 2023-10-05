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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "observation_status_type")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ObservationStatusType.findAll", query = "SELECT o FROM ObservationStatusType o"),
    @NamedQuery(name = "ObservationStatusType.findByStatusTypeId", query = "SELECT o FROM ObservationStatusType o WHERE o.statusTypeId = :statusTypeId"),
    @NamedQuery(name = "ObservationStatusType.findByStatusTitle", query = "SELECT o FROM ObservationStatusType o WHERE o.statusTitle = :statusTitle")})
public class ObservationStatusType implements Serializable {
    
    public static final Integer STATUS_PENDING = 1;
    public static final Integer STATUS_REJECTED = 2;
    public static final Integer STATUS_APPROVED = 3;

    

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "status_type_id")
    private Integer statusTypeId;
    @Size(max = 63)
    @Column(name = "status_title")
    private String statusTitle;

    public ObservationStatusType() {
    }

    public ObservationStatusType(Integer statusTypeId) {
        this.statusTypeId = statusTypeId;
    }

    public Integer getStatusTypeId() {
        return statusTypeId;
    }

    public void setStatusTypeId(Integer statusTypeId) {
        this.statusTypeId = statusTypeId;
    }

    public String getStatusTitle() {
        return statusTitle;
    }

    public void setStatusTitle(String statusTitle) {
        this.statusTitle = statusTitle;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (statusTypeId != null ? statusTypeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ObservationStatusType)) {
            return false;
        }
        ObservationStatusType other = (ObservationStatusType) object;
        if ((this.statusTypeId == null && other.statusTypeId != null) || (this.statusTypeId != null && !this.statusTypeId.equals(other.statusTypeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.ObservationStatusType[ statusTypeId=" + statusTypeId + " ]";
    }

}
