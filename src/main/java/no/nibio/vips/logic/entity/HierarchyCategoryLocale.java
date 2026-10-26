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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "hierarchy_category_locale")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "HierarchyCategoryLocale.findAll", query = "SELECT h FROM HierarchyCategoryLocale h"),
    @NamedQuery(name = "HierarchyCategoryLocale.findByHierarchyCategoryId", query = "SELECT h FROM HierarchyCategoryLocale h WHERE h.hierarchyCategoryLocalePK.hierarchyCategoryId = :hierarchyCategoryId"),
    @NamedQuery(name = "HierarchyCategoryLocale.findByLocale", query = "SELECT h FROM HierarchyCategoryLocale h WHERE h.hierarchyCategoryLocalePK.locale = :locale"),
    @NamedQuery(name = "HierarchyCategoryLocale.findByLocalName", query = "SELECT h FROM HierarchyCategoryLocale h WHERE h.localName = :localName")})
public class HierarchyCategoryLocale implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected HierarchyCategoryLocalePK hierarchyCategoryLocalePK;
    @Size(max = 255)
    @Column(name = "local_name")
    private String localName;
    @Column(name = "hierarchy_category_id", insertable = false, updatable = false)
    private Integer hierarchyCategoryId;

    public HierarchyCategoryLocale() {
    }

    public HierarchyCategoryLocale(HierarchyCategoryLocalePK hierarchyCategoryLocalePK) {
        this.hierarchyCategoryLocalePK = hierarchyCategoryLocalePK;
    }

    public HierarchyCategoryLocale(int hierarchyCategoryId, String locale) {
        this.hierarchyCategoryLocalePK = new HierarchyCategoryLocalePK(hierarchyCategoryId, locale);
    }

    public HierarchyCategoryLocalePK getHierarchyCategoryLocalePK() {
        return hierarchyCategoryLocalePK;
    }

    public void setHierarchyCategoryLocalePK(HierarchyCategoryLocalePK hierarchyCategoryLocalePK) {
        this.hierarchyCategoryLocalePK = hierarchyCategoryLocalePK;
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
        hash += (hierarchyCategoryLocalePK != null ? hierarchyCategoryLocalePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof HierarchyCategoryLocale)) {
            return false;
        }
        HierarchyCategoryLocale other = (HierarchyCategoryLocale) object;
        if ((this.hierarchyCategoryLocalePK == null && other.hierarchyCategoryLocalePK != null) || (this.hierarchyCategoryLocalePK != null && !this.hierarchyCategoryLocalePK.equals(other.hierarchyCategoryLocalePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.HierarchyCategoryLocale[ hierarchyCategoryLocalePK=" + hierarchyCategoryLocalePK + " ]";
    }

}
