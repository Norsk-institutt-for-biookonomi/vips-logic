/*
 * Copyright (c) 2018 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.entity.rest;

import java.util.Date;
import no.nibio.vips.observationdata.ObservationDataSchema;

/**
 * A JSON friendly, public representation of the Observation
 * @copyright 2018 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class ObservationListItem implements Comparable{
    private Integer observationId, observationTimeSeriesId, organismId, cropOrganismId;
    private Integer observerId;
    private String observerName;
    private Date timeOfObservation;
    private String organismName, cropOrganismName;
    private String observationTimeSeriesLabel;
    private String geoInfo;
    private String observationHeading;
    private String observationText;
    private String observationData;
    private ObservationDataSchema observationDataSchema;
    private Boolean broadcastMessage;
    private Boolean locationIsPrivate;
    private Boolean isPositive;

    public ObservationListItem(
            Integer observationId,
            Integer observerId,
            String observerName,
            Integer observationTimeSeriesId,
            Date timeOfObservation,
            Integer organismId,
            String organismName,
            Integer cropOrganismId,
            String cropOrganismName,
            String observationTimeSeriesLabel,
            String geoinfo,
            String observationHeading,
            String observationText,
            Boolean broadcastMessage,
            Boolean locationIsPrivate,
            Boolean isPositive,
            String observationData,
            ObservationDataSchema observationDataSchema
    ){
        this.observationId = observationId;
        this.observerId = observerId;
        this.observerName = observerName;
        this.observationTimeSeriesId = observationTimeSeriesId;
        this.timeOfObservation = timeOfObservation;
        this.organismId = organismId;
        this.organismName = organismName;
        this.cropOrganismId = cropOrganismId;
        this.cropOrganismName = cropOrganismName;
        this.observationTimeSeriesLabel = observationTimeSeriesLabel;
        this.geoInfo = geoinfo;
        this.observationHeading = observationHeading;
        this.observationText = observationText;
        this.broadcastMessage = broadcastMessage;
        this.locationIsPrivate = locationIsPrivate;
        this.isPositive = isPositive;
        this.observationData = observationData;
        this.observationDataSchema = observationDataSchema;
    }
    

    @Override
    public int compareTo(Object otherOne)
    {
        if(! (otherOne instanceof ObservationListItem))
        {
            return 0;
        }
        return this.getTimeOfObservation().compareTo(((ObservationListItem) otherOne).getTimeOfObservation());
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
     * @return The ID of the observer
     */
    public Integer getObserverId() {
        return observerId;
    }

    /**
     * @param observerId The ID to set
     */
    public void setObserverId(Integer observerId) {
        this.observerId = observerId;
    }

    /**
     * @return The full name of the observer
     */
    public String getObserverName() {
        return observerName;
    }

    /**
     * @param observerName The observer name to set
     */
    public void setObserverName(String observerName) {
        this.observerName = observerName;
    }

    /**
     * @return the observationTimeSeriesId
     */
    public Integer getObservationTimeSeriesId() {
        return observationTimeSeriesId;
    }

    /**
     * @param observationTimeSeriesId the observationTimeSeriesId to set
     */
    public void setObservationTimeSeriesId(Integer observationTimeSeriesId) {
        this.observationTimeSeriesId = observationTimeSeriesId;
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

    public String getObservationTimeSeriesLabel() {
        return observationTimeSeriesLabel;
    }

    public void setObservationTimeSeriesLabel(String observationTimeSeriesLabel) {
        this.observationTimeSeriesLabel = observationTimeSeriesLabel;
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
     * @return the observation text
     */
    public String getObservationText() {
        return observationText;
    }

    /**
     * @param observationText The observation text to set
     */
    public void setObservationText(String observationText) {
        this.observationText = observationText;
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

    /**
     * @return the observationDataSchema
     */
    public ObservationDataSchema getObservationDataSchema() {
        return observationDataSchema;
    }

    /**
     * @param observationDataSchema the observationDataSchema to set
     */
    public void setObservationDataSchema(ObservationDataSchema observationDataSchema) {
        this.observationDataSchema = observationDataSchema;
    }

    /**
     * @return the observationData
     */
    public String getObservationData() {
        return observationData;
    }

    /**
     * @param observationData the observationData to set
     */
    public void setObservationData(String observationData) {
        this.observationData = observationData;
    }

    public Boolean getIsPositive() {
        return isPositive;
    }

    public void setIsPositive(Boolean positive) {
        isPositive = positive;
    }
}
