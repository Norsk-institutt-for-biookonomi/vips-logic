/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package no.bioforsk.vips.logic.entity;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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
@Table(name = "hierarchy_category")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "HierarchyCategory.findAll", query = "SELECT h FROM HierarchyCategory h"),
    @NamedQuery(name = "HierarchyCategory.findByHierarchyCategoryId", query = "SELECT h FROM HierarchyCategory h WHERE h.hierarchyCategoryId = :hierarchyCategoryId"),
    @NamedQuery(name = "HierarchyCategory.findByDefaultName", query = "SELECT h FROM HierarchyCategory h WHERE h.defaultName = :defaultName"),
    @NamedQuery(name = "HierarchyCategory.findByLogicallyDeleted", query = "SELECT h FROM HierarchyCategory h WHERE h.logicallyDeleted = :logicallyDeleted")})
public class HierarchyCategory implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "hierarchy_category_id")
    private Integer hierarchyCategoryId;
    @Size(max = 63)
    @Column(name = "default_name")
    private String defaultName;
    @Basic(optional = false)
    @NotNull
    @Column(name = "logically_deleted")
    private boolean logicallyDeleted;
    @OneToMany(mappedBy = "hierarchyCategoryId")
    private Set<Organism> organismSet;

    public HierarchyCategory() {
    }

    public HierarchyCategory(Integer hierarchyCategoryId) {
        this.hierarchyCategoryId = hierarchyCategoryId;
    }

    public HierarchyCategory(Integer hierarchyCategoryId, boolean logicallyDeleted) {
        this.hierarchyCategoryId = hierarchyCategoryId;
        this.logicallyDeleted = logicallyDeleted;
    }

    public Integer getHierarchyCategoryId() {
        return hierarchyCategoryId;
    }

    public void setHierarchyCategoryId(Integer hierarchyCategoryId) {
        this.hierarchyCategoryId = hierarchyCategoryId;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    public boolean getLogicallyDeleted() {
        return logicallyDeleted;
    }

    public void setLogicallyDeleted(boolean logicallyDeleted) {
        this.logicallyDeleted = logicallyDeleted;
    }

    @XmlTransient
    @JsonIgnore
    public Set<Organism> getOrganismSet() {
        return organismSet;
    }

    public void setOrganismSet(Set<Organism> organismSet) {
        this.organismSet = organismSet;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (hierarchyCategoryId != null ? hierarchyCategoryId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof HierarchyCategory)) {
            return false;
        }
        HierarchyCategory other = (HierarchyCategory) object;
        if ((this.hierarchyCategoryId == null && other.hierarchyCategoryId != null) || (this.hierarchyCategoryId != null && !this.hierarchyCategoryId.equals(other.hierarchyCategoryId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.HierarchyCategory[ hierarchyCategoryId=" + hierarchyCategoryId + " ]";
    }

}
