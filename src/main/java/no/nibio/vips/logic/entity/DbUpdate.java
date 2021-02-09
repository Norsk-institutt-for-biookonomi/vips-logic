/*
 * Copyright (c) 2021 NIBIO <http://www.nibio.no/>. 
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
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2021 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "db_update")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "DbUpdate.findAll", query = "SELECT d FROM DbUpdate d"),
    @NamedQuery(name = "DbUpdate.findByDbUpdateId", query = "SELECT d FROM DbUpdate d WHERE d.dbUpdateId = :dbUpdateId"),
    @NamedQuery(name = "DbUpdate.findByTitle", query = "SELECT d FROM DbUpdate d WHERE d.title = :title"),
    @NamedQuery(name = "DbUpdate.findByDescription", query = "SELECT d FROM DbUpdate d WHERE d.description = :description"),
    @NamedQuery(name = "DbUpdate.findByLastUpdate", query = "SELECT d FROM DbUpdate d WHERE d.lastUpdate = :lastUpdate")})
public class DbUpdate implements Serializable {
	
	public final static int CROP_CATEGORIES_AND_ORGANISMS = 1;

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "db_update_id")
    private Integer dbUpdateId;
    @Size(max = 255)
    @Column(name = "title")
    private String title;
    @Size(max = 2147483647)
    @Column(name = "description")
    private String description;
    @Column(name = "last_update")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;

    public DbUpdate() {
    }

    public DbUpdate(Integer dbUpdateId) {
        this.dbUpdateId = dbUpdateId;
    }

    public Integer getDbUpdateId() {
        return dbUpdateId;
    }

    public void setDbUpdateId(Integer dbUpdateId) {
        this.dbUpdateId = dbUpdateId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (dbUpdateId != null ? dbUpdateId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DbUpdate)) {
            return false;
        }
        DbUpdate other = (DbUpdate) object;
        if ((this.dbUpdateId == null && other.dbUpdateId != null) || (this.dbUpdateId != null && !this.dbUpdateId.equals(other.dbUpdateId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.DbUpdate[ dbUpdateId=" + dbUpdateId + " ]";
    }

}
