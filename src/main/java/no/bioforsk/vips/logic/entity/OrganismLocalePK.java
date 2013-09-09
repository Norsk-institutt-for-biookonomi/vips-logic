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
import javax.validation.constraints.Size;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Embeddable
public class OrganismLocalePK implements Serializable {
    @Basic(optional = false)
    @NotNull
    @Column(name = "organism_id")
    private int organismId;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10)
    @Column(name = "locale")
    private String locale;

    public OrganismLocalePK() {
    }

    public OrganismLocalePK(int organismId, String locale) {
        this.organismId = organismId;
        this.locale = locale;
    }

    public int getOrganismId() {
        return organismId;
    }

    public void setOrganismId(int organismId) {
        this.organismId = organismId;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) organismId;
        hash += (locale != null ? locale.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof OrganismLocalePK)) {
            return false;
        }
        OrganismLocalePK other = (OrganismLocalePK) object;
        if (this.organismId != other.organismId) {
            return false;
        }
        if ((this.locale == null && other.locale != null) || (this.locale != null && !this.locale.equals(other.locale))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.OrganismLocalePK[ organismId=" + organismId + ", locale=" + locale + " ]";
    }

}
