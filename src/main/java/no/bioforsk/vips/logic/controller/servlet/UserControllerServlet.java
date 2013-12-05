package no.bioforsk.vips.logic.controller.servlet;

import java.io.IOException;
import java.util.HashSet;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.bioforsk.vips.logic.entity.Organization;
import no.bioforsk.vips.logic.entity.UserAuthentication;
import no.bioforsk.vips.logic.entity.UserAuthenticationType;
import no.bioforsk.vips.logic.entity.VipsLogicUser;
import no.bioforsk.vips.logic.i18n.SessionLocaleUtil;
import no.bioforsk.vips.logic.util.Globals;
import no.bioforsk.vips.logic.util.SessionControllerGetter;
import no.bioforsk.vips.util.MD5Encrypter;
import no.bioforsk.vips.util.ServletUtil;
import no.bioforsk.web.forms.FormField;
import no.bioforsk.web.forms.FormValidation;
import no.bioforsk.web.forms.FormValidator;

/**
 * Handles user actions
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class UserControllerServlet extends HttpServlet {

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
        
        if(action == null)
        {
            response.sendRedirect("/");
            return;
        }
        
        // Show user application form
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
        else if(action.equals("registerNewUserFormSubmit"))
        {
            VipsLogicUser user = new VipsLogicUser();
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
            if(userAuthenticationType.getUserAuthenticationTypeId().equals(UserAuthenticationType.TYPE_PASSWORD))
            {
                // Standard form validation
                FormValidation formValidation = FormValidator.validateForm("userRegistrationForm",request,getServletContext());
                // Extra check: That user's email is not already in use
                FormField emailField = formValidation.getFormField("email");
                if(SessionControllerGetter.getUserBean().getUserByEmail(emailField.getWebValue()) != null)
                {
                    emailField.setValid(false);
                    emailField.setValidationMessage(SessionLocaleUtil.getI18nText(request, "emailAddressIsAlreadyInUse"));
                }
                
                if(formValidation.isValid())
                {
                    
                    // Setting authentication info first
                    auth.setUserAuthenticationType(userAuthenticationType);
                    auth.setUsername(formValidation.getFormField("username").getWebValue());
                    auth.setPassword(SessionControllerGetter.getUserBean().getMD5EncryptedString(formValidation.getFormField("password").getWebValue()));
                    
                    // Adding user info
                    user.setEmail(formValidation.getFormField("email").getWebValue());
                    user.setFirstName(formValidation.getFormField("firstName").getWebValue());
                    user.setLastName(formValidation.getFormField("lastName").getWebValue());
                    Organization organization = em.find(Organization.class, formValidation.getFormField("organizationId").getValueAsInteger());
                    user.setOrganizationId(organization);
                    user.setApprovalApplication(formValidation.getFormField("approvalApplication").getWebValue());
                    user.setUserStatusId(Globals.USER_STATUS_AWAITING_EMAIL_VERIFICATION);
                    SessionControllerGetter.getUserBean().storeUserFirstTime(user, auth);
                    response.sendRedirect(new StringBuilder("http://").append(ServletUtil.getServerName(request)).append("/user?action=registerNewUserFormReceipt").toString());
                }
                else
                {
                    request.setAttribute("formValidation", formValidation);
                    request.setAttribute("organizations", em.createNamedQuery("Organization.findAll").getResultList());
                    request.setAttribute("userAuthenticationTypeId", userAuthenticationType.getUserAuthenticationTypeId());
                    request.getRequestDispatcher("/userRegistrationForm.ftl").forward(request, response);
                    return;
                }
                
            }
                
        }
        else if(action.equals("registerNewUserFormReceipt"))
        {
            request.setAttribute("messageKey","registerNewUserReceipt");
            request.getRequestDispatcher("/login.ftl").forward(request, response);
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
