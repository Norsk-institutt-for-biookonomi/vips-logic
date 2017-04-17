/*
 * Copyright (c) 2017 NIBIO <http://www.nibio.no/>. 
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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.nibio.vips.logic.entity.Observation;
import no.nibio.vips.logic.entity.ObservationFormShortcut;
import no.nibio.vips.logic.entity.ObservationMethod;
import no.nibio.vips.logic.entity.Organism;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.OrganizationGroup;
import no.nibio.vips.logic.entity.VipsLogicRole;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.util.Globals;
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
 * @copyright 2014-2017 <a href="http://www.nibio.no/">NIBIO</a>
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
        // Get the organization groups for the current user
        List<OrganizationGroup> organizationGroups = null;
        if(user.isSuperUser())
        {
           organizationGroups =  SessionControllerGetter.getUserBean().getOrganizationGroups();
        }
        else if(user.isOrganizationAdmin())
        {
            organizationGroups = SessionControllerGetter.getUserBean().getOrganizationGroups(user.getOrganizationId());
        }
        else
        {
            organizationGroups = SessionControllerGetter.getUserBean().getOrganizationGroups(user);
        }
        // Default: View observation list
        // for everyone
        if(request.getServletPath().endsWith("/observation"))
        {
            if(action == null)
            {
                List<Observation> observations;
                Integer statusTypeId;
                Integer pestOrganismId;
                Set<Integer> organizationGroupId;
                Boolean viewOthersObservations;
                
                // Default period is current season
                Calendar cal = Calendar.getInstance();
                cal.setTime(SystemTime.getSystemTime());
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date timeOfObservationFrom;
                Date timeOfObservationTo;
                Boolean userDefinedFrom = false;
                Boolean userDefinedTo = false;
                try
                {
                    timeOfObservationFrom = format.parse(request.getParameter("timeOfObservationFrom"));
                    userDefinedFrom = true;
                }
                catch(NullPointerException | ParseException ex)
                {
                    cal.set(cal.get(Calendar.YEAR), Calendar.JANUARY, 1, 0, 0, 0);
                    timeOfObservationFrom = cal.getTime();
                }
                try
                {
                    timeOfObservationTo = format.parse(request.getParameter("timeOfObservationTo"));
                    userDefinedTo = true;
                }
                catch(NullPointerException | ParseException ex)
                {
                    cal.set(cal.get(Calendar.YEAR), Calendar.DECEMBER, 31, 23, 59, 59);
                    timeOfObservationTo = cal.getTime();
                }
                
                // If only one date is set, the unset date should not limit the data set
                if(!userDefinedFrom && userDefinedTo)
                {
                    cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 100);
                    timeOfObservationFrom = cal.getTime();
                }
                if(userDefinedFrom && !userDefinedTo)
                {
                    cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + 100);
                    timeOfObservationTo = cal.getTime();
                }
                
                try 
                {
                    statusTypeId = request.getParameter("statusTypeId") != null ? Integer.valueOf(request.getParameter("statusTypeId")) : -1;
                    pestOrganismId = request.getParameter("pestOrganismId") != null ? Integer.valueOf(request.getParameter("pestOrganismId")) : -1;
                    viewOthersObservations = request.getParameter("viewOthersObservations") != null;
                    if(request.getParameterValues("organizationGroupId") != null && request.getParameterValues("organizationGroupId").length > 0)
                    {
                        organizationGroupId = new HashSet<>();
                        for(String id:request.getParameterValues("organizationGroupId"))
                        {
                            organizationGroupId.add(Integer.valueOf(id));
                        }
                    }
                    else
                    {
                        organizationGroupId = null;
                    }
                }
                catch(NumberFormatException nfe) {
                    statusTypeId = null;
                    pestOrganismId = null;
                    viewOthersObservations = false;
                    organizationGroupId = null;
                }
                
                // First: Get observations for organization or user
                if(viewOthersObservations && SessionControllerGetter.getUserBean().authorizeUser(user, 
                        VipsLogicRole.OBSERVATION_AUTHORITY, 
                        VipsLogicRole.ORGANIZATION_ADMINISTRATOR, 
                        VipsLogicRole.SUPERUSER))
                {
                    observations = SessionControllerGetter.getObservationBean().getObservations(user.getOrganizationId().getOrganizationId(), timeOfObservationFrom, timeOfObservationTo);
                }
                else 
                {
                    observations = SessionControllerGetter.getObservationBean().getObservationsForUser(user, timeOfObservationFrom, timeOfObservationTo);
                }
                
                // Then: Filter on other criteria
                if(statusTypeId > 0)
                {
                    final Integer s = statusTypeId; // Local variables used in lambdas must be effectively final
                    observations = observations.stream().
                            filter(obs->obs.getStatusTypeId().equals(s))
                            .collect(Collectors.toList());
                }
                if(pestOrganismId > 0)
                {
                    final Integer p = pestOrganismId;
                    observations = observations.stream()
                            .filter(obs->obs.getOrganismId().equals(p))
                            .collect(Collectors.toList());
                }
                // A bit hairy this one
                // If the user wants to filter on organization groups
                //      Loop through all observations
                //          If the candidate obs has not specified groups, it's OUT
                //          If the candiate obs has specified groups that intersect with filter, it's IN
                //          Otherwise it's OUT
                // If the user has no filter for organization groups, all observations are kept
                if(organizationGroupId != null)
                {
                    final Set<Integer> ogi = organizationGroupId;
                    observations = observations.stream()
                            .filter((Observation obs) -> {
                                List<Integer> availableOrganizationGroupIds = SessionControllerGetter.getObservationBean().getOrganizationGroupIds(obs);
                                if(availableOrganizationGroupIds != null && ! availableOrganizationGroupIds.isEmpty())
                                {
                                    if (availableOrganizationGroupIds.stream().anyMatch((gId) -> (ogi.contains(gId)))) {
                                        return true;
                                    }
                                }
                                return false;
                            })
                            .collect(Collectors.toList());
                }
                
                // Check for form shortcuts
                List<ObservationFormShortcut> shortcuts = SessionControllerGetter.getObservationBean().getObservationFormShortcuts(user.getOrganizationId());
                
                Collections.sort(observations);
                Collections.reverse(observations);
                List<Organism> allPests = em.createNamedQuery("Organism.findAllPests").getResultList();
                request.setAttribute("shortcuts", shortcuts);
                request.setAttribute("allPests", SessionControllerGetter.getOrganismBean().sortOrganismsByLocalName(allPests, SessionLocaleUtil.getCurrentLocale(request).getLanguage()));
                request.setAttribute("hierarchyCategories", SessionControllerGetter.getOrganismBean().getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                request.setAttribute("timeOfObservationFrom", userDefinedFrom ? timeOfObservationFrom : null);
                request.setAttribute("timeOfObservationTo", userDefinedTo ? timeOfObservationTo : null);
                request.setAttribute("selectedPestOrganismId", pestOrganismId);
                request.setAttribute("selectedStatusTypeId", statusTypeId);
                request.setAttribute("viewOthersObservations",viewOthersObservations);
                request.setAttribute("organizationGroups", organizationGroups);
                request.setAttribute("organizationGroupId", organizationGroupId);
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
                        // See if there are presets on the URL string
                        if(request.getParameter("cropOrganismId") != null)
                        {
                            observation.setCropOrganism(em.find(Organism.class, Integer.valueOf(request.getParameter("cropOrganismId"))));
                        }
                        
                        List<Organism> allCrops;
                        if(request.getParameter("organismId") != null)
                        {
                            Integer organismId = Integer.valueOf(request.getParameter("organismId"));
                            observation.setOrganism(em.find(Organism.class, organismId));
                            // If pest has been selected, include only associated crops
                            allCrops = SessionControllerGetter.getOrganismBean().getPestCrops(organismId);
                        }
                        else
                        {
                            allCrops = em.createNamedQuery("Organism.findAllCrops").getResultList();
                        }
                        
                        request.setAttribute("observation", observation);
                        request.setAttribute("shortcut", request.getParameter("observationFormShortcutId") != null ?
                                    em.find(ObservationFormShortcut.class, Integer.valueOf(request.getParameter("observationFormShortcutId")))
                                    : null
                                );
                        request.setAttribute("hideObservationFormMap", request.getParameter("hideObservationFormMap") != null ? request.getParameter("hideObservationFormMap").equals("true") : false);
                        request.setAttribute("noBroadcast", request.getParameter("noBroadcast") != null);
                        request.setAttribute("mapLayers", SessionControllerGetter.getUserBean().getMapLayerJSONForUser(user));
                        request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                        request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
                        request.setAttribute("locationPointOfInterests", SessionControllerGetter.getPointOfInterestBean().getRelevantPointOfInterestsForUser(user));
                        List<Organism> allPests = em.createNamedQuery("Organism.findAllPests").getResultList();
                        request.setAttribute("allPests", SessionControllerGetter.getOrganismBean().sortOrganismsByLocalName(allPests, SessionLocaleUtil.getCurrentLocale(request).getLanguage()));
                        request.setAttribute("allCrops", SessionControllerGetter.getOrganismBean().sortOrganismsByLocalName(allCrops, SessionLocaleUtil.getCurrentLocale(request).getLanguage()));
                        request.setAttribute("selectedOrganizationGroupIds", new ArrayList<>());
                        // Hierarchy categories
                        request.setAttribute("hierarchyCategories", SessionControllerGetter.getOrganismBean().getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                        request.setAttribute("observationMethods", em.createNamedQuery("ObservationMethod.findAll", ObservationMethod.class).getResultList());
                        request.setAttribute("organizationGroups", organizationGroups);
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
                        request.setAttribute("noBroadcast", request.getParameter("noBroadcast") != null);
                        request.setAttribute("mapLayers", SessionControllerGetter.getUserBean().getMapLayerJSONForUser(user));
                        //System.out.println(observation.getGeoinfo());
                        request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                        request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
                        List<Organism> allPests = em.createNamedQuery("Organism.findAllPests").getResultList();
                        request.setAttribute("allPests", SessionControllerGetter.getOrganismBean().sortOrganismsByLocalName(allPests, SessionLocaleUtil.getCurrentLocale(request).getLanguage()));
                        List<Organism> allCrops = em.createNamedQuery("Organism.findAllCrops").getResultList();
                        request.setAttribute("allCrops", SessionControllerGetter.getOrganismBean().sortOrganismsByLocalName(allCrops, SessionLocaleUtil.getCurrentLocale(request).getLanguage()));
                        request.setAttribute("selectedOrganizationGroupIds", SessionControllerGetter.getObservationBean().getOrganizationGroupIds(observation));
                        // Hierarchy categories
                        request.setAttribute("hierarchyCategories", SessionControllerGetter.getOrganismBean().getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                        request.setAttribute("observationMethods", em.createNamedQuery("ObservationMethod.findAll", ObservationMethod.class).getResultList());
                        request.setAttribute("organizationGroups", organizationGroups);
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
                            observation.setLocationIsPrivate(formValidation.getFormField("locationIsPrivate").getWebValue() != null);
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
                            
                            // Checking for organization groups
                            SessionControllerGetter.getObservationBean().storeOrganizationGroupObservationIds(observation, formValidation.getFormField("organizationGroupId").getWebValues());
                            
                            // All transactions finished, we can send notifications
                            // if conditions are met
                            if(sendNotification)
                            {
                                SessionControllerGetter.getMessagingBean().sendUniversalMessage(observation);
                            }

                            // Redirect to form
                            response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://")
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
                        response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://")
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
