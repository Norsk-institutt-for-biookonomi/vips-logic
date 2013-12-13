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

package no.bioforsk.vips.logic.scheduling.tasks;

import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;
import java.util.List;
import no.bioforsk.vips.coremanager.service.ManagerResource;
import no.bioforsk.vips.logic.entity.ForecastConfiguration;
import no.bioforsk.vips.logic.entity.Organization;
import no.bioforsk.vips.logic.util.SessionControllerGetter;
import no.bioforsk.vips.logic.util.SystemTime;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class RunAllForecastConfigurationsTask extends Task{

    @Override
    public void execute(TaskExecutionContext tec) throws RuntimeException {
        System.out.println("Starting " + this.getClass().getName());
        // Get all organizations, loop, get all current forecasts, loop, run models, store results
        for(Organization organization : SessionControllerGetter.getUserBean().getOrganizations())
        {
            List<ForecastConfiguration> currentForecastConfigurations = SessionControllerGetter.getForecastBean().getForecastConfigurationsValidAtTime(organization, SystemTime.getSystemTime());
            for(ForecastConfiguration forecastConfiguration:currentForecastConfigurations)
            {
                SessionControllerGetter.getForecastBean().runForecast(forecastConfiguration);
            }
        }
    }

}
