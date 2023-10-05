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
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.OrganizationGroup;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.util.ServletUtil;
import no.nibio.web.forms.FormValidation;
import no.nibio.web.forms.FormValidationException;
import no.nibio.web.forms.FormValidator;

/**
 * @copyright 2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class OrganizationGroupController extends HttpServlet {

    
    @EJB
    UserBean userBean;
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
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
        // Default: View list of groups for user's organization or in which
        // the user is a member
        if(action == null)
        {
            List<OrganizationGroup> groups;
            if(user.isSuperUser() || user.isOrganizationAdmin())
            {
                Organization organization = user.getOrganizationId();
                if(user.isSuperUser() && request.getParameter("organizationId") != null)
                {
                    organization = userBean.getOrganization(Integer.valueOf(request.getParameter("organizationId")));
                }
                groups = userBean.getOrganizationGroups(organization);
            }
            else
            {
                groups = userBean.getOrganizationGroups(user);
            }
            request.setAttribute("organizationGroups", groups);
            request.setAttribute("messageKey",request.getParameter("messageKey"));
            request.getRequestDispatcher("/organizationGroupList.ftl").forward(request, response);
        }
        else if(action.equals("editOrganizationGroup"))
        {
            try
            {
                // TODO: Restrict group access based on user privileges
                Integer organizationGroupId = Integer.valueOf(request.getParameter("organizationGroupId"));
                OrganizationGroup oGroup = organizationGroupId > 0 ? userBean.getOrganizationGroup(organizationGroupId)
                        : new OrganizationGroup();

                request.setAttribute("messageKey",request.getParameter("messageKey"));
                request.setAttribute("organizationGroup", oGroup);
                request.setAttribute("users", userBean.getUsers(user.getOrganizationId()));
                request.setAttribute("memberIds", userBean.getOrganizationGroupMemberIds(oGroup));
                request.getRequestDispatcher("organizationGroupForm.ftl").forward(request, response);
            }
            catch(NullPointerException | NumberFormatException ex)
            {
                ex.printStackTrace();
            }
        }
        else if(action.equals("organizationGroupFormSubmit"))
        {
            try
            {
                Integer organizationGroupId = Integer.valueOf(request.getParameter("organizationGroupId"));
                OrganizationGroup oGroup = organizationGroupId > 0 ? 
                        userBean.getOrganizationGroup(organizationGroupId)
                        :new OrganizationGroup();
                
                // Validate form fields (except uploaded file)
                FormValidation formValidation = FormValidator.validateForm("organizationGroupForm", request, getServletContext());
                
                if(formValidation.isValid())
                {
                    oGroup.setGroupName(formValidation.getFormField("groupName").getWebValue());
                    if(user.isSuperUser() && formValidation.getFormField("organizationId") != null)
                    {
                        oGroup.setOrganizationId(formValidation.getFormField("organizationId").getValueAsInteger());
                    }
                    else
                    {
                        oGroup.setOrganizationId(user.getOrganizationId().getOrganizationId());
                    }
                    
                    // Store the organizationGroup
                    oGroup = userBean.storeOrganizationGroup(oGroup, formValidation.getFormField("organizationGroupUserIds").getWebValues());
                    
                    response.sendRedirect(
                            Globals.PROTOCOL + "://" + ServletUtil.getServerName(request)
                            + "/organizationgroup?action=editOrganizationGroup&organizationGroupId=" + oGroup.getOrganizationGroupId()
                            + "&messageKey=organizationGroupStored" 
                    );
                }
                
            }
            catch(NullPointerException | NumberFormatException | FormValidationException ex)
            {
                ex.printStackTrace();
            }
        }
        else if(action.equals("deleteOrganizationGroup"))
        {
            if(user.isOrganizationAdmin() || user.isSuperUser())
            {
                Integer organizationGroupId = Integer.valueOf(request.getParameter("organizationGroupId"));
                OrganizationGroup oGroup = userBean.getOrganizationGroup(organizationGroupId);
                String messageKey = "";
                if(oGroup != null)
                {
                    userBean.deleteOrganizationGroup(oGroup);
                    messageKey = "organizationGroupDeleted";
                }
                
                response.sendRedirect(
                            Globals.PROTOCOL + "://" + ServletUtil.getServerName(request)
                            + "/organizationgroup?messageKey="  + messageKey
                    );
            }
            else
            {
                response.sendError(403,"Access not authorized");
            }
        }
        else if(action.equals("viewUserOrganizationGroups"))
        {
            
            try
            {
                VipsLogicUser viewUser = null;
                if(user.isSuperUser() || user.isOrganizationAdmin())
                {
                    Integer userId = Integer.valueOf(request.getParameter("userId"));
                    viewUser = userBean.getVipsLogicUser(userId);
                }
                else
                {
                    viewUser = user;
                }
                List<OrganizationGroup> userGroups = userBean.getOrganizationGroups(viewUser);
                request.setAttribute("viewUser", user);
                request.setAttribute("userGroups", userGroups);
                request.getRequestDispatcher("userOrganizationGroupsList.ftl").forward(request, response);
            }
            catch(NullPointerException | NumberFormatException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
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
     * Handles the HTTP <code>POST</code> method.
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
