/*
 * Copyright (c) 2026 NIBIO <http://www.nibio.no/>. 
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
package no.nibio.vips.logic.scheduling.model.preprocessor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;

/**
 * Gathers necessary data to run the RIMPROXVEN model, and validates that the
 * data is sufficient to run the model.
 * Also responsible for providing forecast configuration form with the correct
 * input parameters
 * 
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class RimProxyVenturiaModelPreprocessor extends ModelRunPreprocessor {
    private static Logger LOGGER = LoggerFactory.getLogger(RimProxyVenturiaModelPreprocessor.class);

    public final static String STATION_ID_PROPERTY = "RIMPROXVEN_STATION_ID";

    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException {
        LOGGER.debug("RimProxyVenturiaModelPreprocessor.getModelConfiguration() called");
        LOGGER.debug("ModelConfiguration=" + configuration.toString());
        Integer stationId;
        try {
            stationId = Integer.valueOf(configuration.getForecastModelConfigurationValue(RimProxyVenturiaModelPreprocessor.STATION_ID_PROPERTY));

        } catch (NumberFormatException ex) {
            throw new PreprocessorException("Could not parse station ID. Given string was: "
                    + configuration.getForecastModelConfigurationValue(RimProxyVenturiaModelPreprocessor.STATION_ID_PROPERTY));
        }

        // Get username and password for RIMPROXVEN from system properties
        String username = System.getProperty("no.nibio.vips.logic.RIMPROXVEN_USERNAME");
        String password = System.getProperty("no.nibio.vips.logic.RIMPROXVEN_PASSWORD");
        if (username == null || password == null) {
            throw new PreprocessorException(
                    "RIMPROXVEN username and password must be set in system properties (no.nibio.vips.logic.RIMPROXVEN_USERNAME and no.nibio.vips.logic.RIMPROXVEN_PASSWORD)");
        }

        // Create the complete model configuration object
        ModelConfiguration retVal = new ModelConfiguration();
        retVal.setModelId(this.getModelId());
        retVal.setConfigParameter("stationId", stationId);
        retVal.setConfigParameter("username", username);
        retVal.setConfigParameter("password", password);
        return retVal;
    }

    @Override
    public String getModelId() {
        return "RIMPROXVEN";
    }

}
