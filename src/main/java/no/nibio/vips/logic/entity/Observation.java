/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.validation.constraints.Size;
import no.nibio.vips.logic.util.GISEntityUtil;
import no.nibio.vips.gis.GISUtil;
import no.nibio.vips.logic.entity.rest.ObservationListItem;
import no.nibio.vips.logic.util.StringJsonUserType;
import no.nibio.vips.observationdata.ObservationDataSchema;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "observation")
@XmlRootElement
@TypeDefs( {@TypeDef( name= "StringJsonObject", typeClass = StringJsonUserType.class)})
@NamedQueries({
    @NamedQuery(name = "Observation.findAll", query = "SELECT o FROM Observation o"),
    @NamedQuery(name = "Observation.findByObservationId", query = "SELECT o FROM Observation o WHERE o.observationId = :observationId"),
    @NamedQuery(name = "Observation.findByUserId", query = "SELECT o FROM Observation o WHERE o.userId IN(:userId)"),
    @NamedQuery(name = "Observation.findByLastEditedBy", query = "SELECT o FROM Observation o WHERE o.lastEditedBy IN(:lastEditedBy)"),
    @NamedQuery(name = "Observation.findByLocationPointOfInterestId", query = "SELECT o FROM Observation o WHERE o.locationPointOfInterestId = :locationPointOfInterestId"),
    @NamedQuery(name = "Observation.findByStatusChangedByUserId", query = "SELECT o FROM Observation o WHERE o.statusChangedByUserId IN(:statusChangedByUserId)"),
    @NamedQuery(name = "Observation.findByUserIdAndPeriod", query = "SELECT o FROM Observation o WHERE o.timeOfObservation BETWEEN :start AND :end AND o.userId IN(:userId)"),
    @NamedQuery(name = "Observation.findByUserIdAndStatusTypeId", query = "SELECT o FROM Observation o WHERE o.userId IN(:userId) AND o.statusTypeId= :statusTypeId"),
    @NamedQuery(name = "Observation.findByUserIdAndOrganism", query = "SELECT o FROM Observation o WHERE o.userId IN(:userId) AND o.organism =  :organism"),
    @NamedQuery(name = "Observation.findByOrganizationId", query = "SELECT o FROM Observation o WHERE o.userId IN(SELECT v.userId FROM VipsLogicUser v WHERE v.organizationId = :organizationId OR  v.organizationId IN(SELECT o.organizationId FROM Organization o WHERE o.parentOrganizationId = :organizationId))"),
    @NamedQuery(name = "Observation.findByOrganizationIdAndPeriod", query = "SELECT o FROM Observation o WHERE o.timeOfObservation BETWEEN :start AND :end AND o.userId IN(SELECT v.userId FROM VipsLogicUser v WHERE v.organizationId = :organizationId OR  v.organizationId IN(SELECT o.organizationId FROM Organization o WHERE o.parentOrganizationId = :organizationId))"),
    @NamedQuery(name = "Observation.findByOrganizationIdAndStatusTypeId", query = "SELECT o FROM Observation o WHERE o.userId IN(SELECT v.userId FROM VipsLogicUser v WHERE v.organizationId = :organizationId OR  v.organizationId IN(SELECT o.organizationId FROM Organization o WHERE o.parentOrganizationId = :organizationId)) AND o.statusTypeId= :statusTypeId"),
    @NamedQuery(name = "Observation.findByOrganizationIdAndStatusTypeIdAndBroadcastMessage", query = "SELECT o FROM Observation o WHERE o.userId IN(SELECT v.userId FROM VipsLogicUser v WHERE v.organizationId = :organizationId OR  v.organizationId IN(SELECT o.organizationId FROM Organization o WHERE o.parentOrganizationId = :organizationId)) AND o.statusTypeId= :statusTypeId AND o.broadcastMessage IS TRUE"),
    @NamedQuery(name = "Observation.findByOrganizationIdAndStatusTypeIdAndBroadcastMessageAndPeriod", query = "SELECT o FROM Observation o WHERE o.timeOfObservation BETWEEN :start AND :end AND o.userId IN(SELECT v.userId FROM VipsLogicUser v WHERE v.organizationId = :organizationId OR  v.organizationId IN(SELECT o.organizationId FROM Organization o WHERE o.parentOrganizationId = :organizationId)) AND o.statusTypeId= :statusTypeId AND o.broadcastMessage IS TRUE"),
    @NamedQuery(name = "Observation.findByOrganism", query = "SELECT o FROM Observation o WHERE o.organism =  :organism"),
    @NamedQuery(name = "Observation.findFirstByOrganism", query = "SELECT o FROM Observation o WHERE o.organism =  :organism AND o.timeOfObservation = (SELECT MIN(ob.timeOfObservation) FROM Observation ob WHERE ob.organism = :organism)"),
    @NamedQuery(name = "Observation.findByTimeOfObservation", query = "SELECT o FROM Observation o WHERE o.timeOfObservation = :timeOfObservation")
})
public class Observation implements Serializable, no.nibio.vips.observation.Observation {

