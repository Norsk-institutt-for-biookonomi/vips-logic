/*
 * Copyright (c) 2021 NIBIO <http://www.nibio.no/>. 
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

import java.util.Date;

/**
 * A minimized object for serialization when only sync related
 * information is needed
 * 
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 *
 */
public class ObservationSyncInfo {
	private Integer observationId;
	private Date lastEditedTime;
	
	public ObservationSyncInfo(Observation obs)
	{
		this.observationId = obs.getObservationId();
		this.lastEditedTime = obs.getLastEditedTime();
	}
	
	public Integer getObservationId() {
		return observationId;
	}
	public void setObservationId(Integer observationId) {
		this.observationId = observationId;
	}
	public Date getLastEditedTime() {
		return lastEditedTime;
	}
	public void setLastEditedTime(Date lastEditedTime) {
		this.lastEditedTime = lastEditedTime;
	}
}
