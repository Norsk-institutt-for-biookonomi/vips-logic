/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package no.bioforsk.vips.logic.entity;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@Table(name = "vipslogic_role")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "VipslogicRole.findAll", query = "SELECT v FROM VipslogicRole v"),
    @NamedQuery(name = "VipslogicRole.findByVipslogicRoleId", query = "SELECT v FROM VipslogicRole v WHERE v.vipslogicRoleId = :vipslogicRoleId"),
    @NamedQuery(name = "VipslogicRole.findByDefaultTitle", query = "SELECT v FROM VipslogicRole v WHERE v.defaultTitle = :defaultTitle"),
    @NamedQuery(name = "VipslogicRole.findByDefaultDescription", query = "SELECT v FROM VipslogicRole v WHERE v.defaultDescription = :defaultDescription")})
public class VipslogicRole implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "vipslogic_role_id")
    private Integer vipslogicRoleId;
    @Size(max = 255)
    @Column(name = "default_title")
    private String defaultTitle;
    @Size(max = 2147483647)
    @Column(name = "default_description")
    private String defaultDescription;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "vipslogicRole")
    private Set<UserVipslogicRole> userVipslogicRoleSet;

    public VipslogicRole() {
    }

    public VipslogicRole(Integer vipslogicRoleId) {
        this.vipslogicRoleId = vipslogicRoleId;
    }

    public Integer getVipslogicRoleId() {
        return vipslogicRoleId;
    }

    public void setVipslogicRoleId(Integer vipslogicRoleId) {
        this.vipslogicRoleId = vipslogicRoleId;
    }

    public String getDefaultTitle() {
        return defaultTitle;
    }

    public void setDefaultTitle(String defaultTitle) {
        this.defaultTitle = defaultTitle;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }

    public void setDefaultDescription(String defaultDescription) {
        this.defaultDescription = defaultDescription;
    }

    @XmlTransient
    @JsonIgnore
    public Set<UserVipslogicRole> getUserVipslogicRoleSet() {
        return userVipslogicRoleSet;
    }

    public void setUserVipslogicRoleSet(Set<UserVipslogicRole> userVipslogicRoleSet) {
        this.userVipslogicRoleSet = userVipslogicRoleSet;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (vipslogicRoleId != null ? vipslogicRoleId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof VipslogicRole)) {
            return false;
        }
        VipslogicRole other = (VipslogicRole) object;
        if ((this.vipslogicRoleId == null && other.vipslogicRoleId != null) || (this.vipslogicRoleId != null && !this.vipslogicRoleId.equals(other.vipslogicRoleId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.VipslogicRole[ vipslogicRoleId=" + vipslogicRoleId + " ]";
    }

}
