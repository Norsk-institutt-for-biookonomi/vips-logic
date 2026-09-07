/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>.
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
package no.nibio.vips.logic.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import no.nibio.vips.logic.util.GISEntityUtil;

import javax.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "observation_time_series")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ObservationTimeSeries.findAll", query = "SELECT ots FROM ObservationTimeSeries ots"),
    @NamedQuery(name = "ObservationTimeSeries.findByObservationTimeSeriesId", query = "SELECT ots FROM ObservationTimeSeries ots WHERE ots.observationTimeSeriesId = :id"),
    @NamedQuery(name = "ObservationTimeSeries.findByObservationTimeSeriesIds", query = "SELECT ots FROM ObservationTimeSeries ots WHERE ots.observationTimeSeriesId IN :observationTimeSeriesIds"),
    @NamedQuery(name = "ObservationTimeSeries.findByUserId", query = "SELECT ots FROM ObservationTimeSeries ots WHERE ots.userId IN(:userId) order by ots.year desc, ots.created desc"),
    @NamedQuery(name = "ObservationTimeSeries.findByLocationPointOfInterestId", query = "SELECT ots FROM ObservationTimeSeries ots WHERE ots.locationPointOfInterestId = :locationPointOfInterestId")
})
public class ObservationTimeSeries implements Serializable {

    private static final long serialVersionUID = 1L;
    private Integer observationTimeSeriesId;
    private Organism cropOrganism;
    private Organism organism;
    private Integer year;
    private String name;
    private String description;
    private Date created;
    private Integer userId;
    private VipsLogicUser user; // Transient
    private String source;
    private Date lastModified;
    private Integer lastModifiedBy;
    private VipsLogicUser lastModifiedByUser; // Transient
    private Integer locationPointOfInterestId;
    private PointOfInterest locationPointOfInterest;
    private List<Gis> geoInfoList;
    private Boolean locationIsPrivate;
    private PolygonService polygonService;

    private GISEntityUtil GISEntityUtil;

    public ObservationTimeSeries() {
        this.GISEntityUtil = new GISEntityUtil();
        this.source = "WEB";
        this.created = new Date();
    }

    public ObservationTimeSeries(String source) {
        this();
        this.source = source;
    }

    /**
     * @return the id of the observation time series
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "observation_time_series_id")
    public Integer getObservationTimeSeriesId() {
        return observationTimeSeriesId;
    }

    /**
     * @param id the observation time series id to set
     */
    public void setObservationTimeSeriesId(Integer id) {
        this.observationTimeSeriesId = id;
    }


    /**
     * @return the crop organism
     */
    @JoinColumn(name = "crop_organism_id", referencedColumnName = "organism_id")
    @ManyToOne
    public Organism getCropOrganism() {
        return cropOrganism;
    }

    /**
     * @param cropOrganism the crop organism to set
     */
    public void setCropOrganism(Organism cropOrganism) {
        this.cropOrganism = cropOrganism;
    }

    /**
     * @return the crop organism id
     */
    @Transient
    public Integer getCropOrganismId() {
        return this.getCropOrganism() != null ? this.getCropOrganism().getOrganismId() : null;
    }

    /**
     * @return the organism
     */
    @JoinColumn(name = "organism_id", referencedColumnName = "organism_id")
    @ManyToOne
    public Organism getOrganism() {
        return organism;
    }

    /**
     * @param organism the organism to set
     */
    public void setOrganism(Organism organism) {
        this.organism = organism;
    }

    /**
     * @return the organism id
     */
    @Transient
    public Integer getOrganismId() {
        return this.getOrganism() != null ? this.getOrganism().getOrganismId() : null;
    }

    /**
     * @return the year of the observation time series
     */
    @Column(name = "year")
    public Integer getYear() {
        return year;
    }

    /**
     * @param year the observation time series year to set
     */
    public void setYear(Integer year) {
        this.year = year;
    }

    /**
     * @return the name of the observation time series
     */
    @Column(name = "name")
    public String getName() {
        return name;
    }

    /**
     * @param name the observation time series name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * This logic if also implemented in the observation app, see the function timeSeriesLabel in CommonUtil.vue
     * @return the first eight letters of the given name, in uppercase with spaces removed
     */
    @Transient
    public String getLabel() {
        if (this.name == null) {
            return null;
        }
        String timeSeriesName = this.name;
        timeSeriesName = timeSeriesName.replaceAll("\\s", "");
        timeSeriesName = timeSeriesName.substring(0, Math.min(timeSeriesName.length(), 8));
        return timeSeriesName.toUpperCase();
    }

