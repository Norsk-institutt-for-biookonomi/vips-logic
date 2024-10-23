/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * <https://www.gnu.org/licenses/>.
 *
 */

package no.nibio.vips.logic.controller.servlet;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ibm.icu.util.Calendar;
import no.nibio.vips.logic.controller.session.ForecastBean;
import no.nibio.vips.logic.controller.session.OrganismBean;
import no.nibio.vips.logic.controller.session.PointOfInterestBean;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.ModelInformation;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.VipsLogicRole;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.ArrayUtil;
import no.nibio.vips.util.ServletUtil;
import no.nibio.web.forms.FormField;
import no.nibio.web.forms.FormValidation;
import no.nibio.web.forms.FormValidationException;
import no.nibio.web.forms.FormValidator;

/**
 * Handles form configuration actions
 * 
 * @copyright 2020-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class ForecastConfigurationController extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForecastConfigurationController.class);

    @PersistenceContext(unitName = "VIPSLogic-PU")
    EntityManager em;

    @EJB
    ForecastBean forecastBean;

    @EJB
    UserBean userBean;

    @EJB
    OrganismBean organismBean;

    @EJB
    PointOfInterestBean pointOfInterestBean;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
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

        // Default: View list of forecast configurations
        // for SUPERUSERS and ORGANIZATION ADMINS
        if (action == null) {
            Map<String, ModelInformation> modelInformationMap = forecastBean.getIndexedBatchableModelInformation();
            if (userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER)) {
                // Check filtering conditions
                List<String> selectedModelIds;
                try {
                    selectedModelIds = Arrays.asList(request.getParameterValues("modelId"));
                } catch (NullPointerException ex) {
                    selectedModelIds = new ArrayList(modelInformationMap.keySet());
                }

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date from, to;
                try {
                    from = format.parse(request.getParameter("from"));
                    to = format.parse(request.getParameter("to"));
                } catch (NullPointerException | ParseException ex) {
                    to = SystemTime.getSystemTime();
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(to);
                    cal.add(Calendar.MONTH, -2);
                    from = cal.getTime();
                }

                List<ForecastConfiguration> forecasts;

                if (user.isSuperUser()) {
                    // Filtering of organizations
                    List<Organization> organizations = userBean.getOrganizations();
                    List<Integer> selectedOrganizationIds;
                    try {
                        selectedOrganizationIds = Arrays.asList(
                                ArrayUtil.stringArrayToIntegerArray(request.getParameterValues("organizationId")));
                    }
                    // No filter set -> select all organizations (default)
                    catch (NullPointerException ex) {
                        selectedOrganizationIds = new ArrayList<>();
                        for (Organization o : organizations) {
                            selectedOrganizationIds.add(o.getOrganizationId());
                        }
                    }
                    forecasts =
                            forecastBean.getForecastConfigurations(selectedOrganizationIds, selectedModelIds, from, to);

                    request.setAttribute("organizations", organizations);
                    request.setAttribute("selectedOrganizationIds", selectedOrganizationIds);

                    request.setAttribute("allUsers", userBean.getAllUsers());
                    // If super user requests private forecasts for a user
                    try {
                        Integer otherUserId = Integer.valueOf(request.getParameter("otherUserId"));
                        List<ForecastConfiguration> privateForecastConfigurationsForOtherUser =
                                forecastBean.getPrivateForecastConfigurationsForUser(otherUserId);
                        request.setAttribute("privateForecastConfigurationsForOtherUser",
                                privateForecastConfigurationsForOtherUser);
                        request.setAttribute("otherUserId", otherUserId);
                    } catch (NumberFormatException nfe) {
                    }
                } else {
                    forecasts = forecastBean.getForecastConfigurations(user.getOrganizationId(), selectedModelIds, from,
                            to);
                }
                Collections.sort(forecasts);

                request.setAttribute("currentLanguage", SessionLocaleUtil.getCurrentLocale(request).getLanguage());
                request.setAttribute("forecastConfigurations", forecasts);

                List<ForecastConfiguration> privateForecasts;
                privateForecasts = forecastBean.getPrivateForecastConfigurationsForUser(user.getUserId());
                request.setAttribute("privateForecastConfigurations", privateForecasts);
                request.setAttribute("modelInformation", modelInformationMap);
                request.setAttribute("selectedModelIds", selectedModelIds);
                request.setAttribute("from", from);
                request.setAttribute("to", to);
                // If this is a redirect from a controller, with a message to be passed on
                request.setAttribute("messageKey", request.getParameter("messageKey"));
                request.getRequestDispatcher("/forecastConfigurationList.ftl").forward(request, response);
            } else {
                List<ForecastConfiguration> privateForecasts;
                privateForecasts = forecastBean.getPrivateForecastConfigurationsForUser(user.getUserId());
                request.setAttribute("privateForecastConfigurations", privateForecasts);
                request.setAttribute("modelInformation", modelInformationMap);
                request.setAttribute("messageKey", request.getParameter("messageKey"));
                request.getRequestDispatcher("/forecastConfigurationList.ftl").forward(request, response);
            }

        }

        // View and edit a forecast configuration
        else if (action.equals("viewForecastConfiguration")) {
            if (userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER)) {
                try {
                    Long forecastConfigurationId = Long.valueOf(request.getParameter("forecastConfigurationId"));
                    ForecastConfiguration forecastConfiguration =
                            em.find(ForecastConfiguration.class, forecastConfigurationId);
                    boolean multipleNew = false;
                    // No forecastconfiguration found, assuming user want to register new
                    if (forecastConfiguration == null) {
                        forecastConfiguration = new ForecastConfiguration();
                        multipleNew = request.getParameter("multipleNew") != null
                                && request.getParameter("multipleNew").equals("true");
                    }
                    // Only superusers can view and edit forecasts from other organizations
                    if (!user.isSuperUser() && forecastConfiguration.getVipsLogicUserId() != null
                            && !forecastConfiguration.getVipsLogicUserId().getOrganizationId()
                                    .equals(user.getOrganizationId())) {
                        response.sendError(403, "Access not authorized"); // HTTP Forbidden
                    }
                    // Only superusers can view and edit private forecasts from other users
                    else if (forecastConfiguration.getIsPrivate() && !user.isSuperUser()
                            && forecastConfiguration.getVipsLogicUserId() != null
                            && !forecastConfiguration.getVipsLogicUserId().getUserId().equals(user.getUserId())) {
                        response.sendError(403, "Access not authorized"); // HTTP Forbidden
                    } else {
                        // TODO: More intelligent selection of locations, weather stations and users
                        if (user.isSuperUser()) {
                            request.setAttribute("locationPointOfInterests",
                                    em.createNamedQuery("PointOfInterest.findAll").getResultList());
                            request.setAttribute("weatherStationPointOfInterests",
                                    em.createNamedQuery("PointOfInterestWeatherStation.findAllByActivity")
                                            .setParameter("active", Boolean.TRUE).getResultList());
                            request.setAttribute("vipsLogicUsers",
                                    em.createNamedQuery("VipsLogicUser.findAll").getResultList());
                        } else {
                            request.setAttribute("locationPointOfInterests",
                                    em.createNamedQuery("PointOfInterest.findByOrganizationId")
                                            .setParameter("organizationId", user.getOrganizationId()).getResultList());
                            request.setAttribute("weatherStationPointOfInterests",
                                    em.createNamedQuery("PointOfInterestWeatherStation.findByActivityAndOrganizationId")
                                            .setParameter("active", Boolean.TRUE)
                                            .setParameter("organizationId", user.getOrganizationId()).getResultList());
                            request.setAttribute("vipsLogicUsers",
                                    em.createNamedQuery("VipsLogicUser.findByOrganizationId")
                                            .setParameter("organizationId", user.getOrganizationId()).getResultList());
                        }
                        request.setAttribute("currentLanguage",
                                SessionLocaleUtil.getCurrentLocale(request).getLanguage());
                        request.setAttribute("forecastConfiguration", forecastConfiguration);
                        request.setAttribute("formId",
                                multipleNew ? "forecastConfigurationMultipleNewForm" : "forecastConfigurationForm");
                        request.setAttribute("multipleNew", multipleNew);
                        request.getSession().setAttribute("availableTimeZones", SystemTime.getAvailableTimeZones());
                        request.getSession().setAttribute("defaultTimeZoneId",
                                user.getOrganizationId().getDefaultTimeZone());
                        // All organisms used for parent organism list
                        request.setAttribute("allCrops", em.createNamedQuery("Organism.findAllCrops").getResultList());
                        request.setAttribute("allPests", em.createNamedQuery("Organism.findAllPests").getResultList());
                        // Hierarchy categories
                        request.setAttribute("hierarchyCategories",
                                organismBean.getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                        request.setAttribute("modelInformations", forecastBean.getBatchableModels());
                        request.setAttribute("messageKey", request.getParameter("messageKey"));
                        request.getRequestDispatcher("/forecastConfigurationForm.ftl").forward(request, response);

                    }
                } catch (NullPointerException | NumberFormatException ex) {
                    response.sendError(500,
                            "Invalid forecast configurationId " + request.getParameter("forecastConfigurationId"));
                }
            } else {
                Long forecastConfigurationId = Long.valueOf(request.getParameter("forecastConfigurationId"));
                ForecastConfiguration forecastConfiguration =
                        em.find(ForecastConfiguration.class, forecastConfigurationId);
                if (forecastConfiguration == null
                        || forecastConfiguration.getVipsLogicUserId().getUserId().equals(user.getUserId())) {
                    if (forecastConfiguration == null) {
                        forecastConfiguration = new ForecastConfiguration();
                        forecastConfiguration.setIsPrivate(Boolean.TRUE);
                    }
                    request.setAttribute("currentLanguage", SessionLocaleUtil.getCurrentLocale(request).getLanguage());
                    request.setAttribute("forecastConfiguration", forecastConfiguration);
                    request.setAttribute("locationPointOfInterests",
                            em.createNamedQuery("PointOfInterest.findByOrganizationId")
                                    .setParameter("organizationId", user.getOrganizationId()).getResultList());
                    request.setAttribute("weatherStationPointOfInterests",
                            em.createNamedQuery("PointOfInterestWeatherStation.findByActivityAndOrganizationId")
                                    .setParameter("active", Boolean.TRUE)
                                    .setParameter("organizationId", user.getOrganizationId()).getResultList());
                    request.setAttribute("formId", "forecastConfigurationForm");
                    request.getSession().setAttribute("availableTimeZones", SystemTime.getAvailableTimeZones());
                    request.getSession().setAttribute("defaultTimeZoneId",
                            user.getOrganizationId().getDefaultTimeZone());

                    request.setAttribute("allCrops", em.createNamedQuery("Organism.findAllCrops").getResultList());
                    request.setAttribute("allPests", em.createNamedQuery("Organism.findAllPests").getResultList());
                    // Hierarchy categories
                    request.setAttribute("hierarchyCategories",
                            organismBean.getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                    request.setAttribute("modelInformations",
                            em.createNamedQuery("ModelInformation.findAll").getResultList());
                    request.setAttribute("messageKey", request.getParameter("messageKey"));
                    request.getRequestDispatcher("/forecastConfigurationForm.ftl").forward(request, response);
                } else {
                    response.sendError(403, "Access not authorized");
                }
            }
        }

        // Store forecast configuration(s)
        // Authorization: SUPERUSERS and ORGANIZATION ADMINS
        else if (action.equals("forecastConfigurationFormSubmit")) {
            try {
                Long forecastConfigurationId = Long.valueOf(request.getParameter("forecastConfigurationId"));
                ForecastConfiguration forecastConfiguration =
                        em.find(ForecastConfiguration.class, forecastConfigurationId);
                // No forecastconfiguration found, assuming user want to register new
                if (forecastConfiguration == null) {
                    forecastConfiguration = new ForecastConfiguration();
                }
                // Only superusers can view and edit forecasts from other organizations
                // Regular users can only edit own forecasts
                if ((!user.isSuperUser() && forecastConfiguration.getVipsLogicUserId() != null
                        && !forecastConfiguration.getVipsLogicUserId().getOrganizationId()
                                .equals(user.getOrganizationId()))
                        || (!user.isSuperUser() && !user.isOrganizationAdmin()
                                && forecastConfiguration.getVipsLogicUserId() != null
                                && !forecastConfiguration.getVipsLogicUserId().getUserId().equals(user.getUserId()))) {
                    response.sendError(403, "Access not authorized"); // HTTP Forbidden
                } else {
                    String formId = request.getParameter("multipleNew") != null
                            && request.getParameter("multipleNew").equals("true")
                                    ? "forecastConfigurationMultipleNewForm"
                                    : "forecastConfigurationForm";
                    FormValidation formValidation = FormValidator.validateForm(formId, request, getServletContext());
                    LOGGER.debug("formValidation=" + formValidation.isValid());
                    // Also validation the model specific fields
                    String modelId = formValidation.getFormField("modelId").getWebValue();
                    FormValidation modelFieldFormValidation =
                            FormValidator.validateForm("models/" + modelId, request, getServletContext());

                    // Additional input check: If the Grid data checkbox is not checked, a

                    if (formValidation.isValid() && modelFieldFormValidation.isValid()) {
                        if (formId.equals("forecastConfigurationForm")) {
                            // Ensure regular users always enter private forecasts
                            if (!userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR,
                                    VipsLogicRole.SUPERUSER)) {
                                formValidation.getFormField("isPrivate").setWebValue("true");
                            }
                            forecastConfiguration = forecastBean.storeForecastConfiguration(forecastConfiguration,
                                    formValidation.getFormFields(), modelFieldFormValidation.getFormFields());
                            // Store form config


                            // forecastBean.storeForecastModelConfigurations(forecastConfiguration,
                            // modelFieldFormValidation.getFormFields());
                            // TODO: Store model specific fields
                            // First: Delete all o


                            request.setAttribute("messageKey", request.getParameter("formConfigurationUpdated"));
                            response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://")
                                    .append(ServletUtil.getServerName(request))
                                    .append("/forecastConfiguration?action=viewForecastConfiguration&forecastConfigurationId=")
                                    .append(forecastConfiguration.getForecastConfigurationId()).append("&messageKey=")
                                    .append("forecastConfigurationUpdated").toString());
                        } else {
                            for (String optionVal : formValidation.getFormField("weatherStationPointOfInterestIds")
                                    .getWebValues()) {
                                Integer weatherStationPointOfInterestId = Integer.valueOf(optionVal);
                                forecastBean.storeNewMultipleForecastConfiguration(weatherStationPointOfInterestId,
                                        formValidation.getFormFields(), modelFieldFormValidation.getFormFields());
                            }
                            request.setAttribute("messageKey",
                                    request.getParameter("multipleForecastConfigurationsCreated"));
                            response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://")
                                    .append(ServletUtil.getServerName(request))
                                    .append("/forecastConfiguration?messageKey=")
                                    .append("multipleForecastConfigurationsCreated").toString());
                        }
                    } else {
                        // Return to form with error messages
                        request.setAttribute("formValidation", formValidation);
                        // We must get date formats!
                        Map<String, FormField> formFields =
                                FormValidator.getFormFields("forecastConfigurationForm", getServletContext());
                        // TODO: More intelligent selection of locations, weather stations and users
                        request.setAttribute("formId", "forecastConfigurationForm");
                        request.setAttribute("allCrops", em.createNamedQuery("Organism.findAllCrops").getResultList());
                        request.setAttribute("allPests", em.createNamedQuery("Organism.findAllPests").getResultList());
                        request.setAttribute("locationPointOfInterests",
                                em.createNamedQuery("PointOfInterest.findAll").getResultList());
                        request.setAttribute("weatherStationPointOfInterests",
                                em.createNamedQuery("PointOfInterestWeatherStation.findAll").getResultList());
                        request.setAttribute("vipsLogicUsers",
                                em.createNamedQuery("VipsLogicUser.findAll").getResultList());
                        request.setAttribute("dateStart_dateFormat", formFields.get("dateStart").getDateFormat());
                        request.setAttribute("dateEnd_dateFormat", formFields.get("dateEnd").getDateFormat());
                        request.getSession().setAttribute("availableTimeZones", SystemTime.getAvailableTimeZones());
                        request.getSession().setAttribute("defaultTimeZoneId",
                                user.getOrganizationId().getDefaultTimeZone());
                        // Hierarchy categories
                        request.setAttribute("hierarchyCategories",
                                organismBean.getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
                        request.setAttribute("modelInformations",
                                em.createNamedQuery("ModelInformation.findAll").getResultList());
                        request.setAttribute("currentLanguage",
                                SessionLocaleUtil.getCurrentLocale(request).getLanguage());
                        request.setAttribute("forecastConfiguration", forecastConfiguration);
                        request.getRequestDispatcher("/forecastConfigurationForm.ftl").forward(request, response);
                    }
                }
            } catch (NullPointerException | NumberFormatException | FormValidationException ex) {
                ex.printStackTrace();
                if (ex instanceof NumberFormatException) {
                    response.sendError(500,
                            "Invalid forecast configurationId " + request.getParameter("forecastConfigurationId"));
                } else {
                    response.sendError(500, ex.getMessage());
                }
            }
        }
        // Delete forecast configuration
        // Authorization: SUPERUSERS and ORGANIZATION ADMINS OR regular users owning the forecast config
        else if (action.equals("deleteForecastConfiguration")) {
            Long forecastConfigurationId = Long.valueOf(request.getParameter("forecastConfigurationId"));
            ForecastConfiguration forecastConfiguration = em.find(ForecastConfiguration.class, forecastConfigurationId);
            if (userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER)
                    || user.getUserId().equals(forecastConfiguration.getVipsLogicUserId().getUserId())) {
                // Only superusers can delete forecasts from other organizations
                if (!user.isSuperUser() && forecastConfiguration.getVipsLogicUserId() != null && !forecastConfiguration
                        .getVipsLogicUserId().getOrganizationId().equals(user.getOrganizationId())) {
                    response.sendError(403, "Access not authorized"); // HTTP Forbidden
                    return;
                }
                try {
                    forecastBean.deleteForecastConfiguration(forecastConfigurationId);
                    response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://")
                            .append(ServletUtil.getServerName(request)).append("/forecastConfiguration?")
                            .append("&messageKey=").append("forecastConfigurationDeleted").toString());
                } catch (NullPointerException | NumberFormatException ex) {
                    response.sendError(500,
                            "Invalid forecast configurationId " + request.getParameter("forecastConfigurationId"));
                }

            } else {
                response.sendError(403, "Access not authorized"); // HTTP Forbidden
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the
    // code.">
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
