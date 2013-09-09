package no.bioforsk.vips.logic.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import no.bioforsk.vips.coremanager.service.ManagerResource;
import no.bioforsk.vips.logic.entity.UserVipslogicRole;
import no.bioforsk.vips.logic.session.SessionControllerGetter;
import no.bioforsk.vips.logic.util.ServletUtil;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

/**
 * Logs a user in or out
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class LoginController extends HttpServlet {
    
    private final static String VIPSCOREMANAGER_URL = "https://localhost:8443/VIPSCoreManager";

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Resetting messages
        request.getSession().removeAttribute("messageKey");
        request.getSession().removeAttribute("errorMessageKey");
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String logout = request.getParameter("logout");
        
        if(logout != null && logout.equals("true"))
        {
            request.getSession().removeAttribute("user");
            request.getSession().setAttribute("messageKey","logoutsuccess");
            request.getRequestDispatcher("/login.ftl").forward(request, response);
        }
        else if(username != null && password != null)
        {
            try (PrintWriter out = response.getWriter()) {
                String nextPage = request.getParameter("nextPage");
                if(nextPage == null)
                {
                    nextPage="/";
                }
                Map<String,String> creds = new HashMap();
                creds.put("username", username);
                creds.put("password", password);

                Response resp = this.getManagerResource().getUserInfo(creds);
                if(resp.getStatus() == Response.Status.OK.getStatusCode())
                {
                    Map user = resp.readEntity(HashMap.class);
                    if(user != null)
                    {
                        List<UserVipslogicRole> roles = SessionControllerGetter.getUserBean().getUserVipslogicRole((Integer)user.get("vipsCoreUserId"));
                        user.put("roles", roles);
                        request.getSession().setAttribute("user", user);
                        response.sendRedirect(new StringBuilder("http://").append(ServletUtil.getServerName(request)).append(nextPage).toString());
                    }
                    else
                    {
                        request.setAttribute("errorMessageKey", "invalidcredentials");
                        request.getRequestDispatcher("/login.ftl").forward(request, response);
                    }
                }
                else
                {
                    request.setAttribute("errorMessageKey", "invalidcredentials");
                    request.getRequestDispatcher("/login.ftl").forward(request, response);
                }

            }
        }
        else
        {
            request.getRequestDispatcher("/login.ftl").forward(request, response);
        }
    }

    private ManagerResource getManagerResource()
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(VIPSCOREMANAGER_URL);
        ResteasyWebTarget rTarget = (ResteasyWebTarget) target;
        ManagerResource resource = rTarget.proxy(ManagerResource.class);
        return resource;
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
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
     * Handles the HTTP
     * <code>POST</code> method.
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
