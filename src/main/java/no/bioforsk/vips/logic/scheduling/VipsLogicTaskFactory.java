/*
 * Copyright (c) 2013 Bioforsk <http://www.bioforsk.no/>. 
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
import java.util.List;
import java.util.Map;
import no.bioforsk.vips.logic.scheduling.tasks.RunAllForecastConfigurationsTask;

/**
 * Provides the requested VipsLogicTask
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class VipsLogicTaskFactory {

    public static final int RUN_ALL_FORECAST_CONFIGURATIONS_TASK = 1;
    
    private final static int[] ALL_TASK_IDS = {1};
    
    /**
     * 
     * @param factoryId
     * @return 
     */
    public static VipsLogicTask createVipsLogicTask(Integer factoryId)
    {
        VipsLogicTask retVal;
        switch(factoryId)
        {
            case RUN_ALL_FORECAST_CONFIGURATIONS_TASK:
                retVal =  new RunAllForecastConfigurationsTask();
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
        List<VipsLogicTask> allTasks = new ArrayList<>();
        for(int i:ALL_TASK_IDS)
        {
            allTasks.add(createVipsLogicTask(i));
        }
        return allTasks;
    }
}
