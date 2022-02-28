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

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ejb.EJB;
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
import no.nibio.vips.logic.entity.PointOfInterest;
import no.nibio.vips.logic.entity.PointOfInterestExternalResource;
import no.nibio.vips.logic.entity.PointOfInterestExternalResourcePK;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.entity.VipsLogicRole;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.entity.WeatherStationDataSource;
import no.nibio.vips.util.ExceptionUtil;
import no.nibio.vips.util.ServletUtil;
import no.nibio.vips.logic.entity.WeatherForecastProvider;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.gis.GISUtil;
import no.nibio.vips.logic.controller.session.ForecastBean;
import no.nibio.vips.logic.controller.session.ObservationBean;
import no.nibio.vips.logic.controller.session.PointOfInterestBean;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.Observation;
import no.nibio.vips.logic.entity.helpers.PointOfInterestFactory;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.web.forms.FormField;
import no.nibio.web.forms.FormValidation;
import no.nibio.web.forms.FormValidationException;
import no.nibio.web.forms.FormValidator;
import org.apache.http.client.utils.URIBuilder;

/**
 * Handles transactions for POIs
 * @copyright 2013-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class PointOfInterestController extends HttpServlet {
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    @EJB
    PointOfInterestBean pointOfInterestBean;
    @EJB
    UserBean userBean;
    @EJB
    ForecastBean forecastBean;
    @EJB
    ObservationBean observationBean;
    
    
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
                            activeWeatherStations = pointOfInterestBean.getAllWeatherStations(true);
                            inactiveWeatherStations = pointOfInterestBean.getAllWeatherStations(false);
                        }
                        else
                        {
                            organization = em.find(Organization.class, organizationId);
                            activeWeatherStations = pointOfInterestBean.getWeatherstationsForOrganization(organization, true);
                            inactiveWeatherStations = pointOfInterestBean.getWeatherstationsForOrganization(organization, false);
                        }
                        request.setAttribute("organizations", userBean.getOrganizations());
                        request.setAttribute("organizationId", organizationId);
                        
                    }
                    else
                    {
                        organization = user.getOrganizationId();
                        activeWeatherStations = pointOfInterestBean.getWeatherstationsForOrganization(organization,true);
                        inactiveWeatherStations = pointOfInterestBean.getWeatherstationsForOrganization(organization, false);
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
                    PointOfInterest weatherStation = pointOfInterestBean.getPointOfInterest(Integer.valueOf(pointOfInterestId));
                    if(weatherStation instanceof PointOfInterestWeatherStation)
                    {
                        PointOfInterestWeatherStation stationWithDataSource = (PointOfInterestWeatherStation) weatherStation;
                        request.getSession().setAttribute("weatherStation", stationWithDataSource);
                        try
                        {
                            // Find query parameters in datasource query, add to template (due to GET form submission)
                            URIBuilder uriBuilder = new URIBuilder(stationWithDataSource.getDataFetchUri());
                            request.getSession().setAttribute("queryParams", uriBuilder.getQueryParams());
                        }catch(URISyntaxException ex) {ex.printStackTrace();}
                    }
                    else
                    {
                        request.getSession().setAttribute("weatherStation", weatherStation);
                    }
                    request.getSession().setAttribute("availableTimeZones", SystemTime.getAvailableTimeZones());
                    request.getSession().setAttribute("defaultTimeZoneId", user.getOrganizationId().getDefaultTimeZone());
                    request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                    request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
                    request.setAttribute("messageKey", request.getParameter("messageKey"));
                    request.setAttribute("returnURL","weatherStation?organizationId=" + weatherStation.getUser().getOrganizationId().getOrganizationId());
                    request.getRequestDispatcher("/weatherstationView.ftl").forward(request, response);

                }
            }
            else if(action.equals("newWeatherStationForm"))
            {
                if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    try
                    {
                        PointOfInterest weatherStation = new PointOfInterestWeatherStation();  
                        request.getSession().setAttribute("weatherStation", weatherStation);
                        request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                        request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
                        request.getSession().setAttribute("dataSources", pointOfInterestBean.getWeatherStationDataSources());
                        request.getSession().setAttribute("availableTimeZones", SystemTime.getAvailableTimeZones());
                        request.getSession().setAttribute("defaultTimeZoneId", user.getOrganizationId().getDefaultTimeZone());
                        request.getSession().setAttribute("availableCountries", em.createNamedQuery("Country.findAll").getResultList());
                        request.getSession().setAttribute("defaultCountryCode", user.getOrganizationId().getCountryCode().getCountryCode());
                        request.getSession().setAttribute("weatherForecastProviders", em.createNamedQuery("WeatherForecastProvider.findAll").getResultList());
                        // Finding all external resources where entry is missing
                        request.setAttribute("unreferencedExternalResources", pointOfInterestBean.getUnusedExternalResourcesForPointOfInterest(weatherStation));
                
                        if(user.isSuperUser())
                        {
                            List<VipsLogicUser> users = em.createNamedQuery("VipsLogicUser.findAll", VipsLogicUser.class).getResultList();
                            Collections.sort(users);
                            request.getSession().setAttribute("users", users);
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
                if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    try
                    {
                        Integer pointOfInterestId = Integer.valueOf(request.getParameter("pointOfInterestId"));
                        PointOfInterest weatherStation = em.find(PointOfInterest.class, pointOfInterestId);
                        request.getSession().setAttribute("weatherStation", weatherStation);
                        request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                        request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
                        request.getSession().setAttribute("dataSources", pointOfInterestBean.getWeatherStationDataSources());
                        request.getSession().setAttribute("availableTimeZones", SystemTime.getAvailableTimeZones());
                        request.getSession().setAttribute("defaultTimeZoneId", user.getOrganizationId().getDefaultTimeZone());
                        request.getSession().setAttribute("availableCountries", em.createNamedQuery("Country.findAll").getResultList());
                        request.getSession().setAttribute("defaultCountryCode", user.getOrganizationId().getCountryCode().getCountryCode());
                        request.getSession().setAttribute("weatherForecastProviders", em.createNamedQuery("WeatherForecastProvider.findAll").getResultList());
                        // Finding all external resources where entry is missing
                        request.setAttribute("unreferencedExternalResources", pointOfInterestBean.getUnusedExternalResourcesForPointOfInterest(weatherStation));
                        request.setAttribute("messageKey", request.getParameter("messageKey"));
                        if(user.isSuperUser())
                        {
                            List<VipsLogicUser> users = em.createNamedQuery("VipsLogicUser.findAll", VipsLogicUser.class).getResultList();
                            Collections.sort(users);
                            request.getSession().setAttribute("users", users);
                        }
                        request.setAttribute("returnURL","weatherStation?organizationId=" + weatherStation.getUser().getOrganizationId().getOrganizationId());
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
                if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
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
                                weatherStation.setUser(em.find(VipsLogicUser.class, formValidation.getFormField("userId").getValueAsInteger()));
                            }
                            // If user is not set, use current user
                            else if(weatherStation.getUser() == null)
                            {
                                weatherStation.setUser(user);
                            }
                            // Store
                            weatherStation = pointOfInterestBean.storeWeatherStation(weatherStation);
                            
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
                                        pointOfInterestBean.deletePointOfInterestExternalResource(weatherStation.getPointOfInterestId(), externalResourceId);
                                    }
                                    else
                                    {

                                        PointOfInterestExternalResource poiExternalResource = new PointOfInterestExternalResource();
                                        PointOfInterestExternalResourcePK pk = new PointOfInterestExternalResourcePK(weatherStation.getPointOfInterestId(), externalResourceId);
                                        poiExternalResource.setPointOfInterestExternalResourcePK(pk);
                                        poiExternalResource.setResourceIdentifier(identifierField.getWebValue());
                                        pointOfInterestBean.storePointOfInterestExternalResource(poiExternalResource);
                                    }
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
                            request.setAttribute("returnURL","weatherStation?organizationId=" + user.getOrganizationId().getOrganizationId());
                            request.getSession().setAttribute("dataSources", pointOfInterestBean.getWeatherStationDataSources());
                            request.getSession().setAttribute("availableTimeZones", SystemTime.getAvailableTimeZones());
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
                if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    try
                    {
                        Integer pointOfInterestId = Integer.valueOf(request.getParameter("pointOfInterestId"));
                        PointOfInterestWeatherStation weatherStation = em.find(PointOfInterestWeatherStation.class, pointOfInterestId);
                        List<ForecastConfiguration> forecastConfigurations = forecastBean.getForecastConfigurationsByWeatherStation(weatherStation);
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
                            Map<String, ModelInformation> modelInformationMap = forecastBean.getIndexedModelInformation();
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
                if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    try
                    {
                        Integer pointOfInterestId = Integer.valueOf(request.getParameter("pointOfInterestId"));
                        //PointOfInterestWeatherStation weatherStation = em.find(PointOfInterestWeatherStation.class, pointOfInterestId);
                        pointOfInterestBean.deleteWeatherStation(pointOfInterestId);
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
                            pois = pointOfInterestBean.getAllPois();
                        }
                        else
                        {
                            organization = em.find(Organization.class, organizationId);
                            pois = pointOfInterestBean.getPoisForOrganization(organization);
                        }
                        request.setAttribute("organizations", userBean.getOrganizations());
                        request.setAttribute("organizationId", organizationId);
                        
                    }
                    else if(user.isOrganizationAdmin())
                    {
                        organization = user.getOrganizationId();
                        pois = pointOfInterestBean.getPoisForOrganization(organization);
                        request.setAttribute("organizationId", organization.getOrganizationId());
                    }
                    else
                    {
                        pois = pointOfInterestBean.getRelevantPointOfInterestsForUser(user);
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
                    PointOfInterest poi = pointOfInterestBean.getPointOfInterest(Integer.valueOf(pointOfInterestId));
                    request.getSession().setAttribute("poi", poi);
                    request.getSession().setAttribute("availableTimeZones", SystemTime.getAvailableTimeZones());
                    request.getSession().setAttribute("defaultTimeZoneId", user.getOrganizationId().getDefaultTimeZone());
                    request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                    request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
                    request.setAttribute("messageKey", request.getParameter("messageKey"));
                    request.setAttribute("returnURL","poi?organizationId=" + poi.getUser().getOrganizationId().getOrganizationId());
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
                        request.getSession().setAttribute("availableTimeZones", SystemTime.getAvailableTimeZones());
                        request.getSession().setAttribute("defaultTimeZoneId", user.getOrganizationId().getDefaultTimeZone());
                        request.getSession().setAttribute("availableCountries", em.createNamedQuery("Country.findAll").getResultList());
                        request.getSession().setAttribute("defaultCountryCode", user.getOrganizationId().getCountryCode().getCountryCode());
                        request.getSession().setAttribute("groups", user.isSuperUser() || user.isOrganizationAdmin() ? userBean.getOrganizationGroups(user.getOrganizationId())
                                :userBean.getOrganizationGroups(user));
                        request.getSession().setAttribute("poiGroupIds", pointOfInterestBean.getPoiGroupIds(poi));
                        // Finding all external resources where entry is missing
                        request.setAttribute("unreferencedExternalResources", pointOfInterestBean.getUnusedExternalResourcesForPointOfInterest(poi));
                
                        if(user.isSuperUser())
                        {
                            request.getSession().setAttribute("users", em.createNamedQuery("VipsLogicUser.findAll", VipsLogicUser.class).getResultList());
                        }
                        else if(user.isOrganizationAdmin())
                        {
                            request.getSession().setAttribute("users", 
                                    em.createNamedQuery("VipsLogicUser.findByOrganizationId", VipsLogicUser.class)
                                            .setParameter("organizationId", user.getOrganizationId()).getResultList()
                            );
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
                    if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER)
                            || Objects.equals(user.getUserId(), poi.getUser().getUserId())
                    )
                    {
                        request.getSession().setAttribute("poi", poi);
                        request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                        request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
                        request.getSession().setAttribute("availableTimeZones", SystemTime.getAvailableTimeZones());
                        request.getSession().setAttribute("defaultTimeZoneId", user.getOrganizationId().getDefaultTimeZone());
                        request.getSession().setAttribute("weatherForecastProviders", em.createNamedQuery("WeatherForecastProvider.findAll").getResultList());
                        request.getSession().setAttribute("availableCountries", em.createNamedQuery("Country.findAll").getResultList());
                        request.getSession().setAttribute("defaultCountryCode", user.getOrganizationId().getCountryCode().getCountryCode());
                        request.getSession().setAttribute("groups", user.isSuperUser() || user.isOrganizationAdmin() ? userBean.getOrganizationGroups(user.getOrganizationId())
                                :userBean.getOrganizationGroups(user));
                        request.getSession().setAttribute("poiGroupIds", pointOfInterestBean.getPoiGroupIds(poi));
                        // Finding all external resources where entry is missing
                        request.setAttribute("unreferencedExternalResources", pointOfInterestBean.getUnusedExternalResourcesForPointOfInterest(poi));
                        request.setAttribute("messageKey", request.getParameter("messageKey"));
                        if(user.isSuperUser())
                        {
                            request.getSession().setAttribute("users", em.createNamedQuery("VipsLogicUser.findAll", VipsLogicUser.class).getResultList());
                        }
                        else if(user.isOrganizationAdmin())
                        {
                            request.getSession().setAttribute("users", 
                                    em.createNamedQuery("VipsLogicUser.findByOrganizationId", VipsLogicUser.class)
                                            .setParameter("organizationId", user.getOrganizationId()).getResultList()
                            );
                        }
                        request.setAttribute("returnURL","poi?organizationId=" + poi.getUser().getOrganizationId().getOrganizationId());
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
                        FormValidation formValidation = FormValidator.validateForm("poiForm", request, getServletContext());
                        Integer pointOfInterestId = Integer.valueOf(request.getParameter("pointOfInterestId"));
                        PointOfInterest poi = pointOfInterestId > 0 ? 
                                                                        em.find(PointOfInterest.class, pointOfInterestId) 
                                                                        : PointOfInterestFactory.getPointOfInterest(formValidation.getFormField("pointOfInterestTypeId").getValueAsInteger());
                        if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER)
                            || poi.getPointOfInterestId() == null
                            || Objects.equals(user.getUserId(), poi.getUser().getUserId())
                            )
                        {
                            Boolean poiNameAlreadyExists = pointOfInterestBean.getPointOfInterest(formValidation.getFormField("name").getWebValue()) != null;
                            // Only store if valid form data and NOT a new poi with an existing poiName
                            if(formValidation.isValid() && !(poi.getPointOfInterestId() == null && poiNameAlreadyExists))
                            {
                                // Set values
                                poi.setName(formValidation.getFormField("name").getWebValue());                                
                                // A POI is per default not a forecast location. Only superusers and orgadmins 
                                // may change the status
                                if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
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


                                if((user.isSuperUser() || user.isOrganizationAdmin()) && !formValidation.getFormField("userId").isEmpty())
                                {
                                    poi.setUser(em.find(VipsLogicUser.class, formValidation.getFormField("userId").getValueAsInteger()));
                                }
                                // If user is not set, use current user
                                else if(poi.getUser() == null)
                                {
                                    poi.setUser(user);
                                }
                                // Store
                                poi = pointOfInterestBean.storePoi(poi);

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
                                            pointOfInterestBean.deletePointOfInterestExternalResource(poi.getPointOfInterestId(), externalResourceId);
                                        }
                                        else
                                        {

                                            PointOfInterestExternalResource poiExternalResource = new PointOfInterestExternalResource();
                                            PointOfInterestExternalResourcePK pk = new PointOfInterestExternalResourcePK(poi.getPointOfInterestId(), externalResourceId);
                                            poiExternalResource.setPointOfInterestExternalResourcePK(pk);
                                            poiExternalResource.setResourceIdentifier(identifierField.getWebValue());
                                            pointOfInterestBean.storePointOfInterestExternalResource(poiExternalResource);
                                        }
                                    }
                                }
                                
                                pointOfInterestBean.storePointOfInterestOrganizationGroupIds(poi,formValidation.getFormField("organizationGroupIds").getWebValues());
                                
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
                                if(poiNameAlreadyExists)
                                {
                                    FormField poiNameField = formValidation.getFormField("name");
                                    poiNameField.setValid(false);
                                    poiNameField.setValidationMessage(SessionLocaleUtil.getI18nBundle(request).getString("nameAlreadyExists"));
                                }
                                request.setAttribute("formValidation", formValidation);
                                request.setAttribute("poi", poi);
                                request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                                request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
                                request.setAttribute("returnURL","poi?organizationId=" + user.getOrganizationId().getOrganizationId());
                                // Finding all external resources where entry is missing
                                request.setAttribute("unreferencedExternalResources", pointOfInterestBean.getUnusedExternalResourcesForPointOfInterest(poi));
                                request.getSession().setAttribute("availableTimeZones", SystemTime.getAvailableTimeZones());
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
                Integer pointOfInterestId = Integer.valueOf(request.getParameter("pointOfInterestId"));
                PointOfInterest poi = em.find(PointOfInterest.class, pointOfInterestId);
                if(
                        userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER)
                        || user.getUserId().equals(poi.getUser().getUserId())
                )
                {
                    try
                    {

                        // Are there forecasts attached to this location
                        List<ForecastConfiguration> forecastConfigurations = forecastBean.getForecastConfigurationsByLocation(poi);
                        
                        // TODO: Are there observations attached to this location?
                        List<Observation> observations = observationBean.getObservationsByLocation(poi);
                        // 
                        // If no strings attached, delete immediately
                        if(forecastConfigurations.isEmpty() && observations.isEmpty())
                        {
                            response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://")
                                .append(ServletUtil.getServerName(request))
                                .append("/poi?action=deletePoi&pointOfInterestId=").append(poi.getPointOfInterestId())
                                .toString()
                            );
                        }
                        else
                        {
                            Map<String, ModelInformation> modelInformationMap = forecastBean.getIndexedModelInformation();
                            request.setAttribute("returnURL","poi?action=editPoiForm&pointOfInterestId=" + pointOfInterestId);
                            request.setAttribute("poi", poi);
                            request.setAttribute("forecastConfigurations", forecastConfigurations);
                            // TODO Set observations
                            request.setAttribute("observations", observations);
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
                Integer pointOfInterestId = Integer.valueOf(request.getParameter("pointOfInterestId"));
                PointOfInterest poi = em.find(PointOfInterest.class, pointOfInterestId);
                if(
                        userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER)
                        || user.getUserId().equals(poi.getUser().getUserId())
                )
                {
                    try
                    {
                        pointOfInterestBean.deletePoi(pointOfInterestId);
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
