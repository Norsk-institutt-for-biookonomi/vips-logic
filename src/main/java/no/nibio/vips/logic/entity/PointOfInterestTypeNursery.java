/*
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
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
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @copyright 2023 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Entity
@DiscriminatorValue("7")
@Table(name = "point_of_interest_nursery")
public class PointOfInterestTypeNursery extends PointOfInterest implements Serializable {
    
    private Double impactRadius;

    /**
     * Radius (in km) of the expected impact (implemented for fire blight)
     * of the nursery. Used for map rendering
     * @return the impactRadius
     */
    @Column(name="impact_radius")
    public Double getImpactRadius() {
        return impactRadius;
    }

    /**
     * @param impactRadius the impactRadius to set
     */
    public void setImpactRadius(Double impactRadius) {
        this.impactRadius = impactRadius;
    }
    
    @Transient
    @Override
    public String getGeoJSON()
    {
        this.addProperty("impactRadius", this.getImpactRadius());
        return super.getGeoJSON();
    }
    
    @Transient
    @Override
    public Map<String,Object> getProperties() {
        this.addProperty("impactRadius", this.getImpactRadius());
        return super.getProperties();
    }
}
