package no.nibio.vips.logic.entity;

import java.util.Date;

public class AppObservation implements no.nibio.vips.observation.Observation{
    private Date timeOfObservation;
    private String geoinfo, observationData, name;


    
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
     * @return the geoinfo
     */
    public String getGeoinfo() {
        return geoinfo;
    }

    /**
     * @param geoinfo the geoinfo to set
     */
    public void setGeoinfo(String geoinfo) {
        this.geoinfo = geoinfo;
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

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Object t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
