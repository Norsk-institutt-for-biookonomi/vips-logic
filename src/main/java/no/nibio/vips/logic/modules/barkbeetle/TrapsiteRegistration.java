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

import java.io.Serializable;
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
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "trapsite_registration", schema="barkbeetle")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "TrapsiteRegistration.findAll", query = "SELECT t FROM TrapsiteRegistration t"),
    @NamedQuery(name = "TrapsiteRegistration.findBySeasonTrapsiteId", query = "SELECT t FROM TrapsiteRegistration t WHERE t.trapsiteRegistrationPK.seasonTrapsiteId = :seasonTrapsiteId"),
    @NamedQuery(name = "TrapsiteRegistration.findByWeek", query = "SELECT t FROM TrapsiteRegistration t WHERE t.trapsiteRegistrationPK.week = :week"),
    @NamedQuery(name = "TrapsiteRegistration.findByTrap1", query = "SELECT t FROM TrapsiteRegistration t WHERE t.trap1 = :trap1"),
    @NamedQuery(name = "TrapsiteRegistration.findByTrap2", query = "SELECT t FROM TrapsiteRegistration t WHERE t.trap2 = :trap2"),
    @NamedQuery(name = "TrapsiteRegistration.findByTrap3", query = "SELECT t FROM TrapsiteRegistration t WHERE t.trap3 = :trap3"),
    @NamedQuery(name = "TrapsiteRegistration.findByTrap4", query = "SELECT t FROM TrapsiteRegistration t WHERE t.trap4 = :trap4"),
    @NamedQuery(name = "TrapsiteRegistration.findByTrap1Remarks", query = "SELECT t FROM TrapsiteRegistration t WHERE t.trap1Remarks = :trap1Remarks"),
    @NamedQuery(name = "TrapsiteRegistration.findByTrap2Remarks", query = "SELECT t FROM TrapsiteRegistration t WHERE t.trap2Remarks = :trap2Remarks"),
    @NamedQuery(name = "TrapsiteRegistration.findByTrap3Remarks", query = "SELECT t FROM TrapsiteRegistration t WHERE t.trap3Remarks = :trap3Remarks"),
    @NamedQuery(name = "TrapsiteRegistration.findByTrap4Remarks", query = "SELECT t FROM TrapsiteRegistration t WHERE t.trap4Remarks = :trap4Remarks"),
    @NamedQuery(name = "TrapsiteRegistration.findByRegistrationStatusTypeId", query = "SELECT t FROM TrapsiteRegistration t WHERE t.registrationStatusTypeId = :registrationStatusTypeId")})
public class TrapsiteRegistration implements Serializable, Comparable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected TrapsiteRegistrationPK trapsiteRegistrationPK;
    @Column(name = "date_registration")
    @Temporal(TemporalType.DATE)
    private Date dateRegistration;
    @Column(name = "trap_1")
    private Integer trap1;
    @Column(name = "trap_2")
    private Integer trap2;
    @Column(name = "trap_3")
    private Integer trap3;
    @Column(name = "trap_4")
    private Integer trap4;
    @Size(max = 2147483647)
    @Column(name = "trap_1_remarks")
    private String trap1Remarks;
    @Size(max = 2147483647)
    @Column(name = "trap_2_remarks")
    private String trap2Remarks;
    @Size(max = 2147483647)
    @Column(name = "trap_3_remarks")
    private String trap3Remarks;
    @Size(max = 2147483647)
    @Column(name = "trap_4_remarks")
    private String trap4Remarks;
    @Column(name = "registration_status_type_id")
    private Integer registrationStatusTypeId;
    @JoinColumn(name = "season_trapsite_id", referencedColumnName = "season_trapsite_id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private SeasonTrapsite seasonTrapsite;
    
    public TrapsiteRegistration() {
    }

    public TrapsiteRegistration(TrapsiteRegistrationPK trapsiteRegistrationPK) {
        this.trapsiteRegistrationPK = trapsiteRegistrationPK;
    }

    public TrapsiteRegistration(int seasonTrapsiteId, int week) {
        this.trapsiteRegistrationPK = new TrapsiteRegistrationPK(seasonTrapsiteId, week);
    }

    public TrapsiteRegistrationPK getTrapsiteRegistrationPK() {
        return trapsiteRegistrationPK;
    }

    public void setTrapsiteRegistrationPK(TrapsiteRegistrationPK trapsiteRegistrationPK) {
        this.trapsiteRegistrationPK = trapsiteRegistrationPK;
    }

    public Integer getTrap1() {
        return trap1;
    }

    public void setTrap1(Integer trap1) {
        this.trap1 = trap1;
    }

    public Integer getTrap2() {
        return trap2;
    }

    public void setTrap2(Integer trap2) {
        this.trap2 = trap2;
    }

    public Integer getTrap3() {
        return trap3;
    }

    public void setTrap3(Integer trap3) {
        this.trap3 = trap3;
    }

    public Integer getTrap4() {
        return trap4;
    }

    public void setTrap4(Integer trap4) {
        this.trap4 = trap4;
    }

    public String getTrap1Remarks() {
        return trap1Remarks;
    }

    public void setTrap1Remarks(String trap1Remarks) {
        this.trap1Remarks = trap1Remarks;
    }

    public String getTrap2Remarks() {
        return trap2Remarks;
    }

    public void setTrap2Remarks(String trap2Remarks) {
        this.trap2Remarks = trap2Remarks;
    }

    public String getTrap3Remarks() {
        return trap3Remarks;
    }

    public void setTrap3Remarks(String trap3Remarks) {
        this.trap3Remarks = trap3Remarks;
    }

    public String getTrap4Remarks() {
        return trap4Remarks;
    }

    public void setTrap4Remarks(String trap4Remarks) {
        this.trap4Remarks = trap4Remarks;
    }

    public Integer getRegistrationStatusTypeId() {
        return registrationStatusTypeId;
    }

    public void setRegistrationStatusTypeId(Integer registrationStatusTypeId) {
        this.registrationStatusTypeId = registrationStatusTypeId;
    }

    public SeasonTrapsite getSeasonTrapsite() {
        return seasonTrapsite;
    }

    public void setSeasonTrapsite(SeasonTrapsite seasonTrapsite) {
        this.seasonTrapsite = seasonTrapsite;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (trapsiteRegistrationPK != null ? trapsiteRegistrationPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof TrapsiteRegistration)) {
            return false;
        }
        TrapsiteRegistration other = (TrapsiteRegistration) object;
        if ((this.trapsiteRegistrationPK == null && other.trapsiteRegistrationPK != null) || (this.trapsiteRegistrationPK != null && !this.trapsiteRegistrationPK.equals(other.trapsiteRegistrationPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.modules.barkbeetle.TrapsiteRegistration[ trapsiteRegistrationPK=" + trapsiteRegistrationPK + " ]";
    }

    @Override
    public int compareTo(Object t) {
        TrapsiteRegistration other = (TrapsiteRegistration) t;
        return this.getTrapsiteRegistrationPK().getWeek().compareTo(other.getTrapsiteRegistrationPK().getWeek());
    }

    /**
     * @return the dateRegistration
     */
    public Date getDateRegistration() {
        return dateRegistration;
    }

    /**
     * @param dateRegistration the dateRegistration to set
     */
    public void setDateRegistration(Date dateRegistration) {
        this.dateRegistration = dateRegistration;
    }

}
