package no.bioforsk.vips.logic.entity;

import java.io.Serializable;
import java.text.MessageFormat;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Extension of {@see PointOfInterest}, with additional info for weather stations
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Entity
@DiscriminatorValue("1")
@Table(name = "point_of_interest_weather_station")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PointOfInterestWeatherStation.findAll", query = "SELECT p FROM PointOfInterest p WHERE p.pointOfInterestType.pointOfInterestTypeId=1"),
    @NamedQuery(name = "PointOfInterestWeatherStation.findByPointOfInterestId", query = "SELECT p FROM PointOfInterest p WHERE p.pointOfInterestId = :pointOfInterestId AND p.pointOfInterestType.pointOfInterestTypeId=1"),
    @NamedQuery(name = "PointOfInterestWeatherStation.findByUserId", query = "SELECT p FROM PointOfInterest p WHERE p.pointOfInterestType.pointOfInterestTypeId=1 AND p.pointOfInterestId IN(SELECT up.pointOfInterest.pointOfInterestId FROM UserPointOfInterest up WHERE up.userPointOfInterestPK.userId = :userId)")
})
public class PointOfInterestWeatherStation extends PointOfInterest implements Serializable {
    @Size(max = 255)
    @Column(name = "weather_station_remote_id")
    private String weatherStationRemoteId;
    @JoinColumn(name = "weather_station_data_source_id", referencedColumnName = "weather_station_data_source_id")
    @ManyToOne
    private WeatherStationDataSource weatherStationDataSourceId;
    @JoinColumn(name = "point_of_interest_id", referencedColumnName = "point_of_interest_id", insertable = false, updatable = false)
    @OneToOne(optional = false)
    private PointOfInterest pointOfInterest;

    public PointOfInterestWeatherStation() {
    }


    public String getWeatherStationRemoteId() {
        return weatherStationRemoteId;
    }

    public void setWeatherStationRemoteId(String weatherStationRemoteId) {
        this.weatherStationRemoteId = weatherStationRemoteId;
    }

    public WeatherStationDataSource getWeatherStationDataSourceId() {
        return weatherStationDataSourceId;
    }

    public void setWeatherStationDataSourceId(WeatherStationDataSource weatherStationDataSourceId) {
        this.weatherStationDataSourceId = weatherStationDataSourceId;
    }

    public PointOfInterest getPointOfInterest() {
        return pointOfInterest;
    }

    public void setPointOfInterest(PointOfInterest pointOfInterest) {
        this.pointOfInterest = pointOfInterest;
    }
    
    @Override
    public Integer getPointOfInterestId()
    {
        return super.getPointOfInterestId();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.getPointOfInterestId() != null ? this.getPointOfInterestId().hashCode() : 0);
        return hash;
    }

    
    
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PointOfInterestWeatherStation)) {
            return false;
        }
        PointOfInterestWeatherStation other = (PointOfInterestWeatherStation) object;
        if ((this.getPointOfInterestId() == null && other.getPointOfInterestId() != null) || (this.getPointOfInterestId() != null && !this.getPointOfInterestId().equals(other.getPointOfInterestId()))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.entity.PointOfInterestWeatherStation[ pointOfInterestId=" + this.getPointOfInterestId() + " ]";
    }

    /**
     * Returns the complete URI for fetching weather data from the given
     * weather data source and weather station
     * @return 
     */
    public String getDataFetchUri()
    {
        String[] formatParam = new String[1];
        formatParam[0] = this.getWeatherStationRemoteId();
        String dataRequestUri = new MessageFormat(this.getWeatherStationDataSourceId().getDatafetchUriExpression()).format(formatParam);
        return dataRequestUri;
    }
    
}
