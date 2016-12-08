/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the NIBIO Open Source License as published by 
 * NIBIO, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * NIBIO Open Source License for more details.
 * 
 * You should have received a copy of the NIBIO Open Source License
 * along with VIPSLogic.  If not, see <http://www.nibio.no/licenses/>.
 * 
 */

package no.nibio.vips.logic.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Embeddable
public class UserPointOfInterestPK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "point_of_interest_id")
    private int pointOfInterestId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "user_id")
    private int userId;

    public UserPointOfInterestPK() {
    }

    public UserPointOfInterestPK(int pointOfInterestId, int userId) {
        this.pointOfInterestId = pointOfInterestId;
        this.userId = userId;
    }

    public int getPointOfInterestId() {
        return pointOfInterestId;
    }

    public void setPointOfInterestId(int pointOfInterestId) {
        this.pointOfInterestId = pointOfInterestId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) pointOfInterestId;
        hash += (int) userId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserPointOfInterestPK)) {
            return false;
        }
        UserPointOfInterestPK other = (UserPointOfInterestPK) object;
        if (this.pointOfInterestId != other.pointOfInterestId) {
            return false;
        }
        if (this.userId != other.userId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.UserPointOfInterestPK[ pointOfInterestId=" + pointOfInterestId + ", userId=" + userId + " ]";
    }

}
