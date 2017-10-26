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

import java.io.Serializable;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import no.nibio.vips.logic.entity.Gis;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "observation_site_point", schema="applefruitmoth")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ObservationSitePoint.findAll", query = "SELECT o FROM ObservationSitePoint o"),
    @NamedQuery(name = "ObservationSitePoint.findByObservationSitePointId", query = "SELECT o FROM ObservationSitePoint o WHERE o.observationSitePointId = :observationSitePointId"),
    @NamedQuery(name = "ObservationSitePoint.findByPointName", query = "SELECT o FROM ObservationSitePoint o WHERE o.pointName = :pointName")
})
public class ObservationSitePoint implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "observation_site_point_id")
    private Integer observationSitePointId;
    @Size(max = 63)
    @Column(name = "point_name")
    private String pointName;
   
    @JoinColumn(name = "observation_site_id", referencedColumnName = "observation_site_id")
    @ManyToOne
    private ObservationSite observationSiteId;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "observationSitePoint", fetch = FetchType.EAGER)
    private Set<ObservationSitePointSeasonData> observationSitePointSeasonDataSet;

    public ObservationSitePoint() {
    }

    public ObservationSitePoint(Integer observationSitePointId) {
        this.observationSitePointId = observationSitePointId;
    }
    
    public ObservationSitePoint(ObservationSite observationSiteId, String pointName) {
        this.observationSiteId = observationSiteId;
        this.pointName = pointName;
    }

    public Integer getObservationSitePointId() {
        return observationSitePointId;
    }

    public void setObservationSitePointId(Integer observationSitePointId) {
        this.observationSitePointId = observationSitePointId;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    

    public ObservationSite getObservationSiteId() {
        return observationSiteId;
    }

    public void setObservationSiteId(ObservationSite observationSiteId) {
        this.observationSiteId = observationSiteId;
    }

    @XmlTransient
    public Set<ObservationSitePointSeasonData> getObservationSitePointSeasonDataSet() {
        return observationSitePointSeasonDataSet;
    }

    public void setObservationSitePointSeasonDataSet(Set<ObservationSitePointSeasonData> observationSitePointSeasonDataSet) {
        this.observationSitePointSeasonDataSet = observationSitePointSeasonDataSet;
    }
    
    @Transient
    public ObservationSitePointSeasonData getSeasonData(Integer season)
    {
        for(ObservationSitePointSeasonData ospsData : this.getObservationSitePointSeasonDataSet())
        {
            if(ospsData.getObservationSitePointSeasonDataPK().getSeason() == season)
            {
                return ospsData;
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (observationSitePointId != null ? observationSitePointId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ObservationSitePoint)) {
            return false;
        }
        ObservationSitePoint other = (ObservationSitePoint) object;
        if ((this.observationSitePointId == null && other.observationSitePointId != null) || (this.observationSitePointId != null && !this.observationSitePointId.equals(other.observationSitePointId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.modules.applefruitmoth.ObservationSitePoint[ observationSitePointId=" + observationSitePointId + " ]";
    }

}
