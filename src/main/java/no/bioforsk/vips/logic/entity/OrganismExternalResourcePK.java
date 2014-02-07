/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with VIPSLogic.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.entity;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Embeddable
public class OrganismExternalResourcePK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "organism_id")
    private int organismId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "external_resource_id")
    private int externalResourceId;

    public OrganismExternalResourcePK() {
    }

    public OrganismExternalResourcePK(int organismId, int externalResourceId) {
        this.organismId = organismId;
        this.externalResourceId = externalResourceId;
    }

    public int getOrganismId() {
        return organismId;
    }

    public void setOrganismId(int organismId) {
        this.organismId = organismId;
    }

    public int getExternalResourceId() {
        return externalResourceId;
    }

    public void setExternalResourceId(int externalResourceId) {
        this.externalResourceId = externalResourceId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) organismId;
        hash += (int) externalResourceId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof OrganismExternalResourcePK)) {
            return false;
        }
        OrganismExternalResourcePK other = (OrganismExternalResourcePK) object;
        if (this.organismId != other.organismId) {
            return false;
        }
        if (this.externalResourceId != other.externalResourceId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.OrganismExternalResourcePK[ organismId=" + organismId + ", externalResourceId=" + externalResourceId + " ]";
    }

}
