/*
 * Copyright (c) 2019 NIBIO <http://www.nibio.no/>. 
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
import java.util.Map;
import javax.ejb.EJB;
import no.nibio.vips.i18n.I18nImpl;
import no.nibio.vips.logic.controller.session.ForecastBean;
import no.nibio.vips.logic.controller.session.SessionControllerGetter;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ModelInformation;
import no.nibio.vips.logic.scheduling.SchedulingUtil;
import no.nibio.vips.logic.scheduling.VipsLogicTask;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.logic.util.RunModelException;
import no.nibio.web.forms.FormField;

/**
 * @copyright 2013-2018 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class RunForecastConfigurationsByIdTask extends VipsLogicTask{

    
    private I18nImpl i18n;
    //private boolean DEBUG=true;
    @Override
    public void execute(TaskExecutionContext tec) throws RuntimeException {
        String[] forecastConfigurationIds;
        if(this.getConfiguration() != null && this.getConfiguration().get("forecastConfigurationIds") != null)
        { 
            forecastConfigurationIds = ((String) (this.getConfiguration().get("forecastConfigurationIds")[0])).split(",");
        }
        else
        {
            tec.setCompleteness(1.0);
            tec.setStatusMessage("No forecast configurations were specified");
            return;
        }
        
        String errorMessage = "";
        tec.setCompleteness(0d);
        Integer numberOfAttemptedForecastConfigurations = 0;
        Integer numberOfCompletedForecastConfigurations = 0;
        Boolean noForecastConfigurationsFound = forecastConfigurationIds.length == 0;
        Map<String, ModelInformation> modelInformationMap = SessionControllerGetter.getForecastBean().getIndexedBatchableModelInformation();
        for(String forecastConfigurationId:forecastConfigurationIds)
        {
            noForecastConfigurationsFound = false;
            ForecastConfiguration forecastConfiguration = SessionControllerGetter.getForecastBean().getForecastConfiguration(Long.valueOf(forecastConfigurationId));
            try 
            {
                numberOfAttemptedForecastConfigurations++;
                //System.out.println("Running forecast #" + forecastConfiguration.getForecastConfigurationId());
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
                errorMessage += 
                    SchedulingUtil.createSchedulingMessageHTML(
                        "Error with forecast #" + forecastConfiguration.getForecastConfigurationId()  + " (" + forecastConfiguration.getLocationPointOfInterestId().getName() + " - " + modelInformationMap.get(forecastConfiguration.getModelId()).getDefaultName() + ")", 
                        ex.getMessage(), 
                        SchedulingUtil.MESSAGE_STATUS_DANGER)
                ;
                //System.out.println("###########################   Error caught: " + errorMessage);
                //System.out.println("numberOfCompletedForecastConfigurations=" + numberOfCompletedForecastConfigurations);
                //System.out.println("totalNumberofForecastConfigurations=" + totalNumberofForecastConfigurations);
                //continue;
            }
            double completeness = (double) numberOfCompletedForecastConfigurations/forecastConfigurationIds.length;
            tec.setCompleteness(completeness);
        }
        
        if(noForecastConfigurationsFound)
        {
               tec.setCompleteness(1.0);
               tec.setStatusMessage("No current forecast configurations were found");
        }
        
        //System.out.println("Total completeness=" + tec.getTaskExecutor().getCompleteness());
        
        if(tec.getTaskExecutor().getCompleteness() != 1.0)
        {
            //System.out.println("Error detected, RuntimeException thrown just after this");
            tec.setStatusMessage(errorMessage);
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
                .append("           \"name\":\"forecastConfigurationIds\",")
                .append("           \"dataType\":\"").append(FormField.DATA_TYPE_STRING).append("\",")
                .append("           \"fieldType\":\"").append(FormField.FIELD_TYPE_INPUT).append("\",")
                .append("           \"nullValue\":\"\",")
                .append("           \"required\":true")
                .append("       }")
                .append("   ]")
                .append("}");


        return retVal.toString();
    }

    
}
