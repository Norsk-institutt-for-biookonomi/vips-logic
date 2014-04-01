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

import it.sauronsoftware.cron4j.TaskExecutionContext;
import java.util.Collections;
import java.util.List;
import no.bioforsk.vips.i18n.I18nImpl;
import no.bioforsk.vips.logic.entity.ForecastConfiguration;
import no.bioforsk.vips.logic.entity.Organization;
import no.bioforsk.vips.logic.entity.PointOfInterest;
import no.bioforsk.vips.logic.entity.PointOfInterestWeatherStation;
import no.bioforsk.vips.logic.scheduling.VipsLogicTask;
import no.bioforsk.vips.logic.scheduling.VipsLogicTaskFactory;
import no.bioforsk.vips.logic.scheduling.model.PreprocessorException;
import no.bioforsk.vips.logic.util.RunModelException;
import no.bioforsk.vips.logic.util.SessionControllerGetter;
import no.bioforsk.vips.logic.util.SystemTime;
import no.bioforsk.web.forms.FormField;

/**
 * @copyright 2013-2014 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class RunAllForecastConfigurationsTask extends VipsLogicTask{
    private I18nImpl i18n;
    //private boolean DEBUG=true;
    @Override
    public void execute(TaskExecutionContext tec) throws RuntimeException {
        Integer weatherStationPointOfInterestId = null;
        if(this.getConfiguration() != null && this.getConfiguration().get("weatherStationPointOfInterestId") != null)
        { 
            weatherStationPointOfInterestId =  Integer.valueOf(this.getConfiguration().get("weatherStationPointOfInterestId")[0]);
        }
        tec.setCompleteness(0d);
        int totalNumberofForecastConfigurations = 0;
        int numberOfCompletedForecastConfigurations = 0;
        StringBuilder errorMessage = new StringBuilder();
        // Get all organizations, loop, get all current forecasts, loop, run models, store results
        for(Organization organization : SessionControllerGetter.getUserBean().getOrganizations())
        {
            List<ForecastConfiguration> currentForecastConfigurations = SessionControllerGetter.getForecastBean().getForecastConfigurationsValidAtTime(organization, SystemTime.getSystemTime());
            
            for(ForecastConfiguration forecastConfiguration:currentForecastConfigurations)
            {
                if(
                        weatherStationPointOfInterestId == null
                        || weatherStationPointOfInterestId <= 0
                        || forecastConfiguration.getWeatherStationPointOfInterestId().getPointOfInterestId().equals(weatherStationPointOfInterestId))
                {
                    try 
                    {
                        totalNumberofForecastConfigurations++;
                        SessionControllerGetter.getForecastBean().runForecast(forecastConfiguration);
                        /*
                        if(DEBUG && totalNumberofForecastConfigurations == 2)
                        {
                            throw new RunModelException("This is a test!!!");
                        }*/
                        numberOfCompletedForecastConfigurations++;
                        //System.out.println("All went well");
                    }
                    catch (PreprocessorException | RunModelException ex) 
                    {
                        errorMessage.append("Error with forecast #").append(forecastConfiguration.getForecastConfigurationId()).append(": ").append(ex.getMessage());
                        //System.out.println("Error caught");
                        continue;
                    }
                    
                }
                
                double completeness = (double) numberOfCompletedForecastConfigurations/totalNumberofForecastConfigurations;
                tec.setCompleteness(completeness);
            }
        }
        if(tec.getTaskExecutor().getCompleteness() != 1.0)
        {
            //System.out.println("Error detected");
            tec.setStatusMessage(errorMessage.toString());
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

    /**
     * 
     * @return Form definition
     */
    @Override
    public String getConfigFormDefinition(String language) {
        StringBuilder retVal = new StringBuilder()
                .append("{")
                .append("   \"fields\":[")
                .append("       {")
                .append("           \"name\":\"factoryId\",")
                .append("           \"dataType\":\"").append(FormField.DATA_TYPE_INTEGER).append("\",")
                .append("           \"fieldType\":\"").append(FormField.FIELD_TYPE_HIDDEN).append("\",")
                .append("           \"webValue\":[\"").append(VipsLogicTaskFactory.RUN_ALL_FORECAST_CONFIGURATIONS_TASK).append("\"]")
                .append("       },")
                .append("       {")
                .append("           \"name\":\"weatherStationPointOfInterestId\",")
                .append("           \"dataType\":\"").append(FormField.DATA_TYPE_INTEGER).append("\",")
                .append("           \"fieldType\":\"").append(FormField.FIELD_TYPE_SELECT_SINGLE).append("\",")
                .append("           \"nullValue\":\"-1\",")
                .append("           \"required\":false,")
                .append("           \"options\": [");

        
        retVal.append("               {\"value\":\"")
                    .append("-1")
                    .append("\", \"label\":\"")
                    .append(this.getI18nImpl().getText("pleaseSelect", language)).append(" ").append(this.getI18nImpl().getText("weatherStationPointOfInterestId", language).toLowerCase())
                    .append("\",\"selected\":")
                    .append("false")
                    .append("}\n");
        List<PointOfInterestWeatherStation> stations = SessionControllerGetter.getPointOfInterestBean().getAllWeatherStations();
        Collections.sort(stations);
        for(PointOfInterest station:stations)
        {
            retVal.append("               ,{\"value\":\"")
                    .append(station.getPointOfInterestId())
                    .append("\", \"label\":\"")
                    .append(station.getName())
                    .append("\",\"selected\":")
                    .append("false")
                    .append("}\n");

        }
        retVal  .append("           ]")
                .append("       }")
                .append("   ]")
                .append("}");


        return retVal.toString();
    }

    private I18nImpl getI18nImpl()
    {
        if(this.i18n == null)
        {
            this.i18n = new I18nImpl("no.bioforsk.vips.logic.i18n.vipslogictexts");
        }
        return this.i18n;
    }
    
}
