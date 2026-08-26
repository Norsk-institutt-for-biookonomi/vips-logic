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

package no.nibio.vips.logic.controller.session;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import io.quarkus.arc.Unremovable;
import no.nibio.vips.logic.messaging.MessagingBean;
import no.nibio.vips.logic.modules.barkbeetle.BarkbeetleBean;

/**
 * CDI bridge for code outside the container (scheduler tasks, preprocessors, static helpers).
 * Prefer {@code @Inject} in CDI-managed classes; use these static accessors only when injection is not possible.
 *
 * @copyright 2013-2025 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@ApplicationScoped
@Unremovable
public class SessionControllerGetter {

    private static <T> T getBean(Class<T> type) {
        return CDI.current().select(type).get();
    }

    public static SchedulingBean getSchedulingBean() {
        return getBean(SchedulingBean.class);
    }

    public static ForecastBean getForecastBean() {
        return getBean(ForecastBean.class);
    }

    public static PointOfInterestBean getPointOfInterestBean() {
        return getBean(PointOfInterestBean.class);
    }

    public static UserBean getUserBean() {
        return getBean(UserBean.class);
    }

    public static MessagingBean getMessagingBean() {
        return getBean(MessagingBean.class);
    }

    public static OrganismBean getOrganismBean() {
        return getBean(OrganismBean.class);
    }

    public static ObservationBean getObservationBean() {
        return getBean(ObservationBean.class);
    }

    public static BarkbeetleBean getBarkbeetleBean() {
        return getBean(BarkbeetleBean.class);
    }
}
