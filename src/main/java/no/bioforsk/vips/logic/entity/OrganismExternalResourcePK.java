/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
