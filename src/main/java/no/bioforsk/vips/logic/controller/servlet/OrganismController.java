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
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.bioforsk.vips.logic.controller.session.UserBean;
import no.bioforsk.vips.logic.entity.Organism;
import no.bioforsk.vips.logic.entity.OrganismExternalResource;
import no.bioforsk.vips.logic.entity.OrganismExternalResourcePK;
import no.bioforsk.vips.logic.entity.VipsLogicUser;
import no.bioforsk.vips.logic.i18n.SessionLocaleUtil;
import no.bioforsk.vips.logic.util.SessionControllerGetter;
import no.bioforsk.vips.util.ExceptionUtil;
import no.bioforsk.vips.util.ServletUtil;
import no.bioforsk.web.forms.FormField;
import no.bioforsk.web.forms.FormValidation;
import no.bioforsk.web.forms.FormValidationException;
import no.bioforsk.web.forms.FormValidator;

/**
 * @copyright 2014 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class OrganismController extends HttpServlet {
   
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
        UserBean userBean = SessionControllerGetter.getUserBean();
        
        // Default: View top organisms
        // for everyone
        if(action == null || action.equals("listChildOrganisms"))
        {
            try
            {
                Organism organism;
                if(     action == null 
                        || (request.getParameter("organismId") != null && (request.getParameter("organismId").isEmpty() || request.getParameter("organismId").equals("null")))
                )
                {
                    organism = new Organism();
                    organism.setChildOrganisms(SessionControllerGetter.getOrganismBean().getTopLevelOrganisms());
                }
                else
                {
                    Integer organismId = Integer.valueOf(request.getParameter("organismId"));
                    organism = SessionControllerGetter.getOrganismBean().getOrganismAndChildrenTwoLevels(organismId);
                }
                request.setAttribute("organism", organism);
                // Check if any of the child organisms has trade name
                Boolean tradeNamePresent = Boolean.FALSE;
                for(Organism child:organism.getChildOrganisms())
                {
                    if(child.getTradeName() != null && !child.getTradeName().isEmpty())
                    {
                        tradeNamePresent = Boolean.TRUE;
                    }
                }
                request.setAttribute("tradeNamePresent", tradeNamePresent);
                // Check if message has been passed on
                request.setAttribute("messageKey", request.getParameter("messageKey"));
                // Delegate to template
                request.getRequestDispatcher("/organismList.ftl").forward(request, response);
            }
            catch(NullPointerException | NumberFormatException ex)
            {
                if(ex instanceof NumberFormatException)
                {
                    response.sendError(500, "Invalid organism id " + request.getParameter("organismId"));
                }
                else
                {
                    response.sendError(500, ex.getMessage());
                }
            }
        }
        else if(action.equals("viewOrganism"))
        {
            try
            {
                
                Integer organismId = Integer.valueOf(request.getParameter("organismId"));
                Organism organism = em.find(Organism.class, organismId);
                request.setAttribute("organism", organism);
                // Hierarchy categories
                request.setAttribute("hierarchyCategories", SessionControllerGetter.getOrganismBean().getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                request.setAttribute("parentOrganism", organism.getParentOrganismId() != null ? em.find(Organism.class, organism.getParentOrganismId()) : null);
                request.getRequestDispatcher("/organismDetails.ftl").forward(request, response);
            }
            catch(NullPointerException | NumberFormatException ex)
            {
                if(ex instanceof NumberFormatException)
                {
                    response.sendError(500, "Invalid organism id " + request.getParameter("organismId"));
                }
                else
                {
                    response.sendError(500, ExceptionUtil.getStackTrace(ex));
                }
            }
        }
        else if(action.equals("editOrganismForm"))
        {
            try
            {
                
                Integer organismId = Integer.valueOf(request.getParameter("organismId"));
                Organism organism = em.find(Organism.class, organismId);
                request.setAttribute("organism", organism);
                // All organisms used for parent organism list
                List<Organism> allOrganisms = em.createNamedQuery("Organism.findAll").getResultList();
                request.setAttribute("allOrganisms", allOrganisms);
                // Hierarchy categories
                request.setAttribute("hierarchyCategories", SessionControllerGetter.getOrganismBean().getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                // Finding all external resources where entry is missing
                request.setAttribute("unreferencedExternalResources", SessionControllerGetter.getOrganismBean().getUnusedExternalResourcesForOrganism(organism));
                request.getRequestDispatcher("/organismForm.ftl").forward(request, response);
            }
            catch(NullPointerException | NumberFormatException ex)
            {
                response.sendError(500, "Invalid organism id " + request.getParameter("organismId"));
            }
        }
        else if(action.equals("newOrganismForm"))
        {
            try
            {
                Integer parentOrganismId = null;
                if(request.getParameter("parentOrganismId") != null && !request.getParameter("parentOrganismId").equals("null") && ! request.getParameter("parentOrganismId").isEmpty())
                {
                    parentOrganismId = Integer.valueOf(request.getParameter("parentOrganismId"));
                }
                request.setAttribute("parentOrganismId", parentOrganismId);
                Organism organism = new Organism();
                request.setAttribute("organism", organism);
                request.setAttribute("allOrganisms", em.createNamedQuery("Organism.findAll").getResultList());
                // Hierarchy categories
                request.setAttribute("hierarchyCategories", SessionControllerGetter.getOrganismBean().getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                
                // Finding all external resources where entry is missing
                request.setAttribute("unreferencedExternalResources", SessionControllerGetter.getOrganismBean().getUnusedExternalResourcesForOrganism(organism));

                request.getRequestDispatcher("/organismForm.ftl").forward(request, response);
            }
            catch(NullPointerException | NumberFormatException ex)
            {
                response.sendError(500, "Invalid organism id " + request.getParameter("organismId"));
            }
        }
        else if(action.equals("organismFormSubmit"))
        {
            try
            {
                Integer organismId = Integer.valueOf(request.getParameter("organismId"));
                Organism organism = organismId > 0 ? em.find(Organism.class, organismId) : new Organism();
                FormValidation formValidation = FormValidator.validateForm("organismForm",request,getServletContext());
                if(formValidation.isValid())
                {
                    organism.setIsCrop(formValidation.getFormField("isCrop").getWebValue() != null);
                    organism.setIsPest(formValidation.getFormField("isPest").getWebValue() != null);
                    organism.setLatinName(formValidation.getFormField("latinName").getWebValue());
                    organism.setTradeName(formValidation.getFormField("tradeName").getWebValue());
                    organism.setHierarchyCategoryId(formValidation.getFormField("hierarchyCategoryId").getValueAsInteger());
                    organism.setParentOrganismId(formValidation.getFormField("parentOrganismId").getValueAsInteger());
                    
                    organism = SessionControllerGetter.getOrganismBean().storeOrganismWithLocalName(
                            organism,
                            formValidation.getFormField("localName").getWebValue(),
                            SessionLocaleUtil.getCurrentLocale(request)
                            );
                    
                    //System.out.println(formValidation.getFormFields().toString());
                    Map<String, FormField> externalResourceIdentifiers = formValidation.getMultipleMapFormFields().get("externalResourceIdentifier");
                    for(String key:externalResourceIdentifiers.keySet())
                    {
                        FormField identifierField = externalResourceIdentifiers.get(key);
                        if(identifierField.getWebValue() == null || identifierField.getWebValue().isEmpty())
                        {
                            continue;
                        }
                        Integer externalResourceId = Integer.valueOf(key);
                        OrganismExternalResource organismExternalResource = new OrganismExternalResource();
                        OrganismExternalResourcePK pk = new OrganismExternalResourcePK(organism.getOrganismId(), externalResourceId);
                        organismExternalResource.setOrganismExternalResourcePK(pk);
                        organismExternalResource.setResourceIdentifier(identifierField.getWebValue());
                        SessionControllerGetter.getOrganismBean().storeOrganismExternalResource(organismExternalResource);
                    }
                    // Need to refresh organism after storing the external resources
                    Organism refreshedOrganism = em.find(Organism.class, organism.getOrganismId());
                    request.setAttribute("organism", refreshedOrganism);
                    request.setAttribute("allOrganisms", em.createNamedQuery("Organism.findAll").getResultList());
                    // Hierarchy categories
                    request.setAttribute("hierarchyCategories", SessionControllerGetter.getOrganismBean().getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                    // Finding all external resources where entry is missing
                    request.setAttribute("unreferencedExternalResources", SessionControllerGetter.getOrganismBean().getUnusedExternalResourcesForOrganism(refreshedOrganism));
                    request.setAttribute("messageKey", organismId > 0 ? "organismUpdated" : "organismRegistered");
                    request.getRequestDispatcher("/organismForm.ftl").forward(request, response);
                }
                else
                {
                    request.setAttribute("formValidation", formValidation);
                    request.setAttribute("organism", organism);
                    // Finding all external resources where entry is missing
                    request.setAttribute("unreferencedExternalResources", SessionControllerGetter.getOrganismBean().getUnusedExternalResourcesForOrganism(organism));
                    request.getRequestDispatcher("/organismForm.ftl").forward(request, response);
                }
            }
            catch(NullPointerException | NumberFormatException | FormValidationException ex)
            {
                if(ex instanceof NumberFormatException)
                {
                    response.sendError(500, "Invalid organism id " + request.getParameter("organismId"));
                }
                else if(ex instanceof FormValidationException)
                {
                    response.sendError(500, "Form validation exception: " + ex.getMessage());
                }
                else
                {
                    ex.printStackTrace();
                    response.sendError(500, ex.getMessage());
                }
            }
        }
        else if(action.equals("deleteOrganism"))
        {
            try
            {
                Integer organismId = Integer.valueOf(request.getParameter("organismId"));
                Organism organism = em.find(Organism.class, organismId);
                String parentOrganismId = organism.getParentOrganismId() != null ? String.valueOf(organism.getParentOrganismId()) : "null";
                if(SessionControllerGetter.getOrganismBean().deleteOrganism(organismId))
                {
                    // Route back to where we were with confirmation message
                    response.sendRedirect(new StringBuilder("http://").append(ServletUtil.getServerName(request)).append("/organism?action=listChildOrganisms&organismId=").append(parentOrganismId).append("&messageKey=organismDeleted").toString());
                }
                else
                {
                    // Route back to where we were with error message
                    response.sendRedirect("/organism?action=listChildOrganisms&organismId=" + parentOrganismId + "&errorMessageKey=organismNotDeleted");
                }
            }
            catch(NullPointerException | NumberFormatException ex)
            {
                if(ex instanceof NumberFormatException)
                {
                    response.sendError(500, "Invalid organism id " + request.getParameter("organismId"));
                }
                else
                {
                    response.sendError(500, ex.getMessage());
                }
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
