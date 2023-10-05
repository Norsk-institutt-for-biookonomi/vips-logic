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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Embeddable
public class TrapsiteRegistrationPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "season_trapsite_id")
    private int seasonTrapsiteId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "week")
    private Integer week;

    public TrapsiteRegistrationPK() {
    }

    public TrapsiteRegistrationPK(int seasonTrapsiteId, int week) {
        this.seasonTrapsiteId = seasonTrapsiteId;
        this.week = week;
    }

    public int getSeasonTrapsiteId() {
        return seasonTrapsiteId;
    }

    public void setSeasonTrapsiteId(int seasonTrapsiteId) {
        this.seasonTrapsiteId = seasonTrapsiteId;
    }

    public Integer getWeek() {
        return week;
    }

    public void setWeek(Integer week) {
        this.week = week;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) seasonTrapsiteId;
        hash += (int) week;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TrapsiteRegistrationPK)) {
            return false;
        }
        TrapsiteRegistrationPK other = (TrapsiteRegistrationPK) object;
        if (this.seasonTrapsiteId != other.seasonTrapsiteId) {
            return false;
        }
        if (this.week != other.week) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.modules.barkbeetle.TrapsiteRegistrationPK[ seasonTrapsiteId=" + seasonTrapsiteId + ", week=" + week + " ]";
    }

}
