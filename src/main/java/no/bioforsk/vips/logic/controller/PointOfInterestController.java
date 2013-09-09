package no.bioforsk.vips.logic.controller;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.bioforsk.vips.logic.session.SessionControllerGetter;

/**
 * Handles transactions for POIs
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class PointOfInterestController extends HttpServlet {

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
        
        request.getSession().removeAttribute("weatherStations");
        request.getSession().removeAttribute("weatherStation");
        
        Map userInfo = (Map) request.getSession().getAttribute("user");
        Integer userId = (Integer) userInfo.get("vipsCoreUserId");
        
        if(request.getServletPath().equals("/weatherStation"))
        {
            String pointOfInterestId = request.getParameter("pointOfInterestId");
            if(pointOfInterestId == null)
            {
                request.getSession().setAttribute("weatherStations", SessionControllerGetter.getPointOfInterestBean().getWeatherstationsForUser(userId));
                request.getRequestDispatcher("/weatherstationlist.ftl").forward(request, response);
            }
            else
            {
                request.getSession().setAttribute("weatherStation", SessionControllerGetter.getPointOfInterestBean().getPointOfInterest(Integer.valueOf(pointOfInterestId)));
                request.getRequestDispatcher("/weatherstation.ftl").forward(request, response);
            }
        }
        
        
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
