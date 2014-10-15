/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the Bioforsk Open Source License as published by 
 * Bioforsk, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Bioforsk Open Source License for more details.
 * 
 * You should have received a copy of the Bioforsk Open Source License
 * along with VIPSLogic.  If not, see <http://www.bioforsk.no/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.scheduling;

import it.sauronsoftware.cron4j.TaskExecutionContext;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class SchedulingTest extends it.sauronsoftware.cron4j.Task{

    @Override
    public void execute(TaskExecutionContext tec) throws RuntimeException {
        System.out.println("Another minute ticking by...");
        tec.setStatusMessage("Oh yes, we're running!!!");
        tec.setCompleteness(0d);
        try {
			Thread.sleep(1000L * 10L );
        } catch (InterruptedException e) {
                return;
        }
        
        double completeness = (double) 10/60;
        System.out.println("double completeness = " + completeness);
        tec.setCompleteness(completeness);
        System.out.println("Completeness=" + tec.getTaskExecutor().getCompleteness());
        
        try {
			Thread.sleep(1000L * 30L );
        } catch (InterruptedException e) {
                return;
        }
        completeness = (double) 40/60;
        tec.setCompleteness(completeness);
        System.out.println("Completeness=" + tec.getTaskExecutor().getCompleteness());
        try {
			Thread.sleep(1000L * 30L );
        } catch (InterruptedException e) {
                return;
        }
        System.out.println("Exit job after 70 seconds...");
    }
    
    @Override
    public boolean supportsStatusTracking()
    {
        return true;
    }
    
    @Override
    public boolean supportsCompletenessTracking()
    {
        return true;
    }

}
