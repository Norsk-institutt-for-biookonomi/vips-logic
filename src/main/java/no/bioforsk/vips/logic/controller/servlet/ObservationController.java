/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with VIPSLogic.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.controller.servlet;

import java.io.IOException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.bioforsk.vips.logic.entity.Observation;
import no.bioforsk.vips.logic.entity.ObservationMethod;
import no.bioforsk.vips.logic.entity.Organism;
import no.bioforsk.vips.logic.entity.VipsLogicRole;
import no.bioforsk.vips.logic.entity.VipsLogicUser;
import no.bioforsk.vips.logic.i18n.SessionLocaleUtil;
import no.bioforsk.vips.logic.util.SessionControllerGetter;
import no.bioforsk.vips.util.ExceptionUtil;
import no.bioforsk.vips.util.ServletUtil;
import no.bioforsk.web.forms.FormValidation;
import no.bioforsk.web.forms.FormValidationException;
import no.bioforsk.web.forms.FormValidator;

/**
 * @copyright 2014 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
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
        if(action == null)
        {
            List<Observation> observations = SessionControllerGetter.getObservationBean().getObservations(user.getOrganizationId().getOrganizationId());
            request.setAttribute("observations", observations);
            request.setAttribute("userHasObserverPrivilege", SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.OBSERVER, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER));
            // If this is a redirect from a controller, with a message to be passed on
            request.setAttribute("messageKey", request.getParameter("messageKey"));
            request.getRequestDispatcher("/observationList.ftl").forward(request, response);
        }
        // Create a new observation
        // Authorization: ORGANIZATION ADMIN, OBSERVER or SUPERUSER
        else if(action.equals("newObservationForm"))
        {
            if(SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.OBSERVER, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
            {
                try
                {
                    Observation observation = new Observation();
                    request.setAttribute("observation", observation);
                    request.setAttribute("defaultMapCenter",user.getOrganizationId().getDefaultMapCenter());
                    request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
                    List<Organism> allOrganisms = em.createNamedQuery("Organism.findAll").getResultList();
                    request.setAttribute("allOrganisms", allOrganisms);
                    // Hierarchy categories
                    request.setAttribute("hierarchyCategories", SessionControllerGetter.getOrganismBean().getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                    request.setAttribute("observationMethods", em.createNamedQuery("ObservationMethod.findAll", ObservationMethod.class).getResultList());
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
                    Observation observation = em.find(Observation.class, observationId);
                    request.setAttribute("observation", observation);
                    List<Organism> allOrganisms = em.createNamedQuery("Organism.findAll").getResultList();
                    request.setAttribute("allOrganisms", allOrganisms);
                    // Hierarchy categories
                    request.setAttribute("hierarchyCategories", SessionControllerGetter.getOrganismBean().getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                    request.setAttribute("observationMethods", em.createNamedQuery("ObservationMethod.findAll", ObservationMethod.class).getResultList());
                    if(request.getParameter("messageKey") != null)
                    {
                        request.setAttribute("messageKey",request.getParameter("messageKey"));
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
        // Store an observation
        // Authorization: ORGANIZATION ADMIN, OBSERVER or SUPERUSER
        else if(action.equals("observationFormSubmit"))
        {
            if(SessionControllerGetter.getUserBean().authorizeUser(user, VipsLogicRole.OBSERVER, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
            {
                try
                {
                    Integer observationId = Integer.valueOf(request.getParameter("observationId"));
                    Observation observation = observationId > 0 ? em.find(Observation.class, observationId) : new Observation();
                    FormValidation formValidation = FormValidator.validateForm("observationForm", request, getServletContext());
                    if(formValidation.isValid())
                    {
                        // Storing observation
                        observation.setOrganism(em.find(Organism.class, formValidation.getFormField("organismId").getValueAsInteger()));
                        observation.setDenominator(formValidation.getFormField("denominator").getValueAsInteger());
                        observation.setObservationMethodId(em.find(ObservationMethod.class, formValidation.getFormField("observationMethodId").getWebValue()));
                        observation.setTimeOfObservation(formValidation.getFormField("timeOfObservation").getValueAsTimestamp());
                        observation.setObservedValue(formValidation.getFormField("observedValue").getValueAsDouble());
                        observation.setUserId(user.getUserId());
                        observation.setLocation(formValidation.getFormField("location").getValueAsPointWGS84());
                        observation = SessionControllerGetter.getObservationBean().storeObservation(observation);
                        
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
