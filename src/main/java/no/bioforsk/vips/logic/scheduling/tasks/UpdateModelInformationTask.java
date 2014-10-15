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

package no.bioforsk.vips.logic.scheduling.tasks;

import it.sauronsoftware.cron4j.TaskExecutionContext;
import no.bioforsk.vips.logic.scheduling.VipsLogicTask;
import no.bioforsk.vips.logic.scheduling.VipsLogicTaskFactory;
import no.bioforsk.vips.logic.util.SessionControllerGetter;
import no.bioforsk.web.forms.FormField;

/**
 * Ensures model information is imported from VIPSCoreManager and 
 * @copyright 2014 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class UpdateModelInformationTask extends VipsLogicTask{

    @Override
    public String getConfigFormDefinition(String language) {
         StringBuilder retVal = new StringBuilder()
                .append("{")
                .append("   \"fields\":[")
                .append("       {")
                .append("           \"name\":\"factoryId\",")
                .append("           \"dataType\":\"").append(FormField.DATA_TYPE_INTEGER).append("\",")
                .append("           \"fieldType\":\"").append(FormField.FIELD_TYPE_HIDDEN).append("\",")
                .append("           \"webValue\":[\"").append(VipsLogicTaskFactory.UPDATE_MODEL_INFORMATION_TASK).append("\"]")
                .append("       }")
                .append("    ]")
                .append("}");
         return retVal.toString();
    }

    @Override
    public void execute(TaskExecutionContext tec) throws RuntimeException {
        tec.setCompleteness(0d);
        SessionControllerGetter.getForecastBean().updateModelInformation();
        tec.setCompleteness(1d);
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
