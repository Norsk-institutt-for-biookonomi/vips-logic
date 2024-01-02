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

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Embeddable
public class PointOfInterestExternalResourcePK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "point_of_interest_id")
    private int pointOfInterestId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "external_resource_id")
    private int externalResourceId;

    public PointOfInterestExternalResourcePK() {
    }

    public PointOfInterestExternalResourcePK(int pointOfInterestId, int externalResourceId) {
        this.pointOfInterestId = pointOfInterestId;
        this.externalResourceId = externalResourceId;
    }

    public int getPointOfInterestId() {
        return pointOfInterestId;
    }

    public void setPointOfInterestId(int pointOfInterestId) {
        this.pointOfInterestId = pointOfInterestId;
    }

    public int getExternalResourceId() {
        return externalResourceId;
    }

    public void setExternalResourceId(int externalResourceId) {
        this.externalResourceId = externalResourceId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) pointOfInterestId;
        hash += (int) externalResourceId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PointOfInterestExternalResourcePK)) {
            return false;
        }
        PointOfInterestExternalResourcePK other = (PointOfInterestExternalResourcePK) object;
        if (this.pointOfInterestId != other.pointOfInterestId) {
            return false;
        }
        if (this.externalResourceId != other.externalResourceId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.PointOfInterestExternalResourcePK[ pointOfInterestId=" + pointOfInterestId + ", externalResourceId=" + externalResourceId + " ]";
    }

}
