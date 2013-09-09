/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package no.bioforsk.vips.logic.entity;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@Table(name = "organism")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Organism.findAll", query = "SELECT o FROM Organism o"),
    @NamedQuery(name = "Organism.findByOrganismId", query = "SELECT o FROM Organism o WHERE o.organismId = :organismId"),
    @NamedQuery(name = "Organism.findByLatinName", query = "SELECT o FROM Organism o WHERE o.latinName = :latinName"),
    @NamedQuery(name = "Organism.findByTradeName", query = "SELECT o FROM Organism o WHERE o.tradeName = :tradeName"),
    @NamedQuery(name = "Organism.findByLogicallyDeleted", query = "SELECT o FROM Organism o WHERE o.logicallyDeleted = :logicallyDeleted")})
public class Organism implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "organism_id")
    private Integer organismId;
    @Size(max = 255)
    @Column(name = "latin_name")
    private String latinName;
    @Size(max = 255)
    @Column(name = "trade_name")
    private String tradeName;
    @Basic(optional = false)
    @NotNull
    @Column(name = "logically_deleted")
    private boolean logicallyDeleted;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "organism")
    private Set<OrganismLocale> organismLocaleSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "organism")
    private Set<OrganismExternalResource> organismExternalResourceSet;
    @OneToMany(mappedBy = "parentOrganismId")
    private Set<Organism> organismSet;
    @JoinColumn(name = "parent_organism_id", referencedColumnName = "organism_id")
    @ManyToOne
    private Organism parentOrganismId;
    @JoinColumn(name = "hierarchy_category_id", referencedColumnName = "hierarchy_category_id")
    @ManyToOne
    private HierarchyCategory hierarchyCategoryId;

    public Organism() {
    }

    public Organism(Integer organismId) {
        this.organismId = organismId;
    }

    public Organism(Integer organismId, boolean logicallyDeleted) {
        this.organismId = organismId;
        this.logicallyDeleted = logicallyDeleted;
    }

    public Integer getOrganismId() {
        return organismId;
    }

    public void setOrganismId(Integer organismId) {
        this.organismId = organismId;
    }

    public String getLatinName() {
        return latinName;
    }

    public void setLatinName(String latinName) {
        this.latinName = latinName;
    }

    public String getTradeName() {
        return tradeName;
    }

    public void setTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public boolean getLogicallyDeleted() {
        return logicallyDeleted;
    }

    public void setLogicallyDeleted(boolean logicallyDeleted) {
        this.logicallyDeleted = logicallyDeleted;
    }

    @XmlTransient
    @JsonIgnore
    public Set<OrganismLocale> getOrganismLocaleSet() {
        return organismLocaleSet;
    }

    public void setOrganismLocaleSet(Set<OrganismLocale> organismLocaleSet) {
        this.organismLocaleSet = organismLocaleSet;
    }

    @XmlTransient
    @JsonIgnore
    public Set<OrganismExternalResource> getOrganismExternalResourceSet() {
        return organismExternalResourceSet;
    }

    public void setOrganismExternalResourceSet(Set<OrganismExternalResource> organismExternalResourceSet) {
        this.organismExternalResourceSet = organismExternalResourceSet;
    }

    @XmlTransient
    @JsonIgnore
    public Set<Organism> getOrganismSet() {
        return organismSet;
    }

    public void setOrganismSet(Set<Organism> organismSet) {
        this.organismSet = organismSet;
    }

    public Organism getParentOrganismId() {
        return parentOrganismId;
    }

    public void setParentOrganismId(Organism parentOrganismId) {
        this.parentOrganismId = parentOrganismId;
    }

    public HierarchyCategory getHierarchyCategoryId() {
        return hierarchyCategoryId;
    }

    public void setHierarchyCategoryId(HierarchyCategory hierarchyCategoryId) {
        this.hierarchyCategoryId = hierarchyCategoryId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (organismId != null ? organismId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Organism)) {
            return false;
        }
        Organism other = (Organism) object;
        if ((this.organismId == null && other.organismId != null) || (this.organismId != null && !this.organismId.equals(other.organismId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.Organism[ organismId=" + organismId + " ]";
    }

}
