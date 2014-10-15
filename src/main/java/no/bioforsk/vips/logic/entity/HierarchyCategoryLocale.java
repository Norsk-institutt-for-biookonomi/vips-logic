/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the Bioforsk Open Source License as published by 
 * Bioforsk, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Bioforsk Open Source License for more details.
 * 
 * You should have received a copy of the Bioforsk Open Source License
 * along with VIPSLogic.  If not, see <http://www.bioforsk.no/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2014 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
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
        return "no.bioforsk.vips.logic.entity.HierarchyCategoryLocale[ hierarchyCategoryLocalePK=" + hierarchyCategoryLocalePK + " ]";
    }

}
