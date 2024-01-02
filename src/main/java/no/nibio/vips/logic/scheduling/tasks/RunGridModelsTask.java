/*
 * Copyright (c) 2018 NIBIO <http://www.nibio.no/>. 
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
import javax.ejb.EJB;
import no.nibio.vips.logic.controller.session.ForecastBean;
import no.nibio.vips.logic.controller.session.SessionControllerGetter;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.scheduling.SchedulingUtil;
import no.nibio.vips.logic.scheduling.VipsLogicTask;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.logic.util.RunModelException;

/**
 * @copyright 2018-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class RunGridModelsTask extends VipsLogicTask{
    
    @Override
    public String getConfigFormDefinition(String language) {
        return "{\"fields\":[]}";
    }

    @Override
    public void execute(TaskExecutionContext tec) throws RuntimeException {
        tec.setCompleteness(0.0);
        //ZymoseptoriaSimpleRiskGridModelPreprocessor pp = new ZymoseptoriaSimpleRiskGridModelPreprocessor();
        // TODO: Must find a forecast configuration id! -1000 !
        
        ForecastConfiguration fConfig = SessionControllerGetter.getForecastBean().getForecastConfiguration(-1000l);

        try
        {
        	SessionControllerGetter.getForecastBean().runForecast(fConfig);
            tec.setCompleteness(1.0);
        }
        catch(PreprocessorException | RunModelException ex)
        {
            tec.setCompleteness(0.0);
            tec.setStatusMessage(SchedulingUtil.createSchedulingMessageHTML(
                                            "Error with forecast #" + fConfig.getForecastConfigurationId(), 
                                            ex.getMessage(), 
                                            SchedulingUtil.MESSAGE_STATUS_DANGER
                                    )
            );
            
            ex.printStackTrace();
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
}
