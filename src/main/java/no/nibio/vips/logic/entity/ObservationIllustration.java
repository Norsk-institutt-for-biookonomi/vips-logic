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

package no.nibio.vips.logic.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "observation_illustration")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ObservationIllustration.findAll", query = "SELECT o FROM ObservationIllustration o"),
    @NamedQuery(name = "ObservationIllustration.findByObservationId", query = "SELECT o FROM ObservationIllustration o WHERE o.observationIllustrationPK.observationId = :observationId"),
    @NamedQuery(name = "ObservationIllustration.findByFileName", query = "SELECT o FROM ObservationIllustration o WHERE o.observationIllustrationPK.fileName = :fileName")})
public class ObservationIllustration implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected ObservationIllustrationPK observationIllustrationPK;
    @Column(name = "observation_id", insertable = false, updatable = false)
    private Integer observationId;

    public ObservationIllustration() {
    }

    public ObservationIllustration(ObservationIllustrationPK observationIllustrationPK) {
        this.observationIllustrationPK = observationIllustrationPK;
    }

    public ObservationIllustration(int observationId, String fileName) {
        this.observationIllustrationPK = new ObservationIllustrationPK(observationId, fileName);
    }

    public ObservationIllustrationPK getObservationIllustrationPK() {
        return observationIllustrationPK;
    }

    public void setObservationIllustrationPK(ObservationIllustrationPK observationIllustrationPK) {
        this.observationIllustrationPK = observationIllustrationPK;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (observationIllustrationPK != null ? observationIllustrationPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ObservationIllustration)) {
            return false;
        }
        ObservationIllustration other = (ObservationIllustration) object;
        if ((this.observationIllustrationPK == null && other.observationIllustrationPK != null) || (this.observationIllustrationPK != null && !this.observationIllustrationPK.equals(other.observationIllustrationPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.ObservationIllustration[ observationIllustrationPK=" + observationIllustrationPK + " ]";
    }

}
