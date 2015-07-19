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
import no.nibio.vips.logic.scheduling.VipsLogicTask;
import no.nibio.vips.logic.scheduling.VipsLogicTaskFactory;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.web.forms.FormField;

/**
 * @copyright 2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class UpdateForecastSummaryTableTask  extends VipsLogicTask{

    @Override
    public String getConfigFormDefinition(String language) {
        StringBuilder retVal = new StringBuilder()
                .append("{")
                .append("   \"fields\":[")
                .append("       {")
                .append("           \"name\":\"factoryId\",")
                .append("           \"dataType\":\"").append(FormField.DATA_TYPE_INTEGER).append("\",")
                .append("           \"fieldType\":\"").append(FormField.FIELD_TYPE_HIDDEN).append("\",")
                .append("           \"webValue\":[\"").append(VipsLogicTaskFactory.UPDATE_FORECAST_SUMMARY_TABLE_TASK).append("\"]")
                .append("       }")
                .append("    ]")
                .append("}");
         return retVal.toString();
    }

    @Override
    public void execute(TaskExecutionContext tec) throws RuntimeException {
        tec.setCompleteness(0d);
        SessionControllerGetter.getForecastBean().updateForecastSummaryTable(SystemTime.getSystemTime());
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
