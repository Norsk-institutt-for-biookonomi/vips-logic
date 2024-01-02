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

package no.nibio.vips.logic.scheduling.tasks;

import it.sauronsoftware.cron4j.TaskExecutionContext;
import javax.ejb.EJB;
import no.nibio.vips.logic.controller.session.ForecastBean;
import no.nibio.vips.logic.controller.session.SessionControllerGetter;
import no.nibio.vips.logic.scheduling.TaskResult;
import no.nibio.vips.logic.scheduling.VipsLogicTask;

/**
 * Ensures model information is imported from VIPSCoreManager and 
 * @copyright 2014-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class UpdateModelInformationTask extends VipsLogicTask{

    /**
     * 
     * @param language
     * @return 
     */
    @Override
    public String getConfigFormDefinition(String language) {
         return "{\"fields\":[]}";
    }

    @Override
    public void execute(TaskExecutionContext tec) throws RuntimeException {
        tec.setCompleteness(0d);
        TaskResult taskResult = SessionControllerGetter.getForecastBean().updateModelInformation();
        tec.setCompleteness(taskResult.getCompleteness());
        tec.setStatusMessage(taskResult.getMessage());
        if(taskResult.getCompleteness() < 1.0){
            throw new RuntimeException();
        }
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
