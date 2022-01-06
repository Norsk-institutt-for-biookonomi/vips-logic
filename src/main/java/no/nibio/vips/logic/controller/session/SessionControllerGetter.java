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

package no.nibio.vips.logic.controller.session;

import javax.ejb.LocalBean;
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
    private static final String JNDI_PATH = "java:global/VIPSLogic-2022.1/";

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
    
    public static String getJndiPath(Class obj)
    {
        String retVal = SessionControllerGetter.JNDI_PATH + obj.getSimpleName();
        //System.out.println("JNDI-path=" + retVal);
        return retVal;
    }

}