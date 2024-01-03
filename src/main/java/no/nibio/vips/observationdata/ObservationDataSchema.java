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

package no.nibio.vips.observationdata;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import no.nibio.vips.logic.util.StringJsonUserType;
import org.hibernate.annotations.Type;


/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "observation_data_schema")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ObservationDataSchema.findAll", query = "SELECT o FROM ObservationDataSchema o"),
    @NamedQuery(name = "ObservationDataSchema.findByPK", query = "SELECT o FROM ObservationDataSchema o WHERE o.observationDataSchemaPK.organizationId = :organizationId AND o.observationDataSchemaPK.organismId = :organismId"),
    @NamedQuery(name = "ObservationDataSchema.findByOrganizationId", query = "SELECT o FROM ObservationDataSchema o WHERE o.observationDataSchemaPK.organizationId = :organizationId"),
    @NamedQuery(name = "ObservationDataSchema.findByOrganismId", query = "SELECT o FROM ObservationDataSchema o WHERE o.observationDataSchemaPK.organismId = :organismId")})
public class ObservationDataSchema implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected ObservationDataSchemaPK observationDataSchemaPK;
    @Convert(converter = StringJsonUserType.class)
    @Column(name = "data_schema")
    private String dataSchema;
    @Convert(converter = StringJsonUserType.class)
    @Column(name = "data_model", columnDefinition="json")
    private String dataModel;

    public ObservationDataSchema() {
    }

    public ObservationDataSchema(ObservationDataSchemaPK observationDataSchemaPK) {
        this.observationDataSchemaPK = observationDataSchemaPK;
    }

    public ObservationDataSchema(int organizationId, int organismId) {
        this.observationDataSchemaPK = new ObservationDataSchemaPK(organizationId, organismId);
    }

    public ObservationDataSchemaPK getObservationDataSchemaPK() {
        return observationDataSchemaPK;
    }

    public void setObservationDataSchemaPK(ObservationDataSchemaPK observationDataSchemaPK) {
        this.observationDataSchemaPK = observationDataSchemaPK;
    }

    public String getDataSchema() {
        return dataSchema;
    }

    public void setDataSchema(String dataSchema) {
        this.dataSchema = dataSchema;
    }

    public String getDataModel() {
        return dataModel;
    }

    public void setDataModel(String dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (observationDataSchemaPK != null ? observationDataSchemaPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ObservationDataSchema)) {
            return false;
        }
        ObservationDataSchema other = (ObservationDataSchema) object;
        if ((this.observationDataSchemaPK == null && other.observationDataSchemaPK != null) || (this.observationDataSchemaPK != null && !this.observationDataSchemaPK.equals(other.observationDataSchemaPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.observationdata.ObservationDataSchema[ observationDataSchemaPK=" + observationDataSchemaPK + " ]";
    }

}
