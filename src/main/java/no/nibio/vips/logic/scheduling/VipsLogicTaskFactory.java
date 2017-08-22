/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.nibio.vips.logic.scheduling.tasks.DeleteAllExpiredUserUuidsTask;
import no.nibio.vips.logic.scheduling.tasks.RunAllForecastConfigurationsForOrganizationTask;
import no.nibio.vips.logic.scheduling.tasks.RunAllForecastConfigurationsTask;
import no.nibio.vips.logic.scheduling.tasks.SendForecastEventNotificationsTask;
import no.nibio.vips.logic.scheduling.tasks.UpdateForecastResultCacheTableTask;
import no.nibio.vips.logic.scheduling.tasks.UpdateForecastSummaryTableTask;
import no.nibio.vips.logic.scheduling.tasks.UpdateModelInformationTask;

/**
 * Provides the requested VipsLogicTask
 * @copyright 2013-2017 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class VipsLogicTaskFactory {

    public static final int RUN_ALL_FORECAST_CONFIGURATIONS_TASK = 1;
    public static final int UPDATE_MODEL_INFORMATION_TASK = 2;
    public static final int UPDATE_FORECAST_RESULT_CACHE_TABLE_TASK = 3;
    public static final int UPDATE_FORECAST_SUMMARY_TABLE_TASK = 4;
    public static final int DELETE_ALL_EXPIRED_UUIDS_TASK = 5;
    public static final int SEND_FORECAST_EVENT_NOTIFICATIONS_TASK = 6;
    public static final int RUN_ALL_FORECAST_CONFIGURATIONS_FOR_ORGANIZATION_TASK = 7;
    
    private final static int[] ALL_TASK_IDS = {1,2,3,4,5,6,7};
    
    private final static int[] ORGANIZATION_ADMIN_TASK_IDS = {7};
    
    private static List<VipsLogicTask> allTasksList;
    private static List<VipsLogicTask> organizationAdminTasksList;
    private static Map<String,VipsLogicTask> allTasksMap;
    
    /**
     * 
     * @param factoryId
     * @return the requested task
     */
    public static VipsLogicTask createVipsLogicTask(Integer factoryId)
    {
        VipsLogicTask retVal;
        switch(factoryId)
        {
            case RUN_ALL_FORECAST_CONFIGURATIONS_TASK:
                retVal =  new RunAllForecastConfigurationsTask();
                break;
            case UPDATE_MODEL_INFORMATION_TASK:
                retVal =  new UpdateModelInformationTask();
                break;
            case UPDATE_FORECAST_RESULT_CACHE_TABLE_TASK:
                retVal =  new UpdateForecastResultCacheTableTask();
                break;
            case UPDATE_FORECAST_SUMMARY_TABLE_TASK:
                retVal =  new UpdateForecastSummaryTableTask();
                break;
            case DELETE_ALL_EXPIRED_UUIDS_TASK:
                retVal =  new DeleteAllExpiredUserUuidsTask();
                break;
            case SEND_FORECAST_EVENT_NOTIFICATIONS_TASK:
                retVal =  new SendForecastEventNotificationsTask();
                break;
            case RUN_ALL_FORECAST_CONFIGURATIONS_FOR_ORGANIZATION_TASK:
                retVal =  new RunAllForecastConfigurationsForOrganizationTask();
                break;
            default:
                return null;
        }
        retVal.setFactoryId(factoryId);
        return retVal;
    }
    
    public static VipsLogicTask createVipsLogicTask(Integer vipsLogicTaskId, Map configuration)
    {
        VipsLogicTask task = createVipsLogicTask(vipsLogicTaskId);
        task.setConfiguration(configuration);
        return task;
    }
    
    public static List<VipsLogicTask> getAllVipsLogicTasks()
    {
        if(allTasksList == null)
        {
            allTasksList = new ArrayList<>();
            for(int i:ALL_TASK_IDS)
            {
                allTasksList.add(createVipsLogicTask(i));
            }
        }
        return allTasksList;
    }
    
    public static Map<String, VipsLogicTask> getAllVipsLogicTasksMap()
    {
        if(allTasksMap == null)
        {
            allTasksMap = new HashMap<>();
            for(int i:ALL_TASK_IDS)
            {
                allTasksMap.put(String.valueOf(i), createVipsLogicTask(i));
            }
        }
        return allTasksMap;
    }

    public static List<VipsLogicTask> getOrganizationAdminVipsLogicTasks() {
        if(organizationAdminTasksList == null)
        {
            organizationAdminTasksList = new ArrayList<>();
            for(int i:ORGANIZATION_ADMIN_TASK_IDS)
            {
                organizationAdminTasksList.add(createVipsLogicTask(i));
            }
        }
        return organizationAdminTasksList;
    }
}
