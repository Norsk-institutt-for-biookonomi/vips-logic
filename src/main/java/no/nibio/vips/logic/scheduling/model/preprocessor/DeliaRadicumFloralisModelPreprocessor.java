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

package no.nibio.vips.logic.scheduling.model.preprocessor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.Organism;
import no.nibio.vips.logic.entity.PointOfInterest;
import no.nibio.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.observation.Observation;
import no.nibio.vips.util.WeatherUtil;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class DeliaRadicumFloralisModelPreprocessor extends ModelRunPreprocessor{
    public final static String START_DATE_GROWTH = "START_DATE_GROWTH";

    @Override
    public ModelConfiguration getModelConfiguration(ForecastConfiguration configuration) throws PreprocessorException {
        ModelConfiguration config = null;
        try
        {
            PointOfInterest location = configuration.getLocationPointOfInterestId();
            config = new ModelConfiguration();
            String tzID = configuration.getVipsLogicUserId().getOrganizationId().getDefaultTimeZone();
            TimeZone timeZone = TimeZone.getTimeZone(tzID);
            config.setConfigParameter("timeZone", tzID);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setTimeZone(timeZone);

            Date startDateGrowth = format.parse(configuration.getForecastModelConfigurationValue(this.getModelId() + "_" + START_DATE_GROWTH));
            config.setConfigParameter("startDateGrowth", configuration.getForecastModelConfigurationValue(this.getModelId() + "_" + START_DATE_GROWTH));
            WeatherUtil wUtil = new WeatherUtil();
            Date endDateCalculation = wUtil.normalizeToExactDate(SystemTime.getSystemTime(),timeZone);
            config.setConfigParameter("endDateCalculation", format.format(endDateCalculation));

            Organism organism = SessionControllerGetter.getOrganismBean().getOrganismByLatinName("Delia floralis/radicum");
            List<Observation> pestObservations = SessionControllerGetter.getObservationBean().getObservations(organism.getOrganismId(), location.getPointOfInterestId(),startDateGrowth,SystemTime.getSystemTime());

            config.setConfigParameter("pestObservations", pestObservations);
            config.setModelId(this.getModelId());
        }
        catch(ParseException | NullPointerException ex)
        {
            throw new PreprocessorException(ex.getMessage());
        }
        return config;
    }

    @Override
    public String getModelId() {
        return "DELIARFOBS";
    }

}
