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

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "weather_station_data_source")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "WeatherStationDataSource.findAll", query = "SELECT w FROM WeatherStationDataSource w"),
    @NamedQuery(name = "WeatherStationDataSource.findGridSources", query = "SELECT w FROM WeatherStationDataSource w WHERE w.isGrid IS TRUE"),
    @NamedQuery(name = "WeatherStationDataSource.findByWeatherStationDataSourceId", query = "SELECT w FROM WeatherStationDataSource w WHERE w.weatherStationDataSourceId = :weatherStationDataSourceId"),
    @NamedQuery(name = "WeatherStationDataSource.findByName", query = "SELECT w FROM WeatherStationDataSource w WHERE w.name = :name"),
    @NamedQuery(name = "WeatherStationDataSource.findByDefaultDescription", query = "SELECT w FROM WeatherStationDataSource w WHERE w.defaultDescription = :defaultDescription"),
    @NamedQuery(name = "WeatherStationDataSource.findByUri", query = "SELECT w FROM WeatherStationDataSource w WHERE w.uri = :uri"),
    @NamedQuery(name = "WeatherStationDataSource.findByDatafetchUriExpression", query = "SELECT w FROM WeatherStationDataSource w WHERE w.datafetchUriExpression = :datafetchUriExpression")})
public class WeatherStationDataSource implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "weather_station_data_source_id")
    private Integer weatherStationDataSourceId;
    @Size(max = 255)
    @Column(name = "name")
    private String name;
    @Size(max = 2147483647)
    @Column(name = "default_description")
    private String defaultDescription;
    @Size(max = 1024)
    @Column(name = "uri")
    private String uri;
    @Size(max = 1024)
    @Column(name = "datafetch_uri_expression")
    private String datafetchUriExpression;
    @Size(max = 1024)
    @Column(name = "info_uri_expression")
    private String infoUriExpression;
    @Column(name = "is_grid")
    private Boolean isGrid;

   

    public Boolean getIsGrid() {
        return isGrid;
    }

    public void setIsGrid(Boolean isGrid) {
        this.isGrid = isGrid;
    }

    public WeatherStationDataSource() {
    }

    public WeatherStationDataSource(Integer weatherStationDataSourceId) {
        this.weatherStationDataSourceId = weatherStationDataSourceId;
    }

    public Integer getWeatherStationDataSourceId() {
        return weatherStationDataSourceId;
    }

    public void setWeatherStationDataSourceId(Integer weatherStationDataSourceId) {
        this.weatherStationDataSourceId = weatherStationDataSourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultDescription() {
        return defaultDescription;
    }

    public void setDefaultDescription(String defaultDescription) {
        this.defaultDescription = defaultDescription;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getDatafetchUriExpression() {
        return datafetchUriExpression;
    }
    
    public void setDatafetchUriExpression(String datafetchUriExpression) {
        this.datafetchUriExpression = datafetchUriExpression;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (weatherStationDataSourceId != null ? weatherStationDataSourceId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof WeatherStationDataSource)) {
            return false;
        }
        WeatherStationDataSource other = (WeatherStationDataSource) object;
        return (this.weatherStationDataSourceId != null || other.weatherStationDataSourceId == null) && (this.weatherStationDataSourceId == null || this.weatherStationDataSourceId.equals(other.weatherStationDataSourceId));
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.WeatherStationDataSource[ weatherStationDataSourceId=" + weatherStationDataSourceId + " ]";
    }

    /**
     * @return the infoUriExpression
     */
    public String getInfoUriExpression() {
        return infoUriExpression;
    }

    /**
     * @param infoUriExpression the infoUriExpression to set
     */
    public void setInfoUriExpression(String infoUriExpression) {
        this.infoUriExpression = infoUriExpression;
    }

    

}
