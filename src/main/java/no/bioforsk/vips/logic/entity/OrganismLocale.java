/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
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
    @JoinColumn(name = "organism_id", referencedColumnName = "organism_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Organism organism;

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

    public Organism getOrganism() {
        return organism;
    }

    public void setOrganism(Organism organism) {
        this.organism = organism;
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
        return "no.bioforsk.vips.logic.entity.OrganismLocale[ organismLocalePK=" + organismLocalePK + " ]";
    }

}