    private static final long serialVersionUID = 1L;
    private Integer observationId;
    private Date timeOfObservation;
    private Organism organism;
    private Organism cropOrganism;
    private Integer userId;
    private Integer lastEditedBy;
    private List<Gis> geoinfo;
    private Integer locationPointOfInterestId;
    //private Double observedValue;
    //private Integer denominator;
    //private ObservationMethod observationMethodId;
    private String observationHeading;
    private String observationText;
    private Integer statusTypeId;
    private Integer statusChangedByUserId;
    private Date statusChangedTime;
    private String statusRemarks;
    private String observationData;
    private Boolean isQuantified;
    private Boolean broadcastMessage;
    private Boolean locationIsPrivate;
    private PolygonService polygonService;
    
    private ObservationDataSchema observationDataSchema;
    
    private VipsLogicUser user; // Transient
    private VipsLogicUser lastEditedByUser; //
    private PointOfInterest location; // Transient
    
    private Set<ObservationIllustration> observationIllustrationSet;
    
    private GISEntityUtil GISEntityUtil;
    private GISUtil GISUtil;

    public Observation() {
        this.GISEntityUtil = new GISEntityUtil();
        this.GISUtil = new GISUtil();
    }

    public Observation(Integer observationId) {
        this.observationId = observationId;
    }

    /*public Observation(Integer observationId, Date timeOfObservation, Double registeredValue) {
        this.observationId = observationId;
        this.timeOfObservation = timeOfObservation;
        this.observedValue = registeredValue;
    }*/

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
    @Override
    public Date getTimeOfObservation() {
        return timeOfObservation;
    }

    public void setTimeOfObservation(Date timeOfObservation) {
        this.timeOfObservation = timeOfObservation;
    }

    
    // Using PostGIS + Hibernate-spatial + Java Topology Suite to make this work
    /*@JsonIgnore
    @Type(type = "org.hibernate.spatial.GeometryType")
    @Column(name = "location", columnDefinition = "Geometry")
    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }*/
    
    public void setGeoinfos(List<Gis> geoinfo)
    {
        this.geoinfo = geoinfo;
    }
    
    public void addGeoInfo(Gis geoinfo)
    {
        if(this.geoinfo == null)
        {
            this.geoinfo = new ArrayList<>();
        }
        this.geoinfo.add(geoinfo);
    }
    
    @JsonIgnore
    @Transient
    public List<Gis> getGeoinfos()
    {
        return this.geoinfo;
    }
    
    
    public void setGeoinfo(String json)
    {
        this.setGeoinfos(this.GISEntityUtil.getGisFromGeoJSON(json));
    }
    
    @Transient
    @Override
    public String getGeoinfo()
    {
        Map<String, Object> properties = new HashMap<>();
        properties.put("observationId", this.getObservationId());
        return this.GISEntityUtil.getGeoJSONFromGis(this.geoinfo, properties);
    }
    
    /*@Transient
    public Coordinate getLocationCoordinate()
    {
        return this.location.getCoordinate();
    }*/
    
    /*@NotNull
    @Basic(optional = false)
    @Column(name = "observed_value")
    @Override
    public Double getObservedValue() {
        return observedValue;
    }

    public void setObservedValue(Double observedValue) {
        this.observedValue = observedValue;
    }

    @Column(name = "denominator")
    @Override
    public Integer getDenominator() {
        return denominator;
    }

    public void setDenominator(Integer denominator) {
        this.denominator = denominator;
    }*/

