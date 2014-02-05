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
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@Table(name = "organism_external_resource")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "OrganismExternalResource.findAll", query = "SELECT o FROM OrganismExternalResource o"),
    @NamedQuery(name = "OrganismExternalResource.findByOrganismId", query = "SELECT o FROM OrganismExternalResource o WHERE o.organismExternalResourcePK.organismId = :organismId"),
    @NamedQuery(name = "OrganismExternalResource.findByExternalResourceId", query = "SELECT o FROM OrganismExternalResource o WHERE o.organismExternalResourcePK.externalResourceId = :externalResourceId"),
    @NamedQuery(name = "OrganismExternalResource.findByResourceIdentifier", query = "SELECT o FROM OrganismExternalResource o WHERE o.resourceIdentifier = :resourceIdentifier")})
public class OrganismExternalResource implements Serializable {
    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected OrganismExternalResourcePK organismExternalResourcePK;
    @Size(max = 255)
    @Column(name = "resource_identifier")
    private String resourceIdentifier;
    @JoinColumn(name = "organism_id", referencedColumnName = "organism_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Organism organism;
    @JoinColumn(name = "external_resource_id", referencedColumnName = "external_resource_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private ExternalResource externalResource;

    public OrganismExternalResource() {
    }

    public OrganismExternalResource(OrganismExternalResourcePK organismExternalResourcePK) {
        this.organismExternalResourcePK = organismExternalResourcePK;
    }

    public OrganismExternalResource(int organismId, int externalResourceId) {
        this.organismExternalResourcePK = new OrganismExternalResourcePK(organismId, externalResourceId);
    }

    public OrganismExternalResourcePK getOrganismExternalResourcePK() {
        return organismExternalResourcePK;
    }

    public void setOrganismExternalResourcePK(OrganismExternalResourcePK organismExternalResourcePK) {
        this.organismExternalResourcePK = organismExternalResourcePK;
    }

    public String getResourceIdentifier() {
        return resourceIdentifier;
    }

    public void setResourceIdentifier(String resourceIdentifier) {
        this.resourceIdentifier = resourceIdentifier;
    }

    public Organism getOrganism() {
        return organism;
    }

    public void setOrganism(Organism organism) {
        this.organism = organism;
    }

    public ExternalResource getExternalResource() {
        return externalResource;
    }

    public void setExternalResource(ExternalResource externalResource) {
        this.externalResource = externalResource;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (organismExternalResourcePK != null ? organismExternalResourcePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof OrganismExternalResource)) {
            return false;
        }
        OrganismExternalResource other = (OrganismExternalResource) object;
        if ((this.organismExternalResourcePK == null && other.organismExternalResourcePK != null) || (this.organismExternalResourcePK != null && !this.organismExternalResourcePK.equals(other.organismExternalResourcePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.OrganismExternalResource[ organismExternalResourcePK=" + organismExternalResourcePK + " ]";
    }

    @Transient
    public String getResourceUrl()
    {
        StringBuilder retVal = new StringBuilder()
                .append(this.getExternalResource().getUri())
                .append(this.getExternalResource().getIdentifierTemplate());
        return String.format(retVal.toString(), this.getResourceIdentifier());
    }
}
