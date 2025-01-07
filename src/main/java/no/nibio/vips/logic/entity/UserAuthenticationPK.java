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
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

/**
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Embeddable
public class UserAuthenticationPK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "user_id")
    private int userId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "user_authentication_type_id")
    private int userAuthenticationTypeId;

    public UserAuthenticationPK() {
    }

    public UserAuthenticationPK(int userId, int userAuthenticationTypeId) {
        this.userId = userId;
        this.userAuthenticationTypeId = userAuthenticationTypeId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserAuthenticationTypeId() {
        return userAuthenticationTypeId;
    }

    public void setUserAuthenticationTypeId(int userAuthenticationTypeId) {
        this.userAuthenticationTypeId = userAuthenticationTypeId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) userId;
        hash += (int) userAuthenticationTypeId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserAuthenticationPK)) {
            return false;
        }
        UserAuthenticationPK other = (UserAuthenticationPK) object;
        if (this.userId != other.userId) {
            return false;
        }
        if (this.userAuthenticationTypeId != other.userAuthenticationTypeId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.UserAuthenticationPK[ userId=" + userId + ", userAuthenticationTypeId=" + userAuthenticationTypeId + " ]";
    }

}
