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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.bioforsk.vips.logic.controller.session.UserBean;
import no.bioforsk.vips.logic.entity.Organization;
import no.bioforsk.vips.logic.entity.UserAuthentication;
import no.bioforsk.vips.logic.entity.UserAuthenticationPK;
import no.bioforsk.vips.logic.entity.UserAuthenticationType;
import no.bioforsk.vips.logic.entity.VipsLogicRole;
import no.bioforsk.vips.logic.entity.VipsLogicUser;
import no.bioforsk.vips.logic.i18n.SessionLocaleUtil;
import no.bioforsk.vips.logic.util.Globals;
import no.bioforsk.vips.logic.util.SessionControllerGetter;
import no.bioforsk.vips.util.ServletUtil;
import no.bioforsk.web.forms.FormField;
import no.bioforsk.web.forms.FormValidation;
import no.bioforsk.web.forms.FormValidator;

/**
 * Handles user actions
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class UserController extends HttpServlet {

    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
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
        
        String action = request.getParameter("action");
        VipsLogicUser user = (VipsLogicUser) request.getSession().getAttribute("user");
        UserBean userBean = SessionControllerGetter.getUserBean();
        
        // Default: View list of users
        // for SUPERUSERS and ORGANIZATION ADMINS
        if(action == null)
        {
            if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
            {
                List<VipsLogicUser> users;
                if(user.isSuperUser())
                {
                    users = userBean.getAllUsers();
                }
                else
                {
                    users = userBean.getUsers(user.getOrganizationId());
                }
                request.setAttribute("users", users);
                // If this is a redirect from a controller, with a message to be passed on
                request.setAttribute("messageKey", request.getParameter("messageKey"));
                request.getRequestDispatcher("/userList.ftl").forward(request, response);
            }
            else
            {
                response.sendError(403,"Access not authorized"); // HTTP Forbidden
            }
      
        }
        
        // View one user
        // Authorization: SUPERUSERS and ORGANIZATION ADMINS
        else if(action.equals("viewUser"))
        {
            if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
            {
                try
                {
                    Integer userId = Integer.valueOf(request.getParameter("userId"));
                    VipsLogicUser viewUser = em.find(VipsLogicUser.class, userId);
                    // Only superusers may view users from different organizations
                    if(! user.isSuperUser() && ! viewUser.getOrganizationId().equals(user.getOrganizationId()))
                    {
                        response.sendError(403,"Only super users can view users outside their own organization"); // HTTP Forbidden
                    }
                    else
                    {
                        request.setAttribute("viewUser", viewUser);
                        request.setAttribute("organizations", em.createNamedQuery("Organization.findAll").getResultList());
                        request.setAttribute("userStatusIds", userBean.getUserStatusIds());
                        request.setAttribute("vipsLogicRoles", em.createNamedQuery("VipsLogicRole.findAll").getResultList());
                        // In case this is a redirect from another action, and a message should be forwarded
                        request.setAttribute("messageKey", request.getParameter("messageKey")); 
                        request.getRequestDispatcher("/userForm.ftl").forward(request, response);
                    }
                }
                catch(NullPointerException | NumberFormatException ex)
                {
                    response.sendError(500, "Invalid userId " + request.getParameter("userId"));
                }
            }
            else
            {
                response.sendError(403,"Access not authorized"); // HTTP Forbidden
            }
            
        }
        
        // Store information about user
        // Authorization: SUPERUSERS and ORGANIZATION ADMINS
        else if(action.equals("userFormSubmit"))
        {
            if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
            {
                try
                {
                    Integer userId = Integer.valueOf(request.getParameter("userId"));
                    VipsLogicUser viewUser = em.find(VipsLogicUser.class, userId);
                    // Only superusers may view users from different organizations
                    if(! user.isSuperUser() && ! viewUser.getOrganizationId().equals(user.getOrganizationId()))
                    {
                        response.sendError(403); // HTTP Forbidden
                    }
                    else
                    {
                        // Standard form validation
                        FormValidation formValidation = FormValidator.validateForm("userForm",request,getServletContext());
                        if(formValidation.isValid())
                        {
                            viewUser.setEmail(formValidation.getFormField("email").getWebValue());
                            viewUser.setFirstName(formValidation.getFormField("firstName").getWebValue());
                            viewUser.setLastName(formValidation.getFormField("lastName").getWebValue());
                            if(user.isSuperUser())
                            {
                                Organization organization = em.find(Organization.class, formValidation.getFormField("organizationId").getValueAsInteger());
                                viewUser.setOrganizationId(organization);
                            }
                            viewUser.setUserStatusId(formValidation.getFormField("userStatusId").getValueAsInteger());
                            viewUser.setRemarks(formValidation.getFormField("remarks").getWebValue());
                            FormField roles = formValidation.getFormField("vipsLogicRoles");
                            
                            if(roles.getWebValues() != null)
                            {
                                Set<VipsLogicRole> vipsLogicRoles = new HashSet();
                                for(String roleId :roles.getWebValues())
                                {
                                    vipsLogicRoles.add(em.find(VipsLogicRole.class, Integer.valueOf(roleId)));
                                }
                                viewUser.setVipsLogicRoles(vipsLogicRoles);
                            }
                            userBean.storeUser(viewUser);
                            // Add confirmation message
                            request.setAttribute("messageKey", "userUpdated");
                            response.sendRedirect(new StringBuilder("http://").append(ServletUtil.getServerName(request)).append("/user?action=viewUser&userId=").append(viewUser.getUserId()).append("&messageKey=").append("userUpdated").toString());
                        }
                        else
                        {
                            request.setAttribute("formValidation", formValidation);
                            request.setAttribute("viewUser", viewUser);
                            request.setAttribute("organizations", em.createNamedQuery("Organization.findAll").getResultList());
                            request.setAttribute("userStatusIds", userBean.getUserStatusIds());
                            request.setAttribute("vipsLogicRoles", em.createNamedQuery("VipsLogicRole.findAll").getResultList());
                            request.getRequestDispatcher("/userForm.ftl").forward(request, response);
                        }
                        
                    }
                }
                catch(NullPointerException | NumberFormatException ex)
                {
                    ex.printStackTrace();
                    response.sendError(500, ex.getMessage());
                }
            }
            else
            {
                response.sendError(403,"Access not authorized"); // HTTP Forbidden
            }
        }
        
        // DELETE user
        // Authorization: SUPERUSERS and ORGANIZATION ADMINS
        else if(action.equals("deleteUser"))
        {
            if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
            {
                try
                {
                    Integer userId = Integer.valueOf(request.getParameter("userId"));
                    VipsLogicUser viewUser = em.find(VipsLogicUser.class, userId);
                    //em.remove(viewUser);
                    userBean.deleteUser(viewUser);
                    response.sendRedirect(new StringBuilder("http://").append(ServletUtil.getServerName(request)).append("/user").append("?messageKey=").append("userDeleted").toString());
                    
                }
                catch(NullPointerException | NumberFormatException ex)
                {
                    response.sendError(500, ex.getMessage());
                }
            }
            else
            {
                response.sendError(403,"Access not authorized"); // HTTP Forbidden
            }
        }
        
        // Show user application form
        // FOR ANY USER
        else if(action.equals("registerNewUserForm"))
        {
            Integer userAuthenticationTypeId = ServletUtil.getIntegerParameter(request, "userAuthenticationTypeId");
            if(userAuthenticationTypeId == null)
            {
                response.sendRedirect("/");
                return;
            }

            // Password auth?
            if(userAuthenticationTypeId.equals(UserAuthenticationType.TYPE_PASSWORD))
            {
                
            }
            // OpenId or Google?
            else if(
                    userAuthenticationTypeId.equals(UserAuthenticationType.TYPE_OPENID) 
                    || userAuthenticationTypeId.equals(UserAuthenticationType.TYPE_OPENID_GOOGLE)
                    )
            {
                
            }
            // If none of these, return to front page
            else
            {
                response.sendRedirect("/");
                return;
            }
            
            // Add the userAuthenticationTypeId as template parameter
            request.setAttribute("userAuthenticationTypeId", userAuthenticationTypeId);
            // Get available organizations, add for select list
            request.setAttribute("organizations", em.createNamedQuery("Organization.findAll").getResultList());
            request.getRequestDispatcher("/userRegistrationForm.ftl").forward(request, response);
        }
        
        // Register the new user
        // FOR ANY USER
        else if(action.equals("registerNewUserFormSubmit"))
        {
            VipsLogicUser newUser = new VipsLogicUser();
            UserAuthentication auth = new UserAuthentication();
            // What kind of authetication type is this?
            UserAuthenticationType userAuthenticationType = null;
            try{
                userAuthenticationType = em.find(UserAuthenticationType.class, ServletUtil.getIntegerParameter(request, "userAuthenticationTypeId"));
            }
            catch(Exception e)
            {
                request.getRequestDispatcher("/login.ftl").forward(request, response);
                return;
            }

            // Standard form validation
            FormValidation formValidation = FormValidator.validateForm(
                                                "userRegistrationFormType" + userAuthenticationType.getUserAuthenticationTypeId(),
                                                request,getServletContext()
                                            );
            // Extra check: That user's email is not already in use
            FormField emailField = formValidation.getFormField("email");
            if(userBean.getUserByEmail(emailField.getWebValue()) != null)
            {
                emailField.setValid(false);
                emailField.setValidationMessage(SessionLocaleUtil.getI18nText(request, "emailAddressIsAlreadyInUse"));
            }

            if(formValidation.isValid())
            {

                // Setting authentication info first
                auth.setUserAuthenticationType(userAuthenticationType);
                if(userAuthenticationType.getUserAuthenticationTypeId().equals(UserAuthenticationType.TYPE_PASSWORD))
                {
                    auth.setUsername(formValidation.getFormField("username").getWebValue());
                    auth.setPassword(userBean.getMD5EncryptedString(formValidation.getFormField("password").getWebValue()));
                }
                // For OpenId and GoogleId
                else
                {
                    auth.setUsername((String)request.getSession().getAttribute("openId"));
                }
                // Adding user info
                newUser.setEmail(formValidation.getFormField("email").getWebValue());
                newUser.setFirstName(formValidation.getFormField("firstName").getWebValue());
                newUser.setLastName(formValidation.getFormField("lastName").getWebValue());
                Organization organization = em.find(Organization.class, formValidation.getFormField("organizationId").getValueAsInteger());
                newUser.setOrganizationId(organization);
                newUser.setApprovalApplication(formValidation.getFormField("approvalApplication").getWebValue());
                newUser.setUserStatusId(Globals.USER_STATUS_AWAITING_EMAIL_VERIFICATION);
                userBean.storeUserFirstTime(newUser, auth);
                // Send email about user verification
                if(newUser.getUserStatusId().equals(Globals.USER_STATUS_AWAITING_EMAIL_VERIFICATION))
                {
                    userBean.sendUserEmailVerification(newUser, SessionLocaleUtil.getI18nBundle(request), ServletUtil.getServerName(request));
                }
                response.sendRedirect(new StringBuilder("http://").append(ServletUtil.getServerName(request)).append("/user?action=registerNewUserFormReceipt").toString());
            }
            else
            {
                request.setAttribute("formValidation", formValidation);
                request.setAttribute("organizations", em.createNamedQuery("Organization.findAll").getResultList());
                request.setAttribute("userAuthenticationTypeId", userAuthenticationType.getUserAuthenticationTypeId());
                request.getRequestDispatcher("/userRegistrationForm.ftl").forward(request, response);
            }
        }
        else if(action.equals("registerNewUserFormReceipt"))
        {
            request.setAttribute("messageKey","registerNewUserReceipt");
            request.getRequestDispatcher("/login.ftl").forward(request, response);
        }
        else if(action.equals("confirmEmail"))
        {
            String verificationCode = request.getParameter("verificationCode");
            VipsLogicUser confirmUser = SessionControllerGetter.getUserBean().verifyUserEmail(verificationCode);
            if(confirmUser != null)
            {
                // Success
                request.setAttribute("messageKey","confirmEmailReceipt");
                request.getRequestDispatcher("/login.ftl").forward(request, response);
            }
            else
            {
                // Failure
                request.setAttribute("errorMessageKey","confirmEmailFailed");
                request.getRequestDispatcher("/login.ftl").forward(request, response);
            }
        }
        // An OpenId or GoogleId is requested to be attached to a user, identified by username/password
        else if(action.equals("attachIdToExistingUser"))
        {
            UserAuthenticationType userAuthenticationType = null;
            try{
                userAuthenticationType = em.find(UserAuthenticationType.class, ServletUtil.getIntegerParameter(request, "userAuthenticationTypeId"));
            }
            catch(Exception e)
            {
                request.getRequestDispatcher("/login.ftl").forward(request, response);
                return;
            }
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            Map<String,String> creds = new HashMap();
            creds.put("username", username);
            creds.put("password", password);

            VipsLogicUser searchUser = SessionControllerGetter.getUserBean().authenticateUser(creds);

            if(searchUser != null)
            {
                UserAuthentication userAuth = new UserAuthentication();
                userAuth.setVipsLogicUser(searchUser);
                userAuth.setUserAuthenticationType(userAuthenticationType);
                userAuth.setUsername((String)request.getSession().getAttribute("openId"));
                UserAuthenticationPK pk = new UserAuthenticationPK(searchUser.getUserId(), userAuthenticationType.getUserAuthenticationTypeId());
                userAuth.setUserAuthenticationPK(pk);
                SessionControllerGetter.getUserBean().storeUserAuthentication(userAuth);
                //em.persist(userAuth);
                request.setAttribute("messageKey", "attachIdToExistingUserReceipt");
                request.getRequestDispatcher("/login.ftl").forward(request, response);
            }
            else
            {
                request.setAttribute("errorMessageKey", "invalidcredentials");
                request.getRequestDispatcher("/login.ftl").forward(request, response);
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
