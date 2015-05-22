/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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

package no.bioforsk.vips.logic.modules.barleynetblotch;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "preparation_effect_factor", schema = "barleynetb")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "PreparationEffectFactor.findAll", query = "SELECT p FROM PreparationEffectFactor p"),
    @NamedQuery(name = "PreparationEffectFactor.findByPreparationId", query = "SELECT p FROM PreparationEffectFactor p WHERE p.preparationId = :preparationId"),
    @NamedQuery(name = "PreparationEffectFactor.findByFullDosisMlDaa", query = "SELECT p FROM PreparationEffectFactor p WHERE p.fullDosisMlDaa = :fullDosisMlDaa"),
    @NamedQuery(name = "PreparationEffectFactor.findByFactorValue", query = "SELECT p FROM PreparationEffectFactor p WHERE p.factorValue = :factorValue")})
public class PreparationEffectFactor implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "preparation_id")
    private Integer preparationId;
    @Column(name = "full_dosis_ml_daa")
    private BigDecimal fullDosisMlDaa;
    @Column(name = "factor_value")
    private BigDecimal factorValue;

    public PreparationEffectFactor() {
    }

    public PreparationEffectFactor(Integer preparationId) {
        this.preparationId = preparationId;
    }

    public Integer getPreparationId() {
        return preparationId;
    }

    public void setPreparationId(Integer preparationId) {
        this.preparationId = preparationId;
    }

    public BigDecimal getFullDosisMlDaa() {
        return fullDosisMlDaa;
    }

    public void setFullDosisMlDaa(BigDecimal fullDosisMlDaa) {
        this.fullDosisMlDaa = fullDosisMlDaa;
    }

    public BigDecimal getFactorValue() {
        return factorValue;
    }

    public void setFactorValue(BigDecimal factorValue) {
        this.factorValue = factorValue;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (preparationId != null ? preparationId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PreparationEffectFactor)) {
            return false;
        }
        PreparationEffectFactor other = (PreparationEffectFactor) object;
        if ((this.preparationId == null && other.preparationId != null) || (this.preparationId != null && !this.preparationId.equals(other.preparationId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.bioforsk.vips.logic.modules.barleynetblotch.PreparationEffectFactor[ preparationId=" + preparationId + " ]";
    }

}
