/*
 * Copyright (c) 2025 NIBIO <http://www.nibio.no/>. 
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
import java.util.Calendar;
import java.util.Date;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.UserAuthentication;
import no.nibio.vips.logic.entity.UserAuthenticationType;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.util.ServletUtil;
import no.nibio.web.forms.FormField;
import no.nibio.web.forms.FormUtil;
import no.nibio.web.forms.FormValidation;
import no.nibio.web.forms.FormValidationException;
import no.nibio.web.forms.FormValidator;

/**
 * Handles user actions that do not require login, such as register a user, request new password etc
 * @copyright 2025 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class PublicUserController extends HttpServlet {

    @Inject
    EntityManager em;
    
    @Inject
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
        
        String action = request.getParameter("action");
        
        // Show user application form
        // FOR ANY USER
        if(action.equals("registerNewUserForm"))
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
            try
            {
                FormValidation formValidation = FormValidator.validateForm(
                                                    "userRegistrationFormType" + userAuthenticationType.getUserAuthenticationTypeId(),
                                                    request,getServletContext()
                                                );
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
                FormField usernameField = formValidation.getFormField("username");
                String username = usernameField.getWebValue().trim();
                if(FormUtil.whitespaceFoundInString(username))
                {
                    usernameField.setValid(false);
                    usernameField.setValidationMessage(SessionLocaleUtil.getI18nText(request, "noWhitespaceAllowed") + ": " + username);
                }
                

                if(formValidation.isValid())
                {
                    
                    // Setting authentication info first
                    auth.setUserAuthenticationType(userAuthenticationType);
                    if(userAuthenticationType.getUserAuthenticationTypeId().equals(UserAuthenticationType.TYPE_PASSWORD))
                    {
                        auth.setUsername(username);
                        auth.setPassword(userBean.getMD5EncryptedString(formValidation.getFormField("password").getWebValue()));
                    }
                    // For OpenId and GoogleId
                    else
                    {
                        auth.setUsername((String)request.getSession().getAttribute("openId"));
                    }
                    // Adding user info
                    newUser.setEmail(formValidation.getFormField("email").getWebValue().toLowerCase());
                    newUser.setFirstName(formValidation.getFormField("firstName").getWebValue());
                    newUser.setLastName(formValidation.getFormField("lastName").getWebValue());
                    newUser.setPreferredLocale(formValidation.getFormField("preferredLocale").getWebValue());
                    newUser.setPhoneCountryCode("47");// TODO: More intelligent selection here?
                    Organization organization = em.find(Organization.class, formValidation.getFormField("organizationId").getValueAsInteger());
                    newUser.setOrganizationId(organization);
                    newUser.setApprovalApplication(formValidation.getFormField("suggestedUserRole").getWebValue() + "\n" + formValidation.getFormField("approvalApplication").getWebValue());
                    newUser.setUserStatusId(Globals.USER_STATUS_AWAITING_EMAIL_VERIFICATION);
                    userBean.storeUserFirstTime(newUser, auth);
                    // Send email about user verification
                    if(newUser.getUserStatusId().equals(Globals.USER_STATUS_AWAITING_EMAIL_VERIFICATION))
                    {
                        userBean.sendUserEmailVerification(newUser, SessionLocaleUtil.getI18nBundle(request), ServletUtil.getServerName(request));
                    }
                    response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://").append(ServletUtil.getServerName(request)).append("/login?nextPage=%2Findex.html&messageKey=registerNewUserReceipt").toString());
                }
                else
                {
                    request.setAttribute("formValidation", formValidation);
                    request.setAttribute("organizations", em.createNamedQuery("Organization.findAll").getResultList());
                    request.setAttribute("userAuthenticationTypeId", userAuthenticationType.getUserAuthenticationTypeId());
                    request.getRequestDispatcher("/userRegistrationForm.ftl").forward(request, response);
                }
            }
            catch(FormValidationException ex)
            {
                response.sendError(500, ex.getMessage());
            }
        }
        else if(action.equals("confirmEmail"))
        {
            String verificationCode = request.getParameter("verificationCode");
            VipsLogicUser confirmUser = userBean.verifyUserEmail(verificationCode);
            if(confirmUser != null)
            {
                // Success
                // Send message to organization admin that user has confirmed his/her email
                userBean.informAdminOfConfirmedEmail(confirmUser, SessionLocaleUtil.getI18nBundle(request), ServletUtil.getServerName(request));
                response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://").append(ServletUtil.getServerName(request)).append("/login?nextPage=%2Findex.html&messageKey=confirmEmailReceipt").toString());
            }
            else
            {
                // Failure
                response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://").append(ServletUtil.getServerName(request)).append("/login?nextPage=%2Findex.html&messageKey=confirmEmailFailure").toString());
            }
        }
        // Type email address to get a link to resetting password
        else if(action.equals("resetPasswordRequestForm"))
        {
            request.getRequestDispatcher("/resetPasswordRequestForm.ftl").forward(request, response);
        }
        // Treating request to get a link to resetting password
        else if(action.equals("resetPasswordRequestFormSubmit"))
        {
            try
            {
                FormValidation formValidation = FormValidator.validateForm("resetPasswordRequestForm",
                                                    request,getServletContext()
                                                );
                if(formValidation.isValid())
                {
                    try
                    {
                        String email = formValidation.getFormField("email").getWebValue().toLowerCase();
                        // Check that this email address exists in the system
                        VipsLogicUser resetPasswordUser = em.createNamedQuery("VipsLogicUser.findByEmail", VipsLogicUser.class)
                                                    .setParameter("email", email)
                                                    .getSingleResult();
                        // Create password reset code and send to user
                        userBean.createPasswordResetCodeAndSendToUser(resetPasswordUser, SessionLocaleUtil.getI18nBundle(request), ServletUtil.getServerName(request));
                        
                        // Show login page
                        response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://").append(ServletUtil.getServerName(request)).append("/login?messageKey=").append("resetPasswordRequestAccepted").toString());
                    }
                    catch(NoResultException | NonUniqueResultException ex)
                    {
                        
                        request.setAttribute("errorMessageKey", 
                                                (ex instanceof NoResultException ? 
                                                        "emailNotRegistered" :
                                                        (ex instanceof NonUniqueResultException ?
                                                            "emailNotUnique":
                                                            null
                                                        )
                                                 )
                        );
                        request.getRequestDispatcher("/resetPasswordRequestForm.ftl").forward(request, response);
                    }
                }
                else
                {
                    // Send validation message back to form
                    request.setAttribute("formValidation", formValidation);
                    request.getRequestDispatcher("/resetPasswordRequestForm.ftl").forward(request, response);
                }
            }
            catch(FormValidationException ex)
            {
                ex.printStackTrace();
            }
        }
        else if(action.equals("resetPassword"))
        {
            String passwordResetCode = request.getParameter("passwordResetCode");
            try
            {
                UserAuthentication auth = em.createNamedQuery("UserAuthentication.findByPasswordResetCode", UserAuthentication.class)
                                            .setParameter("passwordResetCode", passwordResetCode)
                                            .getSingleResult();
                // You have 24 hours from reset request to actually doing this
                Date now = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(now);
                cal.add(Calendar.DATE, -1);
                if(auth.getPasswordResetCodeCreationTime().compareTo(cal.getTime()) < 0)
                {
                    // Too late, redirect to login page with error message
                    response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://").append(ServletUtil.getServerName(request)).append("/login?errorMessageKey=").append("passwordResetCodeExpired").toString());
                }
                else
                {
                    request.setAttribute("passwordResetCode",passwordResetCode);
                    request.getRequestDispatcher("/resetPasswordForm.ftl").forward(request, response);
                }
            }
            catch(NoResultException ex)
            {
                // Too late, redirect to login page with error message
                    response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://").append(ServletUtil.getServerName(request)).append("/login?errorMessageKey=").append("passwordResetCodeNotFound").toString());
            }
        }
        else if(action.equals("resetPasswordFormSubmit"))
        {
            String passwordResetCode = request.getParameter("passwordResetCode");
            try
            {
                UserAuthentication auth = em.createNamedQuery("UserAuthentication.findByPasswordResetCode", UserAuthentication.class)
                                            .setParameter("passwordResetCode", passwordResetCode)
                                            .getSingleResult();
                // You have 24 hours from reset request to actually doing this
                Date now = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(now);
                cal.add(Calendar.DATE, -1);
                if(auth.getPasswordResetCodeCreationTime().compareTo(cal.getTime()) < 0)
                {
                    // Too late, redirect to login page with error message
                    response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://").append(ServletUtil.getServerName(request)).append("/login?errorMessageKey=").append("passwordResetCodeExpired").toString());
                }
                else
                {
                    auth.setPasswordResetCode(null);
                    auth.setPasswordResetCodeCreationTime(null);
                    auth.setPassword(userBean.getMD5EncryptedString(request.getParameter("password")));
                    userBean.storeUserAuthentication(auth);
                    response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://").append(ServletUtil.getServerName(request)).append("/login?messageKey=").append("passwordResetSuccess").toString());
                }
            }
            catch(NoResultException ex)
            {
                // Too late, redirect to login page with error message
                    response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://").append(ServletUtil.getServerName(request)).append("/login?errorMessageKey=").append("passwordResetCodeNotFound").toString());
            }
        }
        else {
            redirectToLogin(request, response);
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
