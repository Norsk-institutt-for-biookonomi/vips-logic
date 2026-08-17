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

import jakarta.ejb.EJB;
import jakarta.servlet.ServletContextEvent;
import no.nibio.vips.logic.controller.session.SchedulingBean;

/**
 * This class is created and method contextInitialized called when the application
 * is deployed (either at startup of application container or when redeploying the app)
 * 
 * @copyright 2013-2022 {@link http://www.nibio.no NIBIO}
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class StartupListener implements jakarta.servlet.ServletContextListener{

    @EJB
    SchedulingBean schedulingBean;
    
    /**
     * Called when the application
     * is deployed (either at startup of application container or when redeploying the app)
     * @param sce 
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("VIPSLogic system initializing");
        // Flyway runs via Quarkus (quarkus.flyway.* in application.properties)
        
        schedulingBean.initSystemScheduler();
        if(System.getProperty("no.nibio.vips.logic.START_SCHEDULING_ON_BOOT").equals("true"))
        {
            System.out.println("VIPSLogic starting scheduling services");
            schedulingBean.startSystemScheduler();
        }
        else
        {
            System.out.println("VIPSLogic scheduling services configured NOT to start.");
        }
        
    }
    

    /**
     * Called when the web app is closed
     * @param sce 
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("VIPSLogic system shutting down");
        // Shutting down scheduler
        schedulingBean.stopSystemScheduler();
    }
}
