/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package no.nibio.vips.logic.modules.applefruitmoth;

import java.io.Serializable;
import java.util.Set;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
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
