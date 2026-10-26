/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import no.nibio.vips.logic.controller.session.SchedulingBean;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.TaskHistory;
import no.nibio.vips.logic.entity.VipsLogicRole;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.scheduling.VipsLogicTask;
import no.nibio.vips.logic.scheduling.VipsLogicTask.TaskExecutor;
import no.nibio.vips.logic.scheduling.VipsLogicTaskFactory;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.util.ServletUtil;
import no.nibio.web.forms.FormValidation;
import no.nibio.web.forms.FormValidationException;
import no.nibio.web.forms.FormValidator;
import no.nibio.web.forms.HTMLFormGenerator;

/**
 * Handles scheduling of jobs
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class SchedulingController extends HttpServlet {

    @Inject
    EntityManager em;
    
    @Inject
    UserBean userBean;
    @Inject
    SchedulingBean schedulingBean;
    
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
        
        // Default: Overview of running tasks
        // for SUPERUSERS and ORGANIZATION ADMINS
        if(action == null)
        {
            if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
            {
                try
                {
                    FormValidation formValidation = FormValidator.validateForm("taskHistoryDateForm", request, getServletContext());
                    Date taskHistoryDate = new Date();
                    if(formValidation.isValid())
                    {
                        if(! formValidation.getFormField("taskHistoryDate").isEmpty())
                        {
                            taskHistoryDate = formValidation.getFormField("taskHistoryDate").getValueAsDate();
                        }
                    }
                    else
                    {
                        request.setAttribute("formValidation", formValidation);
                    }


                    TaskExecutor[] taskExecutors = schedulingBean.getRunningTasks();
                    request.setAttribute("taskExecutors", taskExecutors);
                    request.setAttribute("schedulingStarted", schedulingBean.isSystemSchedulerStarted());
                    request.setAttribute("orderedScheduledTasks", schedulingBean.getOrderedScheduledTasks(user));
                    request.setAttribute("taskHistoryDate",taskHistoryDate);
                    request.setAttribute("taskHistory",schedulingBean.getTaskHistory(taskHistoryDate, user));
                    request.setAttribute("allTasksMap", VipsLogicTaskFactory.getAllVipsLogicTasksMap());
                    // If this is a redirect from a controller, with a message to be passed on
                    request.setAttribute("messageKey", request.getParameter("messageKey"));
                    request.setAttribute("message", request.getParameter("message"));
                    request.getRequestDispatcher("/schedulingOverview.ftl").forward(request, response);
                }
                catch(FormValidationException ex)
                {
                    response.sendError(500, ex.getMessage());
                }
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
            if(userBean.authorizeUser(user, VipsLogicRole.SUPERUSER, VipsLogicRole.ORGANIZATION_ADMINISTRATOR))
            {
                List<VipsLogicTask> allTasks = schedulingBean.getVipsLogicTasks(user);
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
            if(userBean.authorizeUser(user, VipsLogicRole.SUPERUSER, VipsLogicRole.ORGANIZATION_ADMINISTRATOR))
            {
                try
                {
                    Integer factoryId = Integer.valueOf(request.getParameter("factoryId"));
                    VipsLogicTask task = VipsLogicTaskFactory.createVipsLogicTask(factoryId);
                    if(!user.isSuperUser()) // Some tasks need organization id set to create a sensible form for an orgAdmin
                    {
                        task.setOrganization(user.getOrganizationId());
                    }
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
                	ex.printStackTrace();
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
            if(userBean.authorizeUser(user, VipsLogicRole.SUPERUSER, VipsLogicRole.ORGANIZATION_ADMINISTRATOR))
            {
                try
                {
                    Integer factoryId = Integer.valueOf(request.getParameter("factoryId"));
                    VipsLogicTask task = VipsLogicTaskFactory.createVipsLogicTask(factoryId);
                    FormValidation formValidation = FormValidator.validateForm(task.getConfigFormDefinition(SessionLocaleUtil.getCurrentLocale(request).getLanguage()), request.getParameterMap(), SessionLocaleUtil.getI18nBundle(request));
                    if(formValidation.isValid())
                    {
                        // RUN THE JOB!!!
                        // Need to copy the ParameterMap (not just pass it along), as the values get lost 
                        // after the servlet disconnects itself from the task
                        task.setConfiguration(new HashMap<>(request.getParameterMap()));
                        if(user.isOrganizationAdmin())
                        {
                            task.setOrganization(user.getOrganizationId());
                        }
                        schedulingBean.launchSystemSchedulerTask(task);
                        String message = MessageFormat.format(SessionLocaleUtil.getI18nText(request, "taskXWasLaunched"), task.getName(SessionLocaleUtil.getCurrentLocale(request).getLanguage()));
                        response.sendRedirect(Globals.PROTOCOL + "://" + ServletUtil.getServerName(request) + "/scheduling?message=" + URLEncoder.encode(message, StandardCharsets.UTF_8.toString()));
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
                catch(NullPointerException | NumberFormatException | FormValidationException ex)
                {
                    ex.printStackTrace();
                    response.sendError(500, "ERROR: " + ex.getMessage());
                }
            }
            else
            {
                response.sendError(403,"Access not authorized"); // HTTP Forbidden
            }
        }
        // Stopping system scheduling
        // Authorization: SUPERUSER ONLY
        else if(action.equals("stopScheduling"))
        {
            if(userBean.authorizeUser(user, VipsLogicRole.SUPERUSER))
            {
                schedulingBean.stopSystemScheduler();
                String message = SessionLocaleUtil.getI18nText(request, "schedulingStopped");
                response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://").append(ServletUtil.getServerName(request)).append("/scheduling?message=").append(message).toString());
            }
            else
            {
                response.sendError(403,"Access not authorized"); // HTTP Forbidden
            }
        }
        // Starting system scheduling
        // Authorization: SUPERUSER ONLY
        else if(action.equals("startScheduling"))
        {
            if(userBean.authorizeUser(user, VipsLogicRole.SUPERUSER))
            {
                schedulingBean.startSystemScheduler();
                String message = SessionLocaleUtil.getI18nText(request, "schedulingStarted");
                response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://").append(ServletUtil.getServerName(request)).append("/scheduling?message=").append(message).toString());
            }
            else
            {
                response.sendError(403,"Access not authorized"); // HTTP Forbidden
            }
        }
        // View details about a particular execution of a task
        // for SUPERUSERS and ORGANIZATION ADMINS
        else if(action.equals("viewTaskHistoryDetails"))
        {
            if(userBean.authorizeUser(user, VipsLogicRole.ORGANIZATION_ADMINISTRATOR, VipsLogicRole.SUPERUSER))
            {
                try
                {
                    Long taskHistoryId = Long.valueOf(request.getParameter("taskHistoryId"));
                    TaskHistory taskHistory = em.find(TaskHistory.class, taskHistoryId);
                    request.setAttribute("taskHistory",taskHistory);
                    request.setAttribute("allTasksMap", VipsLogicTaskFactory.getAllVipsLogicTasksMap());
                    // If this is a redirect from a controller, with a message to be passed on
                    request.setAttribute("messageKey", request.getParameter("messageKey"));
                    request.setAttribute("message", request.getParameter("message"));
                    request.getRequestDispatcher("/taskHistoryDetails.ftl").forward(request, response);
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
