/*
 * Copyright (c) 2021 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic;

import java.util.Set;
import jakarta.enterprise.inject.Vetoed;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Responsible for adding REST resources
 * @copyright 2016-2021 {@link http://www.nibio.no NIBIO}
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Vetoed
@ApplicationPath("/rest/")
public class VIPSLogicApplication extends Application
{
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        //addRestResourceClasses(resources);
        addRestResourceClassesManually(resources);
        return resources;
    }

    /**
     * Handmade because NetBeans doesn't discover the services that are implementations
     * of service interfaces. And it adds the interfaces it finds, which do not work
     * @param resources 
     */
    private void addRestResourceClassesManually(Set<Class<?>> resources) {
        resources.add(no.nibio.vips.logic.service.LogicService.class);
        resources.add(no.nibio.vips.logic.organism.OrganismResource.class);
        resources.add(no.nibio.vips.logic.organization.OrganizationResource.class);
        resources.add(no.nibio.vips.logic.message.MessageResource.class);
        resources.add(no.nibio.vips.logic.message.MessageTagResource.class);
        resources.add(no.nibio.vips.logic.user.UserResource.class);
        resources.add(no.nibio.vips.logic.user.EvaluatePasswordResource.class);
        resources.add(no.nibio.vips.logic.poi.WeatherStationKmlResource.class);
        resources.add(no.nibio.vips.logic.poi.PoiKmlResource.class);
        resources.add(no.nibio.vips.logic.forecast.ForecastResultResource.class);
        resources.add(no.nibio.vips.logic.forecast.ForecastConfigurationResource.class);
        resources.add(no.nibio.vips.logic.forecast.ForecastResource.class);
        resources.add(no.nibio.vips.logic.service.POIService.class);
        resources.add(no.nibio.vips.logic.service.AuthenticationService.class);
        resources.add(no.nibio.vips.logic.service.VIPSMobileService.class);
        resources.add(no.nibio.vips.logic.modules.barleynetblotch.BarleyNetBlotchModelService.class);
        resources.add(no.nibio.vips.logic.modules.roughage.RoughageService.class);
        resources.add(no.nibio.vips.logic.modules.barkbeetle.BarkbeetleService.class);
        resources.add(no.nibio.vips.observationdata.ObservationDataService.class);
        resources.add(no.nibio.vips.logic.messaging.sms.SMSHandlingService.class);
        resources.add(no.nibio.vips.logic.modules.applefruitmoth.AppleFruitMothService.class);
        resources.add(no.nibio.vips.logic.service.ObservationService.class);
        resources.add(no.nibio.vips.logic.service.ObservationTimeSeriesService.class);
        resources.add(no.nibio.vips.logic.service.ModelFormService.class);
        resources.add(no.nibio.vips.logic.service.JacksonConfig.class);
        resources.add(no.nibio.vips.logic.entity.helpers.KmlMessageBodyWriter.class);
        //resources.add(no.nibio.vips.logic.service.JSONBConfig.class);
        //resources.add(no.nibio.vips.coremanager.service.ManagerResourceImpl.class);
    }
    
}