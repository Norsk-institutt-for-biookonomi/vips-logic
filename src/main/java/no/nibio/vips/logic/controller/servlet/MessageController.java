/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the NIBIO Open Source License as published by 
 * NIBIO, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * NIBIO Open Source License for more details.
 * 
 * You should have received a copy of the NIBIO Open Source License
 * along with VIPSLogic.  If not, see <http://www.nibio.no/licenses/>.
 * 
 */

package no.nibio.vips.logic.controller.servlet;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.nibio.vips.i18n.LanguageUtil;
import no.nibio.vips.logic.entity.Message;
import no.nibio.vips.logic.entity.MessageTag;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.logic.util.SystemTime;
import no.nibio.vips.util.ExceptionUtil;
import no.nibio.vips.util.ServletUtil;
import no.nibio.web.forms.FormUtil;
import no.nibio.web.forms.FormValidation;
import no.nibio.web.forms.FormValidationException;
import no.nibio.web.forms.FormValidator;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 * @copyright 2014 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class MessageController extends HttpServlet {
   
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
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
        String action = request.getParameter("action");
        VipsLogicUser user = (VipsLogicUser) request.getSession().getAttribute("user");
        // Default: View message list
        // for everyone
        if(action == null)
        {
            // Default date filter is one year back and one year ahead (related to SystemTime.getSystemTime())
            Calendar cal = Calendar.getInstance();
            cal.setTime(SystemTime.getSystemTime());
            cal.add(Calendar.YEAR, -1);
            Date datePubStart = cal.getTime();
            cal.add(Calendar.YEAR, 2);
            Date datePubEnd = cal.getTime();
            // Default tag filter is no tag filter
            
            List<Message> messages;
            if(user.isSuperUser())
            {
                messages = SessionControllerGetter.getMessageBean().getFilteredMessages(null,null,datePubStart, datePubEnd);
            }
            else
            {
                messages = SessionControllerGetter.getMessageBean().getFilteredMessages(user.getOrganizationId().getOrganizationId(),null, datePubStart, datePubEnd);
            }
            request.setAttribute("messages", messages);
            request.setAttribute("selectedMessageTags", em.createNamedQuery("MessageTag.findAll", MessageTag.class).getResultList());
            request.setAttribute("notSelectedMessageTags", new ArrayList<MessageTag>());
            request.setAttribute("datePubStart", datePubStart);
            request.setAttribute("datePubEnd", datePubEnd);
            
            // If this is a redirect from a controller, with a message to be passed on
            request.setAttribute("messageKey", request.getParameter("messageKey"));
            request.getRequestDispatcher("/messageList.ftl").forward(request, response);
        }
        else if(action.equals("filter"))
        {
            // Handling filters
            // Message tags
            // If no tags are selected, ALL tags are selected
            List<Integer> selectedMessageTags = request.getParameterValues("filterMessageTags") != null ? 
                                        FormUtil.getIdsFromMultipleSelect(request.getParameterValues("filterMessageTags"))
                                        : SessionControllerGetter.getMessageBean().getAllMessageTagIds();
            Map<String, List<MessageTag>> messageTags = SessionControllerGetter.getMessageBean()
                    .getFilterMessageTags(selectedMessageTags);
            request.setAttribute("selectedMessageTags", messageTags.get("selected"));
            request.setAttribute("notSelectedMessageTags", messageTags.get("notSelected"));
            
            // Dates
            Date datePubStart, datePubEnd;
            try
            {
                datePubStart = FormUtil.getDate(request.getParameter("datePubStart"));
                datePubEnd = FormUtil.getDate(request.getParameter("datePubEnd"));
            }
            catch(ParseException ex)
            {
                Calendar cal = Calendar.getInstance();
                cal.setTime(SystemTime.getSystemTime());
                cal.add(Calendar.YEAR, -1);
                datePubStart = cal.getTime();
                cal.add(Calendar.YEAR, 2);
                datePubEnd = cal.getTime();
            }
            List<Message> messages;
            if(user.isSuperUser())
            {
                messages = SessionControllerGetter.getMessageBean().getFilteredMessages(null,selectedMessageTags, datePubStart, datePubEnd);
            }
            else
            {
                messages = SessionControllerGetter.getMessageBean().getFilteredMessages(user.getOrganizationId().getOrganizationId(),selectedMessageTags, datePubStart, datePubEnd);
            }
            request.setAttribute("datePubStart", datePubStart);
            request.setAttribute("datePubEnd", datePubEnd);
            request.setAttribute("messages", messages);
            
            // If this is a redirect from a controller, with a message to be passed on
            request.setAttribute("messageKey", request.getParameter("messageKey"));
            request.getRequestDispatcher("/messageList.ftl").forward(request, response);
        }
        else if(action.equals("editMessageForm"))
        {
            try
            {
                Integer messageId = Integer.valueOf(request.getParameter("messageId"));
                Message message = em.find(Message.class, messageId);
                request.setAttribute("message", message);
                request.setAttribute("messageLocale", 
                        request.getParameter("messageLocale") != null ? 
                                request.getParameter("messageLocale") 
                                : SessionLocaleUtil.getCurrentLocale(request).getLanguage()
                );
                request.setAttribute("availableLocales", LanguageUtil.getAvailableLocalesWithDistinctLanguage());
                List<MessageTag> tags = em.createNamedQuery("MessageTag.findAll").getResultList();
                tags.removeAll(message.getMessageTagSet());
                request.setAttribute("unusedTags", tags);
                request.setAttribute("messageKey", request.getParameter("messageKey"));
                request.setAttribute("organizations", SessionControllerGetter.getUserBean().getOrganizations());
                request.getRequestDispatcher("/messageForm.ftl").forward(request, response);
            }
            catch(NullPointerException | NumberFormatException ex)
            {
                response.sendError(500, ExceptionUtil.getStackTrace(ex));
            }
        }
        else if(action.equals("newMessageForm"))
        {
            try
            {
                Message message = new Message();
                request.setAttribute("message", message);
                request.setAttribute("messageLocale", 
                        request.getParameter("messageLocale") != null ? 
                                request.getParameter("messageLocale") 
                                : SessionLocaleUtil.getCurrentLocale(request).getLanguage()
                );
                request.setAttribute("availableLocales", LanguageUtil.getAvailableLocalesWithDistinctLanguage());
                request.setAttribute("unusedTags", em.createNamedQuery("MessageTag.findAll").getResultList());
                request.setAttribute("organizations", SessionControllerGetter.getUserBean().getOrganizations());
                request.getRequestDispatcher("/messageForm.ftl").forward(request, response);
            }
            catch(NullPointerException | NumberFormatException ex)
            {
                response.sendError(500, ExceptionUtil.getStackTrace(ex));
            }
        }
        else if(action.equals("messageFormSubmit"))
        {
            try
            {
                Map<String,String[]> parameterMap;
                List<FileItem> items = null;
                if(ServletFileUpload.isMultipartContent(request))
                {
                    // Create a factory for disk-based file items
                    DiskFileItemFactory factory = new DiskFileItemFactory();

                    // Configure a repository (to ensure a secure temp location is used)
                    ServletContext servletContext = this.getServletConfig().getServletContext();
                    File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
                    factory.setRepository(repository);

                    // Create a new file upload handler
                    ServletFileUpload upload = new ServletFileUpload(factory);

                    // Parse the request
                    items = upload.parseRequest(request);
                    parameterMap = FormUtil.getParameterMap(items,"UTF-8");
                }
                else
                {
                    parameterMap = request.getParameterMap();
                }
                
                // Validate form fields (except uploaded file)
                FormValidation formValidation = FormValidator.validateForm("messageForm", parameterMap,SessionLocaleUtil.getI18nBundle(request),getServletContext());
                
                if(formValidation.isValid())
                {
                    // Store the message
                    Message message = SessionControllerGetter.getMessageBean().storeMessage(formValidation.getFormFields(), user);
                    // Delete the current illustration
                    String deleteIllustration = formValidation.getFormField("deleteIllustration").getWebValue();
                    if(deleteIllustration != null && deleteIllustration.equals("true"))
                    {
                        message = SessionControllerGetter.getMessageBean().deleteMessageIllustration(message);
                    }
                    // Store the new illustration (replaces former illustration if not already deleted)
                    if(items != null)
                    {
                        for(FileItem item:items)
                        {
                            if(!item.isFormField() && item.getSize() > 0)
                            {
                                message = SessionControllerGetter.getMessageBean().storeMessageIllustration(message, item);
                            }
                        }
                    }   
                    // If an illustration is attached to the message, update the caption
                    SessionControllerGetter.getMessageBean().updateMessageIllustrationCaption(message, 
                                formValidation.getFormField("messageIllustrationCaptionLocale").getWebValue(),
                                formValidation.getFormField("locale").getWebValue()
                                );
                    
                    
                    // Redirect to form
                    response.sendRedirect(new StringBuilder("http://")
                            .append(ServletUtil.getServerName(request))
                            .append("/message?action=editMessageForm&messageId=").append(message.getMessageId())
                            .append("&messageLocale=").append(formValidation.getFormField("locale").getWebValue())
                            .append("&messageKey=").append("messageUpdated").toString()
                            
                    );
                    
                }
                else
                {
                    // Redirect to form with error messages
                    request.setAttribute("formValidation", formValidation);
                    Integer messageId = formValidation.getFormField("messageId").getValueAsInteger();
                    // Get message, attach it
                    Message message;
                    if(messageId > 0)
                    {
                        message = em.find(Message.class, messageId);
                    }
                    else
                    {
                        message = new Message();
                    }
                    request.setAttribute("message", message);
                    request.setAttribute("messageLocale", 
                            request.getParameter("messageLocale") != null ? 
                                    request.getParameter("messageLocale") 
                                    : SessionLocaleUtil.getCurrentLocale(request).getLanguage()
                    );
                    request.setAttribute("availableLocales", LanguageUtil.getAvailableLocalesWithDistinctLanguage());
                    request.getRequestDispatcher("/messageForm.ftl").forward(request, response);
                }
            }
            catch(NullPointerException | NumberFormatException | FormValidationException | FileUploadException  ex)
            {
                ex.printStackTrace();
                response.sendError(500, ExceptionUtil.getStackTrace(ex));
            }
            catch(Exception ex)
            {
                ex.printStackTrace();
                response.sendError(500, ExceptionUtil.getStackTrace(ex));
            }
        }
        else if(action.equals("deleteMessage"))
        {
            try
            {
                Integer messageId = Integer.valueOf(request.getParameter("messageId"));
                SessionControllerGetter.getMessageBean().deleteMessage(messageId);
                // Redirect to list
                response.sendRedirect(new StringBuilder("http://")
                        .append(ServletUtil.getServerName(request))
                        .append("/message")
                        .append("?messageKey=").append("messageDeleted").toString());
            }
            catch(NullPointerException | NumberFormatException ex)
            {
                
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
