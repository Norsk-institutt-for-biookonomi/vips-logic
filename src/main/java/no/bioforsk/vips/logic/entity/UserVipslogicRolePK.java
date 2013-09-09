/*
 * To change this template, choose Tools | Templates
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
public class UserVipslogicRolePK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "vipslogic_role_id")
    private int vipslogicRoleId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "user_id")
    private int userId;

    public UserVipslogicRolePK() {
    }

    public UserVipslogicRolePK(int vipslogicRoleId, int userId) {
        this.vipslogicRoleId = vipslogicRoleId;
        this.userId = userId;
    }

    public int getVipslogicRoleId() {
        return vipslogicRoleId;
    }

    public void setVipslogicRoleId(int vipslogicRoleId) {
        this.vipslogicRoleId = vipslogicRoleId;
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
        hash += (int) vipslogicRoleId;
        hash += (int) userId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserVipslogicRolePK)) {
            return false;
        }
        UserVipslogicRolePK other = (UserVipslogicRolePK) object;
        if (this.vipslogicRoleId != other.vipslogicRoleId) {
            return false;
        }
        if (this.userId != other.userId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.UserVipslogicRolePK[ vipslogicRoleId=" + vipslogicRoleId + ", userId=" + userId + " ]";
    }

}
