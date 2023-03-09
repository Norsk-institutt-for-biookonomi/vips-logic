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
