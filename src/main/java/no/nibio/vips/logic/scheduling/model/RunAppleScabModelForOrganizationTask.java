/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.scheduling.model;

import no.nibio.vips.logic.scheduling.VipsLogicTask;
import no.nibio.vips.logic.scheduling.VipsLogicTask.TaskExecutionContext;

/**
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class RunAppleScabModelForOrganizationTask extends VipsLogicTask {
    
    public Integer organizationId;
    
    public RunAppleScabModelForOrganizationTask(Integer organizationId){
        this.organizationId = organizationId;
    }

    @Override
    public void execute(TaskExecutionContext tec) throws RuntimeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getConfigFormDefinition(String language) {
        return "{\"fields\":[]}";
    }

}
