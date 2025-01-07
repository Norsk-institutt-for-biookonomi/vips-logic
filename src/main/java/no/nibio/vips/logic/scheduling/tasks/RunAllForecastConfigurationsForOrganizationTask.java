/*
 * Copyright (c) 2017 NIBIO <http://www.nibio.no/>. 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package no.nibio.vips.logic.scheduling.tasks;

import it.sauronsoftware.cron4j.TaskExecutionContext;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import jakarta.ejb.EJB;
import no.nibio.vips.i18n.I18nImpl;
import no.nibio.vips.logic.controller.session.ForecastBean;
import no.nibio.vips.logic.controller.session.PointOfInterestBean;
import no.nibio.vips.logic.controller.session.SessionControllerGetter;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ModelInformation;
import no.nibio.vips.logic.entity.PointOfInterest;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.scheduling.SchedulingUtil;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.logic.util.RunModelException;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.web.forms.FormField;

/**
 * @copyright 2018 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class RunAllForecastConfigurationsForOrganizationTask extends RunAllForecastConfigurationsTask{
    
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
        boolean noForecastConfigurationsFound = true;
        Map<String, ModelInformation> modelInformationMap = SessionControllerGetter.getForecastBean().getIndexedBatchableModelInformation();
        
        List<ForecastConfiguration> currentForecastConfigurations = SessionControllerGetter.getForecastBean().getForecastConfigurationsValidAtTime(this.getOrganization(), SystemTime.getSystemTime());
        //System.out.println("Current forecasts for " + organization.getOrganizationName() + ":" + currentForecastConfigurations.size());
        if(currentForecastConfigurations != null && !currentForecastConfigurations.isEmpty())
        {
            noForecastConfigurationsFound = false;
            for(ForecastConfiguration forecastConfiguration:currentForecastConfigurations)
            {
                if(
                        weatherStationPointOfInterestId == null
                        || weatherStationPointOfInterestId <= 0
                        || (forecastConfiguration.getWeatherStationPointOfInterestId() != null && forecastConfiguration.getWeatherStationPointOfInterestId().getPointOfInterestId().equals(weatherStationPointOfInterestId))
                        )
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
                        errorMessage
                                .append(
                                    SchedulingUtil.createSchedulingMessageHTML(
                                        "Error with forecast #" + forecastConfiguration.getForecastConfigurationId() + "(" + forecastConfiguration.getLocationPointOfInterestId().getName() + " - " + modelInformationMap.get(forecastConfiguration.getModelId()).getDefaultName() + ")", 
                                        ex.getMessage(), 
                                        SchedulingUtil.MESSAGE_STATUS_DANGER)
                                );
                        //System.out.println("Error caught");
                        continue;
                    }

                }

                double completeness = (double) numberOfCompletedForecastConfigurations/totalNumberofForecastConfigurations;
                tec.setCompleteness(completeness);
            }
        }
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
        List<PointOfInterestWeatherStation> stations = SessionControllerGetter.getPointOfInterestBean().getWeatherstationsForOrganization(this.getOrganization(), Boolean.TRUE);
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
                .append("       }");
       
        
        retVal  .append("           ]")
                .append("       }");
                

        return retVal.toString();
    }

    private I18nImpl getI18nImpl()
    {
        if(this.i18n == null)
        {
            this.i18n = new I18nImpl("no.nibio.vips.logic.i18n.vipslogictexts");
        }
        return this.i18n;
    }
    
}
