package no.bioforsk.vips.logic.controller.servlet;

import it.sauronsoftware.cron4j.Predictor;
import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.SchedulingPattern;
import it.sauronsoftware.cron4j.TaskCollector;
import it.sauronsoftware.cron4j.TaskExecutor;
import it.sauronsoftware.cron4j.TaskTable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.security.Timestamp;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.bioforsk.vips.logic.controller.session.SchedulingBean;
import no.bioforsk.vips.logic.controller.session.UserBean;
import no.bioforsk.vips.logic.entity.VipsLogicRole;
import no.bioforsk.vips.logic.entity.VipsLogicUser;
import no.bioforsk.vips.logic.i18n.SessionLocaleUtil;
import no.bioforsk.vips.logic.scheduling.VipsLogicTask;
import no.bioforsk.vips.logic.scheduling.VipsLogicTaskFactory;
import no.bioforsk.vips.logic.util.SessionControllerGetter;
import no.bioforsk.vips.util.ServletUtil;
import no.bioforsk.web.forms.FormValidation;
import no.bioforsk.web.forms.FormValidator;
import no.bioforsk.web.forms.HTMLFormGenerator;

/**
 * Handles scheduling of jobs
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class SchedulingController extends HttpServlet {

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
        
        String action = request.getParameter("action");
        VipsLogicUser user = (VipsLogicUser) request.getSession().getAttribute("user");
        UserBean userBean = SessionControllerGetter.getUserBean();
        SchedulingBean schedulingBean = SessionControllerGetter.getSchedulingBean();
        
        // Default: Overview of running tasks
        // for SUPERUSERS and ORGANIZATION ADMINS
        if(action == null)
        {
            if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
            {
                TaskExecutor[] taskExecutors = schedulingBean.getRunningTasks();
                request.setAttribute("taskExecutors", taskExecutors);
                request.setAttribute("orderedScheduledTasks", schedulingBean.getOrderedScheduledTasks());
                // If this is a redirect from a controller, with a message to be passed on
                request.setAttribute("messageKey", request.getParameter("messageKey"));
                request.setAttribute("message", request.getParameter("message"));
                request.getRequestDispatcher("/schedulingOverview.ftl").forward(request, response);
            }
            else
            {
                response.sendError(403,"Access not authorized"); // HTTP Forbidden
            }
      
        }
        
        // List of all tasks
        // Authorization: SUPERUSER ONLY
        else if(action.equals("viewAllTasks"))
        {
            if(userBean.authorizeUser(user, VipsLogicRole.SUPERUSER))
            {
                List<VipsLogicTask> allTasks = schedulingBean.getAllVipsLogicTasks();
                request.setAttribute("tasks", allTasks);
                // If this is a redirect from a controller, with a message to be passed on
                request.setAttribute("messageKey", request.getParameter("messageKey"));
                request.getRequestDispatcher("/taskList.ftl").forward(request, response);
            }
            else
            {
                response.sendError(403,"Access not authorized"); // HTTP Forbidden
            }
        }
        
        // Form for running a task manually
        // Authorization: SUPERUSER ONLY
        else if(action.equals("runTaskManuallyForm"))
        {
            if(userBean.authorizeUser(user, VipsLogicRole.SUPERUSER))
            {
                try
                {
                    Integer factoryId = Integer.valueOf(request.getParameter("factoryId"));
                    VipsLogicTask task = VipsLogicTaskFactory.createVipsLogicTask(factoryId);
                    request.setAttribute("vipsLogicTask", task);
                    //request.setAttribute("testJSON", task.getConfigFormDefinition());
                    String language = SessionLocaleUtil.getCurrentLocale(request).getLanguage();
                    String formHTML = new HTMLFormGenerator().getHTMLFormFields(task.getConfigFormDefinition(language), "runTaskManuallyForm", language);
                    request.setAttribute("vipsLogicTaskFormFields", formHTML);
                    // If this is a redirect from a controller, with a message to be passed on
                    request.setAttribute("messageKey", request.getParameter("messageKey"));
                    request.getRequestDispatcher("/runTaskManuallyForm.ftl").forward(request, response);
                }
                catch(NullPointerException | NumberFormatException ex)
                {
                    response.sendError(500, "ERROR: " + ex.getMessage());
                }
            }
            else
            {
                response.sendError(403,"Access not authorized"); // HTTP Forbidden
            }
        }
        // Form for running a task manually
        // Authorization: SUPERUSER ONLY
        else if(action.equals("runTaskManuallyFormSubmit"))
        {
            if(userBean.authorizeUser(user, VipsLogicRole.SUPERUSER))
            {
                try
                {
                    Integer factoryId = Integer.valueOf(request.getParameter("factoryId"));
                    VipsLogicTask task = VipsLogicTaskFactory.createVipsLogicTask(factoryId);
                    FormValidation formValidation = FormValidator.validateForm(task.getConfigFormDefinition(SessionLocaleUtil.getCurrentLocale(request).getLanguage()), request);
                    if(formValidation.isValid())
                    {
                        // RUN THE JOB!!!
                        // How to pass on the form fields to the task??
                        task.setConfiguration(request.getParameterMap());
                        schedulingBean.getSystemScheduler().launch(task);
                        String message = MessageFormat.format(SessionLocaleUtil.getI18nText(request, "taskXWasLaunched"), task.getName(SessionLocaleUtil.getCurrentLocale(request).getLanguage()));
                        response.sendRedirect(new StringBuilder("http://").append(ServletUtil.getServerName(request)).append("/scheduling?message=").append(message).toString());
                    }
                    else
                    {
                        request.setAttribute("formValidation", formValidation);
                        request.setAttribute("vipsLogicTask", task);
                        String language = SessionLocaleUtil.getCurrentLocale(request).getLanguage();
                        String formHTML = new HTMLFormGenerator().getHTMLFormFields(task.getConfigFormDefinition(language), "runTaskManuallyForm", language);
                        request.setAttribute("vipsLogicTaskFormFields", formHTML);
                        // If this is a redirect from a controller, with a message to be passed on
                        request.setAttribute("messageKey", request.getParameter("messageKey"));
                        request.getRequestDispatcher("/runTaskManuallyForm.ftl").forward(request, response);
                    }
                }
                catch(NullPointerException | NumberFormatException ex)
                {
                    response.sendError(500, "ERROR: " + ex.getMessage());
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
