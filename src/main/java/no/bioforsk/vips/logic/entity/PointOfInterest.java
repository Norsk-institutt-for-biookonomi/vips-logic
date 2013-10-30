package no.bioforsk.vips.logic.entity;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@Table(name = "point_of_interest")
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="point_of_interest_type_id")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PointOfInterest.findAll", query = "SELECT p FROM PointOfInterest p"),
    @NamedQuery(name = "PointOfInterest.findByPointOfInterestId", query = "SELECT p FROM PointOfInterest p WHERE p.pointOfInterestId = :pointOfInterestId"),
    @NamedQuery(name = "PointOfInterest.findByPointOfInterestTypeId", query = "SELECT p FROM PointOfInterest p WHERE p.pointOfInterestType = :pointOfInterestType"),
    @NamedQuery(name = "PointOfInterest.findByName", query = "SELECT p FROM PointOfInterest p WHERE p.name = :name"),
    @NamedQuery(name = "PointOfInterest.findByLongitude", query = "SELECT p FROM PointOfInterest p WHERE p.longitude = :longitude"),
    @NamedQuery(name = "PointOfInterest.findByLatitude", query = "SELECT p FROM PointOfInterest p WHERE p.latitude = :latitude"),
    @NamedQuery(name = "PointOfInterest.findByAltitude", query = "SELECT p FROM PointOfInterest p WHERE p.altitude = :altitude")
})
public class PointOfInterest implements Serializable, Comparable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "point_of_interest_id")
    private Integer pointOfInterestId;
    @Size(max = 255)
    @Column(name = "name")
    private String name;
    @Size(max = 255)
    @Column(name = "time_zone")
    private String timeZone;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "longitude")
    private Double longitude;
    @Column(name = "latitude")
    private Double latitude;
    @Column(name = "altitude")
    private Double altitude;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "pointOfInterest")
    private Set<UserPointOfInterest> userPointOfInterestSet;
    @JoinColumn(name = "country_code", referencedColumnName = "country_code")
    @ManyToOne
    private Country countryCode;
    @JoinColumn(name="point_of_interest_type_id", referencedColumnName="point_of_interest_type_id")
    @ManyToOne
    private PointOfInterestType pointOfInterestType;
    
    public PointOfInterest() {
    }

    public PointOfInterest(Integer pointOfInterestId) {
        this.pointOfInterestId = pointOfInterestId;
    }

    public Integer getPointOfInterestId() {
        return pointOfInterestId;
    }

    public void setPointOfInterestId(Integer pointOfInterestId) {
        this.pointOfInterestId = pointOfInterestId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    @XmlTransient
    @JsonIgnore
    public Set<UserPointOfInterest> getUserPointOfInterestSet() {
        return userPointOfInterestSet;
    }

    public void setUserPointOfInterestSet(Set<UserPointOfInterest> userPointOfInterestSet) {
        this.userPointOfInterestSet = userPointOfInterestSet;
    }

    public Country getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(Country countryCode) {
        this.countryCode = countryCode;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (pointOfInterestId != null ? pointOfInterestId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PointOfInterest)) {
            return false;
        }
        PointOfInterest other = (PointOfInterest) object;
        if ((this.pointOfInterestId == null && other.pointOfInterestId != null) || (this.pointOfInterestId != null && !this.pointOfInterestId.equals(other.pointOfInterestId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.PointOfInterest[ pointOfInterestId=" + pointOfInterestId + " ]";
    }

    /**
     * @return the pointOfInterestType
     */
    public PointOfInterestType getPointOfInterestType() {
        return pointOfInterestType;
    }

    /**
     * @param pointOfInterestType the pointOfInterestType to set
     */
    public void setPointOfInterestType(PointOfInterestType pointOfInterestType) {
        this.pointOfInterestType = pointOfInterestType;
    }

    @Override
    public int compareTo(Object t) {
        return this.compareTo((PointOfInterest) t);
    }
    
    /**
     * We sort alphabetically by name
     * @param other
     * @return 
     */
    public int compareTo(PointOfInterest other)
    {
        return this.getName().compareToIgnoreCase(other.getName());
    }

    

    /**
     * @return the timeZone
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * @param timeZone the timeZone to set
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}
