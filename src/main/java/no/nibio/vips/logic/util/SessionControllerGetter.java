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

package no.nibio.vips.logic.util;

import no.nibio.vips.logic.controller.session.SchedulingBean;
import no.nibio.vips.logic.controller.session.ForecastBean;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.controller.session.PointOfInterestBean;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import no.nibio.vips.logic.controller.session.MessageBean;
import no.nibio.vips.logic.controller.session.ObservationBean;
import no.nibio.vips.logic.controller.session.OrganismBean;
import no.nibio.vips.logic.messaging.MessagingBean;
import no.nibio.vips.logic.modules.applefruitmoth.AppleFruitMothBean;

/**
 * @copyright 2013-2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
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
    
    public static ForecastBean getForecastBean(){
        try
        {
            InitialContext ic = new InitialContext();
            ForecastBean retVal = (ForecastBean) ic.lookup(SessionControllerGetter.getJndiPath(ForecastBean.class));

            return retVal;
        }catch(NamingException ne)
        {
            System.out.println("Could not find " + ForecastBean.class.getSimpleName());
            return null;
        }
    }
    
    public static OrganismBean getOrganismBean(){
        try
        {
            InitialContext ic = new InitialContext();
            OrganismBean retVal = (OrganismBean) ic.lookup(SessionControllerGetter.getJndiPath(OrganismBean.class));

            return retVal;
        }catch(NamingException ne)
        {
            System.out.println("Could not find " + OrganismBean.class.getSimpleName());
            return null;
        }
    }
    
    public static MessageBean getMessageBean(){
        try
        {
            InitialContext ic = new InitialContext();
            MessageBean retVal = (MessageBean) ic.lookup(SessionControllerGetter.getJndiPath(MessageBean.class));

            return retVal;
        }catch(NamingException ne)
        {
            System.out.println("Could not find " + MessageBean.class.getSimpleName());
            return null;
        }
    }
    
    public static MessagingBean getMessagingBean(){
        try
        {
            InitialContext ic = new InitialContext();
            MessagingBean retVal = (MessagingBean) ic.lookup(SessionControllerGetter.getJndiPath(MessagingBean.class));
            return retVal;
        }
        catch(NamingException ne)
        {
            System.out.println("Could not find " + MessagingBean.class.getSimpleName());
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
            System.out.println("Could not find " + ObservationBean.class.getSimpleName());
            return null;
        }
    }
    
    public static AppleFruitMothBean getAppleFruitMothBean()
    {
        try
        {
            InitialContext ic = new InitialContext();
            AppleFruitMothBean retVal = (AppleFruitMothBean) ic.lookup(SessionControllerGetter.getJndiPath(AppleFruitMothBean.class));

            return retVal;
        }catch(NamingException ne)
        {
            System.out.println("Could not find " + AppleFruitMothBean.class.getSimpleName());
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
