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
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2013-2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "organism_locale")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "OrganismLocale.findAll", query = "SELECT o FROM OrganismLocale o"),
    @NamedQuery(name = "OrganismLocale.findByOrganismId", query = "SELECT o FROM OrganismLocale o WHERE o.organismLocalePK.organismId = :organismId"),
    @NamedQuery(name = "OrganismLocale.findByLocale", query = "SELECT o FROM OrganismLocale o WHERE o.organismLocalePK.locale = :locale"),
    @NamedQuery(name = "OrganismLocale.findByLocalName", query = "SELECT o FROM OrganismLocale o WHERE o.localName = :localName")//,
})
public class OrganismLocale implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected OrganismLocalePK organismLocalePK;
    @Size(max = 255)
    @Column(name = "local_name")
    private String localName;
    @Column(name = "organism_id", insertable = false, updatable = false)
    private Integer organismId;
    
    public OrganismLocale() {
    }

    public OrganismLocale(OrganismLocalePK organismLocalePK) {
        this.organismLocalePK = organismLocalePK;
    }

    public OrganismLocale(int organismId, String locale) {
        this.organismLocalePK = new OrganismLocalePK(organismId, locale);
    }

    public OrganismLocalePK getOrganismLocalePK() {
        return organismLocalePK;
    }

    public void setOrganismLocalePK(OrganismLocalePK organismLocalePK) {
        this.organismLocalePK = organismLocalePK;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (organismLocalePK != null ? organismLocalePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof OrganismLocale)) {
            return false;
        }
        OrganismLocale other = (OrganismLocale) object;
        if ((this.organismLocalePK == null && other.organismLocalePK != null) || (this.organismLocalePK != null && !this.organismLocalePK.equals(other.organismLocalePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.OrganismLocale[ organismLocalePK=" + organismLocalePK + " ]";
    }

}
