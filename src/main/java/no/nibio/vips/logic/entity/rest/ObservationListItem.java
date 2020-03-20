/*
 * Copyright (c) 2018 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.entity.rest;

import java.util.Date;

/**
 * A JSON friendly, public representation of the Observation
 * @copyright 2018 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class ObservationListItem {
    private Integer observationId, organismId, cropOrganismId;
    private Date timeOfObservation;
    private String organismName, cropOrganismName;
    private String geoInfo;
    private String observationHeading;
    private Boolean broadcastMessage, locationIsPrivate;

    public ObservationListItem(
            Integer observationId,
            Date timeOfObservation,
            Integer organismId,
            String organismName,
            Integer cropOrganismId,
            String cropOrganismName,
            String geoinfo,
            String observationHeading,
            Boolean broadcastMessage,
            Boolean locationIsPrivate
    ){
        this.observationId = observationId;
        this.timeOfObservation = timeOfObservation;
        this.organismId = organismId;
        this.organismName = organismName;
        this.cropOrganismId = cropOrganismId;
        this.cropOrganismName = cropOrganismName;
        this.geoInfo = geoinfo;
        this.observationHeading = observationHeading;
        this.broadcastMessage = broadcastMessage;
        this.locationIsPrivate = locationIsPrivate;
    }
    
    /**
     * @return the observationId
     */
    public Integer getObservationId() {
        return observationId;
    }

    /**
     * @param observationId the observationId to set
     */
    public void setObservationId(Integer observationId) {
        this.observationId = observationId;
    }

    /**
     * @return the timeOfObservation
     */
    public Date getTimeOfObservation() {
        return timeOfObservation;
    }

    /**
     * @param timeOfObservation the timeOfObservation to set
     */
    public void setTimeOfObservation(Date timeOfObservation) {
        this.timeOfObservation = timeOfObservation;
    }

    /**
     * @return the organismName
     */
    public String getOrganismName() {
        return organismName;
    }

    /**
     * @param organismName the organismName to set
     */
    public void setOrganismName(String organismName) {
        this.organismName = organismName;
    }

    /**
     * @return the cropOrganismName
     */
    public String getCropOrganismName() {
        return cropOrganismName;
    }

    /**
     * @param cropOrganismName the cropOrganismName to set
     */
    public void setCropOrganismName(String cropOrganismName) {
        this.cropOrganismName = cropOrganismName;
    }

    /**
     * @return the geoInfo
     */
    public String getGeoInfo() {
        return geoInfo;
    }

    /**
     * @param geoInfo the geoInfo to set
     */
    public void setGeoInfo(String geoInfo) {
        this.geoInfo = geoInfo;
    }

    /**
     * @return the observationHeading
     */
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
     * @return the organismId
     */
    public Integer getOrganismId() {
        return organismId;
    }

    /**
     * @param organismId the organismId to set
     */
    public void setOrganismId(Integer organismId) {
        this.organismId = organismId;
    }

    /**
     * @return the cropOrganismId
     */
    public Integer getCropOrganismId() {
        return cropOrganismId;
    }

    /**
     * @param cropOrganismId the cropOrganismId to set
     */
    public void setCropOrganismId(Integer cropOrganismId) {
        this.cropOrganismId = cropOrganismId;
    }

    /**
     * @return the broadcastMessage
     */
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
     * @return the locationIsPrivate
     */
    public Boolean getLocationIsPrivate() {
        return locationIsPrivate;
    }

    /**
     * @param locationIsPrivate the locationIsPrivate to set
     */
    public void setLocationIsPrivate(Boolean locationIsPrivate) {
        this.locationIsPrivate = locationIsPrivate;
    }
}
