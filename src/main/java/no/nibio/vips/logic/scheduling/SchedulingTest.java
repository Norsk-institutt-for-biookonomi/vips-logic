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

import no.nibio.vips.logic.scheduling.VipsLogicTask.TaskExecutionContext;

/**
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class SchedulingTest extends VipsLogicTask {

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
    public String getConfigFormDefinition(String language) {
        return "{\"fields\":[]}";
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