    /*
    @JoinColumn(name = "observation_method_id", referencedColumnName = "observation_method_id")
    @ManyToOne
    public ObservationMethod getObservationMethodId() {
        return observationMethodId;
    }

    public void setObservationMethodId(ObservationMethod observationMethodId) {
        this.observationMethodId = observationMethodId;
    }
*/
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
        return "no.nibio.vips.logic.entity.Observation[ observationId=" + observationId + " ]";
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
    //@JsonIgnore
    public Organism getOrganism() {
        return organism;
    }

    /**
     * @param organism the organism to set
     */
    public void setOrganism(Organism organism) {
        this.organism = organism;
    }
    
    @JoinColumn(name = "polygon_service_id", referencedColumnName = "polygon_service_id")
    @ManyToOne
    public PolygonService getPolygonService(){
        return this.polygonService;
    }
    
    public void setPolygonService(PolygonService polygonService)
    {
        this.polygonService = polygonService;
    }
    
    /*
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
    */
    
    @Override
    @Transient
    public String getName() {
        return this.getOrganism().getLatinName();
    }

    /**
     * @return the observationHeading
     */
    @Column(name = "observation_heading")
    @Size(max = 1023)
    public String getObservationHeading() {
        return observationHeading;
    }

    /**
     * @param observationHeading the observationHeading to set
     */
    public void setObservationHeading(String observationHeading) {
        this.observationHeading = observationHeading;
    }

    /**
     * @return the observationText
     */
    @Column(name = "observation_text")
    @Size(max = 2147483647)
    public String getObservationText() {
        return observationText;
    }

    /**
     * @param observationText the observationText to set
     */
    public void setObservationText(String observationText) {
        this.observationText = observationText;
    }

    /**
     * @return the statusTypeId
     */
    @Column(name = "status_type_id")
    public Integer getStatusTypeId() {
        return statusTypeId;
    }

    /**
     * @param statusTypeId the statusTypeId to set
     */
    public void setStatusTypeId(Integer statusTypeId) {
        this.statusTypeId = statusTypeId;
    }

    /**
     * @return the statusChangedByUserId
     */
    @Column(name = "status_changed_by_user_id")
    public Integer getStatusChangedByUserId() {
        return statusChangedByUserId;
    }

    /**
     * @param statusChangedByUserId the statusChangedByUserId to set
     */
    public void setStatusChangedByUserId(Integer statusChangedByUserId) {
        this.statusChangedByUserId = statusChangedByUserId;
    }

    /**
     * @return the statusChangedTime
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "status_changed_time")
    public Date getStatusChangedTime() {
        return statusChangedTime;
    }

    /**
     * @param statusChangedTime the statusChangedTime to set
     */
    public void setStatusChangedTime(Date statusChangedTime) {
        this.statusChangedTime = statusChangedTime;
    }

    /**
     * @return the statusRemarks
     */
    @Column(name = "status_remarks")
    @Size(max = 2147483647)
    public String getStatusRemarks() {
        return statusRemarks;
    }

    /**
     * @param statusRemarks the statusRemarks to set
     */
    public void setStatusRemarks(String statusRemarks) {
        this.statusRemarks = statusRemarks;
    }

    /**
     * @return the observationData
     */
    @Type(type = "StringJsonObject")
    @Column(name = "observation_data")
    @Override
    public String getObservationData() {
        return observationData;
    }

    /**
     * @param observationData the observationData to set
     */
    public void setObservationData(String observationData) {
        this.observationData = observationData;
    }
    
    @Transient
    public ObservationDataSchema getObservationDataSchema()
    {
        return this.observationDataSchema != null ? this.observationDataSchema : null;
    }
    
    public void setObservationDataSchema(ObservationDataSchema observationDataSchema)
    {
        this.observationDataSchema = observationDataSchema;
    }

    /**
     * @return the isQuantified
     */
    @Column(name = "is_quantified")
    public Boolean getIsQuantified() {
        return isQuantified;
    }

    /**
     * @param isQuantified the isQuantified to set
     */
    public void setIsQuantified(Boolean isQuantified) {
        this.isQuantified = isQuantified;
    }

    /**
     * @return the cropOrganism
     */
    @JoinColumn(name = "crop_organism_id", referencedColumnName = "organism_id")
    @ManyToOne
    public Organism getCropOrganism() {
        return cropOrganism;
    }

