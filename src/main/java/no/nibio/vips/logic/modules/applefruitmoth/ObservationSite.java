/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.modules.applefruitmoth;

import com.vividsolutions.jts.geom.Geometry;
import java.io.Serializable;
import java.util.HashSet;
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
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "observation_site", schema="applefruitmoth")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ObservationSite.findAll", query = "SELECT o FROM ObservationSite o ORDER BY o.observationSiteName ASC"),
    @NamedQuery(name = "ObservationSite.findByObservationSiteId", query = "SELECT o FROM ObservationSite o WHERE o.observationSiteId = :observationSiteId"),
    @NamedQuery(name = "ObservationSite.findByObservationSiteName", query = "SELECT o FROM ObservationSite o WHERE o.observationSiteName = :observationSiteName")})
public class ObservationSite implements Serializable, Comparable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "observation_site_id")
    private Integer observationSiteId;
    @Size(max = 127)
    @Column(name = "observation_site_name")
    private String observationSiteName;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "observationSite", fetch = FetchType.EAGER)
    private Set<ObservationSiteSeasonCommonData> observationSiteSeasonCommonDataSet;
    @OneToMany(mappedBy = "observationSiteId", fetch = FetchType.EAGER)
    private Set<ObservationSitePoint> observationSitePointSet;
    @Transient
    private Geometry siteCenterPoint;

    public ObservationSite() {
    }

    public ObservationSite(Integer observationSiteId) {
        this.observationSiteId = observationSiteId;
    }

    public Integer getObservationSiteId() {
        return observationSiteId;
    }

    public void setObservationSiteId(Integer observationSiteId) {
        this.observationSiteId = observationSiteId;
    }

    public String getObservationSiteName() {
        return observationSiteName;
    }

    public void setObservationSiteName(String observationSiteName) {
        this.observationSiteName = observationSiteName;
    }

    @XmlTransient
    public Set<ObservationSiteSeasonCommonData> getObservationSiteSeasonCommonDataSet() {
        if(this.observationSiteSeasonCommonDataSet == null)
        {
            this.observationSiteSeasonCommonDataSet = new HashSet<>();
        }
        return observationSiteSeasonCommonDataSet;
    }

    public void setObservationSiteSeasonCommonDataSet(Set<ObservationSiteSeasonCommonData> observationSiteSeasonCommonDataSet) {
        this.observationSiteSeasonCommonDataSet = observationSiteSeasonCommonDataSet;
    }

    @XmlTransient
    public Set<ObservationSitePoint> getObservationSitePointSet() {
        return observationSitePointSet;
    }

    public void setObservationSitePointSet(Set<ObservationSitePoint> observationSitePointSet) {
        this.observationSitePointSet = observationSitePointSet;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (observationSiteId != null ? observationSiteId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ObservationSite)) {
            return false;
        }
        ObservationSite other = (ObservationSite) object;
        if ((this.observationSiteId == null && other.observationSiteId != null) || (this.observationSiteId != null && !this.observationSiteId.equals(other.observationSiteId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.modules.applefruitmoth.ObservationSite[ observationSiteId=" + observationSiteId + " ]";
    }

    /**
     * @return the siteCenterPoint
     */
    public Geometry getSiteCenterPoint() {
        return siteCenterPoint;
    }

    /**
     * @param siteCenterPoint the siteCenterPoint to set
     */
    public void setSiteCenterPoint(Geometry siteCenterPoint) {
        this.siteCenterPoint = siteCenterPoint;
    }
    
    @Transient
    public ObservationSiteSeasonCommonData getCommonDataForSeason(Integer season)
    {
        for(ObservationSiteSeasonCommonData cd : observationSiteSeasonCommonDataSet)
        {
            if(cd.getObservationSiteSeasonCommonDataPK().getSeason() == season)
            {
                return cd;
            }
        }
        return null;
    }

    @Override
    public int compareTo(Object t) {
        return this.getObservationSiteName().compareTo(((ObservationSite)t).observationSiteName);
    }

}
