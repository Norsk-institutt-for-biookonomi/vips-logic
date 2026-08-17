/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package no.nibio.vips.logic.scheduling;

import it.sauronsoftware.cron4j.SchedulerListener;
import it.sauronsoftware.cron4j.TaskExecutor;

import no.nibio.vips.logic.controller.session.SessionControllerGetter;

/**
 * Logs task events to the database
 * @copyright 2013-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class TaskLoggerSchedulerListener implements SchedulerListener{

    @Override
    public void taskLaunching(TaskExecutor te) {
    	SessionControllerGetter.getSchedulingBean().logTaskLaunching(te);
    }

    @Override
    public void taskSucceeded(TaskExecutor te) {
    	SessionControllerGetter.getSchedulingBean().logTaskSucceeded(te);
    }

    @Override
    public void taskFailed(TaskExecutor te, Throwable thrwbl) {
    	thrwbl.printStackTrace();
    	SessionControllerGetter.getSchedulingBean().logTaskFailed(te,thrwbl);
    }

}
