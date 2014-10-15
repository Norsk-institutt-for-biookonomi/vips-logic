/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the Bioforsk Open Source License as published by 
 * Bioforsk, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Bioforsk Open Source License for more details.
 * 
 * You should have received a copy of the Bioforsk Open Source License
 * along with VIPSLogic.  If not, see <http://www.bioforsk.no/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.entity;

import java.io.Serializable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@Table(name = "user_point_of_interest")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UserPointOfInterest.findAll", query = "SELECT u FROM UserPointOfInterest u"),
    @NamedQuery(name = "UserPointOfInterest.findByPointOfInterestId", query = "SELECT u FROM UserPointOfInterest u WHERE u.userPointOfInterestPK.pointOfInterestId = :pointOfInterestId"),
    @NamedQuery(name = "UserPointOfInterest.findByUserId", query = "SELECT u FROM UserPointOfInterest u WHERE u.userPointOfInterestPK.userId = :userId")})
public class UserPointOfInterest implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected UserPointOfInterestPK userPointOfInterestPK;
    @JoinColumn(name = "point_of_interest_id", referencedColumnName = "point_of_interest_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private PointOfInterest pointOfInterest;

    public UserPointOfInterest() {
    }

    public UserPointOfInterest(UserPointOfInterestPK userPointOfInterestPK) {
        this.userPointOfInterestPK = userPointOfInterestPK;
    }

    public UserPointOfInterest(int pointOfInterestId, int userId) {
        this.userPointOfInterestPK = new UserPointOfInterestPK(pointOfInterestId, userId);
    }

    public UserPointOfInterestPK getUserPointOfInterestPK() {
        return userPointOfInterestPK;
    }

    public void setUserPointOfInterestPK(UserPointOfInterestPK userPointOfInterestPK) {
        this.userPointOfInterestPK = userPointOfInterestPK;
    }

    public PointOfInterest getPointOfInterest() {
        return pointOfInterest;
    }

    public void setPointOfInterest(PointOfInterest pointOfInterest) {
        this.pointOfInterest = pointOfInterest;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userPointOfInterestPK != null ? userPointOfInterestPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserPointOfInterest)) {
            return false;
        }
        UserPointOfInterest other = (UserPointOfInterest) object;
        if ((this.userPointOfInterestPK == null && other.userPointOfInterestPK != null) || (this.userPointOfInterestPK != null && !this.userPointOfInterestPK.equals(other.userPointOfInterestPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.UserPointOfInterest[ userPointOfInterestPK=" + userPointOfInterestPK + " ]";
    }

}
