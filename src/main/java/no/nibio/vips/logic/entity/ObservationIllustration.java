/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "observation_illustration")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ObservationIllustration.findAll", query = "SELECT o FROM ObservationIllustration o"),
    @NamedQuery(name = "ObservationIllustration.findByObservationId", query = "SELECT o FROM ObservationIllustration o WHERE o.observationIllustrationPK.observationId = :observationId"),
    @NamedQuery(name = "ObservationIllustration.findByFileName", query = "SELECT o FROM ObservationIllustration o WHERE o.observationIllustrationPK.fileName = :fileName")})
public class ObservationIllustration implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected ObservationIllustrationPK observationIllustrationPK;
    @Column(name = "observation_id", insertable = false, updatable = false)
    private Integer observationId;

    public ObservationIllustration() {
    }

    public ObservationIllustration(ObservationIllustrationPK observationIllustrationPK) {
        this.observationIllustrationPK = observationIllustrationPK;
    }

    public ObservationIllustration(int observationId, String fileName) {
        this.observationIllustrationPK = new ObservationIllustrationPK(observationId, fileName);
    }

    public ObservationIllustrationPK getObservationIllustrationPK() {
        return observationIllustrationPK;
    }

    public void setObservationIllustrationPK(ObservationIllustrationPK observationIllustrationPK) {
        this.observationIllustrationPK = observationIllustrationPK;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (observationIllustrationPK != null ? observationIllustrationPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ObservationIllustration)) {
            return false;
        }
        ObservationIllustration other = (ObservationIllustration) object;
        if ((this.observationIllustrationPK == null && other.observationIllustrationPK != null) || (this.observationIllustrationPK != null && !this.observationIllustrationPK.equals(other.observationIllustrationPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.ObservationIllustration[ observationIllustrationPK=" + observationIllustrationPK + " ]";
    }

}
