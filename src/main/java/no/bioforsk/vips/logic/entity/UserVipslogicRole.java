/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
@Table(name = "user_vipslogic_role")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UserVipslogicRole.findAll", query = "SELECT u FROM UserVipslogicRole u"),
    @NamedQuery(name = "UserVipslogicRole.findByVipslogicRoleId", query = "SELECT u FROM UserVipslogicRole u WHERE u.userVipslogicRolePK.vipslogicRoleId = :vipslogicRoleId"),
    @NamedQuery(name = "UserVipslogicRole.findByUserId", query = "SELECT u FROM UserVipslogicRole u WHERE u.userVipslogicRolePK.userId = :userId")})
public class UserVipslogicRole implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected UserVipslogicRolePK userVipslogicRolePK;
    @JoinColumn(name = "vipslogic_role_id", referencedColumnName = "vipslogic_role_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private VipslogicRole vipslogicRole;

    public UserVipslogicRole() {
    }

    public UserVipslogicRole(UserVipslogicRolePK userVipslogicRolePK) {
        this.userVipslogicRolePK = userVipslogicRolePK;
    }

    public UserVipslogicRole(int vipslogicRoleId, int userId) {
        this.userVipslogicRolePK = new UserVipslogicRolePK(vipslogicRoleId, userId);
    }

    public UserVipslogicRolePK getUserVipslogicRolePK() {
        return userVipslogicRolePK;
    }

    public void setUserVipslogicRolePK(UserVipslogicRolePK userVipslogicRolePK) {
        this.userVipslogicRolePK = userVipslogicRolePK;
    }

    public VipslogicRole getVipslogicRole() {
        return vipslogicRole;
    }

    public void setVipslogicRole(VipslogicRole vipslogicRole) {
        this.vipslogicRole = vipslogicRole;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userVipslogicRolePK != null ? userVipslogicRolePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserVipslogicRole)) {
            return false;
        }
        UserVipslogicRole other = (UserVipslogicRole) object;
        if ((this.userVipslogicRolePK == null && other.userVipslogicRolePK != null) || (this.userVipslogicRolePK != null && !this.userVipslogicRolePK.equals(other.userVipslogicRolePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.UserVipslogicRole[ userVipslogicRolePK=" + userVipslogicRolePK + " ]";
    }

}
