/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.nibio.vips.logic.entity.Country;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ModelInformation;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.OrganizationGroup;
import no.nibio.vips.logic.entity.PointOfInterest;
import no.nibio.vips.logic.entity.PointOfInterestExternalResource;
import no.nibio.vips.logic.entity.PointOfInterestExternalResourcePK;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.entity.VipsLogicRole;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.entity.WeatherStationDataSource;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.util.ExceptionUtil;
import no.nibio.vips.util.ServletUtil;
import no.nibio.vips.logic.entity.WeatherForecastProvider;
import no.nibio.vips.logic.util.GISUtil;
import no.nibio.vips.logic.util.Globals;
import no.nibio.web.forms.FormField;
import no.nibio.web.forms.FormValidation;
import no.nibio.web.forms.FormValidationException;
import no.nibio.web.forms.FormValidator;

/**
 * Handles transactions for POIs
 * @copyright 2013-2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
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
                    Organization organization = null;
                    List<PointOfInterestWeatherStation> activeWeatherStations;
                    List<PointOfInterestWeatherStation> inactiveWeatherStations;
                    if(user.isSuperUser()){
                        Integer organizationId = request.getParameter("organizationId") != null ?
                                Integer.parseInt(request.getParameter("organizationId"))
                                :user.getOrganizationId().getOrganizationId();
                        if(organizationId.equals(-1))
                        {
                            activeWeatherStations = SessionControllerGetter.getPointOfInterestBean().getAllWeatherStations(true);
                            inactiveWeatherStations = SessionControllerGetter.getPointOfInterestBean().getAllWeatherStations(false);
                        }
                        else
                        {
                            organization = em.find(Organization.class, organizationId);
                            activeWeatherStations = SessionControllerGetter.getPointOfInterestBean().getWeatherstationsForOrganization(organization, true);
                            inactiveWeatherStations = SessionControllerGetter.getPointOfInterestBean().getWeatherstationsForOrganization(organization, false);
                        }
                        request.setAttribute("organizations", SessionControllerGetter.getUserBean().getOrganizations());
                        request.setAttribute("organizationId", organizationId);
                        
                    }
                    else
                    {
                        organization = user.getOrganizationId();
                        activeWeatherStations = SessionControllerGetter.getPointOfInterestBean().getWeatherstationsForOrganization(organization,true);
                        inactiveWeatherStations = SessionControllerGetter.getPointOfInterestBean().getWeatherstationsForOrganization(organization, false);
                        request.setAttribute("organizationId", organization.getOrganizationId());
                    }
                    
                    if(organization != null)
                    {
                        request.setAttribute("defaultMapCenter",organization.getDefaultMapCenter());
                        request.setAttribute("defaultMapZoom", organization.getDefaultMapZoom());
                    }
                    else
                    {
                        // Set map to cover Europe
                        GeometryFactory gFact = new GeometryFactory();
                        Coordinate coord = new Coordinate(16.51699219, 55.39404223);
                        Point mapCenter = gFact.createPoint(coord);
                        request.setAttribute("defaultMapCenter",mapCenter);
                        request.setAttribute("defaultMapZoom", 3);
                    }
                    request.getSession().setAttribute("activeWeatherStations", activeWeatherStations);
                    request.getSession().setAttribute("inactiveWeatherStations", inactiveWeatherStations);
                    request.setAttribute("messageKey", request.getParameter("messageKey"));
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
                    request.getSession().setAttribute("availableTimeZones", Globals.availableTimeZones);
                    request.getSession().setAttribute("defaultTimeZoneId", user.getOrganizationId().getDefaultTimeZone());
                    request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                    request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
                    request.setAttribute("messageKey", request.getParameter("messageKey"));
                    request.setAttribute("returnURL","weatherStation?organizationId=" + weatherStation.getUserId().getOrganizationId().getOrganizationId());
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
                        request.getSession().setAttribute("availableTimeZones", Globals.availableTimeZones);
                        request.getSession().setAttribute("defaultTimeZoneId", user.getOrganizationId().getDefaultTimeZone());
                        request.getSession().setAttribute("availableCountries", em.createNamedQuery("Country.findAll").getResultList());
                        request.getSession().setAttribute("defaultCountryCode", user.getOrganizationId().getCountryCode().getCountryCode());
                        request.getSession().setAttribute("weatherForecastProviders", em.createNamedQuery("WeatherForecastProvider.findAll").getResultList());
                        // Finding all external resources where entry is missing
                        request.setAttribute("unreferencedExternalResources", SessionControllerGetter.getPointOfInterestBean().getUnusedExternalResourcesForPointOfInterest(weatherStation));
                
                        if(user.isSuperUser())
                        {
                            request.getSession().setAttribute("users", em.createNamedQuery("VipsLogicUser.findAll", VipsLogicUser.class).getResultList());
                        }
                        Integer organizationId = Integer.valueOf(request.getParameter("organizationId"));
                        request.setAttribute("returnURL","weatherStation?organizationId=" + organizationId);
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
                        request.getSession().setAttribute("availableTimeZones", Globals.availableTimeZones);
                        request.getSession().setAttribute("defaultTimeZoneId", user.getOrganizationId().getDefaultTimeZone());
                        request.getSession().setAttribute("availableCountries", em.createNamedQuery("Country.findAll").getResultList());
                        request.getSession().setAttribute("defaultCountryCode", user.getOrganizationId().getCountryCode().getCountryCode());
                        request.getSession().setAttribute("weatherForecastProviders", em.createNamedQuery("WeatherForecastProvider.findAll").getResultList());
                        // Finding all external resources where entry is missing
                        request.setAttribute("unreferencedExternalResources", SessionControllerGetter.getPointOfInterestBean().getUnusedExternalResourcesForPointOfInterest(weatherStation));
                        request.setAttribute("messageKey", request.getParameter("messageKey"));
                        if(user.isSuperUser())
                        {
                            request.getSession().setAttribute("users", em.createNamedQuery("VipsLogicUser.findAll", VipsLogicUser.class).getResultList());
                        }
                        request.setAttribute("returnURL","weatherStation?organizationId=" + weatherStation.getUserId().getOrganizationId().getOrganizationId());
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
                            weatherStation.setActive(formValidation.getFormField("active").getWebValue() != null);
                            // New weather stations are forecast locations by default
                            if(weatherStation.getPointOfInterestId() == null)
                            {
                                weatherStation.setIsForecastLocation(Boolean.TRUE);
                            }

                            Double altitude = 0.0;
                            Point p2d = formValidation.getFormField("location").getValueAsPointWGS84();
                            try
                            {
                                altitude = formValidation.getFormField("altitude").getValueAsDouble();
                                weatherStation.setAltitude(altitude);
                                
                            }
                            catch(NullPointerException | NumberFormatException ex) {}
                            Coordinate coordinate = new Coordinate(p2d.getX(), p2d.getY(),altitude);
                            GISUtil gisUtil = new GISUtil();
                            Point p3d = gisUtil.createPointWGS84(coordinate);
                            weatherStation.setGisGeom(p3d);
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
                            
                            Map<String, FormField> externalResourceIdentifiers = formValidation.getMultipleMapFormFields().get("externalResourceIdentifier");
                            for(String key:externalResourceIdentifiers.keySet())
                            {
                                Integer externalResourceId = Integer.valueOf(key);
                                FormField identifierField = externalResourceIdentifiers.get(key);
                                if(identifierField.getWebValue() == null || identifierField.getWebValue().isEmpty())
                                {
                                    // We delete existing if unset
                                    SessionControllerGetter.getPointOfInterestBean().deletePointOfInterestExternalResource(weatherStation.getPointOfInterestId(), externalResourceId);
                                }
                                else
                                {
                                    
                                    PointOfInterestExternalResource poiExternalResource = new PointOfInterestExternalResource();
                                    PointOfInterestExternalResourcePK pk = new PointOfInterestExternalResourcePK(weatherStation.getPointOfInterestId(), externalResourceId);
                                    poiExternalResource.setPointOfInterestExternalResourcePK(pk);
                                    poiExternalResource.setResourceIdentifier(identifierField.getWebValue());
                                    SessionControllerGetter.getPointOfInterestBean().storePointOfInterestExternalResource(poiExternalResource);
                                }
                            }
                            
                            // Redirect to form
                            response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://")
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
                            request.getSession().setAttribute("availableTimeZones", Globals.availableTimeZones);
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
            else if(action.equals("deleteWeatherStationPreview"))
            {
                if(SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    try
                    {
                        Integer pointOfInterestId = Integer.valueOf(request.getParameter("pointOfInterestId"));
                        PointOfInterestWeatherStation weatherStation = em.find(PointOfInterestWeatherStation.class, pointOfInterestId);
                        List<ForecastConfiguration> forecastConfigurations = SessionControllerGetter.getForecastBean().getForecastConfigurationsByWeatherStation(weatherStation);
                        // If no strings attached, delete immediately
                        if(forecastConfigurations.isEmpty())
                        {
                            response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://")
                                .append(ServletUtil.getServerName(request))
                                .append("/weatherStation?action=deleteWeatherStation&pointOfInterestId=").append(weatherStation.getPointOfInterestId())
                                .toString()
                            );
                        }
                        else
                        {
                            Map<String, ModelInformation> modelInformationMap = SessionControllerGetter.getForecastBean().getIndexedModelInformation();
                            request.setAttribute("returnURL","weatherStation?action=editWeatherStationForm&pointOfInterestId=" + pointOfInterestId);
                            request.setAttribute("weatherStation", weatherStation);
                            request.setAttribute("forecastConfigurations", forecastConfigurations);
                            request.setAttribute("modelInformation", modelInformationMap);
                            request.getRequestDispatcher("/weatherstationDeletePreview.ftl").forward(request, response);
                        }
                    }
                    catch(NumberFormatException ex)
                    {
                        response.sendError(500, ExceptionUtil.getStackTrace(ex));
                    }
                }
                else
                {
                    response.sendError(403,"Access not authorized"); // HTTP Forbidden
                }
            }
            else if(action.equals("deleteWeatherStation"))
            {
                if(SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    try
                    {
                        Integer pointOfInterestId = Integer.valueOf(request.getParameter("pointOfInterestId"));
                        //PointOfInterestWeatherStation weatherStation = em.find(PointOfInterestWeatherStation.class, pointOfInterestId);
                        SessionControllerGetter.getPointOfInterestBean().deleteWeatherStation(pointOfInterestId);
                        response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://")
                                .append(ServletUtil.getServerName(request))
                                .append("/weatherStation")
                                .append("?messageKey=").append("weatherStationDeleted").toString()

                            );
                    }
                    catch(NumberFormatException ex)
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
        else if(request.getServletPath().equals("/poi"))
        {
            if(action == null)
            {
                // Decide if requested view is list view or single view
                String pointOfInterestId = request.getParameter("pointOfInterestId");
                // List view
                if(pointOfInterestId == null)
                {
                    Organization organization = null;
                    List<PointOfInterest> pois;
                    if(user.isSuperUser()){
                        Integer organizationId = request.getParameter("organizationId") != null ?
                                Integer.parseInt(request.getParameter("organizationId"))
                                :user.getOrganizationId().getOrganizationId();
                        if(organizationId.equals(-1))
                        {
                            pois = SessionControllerGetter.getPointOfInterestBean().getAllPois();
                        }
                        else
                        {
                            organization = em.find(Organization.class, organizationId);
                            pois = SessionControllerGetter.getPointOfInterestBean().getPoisForOrganization(organization);
                        }
                        request.setAttribute("organizations", SessionControllerGetter.getUserBean().getOrganizations());
                        request.setAttribute("organizationId", organizationId);
                        
                    }
                    else if(user.isOrganizationAdmin())
                    {
                        organization = user.getOrganizationId();
                        pois = SessionControllerGetter.getPointOfInterestBean().getPoisForOrganization(organization);
                        request.setAttribute("organizationId", organization.getOrganizationId());
                    }
                    else
                    {
                        pois = SessionControllerGetter.getPointOfInterestBean().getRelevantPointOfInterestsForUser(user);
                        request.setAttribute("organizationId", user.getOrganizationId().getOrganizationId());
                    }
                    
                    if(organization != null)
                    {
                        request.setAttribute("defaultMapCenter",organization.getDefaultMapCenter());
                        request.setAttribute("defaultMapZoom", organization.getDefaultMapZoom());
                    }
                    else
                    {
                        // Set map to cover Europe
                        GeometryFactory gFact = new GeometryFactory();
                        Coordinate coord = new Coordinate(16.51699219, 55.39404223);
                        Point mapCenter = gFact.createPoint(coord);
                        request.setAttribute("defaultMapCenter",mapCenter);
                        request.setAttribute("defaultMapZoom", 3);
                    }
                    request.getSession().setAttribute("pois", pois);
                    request.setAttribute("messageKey", request.getParameter("messageKey"));
                    request.getRequestDispatcher("/poiList.ftl").forward(request, response);
                }
                // Single view
                else
                {
                    PointOfInterest poi = SessionControllerGetter.getPointOfInterestBean().getPointOfInterest(Integer.valueOf(pointOfInterestId));
                    request.getSession().setAttribute("poi", poi);
                    request.getSession().setAttribute("availableTimeZones", Globals.availableTimeZones);
                    request.getSession().setAttribute("defaultTimeZoneId", user.getOrganizationId().getDefaultTimeZone());
                    request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                    request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
                    request.setAttribute("messageKey", request.getParameter("messageKey"));
                    request.setAttribute("returnURL","poi?organizationId=" + poi.getUserId().getOrganizationId().getOrganizationId());
                    request.getRequestDispatcher("/poiView.ftl").forward(request, response);

                }
            }
            else if(action.equals("newPoiForm"))
            {

                    try
                    {
                        PointOfInterest poi = new PointOfInterestWeatherStation();  
                        request.getSession().setAttribute("poi", poi);
                        request.setAttribute("returnIdCallback", request.getParameter("returnIdCallback") != null ? request.getParameter("returnIdCallback") :"");
                        request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                        request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
                        request.getSession().setAttribute("availableTimeZones", Globals.availableTimeZones);
                        request.getSession().setAttribute("defaultTimeZoneId", user.getOrganizationId().getDefaultTimeZone());
                        request.getSession().setAttribute("weatherForecastProviders", em.createNamedQuery("WeatherForecastProvider.findAll").getResultList());
                        request.getSession().setAttribute("availableCountries", em.createNamedQuery("Country.findAll").getResultList());
                        request.getSession().setAttribute("defaultCountryCode", user.getOrganizationId().getCountryCode().getCountryCode());
                        request.getSession().setAttribute("groups", user.isSuperUser() || user.isOrganizationAdmin() ? SessionControllerGetter.getUserBean().getOrganizationGroups(user.getOrganizationId())
                                :SessionControllerGetter.getUserBean().getOrganizationGroups(user));
                        request.getSession().setAttribute("poiGroupIds", SessionControllerGetter.getPointOfInterestBean().getPoiGroupIds(poi));
                        // Finding all external resources where entry is missing
                        request.setAttribute("unreferencedExternalResources", SessionControllerGetter.getPointOfInterestBean().getUnusedExternalResourcesForPointOfInterest(poi));
                
                        if(user.isSuperUser())
                        {
                            request.getSession().setAttribute("users", em.createNamedQuery("VipsLogicUser.findAll", VipsLogicUser.class).getResultList());
                        }
                        Integer organizationId = Integer.valueOf(request.getParameter("organizationId"));
                        request.setAttribute("returnURL","poi?organizationId=" + organizationId);
                        request.getRequestDispatcher("/poiForm.ftl").forward(request, response);
                    }
                    catch(NullPointerException | NumberFormatException ex)
                    {
                        response.sendError(500, ExceptionUtil.getStackTrace(ex));
                    }
                
            }
            // Authorization: ORGANIZATION ADMIN or SUPERUSER
            else if(action.equals("editPoiForm"))
            {
                try
                {
                    Integer pointOfInterestId = Integer.valueOf(request.getParameter("pointOfInterestId"));
                    PointOfInterest poi = em.find(PointOfInterest.class, pointOfInterestId);
                    // Does the current user have the rights to edit this poi?
                    if(SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER)
                            || Objects.equals(user.getUserId(), poi.getUserId().getUserId())
                    )
                    {
                        request.getSession().setAttribute("poi", poi);
                        request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                        request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
                        request.getSession().setAttribute("availableTimeZones", Globals.availableTimeZones);
                        request.getSession().setAttribute("defaultTimeZoneId", user.getOrganizationId().getDefaultTimeZone());
                        request.getSession().setAttribute("weatherForecastProviders", em.createNamedQuery("WeatherForecastProvider.findAll").getResultList());
                        request.getSession().setAttribute("availableCountries", em.createNamedQuery("Country.findAll").getResultList());
                        request.getSession().setAttribute("defaultCountryCode", user.getOrganizationId().getCountryCode().getCountryCode());
                        request.getSession().setAttribute("groups", user.isSuperUser() || user.isOrganizationAdmin() ? SessionControllerGetter.getUserBean().getOrganizationGroups(user.getOrganizationId())
                                :SessionControllerGetter.getUserBean().getOrganizationGroups(user));
                        request.getSession().setAttribute("poiGroupIds", SessionControllerGetter.getPointOfInterestBean().getPoiGroupIds(poi));
                        // Finding all external resources where entry is missing
                        request.setAttribute("unreferencedExternalResources", SessionControllerGetter.getPointOfInterestBean().getUnusedExternalResourcesForPointOfInterest(poi));
                        request.setAttribute("messageKey", request.getParameter("messageKey"));
                        if(user.isSuperUser())
                        {
                            request.getSession().setAttribute("users", em.createNamedQuery("VipsLogicUser.findAll", VipsLogicUser.class).getResultList());
                        }
                        request.setAttribute("returnURL","poi?organizationId=" + poi.getUserId().getOrganizationId().getOrganizationId());
                        request.getRequestDispatcher("/poiForm.ftl").forward(request, response);
                    }
                    else
                    {
                        response.sendError(403,"Access not authorized"); // HTTP Forbidden
                    }
                }
                catch(NullPointerException | NumberFormatException ex)
                {
                    response.sendError(500, ExceptionUtil.getStackTrace(ex));
                }
                
            }
            else if(action.equals("poiFormSubmit"))
            {
                    try
                    {
                        Integer pointOfInterestId = Integer.valueOf(request.getParameter("pointOfInterestId"));
                        PointOfInterest poi = pointOfInterestId > 0 ? 
                                                                        em.find(PointOfInterest.class, pointOfInterestId) 
                                                                        : new PointOfInterest();
                        if(SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER)
                            || poi.getPointOfInterestId() == null
                            || Objects.equals(user.getUserId(), poi.getUserId().getUserId())
                            )
                        {
                            FormValidation formValidation = FormValidator.validateForm("poiForm", request, getServletContext());
                            if(formValidation.isValid())
                            {
                                // Set values
                                poi.setName(formValidation.getFormField("name").getWebValue());
                                // A POI is per default not a forecast location. Only superusers and orgadmins 
                                // may change the status
                                if(SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                                {
                                    poi.setIsForecastLocation(formValidation.getFormField("isForecastLocation").getWebValue() != null);
                                }
                                else if(pointOfInterestId <= 0)
                                {
                                    poi.setIsForecastLocation(Boolean.FALSE);
                                }
                                
                                Double altitude = 0.0;
                                Point p2d = formValidation.getFormField("location").getValueAsPointWGS84();
                                try
                                {
                                    altitude = formValidation.getFormField("altitude").getValueAsDouble();
                                    poi.setAltitude(altitude);
                                }
                                catch(NullPointerException | NumberFormatException ex) {}
                                Coordinate coordinate = new Coordinate(p2d.getX(), p2d.getY(),altitude);
                                GISUtil gisUtil = new GISUtil();
                                Point p3d = gisUtil.createPointWGS84(coordinate);
                                poi.setGisGeom(p3d);
                                poi.setLongitude(formValidation.getFormField("location").getValueAsPointWGS84().getX());
                                poi.setLatitude(formValidation.getFormField("location").getValueAsPointWGS84().getY());
                                poi.setWeatherForecastProviderId(formValidation.getFormField("weatherForecastProviderId").getWebValue() != null ?
                                        em.find(WeatherForecastProvider.class, formValidation.getFormField("weatherForecastProviderId").getValueAsInteger())
                                        : null
                                );
                                poi.setTimeZone(formValidation.getFormField("timeZone").getWebValue());
                                poi.setCountryCode(em.find(Country.class, formValidation.getFormField("countryCode").getWebValue()));
                                /*if(weatherStation.getPointOfInterestType() == null)
                                {
                                    //weatherStation.setPointOfInterestType(em.find(PointOfInterestType.class, Globals.POI_TYPE_WEATHERSTATION));
                                }*/
                                // If userId is set from form, always update


                                if(user.isSuperUser() && !formValidation.getFormField("userId").isEmpty())
                                {
                                    poi.setUserId(em.find(VipsLogicUser.class, formValidation.getFormField("userId").getValueAsInteger()));
                                }
                                // If user is not set, use current user
                                else if(poi.getUserId() == null)
                                {
                                    poi.setUserId(user);
                                }
                                // Store
                                poi = SessionControllerGetter.getPointOfInterestBean().storePoi(poi);

                                Map<String, FormField> externalResourceIdentifiers = formValidation.getMultipleMapFormFields().get("externalResourceIdentifier");
                                if(externalResourceIdentifiers != null)
                                {
                                    for(String key:externalResourceIdentifiers.keySet())
                                    {
                                        Integer externalResourceId = Integer.valueOf(key);
                                        FormField identifierField = externalResourceIdentifiers.get(key);
                                        if(identifierField.getWebValue() == null || identifierField.getWebValue().isEmpty())
                                        {
                                            // We delete existing if unset
                                            SessionControllerGetter.getPointOfInterestBean().deletePointOfInterestExternalResource(poi.getPointOfInterestId(), externalResourceId);
                                        }
                                        else
                                        {

                                            PointOfInterestExternalResource poiExternalResource = new PointOfInterestExternalResource();
                                            PointOfInterestExternalResourcePK pk = new PointOfInterestExternalResourcePK(poi.getPointOfInterestId(), externalResourceId);
                                            poiExternalResource.setPointOfInterestExternalResourcePK(pk);
                                            poiExternalResource.setResourceIdentifier(identifierField.getWebValue());
                                            SessionControllerGetter.getPointOfInterestBean().storePointOfInterestExternalResource(poiExternalResource);
                                        }
                                    }
                                }
                                
                                SessionControllerGetter.getPointOfInterestBean().storePointOfInterestOrganizationGroupIds(poi,formValidation.getFormField("organizationGroupIds").getWebValues());
                                
                                String returnIdCallback = request.getParameter("returnIdCallback");

                                if(returnIdCallback != null && !returnIdCallback.isEmpty())
                                {
                                    request.setAttribute("returnIdCallback",returnIdCallback);
                                    request.setAttribute("locationPointOfInterestId", poi.getPointOfInterestId());
                                    request.getRequestDispatcher("/poiFormCloseReturnIdCallback.ftl").forward(request, response);
                                }
                                else
                                {
                                    // Redirect to form
                                    response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://")
                                        .append(ServletUtil.getServerName(request))
                                        .append("/poi?action=editPoiForm&pointOfInterestId=").append(poi.getPointOfInterestId())
                                        .append("&messageKey=").append("poiStored").toString()

                                    );
                                }
                            }
                            else
                            {
                                request.setAttribute("formValidation", formValidation);
                                request.setAttribute("poi", poi);
                                request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                                request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
                                request.getSession().setAttribute("availableTimeZones", Globals.availableTimeZones);
                                request.getSession().setAttribute("defaultTimeZoneId", user.getOrganizationId().getDefaultTimeZone());
                                request.getSession().setAttribute("availableCountries", em.createNamedQuery("Country.findAll").getResultList());
                                request.getSession().setAttribute("defaultCountryCode", user.getOrganizationId().getCountryCode().getCountryCode());
                                request.getRequestDispatcher("/poiForm.ftl").forward(request, response);
                            }
                        }
                        else
                        {
                            response.sendError(403,"Access not authorized"); // HTTP Forbidden
                        }
                    }
                    catch(NullPointerException | NumberFormatException | FormValidationException ex)
                    {
                        response.sendError(500, ExceptionUtil.getStackTrace(ex));
                    }
                
                
            }
            else if(action.equals("deletePoiPreview"))
            {
                if(SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    try
                    {
                        Integer pointOfInterestId = Integer.valueOf(request.getParameter("pointOfInterestId"));
                        PointOfInterest poi = em.find(PointOfInterest.class, pointOfInterestId);
                        // Are there forecasts attached to this location
                        List<ForecastConfiguration> forecastConfigurations = SessionControllerGetter.getForecastBean().getForecastConfigurationsByLocation(poi);
                        
                        // TODO: Are there observations attached to this location?
                        
                        // 
                        // If no strings attached, delete immediately
                        if(forecastConfigurations.isEmpty())
                        {
                            response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://")
                                .append(ServletUtil.getServerName(request))
                                .append("/poi?action=deletePoi&pointOfInterestId=").append(poi.getPointOfInterestId())
                                .toString()
                            );
                        }
                        else
                        {
                            Map<String, ModelInformation> modelInformationMap = SessionControllerGetter.getForecastBean().getIndexedModelInformation();
                            request.setAttribute("returnURL","poi?action=editPoiForm&pointOfInterestId=" + pointOfInterestId);
                            request.setAttribute("poi", poi);
                            request.setAttribute("forecastConfigurations", forecastConfigurations);
                            // TODO Set observations
                            request.setAttribute("modelInformation", modelInformationMap);
                            request.getRequestDispatcher("/poiDeletePreview.ftl").forward(request, response);
                        }
                    }
                    catch(NumberFormatException ex)
                    {
                        response.sendError(500, ExceptionUtil.getStackTrace(ex));
                    }
                }
                else
                {
                    response.sendError(403,"Access not authorized"); // HTTP Forbidden
                }
            }
            else if(action.equals("deletePoi"))
            {
                if(SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    try
                    {
                        Integer pointOfInterestId = Integer.valueOf(request.getParameter("pointOfInterestId"));
                        //PointOfInterestWeatherStation weatherStation = em.find(PointOfInterestWeatherStation.class, pointOfInterestId);
                        SessionControllerGetter.getPointOfInterestBean().deletePoi(pointOfInterestId);
                        response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://")
                                .append(ServletUtil.getServerName(request))
                                .append("/poi")
                                .append("?messageKey=").append("poiDeleted").toString()

                            );
                    }
                    catch(NumberFormatException ex)
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
