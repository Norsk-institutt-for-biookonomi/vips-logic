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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
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
@Table(name = "country")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Country.findAll", query = "SELECT c FROM Country c"),
    @NamedQuery(name = "Country.findByCountryCode", query = "SELECT c FROM Country c WHERE c.countryCode = :countryCode")})
public class Country implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 2)
    @Column(name = "country_code")
    private String countryCode;
    @JoinTable(name = "external_resource_validity", joinColumns = {
        @JoinColumn(name = "country_code", referencedColumnName = "country_code")}, inverseJoinColumns = {
        @JoinColumn(name = "external_resource_id", referencedColumnName = "external_resource_id")})
    @ManyToMany
    private Set<ExternalResource> externalResourceSet;
    @OneToMany(mappedBy = "countryCode")
    private Set<PointOfInterest> pointOfInterestSet;

    public Country() {
    }

    public Country(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @XmlTransient
    @JsonIgnore
    public Set<ExternalResource> getExternalResourceSet() {
        return externalResourceSet;
    }

    public void setExternalResourceSet(Set<ExternalResource> externalResourceSet) {
        this.externalResourceSet = externalResourceSet;
    }

    @XmlTransient
    @JsonIgnore
    public Set<PointOfInterest> getPointOfInterestSet() {
        return pointOfInterestSet;
    }

    public void setPointOfInterestSet(Set<PointOfInterest> pointOfInterestSet) {
        this.pointOfInterestSet = pointOfInterestSet;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (countryCode != null ? countryCode.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Country)) {
            return false;
        }
        Country other = (Country) object;
        if ((this.countryCode == null && other.countryCode != null) || (this.countryCode != null && !this.countryCode.equals(other.countryCode))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.Country[ countryCode=" + countryCode + " ]";
    }

}
