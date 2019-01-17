/*
 * Copyright (c) 2019 NIBIO <http://www.nibio.no/>. 
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
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.nibio.vips.gis.GISUtil;
import no.nibio.vips.gis.LonLatStringFormatException;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.util.ServletUtil;
import no.nibio.web.forms.FormValidation;
import no.nibio.web.forms.FormValidationException;
import no.nibio.web.forms.FormValidator;

/**
 * @copyright 2019 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class OrganizationController extends HttpServlet {
   
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
        
        VipsLogicUser user = (VipsLogicUser) request.getSession().getAttribute("user");
        // Basic authorization
        if(!user.isSuperUser() && ! user.isOrganizationAdmin()){
            response.sendError(403,"Access not authorized");
            return;
        }
        String action = request.getParameter("action");
        // Default view for superuser: List of all organizations
        // the user is a member
        if(action == null && user.isSuperUser())
        {
            List<Organization> organizations = SessionControllerGetter.getUserBean().getTopLevelOrganizations();
            request.setAttribute("organizations", organizations);
            request.getRequestDispatcher("/organizationList.ftl").forward(request, response);
        }
        else if(action.equals("editOrganization") || action.equals("newOrganization"))
        {
            Organization organization;
            List<VipsLogicUser> organizationUsers;
            try
            {
                organization = SessionControllerGetter.getUserBean().getOrganization(Integer.valueOf(request.getParameter("organizationId")));
                organizationUsers = SessionControllerGetter.getUserBean().getUsersByOrganization(organization.getOrganizationId());
                Collections.sort(organizationUsers);
            }
            catch(NumberFormatException ex)
            {
                organization = new Organization();
                organizationUsers = Collections.EMPTY_LIST;
            }
            
            //System.out.println("");
            if(organization.getDefaultMapCenter() != null)
            {
                request.setAttribute("defaultMapCenterLon", organization.getDefaultMapCenter().getCoordinate().x);
                request.setAttribute("defaultMapCenterLat", organization.getDefaultMapCenter().getCoordinate().y);
            }
            
            request.setAttribute("messageKey", request.getParameter("messageKey") != null ? request.getParameter("messageKey") : null);
            request.setAttribute("organizationUsers", organizationUsers);
            request.setAttribute("countries", SessionControllerGetter.getUserBean().getCountries());
            request.setAttribute("timeZones", TimeZone.getAvailableIDs());
            request.setAttribute("organization", organization);
            request.getRequestDispatcher("/organizationForm.ftl").forward(request, response);
        }
        else if(action.equals("organizationFormSubmit"))
        {
            try
            {
                Integer organizationId = Integer.valueOf(request.getParameter("organizationId"));
                Organization organization = organizationId > 0 ? 
                        SessionControllerGetter.getUserBean().getOrganization(organizationId)
                        : new Organization();
                
                FormValidation formValidation = FormValidator.validateForm("organizationForm", request, getServletContext());
                if(formValidation.isValid())
                {
                    organization.setOrganizationName(formValidation.getFormField("organizationName").getWebValue());
                    organization.setAddress1(formValidation.getFormField("address1").getWebValue());
                    organization.setAddress2(formValidation.getFormField("address2").getWebValue());
                    organization.setPostalCode(formValidation.getFormField("postalCode").getWebValue());
                    organization.setCountryCode(SessionControllerGetter.getUserBean().getCountry(formValidation.getFormField("countryCode").getWebValue()));
                    organization.setCity(formValidation.getFormField("city").getWebValue());
                    organization.setDefaultLocale(formValidation.getFormField("defaultLocale").getWebValue());
                    organization.setDefaultTimeZone(formValidation.getFormField("defaultTimeZone").getWebValue());
                    organization.setVipswebUrl(formValidation.getFormField("vipswebUrl").getWebValue());
                    Integer archiveUserId = formValidation.getFormField("archiveUserId").getValueAsInteger();
                    organization.setArchiveUser(archiveUserId > 0 ? SessionControllerGetter.getUserBean().getVipsLogicUser(archiveUserId) : null);
                    organization.setDefaultVipsCoreUserId(
                            formValidation.getFormField("defaultVipsCoreUserId").isEmpty() ? 
                                    null
                                    : formValidation.getFormField("defaultVipsCoreUserId").getValueAsInteger()
                    );
                    organization.setDefaultMapZoom(
                            formValidation.getFormField("defaultMapZoom").isEmpty() ?
                                    null
                                    : formValidation.getFormField("defaultVipsCoreUserId").getValueAsInteger()
                    );
                    organization.setDefaultMapCenter(
                            formValidation.getFormField("defaultMapCenter").isEmpty() || formValidation.getFormField("defaultMapCenter").getWebValue().trim().equals(",") ?
                                    null
                                    : new GISUtil().getJtsPointFromString(formValidation.getFormField("defaultMapCenter").getWebValue())
                    );
                    
                    organization = SessionControllerGetter.getUserBean().storeOrganization(organization);
                    
                    response.sendRedirect(
                            Globals.PROTOCOL + "://" + ServletUtil.getServerName(request)
                            + "/organization?action=editOrganization&organizationId=" + organization.getOrganizationId()
                            + "&messageKey=organizationStored" 
                    );
                }
            }
            catch(FormValidationException | LonLatStringFormatException | NumberFormatException ex)
            {
                ex.printStackTrace();
                response.sendError(500, ex.getClass().toString() + ": " + ex.getMessage());
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
