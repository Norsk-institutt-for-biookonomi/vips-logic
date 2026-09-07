/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * <https://www.gnu.org/licenses/>.
 *
 */

package no.nibio.vips.logic.scheduling.tasks;

import no.nibio.vips.logic.scheduling.VipsLogicTask.TaskExecutionContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nibio.vips.i18n.I18nImpl;
import no.nibio.vips.logic.controller.session.SessionControllerGetter;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ModelInformation;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.PointOfInterest;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.scheduling.SchedulingUtil;
import no.nibio.vips.logic.scheduling.VipsLogicTask;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.logic.util.RunModelException;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.web.forms.FormField;

/**
 * @copyright 2013-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class RunAllForecastConfigurationsTask extends VipsLogicTask {

    private static Logger LOGGER = LoggerFactory.getLogger(RunAllForecastConfigurationsTask.class);

    private I18nImpl i18n;

    // private boolean DEBUG=true;
    @Override
    public void execute(TaskExecutionContext tec) throws RuntimeException {
        Integer weatherStationPointOfInterestId = null;
        if (this.getConfiguration() != null && this.getConfiguration().get("weatherStationPointOfInterestId") != null) {
            weatherStationPointOfInterestId =
                    Integer.valueOf(this.getConfiguration().get("weatherStationPointOfInterestId")[0]);
        }

        // Is organization configured? In that case, pick only one. Otherwhise, pick em all
        // The config could come from a form
        if (this.getConfiguration() != null && this.getConfiguration().get("organizationId") != null) {
            this.setOrganization(SessionControllerGetter.getUserBean()
                    .getOrganization(Integer.valueOf(this.getConfiguration().get("organizationId")[0])));
        }

        List<Organization> organizations = new ArrayList<>();
        if (this.getOrganization() != null) {
            organizations.add(this.getOrganization());
        } else {
            organizations = SessionControllerGetter.getUserBean().getOrganizations();
        }
        tec.setCompleteness(0d);
        int totalNumberofForecastConfigurations = 0;
        int numberOfCompletedForecastConfigurations = 0;
        StringBuilder errorMessage = new StringBuilder();
        // Get all organizations, loop, get all current forecasts, loop, run models, store results
        boolean noForecastConfigurationsFound = true;

        Map<String, ModelInformation> modelInformationMap =
                SessionControllerGetter.getForecastBean().getIndexedBatchableModelInformation();

        for (Organization organization : organizations) {
            List<ForecastConfiguration> currentForecastConfigurations = SessionControllerGetter.getForecastBean()
                    .getForecastConfigurationsValidAtTime(organization, SystemTime.getSystemTime());
            // System.out.println("Current forecasts for " + organization.getOrganizationName() + ":" +
            // currentForecastConfigurations.size());
            if (currentForecastConfigurations != null && !currentForecastConfigurations.isEmpty()) {
                noForecastConfigurationsFound = false;
                for (ForecastConfiguration forecastConfiguration : currentForecastConfigurations) {
                    if (forecastConfiguration.getUseGridWeatherData()
                            && forecastConfiguration.getWeatherStationPointOfInterestId() == null) {
                        errorMessage.append(
                                SchedulingUtil.createSchedulingMessageHTML(
                                        "Error with forecast #" + forecastConfiguration.getForecastConfigurationId()
                                                + " (" + forecastConfiguration.getLocationPointOfInterestId().getName()
                                                + " - "
                                                + modelInformationMap.get(forecastConfiguration.getModelId())
                                                        .getDefaultName()
                                                + ")",
                                        "The forecast is configured to use gridded weather data, but the organization "
                                                + forecastConfiguration.getVipsLogicUserId().getOrganizationId()
                                                        .getOrganizationName()
                                                + " has not set its gridded weather data source. Please contact the system administrator.",
                                        SchedulingUtil.MESSAGE_STATUS_WARNING));
                        totalNumberofForecastConfigurations++;
                    } else if (weatherStationPointOfInterestId == null
                            || weatherStationPointOfInterestId <= 0
                            || (forecastConfiguration.getWeatherStationPointOfInterestId() != null
                                    && forecastConfiguration.getWeatherStationPointOfInterestId()
                                            .getPointOfInterestId() != null
                                    && forecastConfiguration.getWeatherStationPointOfInterestId().getPointOfInterestId()
                                            .equals(weatherStationPointOfInterestId)))

                    {
                        try {
                            totalNumberofForecastConfigurations++;
                            LOGGER.debug("Running forecast #" + forecastConfiguration.getForecastConfigurationId());
                            SessionControllerGetter.getForecastBean().runForecast(forecastConfiguration);
                            numberOfCompletedForecastConfigurations++;
                        } catch (PreprocessorException | RunModelException ex) {
                            errorMessage
                                    .append(
                                            SchedulingUtil.createSchedulingMessageHTML(
                                                    "Error with forecast #"
                                                            + forecastConfiguration.getForecastConfigurationId() + " ("
                                                            + forecastConfiguration.getLocationPointOfInterestId()
                                                                    .getName()
                                                            + " - "
                                                            + modelInformationMap
                                                                    .get(forecastConfiguration.getModelId())
                                                                    .getDefaultName()
                                                            + ")",
                                                    ex.getMessage(),
                                                    SchedulingUtil.MESSAGE_STATUS_DANGER));
                        }

                    }
                    if (totalNumberofForecastConfigurations > 0) {
                        noForecastConfigurationsFound = false;
                        tec.setCompleteness(Double.valueOf(numberOfCompletedForecastConfigurations)
                                / Double.valueOf(totalNumberofForecastConfigurations));
                    } else {
                        noForecastConfigurationsFound = true;

                    }
                }
            }

        }

        if (noForecastConfigurationsFound) {
            tec.setCompleteness(1.0);
            tec.setStatusMessage("No current forecast configurations were found");
        }


        if (tec.getTaskExecutor().getCompleteness() != 1.0 || !errorMessage.isEmpty()) {
            // System.out.println("Error detected, RuntimeException thrown just after this");
            tec.setStatusMessage(errorMessage.toString());
            throw new RuntimeException();
        }
    }

    @Override
    public boolean supportsStatusTracking() {
        return true;
    }

    @Override
    public boolean supportsCompletenessTracking() {
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
                .append("           \"name\":\"weatherStationPointOfInterestId\",")
                .append("           \"dataType\":\"").append(FormField.DATA_TYPE_INTEGER).append("\",")
                .append("           \"fieldType\":\"").append(FormField.FIELD_TYPE_SELECT_SINGLE).append("\",")
                .append("           \"nullValue\":\"-1\",")
                .append("           \"required\":false,")
                .append("           \"options\": [");


        retVal.append("               {\"value\":\"")
                .append("-1")
                .append("\", \"label\":\"")
                .append(this.getI18nImpl().getText("pleaseSelect", language)).append(" ")
                .append(this.getI18nImpl().getText("weatherStationPointOfInterestId", language).toLowerCase())
                .append("\",\"selected\":")
                .append("false")
                .append("}\n");
        List<PointOfInterestWeatherStation> stations =
                SessionControllerGetter.getPointOfInterestBean().getAllWeatherStations();
        Collections.sort(stations);
        for (PointOfInterest station : stations) {
            retVal.append("               ,{\"value\":\"")
                    .append(station.getPointOfInterestId())
                    .append("\", \"label\":\"")
                    .append(station.getName())
                    .append("\",\"selected\":")
                    .append("false")
                    .append("}\n");

        }
        retVal.append("           ]")
                .append("       }");
        retVal.append("       ,{")
                .append("           \"name\":\"organizationId\",")
                .append("           \"dataType\":\"").append(FormField.DATA_TYPE_INTEGER).append("\",")
                .append("           \"fieldType\":\"").append(FormField.FIELD_TYPE_SELECT_SINGLE).append("\",")
                .append("           \"nullValue\":\"-1\",")
                .append("           \"required\":false,")
                .append("           \"options\": [");


        retVal.append("               {\"value\":\"")
                .append("-1")
                .append("\", \"label\":\"")
                .append(this.getI18nImpl().getText("pleaseSelect", language)).append(" ")
                .append(this.getI18nImpl().getText("organizationId", language).toLowerCase())
                .append("\",\"selected\":")
                .append("false")
                .append("}\n");
        List<Organization> organizations = SessionControllerGetter.getUserBean().getOrganizations();
        // Collections.sort(organizations);
        for (Organization organization : organizations) {
            retVal.append("               ,{\"value\":\"")
                    .append(organization.getOrganizationId())
                    .append("\", \"label\":\"")
                    .append(organization.getOrganizationName())
                    .append("\",\"selected\":")
                    .append("false")
                    .append("}\n");

        }
        retVal.append("           ]")
                .append("       }")
                .append("   ]")
                .append("}");


        return retVal.toString();
    }

    private I18nImpl getI18nImpl() {
        if (this.i18n == null) {
            this.i18n = new I18nImpl("no.nibio.vips.logic.i18n.vipslogictexts");
        }
        return this.i18n;
    }

}
