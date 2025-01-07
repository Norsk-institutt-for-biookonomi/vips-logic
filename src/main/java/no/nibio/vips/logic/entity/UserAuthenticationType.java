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
import java.util.Set;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "user_authentication_type")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UserAuthenticationType.findAll", query = "SELECT u FROM UserAuthenticationType u"),
    @NamedQuery(name = "UserAuthenticationType.findByUserAuthenticationTypeId", query = "SELECT u FROM UserAuthenticationType u WHERE u.userAuthenticationTypeId = :userAuthenticationTypeId"),
    @NamedQuery(name = "UserAuthenticationType.findByName", query = "SELECT u FROM UserAuthenticationType u WHERE u.name = :name"),
    @NamedQuery(name = "UserAuthenticationType.findByDescription", query = "SELECT u FROM UserAuthenticationType u WHERE u.description = :description")})
public class UserAuthenticationType implements Serializable {
    
    public static Integer TYPE_PASSWORD = 1;
    public static Integer TYPE_OPENID = 2;
    public static Integer TYPE_OPENID_GOOGLE = 3;
    
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "user_authentication_type_id")
    private Integer userAuthenticationTypeId;
    @Size(max = 63)
    @Column(name = "name")
    private String name;
    @Size(max = 1023)
    @Column(name = "description")
    private String description;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userAuthenticationType")
    private Set<UserAuthentication> userAuthenticationSet;

    public UserAuthenticationType() {
    }

    public UserAuthenticationType(Integer userAuthenticationTypeId) {
        this.userAuthenticationTypeId = userAuthenticationTypeId;
    }

    public Integer getUserAuthenticationTypeId() {
        return userAuthenticationTypeId;
    }

    public void setUserAuthenticationTypeId(Integer userAuthenticationTypeId) {
        this.userAuthenticationTypeId = userAuthenticationTypeId;
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

    @XmlTransient
    @JsonIgnore
    public Set<UserAuthentication> getUserAuthenticationSet() {
        return userAuthenticationSet;
    }

    public void setUserAuthenticationSet(Set<UserAuthentication> userAuthenticationSet) {
        this.userAuthenticationSet = userAuthenticationSet;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userAuthenticationTypeId != null ? userAuthenticationTypeId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserAuthenticationType)) {
            return false;
        }
        UserAuthenticationType other = (UserAuthenticationType) object;
        if ((this.userAuthenticationTypeId == null && other.userAuthenticationTypeId != null) || (this.userAuthenticationTypeId != null && !this.userAuthenticationTypeId.equals(other.userAuthenticationTypeId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.UserAuthenticationType[ userAuthenticationTypeId=" + userAuthenticationTypeId + " ]";
    }

}
