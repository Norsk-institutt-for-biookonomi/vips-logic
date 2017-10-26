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
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.nibio.vips.logic.entity.VipsLogicRole;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.util.Globals;
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
        if(SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.APPLE_FRUIT_MOTH_ADMINISTRATOR, VipsLogicRole.APPLE_FRUIT_MOTH_BERRY_CLUSTER_COUNTER, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
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
                    request.setAttribute("observationSite", observationSite);
                    if(observationSite != null)
                    {
                        request.setAttribute("observationSiteSeasonCommonData", observationSite.getCommonDataForSeason(currentSeason));
                        request.setAttribute("observationSitePoints", observationSite.getObservationSitePointSet());
                    }
                    request.setAttribute("currentSeason", currentSeason);
                    request.setAttribute("lastSeason", lastSeason);
                    request.setAttribute("messageKey", request.getParameter("messageKey"));
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
                            //System.out.println("Warningstatus=" + formValidation.getFormField("warningStatus").getValueAsInteger());
                            
                            ossc.setLastUpdated(SystemTime.getSystemTime());
                            ossc.setWarningStatus(formValidation.getFormField("warningStatus").getValueAsInteger());
                            ossc.setDegreeOfParasitation(formValidation.getFormField("degreeOfParasitation") != null ? BigDecimal.valueOf(formValidation.getFormField("degreeOfParasitation").getValueAsDouble()): null);
                            ossc.setThousandBerrySample(formValidation.getFormField("thousandBerrySample") != null ? BigDecimal.valueOf(formValidation.getFormField("thousandBerrySample").getValueAsDouble()): null);
                            ossc.setRemarks(formValidation.getFormField("remarks") != null ? formValidation.getFormField("remarks").getWebValue() : "");
                            observationSite.getObservationSiteSeasonCommonDataSet().add(ossc);
                            SessionControllerGetter.getAppleFruitMothBean().storeObservationSite(observationSite);
                            
                            // Redirect to form
                            response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://")
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
            else if(action.equals("registerCluster"))
            {
                Integer observationSiteId = Integer.valueOf(request.getParameter("observationSiteId"));
                Calendar cal = Calendar.getInstance();
                cal.setTime(SystemTime.getSystemTime());
                Integer lastSeason = cal.get(Calendar.YEAR);
                Integer currentSeason = request.getParameter("currentSeason") != null ? Integer.valueOf(request.getParameter("currentSeason")) : lastSeason;
                ObservationSite observationSite = SessionControllerGetter.getAppleFruitMothBean().getObservationSite(observationSiteId);
                Map<String, ObservationSitePointSeasonData> treeData = new HashMap<>();

                for(ObservationSitePoint osPoint:observationSite.getObservationSitePointSet())
                {
                    ObservationSitePointSeasonData ospsData = osPoint.getSeasonData(currentSeason);
                    if(ospsData != null)
                    {
                        treeData.put(osPoint.getPointName(), ospsData);
                    }
                }
                request.setAttribute("treeData", treeData);
                request.setAttribute("observationSite", observationSite);
                request.setAttribute("lastSeason", lastSeason);
                request.setAttribute("currentSeason", currentSeason);
                request.setAttribute("messageKey", request.getParameter("messageKey"));
                request.getRequestDispatcher("/appleFruitMothClusterForm.ftl").forward(request, response);
            }
            else if(action.equals("clusterSubmit"))
            {
                try
                {
                    Integer currentSeason = Integer.valueOf(request.getParameter("currentSeason"));
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    Set<String> treeNames = new HashSet<>();
                    for(Enumeration e = request.getParameterNames();e.hasMoreElements();)
                    {
                        String paramName = (String) e.nextElement();
                        if(paramName.indexOf("Value") > 0 && ! request.getParameter(paramName).isEmpty())
                        {
                            treeNames.add(paramName.split("_")[1]);
                            //System.out.println(paramName);
                        }
                    }

                    Integer observationSiteId = Integer.valueOf(request.getParameter("observationSiteId"));
                    ObservationSite observationSite = SessionControllerGetter.getAppleFruitMothBean().getObservationSite(observationSiteId);
                    System.out.println("sitePointSetLength=" + observationSite.getObservationSitePointSet().size());
                    treeNames.stream().forEach(treeName->{
                        try {
                            ObservationSitePoint point = observationSite.getObservationSitePointSet().stream()
                                    .filter(sp->sp.getPointName().equals(treeName))
                                    .findFirst()
                                    // Need to use orElseGet instead of orElse, otherwise
                                    // a new sitePoint will be created each time
                                    .orElseGet(()-> SessionControllerGetter.getAppleFruitMothBean().storeObservationSitePoint(new ObservationSitePoint(observationSite, treeName)));

                            // TODO: Fill in the correct values from POST data, then store
                            //Set<ObservationSitePointSeasonData> dataSet = point.getObservationSitePointSeasonDataSet();
                            Integer autumnValue = Integer.valueOf(request.getParameter("autumnValue_" + treeName));
                            Date autumnDate = format.parse(request.getParameter("autumnDate_" + treeName));
                            Integer springValue = Integer.valueOf(request.getParameter("springValue_" + treeName));
                            Date springDate = format.parse(request.getParameter("springDate_" + treeName));
                            
                            ObservationSitePointSeasonData newData = new ObservationSitePointSeasonData(point.getObservationSitePointId(), currentSeason);
                            newData.setAutumnValue(autumnValue);
                            newData.setAutumnDate(autumnDate);
                            newData.setSpringValue(springValue);
                            newData.setSpringDate(springDate);
                            
                            SessionControllerGetter.getAppleFruitMothBean().storeObservationSitePointSeasonData(newData);
                            //dataSet.add(newData);
                            //point.setObservationSitePointSeasonDataSet(dataSet);
                            //SessionControllerGetter.getAppleFruitMothBean().storeObservationSitePoint(point);
                            

                        } catch (NullPointerException | ParseException ex) {
                            ex.printStackTrace();
                            throw new RuntimeException(ex);
                        }

                    });
                    response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://")
                                .append(ServletUtil.getServerName(request))
                                .append("/applefruitmoth?action=registerCluster&observationSiteId=").append(observationSite.getObservationSiteId())
                                .append("&currentSeason=").append(currentSeason)
                                .append("&messageKey=").append("clusterStored").toString()
                            );
                }
                catch (RuntimeException ex)
                {
                    ex.printStackTrace();
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
