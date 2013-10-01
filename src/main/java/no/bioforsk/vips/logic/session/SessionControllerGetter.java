package no.bioforsk.vips.logic.session;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class SessionControllerGetter {
    
    private static final String JNDI_PATH = "java:global/VIPSLogic-1.0-SNAPSHOT/";

    public static PointOfInterestBean getPointOfInterestBean()
    {
        try
        {
            InitialContext ic = new InitialContext();
            PointOfInterestBean retVal = (PointOfInterestBean) ic.lookup(SessionControllerGetter.getJndiPath(PointOfInterestBean.class));

            return retVal;
        }catch(NamingException ne)
        {
            System.out.println("Could not find " + PointOfInterestBean.class.getSimpleName());
            return null;
        }
    }
    
    public static UserBean getUserBean()
    {
        try
        {
            InitialContext ic = new InitialContext();
            UserBean retVal = (UserBean) ic.lookup(SessionControllerGetter.getJndiPath(UserBean.class));

            return retVal;
        }catch(NamingException ne)
        {
            System.out.println("Could not find " + UserBean.class.getSimpleName());
            return null;
        }
    }
    
    public static SchedulingBean getSchedulingBean()
    {
        try
        {
            InitialContext ic = new InitialContext();
            SchedulingBean retVal = (SchedulingBean) ic.lookup(SessionControllerGetter.getJndiPath(SchedulingBean.class));

            return retVal;
        }catch(NamingException ne)
        {
            System.out.println("Could not find " + SchedulingBean.class.getSimpleName());
            return null;
        }
    }
    
    private static String getJndiPath(Class obj)
    {
        String retVal = SessionControllerGetter.JNDI_PATH + obj.getSimpleName();
        //System.out.println("JNDI-path=" + retVal);
        return retVal;
    }
}
