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
import java.util.List;
import java.util.TimeZone;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.bioforsk.vips.logic.entity.PointOfInterest;
import no.bioforsk.vips.logic.entity.PointOfInterestWeatherStation;
import no.bioforsk.vips.logic.entity.VipsLogicUser;
import no.bioforsk.vips.logic.util.SessionControllerGetter;

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
        
        VipsLogicUser user = (VipsLogicUser) request.getSession().getAttribute("user");
        
        /*
        System.out.println("PathInfo=" + request.getPathInfo());
        System.out.println("PathTranslated=" + request.getPathTranslated());
        System.out.println("ServletPath=" + request.getServletPath());
        System.out.println("RequestURI=" + request.getRequestURI());
        System.out.println("RequestURL=" + request.getRequestURL());
        */
        
        if(request.getServletPath().equals("/weatherStation"))
        {
            // Decide if requsted view is list view or single view
            String pointOfInterestId = request.getParameter("pointOfInterestId");
            // List view
            if(pointOfInterestId == null)
            {
                List<PointOfInterestWeatherStation> weatherStations;
                if(user.isSuperUser()){
                    weatherStations = SessionControllerGetter.getPointOfInterestBean().getAllWeatherStations();
                }
                else
                {
                    weatherStations = SessionControllerGetter.getPointOfInterestBean().getWeatherstationsForUser(user.getUserId());
                }
                request.getSession().setAttribute("weatherStations", weatherStations);
                request.getRequestDispatcher("/weatherstationlist.ftl").forward(request, response);
            }
            // Single view
            else
            {
                PointOfInterest weatherStation = SessionControllerGetter.getPointOfInterestBean().getPointOfInterest(Integer.valueOf(pointOfInterestId));
                if(weatherStation instanceof PointOfInterestWeatherStation)
                {
                    PointOfInterestWeatherStation stationWithDataSource = (PointOfInterestWeatherStation) weatherStation;
                    request.getSession().setAttribute("weatherStation", stationWithDataSource);
                }
                else
                {
                    request.getSession().setAttribute("weatherStation", weatherStation);
                }
                request.getSession().setAttribute("availableTimeZones", TimeZone.getAvailableIDs());
                request.getSession().setAttribute("defaultTimeZoneId", TimeZone.getDefault().getID());
                
                // Dispatch to read or write view?
                if(request.getPathInfo() != null && request.getPathInfo().equals("/edit"))
                {
                    request.getRequestDispatcher("/weatherstationForm.ftl").forward(request, response);
                }
                else
                {
                    request.getRequestDispatcher("/weatherstation.ftl").forward(request, response);
                }
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
