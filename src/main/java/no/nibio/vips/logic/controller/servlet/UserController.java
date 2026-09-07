/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import no.nibio.vips.i18n.LanguageUtil;
import no.nibio.vips.logic.controller.session.DeleteUserException;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.UserAuthentication;
import no.nibio.vips.logic.entity.UserAuthenticationPK;
import no.nibio.vips.logic.entity.UserAuthenticationType;
import no.nibio.vips.logic.entity.VipsLogicRole;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.entity.misc.UserResources;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.modules.barkbeetle.BarkbeetleBean;
import no.nibio.vips.logic.modules.barkbeetle.SeasonTrapsite;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.util.ServletUtil;
import no.nibio.web.forms.FormField;
import no.nibio.web.forms.FormUtil;
import no.nibio.web.forms.FormValidation;
import no.nibio.web.forms.FormValidationException;
import no.nibio.web.forms.FormValidator;

/**
 * Handles user actions that require authorization. For publicly available actions
 * such as applying for a user account, se PublicUserController
 * @copyright 2013-2025 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class UserController extends HttpServlet {

    @Inject
    EntityManager em;
    
    @Inject
    UserBean userBean;

    @Inject
    BarkbeetleBean barkbeetleBean;
    
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
                    List<Organization> organizations = userBean.getOrganizations();
                    request.setAttribute("organizations", organizations);
                    request.setAttribute("vipsLogicRoles", em.createNamedQuery("VipsLogicRole.findAll").getResultList());
                }
                else if(user.isOrganizationAdmin())
                {
                    request.setAttribute("vipsLogicRoles", em.createNamedQuery("VipsLogicRole.findAll").getResultList());
                    users = userBean.getUsers(user.getOrganizationId());
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
                this.redirectToLogin(request, response);
            }
      
        }
        
        // View and edit one user
        // Authorization: SUPERUSERS and ORGANIZATION ADMINS
        else if(action.equals("viewUser"))
        {
            if(user == null)
            {
                this.redirectToLogin(request, response);
            }
            
            try
            {
                Integer userId = Integer.valueOf(request.getParameter("userId"));
                VipsLogicUser viewUser = em.find(VipsLogicUser.class, userId);
                if(viewUser == null)
                {
                    response.sendError(500, "Invalid userId " + request.getParameter("userId"));
                    return;
                }
                // Only superusers may view users from different organizations
                if(! user.isSuperUser() && ! viewUser.getOrganizationId().equals(user.getOrganizationId()))
                {
                    response.sendError(403,"Only super users can view users outside their own organization"); // HTTP Forbidden
                }
                // Regular users can only view and edit themselves
                else if(! user.isSuperUser() && ! user.isOrganizationAdmin() && !viewUser.getUserId().equals(user.getUserId()))
                {
                    response.sendError(403,"Access not authorized");
                }
                else
                {
                    request.setAttribute("viewUser", viewUser);
                    request.setAttribute("countries", userBean.getUserCountries());
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
                ex.printStackTrace();
                response.sendError(500, "Invalid userId " + request.getParameter("userId"));
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
            }
        }
        
        // Create a new user
        // Authorization: SUPERUSERS and ORGANIZATION ADMINS
        else if(action.equals("newUser"))
        {
            if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
            {
                
                VipsLogicUser viewUser = new VipsLogicUser();
                viewUser.setOrganizationId(user.getOrganizationId());

                request.setAttribute("viewUser", viewUser);
                request.setAttribute("countries", userBean.getUserCountries());
                request.setAttribute("defaultLocale", user.getOrganizationId().getDefaultLocale());
                request.setAttribute("organizations", em.createNamedQuery("Organization.findAll").getResultList());
                request.setAttribute("userStatusIds", userBean.getUserStatusIds());
                List<VipsLogicRole> roles = em.createNamedQuery("VipsLogicRole.findAll").getResultList();
                // Only super users can create super users
                if(!user.isSuperUser())
                {
                    for(VipsLogicRole role:roles)
                    {
                        if(role.getVipsLogicRoleId().equals(VipsLogicRole.SUPERUSER))
                        {
                            roles.remove(role);
                            break;
                        }
                    }
                }
                request.setAttribute("vipsLogicRoles", roles);
                // In case this is a redirect from another action, and a message should be forwarded
                request.setAttribute("messageKey", request.getParameter("messageKey")); 
                request.getRequestDispatcher("/userForm.ftl").forward(request, response);

            }
            else
            {
                this.redirectToLogin(request, response);
            }
            
        }
        
        // Store information about user
        else if(action.equals("userFormSubmit"))
        {
            try
            {
                Integer userId = Integer.valueOf(request.getParameter("userId"));
                VipsLogicUser viewUser = userId > 0 ? em.find(VipsLogicUser.class, userId) : new VipsLogicUser();
                // Only superusers may view users from different organizations
                if(userId > 0 && ! user.isSuperUser() && ! viewUser.getOrganizationId().equals(user.getOrganizationId()))
                {
                    response.sendError(403); // HTTP Forbidden
                }
                // Regular user can't create new users, and only edit themselves
                else if(! user.isSuperUser() && ! user.isOrganizationAdmin()
                        && ! userId.equals(user.getUserId()))
                {
                    response.sendError(403); // HTTP Forbidden
                }
                else
                {

                    // Standard form validation
                    FormValidation formValidation = FormValidator.validateForm("userForm",request,getServletContext());
                    FormField usernameField = formValidation.getFormField("username");
                    FormField passwordField = formValidation.getFormField("pass1");
                    String username = (usernameField != null && usernameField.getWebValue() != null && !usernameField.getWebValue().trim().isEmpty()) ?
                                        usernameField.getWebValue().trim()
                                        : null;
                    String password = (passwordField != null && passwordField.getWebValue() != null && !passwordField.getWebValue().trim().isEmpty()) ?
                                        passwordField.getWebValue().trim()
                                        : null;
                   
                    // If new user: check if username or email already exists
                    if(userId <= 0)
                    {
                        
                        // Check for whitespace inside trimmed username
                        if(FormUtil.whitespaceFoundInString(username))
                        {
                             usernameField.setValid(false);
                             usernameField.setValidationMessage(SessionLocaleUtil.getI18nText(request, "noWhitespaceAllowed") + ": " + username);
                        }
                        try
                        {
                            UserAuthentication authTest = em.createNamedQuery("UserAuthentication.findByUsername",UserAuthentication.class).setParameter("username", usernameField.getWebValue()).getSingleResult();
                            if(authTest != null)
                            {
                                usernameField.setValid(false);
                                usernameField.setValidationMessage(MessageFormat.format(SessionLocaleUtil.getI18nText(request, "usernameExists"), formValidation.getFormField("username").getWebValue()));
                            }
                        }
                        catch(NoResultException ex)
                        {
                            // All is well, proceed...
                        }

                        // Extra check: That user's email is not already in use
                        FormField emailField = formValidation.getFormField("email");
                        Boolean emailAlreadyInUse = false;
                        try
                        {
                            VipsLogicUser foundUser = userBean.getUserByEmail(emailField.getWebValue().toLowerCase());
                            emailAlreadyInUse = (foundUser != null);
                        }
                        catch(NonUniqueResultException ex)
                        {
                            emailAlreadyInUse = true;
                        }
                        if(emailAlreadyInUse)
                        {
                            emailField.setValid(false);
                            emailField.setValidationMessage(SessionLocaleUtil.getI18nText(request, "emailAddressIsAlreadyInUse"));
                        }
                    }
                    if(formValidation.isValid())
                    {
                        viewUser.setEmail(formValidation.getFormField("email").getWebValue().toLowerCase());
                        viewUser.setFirstName(formValidation.getFormField("firstName").getWebValue());
                        viewUser.setLastName(formValidation.getFormField("lastName").getWebValue());
                        viewUser.setPhoneCountryCode(formValidation.getFormField("phoneCountryCode").getWebValue());
                        viewUser.setPhone(formValidation.getFormField("phone").getWebValue().replaceAll("[^0-9]", ""));
                        viewUser.setApprovesSmsBilling(formValidation.getFormField("approvesSmsBilling").getWebValue() != null);
                        viewUser.setFreeSms(formValidation.getFormField("freeSms").getWebValue() != null && formValidation.getFormField("freeSms").getWebValue().equals("true") );
                        viewUser.setPreferredLocale(formValidation.getFormField("preferredLocale").getWebValue());
                        if(user.isSuperUser())
                        {
                            Organization organization = em.find(Organization.class, formValidation.getFormField("organizationId").getValueAsInteger());
                            viewUser.setOrganizationId(organization);
                            
                        }
                        else
                        {
                            viewUser.setOrganizationId(user.getOrganizationId());
                        }
                        Integer oldUserStatusId = viewUser.getUserStatusId();
                        if(user.isSuperUser() || user.isOrganizationAdmin())
                        {
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
                        }
                        String messageKey = "";
                        if(userId > 0)
                        {
                            // Superuser can change username and password for everyone
                            // Organization admin can change username and password for members of on organization
                            if(user.isSuperUser() ||
                                    (user.isOrganizationAdmin() && viewUser.getOrganization_id().equals(user.getOrganization_id()))
                            )
                            {
                                UserAuthentication auth = viewUser.getPasswordAuthentication();
                                auth.setUsername(username);
                                if(password != null)
                                {
                                    auth.setPassword(userBean.getMD5EncryptedString(password));
                                }
                            }
                            userBean.storeUser(viewUser);
                            userBean.handleUserStatusChange(oldUserStatusId, viewUser, SessionLocaleUtil.getI18nBundle(request), ServletUtil.getServerName(request));
                            // Add confirmation message
                            messageKey = "userUpdated";
                        }
                        else if(user.isSuperUser() || user.isOrganizationAdmin())
                        {
                            // Create authentication for user
                            UserAuthentication authentication = new UserAuthentication();
                            authentication.setUserAuthenticationType(em.find(UserAuthenticationType.class, UserAuthenticationType.TYPE_PASSWORD));
                            authentication.setUsername(username);
                            password = userBean.generateValidPassword();
                            authentication.setPassword(userBean.getMD5EncryptedString(password));
                            // Store the user for the first time
                            viewUser = userBean.storeUserFirstTime(viewUser, authentication);
                            // Send password to user's email address
                            userBean.sendUsernameAndPasswordToUser(viewUser, 
                                                            authentication.getUsername(),
                                                            password,
                                                            SessionLocaleUtil.getI18nBundle(request),
                                                            ServletUtil.getServerName(request)
                                                          );
                            // Add confirmation message
                            messageKey = "userCreatedAndInformed";
                        }
                        else
                        {
                            response.sendError(403,"Access not authorized"); // HTTP Forbidden
                        }

                        response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://").append(ServletUtil.getServerName(request)).append("/user?action=viewUser&userId=").append(viewUser.getUserId()).append("&messageKey=").append(messageKey).toString());
                    }
                    else
                    {
                        viewUser.setOrganizationId(user.getOrganizationId());
                        request.setAttribute("formValidation", formValidation);
                        request.setAttribute("viewUser", viewUser);
                        request.setAttribute("countries", userBean.getUserCountries());
                        request.setAttribute("organizations", em.createNamedQuery("Organization.findAll").getResultList());
                        request.setAttribute("userStatusIds", userBean.getUserStatusIds());
                        request.setAttribute("vipsLogicRoles", em.createNamedQuery("VipsLogicRole.findAll").getResultList());
                        request.getRequestDispatcher("/userForm.ftl").forward(request, response);
                    }

                }
            }
            catch(NullPointerException | NumberFormatException | FormValidationException ex)
            {
                ex.printStackTrace();
                response.sendError(500, ex.getMessage());
            }
           
        }
        
        // DELETE user
        // First: Display resources that user has. Ask for transfer to new user
        else if(action.equals("userDeleteForm"))
        {
            if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
            {
                try
                {
                    Integer userId = Integer.valueOf(request.getParameter("userId"));
                    VipsLogicUser viewUser = em.find(VipsLogicUser.class, userId);
                    UserResources userResources = userBean.getUserResources(viewUser);

                    // Barkbeetle module dependencies
                    List<SeasonTrapsite> userSeasonTrapsites = barkbeetleBean.getSeasonTrapsites(viewUser);
 
                    // If some resources connected, render form, otherwise, route to user delete
                    if(userResources.isEmpty() && userSeasonTrapsites.isEmpty())
                    {
                        response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://").append(ServletUtil.getServerName(request)).append("/user").append("?action=deleteUser&userId=").append(userId).toString());
                    }
                    else
                    {
                        // Get all possible users to transfer to
                        // For orgadmin: All users of same organization
                        // For superuser: All users
                        List<VipsLogicUser> users = user.isSuperUser() ?
                            em.createNamedQuery("VipsLogicUser.findAll").getResultList()
                            : em.createNamedQuery("VipsLogicUser.findByOrganizationId").setParameter("organizationId", user.getOrganizationId()).getResultList();
                        
                        request.setAttribute("viewUser", viewUser);
                        request.setAttribute("userResources", userResources);
                        java.util.Collections.sort(users);
                        request.setAttribute("users", users);
                        request.setAttribute("userBarkbeetleSeasonTrapsites", userSeasonTrapsites);
                        request.setAttribute("errorMsg", request.getParameter("errorMsg"));
                        request.getRequestDispatcher("/userDeleteForm.ftl").forward(request, response);
                    }
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
        // Authorization: SUPERUSERS and ORGANIZATION ADMINS
        else if(action.equals("deleteUser"))
        {
            if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
            {
                try
                {
                    Integer userId = Integer.valueOf(request.getParameter("userId"));
                    VipsLogicUser viewUser = em.find(VipsLogicUser.class, userId);
                    
                    // Are there resources connected to this user?
                    UserResources userResources = userBean.getUserResources(viewUser);
                    // Barkbeetle module dependencies
                    List<SeasonTrapsite> userSeasonTrapsites = barkbeetleBean.getSeasonTrapsites(viewUser);
                    if(! userResources.isEmpty() || ! userSeasonTrapsites.isEmpty())
                    {
                        boolean resourcesTransferred = false;
                        try
                        {
                            Integer transferToUserId = Integer.valueOf(request.getParameter("transferToUserId"));
                            VipsLogicUser transferToUser = em.find(VipsLogicUser.class, transferToUserId);
                            if(transferToUser != null)
                            {
                                userBean.transferUserResources(viewUser,transferToUser);
                                barkbeetleBean.transferSeasonTrapsites(viewUser,transferToUser);
                                resourcesTransferred = true;
                            }
                        }
                        catch(NullPointerException | NumberFormatException ex ){
                            resourcesTransferred = false;
                        }
                        if(!resourcesTransferred)
                        {
                            response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://").append(ServletUtil.getServerName(request))
                                    .append("/user")
                                    .append("?action=userDeleteForm&userId=").append(userId)
                                    .append("&errorMsg=").append(URLEncoder.encode("User has resources that must be transferred", "UTF-8"))
                                    .toString()
                            );
                            return;
                        }
                    }
                    userBean.deleteUser(viewUser);
                    response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://").append(ServletUtil.getServerName(request)).append("/user").append("?messageKey=").append("userDeleted").toString());
                }
                catch(NullPointerException | NumberFormatException | DeleteUserException ex)
                {
                    response.sendError(500, ex.getMessage());
                }
            }
            else
            {
                response.sendError(403,"Access not authorized"); // HTTP Forbidden
            }
        }
        // Only orgAdmins and super users can do this
        else if(action.equals("approveUser"))
        {
            if(user == null)
            {
                this.redirectToLogin(request, response);
            }
            if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
            {
                try
                {
                    VipsLogicUser userToApprove = em.find(VipsLogicUser.class, Integer.valueOf(request.getParameter("userId")));
                    if(userToApprove != null)
                    {
                        Integer oldUserStatusId = userToApprove.getUserStatusId();
                        userToApprove.setUserStatusId(Globals.USER_STATUS_APPROVED);
                        userBean.storeUser(userToApprove);
                        // Handle user status change
                        userBean.handleUserStatusChange(oldUserStatusId, userToApprove, SessionLocaleUtil.getI18nBundle(request), ServletUtil.getServerName(request));
                        response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://").append(ServletUtil.getServerName(request)).append("/user").append("?messageKey=").append("userApproved").toString());
                    }
                }
                catch(NullPointerException | NumberFormatException ex)
                {
                    response.sendError(500, ex.getMessage());
                }
            }
            else
            {
                request.setAttribute("errorMessageKey", "invalidcredentials");
                request.getRequestDispatcher("/login.ftl").forward(request, response);
            }
        }
    }
    
    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws UnsupportedEncodingException, IOException
    {
        String nextPage = ServletUtil.getFullRequestURI(request);
        String nextPageDirective= "?nextPage=" + URLEncoder.encode(nextPage, "UTF-8");
        response.sendRedirect(Globals.PROTOCOL + "://" + ServletUtil.getServerName(request) + "/login" + nextPageDirective);
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
