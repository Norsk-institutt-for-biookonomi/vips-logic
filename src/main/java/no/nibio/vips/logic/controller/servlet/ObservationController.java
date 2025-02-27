/*
 * Copyright (c) 2017 NIBIO <http://www.nibio.no/>. 
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
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import jakarta.ejb.EJB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import no.nibio.vips.logic.controller.session.ObservationBean;
import no.nibio.vips.logic.controller.session.OrganismBean;
import no.nibio.vips.logic.controller.session.PointOfInterestBean;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.Observation;
import no.nibio.vips.logic.entity.ObservationFormShortcut;
import no.nibio.vips.logic.entity.ObservationMethod;
import no.nibio.vips.logic.entity.Organism;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.OrganizationGroup;
import no.nibio.vips.logic.entity.PolygonService;
import no.nibio.vips.logic.entity.VipsLogicRole;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.messaging.MessagingBean;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.DateUtil;
import no.nibio.vips.util.DateUtilException;
import no.nibio.vips.util.ExceptionUtil;
import no.nibio.vips.util.ServletUtil;
import no.nibio.web.forms.FormUtil;
import no.nibio.web.forms.FormValidation;
import no.nibio.web.forms.FormValidationException;
import no.nibio.web.forms.FormValidator;

/**
 * @copyright 2014-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class ObservationController extends HttpServlet {
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    @EJB
    UserBean userBean;
    @EJB
    ObservationBean observationBean;
    @EJB
    MessagingBean messagingBean;
    @EJB
    OrganismBean organismBean;
    @EJB
    PointOfInterestBean pointOfInterestBean;
    
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
           organizationGroups =  userBean.getOrganizationGroups();
        }
        else if(user.isOrganizationAdmin())
        {
            organizationGroups = userBean.getOrganizationGroups(user.getOrganizationId());
        }
        else
        {
            organizationGroups = userBean.getOrganizationGroups(user);
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
                    //nfe.printStackTrace();
                    statusTypeId = -1;
                    pestOrganismId = -1;
                    viewOthersObservations = false;
                    organizationGroupId = null;
                }
                
                // First: Get observations for organization or user
                if(viewOthersObservations && userBean.authorizeUser(user, 
                        VipsLogicRole.OBSERVATION_AUTHORITY, 
                        VipsLogicRole.ORGANIZATION_ADMINISTRATOR, 
                        VipsLogicRole.SUPERUSER))
                {
                    observations = observationBean.getObservations(user.getOrganizationId().getOrganizationId(), timeOfObservationFrom, timeOfObservationTo);
                }
                else 
                {
                    observations = observationBean.getObservationsForUser(user, timeOfObservationFrom, timeOfObservationTo);
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
                                List<Integer> availableOrganizationGroupIds = observationBean.getOrganizationGroupIds(obs);
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
                List<ObservationFormShortcut> shortcuts = observationBean.getObservationFormShortcuts(user.getOrganizationId());
                
                Collections.sort(observations);
                Collections.reverse(observations);
                //observations.forEach(o->System.out.println(o));
                List<Organism> allPests = em.createNamedQuery("Organism.findAllPests").getResultList();
                request.setAttribute("shortcuts", shortcuts);
                request.setAttribute("allPests", organismBean.sortOrganismsByLocalName(allPests, SessionLocaleUtil.getCurrentLocale(request).getLanguage()));
                request.setAttribute("hierarchyCategories", organismBean.getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                request.setAttribute("timeOfObservationFrom", userDefinedFrom ? timeOfObservationFrom : null);
                request.setAttribute("timeOfObservationTo", userDefinedTo ? timeOfObservationTo : null);
                request.setAttribute("selectedPestOrganismId", pestOrganismId);
                request.setAttribute("selectedStatusTypeId", statusTypeId);
                request.setAttribute("viewOthersObservations",viewOthersObservations);
                request.setAttribute("organizationGroups", organizationGroups);
                request.setAttribute("organizationGroupId", organizationGroupId);
                request.setAttribute("observations", observations);
                request.setAttribute("userHasObserverPrivilege", userBean.authorizeUser(user, VipsLogicRole.OBSERVER, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER));
                request.setAttribute("userIsObservationAuthority", userBean.authorizeUser(user, VipsLogicRole.OBSERVATION_AUTHORITY, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER));
                // If this is a redirect from a controller, with a message to be passed on
                request.setAttribute("messageKey", request.getParameter("messageKey"));
                request.getRequestDispatcher("/observationList.ftl").forward(request, response);
            }
            // Create a new observation
            // Authorization: ORGANIZATION ADMIN, OBSERVER or SUPERUSER
            else if(action.equals("newObservationForm"))
            {
                if(userBean.authorizeUser(user, VipsLogicRole.OBSERVER, VipsLogicRole.OBSERVATION_AUTHORITY, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
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
                            allCrops = organismBean.getPestCrops(organismId);
                        }
                        else
                        {
                            allCrops = em.createNamedQuery("Organism.findAllCrops").getResultList();
                        }
                        
                        // Get the polygonServices
                        List<PolygonService> polygonServices = observationBean.getPolygonServicesForOrganization(user.getOrganization_id());
                        
                        request.setAttribute("observation", observation);
                        request.setAttribute("polygonServices", polygonServices);
                        request.setAttribute("locationVisibilityFormValue", ObservationController.LOCATION_VISIBILITY_FORM_VALUE_PUBLIC);
                        request.setAttribute("shortcut", request.getParameter("observationFormShortcutId") != null ?
                                    em.find(ObservationFormShortcut.class, Integer.valueOf(request.getParameter("observationFormShortcutId")))
                                    : null
                                );
                        request.setAttribute("hideObservationFormMap", request.getParameter("hideObservationFormMap") != null ? request.getParameter("hideObservationFormMap").equals("true") : false);
                        request.setAttribute("noBroadcast", request.getParameter("noBroadcast") != null);
                        request.setAttribute("mapLayers", userBean.getMapLayerJSONForUser(user));
                        request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                        request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
                        request.setAttribute("locationPointOfInterests", pointOfInterestBean.getRelevantPointOfInterestsForUser(user));
                        List<Organism> allPests = em.createNamedQuery("Organism.findAllPests").getResultList();
                        request.setAttribute("allPests", organismBean.sortOrganismsByLocalName(allPests, SessionLocaleUtil.getCurrentLocale(request).getLanguage()));
                        request.setAttribute("allCrops", organismBean.sortOrganismsByLocalName(allCrops, SessionLocaleUtil.getCurrentLocale(request).getLanguage()));
                        request.setAttribute("selectedOrganizationGroupIds", new ArrayList<>());
                        // Hierarchy categories
                        request.setAttribute("hierarchyCategories", organismBean.getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                        request.setAttribute("observationMethods", em.createNamedQuery("ObservationMethod.findAll", ObservationMethod.class).getResultList());
                        request.setAttribute("organizationGroups", organizationGroups);
                        request.setAttribute("editAccess", "W"); // User always has edit access to new observation
                        if(userBean.authorizeUser(user, VipsLogicRole.OBSERVATION_AUTHORITY, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
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
                if(userBean.authorizeUser(user, VipsLogicRole.OBSERVER, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    try
                    {
                        Integer observationId = Integer.valueOf(request.getParameter("observationId"));
                        Observation observation = observationBean.getObservation(observationId);//em.find(Observation.class, observationId);
                        List<PolygonService> polygonServices = observationBean.getPolygonServicesForOrganization(user.getOrganization_id());
                        request.setAttribute("locationVisibilityFormValue", this.getLocationVisibilityFormValue(observation));
                        request.setAttribute("observation", observation);
                        request.setAttribute("polygonServices", polygonServices);
                        request.setAttribute("noBroadcast", request.getParameter("noBroadcast") != null);
                        request.setAttribute("mapLayers", userBean.getMapLayerJSONForUser(user));
                        //System.out.println(observation.getGeoinfo());
                        request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                        request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
                        List<Organism> allPests = em.createNamedQuery("Organism.findAllPests").getResultList();
                        request.setAttribute("allPests", organismBean.sortOrganismsByLocalName(allPests, SessionLocaleUtil.getCurrentLocale(request).getLanguage()));
                        List<Organism> allCrops = em.createNamedQuery("Organism.findAllCrops").getResultList();
                        request.setAttribute("allCrops", organismBean.sortOrganismsByLocalName(allCrops, SessionLocaleUtil.getCurrentLocale(request).getLanguage()));
                        request.setAttribute("selectedOrganizationGroupIds", observationBean.getOrganizationGroupIds(observation));
                        // Hierarchy categories
                        request.setAttribute("hierarchyCategories", organismBean.getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                        request.setAttribute("observationMethods", em.createNamedQuery("ObservationMethod.findAll", ObservationMethod.class).getResultList());
                        request.setAttribute("organizationGroups", organizationGroups);
                        if(userBean.authorizeUser(user, VipsLogicRole.OBSERVATION_AUTHORITY, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                        {
                            request.setAttribute("statusTypeIds", em.createNamedQuery("ObservationStatusType.findAll").getResultList());
                        }
                        if(request.getParameter("messageKey") != null)
                        {
                            request.setAttribute("messageKey",request.getParameter("messageKey"));
                        }
                        
                        // Determine the edit access
                        String editAccess = "R"; // Read access only
                        if(observation.getUserId().equals(user.getUserId()) || user.isSuperUser() || user.isOrganizationAdmin())
                        {
                            editAccess = "W";
                        }
                        else if(user.isObservationAuthority())
                        {
                            editAccess = "A"; // Approve
                        }
                        request.setAttribute("editAccess", editAccess);
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
                if(userBean.authorizeUser(user, VipsLogicRole.OBSERVER, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    try
                    {
                        Map<String,String[]> parameterMap;
                        List<FileItem> items = null;
                        if(JakartaServletFileUpload.isMultipartContent(request))
                        {
                           
                            // Create a new file upload handler
                            DiskFileItemFactory dfif = DiskFileItemFactory.builder().get();
                            JakartaServletFileUpload upload = new JakartaServletFileUpload(dfif);

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
                            observation.setTimeOfObservation(formValidation.getFormField("timeOfObservation").getValueAsTimestamp());
                            if(observationId <= 0)
                            {
                                observation.setUserId(user.getUserId());
                            }
                            else
                            {
                                observation.setLastEditedBy(user.getUserId());
                            }
                            observation.setLastEditedTime(new Date());
                            observation.setObservationHeading(formValidation.getFormField("observationHeading").getWebValue());
                            observation.setObservationText(formValidation.getFormField("observationText").getWebValue());
                            observation.setObservationData(
                                    formValidation.getFormField("observationData").getWebValue().isEmpty() ?
                                            null
                                            : formValidation.getFormField("observationData").getWebValue()
                            );
                            observation.setIsPositive(formValidation.getFormField("isPositive").getWebValue() != null);
                            observation.setIsQuantified(formValidation.getFormField("isQuantified").getWebValue() != null);
                            this.setObservationLocationVisibility(observation, formValidation.getFormField("locationVisibility").getWebValue());
                            observation.setBroadcastMessage(formValidation.getFormField("broadcastMessage").getWebValue() != null);

                            boolean sendNotification = false;    
                            // Storing approval status
                            if(userBean.authorizeUser(user, VipsLogicRole.OBSERVATION_AUTHORITY, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
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
                            
                            observation = observationBean.storeObservation(observation);

                            // Image handling
                            // Delete the current illustration
                            String[] deleteIllustrations = parameterMap.get("deleteIllustration");
                            if(deleteIllustrations != null && deleteIllustrations.length > 0)
                            {
                                observation = observationBean.deleteObservationIllustration(observation, deleteIllustrations);
                            }

                            // Store the new illustration (replaces former illustration if not already deleted)
                            if(items != null)
                            {
                                for(FileItem item:items)
                                {
                                    if(!item.isFormField() && item.getSize() > 0)
                                    {
                                        observation = observationBean.storeObservationIllustration(observation, item);
                                    }
                                }
                            }
                            
                            // Checking for organization groups
                            observationBean.storeOrganizationGroupObservationIds(observation, formValidation.getFormField("organizationGroupId").getWebValues());
                            
                            // All transactions finished, we can send notifications
                            // if conditions are met
                            if(sendNotification && !
                                    (System.getProperty("DISABLE_MESSAGING_SYSTEM") != null && System.getProperty("DISABLE_MESSAGING_SYSTEM").equals("true"))
                                    )
                            {
                                messagingBean.sendUniversalMessage(observation);
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
                            request.setAttribute("hierarchyCategories", organismBean.getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
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
                if(userBean.authorizeUser(user, VipsLogicRole.OBSERVER, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
                {
                    try
                    {
                        Integer observationId = Integer.valueOf(request.getParameter("observationId"));
                        observationBean.deleteObservation(observationId);

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
            if(userBean.authorizeUser(user, 
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
                        request.setAttribute("organizations", userBean.getOrganizations());
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
                    request.setAttribute("periodDays", new DateUtil().getDaysBetween(from, to));
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
                catch(FormValidationException | DateUtilException ex)
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
    
    public static final String LOCATION_VISIBILITY_FORM_VALUE_PRIVATE = "private";
    public static final String LOCATION_VISIBILITY_FORM_VALUE_PUBLIC = "public";
    public static final String LOCATION_VISIBILITY_FORM_VALUE_MASK_PREFIX = "mask_";
    
    private String getLocationVisibilityFormValue(Observation observation)
    {
        // Private is private, no matter what
        if(observation.getLocationIsPrivate())
        {
            return ObservationController.LOCATION_VISIBILITY_FORM_VALUE_PRIVATE;
        }
        // Public can either be completely public or masked by a polygon layer
        // e.g. county borders
        if(observation.getPolygonService() == null)
        {
            return ObservationController.LOCATION_VISIBILITY_FORM_VALUE_PUBLIC;
        }
        return ObservationController.LOCATION_VISIBILITY_FORM_VALUE_MASK_PREFIX + observation.getPolygonService().getPolygonServiceId();
    }
    
    private void setObservationLocationVisibility(Observation observation, String formValue)
    {
        // Private is private, no matter what
        observation.setLocationIsPrivate(formValue.equals(ObservationController.LOCATION_VISIBILITY_FORM_VALUE_PRIVATE));
        observation.setPolygonService(
                // If private or public, set no polygon service
                formValue.equals(ObservationController.LOCATION_VISIBILITY_FORM_VALUE_PRIVATE) || formValue.equals(ObservationController.LOCATION_VISIBILITY_FORM_VALUE_PUBLIC)
                ? null
                // Otherwise, set selected polygon service
                : em.find(PolygonService.class, Integer.valueOf(formValue.split("_")[1]))              
        );
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
