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
import javax.persistence.*;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2023 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "vipscore_instance")
@NamedQueries({
        @NamedQuery(name = "VipsCoreInstance.findAll", query = "SELECT v FROM VipsCoreInstance v")
})
@XmlRootElement
public class VipsCoreInstance implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "vipscore_instance_id")
    private Integer vipscoreInstanceId;
    @Size(max = 255)
    @Column(name = "uri")
    private String uri;
    @Size(max = 255)
    @Column(name = "username")
    private String username;
    @Size(max = 255)
    @Column(name = "password")
    private String password;


    public VipsCoreInstance() {
    }

    public VipsCoreInstance(Integer vipscoreInstanceId) {
        this.setVipscoreInstanceId(vipscoreInstanceId);
    }


    @Override
    public int hashCode() {
        int hash = 0;
        hash += (getVipscoreInstanceId() != null ? getVipscoreInstanceId().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof VipsCoreInstance)) {
            return false;
        }
        VipsCoreInstance other = (VipsCoreInstance) object;
        if ((this.getVipscoreInstanceId() == null && other.getVipscoreInstanceId() != null) || (this.getVipscoreInstanceId() != null && !this.getVipscoreInstanceId().equals(other.getVipscoreInstanceId()))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.VIPSCoreInstance[ vipsCoreInstanceId=" + vipscoreInstanceId + " ]";
    }

    public Integer getVipscoreInstanceId() {
        return vipscoreInstanceId;
    }

    public void setVipscoreInstanceId(Integer vipscoreInstanceId) {
        this.vipscoreInstanceId = vipscoreInstanceId;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
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
}
