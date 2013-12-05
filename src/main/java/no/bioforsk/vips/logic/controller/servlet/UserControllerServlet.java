package no.bioforsk.vips.logic.controller.servlet;

import java.io.IOException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.bioforsk.vips.logic.entity.UserAuthentication;
import no.bioforsk.vips.logic.entity.UserAuthenticationType;
import no.bioforsk.vips.logic.entity.VipsLogicUser;
import no.bioforsk.vips.util.ServletUtil;
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
            }
            if(userAuthenticationType.getUserAuthenticationTypeId().equals(UserAuthenticationType.TYPE_PASSWORD))
            {
                auth.setUsername(request.getParameter("username"));
                FormValidation formValidation = FormValidator.validateForm("userRegistrationForm",request,getServletContext());
                if(formValidation.isValid())
                {
                    // Populate object etc.
                    // Are there special fields??
                    System.out.println("Valid, presumably...");
                }
                else
                {
                    request.setAttribute("formValidation", formValidation);
                    request.setAttribute("organizations", em.createNamedQuery("Organization.findAll").getResultList());
                    request.setAttribute("userAuthenticationTypeId", userAuthenticationType.getUserAuthenticationTypeId());
                    request.getRequestDispatcher("/userRegistrationForm.ftl").forward(request, response);
                    return;
                }
                
                /*
                String password = request.getParameter("password");
                String password2 = request.getParameter("password2");
                if(password == null || password2 == null || ! password.equals(password2))
                {
                    request.setAttribute("errorMessageKey", password == null || password2 == null ? "passwordNotRepeated" : "passwordsDoNotMatch");
                    // Get available organizations, add for select list
                    request.setAttribute("organizations", em.createNamedQuery("Organization.findAll").getResultList());
                    request.setAttribute("userAuthenticationTypeId", userAuthenticationType.getUserAuthenticationTypeId());
                    request.getRequestDispatcher("/userRegistrationForm.ftl").forward(request, response);
                    return;
                }
                // Evaluate password strength
                try
                {
                    Locale currentLocale = SessionLocaleUtil.getCurrentLocale(request);
                    SessionControllerGetter.getUserBean().isPasswordValid(password, currentLocale);
                }
                catch(PasswordValidationException ex)
                {
                    request.setAttribute("errorMessage", ex.getMessage());
                    // Get available organizations, add for select list
                    request.setAttribute("organizations", em.createNamedQuery("Organization.findAll").getResultList());
                    request.setAttribute("userAuthenticationTypeId", userAuthenticationType.getUserAuthenticationTypeId());
                    request.getRequestDispatcher("/userRegistrationForm.ftl").forward(request, response);
                    return;
                }
                auth.setPassword(password);
                
                // Validate email
                String email = request.getParameter("email");
                if(email == null || !WebUtil.isValidEmailAddress(email))
                {
                    request.setAttribute("errorMessageKey", "invalidEmailAddress");
                    // Get available organizations, add for select list
                    request.setAttribute("organizations", em.createNamedQuery("Organization.findAll").getResultList());
                    request.setAttribute("userAuthenticationTypeId", userAuthenticationType.getUserAuthenticationTypeId());
                    request.getRequestDispatcher("/userRegistrationForm").forward(request, response);
                    return;
                }
                
                String firstName = request.getParameter("firstName");
                String lastName = request.getParameter("lastName");
                
                if(lastName == null)
                {
                    request.setAttribute("errorMessageKey", "missingValues");
                }
                
                PrintWriter out = response.getWriter();
                out.print("Tjobing!!!!");
                */
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