    /**
     * @return the description of the observation time series
     */
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    /**
     * @param description the observation time series text to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the creation date of the observation time series
     */
    @NotNull
    @Basic(optional = false)
    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getCreated() {
        return created;
    }

    /**
     * @param created the creation date to set
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    @NotNull
    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_modified")
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
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
     * @return from where the observation time series originally was created, either WEB or APP
     */
    @Column(name = "source")
    public String getSource() {
        return source;
    }

    /**
     * @param source From where the observation time series originally was created
     */
    public void setSource(String source) {
        this.source = source;
    }

    @Column(name = "last_modified_by")
    public Integer getLastModifiedBy() {
        return lastModifiedBy;
    }

    /**
     * @param lastModifiedBy the lastModifiedBy to set
     */
    public void setLastModifiedBy(Integer lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    /**
     * @return the user who last modified the observation time series
     */
    @Transient
    @JsonIgnore
    public VipsLogicUser getLastModifiedByUser() {
        return lastModifiedByUser;
    }

    /**
     * @param lastModifiedByUser the lastModifiedByUser to set
     */
    public void setLastModifiedByUser(VipsLogicUser lastModifiedByUser) {
        this.lastModifiedByUser = lastModifiedByUser;
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

    /**
     * @return the location
     */
    @Transient
    public PointOfInterest getLocationPointOfInterest() {
        return locationPointOfInterest;
    }

    /**
     * @param locationPointOfInterest the location to set
     */
    public void setLocationPointOfInterest(PointOfInterest locationPointOfInterest) {
        this.locationPointOfInterest = locationPointOfInterest;
    }

    public void setGeoInfoList(List<Gis> geoInfoList) {
        this.geoInfoList = geoInfoList;
    }

    @Transient
    public String getGeoInfo() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("observationTimeSeriesId", this.getObservationTimeSeriesId());
        return this.GISEntityUtil.getGeoJSONFromGis(this.geoInfoList, properties);
    }

    /**
     * @return the locationIsPrivate
     */
    @Column(name = "location_is_private")
    public Boolean getLocationIsPrivate() {
        return locationIsPrivate != null ? locationIsPrivate : false;
    }

    /**
     * @param locationIsPrivate the locationIsPrivate to set
     */
    public void setLocationIsPrivate(Boolean locationIsPrivate) {
        this.locationIsPrivate = locationIsPrivate;
    }

    /**
     * @return the polygon service
     */
    @JoinColumn(name = "polygon_service_id", referencedColumnName = "polygon_service_id")
    @ManyToOne
    public PolygonService getPolygonService() {
        return this.polygonService;
    }

    /**
     * @param polygonService the polygon service to set
     */
    public void setPolygonService(PolygonService polygonService) {
        this.polygonService = polygonService;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (observationTimeSeriesId != null ? observationTimeSeriesId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObservationTimeSeries that = (ObservationTimeSeries) o;
        return observationTimeSeriesId.equals(that.observationTimeSeriesId);
    }

    @Override
    public String toString() {
        return "ObservationTimeSeries{" +
            "observationTimeSeriesId=" + observationTimeSeriesId +
            ", cropOrganism=" + cropOrganism +
            ", organism=" + organism +
            ", year=" + year +
            ", name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", created=" + created +
            ", userId=" + userId +
            ", user=" + user +
            ", source=" + source +
            ", lastModified=" + lastModified +
            ", lastModifiedBy=" + lastModifiedBy +
            ", lastModifiedByUser=" + lastModifiedByUser +
            ", locationPointOfInterestId=" + locationPointOfInterestId +
            ", locationPointOfInterest=" + locationPointOfInterest +
            ", geoInfoList=" + geoInfoList +
            ", locationIsPrivate=" + locationIsPrivate +
            ", polygonService=" + polygonService +
            '}';
    }

    public int compareTo(ObservationTimeSeries other) {
        if (this.getYear() != null && other.getYear() != null) {
            return other.getYear().compareTo(this.getYear());
        }
        return this.getName().compareTo(other.getName());
    }
}
