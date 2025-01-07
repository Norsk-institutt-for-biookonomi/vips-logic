/*
 * Copyright (c) 2021 NIBIO <http://www.nibio.no/>. 
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
import java.util.Date;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
