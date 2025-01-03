/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import jakarta.ejb.EJB;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.logic.controller.session.ObservationBean;
import no.nibio.vips.logic.controller.session.OrganismBean;
import no.nibio.vips.logic.controller.session.SessionControllerGetter;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.Organism;
import no.nibio.vips.logic.entity.PointOfInterest;
import no.nibio.vips.logic.scheduling.model.ModelRunPreprocessor;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.observation.Observation;
import no.nibio.vips.util.WeatherUtil;

/**
 * @copyright 2016-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class PsilaRosaeObservationModelPreprocessor extends ModelRunPreprocessor{

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

            
            config.setConfigParameter("startDateCalculation", format.format(configuration.getDateStart()));
            WeatherUtil wUtil = new WeatherUtil();
            Date endDateCalculation = wUtil.normalizeToExactDate(SystemTime.getSystemTime(),timeZone);
            config.setConfigParameter("endDateCalculation", format.format(endDateCalculation));

            Organism organism = SessionControllerGetter.getOrganismBean().getOrganismByLatinName("Psila rosae");
            List<Observation> pestObservations = SessionControllerGetter.getObservationBean().getObservations(organism.getOrganismId(), location.getPointOfInterestId(),configuration.getDateStart(),SystemTime.getSystemTime());

            config.setConfigParameter("pestObservations", pestObservations);
            config.setModelId(this.getModelId());
        }
        catch(NullPointerException ex)
        {
            throw new PreprocessorException(ex.getMessage());
        }
        return config;
    }

    @Override
    public String getModelId() {
        return "PSILAROBSE";
    }

}
