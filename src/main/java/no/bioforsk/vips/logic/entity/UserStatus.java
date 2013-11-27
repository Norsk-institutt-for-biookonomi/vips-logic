/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package no.bioforsk.vips.logic.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@Table(name = "user_status")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UserStatus.findAll", query = "SELECT u FROM UserStatus u"),
    @NamedQuery(name = "UserStatus.findByUserStatusId", query = "SELECT u FROM UserStatus u WHERE u.userStatusId = :userStatusId"),
    @NamedQuery(name = "UserStatus.findByName", query = "SELECT u FROM UserStatus u WHERE u.name = :name"),
    @NamedQuery(name = "UserStatus.findByDescription", query = "SELECT u FROM UserStatus u WHERE u.description = :description")})
public class UserStatus implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "user_status_id")
    private Integer userStatusId;
    @Size(max = 63)
    @Column(name = "name")
    private String name;
    @Size(max = 1023)
    @Column(name = "description")
    private String description;

    public UserStatus() {
    }

    public UserStatus(Integer userStatusId) {
        this.userStatusId = userStatusId;
    }

    public Integer getUserStatusId() {
        return userStatusId;
    }

    public void setUserStatusId(Integer userStatusId) {
        this.userStatusId = userStatusId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userStatusId != null ? userStatusId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserStatus)) {
            return false;
        }
        UserStatus other = (UserStatus) object;
        if ((this.userStatusId == null && other.userStatusId != null) || (this.userStatusId != null && !this.userStatusId.equals(other.userStatusId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.UserStatus[ userStatusId=" + userStatusId + " ]";
    }

}
