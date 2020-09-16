/*
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.modules.barkbeetle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import no.nibio.vips.logic.entity.VipsLogicUser;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "season_trapsite", schema="barkbeetle")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "SeasonTrapsite.findAll", query = "SELECT s FROM SeasonTrapsite s"),
    @NamedQuery(name = "SeasonTrapsite.findBySeasonTrapsiteId", query = "SELECT s FROM SeasonTrapsite s WHERE s.seasonTrapsiteId = :seasonTrapsiteId"),
    @NamedQuery(name = "SeasonTrapsite.findBySeason", query = "SELECT s FROM SeasonTrapsite s WHERE s.season = :season"),
    @NamedQuery(name = "SeasonTrapsite.findByUserId", query = "SELECT s FROM SeasonTrapsite s WHERE s.userId = :userId"),
    @NamedQuery(name = "SeasonTrapsite.findByOwnerName", query = "SELECT s FROM SeasonTrapsite s WHERE s.ownerName = :ownerName"),
    @NamedQuery(name = "SeasonTrapsite.findByOwnerPhone", query = "SELECT s FROM SeasonTrapsite s WHERE s.ownerPhone = :ownerPhone"),
    @NamedQuery(name = "SeasonTrapsite.findByDateInstalled", query = "SELECT s FROM SeasonTrapsite s WHERE s.dateInstalled = :dateInstalled"),
    @NamedQuery(name = "SeasonTrapsite.findByInstallationRemarks", query = "SELECT s FROM SeasonTrapsite s WHERE s.installationRemarks = :installationRemarks")})
public class SeasonTrapsite implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "season_trapsite_id")
    private Integer seasonTrapsiteId;
    @Column(name = "season")
    private Integer season;
    @Column(name = "gis_geom", columnDefinition = "GeometryZ")
    @JsonIgnore
    private Geometry gisGeom;
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ManyToOne(optional = false)
    private VipsLogicUser userId;
    @Size(max = 255)
    @Column(name = "owner_name")
    private String ownerName;
    @Size(max = 31)
    @Column(name = "owner_phone")
    private String ownerPhone;
    @Column(name = "date_installed")
    @Temporal(TemporalType.DATE)
    private Date dateInstalled;
    @Size(max = 2147483647)
    @Column(name = "installation_remarks")
    private String installationRemarks;
    @Column(name = "county_no")
    private Integer countyNo;
    @Column(name = "county_name")
    private String countyName;
    @Column(name = "municipality_no")
    private Integer municipalityNo;
    @Column(name = "municipality_name")
    private String municipalityName;
    @Column(name = "property_no")
    private Integer propertyNo;
    @Column(name = "property_section_no")
    private Integer propertySectionNo;
    @OneToMany(cascade = CascadeType.MERGE, mappedBy = "seasonTrapsite", fetch = FetchType.EAGER)
    private Collection<TrapsiteRegistration> trapsiteRegistrationCollection;
    @JoinColumn(name = "trapsite_type", referencedColumnName = "trapsite_type_id")
    @ManyToOne
    private TrapsiteType trapsiteType;

    public SeasonTrapsite() {
    }

    public SeasonTrapsite(Integer seasonTrapsiteId) {
        this.seasonTrapsiteId = seasonTrapsiteId;
    }

    public Integer getSeasonTrapsiteId() {
        return seasonTrapsiteId;
    }

    public void setSeasonTrapsiteId(Integer seasonTrapsiteId) {
        this.seasonTrapsiteId = seasonTrapsiteId;
    }

    public Integer getSeason() {
        return season;
    }

    public void setSeason(Integer season) {
        this.season = season;
    }

    public Geometry getGisGeom() {
        return gisGeom;
    }

    public void setGisGeom(Geometry gisGeom) {
        this.gisGeom = gisGeom;
    }

    public VipsLogicUser getUserId() {
        return userId;
    }

    public void setUserId(VipsLogicUser userId) {
        this.userId = userId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    public Date getDateInstalled() {
        return dateInstalled;
    }

    public void setDateInstalled(Date dateInstalled) {
        this.dateInstalled = dateInstalled;
    }

    public String getInstallationRemarks() {
        return installationRemarks;
    }

    public void setInstallationRemarks(String installationRemarks) {
        this.installationRemarks = installationRemarks;
    }

    @XmlTransient
    public Collection<TrapsiteRegistration> getTrapsiteRegistrationCollection() {
        return trapsiteRegistrationCollection;
    }

    public void setTrapsiteRegistrationCollection(Collection<TrapsiteRegistration> trapsiteRegistrationCollection) {
        this.trapsiteRegistrationCollection = trapsiteRegistrationCollection;
    }
    
    /**
     * Report tool for listing this site
     * @return 
     */
    @JsonIgnore
    @XmlTransient
    @Transient
    public Integer getMostSevereRegistrationStatus()
    {
        // Weighting the severity of the status codes for easy comparison
        // 1 = not checked, so gets severity status 3
        // 2 = checked and found OK, so gets severity status 1
        // 3 = checked and rejected, so gets severity status 2
        Integer[] codeWeight = {0,3,1,2};
        // 1,3,2
        Integer retVal = 0;
        for(TrapsiteRegistration reg:this.getTrapsiteRegistrationCollection())
        {
            retVal = codeWeight[retVal] > codeWeight[reg.getRegistrationStatusTypeId()] ? retVal : reg.getRegistrationStatusTypeId();
        }
        return retVal;
    }

    public TrapsiteType getTrapsiteType() {
        return trapsiteType;
    }

    public void setTrapsiteType(TrapsiteType trapsiteType) {
        this.trapsiteType = trapsiteType;
    }
    
    // Helper methods
    
    /** Returns a WGS84 projected longitude for the site
     * 
     */
    @Transient
    public Double getLongitude()
    {
        return this.getGisGeom() != null ? ((Point) this.getGisGeom()).getCoordinate().getX()
                : 0.0;
    }
    
    /** Returns a WGS84 projected latitude for the site
     * 
     */
    @Transient
    public Double getLatitude()
    {
        return this.getGisGeom() != null ? ((Point) this.getGisGeom()).getCoordinate().getY()
                : 0.0;
    }
    
    
    /**
     * @return the altitude for the site 
     */
    @Transient
    public Double getAltitude()
    {
        return this.getGisGeom() != null ? ((Point) this.getGisGeom()).getCoordinate().getZ()
                : 0.0;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (seasonTrapsiteId != null ? seasonTrapsiteId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SeasonTrapsite)) {
            return false;
        }
        SeasonTrapsite other = (SeasonTrapsite) object;
        if ((this.seasonTrapsiteId == null && other.seasonTrapsiteId != null) || (this.seasonTrapsiteId != null && !this.seasonTrapsiteId.equals(other.seasonTrapsiteId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.modules.barkbeetle.SeasonTrapsite[ seasonTrapsiteId=" + seasonTrapsiteId + " ]";
    }

    /**
     * @return the countyNo
     */
    public Integer getCountyNo() {
        return countyNo;
    }

    /**
     * @param countyNo the countyNo to set
     */
    public void setCountyNo(Integer countyNo) {
        this.countyNo = countyNo;
    }

    /**
     * @return the countyName
     */
    public String getCountyName() {
        return countyName;
    }

    /**
     * @param countyName the countyName to set
     */
    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    /**
     * @return the municipalityNo
     */
    public Integer getMunicipalityNo() {
        return municipalityNo;
    }

    /**
     * @param municipalityNo the municipalityNo to set
     */
    public void setMunicipalityNo(Integer municipalityNo) {
        this.municipalityNo = municipalityNo;
    }

    /**
     * @return the propertyNo
     */
    public Integer getPropertyNo() {
        return propertyNo;
    }

    /**
     * @param propertyNo the propertyNo to set
     */
    public void setPropertyNo(Integer propertyNo) {
        this.propertyNo = propertyNo;
    }

    /**
     * @return the propertySectionNo
     */
    public Integer getPropertySectionNo() {
        return propertySectionNo;
    }

    /**
     * @param propertySectionNo the propertySectionNo to set
     */
    public void setPropertySectionNo(Integer propertySectionNo) {
        this.propertySectionNo = propertySectionNo;
    }

    /**
     * @return the municipalityName
     */
    public String getMunicipalityName() {
        return municipalityName;
    }

    /**
     * @param municipalityName the municipalityName to set
     */
    public void setMunicipalityName(String municipalityName) {
        this.municipalityName = municipalityName;
    }

}
