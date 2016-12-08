/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "user_uuid")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UserUuid.findAll", query = "SELECT u FROM UserUuid u"),
    @NamedQuery(name = "UserUuid.findByUserId", query = "SELECT u FROM UserUuid u WHERE u.userUuidPK.userId = :userId"),
    @NamedQuery(name = "UserUuid.findByUserUUID", query = "SELECT u FROM UserUuid u WHERE u.userUuidPK.userUuid = :userUUID")
})
public class UserUuid implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected UserUuidPK userUuidPK;
    
    @Column(name = "expires_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiresAt;

    public UserUuid() {
    }

    public UserUuid(UserUuidPK userUuidPK) {
        this.userUuidPK = userUuidPK;
    }

    public UserUuid(UUID userUuid, int userId) {
        this.userUuidPK = new UserUuidPK(userUuid, userId);
    }

    public UserUuidPK getUserUuidPK() {
        return userUuidPK;
    }

    public void setUserUuidPK(UserUuidPK userUuidPK) {
        this.userUuidPK = userUuidPK;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userUuidPK != null ? userUuidPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserUuid)) {
            return false;
        }
        UserUuid other = (UserUuid) object;
        if ((this.userUuidPK == null && other.userUuidPK != null) || (this.userUuidPK != null && !this.userUuidPK.equals(other.userUuidPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.UserUuid[ userUuidPK=" + userUuidPK + " ]";
    }

    /**
     * @return the expiresAt
     */
    public Date getExpiresAt() {
        return expiresAt;
    }

    /**
     * @param expiresAt the expiresAt to set
     */
    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }

}
