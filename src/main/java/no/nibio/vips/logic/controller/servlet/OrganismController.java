/*
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import no.nibio.vips.logic.controller.session.OrganismBean;
import no.nibio.vips.logic.entity.CropPest;
import no.nibio.vips.logic.entity.Organism;
import no.nibio.vips.logic.entity.OrganismExternalResource;
import no.nibio.vips.logic.entity.OrganismExternalResourcePK;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.util.ExceptionUtil;
import no.nibio.vips.util.ServletUtil;
import no.nibio.web.forms.FormField;
import no.nibio.web.forms.FormUtil;
import no.nibio.web.forms.FormValidation;
import no.nibio.web.forms.FormValidationException;
import no.nibio.web.forms.FormValidator;

/**
 * @copyright 2016-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class OrganismController extends HttpServlet {
   
    @Inject
    EntityManager em;
    
    @Inject
    OrganismBean organismBean;

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
                    organism.setChildOrganisms(organismBean.getTopLevelOrganisms());
                }
                else
                {
                    Integer organismId = Integer.valueOf(request.getParameter("organismId"));
                    organism = organismBean.getOrganismAndChildrenTwoLevels(organismId);
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
        else if(action.equals("listCrops"))
        {
            
            List<Organism> allCrops = em.createNamedQuery("Organism.findAllCrops").getResultList();
            Map<String,Organism> allPestsMapped = organismBean.getAllPestsMapped();
            Map<String, CropPest> cropPestsMap = organismBean.getCropPestsMapped();
            request.setAttribute("allCrops", 
                    organismBean.sortOrganismsByLocalName(
                        allCrops, 
                        SessionLocaleUtil.getCurrentLocale(request).getLanguage())
            );
            request.setAttribute("allPestsMapped", allPestsMapped);
            request.setAttribute("cropPestsMap", cropPestsMap);
            request.setAttribute("hierarchyCategories", organismBean.getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
            request.getRequestDispatcher("/cropList.ftl").forward(request, response);
        }
        else if(action.equals("listPests"))
        {
            List<Organism> allPests = em.createNamedQuery("Organism.findAllPests").getResultList();
            request.setAttribute("allPests", 
                    organismBean.sortOrganismsByLocalName(
                            allPests, 
                            SessionLocaleUtil.getCurrentLocale(request).getLanguage())
            );
            request.setAttribute("hierarchyCategories", organismBean.getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
            request.getRequestDispatcher("/pestList.ftl").forward(request, response);
        }
        else if(action.equals("viewOrganism"))
        {
            try
            {
                Integer organismId = Integer.valueOf(request.getParameter("organismId"));
                Organism organism = em.find(Organism.class, organismId);
                request.setAttribute("organism", organism);
                
                // Hierarchy categories
                request.setAttribute("hierarchyCategories", organismBean.getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
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
                if(organism.getIsCrop())
                {
                    request.setAttribute("cropCategories", organismBean.getCropCategories(user.getOrganizationId().getOrganizationId()));
                }
                // All organisms used for parent organism list
                List<Organism> allOrganisms = em.createNamedQuery("Organism.findAll").getResultList();
                request.setAttribute("allOrganisms", allOrganisms);
                // Hierarchy categories
                request.setAttribute("hierarchyCategories", organismBean.getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                // Finding all external resources where entry is missing
                request.setAttribute("unreferencedExternalResources", organismBean.getUnusedExternalResourcesForOrganism(organism));
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
                request.setAttribute("hierarchyCategories", organismBean.getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                
                // Finding all external resources where entry is missing
                request.setAttribute("unreferencedExternalResources", organismBean.getUnusedExternalResourcesForOrganism(organism));

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
                    
                    organism = organismBean.storeOrganismWithLocalName(
                            organism,
                            formValidation.getFormField("localName").getWebValue(),
                            SessionLocaleUtil.getCurrentLocale(request)
                            );
                    
                    Set<Integer> cropCategoryIds = null;
                    if(formValidation.getFormField("cropCategoryIds").getWebValues() != null)
                    {
                       cropCategoryIds = new HashSet<>();
                       for(String idStr:formValidation.getFormField("cropCategoryIds").getWebValues())
                       {
                           cropCategoryIds.add(Integer.valueOf(idStr));
                       }
                    }
                    organismBean.storeOrganismCropCategories(user.getOrganizationId().getOrganizationId(), organism, cropCategoryIds);
                    
                    //System.out.println(formValidation.getFormFields().toString());
                    if(formValidation.getMultipleMapFormFields().get("externalResourceIdentifier") != null)
                    {
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
                            organismBean.storeOrganismExternalResource(organismExternalResource);
                        }
                    }
                    // Need to refresh organism after storing the external resources
                    Organism refreshedOrganism = em.find(Organism.class, organism.getOrganismId());
                    request.setAttribute("organism", refreshedOrganism);
                    if(refreshedOrganism.getIsCrop())
                    {
                        request.setAttribute("cropCategories", organismBean.getCropCategories(user.getOrganizationId().getOrganizationId()));
                    }
                    request.setAttribute("allOrganisms", em.createNamedQuery("Organism.findAll").getResultList());
                    // Hierarchy categories
                    request.setAttribute("hierarchyCategories", organismBean.getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                    // Finding all external resources where entry is missing
                    request.setAttribute("unreferencedExternalResources", organismBean.getUnusedExternalResourcesForOrganism(refreshedOrganism));
                    request.setAttribute("messageKey", organismId > 0 ? "organismUpdated" : "organismRegistered");
                    request.getRequestDispatcher("/organismForm.ftl").forward(request, response);
                }
                else
                {
                    request.setAttribute("formValidation", formValidation);
                    request.setAttribute("organism", organism);
                    // Finding all external resources where entry is missing
                    request.setAttribute("unreferencedExternalResources", organismBean.getUnusedExternalResourcesForOrganism(organism));
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
                if(organismBean.deleteOrganism(organismId))
                {
                    // Route back to where we were with confirmation message
                    response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://").append(ServletUtil.getServerName(request)).append("/organism?action=listChildOrganisms&organismId=").append(parentOrganismId).append("&messageKey=organismDeleted").toString());
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
        else if(action.equals("editCropPest"))
        {
            Integer cropOrganismId = null;
            try
            {
                cropOrganismId = Integer.valueOf(request.getParameter("cropOrganismId"));
            }
            catch(NullPointerException | NumberFormatException ex){}
            
            List<Organism> allPests = em.createNamedQuery("Organism.findAllPests").getResultList();
            request.setAttribute("allPests", allPests);
            List<Organism> allCrops = em.createNamedQuery("Organism.findAllCrops").getResultList();
            request.setAttribute("allCrops", allCrops);
            if(cropOrganismId != null && cropOrganismId > 0)
            {
                request.setAttribute("cropOrganismId", cropOrganismId);
                CropPest cropPest = organismBean.getCropPest(cropOrganismId);
                request.setAttribute("cropPest", cropPest);
            }
            request.setAttribute("hierarchyCategories", organismBean.getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
            request.setAttribute("messageKey", request.getParameter("messageKey"));
            request.getRequestDispatcher("/cropPestForm.ftl").forward(request, response);
        }
        else if(action.equals("submitCropPest"))
        {
            try
            {
                
                FormValidation formValidation = FormValidator.validateForm("cropPestForm",request,getServletContext());
                if(formValidation.isValid())
                {
                    Integer cropOrganismId = formValidation.getFormField("cropOrganismId").getValueAsInteger();
                    boolean includeAllChildCrops = formValidation.getFormField("includeAllChildCrops").getWebValue() != null;
                    List<Integer> pestOrganismIds = FormUtil.getIdsFromMultipleSelect(formValidation.getFormField("pestOrganismIds").getWebValues());
                    CropPest cropPest = em.find(CropPest.class, cropOrganismId);
                    if(cropPest == null)
                    {
                        cropPest = new CropPest();
                        cropPest.setCropOrganismId(cropOrganismId);
                    }
                    
                    cropPest.setIncludeAllChildCrops(includeAllChildCrops);
                    cropPest.setPestOrganismIds(pestOrganismIds);
                    
                    organismBean.storeCropPest(cropPest);
                    
                    response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://").append(ServletUtil.getServerName(request)).append("/organism?action=editCropPest&cropOrganismId=").append(cropOrganismId).append("&messageKey=cropPestUpdated").toString());
                }
                    
                
            }
            catch(NullPointerException | NumberFormatException | FormValidationException ex)
            {
                ex.printStackTrace();
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
