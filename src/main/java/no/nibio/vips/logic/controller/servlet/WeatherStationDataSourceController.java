/*
 * Copyright (c) 2024 NIBIO <http://www.nibio.no/>. 
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nibio.vips.gis.LonLatStringFormatException;
import no.nibio.vips.logic.controller.session.PointOfInterestBean;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.entity.WeatherStationDataSource;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.ServletUtil;
import no.nibio.web.forms.FormValidation;
import no.nibio.web.forms.FormValidationException;
import no.nibio.web.forms.FormValidator;;

/**
 * Handles actions regarding listing and modifying weather station data sources
 * 
 * @copyright 2024 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class WeatherStationDataSourceController  extends HttpServlet{

    private static Logger LOGGER = LoggerFactory.getLogger(WeatherStationDataSourceController.class);

    @EJB
    PointOfInterestBean poiBean;

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

        if(action == null)
        {
            List<WeatherStationDataSource> wsDataSources = poiBean.getWeatherStationDataSources();
            request.setAttribute("messageKey", request.getParameter("messageKey") != null ? request.getParameter("messageKey") : null);
            request.setAttribute("weatherStationDataSources", wsDataSources);
            request.getRequestDispatcher("/weatherStationDataSourceList.ftl").forward(request, response);
        }
        else if(action.equals("editWeatherStationDataSource") || action.equals("newWeatherStationDataSource"))
        {
            WeatherStationDataSource weatherStationDataSource = null;
            try 
            { 
                weatherStationDataSource = poiBean.getWeatherStationDataSource(Integer.valueOf(request.getParameter("weatherStationDataSourceId")));
            }
            catch(NullPointerException | NumberFormatException ex){}

            if(weatherStationDataSource == null)
            {
                LOGGER.debug("Could not find weather data source with ID = " + request.getParameter("weatherStationDataSourceId"));
                weatherStationDataSource = new WeatherStationDataSource();
            }
            
            request.setAttribute("messageKey", request.getParameter("messageKey") != null ? request.getParameter("messageKey") : null);
            request.setAttribute("weatherStationDataSource", weatherStationDataSource);
            request.getRequestDispatcher("/weatherStationDataSourceForm.ftl").forward(request, response);
        }
        else if(action.equals("weatherStationDataSourceFormSubmit"))
        {
            try
            {
                // Check that the provided id is either -1 or it already exists
                Integer weatherStationDataSourceId = null;
                WeatherStationDataSource weatherStationDataSource;
                try
                {
                    weatherStationDataSourceId = Integer.valueOf(request.getParameter("weatherStationDataSourceId"));
                }
                catch(NullPointerException | NumberFormatException ex)
                {
                    response.sendError(400,"Wrong format of id for weather station data source");
                }
                if(weatherStationDataSourceId < 0)
                {
                    weatherStationDataSource = new WeatherStationDataSource();
                }
                else 
                {
                    weatherStationDataSource = poiBean.getWeatherStationDataSource(weatherStationDataSourceId);
                    if(weatherStationDataSource == null)
                    {
                        response.sendError(404,"Could not find weather station data source with id=" + weatherStationDataSourceId);
                    }
                }
                FormValidation formValidation = FormValidator.validateForm("weatherStationDataSourceForm", request, getServletContext());
                if(formValidation.isValid())
                {
                    weatherStationDataSource.setName(formValidation.getFormField("name").getWebValue());
                    weatherStationDataSource.setDefaultDescription(formValidation.getFormField("defaultDescription").getWebValue());
                    weatherStationDataSource.setUri(formValidation.getFormField("uri").getWebValue());
                    weatherStationDataSource.setDatafetchUriExpression(formValidation.getFormField("datafetchUriExpression").getWebValue());
                    weatherStationDataSource.setInfoUriExpression(formValidation.getFormField("infoUriExpression").getWebValue());
                    weatherStationDataSource.setIsGrid(formValidation.getFormField("isGrid").getWebValue() != null);
                    weatherStationDataSource = poiBean.storeWeatherStationDataSource(weatherStationDataSource);

                    response.sendRedirect(
                            Globals.PROTOCOL + "://" + ServletUtil.getServerName(request)
                            + "/weatherstationdatasource?action=editWeatherStationDataSource&weatherStationDataSourceId=" + weatherStationDataSource.getWeatherStationDataSourceId()
                            + "&messageKey=weatherStationDataSourceStored" 
                    );
                }
                else
                {
                    request.setAttribute("formValidation", formValidation);
                    request.setAttribute("weatherStationDataSource", weatherStationDataSource);
                    LOGGER.debug("Form NOT valid. Dispatching");
                    request.getRequestDispatcher("/weatherStationDataSourceForm.ftl").forward(request, response);
                }
            }
            catch(FormValidationException ex)
            {
                ex.printStackTrace();
                response.sendError(500, ex.getClass().toString() + ": " + ex.getMessage());
            }
        }
        else if(action.equals("deleteWeatherStationDataSource"))
        {

            try
            {
                Integer weatherStationDataSourceId = Integer.valueOf(request.getParameter("weatherStationDataSourceId"));
                WeatherStationDataSource weatherStationDataSource = poiBean.getWeatherStationDataSource(weatherStationDataSourceId);
                if(weatherStationDataSource == null)
                {
                    response.sendError(404,"Could not find weather station data source with id=" + weatherStationDataSourceId);
                }
                // Check: Can it be deleted? Not if organizations or weatherStations refer to it
                if(poiBean.isweatherStationDataSourceDeleteable(weatherStationDataSource))
                {
                    poiBean.deleteWeatherStationDataSource(weatherStationDataSource);
                    // Redirect to list from which the deleted item has been removed
                    response.sendRedirect(
                            Globals.PROTOCOL + "://" + ServletUtil.getServerName(request)
                            + "/weatherstationdatasource?messageKey=weatherStationDataSourceDeleted"
                    );
                }
                else 
                {
                    response.sendError(403,weatherStationDataSource.getName() + " is in use by one or more organizations, so it cannot be deleted. Hit the browser's back button return to your form.");
                }
            }
            catch(NullPointerException | NumberFormatException ex)
            {
                response.sendError(400,"Wrong format of id for weather station data source");
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
