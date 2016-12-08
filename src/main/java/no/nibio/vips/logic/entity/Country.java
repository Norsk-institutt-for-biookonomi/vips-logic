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
import java.util.Locale;
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
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "country")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Country.findAll", query = "SELECT c FROM Country c"),
    @NamedQuery(name = "Country.findByCountryCode", query = "SELECT c FROM Country c WHERE c.countryCode = :countryCode"),
    @NamedQuery(name = "Country.findByCountryCodes", query = "SELECT c FROM Country c WHERE c.countryCode IN(:countryCodes)")
})
public class Country implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotNull
    @Size(min = 1, max = 2)
    private String countryCode;
        private Set<ExternalResource> externalResourceSet;
        private Set<PointOfInterest> pointOfInterestSet;
    @Size(max=10)
    private String phoneCode;

    public Country() {
    }

    public Country(String countryCode) {
        this.countryCode = countryCode;
    }

@Id
    @Basic(optional = false)
    @Column(name = "country_code")
    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

@JoinTable(name = "external_resource_validity", joinColumns = {
        @JoinColumn(name = "country_code", referencedColumnName = "country_code")}, inverseJoinColumns = {
            @JoinColumn(name = "external_resource_id", referencedColumnName = "external_resource_id")})
    @ManyToMany
    @XmlTransient
    @JsonIgnore
    public Set<ExternalResource> getExternalResourceSet() {
        return externalResourceSet;
    }

    public void setExternalResourceSet(Set<ExternalResource> externalResourceSet) {
        this.externalResourceSet = externalResourceSet;
    }

@OneToMany(mappedBy = "countryCode")
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
        return "no.nibio.vips.logic.entity.Country[ countryCode=" + countryCode + " ]";
    }

    
    public String getCountryName(String language)
    {
        Locale locale = new Locale(language, this.countryCode);
        return locale.getDisplayCountry();
    }

    /**
     * @return the phoneCode
     */
    @Column(name="phone_code")
    public String getPhoneCode() {
        return phoneCode;
    }

    /**
     * @param phoneCode the phoneCode to set
     */
    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }
}
