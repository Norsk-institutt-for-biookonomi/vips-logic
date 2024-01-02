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
import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "observation_site_point_season_data", schema = "applefruitmoth")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ObservationSitePointSeasonData.findAll", query = "SELECT o FROM ObservationSitePointSeasonData o"),
    @NamedQuery(name = "ObservationSitePointSeasonData.findByObservationSitePointId", query = "SELECT o FROM ObservationSitePointSeasonData o WHERE o.observationSitePointSeasonDataPK.observationSitePointId = :observationSitePointId"),
    @NamedQuery(name = "ObservationSitePointSeasonData.findBySeason", query = "SELECT o FROM ObservationSitePointSeasonData o WHERE o.observationSitePointSeasonDataPK.season = :season"),
    @NamedQuery(name = "ObservationSitePointSeasonData.findByAutumnDate", query = "SELECT o FROM ObservationSitePointSeasonData o WHERE o.autumnDate = :autumnDate"),
    @NamedQuery(name = "ObservationSitePointSeasonData.findByAutumnValue", query = "SELECT o FROM ObservationSitePointSeasonData o WHERE o.autumnValue = :autumnValue"),
    @NamedQuery(name = "ObservationSitePointSeasonData.findBySpringDate", query = "SELECT o FROM ObservationSitePointSeasonData o WHERE o.springDate = :springDate"),
    @NamedQuery(name = "ObservationSitePointSeasonData.findBySpringValue", query = "SELECT o FROM ObservationSitePointSeasonData o WHERE o.springValue = :springValue")})
public class ObservationSitePointSeasonData implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected ObservationSitePointSeasonDataPK observationSitePointSeasonDataPK;
    @Column(name = "autumn_date")
    @Temporal(TemporalType.DATE)
    private Date autumnDate;
    @Column(name = "autumn_value")
    private Integer autumnValue;
    @Column(name = "spring_date")
    @Temporal(TemporalType.DATE)
    private Date springDate;
    @Column(name = "spring_value")
    private Integer springValue;
    @JoinColumn(name = "observation_site_point_id", referencedColumnName = "observation_site_point_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private ObservationSitePoint observationSitePoint;
    @Column(name = "last_updated")
    @Temporal(TemporalType.DATE)
    private Date lastUpdated;

    public ObservationSitePointSeasonData() {
    }

    public ObservationSitePointSeasonData(ObservationSitePointSeasonDataPK observationSitePointSeasonDataPK) {
        this.observationSitePointSeasonDataPK = observationSitePointSeasonDataPK;
    }

    public ObservationSitePointSeasonData(int observationSitePointId, int season) {
        this.observationSitePointSeasonDataPK = new ObservationSitePointSeasonDataPK(observationSitePointId, season);
    }

    public ObservationSitePointSeasonDataPK getObservationSitePointSeasonDataPK() {
        return observationSitePointSeasonDataPK;
    }

    public void setObservationSitePointSeasonDataPK(ObservationSitePointSeasonDataPK observationSitePointSeasonDataPK) {
        this.observationSitePointSeasonDataPK = observationSitePointSeasonDataPK;
    }

    public Date getAutumnDate() {
        return autumnDate;
    }

    public void setAutumnDate(Date autumnDate) {
        this.autumnDate = autumnDate;
    }

    public Integer getAutumnValue() {
        return autumnValue;
    }

    public void setAutumnValue(Integer autumnValue) {
        this.autumnValue = autumnValue;
    }

    public Date getSpringDate() {
        return springDate;
    }

    public void setSpringDate(Date springDate) {
        this.springDate = springDate;
    }

    public Integer getSpringValue() {
        return springValue;
    }

    public void setSpringValue(Integer springValue) {
        this.springValue = springValue;
    }

    public ObservationSitePoint getObservationSitePoint() {
        return observationSitePoint;
    }

    public void setObservationSitePoint(ObservationSitePoint observationSitePoint) {
        this.observationSitePoint = observationSitePoint;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (observationSitePointSeasonDataPK != null ? observationSitePointSeasonDataPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ObservationSitePointSeasonData)) {
            return false;
        }
        ObservationSitePointSeasonData other = (ObservationSitePointSeasonData) object;
        if ((this.observationSitePointSeasonDataPK == null && other.observationSitePointSeasonDataPK != null) || (this.observationSitePointSeasonDataPK != null && !this.observationSitePointSeasonDataPK.equals(other.observationSitePointSeasonDataPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.modules.applefruitmoth.ObservationSitePointSeasonData[ observationSitePointSeasonDataPK=" + observationSitePointSeasonDataPK + " ]";
    }

    /**
     * @return the lastUpdated
     */
    public Date getLastUpdated() {
        return lastUpdated;
    }

    /**
     * @param lastUpdated the lastUpdated to set
     */
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

}
