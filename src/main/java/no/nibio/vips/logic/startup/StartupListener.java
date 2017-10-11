/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.startup;

import javax.annotation.Resource;
import javax.servlet.ServletContextEvent;
import javax.sql.DataSource;
import no.nibio.vips.logic.util.SessionControllerGetter;
import org.flywaydb.core.Flyway;

/**
 * This class is created and method contextInitialized called when the application
 * is deployed (either at startup of application container or when redeploying the app)
 * 
 * @copyright 2013 {@link http://www.nibio.no NIBIO}
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class StartupListener implements javax.servlet.ServletContextListener{

    @Resource(lookup="java:/jboss/datasources/vipslogic")
    DataSource vipslogicDS;
    /**
     * Called when the application
     * is deployed (either at startup of application container or when redeploying the app)
     * @param sce 
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("VIPSLogic system initializing");
        System.out.println("VIPSLogic checking for database migrations");
        // Migrate DB on startup using flywaydb.org
        Flyway flyway = new Flyway();
        flyway.setDataSource(vipslogicDS);
        flyway.migrate();
        
        SessionControllerGetter.getSchedulingBean().initSystemScheduler();
        if(System.getProperty("no.nibio.vips.logic.START_SCHEDULING_ON_BOOT").equals("true"))
        {
            System.out.println("VIPSLogic starting scheduling services");
            SessionControllerGetter.getSchedulingBean().startSystemScheduler();
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
        SessionControllerGetter.getSchedulingBean().stopSystemScheduler();
    }
}
