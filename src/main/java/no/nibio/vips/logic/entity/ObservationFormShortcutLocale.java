/*
 * Copyright (c) 2017 NIBIO <http://www.nibio.no/>. 
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
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2017 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "observation_form_shortcut_locale")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ObservationFormShortcutLocale.findAll", query = "SELECT o FROM ObservationFormShortcutLocale o")
    , @NamedQuery(name = "ObservationFormShortcutLocale.findByObservationFormShortcutId", query = "SELECT o FROM ObservationFormShortcutLocale o WHERE o.observationFormShortcutId = :observationFormShortcutId")
    , @NamedQuery(name = "ObservationFormShortcutLocale.findByLocale", query = "SELECT o FROM ObservationFormShortcutLocale o WHERE o.locale = :locale")
    , @NamedQuery(name = "ObservationFormShortcutLocale.findByLabel", query = "SELECT o FROM ObservationFormShortcutLocale o WHERE o.label = :label")})
public class ObservationFormShortcutLocale implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "observation_form_shortcut_id")
    private Integer observationFormShortcutId;
    @Size(max = 10)
    @Column(name = "locale")
    private String locale;
    @Size(max = 255)
    @Column(name = "label")
    private String label;

    public ObservationFormShortcutLocale() {
    }

    public ObservationFormShortcutLocale(Integer observationFormShortcutId) {
        this.observationFormShortcutId = observationFormShortcutId;
    }

    public Integer getObservationFormShortcutId() {
        return observationFormShortcutId;
    }

    public void setObservationFormShortcutId(Integer observationFormShortcutId) {
        this.observationFormShortcutId = observationFormShortcutId;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (observationFormShortcutId != null ? observationFormShortcutId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ObservationFormShortcutLocale)) {
            return false;
        }
        ObservationFormShortcutLocale other = (ObservationFormShortcutLocale) object;
        if ((this.observationFormShortcutId == null && other.observationFormShortcutId != null) || (this.observationFormShortcutId != null && !this.observationFormShortcutId.equals(other.observationFormShortcutId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.ObservationFormShortcutLocale[ observationFormShortcutId=" + observationFormShortcutId + " ]";
    }

}
