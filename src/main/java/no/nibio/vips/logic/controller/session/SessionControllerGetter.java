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

import javax.naming.InitialContext;
import javax.naming.NamingException;

import no.nibio.vips.logic.messaging.MessagingBean;


/**
 * Thought this was oblivious because of @EJB, but it turns out this is necessary when invoking beans from outside managed beans!
 * @copyright 2013-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */

public class SessionControllerGetter {
    
	// This obviously has to be changed when changing the application name in Maven
	// TODO: Refactor out to System properties (e.g. in standalone.xml in JBoss/WildFly)
    public static final String JNDI_PATH = "java:global/VIPSLogic-2024.2/";

    public static SchedulingBean getSchedulingBean()
    {
        try
        {
            InitialContext ic = new InitialContext();
            SchedulingBean retVal = (SchedulingBean) ic.lookup(SessionControllerGetter.getJndiPath(SchedulingBean.class));

            return retVal;
        }catch(NamingException ne)
        {
            System.out.println("Could not find " + SchedulingBean.class.getSimpleName() + " using JNDI path " + SessionControllerGetter.getJndiPath(SchedulingBean.class));
            return null;
        }
    }
    
    public static ForecastBean getForecastBean()
    {
        try
        {
            InitialContext ic = new InitialContext();
            ForecastBean retVal = (ForecastBean) ic.lookup(SessionControllerGetter.getJndiPath(ForecastBean.class));

            return retVal;
        }catch(NamingException ne)
        {
            System.out.println("Could not find " + ForecastBean.class.getSimpleName() + " using JNDI path " + SessionControllerGetter.getJndiPath(ForecastBean.class));
            return null;
        }
    }
    
    public static PointOfInterestBean getPointOfInterestBean()
    {
        try
        {
            InitialContext ic = new InitialContext();
            PointOfInterestBean retVal = (PointOfInterestBean) ic.lookup(SessionControllerGetter.getJndiPath(PointOfInterestBean.class));

            return retVal;
        }catch(NamingException ne)
        {
            System.out.println("Could not find " + PointOfInterestBean.class.getSimpleName() + " using JNDI path " + SessionControllerGetter.getJndiPath(PointOfInterestBean.class));
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
            System.out.println("Could not find " + UserBean.class.getSimpleName() + " using JNDI path " + SessionControllerGetter.getJndiPath(UserBean.class));
            return null;
        }
    }
    
    public static MessagingBean getMessagingBean()
    {
        try
        {
            InitialContext ic = new InitialContext();
            MessagingBean retVal = (MessagingBean) ic.lookup(SessionControllerGetter.getJndiPath(MessagingBean.class));

            return retVal;
        }catch(NamingException ne)
        {
            System.out.println("Could not find " + MessagingBean.class.getSimpleName() + " using JNDI path " + SessionControllerGetter.getJndiPath(MessagingBean.class));
            return null;
        }
    }
    
    public static OrganismBean getOrganismBean()
    {
        try
        {
            InitialContext ic = new InitialContext();
            OrganismBean retVal = (OrganismBean) ic.lookup(SessionControllerGetter.getJndiPath(OrganismBean.class));

            return retVal;
        }catch(NamingException ne)
        {
            System.out.println("Could not find " + OrganismBean.class.getSimpleName() + " using JNDI path " + SessionControllerGetter.getJndiPath(OrganismBean.class));
            return null;
        }
    }
    
    public static ObservationBean getObservationBean()
    {
        try
        {
            InitialContext ic = new InitialContext();
            ObservationBean retVal = (ObservationBean) ic.lookup(SessionControllerGetter.getJndiPath(ObservationBean.class));

            return retVal;
        }catch(NamingException ne)
        {
            System.out.println("Could not find " + ObservationBean.class.getSimpleName() + " using JNDI path " + SessionControllerGetter.getJndiPath(ObservationBean.class));
            return null;
        }
    }
    
    public static String getJndiPath(Class obj)
    {
        String retVal = SessionControllerGetter.JNDI_PATH + obj.getSimpleName();
        //System.out.println("JNDI-path=" + retVal);
        return retVal;
    }

}