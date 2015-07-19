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
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2013-2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "organism_locale")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "OrganismLocale.findAll", query = "SELECT o FROM OrganismLocale o"),
    @NamedQuery(name = "OrganismLocale.findByOrganismId", query = "SELECT o FROM OrganismLocale o WHERE o.organismLocalePK.organismId = :organismId"),
    @NamedQuery(name = "OrganismLocale.findByLocale", query = "SELECT o FROM OrganismLocale o WHERE o.organismLocalePK.locale = :locale"),
    @NamedQuery(name = "OrganismLocale.findByLocalName", query = "SELECT o FROM OrganismLocale o WHERE o.localName = :localName")})
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
