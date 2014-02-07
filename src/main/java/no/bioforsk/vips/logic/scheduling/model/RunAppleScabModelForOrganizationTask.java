/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with VIPSLogic.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.scheduling.model;

import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
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
