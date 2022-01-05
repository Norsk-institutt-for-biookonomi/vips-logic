/*
 * Copyright (c) 2022 NIBIO <http://www.nibio.no/>. 
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.nibio.vips.logic.controller.session.OrganismBean;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.CropCategory;
import no.nibio.vips.logic.entity.Organism;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.util.ServletUtil;
import no.nibio.web.forms.FormUtil;
import no.nibio.web.forms.FormValidation;
import no.nibio.web.forms.FormValidationException;
import no.nibio.web.forms.FormValidator;

/**
 * @copyright 2016-2022 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class CropCategoryController extends HttpServlet {
   
    @EJB
    UserBean userBean;
    
    @EJB
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
        // Default: View crop category list
        // for everyone
        if(action == null)
        {
            Organization currentOrganization = user.isSuperUser() && request.getParameter("organizationId") != null ? 
                        userBean.getOrganization(Integer.valueOf(request.getParameter("organizationId")))
                        : user.getOrganizationId();
            if(user.isSuperUser())
            {
                List<Organization> allOrganizations = userBean.getTopLevelOrganizations();
                request.setAttribute("allOrganizations", allOrganizations);
            }
            
            List<CropCategory> cropCategories = organismBean.getCropCategories(currentOrganization.getOrganizationId());
            
            request.setAttribute("organization", currentOrganization);
            request.setAttribute("cropCategories", cropCategories);
            List<Organism> cropList = organismBean.getAllCrops();
            Map<String, Organism> allCrops = new HashMap<>();
            for(Organism crop:cropList)
            {
                allCrops.put(String.valueOf(crop.getOrganismId()), crop);
            }
            request.setAttribute("allCrops", allCrops);
            request.getRequestDispatcher("/cropCategoryList.ftl").forward(request, response);
        }
        else if(action.equals("submit"))
        {
            try
            {
                
                FormValidation formValidation = FormValidator.validateForm("cropCategoryCropForm",request,getServletContext());
                if(formValidation.isValid())
                {
                    Integer cropCategoryId = formValidation.getFormField("cropCategoryId").getValueAsInteger();
                    List<Integer> cropCategoryCropOrganismIds = FormUtil.getIdsFromMultipleSelect(formValidation.getFormField("cropCategoryCropOrganismIds").getWebValues());
                    CropCategory cropCategory = organismBean.getCropCategory(cropCategoryId);
                    cropCategory.setCropOrganismIds(cropCategoryCropOrganismIds.toArray(new Integer[cropCategoryCropOrganismIds.size()]));
                    organismBean.storeCropCategory(cropCategory);
                    
                    response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://").append(ServletUtil.getServerName(request)).append("/organism/cropcategory?messageKey=cropCategoryUpdated").toString());
                }
                else
                {
                    System.out.println("Form didn't validate");
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
