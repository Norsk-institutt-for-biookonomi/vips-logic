/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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
package no.nibio.vips.logic.controller.servlet;

import freemarker.core.ParseException;
import java.io.IOException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.VipsLogicRole;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.messaging.ForecastEventNotificationSubscription;
import no.nibio.vips.logic.messaging.MessageNotificationSubscription;
import no.nibio.vips.logic.messaging.UniversalMessageFormat;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.util.ServletUtil;
import no.nibio.web.forms.FormUtil;

/**
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */


public class NotificationSubscriptionController extends HttpServlet {

    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        VipsLogicUser user = (VipsLogicUser) request.getSession().getAttribute("user");
        UserBean userBean = SessionControllerGetter.getUserBean();
        
        try
        {
            // Default: View list of user subscriptions
            // TODO: Only superusers and organization admins should see other users' subscriptions
            if(action == null)
            {
                Integer userId = Integer.valueOf(request.getParameter("userId"));
                VipsLogicUser viewUser;
                if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    viewUser = em.find(VipsLogicUser.class, userId);
                }
                else
                {
                    viewUser = user;
                }
                MessageNotificationSubscription messageNotificationSubscription = SessionControllerGetter.getMessagingBean().getMessageNotificationSubscription(viewUser.getUserId());
                request.setAttribute("messageNotificationSubscription",messageNotificationSubscription);
                ForecastEventNotificationSubscription forecastEventNotificationSubscription = SessionControllerGetter.getMessagingBean().getForecastEventNotificationSubscription(viewUser.getUserId());
                request.setAttribute("forecastEventNotificationSubscription",forecastEventNotificationSubscription);
                request.setAttribute("viewUser", viewUser);
                request.setAttribute("allCrops", em.createNamedQuery("Organism.findAllCrops").getResultList());
                request.setAttribute("messageTagSet", em.createNamedQuery("MessageTag.findAll").getResultList());
                request.setAttribute("weatherStationIds", SessionControllerGetter.getPointOfInterestBean().getWeatherstationsForUserOrganization(viewUser));
                request.setAttribute("universalMessageFormats", SessionControllerGetter.getMessagingBean().getAllUniversalMessageFormats());
                request.setAttribute("messageKey", request.getParameter("messageKey"));
                request.getRequestDispatcher("/notificationSubscriptionForm.ftl").forward(request, response);

            }
            else if(action.equals("notificationSubscriptionFormSubmit"))
            {
                Integer userId = Integer.valueOf(request.getParameter("userId"));
                VipsLogicUser viewUser;
                if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    viewUser = em.find(VipsLogicUser.class, userId);
                }
                else
                {
                    viewUser = user;
                }
                MessageNotificationSubscription mSubscription = new MessageNotificationSubscription();
                mSubscription.setUniversalMessageFormatId(Integer.valueOf(request.getParameter("messageN_universalMessageFormatId")));
                mSubscription.setCropIds(FormUtil.getIdsFromMultipleSelect(request.getParameterValues("messageN_cropOrganismIds")));
                mSubscription.setMessageTagIds(FormUtil.getIdsFromMultipleSelect(request.getParameterValues("messageN_messageTagIds")));
                mSubscription.setUserId(viewUser.getUserId());
                SessionControllerGetter.getMessagingBean().storeMessageNotificationSubscription(mSubscription);
                
                ForecastEventNotificationSubscription fSubscription = new ForecastEventNotificationSubscription();
                fSubscription.setUniversalMessageFormatId(Integer.valueOf(request.getParameter("forecastN_universalMessageFormatId")));
                fSubscription.setCropIds(FormUtil.getIdsFromMultipleSelect(request.getParameterValues("forecastN_cropOrganismIds")));
                fSubscription.setWeatherStationIds(FormUtil.getIdsFromMultipleSelect(request.getParameterValues("forecastN_weatherStationIds")));
                fSubscription.setUserId(viewUser.getUserId());
                SessionControllerGetter.getMessagingBean().storeForecastEventNotificationSubscription(fSubscription);
                
                // Redirect to form with confirmation message
                response.sendRedirect(new StringBuilder("http://")
                            .append(ServletUtil.getServerName(request))
                            .append("/user/notificationsubscription?userId=").append(viewUser.getUserId())
                            .append("&messageKey=").append("notificationSubscriptionsUpdated").toString()
                            
                    );
            }
        }
        catch(ParseException e)
        {
            e.printStackTrace();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
