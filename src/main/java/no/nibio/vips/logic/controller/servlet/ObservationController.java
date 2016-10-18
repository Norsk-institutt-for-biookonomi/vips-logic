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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.nibio.vips.logic.entity.CropCategory;
import no.nibio.vips.logic.entity.Observation;
import no.nibio.vips.logic.entity.ObservationMethod;
import no.nibio.vips.logic.entity.Organism;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.PointOfInterestWeatherStation;
import no.nibio.vips.logic.entity.VipsLogicRole;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.ExceptionUtil;
import no.nibio.vips.util.ServletUtil;
import no.nibio.web.forms.FormUtil;
import no.nibio.web.forms.FormValidation;
import no.nibio.web.forms.FormValidationException;
import no.nibio.web.forms.FormValidator;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * @copyright 2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class ObservationController extends HttpServlet {
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
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
        // Default: View message list
        // for everyone
        if(request.getServletPath().endsWith("/observation"))
        {
            if(action == null)
            {
                List<Observation> observations = null;
                Integer statusTypeId = -1; 
                Integer pestOrganismId = -1;
                try 
                {
                    statusTypeId = request.getParameter("statusTypeId") != null ? Integer.valueOf(request.getParameter("statusTypeId")) : -1;
                    pestOrganismId = request.getParameter("pestOrganismId") != null ? Integer.valueOf(request.getParameter("pestOrganismId")) : -1;
                }
                catch(NumberFormatException nfe) {
                }


                if(statusTypeId > 0)
                {
                    observations = SessionControllerGetter.getObservationBean().getObservations(user.getOrganizationId().getOrganizationId(), statusTypeId);
                }
                else if(pestOrganismId > 0)
                {
                    observations = SessionControllerGetter.getObservationBean().getObservationsOfPest(pestOrganismId);
                }
                else
                {
                    observations = SessionControllerGetter.getObservationBean().getObservations(user.getOrganizationId().getOrganizationId());
                }

                Collections.sort(observations);
                Collections.reverse(observations);
                List<Organism> allPests = em.createNamedQuery("Organism.findAllPests").getResultList();
                request.setAttribute("allPests", SessionControllerGetter.getOrganismBean().sortOrganismsByLocalName(allPests, SessionLocaleUtil.getCurrentLocale(request).getLanguage()));
                request.setAttribute("hierarchyCategories", SessionControllerGetter.getOrganismBean().getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                request.setAttribute("selectedPestOrganismId", pestOrganismId);
                request.setAttribute("selectedStatusTypeId", statusTypeId);
                request.setAttribute("observations", observations);
                request.setAttribute("userHasObserverPrivilege", SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.OBSERVER, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER));
                request.setAttribute("userIsObservationAuthority", SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.OBSERVATION_AUTHORITY, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER));
                // If this is a redirect from a controller, with a message to be passed on
                request.setAttribute("messageKey", request.getParameter("messageKey"));
                request.getRequestDispatcher("/observationList.ftl").forward(request, response);
            }
            // Create a new observation
            // Authorization: ORGANIZATION ADMIN, OBSERVER or SUPERUSER
            else if(action.equals("newObservationForm"))
            {
                if(SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.OBSERVER, VipsLogicRole.OBSERVATION_AUTHORITY, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    try
                    {
                        Observation observation = new Observation();
                        request.setAttribute("observation", observation);
                        request.setAttribute("mapLayers", SessionControllerGetter.getUserBean().getMapLayerJSONForUser(user));
                        request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                        request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
                        request.setAttribute("locationPointOfInterests", SessionControllerGetter.getPointOfInterestBean().getRelevantPointOfInterestsForUser(user));
                        List<Organism> allPests = em.createNamedQuery("Organism.findAllPests").getResultList();
                        request.setAttribute("allPests", SessionControllerGetter.getOrganismBean().sortOrganismsByLocalName(allPests, SessionLocaleUtil.getCurrentLocale(request).getLanguage()));
                        List<Organism> allCrops = em.createNamedQuery("Organism.findAllCrops").getResultList();
                        request.setAttribute("allCrops", SessionControllerGetter.getOrganismBean().sortOrganismsByLocalName(allCrops, SessionLocaleUtil.getCurrentLocale(request).getLanguage()));
                        // Hierarchy categories
                        request.setAttribute("hierarchyCategories", SessionControllerGetter.getOrganismBean().getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                        request.setAttribute("observationMethods", em.createNamedQuery("ObservationMethod.findAll", ObservationMethod.class).getResultList());
                        if(SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.OBSERVATION_AUTHORITY, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                        {
                            request.setAttribute("statusTypeIds", em.createNamedQuery("ObservationStatusType.findAll").getResultList());
                        }
                        request.getRequestDispatcher("/observationForm.ftl").forward(request, response);
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
            // Edit an existing observation
            // Authorization: ORGANIZATION ADMIN, OBSERVER or SUPERUSER
            else if(action.equals("editObservationForm"))
            {
                if(SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.OBSERVER, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    try
                    {
                        Integer observationId = Integer.valueOf(request.getParameter("observationId"));
                        Observation observation = SessionControllerGetter.getObservationBean().getObservation(observationId);//em.find(Observation.class, observationId);
                        request.setAttribute("observation", observation);
                        request.setAttribute("mapLayers", SessionControllerGetter.getUserBean().getMapLayerJSONForUser(user));
                        //System.out.println(observation.getGeoinfo());
                        request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                        request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
                        List<Organism> allPests = em.createNamedQuery("Organism.findAllPests").getResultList();
                        request.setAttribute("allPests", SessionControllerGetter.getOrganismBean().sortOrganismsByLocalName(allPests, SessionLocaleUtil.getCurrentLocale(request).getLanguage()));
                        List<Organism> allCrops = em.createNamedQuery("Organism.findAllCrops").getResultList();
                        request.setAttribute("allCrops", SessionControllerGetter.getOrganismBean().sortOrganismsByLocalName(allCrops, SessionLocaleUtil.getCurrentLocale(request).getLanguage()));
                        // Hierarchy categories
                        request.setAttribute("hierarchyCategories", SessionControllerGetter.getOrganismBean().getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                        request.setAttribute("observationMethods", em.createNamedQuery("ObservationMethod.findAll", ObservationMethod.class).getResultList());
                        if(SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.OBSERVATION_AUTHORITY, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                        {
                            request.setAttribute("statusTypeIds", em.createNamedQuery("ObservationStatusType.findAll").getResultList());
                        }
                        if(request.getParameter("messageKey") != null)
                        {
                            request.setAttribute("messageKey",request.getParameter("messageKey"));
                        }
                        request.getRequestDispatcher("/observationForm.ftl").forward(request, response);
                    }
                    catch(NullPointerException | NumberFormatException ex)
                    {
                        response.sendError(500, ex.getMessage() + ": " + ExceptionUtil.getStackTrace(ex));
                    }
                }
                else
                {
                    response.sendError(403,"Access not authorized"); // HTTP Forbidden
                }
            }
            // Store an observation
            // Authorization: ORGANIZATION ADMIN, OBSERVER or SUPERUSER
            else if(action.equals("observationFormSubmit"))
            {
                if(SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.OBSERVER, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    try
                    {
                        Map<String,String[]> parameterMap;
                        List<FileItem> items = null;
                        if(ServletFileUpload.isMultipartContent(request))
                        {
                            // Create a factory for disk-based file items
                            DiskFileItemFactory factory = new DiskFileItemFactory();

                            // Configure a repository (to ensure a secure temp location is used)
                            ServletContext servletContext = this.getServletConfig().getServletContext();
                            File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
                            factory.setRepository(repository);

                            // Create a new file upload handler
                            ServletFileUpload upload = new ServletFileUpload(factory);

                            // Parse the request
                            items = upload.parseRequest(request);
                            parameterMap = FormUtil.getParameterMap(items,"UTF-8");
                        }
                        else
                        {
                            parameterMap = request.getParameterMap();
                        }
                        FormValidation formValidation = FormValidator.validateForm("observationForm", parameterMap, SessionLocaleUtil.getI18nBundle(request), getServletContext());
                        Integer observationId = formValidation.getFormField("observationId").getValueAsInteger();
                        Observation observation = observationId > 0 ? em.find(Observation.class, observationId) : new Observation();



                        if(formValidation.isValid())
                        {
                            // Storing observation
                            // Only new observations can set the organism
                            if(observationId <= 0 || user.isSuperUser() || user.isOrganizationAdmin())
                            {
                                observation.setOrganism(em.find(Organism.class, formValidation.getFormField("organismId").getValueAsInteger()));
                                observation.setCropOrganism(em.find(Organism.class, formValidation.getFormField("cropOrganismId").getValueAsInteger()));
                            }
                            //observation.setDenominator(formValidation.getFormField("denominator").getValueAsInteger());
                            //observation.setObservationMethodId(em.find(ObservationMethod.class, formValidation.getFormField("observationMethodId").getWebValue()));
                            observation.setTimeOfObservation(formValidation.getFormField("timeOfObservation").getValueAsTimestamp());
                            //observation.setObservedValue(formValidation.getFormField("observedValue").getValueAsDouble());
                            observation.setUserId(user.getUserId());

                            observation.setObservationHeading(formValidation.getFormField("observationHeading").getWebValue());
                            observation.setObservationText(formValidation.getFormField("observationText").getWebValue());
                            observation.setObservationData(formValidation.getFormField("observationData").getWebValue());
                            observation.setIsQuantified(formValidation.getFormField("isQuantified").getWebValue() != null);
                            observation.setBroadcastMessage(formValidation.getFormField("broadcastMessage").getWebValue() != null);

                            boolean sendNotification = false;    
                            // Storing approval status
                            if(SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.OBSERVATION_AUTHORITY, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                            {
                                Integer statusTypeId = formValidation.getFormField("statusTypeId").getValueAsInteger();
                                if(
                                        observation.getStatusTypeId() == null
                                        || ! observation.getStatusTypeId().equals(statusTypeId)
                                        )
                                {
                                    observation.setStatusChangedByUserId(user.getUserId());
                                    observation.setStatusChangedTime(SystemTime.getSystemTime());
                                    // Status changed to approved, sending notifications
                                    if(statusTypeId.equals(Observation.STATUS_TYPE_ID_APPROVED) && observation.getBroadcastMessage())
                                    {
                                        sendNotification = true;
                                    }
                                }
                                observation.setStatusTypeId(statusTypeId);
                                observation.setStatusRemarks(formValidation.getFormField("statusRemarks").getWebValue());
                            }
                            else if(observation.getStatusTypeId() == null)
                            {
                                observation.setStatusTypeId(Observation.STATUS_TYPE_ID_PENDING);
                            }
                            //observation.setLocation(formValidation.getFormField("location").getValueAsPointWGS84());
                            //System.out.println(formValidation.getFormField("geoInfo").getWebValue());
                            observation.setLocationPointOfInterestId(formValidation.getFormField("locationPointOfInterestId").getValueAsInteger() > 0 ? 
                                    formValidation.getFormField("locationPointOfInterestId").getValueAsInteger() : null);
                            if(formValidation.getFormField("locationPointOfInterestId").getValueAsInteger() > 0)
                            {
                                observation.setLocationPointOfInterestId(formValidation.getFormField("locationPointOfInterestId").getValueAsInteger());
                                observation.setGeoinfo(null);
                            }
                            else
                            {
                                observation.setLocationPointOfInterestId(null);
                                observation.setGeoinfo(formValidation.getFormField("geoInfo").getWebValue());
                            }
                            observation = SessionControllerGetter.getObservationBean().storeObservation(observation);

                            // Image handling
                            // Delete the current illustration
                            String deleteIllustration = formValidation.getFormField("deleteIllustration").getWebValue();
                            if(deleteIllustration != null && deleteIllustration.equals("true"))
                            {
                                observation = SessionControllerGetter.getObservationBean().deleteObservationIllustration(observation);
                            }

                            // Store the new illustration (replaces former illustration if not already deleted)
                            if(items != null)
                            {
                                for(FileItem item:items)
                                {
                                    if(!item.isFormField() && item.getSize() > 0)
                                    {
                                        observation = SessionControllerGetter.getObservationBean().storeObservationIllustration(observation, item);
                                    }
                                }
                            }

                            // All transactions finished, we can send notifications
                            // if conditions are met
                            if(sendNotification)
                            {
                                SessionControllerGetter.getMessagingBean().sendUniversalMessage(observation);
                            }

                            // Redirect to form
                            response.sendRedirect(new StringBuilder("http://")
                                    .append(ServletUtil.getServerName(request))
                                    .append("/observation?action=editObservationForm&observationId=").append(observation.getObservationId())
                                    .append("&messageKey=").append("observationStored").toString()

                            );
                        }
                        else
                        {
                            // Redirect to form with error messages
                            request.setAttribute("formValidation", formValidation);
                            request.setAttribute("observation", observation);
                            List<Organism> allOrganisms = em.createNamedQuery("Organism.findAll").getResultList();
                            request.setAttribute("allOrganisms", allOrganisms);
                            // Hierarchy categories
                            request.setAttribute("hierarchyCategories", SessionControllerGetter.getOrganismBean().getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                            request.setAttribute("observationMethods", em.createNamedQuery("ObservationMethod.findAll", ObservationMethod.class).getResultList());
                            request.getRequestDispatcher("/observationForm.ftl").forward(request, response);
                        }

                    }
                    catch(NullPointerException | NumberFormatException | FormValidationException | FileUploadException ex)
                    {
                        response.sendError(500, ExceptionUtil.getStackTrace(ex));
                    }
                    catch(Exception ex)
                    {
                        ex.printStackTrace();
                        response.sendError(500, ExceptionUtil.getStackTrace(ex));
                    }
                }
                else
                {
                    response.sendError(403,"Access not authorized"); // HTTP Forbidden
                }
            }
            // Authorization: ORGANIZATION ADMIN, OBSERVER or SUPERUSER
            else if(action.equals("deleteObservation"))
            {
                if(SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.OBSERVER, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    try
                    {
                        Integer observationId = Integer.valueOf(request.getParameter("observationId"));
                        SessionControllerGetter.getObservationBean().deleteObservation(observationId);

                        // Redirect to list
                        response.sendRedirect(new StringBuilder("http://")
                                .append(ServletUtil.getServerName(request))
                                .append("/observation")
                                .append("?messageKey=").append("observationDeleted").toString()
                        );
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
        }
        else if(request.getServletPath().endsWith("/map"))
        {
            // Only for superusers, organizationadmins and observation authorities
            if(SessionControllerGetter.getUserBean().authorizeUser(user, 
                    VipsLogicRole.SUPERUSER, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.OBSERVATION_AUTHORITY)
                    )
            {
                try
                {
                    FormValidation formValidation = FormValidator.validateForm("observationMapForm", request, getServletContext());
                    Organization organization = null;
                    if(user.isSuperUser()){
                        Integer organizationId = formValidation.getFormField("organizationId") != null ?
                                formValidation.getFormField("organizationId").getValueAsInteger()
                                :user.getOrganizationId().getOrganizationId();
                        organization = em.find(Organization.class, organizationId);
                        request.setAttribute("organizations", SessionControllerGetter.getUserBean().getOrganizations());
                        request.setAttribute("organizationId", organizationId);

                    }
                    else
                    {
                        organization = user.getOrganizationId();
                        request.setAttribute("organizationId", organization.getOrganizationId());
                    }

                    if(organization != null)
                    {
                        request.setAttribute("defaultMapCenter",organization.getDefaultMapCenter());
                        request.setAttribute("defaultMapZoom", organization.getDefaultMapZoom());
                    }

                    // Input control
                    // from: Default is start of year (jan 1st)
                    Date from;
                    Calendar cal = Calendar.getInstance(TimeZone.getDefault());
                    if(!formValidation.getFormField("from").isEmpty())
                    {
                        from = formValidation.getFormField("from").getValueAsDate();
                    }
                    else
                    {
                        cal.setTime(SystemTime.getSystemTime());
                        cal.set(cal.get(Calendar.YEAR), Calendar.JANUARY,1,0,0,0);
                        from = cal.getTime();
                    }
                    request.setAttribute("from", from);
                    // to
                    Date to;
                    if(!formValidation.getFormField("to").isEmpty())
                    {
                        to = formValidation.getFormField("to").getValueAsDate();
                    }
                    else
                    {
                        cal.setTime(SystemTime.getSystemTime());
                        cal.set(cal.get(Calendar.YEAR), Calendar.DECEMBER,31,23,59,59);
                        to = cal.getTime();
                    }
                    request.setAttribute("to", to);
                    // pestId
                    request.setAttribute("pestId", formValidation.getFormField("pestId").isEmpty() ? null : 
                            formValidation.getFormField("pestId").getValueAsInteger()
                    );
                    // cropId
                    request.setAttribute("cropId", formValidation.getFormField("cropId").isEmpty() ? null : 
                            formValidation.getFormField("cropId").getValueAsInteger()
                    );
                    // cropCategoryId
                    request.setAttribute("cropCategoryId", formValidation.getFormField("cropCategoryId").isEmpty() ? null : 
                            formValidation.getFormField("cropCategoryId").getValueAsInteger()
                    );

                    request.setAttribute("messageKey", request.getParameter("messageKey"));
                    request.getRequestDispatcher("/observationMap.ftl").forward(request, response);
                }
                catch(FormValidationException ex)
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

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
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
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
