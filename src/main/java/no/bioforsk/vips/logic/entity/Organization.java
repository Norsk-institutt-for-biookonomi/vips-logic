/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the Bioforsk Open Source License as published by 
 * Bioforsk, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Bioforsk Open Source License for more details.
 * 
 * You should have received a copy of the Bioforsk Open Source License
 * along with VIPSLogic.  If not, see <http://www.bioforsk.no/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.entity;

import com.vividsolutions.jts.geom.Point;
import java.io.Serializable;
import java.util.Set;
import javax.persistence.Basic;
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
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.FetchType;
import org.hibernate.annotations.Type;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@Table(name = "organization")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Organization.findAll", query = "SELECT o FROM Organization o"),
    @NamedQuery(name = "Organization.findAllRelated", query = "SELECT o FROM Organization o where o = :organizationId OR o.parentOrganizationId = :organizationId"),
    @NamedQuery(name = "Organization.findAllTopLevelOrganizations", query = "SELECT o FROM Organization o WHERE o.parentOrganizationId IS NULL"),
    @NamedQuery(name = "Organization.findByOrganizationId", query = "SELECT o FROM Organization o WHERE o.organizationId = :organizationId"),
    @NamedQuery(name = "Organization.findByOrganizationName", query = "SELECT o FROM Organization o WHERE o.organizationName = :organizationName"),
    @NamedQuery(name = "Organization.findByAddress1", query = "SELECT o FROM Organization o WHERE o.address1 = :address1"),
    @NamedQuery(name = "Organization.findByAddress2", query = "SELECT o FROM Organization o WHERE o.address2 = :address2"),
    @NamedQuery(name = "Organization.findByPostalCode", query = "SELECT o FROM Organization o WHERE o.postalCode = :postalCode")})
public class Organization implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "organization_id")
    private Integer organizationId;
    @Size(max = 255)
    @Column(name = "organization_name")
    private String organizationName;
    @Size(max = 255)
    @Column(name = "address1")
    private String address1;
    @Size(max = 255)
    @Column(name = "address2")
    private String address2;
    @Size(max = 63)
    @Column(name = "postal_code")
    private String postalCode;
    @Size(max = 255)
    @Column(name = "city")
    private String city;
    @Column(name = "default_map_zoom")
    private Integer defaultMapZoom;
    @Column(name = "default_time_zone")
    private String defaultTimeZone;
    @Column(name = "default_vips_core_user_id")
    private Integer defaultVipsCoreUserId;
    @JsonIgnore
    @Type(type = "org.hibernate.spatial.GeometryType")
    @Column(name = "default_map_center", columnDefinition = "Geometry")
    private Point defaultMapCenter;
    @OneToMany(mappedBy = "parentOrganizationId", fetch = FetchType.EAGER)
    private Set<Organization> organizationSet;
    @JoinColumn(name = "parent_organization_id", referencedColumnName = "organization_id")
    @ManyToOne
    private Organization parentOrganizationId;
    @JoinColumn(name = "country_code", referencedColumnName = "country_code")
    @ManyToOne
    private Country countryCode;
    @Size(max = 63)
    @Column(name = "default_locale")
    private String defaultLocale;
    @OneToMany(mappedBy = "organizationId")
    private Set<VipsLogicUser> vipsLogicUserSet;

    public Organization() {
    }

    public Organization(Integer organizationId) {
        this.organizationId = organizationId;
    }

    public Integer getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Integer organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    @XmlTransient
    @JsonIgnore
    public Set<Organization> getOrganizationSet() {
        return organizationSet;
    }

    public void setOrganizationSet(Set<Organization> organizationSet) {
        this.organizationSet = organizationSet;
    }

    public Organization getParentOrganizationId() {
        return parentOrganizationId;
    }

    public void setParentOrganizationId(Organization parentOrganizationId) {
        this.parentOrganizationId = parentOrganizationId;
    }

    public Country getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(Country countryCode) {
        this.countryCode = countryCode;
    }

    @XmlTransient
    @JsonIgnore
    public Set<VipsLogicUser> getVipsLogicUserSet() {
        return vipsLogicUserSet;
    }

    public void setVipsLogicUserSet(Set<VipsLogicUser> vipsLogicUserSet) {
        this.vipsLogicUserSet = vipsLogicUserSet;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (organizationId != null ? organizationId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Organization)) {
            return false;
        }
        Organization other = (Organization) object;
        if ((this.organizationId == null && other.organizationId != null) || (this.organizationId != null && !this.organizationId.equals(other.organizationId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.Organization[ organizationId=" + organizationId + " ]";
    }

    /**
     * @return the defaultLocale
     */
    public String getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * @param defaultLocale the defaultLocale to set
     */
    public void setDefaultLocale(String defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    /**
     * @return the defaultMapZoom
     */
    public Integer getDefaultMapZoom() {
        return defaultMapZoom;
    }

    /**
     * @param defaultMapZoom the defaultMapZoom to set
     */
    public void setDefaultMapZoom(Integer defaultMapZoom) {
        this.defaultMapZoom = defaultMapZoom;
    }

    /**
     * @return the defaultMapCenter
     */
    public Point getDefaultMapCenter() {
        return defaultMapCenter;
    }

    /**
     * @param defaultMapCenter the defaultMapCenter to set
     */
    public void setDefaultMapCenter(Point defaultMapCenter) {
        this.defaultMapCenter = defaultMapCenter;
    }

    /**
     * @return the defaultTimeZone
     */
    public String getDefaultTimeZone() {
        return defaultTimeZone;
    }

    /**
     * @param defaultTimeZone the defaultTimeZone to set
     */
    public void setDefaultTimeZone(String defaultTimeZone) {
        this.defaultTimeZone = defaultTimeZone;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return the defaultVipsCoreUserId
     */
    public Integer getDefaultVipsCoreUserId() {
        return defaultVipsCoreUserId;
    }

    /**
     * @param defaultVipsCoreUserId the defaultVipsCoreUserId to set
     */
    public void setDefaultVipsCoreUserId(Integer defaultVipsCoreUserId) {
        this.defaultVipsCoreUserId = defaultVipsCoreUserId;
    }

}
