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
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "user_authentication")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UserAuthentication.findAll", query = "SELECT u FROM UserAuthentication u"),
    @NamedQuery(name = "UserAuthentication.findByUserId", query = "SELECT u FROM UserAuthentication u WHERE u.userAuthenticationPK.userId = :userId"),
    @NamedQuery(name = "UserAuthentication.findByUserAuthenticationTypeId", query = "SELECT u FROM UserAuthentication u WHERE u.userAuthenticationPK.userAuthenticationTypeId = :userAuthenticationTypeId"),
    @NamedQuery(name = "UserAuthentication.findByUsername", query = "SELECT u FROM UserAuthentication u WHERE u.username = :username"),
    @NamedQuery(name = "UserAuthentication.findByPasswordResetCode", query = "SELECT u FROM UserAuthentication u WHERE u.passwordResetCode = :passwordResetCode"),
    @NamedQuery(name = "UserAuthentication.findByUsernameAndPassword", query = "SELECT u FROM UserAuthentication u WHERE u.username = :username AND u.password = :password"),
    @NamedQuery(name = "UserAuthentication.findByPassword", query = "SELECT u FROM UserAuthentication u WHERE u.password = :password")})
public class UserAuthentication implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected UserAuthenticationPK userAuthenticationPK;
    @Size(max = 2047)
    @Column(name = "username")
    private String username;
    @Size(max = 63)
    @Column(name = "password")
    private String password;
    @Size(max = 255)
    @Column(name = "password_reset_code")
    private String passwordResetCode;
    @Column(name = "password_reset_code_creation_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date passwordResetCodeCreationTime;
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private VipsLogicUser vipsLogicUser;
    @JoinColumn(name = "user_authentication_type_id", referencedColumnName = "user_authentication_type_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private UserAuthenticationType userAuthenticationType;

    public UserAuthentication() {
    }

    public UserAuthentication(UserAuthenticationPK userAuthenticationPK) {
        this.userAuthenticationPK = userAuthenticationPK;
    }

    public UserAuthentication(int userId, int userAuthenticationTypeId) {
        this.userAuthenticationPK = new UserAuthenticationPK(userId, userAuthenticationTypeId);
    }

    public UserAuthenticationPK getUserAuthenticationPK() {
        return userAuthenticationPK;
    }

    public void setUserAuthenticationPK(UserAuthenticationPK userAuthenticationPK) {
        this.userAuthenticationPK = userAuthenticationPK;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public VipsLogicUser getVipsLogicUser() {
        return vipsLogicUser;
    }

    public void setVipsLogicUser(VipsLogicUser vipsLogicUser) {
        this.vipsLogicUser = vipsLogicUser;
    }

    public UserAuthenticationType getUserAuthenticationType() {
        return userAuthenticationType;
    }

    public void setUserAuthenticationType(UserAuthenticationType userAuthenticationType) {
        this.userAuthenticationType = userAuthenticationType;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userAuthenticationPK != null ? userAuthenticationPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserAuthentication)) {
            return false;
        }
        UserAuthentication other = (UserAuthentication) object;
        if ((this.userAuthenticationPK == null && other.userAuthenticationPK != null) || (this.userAuthenticationPK != null && !this.userAuthenticationPK.equals(other.userAuthenticationPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.UserAuthentication[ userAuthenticationPK=" + userAuthenticationPK + " ]";
    }

    /**
     * @return the passwordResetCode
     */
    public String getPasswordResetCode() {
        return passwordResetCode;
    }

    /**
     * @param passwordResetCode the passwordResetCode to set
     */
    public void setPasswordResetCode(String passwordResetCode) {
        this.passwordResetCode = passwordResetCode;
    }

    /**
     * @return the passwordResetCodeCreationTime
     */
    public Date getPasswordResetCodeCreationTime() {
        return passwordResetCodeCreationTime;
    }

    /**
     * @param passwordResetCodeCreationTime the passwordResetCodeCreationTime to set
     */
    public void setPasswordResetCodeCreationTime(Date passwordResetCodeCreationTime) {
        this.passwordResetCodeCreationTime = passwordResetCodeCreationTime;
    }
    
}
