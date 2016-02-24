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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2013-2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "organism")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Organism.findAll", query = "SELECT o FROM Organism o"),
    @NamedQuery(name = "Organism.findAllCrops", query = "SELECT o FROM Organism o WHERE o.isCrop = true AND o.logicallyDeleted = false"),
    @NamedQuery(name = "Organism.findAllPests", query = "SELECT o FROM Organism o WHERE o.isPest = true AND o.logicallyDeleted = false"),
    @NamedQuery(name = "Organism.findByOrganismId", query = "SELECT o FROM Organism o WHERE o.organismId = :organismId"),
    @NamedQuery(name = "Organism.findByOrganismIds", query = "SELECT o FROM Organism o WHERE o.organismId IN :organismIds"),
    @NamedQuery(name = "Organism.findByParentOrganismId", query = "SELECT o FROM Organism o WHERE o.parentOrganismId = :parentOrganismId"),
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
    @Column(name = "is_pest")
    private Boolean isPest;
    @Column(name = "is_crop")
    private Boolean isCrop;
    
    // We simplify this to avoid very large queries
    @Column(name = "parent_organism_id")
    private Integer parentOrganismId;
    @Column(name = "hierarchy_category_id")
    private Integer hierarchyCategoryId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "organismId", fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<OrganismLocale> organismLocaleSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "organism", fetch = FetchType.EAGER)
    private Set<OrganismExternalResource> organismExternalResourceSet;
    
    @Transient
    private List<Organism> childOrganisms;
    
    @Transient
    private Map<String,Object> extraProperties;
    
    /*@OneToMany(cascade = CascadeType.ALL, mappedBy = "organism")
    private Set<OrganismLocale> organismLocaleSet;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "organism")
    private Set<OrganismExternalResource> organismExternalResourceSet;
    @OneToMany(mappedBy = "parentOrganismId", fetch = FetchType.EAGER)
    private Set<Organism> organismSet;
    @JoinColumn(name = "parent_organism_id", referencedColumnName = "organism_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Organism parentOrganismId;
    @JoinColumn(name = "hierarchy_category_id", referencedColumnName = "hierarchy_category_id")
    @ManyToOne
    private HierarchyCategory hierarchyCategoryId;
    */

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
        return latinName != null ? latinName : "";
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

    
    public Set<OrganismLocale> getOrganismLocaleSet() {
        if(this.organismLocaleSet == null)
            this.organismLocaleSet = new HashSet<>();
        return organismLocaleSet;
    }

    public void setOrganismLocaleSet(Set<OrganismLocale> organismLocaleSet) {
        this.organismLocaleSet = organismLocaleSet;
    }

    public Set<OrganismExternalResource> getOrganismExternalResourceSet() {
        return organismExternalResourceSet;
    }

    public void setOrganismExternalResourceSet(Set<OrganismExternalResource> organismExternalResourceSet) {
        this.organismExternalResourceSet = organismExternalResourceSet;
    }

    /*
    public Set<Organism> getOrganismSet() {
        return organismSet;
    }

    public void setOrganismSet(Set<Organism> organismSet) {
        this.organismSet = organismSet;
    }

    @JsonIgnore
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
    */

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
        return "no.nibio.vips.logic.entity.Organism[ organismId=" + organismId + " ]";
    }

    /**
     * @return the parentOrganismId
     */
    public Integer getParentOrganismId() {
        return parentOrganismId;
    }

    /**
     * @param parentOrganismId the parentOrganismId to set
     */
    public void setParentOrganismId(Integer parentOrganismId) {
        this.parentOrganismId = parentOrganismId;
    }

    /**
     * @return the hierarchyCategoryId
     */
    public Integer getHierarchyCategoryId() {
        return hierarchyCategoryId;
    }

    /**
     * @param hierarchyCategoryId the hierarchyCategoryId to set
     */
    public void setHierarchyCategoryId(Integer hierarchyCategoryId) {
        this.hierarchyCategoryId = hierarchyCategoryId;
    }

    @Transient
    public List<Organism> getChildOrganisms()
    {
        return this.childOrganisms;
    }

    /**
     * @param childOrganisms the childOrganisms to set
     */
    @Transient
    public void setChildOrganisms(List<Organism> childOrganisms) {
        this.childOrganisms = childOrganisms;
    }
    
    @Transient
    public String getLocalName(String locale)
    {
        for(OrganismLocale oLocale: this.getOrganismLocaleSet())
        {
            if(oLocale.getOrganismLocalePK().getLocale().equals(locale))
            {
                return oLocale.getLocalName();
            }
        }
        
        return "";
    }

    @Transient
    public OrganismLocale getOrganismLocale(String language) {
        if(this.getOrganismLocaleSet() == null)
        {
            return null;
        }
        for(OrganismLocale oLocale:this.getOrganismLocaleSet())
        {
            if(oLocale.getOrganismLocalePK().getLocale().equals(language))
            {
                return oLocale;
            }
        }
        return null;
    }

    /**
     * @return the isPest
     */
    public Boolean getIsPest() {
        return isPest;
    }

    /**
     * @param isPest the isPest to set
     */
    public void setIsPest(Boolean isPest) {
        this.isPest = isPest;
    }

    /**
     * @return the isCrop
     */
    public Boolean getIsCrop() {
        return isCrop;
    }

    /**
     * @param isCrop the isCrop to set
     */
    public void setIsCrop(boolean isCrop) {
        this.isCrop = isCrop;
    }

    /**
     * @return the extraProperties
     */
    @Transient
    public Map<String,Object> getExtraProperties() {
        if(this.extraProperties == null)
        {
            this.extraProperties = new HashMap<>();
        }
        return extraProperties;
    }

    /**
     * @param extraProperties the extraProperties to set
     */
    @Transient
    public void setExtraProperties(Map<String,Object> extraProperties) {
        this.extraProperties = extraProperties;
    }
}
