/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with VIPSLogic.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.startup;

import javax.servlet.ServletContextEvent;
import no.bioforsk.vips.logic.util.SessionControllerGetter;

/**
 * This class is created and method contextInitialized called when the application
 * is deployed (either at startup of application container or when redeploying the app)
 * 
 * @copyright 2013 {@link http://www.bioforsk.no Bioforsk}
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class StartupListener implements javax.servlet.ServletContextListener{

    
    /**
     * Called when the application
     * is deployed (either at startup of application container or when redeploying the app)
     * @param sce 
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("VIPSLogic system initializing");
        SessionControllerGetter.getSchedulingBean().initSystemScheduler();
        if(System.getProperty("no.bioforsk.vips.logic.START_SCHEDULING_ON_BOOT").equals("true"))
        {
            SessionControllerGetter.getSchedulingBean().startSystemScheduler();
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
        SessionControllerGetter.getSchedulingBean().stopSystemScheduler();
    }
}
