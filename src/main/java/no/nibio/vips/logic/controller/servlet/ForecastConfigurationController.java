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

package no.nibio.vips.logic.controller.servlet;

import com.ibm.icu.util.Calendar;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.nibio.vips.logic.controller.session.ForecastBean;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ModelInformation;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.VipsLogicRole;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.ArrayUtil;
import no.nibio.vips.util.ServletUtil;
import no.nibio.web.forms.FormField;
import no.nibio.web.forms.FormValidation;
import no.nibio.web.forms.FormValidationException;
import no.nibio.web.forms.FormValidator;

/**
 * Handles form configuration actions
 * @copyright 2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class ForecastConfigurationController extends HttpServlet {
    
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
        response.setContentType("text/html;charset=UTF-8");
        
        String action = request.getParameter("action");
        VipsLogicUser user = (VipsLogicUser) request.getSession().getAttribute("user");
        ForecastBean forecastBean = SessionControllerGetter.getForecastBean();
        UserBean userBean = SessionControllerGetter.getUserBean();
        
        // Default: View list of forecast configurations
        // for SUPERUSERS and ORGANIZATION ADMINS
        if(action == null)
        {
            if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
            {
                Map<String, ModelInformation> modelInformationMap = forecastBean.getIndexedModelInformation();
                
                // Check filtering conditions
                List<String> selectedModelIds;
                try
                {
                    selectedModelIds = Arrays.asList(request.getParameterValues("modelId"));
                }
                catch(NullPointerException ex)
                {
                    selectedModelIds = new ArrayList(modelInformationMap.keySet());
                }
                
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date from,to;
                try
                {
                    from = format.parse(request.getParameter("from"));
                    to = format.parse(request.getParameter("to"));
                }
                catch(NullPointerException | ParseException ex)
                {
                    to = SystemTime.getSystemTime();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(to);
                    cal.add(Calendar.MONTH, -2);
                    from = cal.getTime();
                }
                
                List<ForecastConfiguration> forecasts;
                
                if(user.isSuperUser())
                {
                    // Filtering of organizations
                    List<Organization> organizations = userBean.getOrganizations();
                    List<Integer> selectedOrganizationIds;
                    try
                    {
                        selectedOrganizationIds = Arrays.asList(ArrayUtil.stringArrayToIntegerArray(request.getParameterValues("organizationId")));
                    }
                    // No filter set -> select all organizations (default)
                    catch(NullPointerException ex)
                    {
                        selectedOrganizationIds = new ArrayList<>();
                        for(Organization o:organizations)
                        {
                            selectedOrganizationIds.add(o.getOrganizationId());
                        }
                    }
                    forecasts = forecastBean.getForecastConfigurations(selectedOrganizationIds, selectedModelIds, from, to);
                    
                    request.setAttribute("organizations", organizations);
                    request.setAttribute("selectedOrganizationIds", selectedOrganizationIds);
                    
                    request.setAttribute("allUsers", userBean.getAllUsers());
                    // If super user requests private forecasts for a user
                    try
                    {
                        Integer otherUserId = Integer.valueOf(request.getParameter("otherUserId"));
                        List<ForecastConfiguration> privateForecastConfigurationsForOtherUser = forecastBean.getPrivateForecastConfigurationsForUser(otherUserId);
                        request.setAttribute("privateForecastConfigurationsForOtherUser", privateForecastConfigurationsForOtherUser);
                        request.setAttribute("otherUserId", otherUserId);
                    }
                    catch(NumberFormatException nfe) {}
                }
                else
                {
                    forecasts = forecastBean.getForecastConfigurations(user.getOrganizationId(), selectedModelIds, from, to);
                }
                Collections.sort(forecasts);
                
                request.setAttribute("forecastConfigurations", forecasts);
                
                List<ForecastConfiguration> privateForecasts;
                privateForecasts = forecastBean.getPrivateForecastConfigurationsForUser(user.getUserId());
                request.setAttribute("privateForecastConfigurations", privateForecasts);
                request.setAttribute("modelInformation", modelInformationMap);
                request.setAttribute("selectedModelIds", selectedModelIds);
                request.setAttribute("from", from);
                request.setAttribute("to", to);
                // If this is a redirect from a controller, with a message to be passed on
                request.setAttribute("messageKey", request.getParameter("messageKey"));
                request.getRequestDispatcher("/forecastConfigurationList.ftl").forward(request, response);
            }
            else
            {
                response.sendError(403,"Access not authorized"); // HTTP Forbidden
            }
      
        }
        
        // View and edit a forecast configuration
        // for SUPERUSERS and ORGANIZATION ADMINS
        else if(action.equals("viewForecastConfiguration"))
        {
            if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
            {
                try
                {
                    Long forecastConfigurationId = Long.valueOf(request.getParameter("forecastConfigurationId"));
                    ForecastConfiguration forecastConfiguration = em.find(ForecastConfiguration.class, forecastConfigurationId);
                    boolean multipleNew = false;
                    // No forecastconfiguration found, assuming user want to register new
                    if(forecastConfiguration == null)
                    {
                        forecastConfiguration = new ForecastConfiguration();
                        multipleNew =   request.getParameter("multipleNew") != null 
                                        && request.getParameter("multipleNew").equals("true");
                    }
                    // Only superusers can view and edit forecasts from other organizations
                    if(! user.isSuperUser() && forecastConfiguration.getVipsLogicUserId() != null && !forecastConfiguration.getVipsLogicUserId().getOrganizationId().equals(user.getOrganizationId()))
                    {
                        response.sendError(403,"Access not authorized"); // HTTP Forbidden
                    }
                    // Only superusers can view and edit private forecasts from other users
                    else if(forecastConfiguration.getIsPrivate() && ! user.isSuperUser() && forecastConfiguration.getVipsLogicUserId() != null && !forecastConfiguration.getVipsLogicUserId().getUserId().equals(user.getUserId()))
                    {
                        response.sendError(403,"Access not authorized"); // HTTP Forbidden
                    }
                    else
                    {
                        // TODO: More intelligent selection of locations, weather stations and users
                        if(user.isSuperUser())
                        {
                            request.setAttribute("locationPointOfInterests", em.createNamedQuery("PointOfInterest.findAll").getResultList());
                            request.setAttribute("weatherStationPointOfInterests", em.createNamedQuery("PointOfInterestWeatherStation.findAll").getResultList());
                            request.setAttribute("vipsLogicUsers", em.createNamedQuery("VipsLogicUser.findAll").getResultList());
                        }
                        else
                        {
                            request.setAttribute("locationPointOfInterests", em.createNamedQuery("PointOfInterest.findByOrganizationId").setParameter("organizationId", user.getOrganizationId()).getResultList());
                            request.setAttribute("weatherStationPointOfInterests", em.createNamedQuery("PointOfInterestWeatherStation.findByOrganizationId").setParameter("organizationId", user.getOrganizationId()).getResultList());
                            request.setAttribute("vipsLogicUsers", em.createNamedQuery("VipsLogicUser.findByOrganizationId").setParameter("organizationId", user.getOrganizationId()).getResultList());
                        }
                        request.setAttribute("forecastConfiguration", forecastConfiguration);
                        request.setAttribute("formId", multipleNew ? "forecastConfigurationMultipleNewForm" : "forecastConfigurationForm");
                        request.setAttribute("multipleNew", multipleNew);
                        request.getSession().setAttribute("availableTimeZones", Globals.availableTimeZones);
                        request.getSession().setAttribute("defaultTimeZoneId", user.getOrganizationId().getDefaultTimeZone());
                        // All organisms used for parent organism list
                        request.setAttribute("allCrops", em.createNamedQuery("Organism.findAllCrops").getResultList());
                        request.setAttribute("allPests", em.createNamedQuery("Organism.findAllPests").getResultList());
                        // Hierarchy categories
                        request.setAttribute("hierarchyCategories", SessionControllerGetter.getOrganismBean().getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                        request.setAttribute("modelInformations", em.createNamedQuery("ModelInformation.findAll").getResultList());
                        request.setAttribute("messageKey", request.getParameter("messageKey"));
                        request.getRequestDispatcher("/forecastConfigurationForm.ftl").forward(request, response);
                       
                    }
                }
                catch(NullPointerException | NumberFormatException ex)
                {
                    response.sendError(500, "Invalid forecast configurationId " + request.getParameter("forecastConfigurationId"));
                }
                
            }
            else
            {
                response.sendError(403,"Access not authorized"); // HTTP Forbidden
            }
        }
        
        // Store forecast configuration(s)
        // Authorization: SUPERUSERS and ORGANIZATION ADMINS
        else if(action.equals("forecastConfigurationFormSubmit"))
        {
            if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
            {
                try
                {
                    Long forecastConfigurationId = Long.valueOf(request.getParameter("forecastConfigurationId"));
                    ForecastConfiguration forecastConfiguration = em.find(ForecastConfiguration.class, forecastConfigurationId);
                    // No forecastconfiguration found, assuming user want to register new
                    if(forecastConfiguration == null)
                    {
                        forecastConfiguration = new ForecastConfiguration();
                    }
                    // Only superusers can view and edit forecasts from other organizations
                    if(! user.isSuperUser() && forecastConfiguration.getVipsLogicUserId() != null && !forecastConfiguration.getVipsLogicUserId().getOrganizationId().equals(user.getOrganizationId()))
                    {
                        response.sendError(403,"Access not authorized"); // HTTP Forbidden
                    }
                    else
                    {
                        String formId = request.getParameter("multipleNew") != null && request.getParameter("multipleNew").equals("true") ? 
                                "forecastConfigurationMultipleNewForm" 
                                :"forecastConfigurationForm";
                        FormValidation formValidation = FormValidator.validateForm(formId,request,getServletContext());
                        // Also validation the model specific fields
                        String modelId = formValidation.getFormField("modelId").getWebValue();
                        FormValidation modelFieldFormValidation = FormValidator.validateForm("models/" + modelId, request, getServletContext());
                        
                        if(formValidation.isValid() && modelFieldFormValidation.isValid())
                        {
                            if(formId.equals("forecastConfigurationForm"))
                            {
                                forecastConfiguration = forecastBean.storeForecastConfiguration(forecastConfiguration, formValidation.getFormFields(), modelFieldFormValidation.getFormFields());
                                // Store form config


                                //forecastBean.storeForecastModelConfigurations(forecastConfiguration, modelFieldFormValidation.getFormFields());
                                // TODO: Store model specific fields
                                // First: Delete all o


                                request.setAttribute("messageKey", request.getParameter("formConfigurationUpdated"));
                                response.sendRedirect(new StringBuilder("http://").append(ServletUtil.getServerName(request)).append("/forecastConfiguration?action=viewForecastConfiguration&forecastConfigurationId=").append(forecastConfiguration.getForecastConfigurationId()).append("&messageKey=").append("forecastConfigurationUpdated").toString());
                            }
                            else
                            {
                                for(String optionVal:formValidation.getFormField("weatherStationPointOfInterestIds").getWebValues())
                                {
                                    Integer weatherStationPointOfInterestId = Integer.valueOf(optionVal);
                                    forecastBean.storeNewMultipleForecastConfiguration(weatherStationPointOfInterestId, formValidation.getFormFields(), modelFieldFormValidation.getFormFields());    
                                }
                                request.setAttribute("messageKey", request.getParameter("multipleForecastConfigurationsCreated"));
                                response.sendRedirect(new StringBuilder("http://").append(ServletUtil.getServerName(request)).append("/forecastConfiguration?messageKey=").append("multipleForecastConfigurationsCreated").toString());
                            }
                        }
                        else
                        {
                            // Return to form with error messages
                            request.setAttribute("formValidation", formValidation);
                            // We must get date formats!
                            Map<String, FormField> formFields = FormValidator.getFormFields("forecastConfigurationForm",getServletContext());
                            // TODO: More intelligent selection of locations, weather stations and users
                            request.setAttribute("locationPointOfInterests", em.createNamedQuery("PointOfInterest.findAll").getResultList());
                            request.setAttribute("weatherStationPointOfInterests", em.createNamedQuery("PointOfInterestWeatherStation.findAll").getResultList());
                            request.setAttribute("vipsLogicUsers", em.createNamedQuery("VipsLogicUser.findAll").getResultList());
                            request.setAttribute("dateStart_dateFormat", formFields.get("dateStart").getDateFormat());
                            request.setAttribute("dateEnd_dateFormat", formFields.get("dateEnd").getDateFormat());
                            request.getSession().setAttribute("availableTimeZones", Globals.availableTimeZones);
                            request.getSession().setAttribute("defaultTimeZoneId", user.getOrganizationId().getDefaultTimeZone());
                            request.setAttribute("modelInformations", em.createNamedQuery("ModelInformation.findAll").getResultList());
                            request.setAttribute("forecastConfiguration", forecastConfiguration);
                            request.getRequestDispatcher("/forecastConfigurationForm.ftl").forward(request, response);
                        }
                    }
                }
                catch(NullPointerException | NumberFormatException | FormValidationException ex)
                {
                    if(ex instanceof NumberFormatException)
                    {
                        response.sendError(500, "Invalid forecast configurationId " + request.getParameter("forecastConfigurationId"));
                    }
                    else
                    {
                        response.sendError(500, ex.getMessage());
                    }
                }
            }
            else
            {
                response.sendError(403,"Access not authorized"); // HTTP Forbidden
            }
        }
        // Delete forecast configuration
        // Authorization: SUPERUSERS and ORGANIZATION ADMINS
        else if(action.equals("deleteForecastConfiguration"))
        {
            if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
            {
                try
                {
                    Long forecastConfigurationId = Long.valueOf(request.getParameter("forecastConfigurationId"));
                    ForecastConfiguration forecastConfiguration = em.find(ForecastConfiguration.class, forecastConfigurationId);

                    // Only superusers can delete forecasts from other organizations
                    if(! user.isSuperUser() && forecastConfiguration.getVipsLogicUserId() != null && !forecastConfiguration.getVipsLogicUserId().getOrganizationId().equals(user.getOrganizationId()))
                    {
                        response.sendError(403,"Access not authorized"); // HTTP Forbidden
                    }
                    else
                    {
                        forecastBean.deleteForecastConfiguration(forecastConfigurationId);
                        response.sendRedirect(new StringBuilder("http://").append(ServletUtil.getServerName(request)).append("/forecastConfiguration?").append("&messageKey=").append("forecastConfigurationDeleted").toString());
                       
                    }
                }
                catch(NullPointerException | NumberFormatException ex)
                {
                    response.sendError(500, "Invalid forecast configurationId " + request.getParameter("forecastConfigurationId"));
                }
                
            }
            else
            {
                response.sendError(403,"Access not authorized"); // HTTP Forbidden
            }
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
