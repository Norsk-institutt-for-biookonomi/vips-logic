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

import java.io.Serializable;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @copyright 2014 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@Table(name = "observation_method")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ObservationMethod.findAll", query = "SELECT o FROM ObservationMethod o"),
    @NamedQuery(name = "ObservationMethod.findByObservationMethodId", query = "SELECT o FROM ObservationMethod o WHERE o.observationMethodId = :observationMethodId")})
public class ObservationMethod implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 63)
    @Column(name = "observation_method_id")
    private String observationMethodId;
    @OneToMany(mappedBy = "observationMethodId")
    private Set<Observation> observationSet;

    public ObservationMethod() {
    }

    public ObservationMethod(String observationMethodId) {
        this.observationMethodId = observationMethodId;
    }

    public String getObservationMethodId() {
        return observationMethodId;
    }

    public void setObservationMethodId(String observationMethodId) {
        this.observationMethodId = observationMethodId;
    }

    @XmlTransient
    @JsonIgnore
    public Set<Observation> getObservationSet() {
        return observationSet;
    }

    public void setObservationSet(Set<Observation> observationSet) {
        this.observationSet = observationSet;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (observationMethodId != null ? observationMethodId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ObservationMethod)) {
            return false;
        }
        ObservationMethod other = (ObservationMethod) object;
        if ((this.observationMethodId == null && other.observationMethodId != null) || (this.observationMethodId != null && !this.observationMethodId.equals(other.observationMethodId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.ObservationMethod[ observationMethodId=" + observationMethodId + " ]";
    }

}
