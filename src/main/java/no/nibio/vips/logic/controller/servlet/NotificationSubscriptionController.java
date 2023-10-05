/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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
package no.nibio.vips.logic.controller.servlet;

import freemarker.core.ParseException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.nibio.vips.logic.controller.session.OrganismBean;
import no.nibio.vips.logic.controller.session.PointOfInterestBean;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.CropCategory;
import no.nibio.vips.logic.entity.VipsLogicRole;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.messaging.ForecastEventNotificationSubscription;
import no.nibio.vips.logic.messaging.MessageNotificationSubscription;
import no.nibio.vips.logic.messaging.MessagingBean;
import no.nibio.vips.logic.messaging.ObservationNotificationSubscription;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.util.ServletUtil;
import no.nibio.web.forms.FormUtil;

/**
 * @copyright 2016-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */


public class NotificationSubscriptionController extends HttpServlet {

    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    @EJB
    UserBean userBean;
    
    @EJB
    MessagingBean messagingBean;
    
    @EJB
    OrganismBean organismBean;
    
    @EJB
    PointOfInterestBean pointOfInterestBean;
    
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
        if(user == null)
        {
            this.redirectToLogin(request, response);
            return;
        }

        try
        {
            // Default: View list of user subscriptions
            // TODO: Only superusers and organization admins should see other users' subscriptions
            if(action == null)
            {
                
                VipsLogicUser viewUser;
                if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    Integer userId;
                    try
                    {
                        userId = Integer.valueOf(request.getParameter("userId"));
                        viewUser = em.find(VipsLogicUser.class, userId);
                    }
                    catch(NullPointerException | NumberFormatException e)
                    {
                        response.sendError(500, "Invalid user id");
                        return;
                    }
                }
                else
                {
                    viewUser = em.find(VipsLogicUser.class, user.getUserId());
                }
                ObservationNotificationSubscription observationNotificationSubscription = messagingBean.getObservationNotificationSubscription(viewUser.getUserId());
                request.setAttribute("observationNotificationSubscription", observationNotificationSubscription);
                MessageNotificationSubscription messageNotificationSubscription = messagingBean.getMessageNotificationSubscription(viewUser.getUserId());
                request.setAttribute("messageNotificationSubscription",messageNotificationSubscription);
                ForecastEventNotificationSubscription forecastEventNotificationSubscription = messagingBean.getForecastEventNotificationSubscription(viewUser.getUserId());
                request.setAttribute("forecastEventNotificationSubscription",forecastEventNotificationSubscription);
                request.setAttribute("viewUser", viewUser);
                List<CropCategory> allCropCategories = organismBean
                        .sortCropCategoryByLocalName(
                                em.createNamedQuery("CropCategory.findByOrganizationId").setParameter("organizationId", viewUser.getOrganizationId().getOrganizationId()).getResultList(),
                                SessionLocaleUtil.getCurrentLocale(request).getLanguage()
                        );
                request.setAttribute("allCropCategories", allCropCategories);
                request.setAttribute("messageTagSet", em.createNamedQuery("MessageTag.findAll").getResultList());
                request.setAttribute("weatherStationIds", pointOfInterestBean.getWeatherstationsForOrganization(viewUser.getOrganizationId(), true));
                request.setAttribute("universalMessageFormats", messagingBean.getAllUniversalMessageFormats());
                request.setAttribute("messageKey", request.getParameter("messageKey"));
                request.getRequestDispatcher("/notificationSubscriptionForm.ftl").forward(request, response);
            }
            else if(action.equals("notificationSubscriptionFormSubmit"))
            {
                VipsLogicUser viewUser;
                if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    Integer userId = Integer.valueOf(request.getParameter("userId"));
                    viewUser = em.find(VipsLogicUser.class, userId);
                }
                else
                {
                    viewUser = em.find(VipsLogicUser.class, user.getUserId());
                }
                MessageNotificationSubscription mSubscription = new MessageNotificationSubscription();
                mSubscription.setUniversalMessageFormatId(Integer.valueOf(request.getParameter("messageN_universalMessageFormatId")));
                mSubscription.setCropCategoryIds(FormUtil.getIdsFromMultipleSelect(request.getParameterValues("messageN_cropCategoryIds")));
                mSubscription.setMessageTagIds(FormUtil.getIdsFromMultipleSelect(request.getParameterValues("messageN_messageTagIds")));
                mSubscription.setUserId(viewUser.getUserId());
                messagingBean.storeMessageNotificationSubscription(mSubscription);
                
                ForecastEventNotificationSubscription fSubscription = new ForecastEventNotificationSubscription();
                fSubscription.setUniversalMessageFormatId(Integer.valueOf(request.getParameter("forecastN_universalMessageFormatId")));
                fSubscription.setCropCategoryIds(FormUtil.getIdsFromMultipleSelect(request.getParameterValues("forecastN_cropCategoryIds")));
                fSubscription.setWeatherStationIds(FormUtil.getIdsFromMultipleSelect(request.getParameterValues("forecastN_weatherStationIds")));
                fSubscription.setUserId(viewUser.getUserId());
                messagingBean.storeForecastEventNotificationSubscription(fSubscription);
                
                ObservationNotificationSubscription oSubscription = new ObservationNotificationSubscription();
                oSubscription.setUniversalMessageFormatId(Integer.valueOf(request.getParameter("observationN_universalMessageFormatId")));
                oSubscription.setCropCategoryIds(FormUtil.getIdsFromMultipleSelect(request.getParameterValues("observationN_cropCategoryIds")));
                oSubscription.setUserId(viewUser.getUserId());
                messagingBean.storeObservationNotificationSubscription(oSubscription);
                
                // Redirect to form with confirmation message
                response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://")
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

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, IOException
    {
        String nextPage = ServletUtil.getFullRequestURI(request);
        String nextPageDirective= "?nextPage=" + URLEncoder.encode(nextPage, "UTF-8");
        response.sendRedirect(Globals.PROTOCOL + "://" + ServletUtil.getServerName(request) + "/login" + nextPageDirective);
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
