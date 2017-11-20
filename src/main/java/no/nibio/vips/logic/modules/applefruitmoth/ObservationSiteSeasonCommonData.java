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
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "observation_site_season_common_data", schema="applefruitmoth")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ObservationSiteSeasonCommonData.findAll", query = "SELECT o FROM ObservationSiteSeasonCommonData o"),
    @NamedQuery(name = "ObservationSiteSeasonCommonData.findByObservationSiteId", query = "SELECT o FROM ObservationSiteSeasonCommonData o WHERE o.observationSiteSeasonCommonDataPK.observationSiteId = :observationSiteId"),
    @NamedQuery(name = "ObservationSiteSeasonCommonData.findBySeason", query = "SELECT o FROM ObservationSiteSeasonCommonData o WHERE o.observationSiteSeasonCommonDataPK.season = :season"),
    @NamedQuery(name = "ObservationSiteSeasonCommonData.findByThousandBerrySample", query = "SELECT o FROM ObservationSiteSeasonCommonData o WHERE o.thousandBerrySample = :thousandBerrySample"),
    @NamedQuery(name = "ObservationSiteSeasonCommonData.findByDegreeOfParasitation", query = "SELECT o FROM ObservationSiteSeasonCommonData o WHERE o.degreeOfParasitation = :degreeOfParasitation")})
public class ObservationSiteSeasonCommonData implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected ObservationSiteSeasonCommonDataPK observationSiteSeasonCommonDataPK;
    @Column(name = "thousand_berry_sample")
    private Double thousandBerrySample;
    @Column(name = "degree_of_parasitation")
    private Double degreeOfParasitation;
    @Column(name = "warning_status")
    private Integer warningStatus;
    @Column(name = "remarks")
    private String remarks;
    @Column(name = "last_updated")
    private Date lastUpdated;
    @JoinColumn(name = "observation_site_id", referencedColumnName = "observation_site_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private ObservationSite observationSite;

    public ObservationSiteSeasonCommonData() {
    }

    public ObservationSiteSeasonCommonData(ObservationSiteSeasonCommonDataPK observationSiteSeasonCommonDataPK) {
        this.observationSiteSeasonCommonDataPK = observationSiteSeasonCommonDataPK;
    }

    public ObservationSiteSeasonCommonData(int observationSiteId, int season) {
        this.observationSiteSeasonCommonDataPK = new ObservationSiteSeasonCommonDataPK(observationSiteId, season);
    }

    public ObservationSiteSeasonCommonDataPK getObservationSiteSeasonCommonDataPK() {
        return observationSiteSeasonCommonDataPK;
    }

    public void setObservationSiteSeasonCommonDataPK(ObservationSiteSeasonCommonDataPK observationSiteSeasonCommonDataPK) {
        this.observationSiteSeasonCommonDataPK = observationSiteSeasonCommonDataPK;
    }

    public Double getThousandBerrySample() {
        return thousandBerrySample;
    }

    public void setThousandBerrySample(Double thousandBerrySample) {
        this.thousandBerrySample = thousandBerrySample;
    }

    public Double getDegreeOfParasitation() {
        return degreeOfParasitation;
    }

    public void setDegreeOfParasitation(Double degreeOfParasitation) {
        this.degreeOfParasitation = degreeOfParasitation;
    }

    public ObservationSite getObservationSite() {
        return observationSite;
    }

    public void setObservationSite(ObservationSite observationSite) {
        this.observationSite = observationSite;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (observationSiteSeasonCommonDataPK != null ? observationSiteSeasonCommonDataPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ObservationSiteSeasonCommonData)) {
            return false;
        }
        ObservationSiteSeasonCommonData other = (ObservationSiteSeasonCommonData) object;
        if ((this.observationSiteSeasonCommonDataPK == null && other.observationSiteSeasonCommonDataPK != null) || (this.observationSiteSeasonCommonDataPK != null && !this.observationSiteSeasonCommonDataPK.equals(other.observationSiteSeasonCommonDataPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.modules.applefruitmoth.ObservationSiteSeasonCommonData[ observationSiteSeasonCommonDataPK=" + observationSiteSeasonCommonDataPK + " ]";
    }

    /**
     * @return the warningStatus
     */
    public Integer getWarningStatus() {
        return warningStatus;
    }

    /**
     * @param warningStatus the warningStatus to set
     */
    public void setWarningStatus(Integer warningStatus) {
        this.warningStatus = warningStatus;
    }

    /**
     * @return the remarks
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * @param remarks the remarks to set
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
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
