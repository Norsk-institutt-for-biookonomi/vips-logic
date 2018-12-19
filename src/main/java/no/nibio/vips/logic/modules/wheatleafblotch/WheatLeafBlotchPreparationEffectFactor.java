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

package no.nibio.vips.logic.modules.wheatleafblotch;

import java.io.Serializable;
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
 * @copyright 2018 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@Table(name = "wheatleafblotch_preparation_effect_factor", schema = "wheatleafb")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "WheatLeafBlotchPreparationEffectFactor.findAll", query = "SELECT p FROM WheatLeafBlotchPreparationEffectFactor p"),
    @NamedQuery(name = "WheatLeafBlotchPreparationEffectFactor.findByPreparationId", query = "SELECT p FROM WheatLeafBlotchPreparationEffectFactor p WHERE p.preparationId = :preparationId"),
    @NamedQuery(name = "WheatLeafBlotchPreparationEffectFactor.findByFullDosisMlDaa", query = "SELECT p FROM WheatLeafBlotchPreparationEffectFactor p WHERE p.fullDosisMlDaa = :fullDosisMlDaa"),
    @NamedQuery(name = "WheatLeafBlotchPreparationEffectFactor.findByFactorValue", query = "SELECT p FROM WheatLeafBlotchPreparationEffectFactor p WHERE p.factorValue = :factorValue")})
public class WheatLeafBlotchPreparationEffectFactor implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "preparation_id")
    private Integer preparationId;
    @Column(name = "full_dosis_ml_daa")
    private Double fullDosisMlDaa;
    @Column(name = "factor_value")
    private Double factorValue;

    public WheatLeafBlotchPreparationEffectFactor() {
    }

    public WheatLeafBlotchPreparationEffectFactor(Integer preparationId) {
        this.preparationId = preparationId;
    }

    public Integer getPreparationId() {
        return preparationId;
    }

    public void setPreparationId(Integer preparationId) {
        this.preparationId = preparationId;
    }

    public Double getFullDosisMlDaa() {
        return fullDosisMlDaa;
    }

    public void setFullDosisMlDaa(Double fullDosisMlDaa) {
        this.fullDosisMlDaa = fullDosisMlDaa;
    }

    public Double getFactorValue() {
        return factorValue;
    }

    public void setFactorValue(Double factorValue) {
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
        if (!(object instanceof WheatLeafBlotchPreparationEffectFactor)) {
            return false;
        }
        WheatLeafBlotchPreparationEffectFactor other = (WheatLeafBlotchPreparationEffectFactor) object;
        if ((this.preparationId == null && other.preparationId != null) || (this.preparationId != null && !this.preparationId.equals(other.preparationId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "no.nibio.vips.logic.modules.barleynetblotch.PreparationEffectFactor[ preparationId=" + preparationId + " ]";
    }

}
