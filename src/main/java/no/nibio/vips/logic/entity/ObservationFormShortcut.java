/*
 * Copyright (c) 2017 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the NIBIO Open Source License as published by 
 * NIBIO, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * NIBIO Open Source License for more details.
 * 
 * You should have received a copy of the NIBIO Open Source License
 * along with VIPSLogic.  If not, see <http://www.nibio.no/licenses/>.
 * 
 */

package no.nibio.vips.logic.entity;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2017 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "observation_form_shortcut")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "ObservationFormShortcut.findAll", query = "SELECT o FROM ObservationFormShortcut o")
    , @NamedQuery(name = "ObservationFormShortcut.findByObservationFormShortcutId", query = "SELECT o FROM ObservationFormShortcut o WHERE o.observationFormShortcutId = :observationFormShortcutId")
    , @NamedQuery(name = "ObservationFormShortcut.findByOrganizationId", query = "SELECT o FROM ObservationFormShortcut o WHERE o.organizationId = :organizationId")
    , @NamedQuery(name = "ObservationFormShortcut.findByDefaultLabel", query = "SELECT o FROM ObservationFormShortcut o WHERE o.defaultLabel = :defaultLabel")
})
public class ObservationFormShortcut implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "observation_form_shortcut_id")
    private Integer observationFormShortcutId;
    @Column(name = "organization_id")
    private Integer organizationId;
    @Size(max = 255)
    @Column(name = "default_label")
    private String defaultLabel;
    @Size(max = 2047)
    @Column(name = "form_query_param_string")
    private String formQueryParamString;
    @Size(max = 2047)
    @Column(name = "list_query_param_string")
    private String listQueryParamString;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "observationFormShortcutId", fetch = FetchType.EAGER)
    private Set<ObservationFormShortcutLocale> shortcutLocaleSet;

    public ObservationFormShortcut() {
    }

    public ObservationFormShortcut(Integer observationFormShortcutId) {
        this.observationFormShortcutId = observationFormShortcutId;
    }

    public Integer getObservationFormShortcutId() {
        return observationFormShortcutId;
    }

    public void setObservationFormShortcutId(Integer observationFormShortcutId) {
        this.observationFormShortcutId = observationFormShortcutId;
    }

    public String getDefaultLabel() {
        return defaultLabel;
    }

    public void setDefaultLabel(String defaultLabel) {
        this.defaultLabel = defaultLabel;
    }

    public String getFormQueryParamString() {
        return formQueryParamString;
    }

    public void setFormQueryParamString(String formQueryParamString) {
        this.formQueryParamString = formQueryParamString;
    }

    public String getLocalLabel(String language)
    {
        if(this.shortcutLocaleSet == null || this.shortcutLocaleSet.isEmpty())
        {
            return this.getDefaultLabel();
        }
        ObservationFormShortcutLocale l = this.shortcutLocaleSet.stream()
                .filter(localS -> localS.getLocale().equals(language))
                .findAny()
                .orElse(
                    this.shortcutLocaleSet.stream()
                    .filter(localS -> localS.getLocale().equals("en"))
                    .findAny()
                    .orElse(null)
                );
        return l != null ? l.getLabel() : this.getDefaultLabel();
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
        if (!(object instanceof ObservationFormShortcut)) {
            return false;
        }
        ObservationFormShortcut other = (ObservationFormShortcut) object;
        if ((this.observationFormShortcutId == null && other.observationFormShortcutId != null) || (this.observationFormShortcutId != null && !this.observationFormShortcutId.equals(other.observationFormShortcutId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.entity.ObservationFormShortcut[ observationFormShortcutId=" + observationFormShortcutId + " ]";
    }

    /**
     * @return the organizationId
     */
    public Integer getOrganizationId() {
        return organizationId;
    }

    /**
     * @param organizationId the organizationId to set
     */
    public void setOrganizationId(Integer organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * @return the listQueryParamString
     */
    public String getListQueryParamString() {
        return listQueryParamString;
    }

    /**
     * @param listQueryParamString the listQueryParamString to set
     */
    public void setListQueryParamString(String listQueryParamString) {
        this.listQueryParamString = listQueryParamString;
    }

}