    /**
     * @param cropOrganism the cropOrganism to set
     */
    public void setCropOrganism(Organism cropOrganism) {
        this.cropOrganism = cropOrganism;
    }
    
    @Transient
    public Integer getCropOrganismId() {
        return this.getCropOrganism() != null ? this.getCropOrganism().getOrganismId() : null;
    }

    /**
     * @return the broadcastMessage
     */
    @Column(name = "broadcast_message")
    public Boolean getBroadcastMessage() {
        return broadcastMessage;
    }

    /**
     * @param broadcastMessage the broadcastMessage to set
     */
    public void setBroadcastMessage(Boolean broadcastMessage) {
        this.broadcastMessage = broadcastMessage;
    }

    /**
     * @return the observationIllustrationSet
     */
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "observationId", fetch = FetchType.EAGER)
    public Set<ObservationIllustration> getObservationIllustrationSet() {
        return observationIllustrationSet;
    }

    /**
     * @param observationIllustrationSet the observationIllustrationSet to set
     */
    public void setObservationIllustrationSet(Set<ObservationIllustration> observationIllustrationSet) {
        this.observationIllustrationSet = observationIllustrationSet;
    }

    /**
     * @return the locationPointOfInterestId
     */
    @Column(name = "location_point_of_interest_id")
    public Integer getLocationPointOfInterestId() {
        return locationPointOfInterestId;
    }

    /**
     * @param locationPointOfInterestId the locationPointOfInterestId to set
     */
    public void setLocationPointOfInterestId(Integer locationPointOfInterestId) {
        this.locationPointOfInterestId = locationPointOfInterestId;
    }

    @Override
    public int compareTo(Object t) {
        return this.getTimeOfObservation().compareTo(((no.nibio.vips.observation.Observation)t).getTimeOfObservation());
    }

    /**
     * @return the user
     */
    @Transient
    @JsonIgnore
    public VipsLogicUser getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(VipsLogicUser user) {
        this.user = user;
    }

    /**
     * @return the location
     */
    @Transient
    public PointOfInterest getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(PointOfInterest location) {
        this.location = location;
    }

    /**
     * @return the locationIsPrivate
     */
    @Column(name = "location_is_private")
    public Boolean getLocationIsPrivate() {
        return locationIsPrivate;
    }

    /**
     * @param locationIsPrivate the locationIsPrivate to set
     */
    public void setLocationIsPrivate(Boolean locationIsPrivate) {
        this.locationIsPrivate = locationIsPrivate;
    }

    /**
     * @return the lastEditedBy
     */
    @Column(name = "last_edited_by")
    public Integer getLastEditedBy() {
        return lastEditedBy;
    }

    /**
     * @param lastEditedBy the lastEditedBy to set
     */
    public void setLastEditedBy(Integer lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     * @return the lastEditedByUser
     */
    @Transient
    public VipsLogicUser getLastEditedByUser() {
        return lastEditedByUser;
    }

    /**
     * @param lastEditedByUser the lastEditedByUser to set
     */
    public void setLastEditedByUser(VipsLogicUser lastEditedByUser) {
        this.lastEditedByUser = lastEditedByUser;
    }

    /**
     * Simplifies the public JSON object
     * @param locale
     * @return 
     */
    public ObservationListItem getListItem(String locale, ObservationDataSchema observationDataSchema)
    {
        // If geoInfo is from POI, need to add observationId
        if(this.location != null)
        {
            this.location.addProperty("observationId", this.getObservationId());
        }
        return new ObservationListItem(
                this.getObservationId(),
                this.getTimeOfObservation(),
                this.getOrganismId(),
                this.getOrganism().getLocalName(locale).trim() != "" ? this.getOrganism().getLocalName(locale) : this.getOrganism().getLatinName(),
                this.getCropOrganismId(),
                this.getCropOrganism().getLocalName(locale).trim() != "" ? this.getCropOrganism().getLocalName(locale) : this.getCropOrganism().getLatinName(),
                // Specific geoInfo trumps location. This is to be interpreted 
                // as that the observation has been geographically masked by
                // choice of the observer
                this.location != null && this.geoinfo == null ? this.location.getGeoJSON() : this.getGeoinfo(),
                this.getObservationHeading(),
                this.getBroadcastMessage(),
                this.getLocationIsPrivate(),
                this.getObservationData(),
                observationDataSchema
        );
    }
    
}
