package no.bioforsk.vips.logic.controller.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.bioforsk.vips.logic.controller.session.ForecastBean;
import no.bioforsk.vips.logic.controller.session.UserBean;
import no.bioforsk.vips.logic.entity.VipsLogicRole;
import no.bioforsk.vips.logic.entity.VipsLogicUser;
import no.bioforsk.vips.logic.entity.ForecastConfiguration;
import no.bioforsk.vips.logic.util.SessionControllerGetter;
import no.bioforsk.vips.util.ServletUtil;
import no.bioforsk.web.forms.FormField;
import no.bioforsk.web.forms.FormValidation;
import no.bioforsk.web.forms.FormValidationException;
import no.bioforsk.web.forms.FormValidator;

/**
 * Handles form configuration actions
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class ForecastConfigurationController extends HttpServlet {
    
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
        response.setContentType("text/html;charset=UTF-8");
        
        String action = request.getParameter("action");
        VipsLogicUser user = (VipsLogicUser) request.getSession().getAttribute("user");
        ForecastBean forecastBean = SessionControllerGetter.getForecastBean();
        UserBean userBean = SessionControllerGetter.getUserBean();
        
        // Default: View list of users
        // for SUPERUSERS and ORGANIZATION ADMINS
        if(action == null)
        {
            if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
            {
                List<ForecastConfiguration> forecasts;
                if(user.isSuperUser())
                {
                    forecasts = forecastBean.getForecastConfigurations();
                }
                else
                {
                    forecasts = forecastBean.getForecastConfigurations(user.getOrganizationId());
                }
                request.setAttribute("forecastConfigurations", forecasts);
                request.setAttribute("modelInformation", forecastBean.getIndexedModelInformation());
                // If this is a redirect from a controller, with a message to be passed on
                request.setAttribute("messageKey", request.getParameter("messageKey"));
                request.getRequestDispatcher("/forecastConfigurationList.ftl").forward(request, response);
            }
            else
            {
                response.sendError(403,"Access not authorized"); // HTTP Forbidden
            }
      
        }
        
        // View and edit a forecast configuration
        // for SUPERUSERS and ORGANIZATION ADMINS
        else if(action.equals("viewForecastConfiguration"))
        {
            if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
            {
                try
                {
                    Long forecastConfigurationId = Long.valueOf(request.getParameter("forecastConfigurationId"));
                    ForecastConfiguration forecastConfiguration = em.find(ForecastConfiguration.class, forecastConfigurationId);
                    // No forecastconfiguration found, assuming user want to register new
                    if(forecastConfiguration == null)
                    {
                        forecastConfiguration = new ForecastConfiguration();
                    }
                    // Only superusers can view and edit forecasts from other organizations
                    if(! user.isSuperUser() && forecastConfiguration.getVipsLogicUserId() != null && !forecastConfiguration.getVipsLogicUserId().getOrganizationId().equals(user.getOrganizationId()))
                    {
                        response.sendError(403,"Access not authorized"); // HTTP Forbidden
                    }
                    else
                    {
                        // TODO: More intelligent selection of locations, weather stations and users
                        request.setAttribute("locationPointOfInterests", em.createNamedQuery("PointOfInterest.findAll").getResultList());
                        request.setAttribute("weatherStationPointOfInterests", em.createNamedQuery("PointOfInterestWeatherStation.findAll").getResultList());
                        request.setAttribute("vipsLogicUsers", em.createNamedQuery("VipsLogicUser.findAll").getResultList());
                        request.setAttribute("forecastConfiguration", forecastConfiguration);
                        request.setAttribute("modelInformations", em.createNamedQuery("ModelInformation.findAll").getResultList());
                        request.setAttribute("messageKey", request.getParameter("messageKey"));
                        request.getRequestDispatcher("/forecastConfigurationForm.ftl").forward(request, response);
                       
                    }
                }
                catch(NullPointerException | NumberFormatException ex)
                {
                    response.sendError(500, "Invalid forecast configurationId " + request.getParameter("forecastConfigurationId"));
                }
                
            }
            else
            {
                response.sendError(403,"Access not authorized"); // HTTP Forbidden
            }
        }
        
        // Store forecast configuration
        // Authorization: SUPERUSERS and ORGANIZATION ADMINS
        else if(action.equals("forecastConfigurationFormSubmit"))
        {
            if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
            {
                try
                {
                    Long forecastConfigurationId = Long.valueOf(request.getParameter("forecastConfigurationId"));
                    ForecastConfiguration forecastConfiguration = em.find(ForecastConfiguration.class, forecastConfigurationId);
                    // No forecastconfiguration found, assuming user want to register new
                    if(forecastConfiguration == null)
                    {
                        forecastConfiguration = new ForecastConfiguration();
                    }
                    // Only superusers can view and edit forecasts from other organizations
                    if(! user.isSuperUser() && forecastConfiguration.getVipsLogicUserId() != null && !forecastConfiguration.getVipsLogicUserId().getOrganizationId().equals(user.getOrganizationId()))
                    {
                        response.sendError(403,"Access not authorized"); // HTTP Forbidden
                    }
                    else
                    {
                        // Standard form validation
                        FormValidation formValidation = FormValidator.validateForm("forecastConfigurationForm",request,getServletContext());
                        // Also validation the model specific fields
                        String modelId = formValidation.getFormField("modelId").getWebValue();
                        FormValidation modelFieldFormValidation = FormValidator.validateForm("models/" + modelId, request, getServletContext());
                        
                        if(formValidation.isValid() && modelFieldFormValidation.isValid())
                        {
                            forecastConfiguration = forecastBean.storeForecastConfiguration(forecastConfiguration, formValidation.getFormFields(), modelFieldFormValidation.getFormFields());
                            // Store form config
                            

                            //forecastBean.storeForecastModelConfigurations(forecastConfiguration, modelFieldFormValidation.getFormFields());
                            // TODO: Store model specific fields
                            // First: Delete all o
                            
                            
                            request.setAttribute("messageKey", request.getParameter("formConfigurationUpdated"));
                            response.sendRedirect(new StringBuilder("http://").append(ServletUtil.getServerName(request)).append("/forecastConfiguration?action=viewForecastConfiguration&forecastConfigurationId=").append(forecastConfiguration.getForecastConfigurationId()).append("&messageKey=").append("forecastConfigurationUpdated").toString());

                        }
                        else
                        {
                            // Return to form with error messages
                            request.setAttribute("formValidation", formValidation);
                            // We must get date formats!
                            Map<String, FormField> formFields = FormValidator.getFormFields("forecastConfigurationForm",getServletContext());
                            // TODO: More intelligent selection of locations, weather stations and users
                            request.setAttribute("locationPointOfInterests", em.createNamedQuery("PointOfInterest.findAll").getResultList());
                            request.setAttribute("weatherStationPointOfInterests", em.createNamedQuery("PointOfInterestWeatherStation.findAll").getResultList());
                            request.setAttribute("vipsLogicUsers", em.createNamedQuery("VipsLogicUser.findAll").getResultList());
                            request.setAttribute("dateStart_dateFormat", formFields.get("dateStart").getDateFormat());
                            request.setAttribute("dateEnd_dateFormat", formFields.get("dateEnd").getDateFormat());
                            request.setAttribute("modelInformations", em.createNamedQuery("ModelInformation.findAll").getResultList());
                            request.setAttribute("forecastConfiguration", forecastConfiguration);
                            request.getRequestDispatcher("/forecastConfigurationForm.ftl").forward(request, response);
                        }
                    }
                }
                catch(NullPointerException | NumberFormatException | FormValidationException ex)
                {
                    if(ex instanceof NumberFormatException)
                    {
                        response.sendError(500, "Invalid forecast configurationId " + request.getParameter("forecastConfigurationId"));
                    }
                    else
                    {
                        response.sendError(500, ex.getMessage());
                    }
                }
            }
            else
            {
                response.sendError(403,"Access not authorized"); // HTTP Forbidden
            }
        }
        // Delete forecast configuration
        // Authorization: SUPERUSERS and ORGANIZATION ADMINS
        else if(action.equals("deleteForecastConfiguration"))
        {
            if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
            {
                try
                {
                    Long forecastConfigurationId = Long.valueOf(request.getParameter("forecastConfigurationId"));
                    ForecastConfiguration forecastConfiguration = em.find(ForecastConfiguration.class, forecastConfigurationId);

                    // Only superusers can delete forecasts from other organizations
                    if(! user.isSuperUser() && forecastConfiguration.getVipsLogicUserId() != null && !forecastConfiguration.getVipsLogicUserId().getOrganizationId().equals(user.getOrganizationId()))
                    {
                        response.sendError(403,"Access not authorized"); // HTTP Forbidden
                    }
                    else
                    {
                        forecastBean.deleteForecastConfiguration(forecastConfigurationId);
                        response.sendRedirect(new StringBuilder("http://").append(ServletUtil.getServerName(request)).append("/forecastConfiguration?").append("&messageKey=").append("forecastConfigurationDeleted").toString());
                       
                    }
                }
                catch(NullPointerException | NumberFormatException ex)
                {
                    response.sendError(500, "Invalid forecast configurationId " + request.getParameter("forecastConfigurationId"));
                }
                
            }
            else
            {
                response.sendError(403,"Access not authorized"); // HTTP Forbidden
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
