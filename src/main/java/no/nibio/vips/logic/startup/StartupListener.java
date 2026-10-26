/*
 * Copyright (c) 2022 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.startup;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import no.nibio.vips.logic.controller.session.SchedulingBean;
import no.nibio.vips.logic.scheduling.VipsLogicTaskFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * This class is called when the application starts on Quarkus
 * (replaces ServletContextListener on WildFly).
 * 
 * @copyright 2013-2022 {@link http://www.nibio.no NIBIO}
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@ApplicationScoped
public class StartupListener {

    @Inject
    SchedulingBean schedulingBean;

    @ConfigProperty(name = "vipslogic.scheduling.start-on-boot")
    boolean startSchedulingOnBoot;

    @ConfigProperty(name = "vipslogic.scheduling.smoke-launch-expired-uuids-task", defaultValue = "false")
    boolean smokeLaunchExpiredUuidsTask;
    
    void onStart(@Observes StartupEvent event) {
        System.out.println("VIPSLogic system initializing");
        
        schedulingBean.initSystemScheduler();
        if(startSchedulingOnBoot)
        {
            System.out.println("VIPSLogic starting scheduling services");
            schedulingBean.startSystemScheduler();
        }
        else
        {
            System.out.println("VIPSLogic scheduling services configured NOT to start.");
        }
        if (smokeLaunchExpiredUuidsTask) {
            System.out.println("VIPSLogic smoke test: launching DeleteAllExpiredUserUuidsTask");
            java.util.concurrent.CompletableFuture.runAsync(() ->
                    schedulingBean.launchSystemSchedulerTask(
                            VipsLogicTaskFactory.createVipsLogicTask(VipsLogicTaskFactory.DELETE_ALL_EXPIRED_UUIDS_TASK)));
        }
    }

    void onStop(@Observes ShutdownEvent event) {
        System.out.println("VIPSLogic system shutting down");
        schedulingBean.stopSystemScheduler();
    }
}
