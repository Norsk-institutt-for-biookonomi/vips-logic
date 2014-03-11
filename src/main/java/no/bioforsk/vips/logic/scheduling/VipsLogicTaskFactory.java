/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import no.bioforsk.vips.logic.scheduling.tasks.RunAllForecastConfigurationsTask;
import no.bioforsk.vips.logic.scheduling.tasks.UpdateModelInformationTask;

/**
 * Provides the requested VipsLogicTask
 * @copyright 2013-2014 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class VipsLogicTaskFactory {

    public static final int RUN_ALL_FORECAST_CONFIGURATIONS_TASK = 1;
    public static final int UPDATE_MODEL_INFORMATION_TASK = 2;
    
    private final static int[] ALL_TASK_IDS = {1,2};
    
    private static List<VipsLogicTask> allTasksList;
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
}
