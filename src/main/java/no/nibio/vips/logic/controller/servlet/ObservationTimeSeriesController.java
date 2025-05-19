package no.nibio.vips.logic.controller.servlet;

import jakarta.ejb.EJB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import no.nibio.vips.logic.controller.session.*;
import no.nibio.vips.logic.entity.*;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.messaging.MessagingBean;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.util.ExceptionUtil;
import no.nibio.vips.util.ServletUtil;
import no.nibio.web.forms.*;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class ObservationTimeSeriesController extends HttpServlet {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ObservationTimeSeriesController.class);

    private static final int START_YEAR = 2016;
    public static final String LOCATION_VISIBILITY_FORM_VALUE_PRIVATE = "private";
    public static final String LOCATION_VISIBILITY_FORM_VALUE_PUBLIC = "public";
    public static final String LOCATION_VISIBILITY_FORM_VALUE_MASK_PREFIX = "mask_";

    @PersistenceContext(unitName = "VIPSLogic-PU")
    EntityManager em;

    @EJB
    UserBean userBean;
    @EJB
    ObservationTimeSeriesBean observationTimeSeriesBean;
    @EJB
    ObservationBean observationBean;
    @EJB
    MessagingBean messagingBean;
    @EJB
    OrganismBean organismBean;
    @EJB
    PointOfInterestBean pointOfInterestBean;

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        LOGGER.info("Process request in ObservationTimeSeriesController");
        response.setContentType("text/html;charset=UTF-8");
        String action = request.getParameter("action");
        VipsLogicUser user = (VipsLogicUser) request.getSession().getAttribute("user");
        request.setAttribute("messageKey", request.getParameter("messageKey"));
        List<ObservationTimeSeries> observationTimeSeriesList = observationTimeSeriesBean.getObservationTimeSeriesListForUser(user);

        if (request.getServletPath().endsWith("/observationTimeSeries")) {
            LOGGER.info("Action is {}", action);
            if (action == null) {
                request.setAttribute("observationTimeSeriesList", observationTimeSeriesList);
                LOGGER.info("Display list of {} observationTimeSeries", observationTimeSeriesList.size());

                Map<String, Long> observationCounts = new HashMap<>();
                for (ObservationTimeSeries observationTimeSeries : observationTimeSeriesList) {
                    Long count = em.createNamedQuery("Observation.findCountByObservationTimeSeries", Long.class)
                        .setParameter("observationTimeSeries", observationTimeSeries).getSingleResult();
                    observationCounts.put(String.valueOf(observationTimeSeries.getObservationTimeSeriesId()), count);
                }
                request.setAttribute("userIsObservationAuthority",
                    userBean.authorizeUser(user, VipsLogicRole.OBSERVATION_AUTHORITY,
                        VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER));
                request.setAttribute("observationCounts", observationCounts);
                request.getRequestDispatcher("/observationTimeSeriesList.ftl").forward(request, response);
                return;
            }
            boolean authorized =
                userBean.authorizeUser(user, VipsLogicRole.OBSERVER, VipsLogicRole.ORGANIZATION_ADMINISTRATOR,
                    VipsLogicRole.SUPERUSER);
            if (!authorized) {
                LOGGER.info("User {} not authorized", user.getUserId());
                response.sendError(403, "Access not authorized");
            }
            if (action.equals("editObservationTimeSeriesForm")) {
                Integer observationTimeSeriesId = Integer.valueOf(request.getParameter("observationTimeSeriesId"));
                LOGGER.info("Display form for editing observationTimeSeries {}", observationTimeSeriesId);
                ObservationTimeSeries observationTimeSeries =
                    observationTimeSeriesBean.getObservationTimeSeries(observationTimeSeriesId);
                Long observationCount = em.createNamedQuery("Observation.findCountByObservationTimeSeries", Long.class)
                    .setParameter("observationTimeSeries", observationTimeSeries).getSingleResult();

                request.setAttribute("observationTimeSeries", observationTimeSeries);
                request.setAttribute("isEditable", observationCount == 0);
                request.setAttribute("userIsObservationAuthority",
                    userBean.authorizeUser(user, VipsLogicRole.OBSERVATION_AUTHORITY,
                        VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER));
                buildFormRequest(request, user, observationTimeSeries);
                request.setAttribute("observations",
                    observationBean.getObservationsForTimeSeries(observationTimeSeries));
                request.getRequestDispatcher("/observationTimeSeriesForm.ftl").forward(request, response);
            } else if (action.equals("newObservationTimeSeriesForm")) {
                LOGGER.info("Create new observationTimeSeries");
                ObservationTimeSeries observationTimeSeries = new ObservationTimeSeries();
                request.setAttribute("observationTimeSeries", observationTimeSeries);
                buildFormRequest(request, user, observationTimeSeries);
                request.setAttribute("isEditable", true);
                request.getRequestDispatcher("/observationTimeSeriesForm.ftl").forward(request, response);
            } else if (action.equals("observationTimeSeriesFormSubmit")) {
                LOGGER.info("Submit changes for observationTimeSeries");
                Map<String, String[]> parameterMap;
                List<FileItem> items;
                if (JakartaServletFileUpload.isMultipartContent(request)) {
                    DiskFileItemFactory dfif = DiskFileItemFactory.builder().get();
                    JakartaServletFileUpload upload = new JakartaServletFileUpload(dfif);
                    items = upload.parseRequest(request);
                    parameterMap = FormUtil.getParameterMap(items, "UTF-8");
                } else {
                    parameterMap = request.getParameterMap();
                }
                ObservationTimeSeries observationTimeSeries = new ObservationTimeSeries();
                try {
                    FormValidation formValidation = FormValidator.validateForm(
                        "observationTimeSeriesForm",
                        parameterMap,
                        SessionLocaleUtil.getI18nBundle(request),
                        getServletContext()
                    );
                    if (formValidation.isValid()) {
                        observationTimeSeries.setUserId(user.getUserId()); // Should only be set for new time series
                        Long observationCount = 0L;

                        Integer observationTimeSeriesId =
                            formValidation.getFormField("observationTimeSeriesId").getValueAsInteger();
                        if (observationTimeSeriesId > 0) {
                            // Fetch existing time series if id > 0
                            observationTimeSeries = em.find(ObservationTimeSeries.class, observationTimeSeriesId);
                            observationCount = em.createNamedQuery("Observation.findCountByObservationTimeSeries", Long.class)
                                .setParameter("observationTimeSeries", observationTimeSeries).getSingleResult();
                        }
                        observationTimeSeries.setLastModifiedBy(user.getUserId());
                        observationTimeSeries.setLastModified(new Date());

                        // Only observation time series without observations can set the crop, pest and location
                        if (observationCount == 0) {
                            observationTimeSeries.setOrganism(em.find(Organism.class, formValidation.getFormField("organismId").getValueAsInteger()));
                            observationTimeSeries.setCropOrganism(em.find(Organism.class, formValidation.getFormField("cropOrganismId").getValueAsInteger()));
                            observationTimeSeries.setYear(formValidation.getFormField("year").getValueAsInteger());
                            setObservationLocationVisibility(observationTimeSeries, formValidation.getFormField("locationVisibility").getWebValue());
                            observationTimeSeries.setLocationPointOfInterestId(
                                formValidation.getFormField("locationPointOfInterestId").getValueAsInteger());
                        }
                        observationTimeSeries.setName(formValidation.getFormField("name").getWebValue());
                        observationTimeSeries.setDescription(formValidation.getFormField("description").getWebValue());
                        observationTimeSeries = observationTimeSeriesBean.storeObservationTimeSeries(observationTimeSeries);
                        LOGGER.info("Redirect to edit form with observationTimeSeriesId={}", observationTimeSeries.getObservationTimeSeriesId());
                        response.sendRedirect(Globals.PROTOCOL + "://" +
                            ServletUtil.getServerName(request) +
                            "/observationTimeSeries?action=editObservationTimeSeriesForm&observationTimeSeriesId=" +
                            observationTimeSeries.getObservationTimeSeriesId() +
                            "&messageKey=observationTimeSeriesStored"
                        );
                    } else {
                        // Redirect to form with error messages
                        buildFormRequest(request, user, observationTimeSeries);
                        request.setAttribute("formValidation", formValidation);
                        request.getRequestDispatcher("/observationTimeSeriesForm.ftl").forward(request, response);
                    }
                } catch (FormValidationException e) {
                    LOGGER.error("Exception at form validation", e);
                    response.sendError(500, ExceptionUtil.getStackTrace(e));
                }
            } else if (action.equals("deleteObservationTimeSeries")) {
                try {
                    Integer observationTimeSeriesId = Integer.valueOf(request.getParameter("observationTimeSeriesId"));
                    LOGGER.info("Delete observationTimeSeries {}", observationTimeSeriesId);
                    observationTimeSeriesBean.deleteObservationTimeSeries(observationTimeSeriesId);

                    // Redirect to list
                    response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://")
                        .append(ServletUtil.getServerName(request))
                        .append("/observationTimeSeries")
                        .append("?messageKey=").append("observationTimeSeriesDeleted").toString()
                    );
                } catch (NullPointerException | NumberFormatException ex) {
                    response.sendError(500, ExceptionUtil.getStackTrace(ex));
                }

            }
        }
    }

    private void buildFormRequest(HttpServletRequest request, VipsLogicUser user, ObservationTimeSeries observationTimeSeries) {
        request.setAttribute("observationTimeSeries", observationTimeSeries);
        request.setAttribute("years", generateYearList());
        request.setAttribute("mapLayers", userBean.getMapLayerJSONForUser(user));
        request.setAttribute("defaultMapCenter", user.getOrganizationId().getDefaultMapCenter());
        request.setAttribute("defaultMapZoom", user.getOrganizationId().getDefaultMapZoom());
        request.setAttribute("locationPointOfInterests",
            pointOfInterestBean.getRelevantPointOfInterestsForUser(user));
        request.setAttribute("locationVisibilityFormValue",
            getLocationVisibilityFormValue(observationTimeSeries));
        List<PolygonService> polygonServices =
            observationBean.getPolygonServicesForOrganization(user.getOrganization_id());
        request.setAttribute("polygonServices", polygonServices);
        List<Organism> allPests = em.createNamedQuery("Organism.findAllPests").getResultList();
        request.setAttribute("allPests", organismBean.sortOrganismsByLocalName(allPests,
            SessionLocaleUtil.getCurrentLocale(request).getLanguage()));
        List<Organism> allCrops = em.createNamedQuery("Organism.findAllCrops").getResultList();
        request.setAttribute("allCrops", organismBean.sortOrganismsByLocalName(allCrops,
            SessionLocaleUtil.getCurrentLocale(request).getLanguage()));
        request.setAttribute("hierarchyCategories",
            organismBean.getHierarchyCategoryNames(SessionLocaleUtil.getCurrentLocale(request)));
    }

    private List<Integer> generateYearList() {
        List<Integer> years = new ArrayList<>();
        for (int year = START_YEAR; year <= LocalDate.now().getYear(); year++) {
            years.add(year);
        }
        return years;
    }

    private String getLocationVisibilityFormValue(ObservationTimeSeries observationTimeSeries) {
        // Private is private, no matter what
        if (observationTimeSeries.getLocationIsPrivate()) {
            return LOCATION_VISIBILITY_FORM_VALUE_PRIVATE;
        }
        // Public can either be completely public or masked by a polygon layer. e.g. county borders
        if (observationTimeSeries.getPolygonService() == null) {
            return LOCATION_VISIBILITY_FORM_VALUE_PUBLIC;
        }
        return LOCATION_VISIBILITY_FORM_VALUE_MASK_PREFIX + observationTimeSeries.getPolygonService()
            .getPolygonServiceId();
    }

    private void setObservationLocationVisibility(ObservationTimeSeries observationTimeSeries, String formValue) {
        // Private is private, no matter what
        observationTimeSeries.setLocationIsPrivate(
            formValue.equals(ObservationController.LOCATION_VISIBILITY_FORM_VALUE_PRIVATE));
        observationTimeSeries.setPolygonService(
            // If private or public, set no polygon service
            formValue.equals(ObservationController.LOCATION_VISIBILITY_FORM_VALUE_PRIVATE) || formValue.equals(
                ObservationController.LOCATION_VISIBILITY_FORM_VALUE_PUBLIC)
                ? null
                // Otherwise, set selected polygon service
                : em.find(PolygonService.class, Integer.valueOf(formValue.split("_")[1]))
        );
    }
}
