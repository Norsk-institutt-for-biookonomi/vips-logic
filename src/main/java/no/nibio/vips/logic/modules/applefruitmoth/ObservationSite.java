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

import org.locationtech.jts.geom.Geometry;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
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
@Table(name = "observation_site", schema="applefruitmoth")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ObservationSite.findAll", query = "SELECT o FROM ObservationSite o ORDER BY o.observationSiteName ASC"),
    @NamedQuery(name = "ObservationSite.findByObservationSiteId", query = "SELECT o FROM ObservationSite o WHERE o.observationSiteId = :observationSiteId"),
    @NamedQuery(name = "ObservationSite.findByObservationSiteName", query = "SELECT o FROM ObservationSite o WHERE o.observationSiteName = :observationSiteName"),
    @NamedQuery(name = "ObservationSitePoint.findByDescription", query = "SELECT o FROM ObservationSite o WHERE o.description = :description"),
    @NamedQuery(name = "ObservationSitePoint.findByGisId", query = "SELECT o FROM ObservationSite o WHERE o.gisId = :gisId")
})
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
    @Size(max = 2147483647)
    @Column(name = "description")
    private String description;
    @JoinColumn(name = "gis_id", referencedColumnName="gis_id")
    @OneToOne
    private Gis gisId;
    @Transient
    private Date lastUpdatedClusterCount;
    @Column(name = "publicly_available")
    private Boolean publiclyAvailable;


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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Gis getGisId() {
        return gisId;
    }

    public void setGisId(Gis gisId) {
        this.gisId = gisId;
    }

    /**
     * @return the lastUpdatedClusterCount
     */
    public Date getLastUpdatedClusterCount() {
        return lastUpdatedClusterCount;
    }

    /**
     * @param lastUpdatedClusterCount the lastUpdatedClusterCount to set
     */
    public void setLastUpdatedClusterCount(Date lastUpdatedClusterCount) {
        this.lastUpdatedClusterCount = lastUpdatedClusterCount;
    }

    /**
     * @return the publiclyAvailable
     */
    public Boolean getPubliclyAvailable() {
        return publiclyAvailable;
    }

    /**
     * @param publiclyAvailable the publiclyAvailable to set
     */
    public void setPubliclyAvailable(Boolean publiclyAvailable) {
        this.publiclyAvailable = publiclyAvailable;
    }
}
