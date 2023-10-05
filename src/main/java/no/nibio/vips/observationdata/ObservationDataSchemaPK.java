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

package no.nibio.vips.observationdata;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Embeddable
public class ObservationDataSchemaPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "organization_id")
    private int organizationId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "organism_id")
    private int organismId;

    public ObservationDataSchemaPK() {
    }

    public ObservationDataSchemaPK(int organizationId, int organismId) {
        this.organizationId = organizationId;
        this.organismId = organismId;
    }

    public int getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(int organizationId) {
        this.organizationId = organizationId;
    }

    public int getOrganismId() {
        return organismId;
    }

    public void setOrganismId(int organismId) {
        this.organismId = organismId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) organizationId;
        hash += (int) organismId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ObservationDataSchemaPK)) {
            return false;
        }
        ObservationDataSchemaPK other = (ObservationDataSchemaPK) object;
        if (this.organizationId != other.organizationId) {
            return false;
        }
        if (this.organismId != other.organismId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.observationdata.ObservationDataSchemaPK[ organizationId=" + organizationId + ", organismId=" + organismId + " ]";
    }

}
