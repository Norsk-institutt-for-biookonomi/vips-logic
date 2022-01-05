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

import it.sauronsoftware.cron4j.SchedulerListener;
import it.sauronsoftware.cron4j.TaskExecutor;
import javax.ejb.EJB;
import no.nibio.vips.logic.controller.session.SchedulingBean;

/**
 * Logs task events to the database
 * @copyright 2013-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class TaskLoggerSchedulerListener implements SchedulerListener{
    
    @EJB
    SchedulingBean schedulingBean;
    
    @Override
    public void taskLaunching(TaskExecutor te) {
        schedulingBean.logTaskLaunching(te);
        
    }

    @Override
    public void taskSucceeded(TaskExecutor te) {
        schedulingBean.logTaskSucceeded(te);
    }

    @Override
    public void taskFailed(TaskExecutor te, Throwable thrwbl) {
        schedulingBean.logTaskFailed(te,thrwbl);
    }

}
