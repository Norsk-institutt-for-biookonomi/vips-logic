/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the Bioforsk Open Source License as published by 
 * Bioforsk, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Bioforsk Open Source License for more details.
 * 
 * You should have received a copy of the Bioforsk Open Source License
 * along with VIPSLogic.  If not, see <http://www.bioforsk.no/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.controller.servlet;

import java.io.IOException;
import java.util.List;
import java.util.TimeZone;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.bioforsk.vips.logic.entity.Country;
import no.bioforsk.vips.logic.entity.PointOfInterest;
import no.bioforsk.vips.logic.entity.PointOfInterestWeatherStation;
import no.bioforsk.vips.logic.entity.VipsLogicRole;
import no.bioforsk.vips.logic.entity.VipsLogicUser;
import no.bioforsk.vips.logic.entity.WeatherStationDataSource;
import no.bioforsk.vips.logic.util.SessionControllerGetter;
import no.bioforsk.vips.util.ExceptionUtil;
import no.bioforsk.vips.util.ServletUtil;
import no.bioforsk.vips.logic.entity.WeatherForecastProvider;
import no.bioforsk.web.forms.FormValidation;
import no.bioforsk.web.forms.FormValidationException;
import no.bioforsk.web.forms.FormValidator;

/**
 * Handles transactions for POIs
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class PointOfInterestController extends HttpServlet {
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.getSession().removeAttribute("weatherStations");
        request.getSession().removeAttribute("weatherStation");
        String action = request.getParameter("action");
        VipsLogicUser user = (VipsLogicUser) request.getSession().getAttribute("user");
        
        /*
        System.out.println("PathInfo=" + request.getPathInfo());
        System.out.println("PathTranslated=" + request.getPathTranslated());
        System.out.println("ServletPath=" + request.getServletPath());
        System.out.println("RequestURI=" + request.getRequestURI());
        System.out.println("RequestURL=" + request.getRequestURL());
        */
        
        if(request.getServletPath().equals("/weatherStation"))
        {
            if(action == null)
            {
                // Decide if requested view is list view or single view
                String pointOfInterestId = request.getParameter("pointOfInterestId");
                // List view
                if(pointOfInterestId == null)
                {
                    List<PointOfInterestWeatherStation> weatherStations;
                    if(user.isSuperUser()){
                        weatherStations = SessionControllerGetter.getPointOfInterestBean().getAllWeatherStations();
                    }
                    else
                    {
                        weatherStations = SessionControllerGetter.getPointOfInterestBean().getWeatherstationsForOrganization(user.getOrganizationId());
                    }
                    request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                    request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
                    request.getSession().setAttribute("weatherStations", weatherStations);
                    request.getRequestDispatcher("/weatherstationList.ftl").forward(request, response);
                }
                // Single view
                else
                {
                    PointOfInterest weatherStation = SessionControllerGetter.getPointOfInterestBean().getPointOfInterest(Integer.valueOf(pointOfInterestId));
                    if(weatherStation instanceof PointOfInterestWeatherStation)
                    {
                        PointOfInterestWeatherStation stationWithDataSource = (PointOfInterestWeatherStation) weatherStation;
                        request.getSession().setAttribute("weatherStation", stationWithDataSource);
                    }
                    else
                    {
                        request.getSession().setAttribute("weatherStation", weatherStation);
                    }
                    request.getSession().setAttribute("availableTimeZones", TimeZone.getAvailableIDs());
                    request.getSession().setAttribute("defaultTimeZoneId", user.getOrganizationId().getDefaultTimeZone());
                    request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                    request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());

                    request.getRequestDispatcher("/weatherstationView.ftl").forward(request, response);

                }
            }
            else if(action.equals("newWeatherStationForm"))
            {
                if(SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    try
                    {
                        PointOfInterest weatherStation = new PointOfInterestWeatherStation();  
                        request.getSession().setAttribute("weatherStation", weatherStation);
                        request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                        request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
                        request.getSession().setAttribute("dataSources", SessionControllerGetter.getPointOfInterestBean().getWeatherStationDataSources());
                        request.getSession().setAttribute("availableTimeZones", TimeZone.getAvailableIDs());
                        request.getSession().setAttribute("defaultTimeZoneId", user.getOrganizationId().getDefaultTimeZone());
                        request.getSession().setAttribute("availableCountries", em.createNamedQuery("Country.findAll").getResultList());
                        request.getSession().setAttribute("defaultCountryCode", user.getOrganizationId().getCountryCode().getCountryCode());
                        request.getSession().setAttribute("weatherForecastProviders", em.createNamedQuery("WeatherForecastProvider.findAll").getResultList());
                        if(user.isSuperUser())
                        {
                            request.getSession().setAttribute("users", em.createNamedQuery("VipsLogicUser.findAll", VipsLogicUser.class).getResultList());
                        }
                        request.getRequestDispatcher("/weatherstationForm.ftl").forward(request, response);
                    }
                    catch(NullPointerException | NumberFormatException ex)
                    {
                        response.sendError(500, ExceptionUtil.getStackTrace(ex));
                    }
                }
                else
                {
                    response.sendError(403,"Access not authorized"); // HTTP Forbidden
                }
            }
            // Authorization: ORGANIZATION ADMIN or SUPERUSER
            else if(action.equals("editWeatherStationForm"))
            {
                if(SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    try
                    {
                        Integer pointOfInterestId = Integer.valueOf(request.getParameter("pointOfInterestId"));
                        PointOfInterest weatherStation = em.find(PointOfInterest.class, pointOfInterestId);
                        request.getSession().setAttribute("weatherStation", weatherStation);
                        request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                        request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
                        request.getSession().setAttribute("dataSources", SessionControllerGetter.getPointOfInterestBean().getWeatherStationDataSources());
                        request.getSession().setAttribute("availableTimeZones", TimeZone.getAvailableIDs());
                        request.getSession().setAttribute("defaultTimeZoneId", user.getOrganizationId().getDefaultTimeZone());
                        request.getSession().setAttribute("availableCountries", em.createNamedQuery("Country.findAll").getResultList());
                        request.getSession().setAttribute("defaultCountryCode", user.getOrganizationId().getCountryCode().getCountryCode());
                        request.getSession().setAttribute("weatherForecastProviders", em.createNamedQuery("WeatherForecastProvider.findAll").getResultList());
                        request.setAttribute("messageKey", request.getParameter("messageKey"));
                        if(user.isSuperUser())
                        {
                            request.getSession().setAttribute("users", em.createNamedQuery("VipsLogicUser.findAll", VipsLogicUser.class).getResultList());
                        }
                        request.getRequestDispatcher("/weatherstationForm.ftl").forward(request, response);
                    }
                    catch(NullPointerException | NumberFormatException ex)
                    {
                        response.sendError(500, ExceptionUtil.getStackTrace(ex));
                    }
                }
                else
                {
                    response.sendError(403,"Access not authorized"); // HTTP Forbidden
                }
            }
            // Authorization: ORGANIZATION ADMIN or SUPERUSER
            else if(action.equals("weatherStationFormSubmit"))
            {
                if(SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    try
                    {
                        Integer pointOfInterestId = Integer.valueOf(request.getParameter("pointOfInterestId"));
                        PointOfInterestWeatherStation weatherStation = pointOfInterestId > 0 ? 
                                                                        em.find(PointOfInterestWeatherStation.class, pointOfInterestId) 
                                                                        : new PointOfInterestWeatherStation();
                        FormValidation formValidation = FormValidator.validateForm("weatherStationForm", request, getServletContext());
                        if(formValidation.isValid())
                        {
                            // Set values
                            weatherStation.setName(formValidation.getFormField("name").getWebValue());
                            weatherStation.setAltitude(formValidation.getFormField("altitude").getValueAsDouble());
                            weatherStation.setLongitude(formValidation.getFormField("location").getValueAsPointWGS84().getX());
                            weatherStation.setLatitude(formValidation.getFormField("location").getValueAsPointWGS84().getY());
                            weatherStation.setWeatherStationDataSourceId(em.find(WeatherStationDataSource.class, formValidation.getFormField("weatherStationDataSourceId").getValueAsInteger()));
                            weatherStation.setWeatherForecastProviderId(formValidation.getFormField("weatherForecastProviderId").getWebValue() != null ?
                                    em.find(WeatherForecastProvider.class, formValidation.getFormField("weatherForecastProviderId").getValueAsInteger())
                                    : null
                            );
                            weatherStation.setWeatherStationRemoteId(formValidation.getFormField("weatherStationRemoteId").getWebValue());
                            weatherStation.setTimeZone(formValidation.getFormField("timeZone").getWebValue());
                            weatherStation.setCountryCode(em.find(Country.class, formValidation.getFormField("countryCode").getWebValue()));
                            /*if(weatherStation.getPointOfInterestType() == null)
                            {
                                //weatherStation.setPointOfInterestType(em.find(PointOfInterestType.class, Globals.POI_TYPE_WEATHERSTATION));
                            }*/
                            // If userId is set from form, always update
                            if(user.isSuperUser() && !formValidation.getFormField("userId").isEmpty())
                            {
                                weatherStation.setUserId(em.find(VipsLogicUser.class, formValidation.getFormField("userId").getValueAsInteger()));
                            }
                            // If user is not set, use current user
                            else if(weatherStation.getUserId() == null)
                            {
                                weatherStation.setUserId(user);
                            }
                            // Store
                            weatherStation = SessionControllerGetter.getPointOfInterestBean().storeWeatherStation(weatherStation);
                            
                            // Redirect to form
                            response.sendRedirect(new StringBuilder("http://")
                                .append(ServletUtil.getServerName(request))
                                .append("/weatherStation?action=editWeatherStationForm&pointOfInterestId=").append(weatherStation.getPointOfInterestId())
                                .append("&messageKey=").append("weatherStationStored").toString()

                            );
                        }
                        else
                        {
                            request.setAttribute("formValidation", formValidation);
                            request.setAttribute("weatherStation", weatherStation);
                            request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                            request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
                            request.getSession().setAttribute("dataSources", SessionControllerGetter.getPointOfInterestBean().getWeatherStationDataSources());
                            request.getSession().setAttribute("availableTimeZones", TimeZone.getAvailableIDs());
                            request.getSession().setAttribute("defaultTimeZoneId", user.getOrganizationId().getDefaultTimeZone());
                            request.getSession().setAttribute("availableCountries", em.createNamedQuery("Country.findAll").getResultList());
                            request.getSession().setAttribute("defaultCountryCode", user.getOrganizationId().getCountryCode().getCountryCode());
                            request.getRequestDispatcher("/weatherstationForm.ftl").forward(request, response);
                        }
                    }
                    catch(NullPointerException | NumberFormatException | FormValidationException ex)
                    {
                        response.sendError(500, ExceptionUtil.getStackTrace(ex));
                    }
                }
                else
                {
                    response.sendError(403,"Access not authorized"); // HTTP Forbidden
                }
            }
        }
    }
// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
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
     * Handles the HTTP
     * <code>POST</code> method.
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
