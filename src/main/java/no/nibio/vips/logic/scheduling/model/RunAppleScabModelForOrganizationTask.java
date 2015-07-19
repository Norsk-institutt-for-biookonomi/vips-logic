/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.scheduling.model;

import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;

/**
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class RunAppleScabModelForOrganizationTask extends Task {
    
    public Integer organizationId;
    
    public RunAppleScabModelForOrganizationTask(Integer organizationId){
        super();
        this.organizationId = organizationId;
    }

    @Override
    public void execute(TaskExecutionContext tec) throws RuntimeException {
        // Get all configured forecasts for AppleScabModel for this organization
        // in the given period. Run them
        
        // Does this also mean that this is the preprocessor, or should it be separate
        // (i.e. be called from this task)?
        
        // ALSO: Do task status things:
        // Set a status message
        // Set a status bar value (0.0-1.0)
        // Check if the TaskExecutionContext has been stopped between each weather data source
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
