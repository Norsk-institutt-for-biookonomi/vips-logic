/*
 * Copyright (c) 2021 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.modules.barkbeetle;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2021 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "season_trapsite_bivolt", schema="barkbeetle")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "SeasonTrapsiteBivolt.findAll", query = "SELECT s FROM SeasonTrapsiteBivolt s"),
    @NamedQuery(name = "SeasonTrapsiteBivolt.findBySeasonTrapsiteId", query = "SELECT s FROM SeasonTrapsiteBivolt s WHERE s.seasonTrapsiteId = :seasonTrapsiteId"),
    @NamedQuery(name = "SeasonTrapsiteBivolt.findBySeasonTrapsiteIds", query = "SELECT s FROM SeasonTrapsiteBivolt s WHERE s.seasonTrapsiteId IN (:seasonTrapsiteId)"),
    @NamedQuery(name = "SeasonTrapsiteBivolt.findByGen1MatureDoy", query = "SELECT s FROM SeasonTrapsiteBivolt s WHERE s.gen1MatureDoy = :gen1MatureDoy"),
    @NamedQuery(name = "SeasonTrapsiteBivolt.findByGen2MatureDoy", query = "SELECT s FROM SeasonTrapsiteBivolt s WHERE s.gen2MatureDoy = :gen2MatureDoy"),
    @NamedQuery(name = "SeasonTrapsiteBivolt.findByGen1MaturityRate", query = "SELECT s FROM SeasonTrapsiteBivolt s WHERE s.gen1MaturityRate = :gen1MaturityRate"),
    @NamedQuery(name = "SeasonTrapsiteBivolt.findByGen2MaturityRate", query = "SELECT s FROM SeasonTrapsiteBivolt s WHERE s.gen2MaturityRate = :gen2MaturityRate"),
    @NamedQuery(name = "SeasonTrapsiteBivolt.findByDateLastWeatherData", query = "SELECT s FROM SeasonTrapsiteBivolt s WHERE s.dateLastWeatherData = :dateLastWeatherData")})
public class SeasonTrapsiteBivolt implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "season_trapsite_id")
    private Integer seasonTrapsiteId;
    @OneToOne
    @MapsId
    @JoinColumn(name = "season_trapsite_id")
    private SeasonTrapsite seasonTrapsite;
    @Column(name = "gen_1_mature_doy")
    private Integer gen1MatureDoy;
    @Column(name = "gen_2_mature_doy")
    private Integer gen2MatureDoy;
    @Column(name = "gen_1_maturity_rate")
    private Double gen1MaturityRate;
    @Column(name = "gen_2_maturity_rate")
    private Double gen2MaturityRate;
    @Column(name = "date_last_weather_data")
    @Temporal(TemporalType.DATE)
    private Date dateLastWeatherData;

    public SeasonTrapsiteBivolt() {
    }

    public SeasonTrapsiteBivolt(Integer seasonTrapsiteId) {
        this.seasonTrapsiteId = seasonTrapsiteId;
    }

    public Integer getSeasonTrapsiteId() {
        return seasonTrapsiteId;
    }

    public void setSeasonTrapsiteId(Integer seasonTrapsiteId) {
        this.seasonTrapsiteId = seasonTrapsiteId;
    }

    public Integer getGen1MatureDoy() {
        return gen1MatureDoy;
    }

    public void setGen1MatureDoy(Integer gen1MatureDoy) {
        this.gen1MatureDoy = gen1MatureDoy;
    }

    public Integer getGen2MatureDoy() {
        return gen2MatureDoy;
    }

    public void setGen2MatureDoy(Integer gen2MatureDoy) {
        this.gen2MatureDoy = gen2MatureDoy;
    }

    public Double getGen1MaturityRate() {
        return gen1MaturityRate;
    }

    public void setGen1MaturityRate(Double gen1MaturityRate) {
        this.gen1MaturityRate = gen1MaturityRate;
    }

    public Double getGen2MaturityRate() {
        return gen2MaturityRate;
    }

    public void setGen2MaturityRate(Double gen2MaturityRate) {
        this.gen2MaturityRate = gen2MaturityRate;
    }

    public Date getDateLastWeatherData() {
        return dateLastWeatherData;
    }

    public void setDateLastWeatherData(Date dateLastWeatherData) {
        this.dateLastWeatherData = dateLastWeatherData;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (seasonTrapsiteId != null ? seasonTrapsiteId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SeasonTrapsiteBivolt)) {
            return false;
        }
        SeasonTrapsiteBivolt other = (SeasonTrapsiteBivolt) object;
        if ((this.seasonTrapsiteId == null && other.seasonTrapsiteId != null) || (this.seasonTrapsiteId != null && !this.seasonTrapsiteId.equals(other.seasonTrapsiteId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.modules.barkbeetle.SeasonTrapsiteBivolt[ seasonTrapsiteId=" + seasonTrapsiteId + " ]";
    }

	public SeasonTrapsite getSeasonTrapsite() {
		return seasonTrapsite;
	}

	public void setSeasonTrapsite(SeasonTrapsite seasonTrapsite) {
		this.seasonTrapsite = seasonTrapsite;
	}


}
