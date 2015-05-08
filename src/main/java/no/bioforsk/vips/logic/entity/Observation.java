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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Type;

/**
 * @copyright 2014 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@Table(name = "observation")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Observation.findAll", query = "SELECT o FROM Observation o"),
    @NamedQuery(name = "Observation.findByObservationId", query = "SELECT o FROM Observation o WHERE o.observationId = :observationId"),
    @NamedQuery(name = "Observation.findByUserId", query = "SELECT o FROM Observation o WHERE o.userId IN(:userId)"),
    @NamedQuery(name = "Observation.findByOrganism", query = "SELECT o FROM Observation o WHERE o.organism =  :organism"),
    @NamedQuery(name = "Observation.findByTimeOfObservation", query = "SELECT o FROM Observation o WHERE o.timeOfObservation = :timeOfObservation"),
    @NamedQuery(name = "Observation.findByObservedValue", query = "SELECT o FROM Observation o WHERE o.observedValue = :observedValue"),
    @NamedQuery(name = "Observation.findByDenominator", query = "SELECT o FROM Observation o WHERE o.denominator = :denominator")})
public class Observation implements Serializable, no.bioforsk.vips.observation.Observation {

    private static final long serialVersionUID = 1L;
    private Integer observationId;
    private Date timeOfObservation;
    private Organism organism;
    private Integer userId;
    private Point location;
    private Double observedValue;
    private Integer denominator;
    private ObservationMethod observationMethodId;

    public Observation() {
    }

    public Observation(Integer observationId) {
        this.observationId = observationId;
    }

    public Observation(Integer observationId, Date timeOfObservation, Double registeredValue) {
        this.observationId = observationId;
        this.timeOfObservation = timeOfObservation;
        this.observedValue = registeredValue;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "observation_id")
    public Integer getObservationId() {
        return observationId;
    }

    public void setObservationId(Integer observationId) {
        this.observationId = observationId;
    }

    @NotNull
    @Basic(optional = false)
    @Column(name = "time_of_observation")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getTimeOfObservation() {
        return timeOfObservation;
    }

    public void setTimeOfObservation(Date timeOfObservation) {
        this.timeOfObservation = timeOfObservation;
    }

    // Using PostGIS + Hibernate-spatial + Java Topology Suite to make this work
    @JsonIgnore
    @Type(type = "org.hibernate.spatial.GeometryType")
    @Column(name = "location", columnDefinition = "Geometry")
    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }
    
    @Transient
    public Coordinate getLocationCoordinate()
    {
        return this.location.getCoordinate();
    }
    
    @NotNull
    @Basic(optional = false)
    @Column(name = "observed_value")
    public Double getObservedValue() {
        return observedValue;
    }

    public void setObservedValue(Double observedValue) {
        this.observedValue = observedValue;
    }

    @Column(name = "denominator")
    public Integer getDenominator() {
        return denominator;
    }

    public void setDenominator(Integer denominator) {
        this.denominator = denominator;
    }

    @JoinColumn(name = "observation_method_id", referencedColumnName = "observation_method_id")
    @ManyToOne
    public ObservationMethod getObservationMethodId() {
        return observationMethodId;
    }

    public void setObservationMethodId(ObservationMethod observationMethodId) {
        this.observationMethodId = observationMethodId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (observationId != null ? observationId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Observation)) {
            return false;
        }
        Observation other = (Observation) object;
        if ((this.observationId == null && other.observationId != null) || (this.observationId != null && !this.observationId.equals(other.observationId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.Observation[ observationId=" + observationId + " ]";
    }

    /**
     * @return the userId
     */
    @Column(name = "user_id")
    public Integer getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Transient
    public Integer getOrganismId() {
        return this.getOrganism() != null ? this.getOrganism().getOrganismId() : null;
    }

    /**
     * @return the organism
     */
    @JoinColumn(name = "organism_id", referencedColumnName = "organism_id")
    @ManyToOne
    @JsonIgnore
    public Organism getOrganism() {
        return organism;
    }

    /**
     * @param organism the organism to set
     */
    public void setOrganism(Organism organism) {
        this.organism = organism;
    }

    @Override
    @Transient
    public Double getLatitude() {
        return this.getLocationCoordinate().y;
    }

    @Override
    @Transient
    public Double getLongitude() {
        return this.getLocationCoordinate().x;
    }

    @Override
    @Transient
    public String getName() {
        return this.getOrganism().getLatinName();
    }

}
