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

import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.SchedulerListener;
import it.sauronsoftware.cron4j.TaskExecutor;
import javax.ejb.EJB;
import no.nibio.vips.logic.controller.session.SchedulingBean;

/**
 * Add this listener to a scheduler if you want it the scheduler to terminate
 * after a task has completed
 * @copyright 2013-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class TerminateSchedulerListener implements SchedulerListener{
    
    @EJB
    SchedulingBean schedulingBean;

    @Override
    public void taskLaunching(TaskExecutor te) {
        
    }

    @Override
    public void taskSucceeded(TaskExecutor te) {
        terminateScheduler(te.getScheduler());
        
    }

    @Override
    public void taskFailed(TaskExecutor te, Throwable thrwbl) {
        System.out.println("Task failed. Error: " + thrwbl.getMessage() + " Terminating scheduler.");
        terminateScheduler(te.getScheduler());
    }
    
    /**
     * Stop all scheduling and terminate the scheduler.
     * @param scheduler 
     */
    private void terminateScheduler(Scheduler scheduler)
    {
        scheduler.stop();
        schedulingBean.getOneOffSchedulers().remove(scheduler);
        scheduler = null;
    }

}
