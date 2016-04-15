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
package no.nibio.vips.logic.modules.applefruitmoth;

import com.ibm.icu.util.Calendar;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.nibio.vips.logic.entity.VipsLogicRole;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.ExceptionUtil;
import no.nibio.vips.util.ServletUtil;
import no.nibio.web.forms.FormValidation;
import no.nibio.web.forms.FormValidationException;
import no.nibio.web.forms.FormValidator;

/**
 *
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class AppleFruitMothController extends HttpServlet {

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
        response.setContentType("text/html;charset=UTF-8");
        VipsLogicUser user = (VipsLogicUser) request.getSession().getAttribute("user");
        if(SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.APPLE_FRUIT_MOTH_ADMINISTRATOR, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
        {
            String action = request.getParameter("action");
            if(action == null)
            {
                // Show map and list of existing stations
                List<ObservationSite> observationSites = SessionControllerGetter.getAppleFruitMothBean().getObservationSites();
                request.setAttribute("observationSites", observationSites);
                request.getRequestDispatcher("/appleFruitMothStationList.ftl").forward(request, response);
            }
            else if(action.equals("viewObservationSite"))
            {
                try
                {
                    Integer observationSiteId = Integer.valueOf(request.getParameter("observationSiteId"));
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(SystemTime.getSystemTime());
                    Integer lastSeason = cal.get(Calendar.YEAR);
                    Integer currentSeason = request.getParameter("currentSeason") != null ? Integer.valueOf(request.getParameter("currentSeason")) : lastSeason;
                    ObservationSite observationSite = SessionControllerGetter.getAppleFruitMothBean().getObservationSite(observationSiteId);
                    observationSite.setSiteCenterPoint(SessionControllerGetter.getAppleFruitMothBean().getObservationSiteCenter(observationSite));
                    request.setAttribute("observationSite", observationSite);
                    if(observationSite != null)
                    {
                        request.setAttribute("observationSiteSeasonCommonData", observationSite.getCommonDataForSeason(currentSeason));
                        request.setAttribute("observationSitePoints", observationSite.getObservationSitePointSet());
                    }
                    request.setAttribute("currentSeason", currentSeason);
                    request.setAttribute("lastSeason", lastSeason);
                    request.getRequestDispatcher("/appleFruitMothStationForm.ftl").forward(request, response);
                }
                catch(NullPointerException | NumberFormatException ex)
                {
                    response.sendError(500, ExceptionUtil.getStackTrace(ex));
                }
            }
            else if(action.equals("observationSiteSubmit"))
            {
                try
                {
                    
                    Integer observationSiteId = Integer.valueOf(request.getParameter("observationSiteId"));
                    ObservationSite observationSite = SessionControllerGetter.getAppleFruitMothBean().getObservationSite(observationSiteId);
                    if(observationSite != null)
                    {
                        FormValidation formValidation = FormValidator.validateForm("appleFruitMothStationForm", request, getServletContext());
                        if(formValidation.isValid())
                        {
                            Integer currentSeason = formValidation.getFormField("currentSeason").getValueAsInteger();
                            observationSite.setObservationSiteName(formValidation.getFormField("observationSiteName").getWebValue());
                            ObservationSiteSeasonCommonData ossc = observationSite.getCommonDataForSeason(currentSeason);
                            if(ossc == null)
                            {
                                ossc = new ObservationSiteSeasonCommonData(observationSite.getObservationSiteId(), currentSeason);
                            }
                            System.out.println("Warningstatus=" + formValidation.getFormField("warningStatus").getValueAsInteger());
                            
                            ossc.setLastUpdated(SystemTime.getSystemTime());
                            ossc.setWarningStatus(formValidation.getFormField("warningStatus").getValueAsInteger());
                            ossc.setRemarks(formValidation.getFormField("remarks") != null ? formValidation.getFormField("remarks").getWebValue() : "");
                            observationSite.getObservationSiteSeasonCommonDataSet().add(ossc);
                            SessionControllerGetter.getAppleFruitMothBean().storeObservationSite(observationSite);
                            
                            // Redirect to form
                            response.sendRedirect(new StringBuilder("http://")
                                .append(ServletUtil.getServerName(request))
                                .append("/applefruitmoth?action=viewObservationSite&observationSiteId=").append(observationSite.getObservationSiteId())
                                .append("&currentSeason=").append(currentSeason)
                                .append("&messageKey=").append("observationSiteStored").toString()
                            );
                        }
                        else // Sensible form validation error handling
                        {
                            
                        }
                    }
                    else // Something wrong, return warning message. Later on: means new site. 
                    {
                        
                    }
                    
                }
                catch(NullPointerException | NumberFormatException | FormValidationException ex)
                {
                    response.sendError(500, ExceptionUtil.getStackTrace(ex));
                }
            }
        }
        else
        {
            response.sendError(403,"Access not authorized"); // HTTP Forbidden
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
