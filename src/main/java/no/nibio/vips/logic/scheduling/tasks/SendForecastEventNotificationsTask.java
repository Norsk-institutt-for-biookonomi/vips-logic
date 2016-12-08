/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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
import no.nibio.web.forms.FormField;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class SendForecastEventNotificationsTask extends VipsLogicTask {

    @Override
    public String getConfigFormDefinition(String language) {
        String retVal = "{"
                        + "   \"fields\":["
                        + "       {"
                        + "           \"name\":\"factoryId\","
                        + "           \"dataType\":\"" + FormField.DATA_TYPE_INTEGER + "\","
                        + "           \"fieldType\":\"" + FormField.FIELD_TYPE_HIDDEN + "\","
                        + "           \"webValue\":[\"" + VipsLogicTaskFactory.SEND_FORECAST_EVENT_NOTIFICATIONS_TASK + "\"]"
                        + "       }"
                        + "    ]}";
         return retVal;
    }

    @Override
    public void execute(TaskExecutionContext tec) throws RuntimeException {
        tec.setCompleteness(0d);
        SessionControllerGetter.getMessagingBean().sendForecastEventNotifications();
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
