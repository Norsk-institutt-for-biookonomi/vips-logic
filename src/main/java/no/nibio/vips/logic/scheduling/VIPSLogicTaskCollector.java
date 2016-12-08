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

package no.nibio.vips.logic.scheduling;

import it.sauronsoftware.cron4j.TaskCollector;
import it.sauronsoftware.cron4j.TaskTable;

/**
 * Idea: One Task Collector per organization
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class VIPSLogicTaskCollector implements TaskCollector{
    private TaskTable taskTable;
    private Integer organizationId;
    
    public VIPSLogicTaskCollector(Integer organizationId)
    {
        this.organizationId = organizationId;
    }

    @Override
    public TaskTable getTasks() {
        if(this.taskTable == null)
            this.taskTable = new TaskTable();
        return this.taskTable;
    }
    
    /**
     * Intention: Reveal which organization this task collector is for.
     * @return 
     */
    public Integer getOrganizationId(){
        return this.organizationId;
    }
    
    

}
