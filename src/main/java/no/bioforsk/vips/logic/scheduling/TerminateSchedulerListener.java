/*
 * Copyright (c) 2013 Bioforsk <http://www.bioforsk.no/>. 
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

package no.bioforsk.vips.logic.scheduling;

import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.SchedulerListener;
import it.sauronsoftware.cron4j.TaskExecutor;
import no.bioforsk.vips.logic.controller.session.SchedulingBean;
import no.bioforsk.vips.logic.util.SessionControllerGetter;

/**
 * Add this listener to a scheduler if you want it the scheduler to terminate
 * after a task has completed
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class TerminateSchedulerListener implements SchedulerListener{

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
        SchedulingBean schedulingBean = SessionControllerGetter.getSchedulingBean();
        schedulingBean.getOneOffSchedulers().remove(scheduler);
        scheduler = null;
    }

}
