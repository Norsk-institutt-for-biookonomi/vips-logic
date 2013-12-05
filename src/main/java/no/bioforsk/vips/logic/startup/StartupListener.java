package no.bioforsk.vips.logic.startup;

import javax.servlet.ServletContextEvent;
import no.bioforsk.vips.logic.controller.session.SchedulingBean;
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
        // Testing startup of scheduling
        // Temporarily disabled
        //SessionControllerGetter.getSchedulingBean().startSystemScheduler();
        /*try
        {
            SessionControllerGetter.getSchedulingBean().testPreprocessor();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }*/
        
    }

    /**
     * Called when the web app is closed
     * @param sce 
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("VIPSLogic system shutting down");
        // Shutting down scheduler
        //SessionControllerGetter.getSchedulingBean().stopSystemScheduler();
    }
}
