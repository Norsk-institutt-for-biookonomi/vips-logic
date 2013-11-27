/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package no.bioforsk.vips.logic.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
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
        return "no.bioforsk.vips.logic.entity.UserAuthenticationPK[ userId=" + userId + ", userAuthenticationTypeId=" + userAuthenticationTypeId + " ]";
    }

}
