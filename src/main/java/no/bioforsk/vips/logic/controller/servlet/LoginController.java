/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the Bioforsk Open Source License as published by 
 * Bioforsk, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Bioforsk Open Source License for more details.
 * 
 * You should have received a copy of the Bioforsk Open Source License
 * along with VIPSLogic.  If not, see <http://www.bioforsk.no/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.controller.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.bioforsk.vips.logic.authenticate.OpenIdAuthAttributes;
import no.bioforsk.vips.logic.authenticate.OpenIdRelyingParty;
import no.bioforsk.vips.logic.entity.UserAuthenticationType;
import no.bioforsk.vips.logic.entity.VipsLogicUser;
import no.bioforsk.vips.logic.util.SessionControllerGetter;
import no.bioforsk.vips.util.ServletUtil;
import org.openid4java.consumer.ConsumerException;

/**
 * Logs a user in or out
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class LoginController extends HttpServlet {
    
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
        
        // Assigning nextpage to request, so that login.ftl gets access it
        String nextPage = request.getParameter("nextPage");
        if(nextPage == null)
        {
            nextPage="/";
        }
        request.setAttribute("nextPage", nextPage);
        
        // This means that an OpenId authentication has returned the user to this URL
        // See code below
        if(request.getServletPath().indexOf("remoteloginsuccess") >= 0)
        {
            try 
            {
                Integer userAuthenticationTypeId = Integer.valueOf(request.getParameter("userAuthenticationTypeId"));
                OpenIdRelyingParty relP = new OpenIdRelyingParty();
                OpenIdAuthAttributes attributes = relP.verifyResponse(request);
                if(attributes != null)
                {
                    VipsLogicUser user = SessionControllerGetter.getUserBean().getUser(attributes.getOpenId(), userAuthenticationTypeId);
                    if(user != null)
                    {
                        request.getSession().setAttribute("user", user);
                        response.sendRedirect(new StringBuilder("http://").append(ServletUtil.getServerName(request)).append(nextPage).toString());
                    }
                    else
                    {
                        // This might be 
                        // * a new user
                        // * an existing user logging in with OpenId/Google for the first time.
                        // Sending user to form asking this question
                        request.setAttribute("userAuthenticationTypeId", userAuthenticationTypeId);
                        request.getSession().setAttribute("openId", attributes.getOpenId());
                        request.getRequestDispatcher("/registerOpenIdForm.ftl").forward(request, response);
                    }
                }
                else
                {
                    request.setAttribute("errorMessageKey", "remoteloginfailed");
                    request.getRequestDispatcher("/login.ftl").forward(request, response);
                }
            } 
            catch (ConsumerException | NullPointerException | NumberFormatException ex) {
                Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        //String logout = request.getParameter("logout");
        // A log out request
        else if(request.getServletPath().indexOf("logout") >= 0)
        {
            request.getSession().removeAttribute("user");
            request.setAttribute("messageKey","logoutsuccess");
            request.getRequestDispatcher("/login.ftl").forward(request, response);
        }
        // A login attempt
        else if(request.getServletPath().indexOf("loginsubmit") >= 0)
        {
            // Which login method?
            Integer userAuthenticationTypeId = -1;
            try
            {
                userAuthenticationTypeId = Integer.valueOf(request.getParameter("userAuthenticationTypeId"));
            }
            // No method found, redirect to form again
            catch(NumberFormatException | NullPointerException ex)
            {
                request.getRequestDispatcher("/login.ftl").forward(request, response);
                return;
            }
            
            // Standard username/password login
            if(userAuthenticationTypeId.equals(UserAuthenticationType.TYPE_PASSWORD))
            {
                String username = request.getParameter("username");
                String password = request.getParameter("password");
                try (PrintWriter out = response.getWriter()) {
                    
                    Map<String,String> creds = new HashMap();
                    creds.put("username", username);
                    creds.put("password", password);

                    VipsLogicUser user = SessionControllerGetter.getUserBean().authenticateUser(creds);

                    if(user != null)
                    {
                        request.getSession().setAttribute("user", user);
                        response.sendRedirect(new StringBuilder("http://").append(ServletUtil.getServerName(request)).append(nextPage).toString());
                    }
                    else
                    {
                        request.setAttribute("errorMessageKey", "invalidcredentials");
                        request.getRequestDispatcher("/login.ftl").forward(request, response);
                    }


                }
            }
            // Login with OpenId (Including Google's version)
            else if(userAuthenticationTypeId.equals(UserAuthenticationType.TYPE_OPENID) || userAuthenticationTypeId.equals(UserAuthenticationType.TYPE_OPENID_GOOGLE))
            {
                try
                {
                    // configure the return_to URL where your application will receive
                    // the authentication responses from the OpenID provider
                    String serverName = ServletUtil.getServerName(request);
                    String returnToUrl = "http://" + serverName + "/remoteloginsuccess?userAuthenticationTypeId=" + userAuthenticationTypeId + "&nextPage=" + URLEncoder.encode(nextPage, "UTF-8");
                    
                    String openIdStr;
                    // Google is of course special....
                    if(userAuthenticationTypeId.equals(UserAuthenticationType.TYPE_OPENID_GOOGLE))
                    {
                        openIdStr = "https://www.google.com/accounts/o8/id";
                    }
                    else
                    {
                        openIdStr= request.getParameter("openIdStr");
                    }
                    // Send the authentication request to third party
                    OpenIdRelyingParty openId = new OpenIdRelyingParty();
                    openId.authRequest(openIdStr, returnToUrl, request, response);
                }
                catch(ConsumerException ex)
                {
                    request.setAttribute("errorMessageKey", ex.getMessage());
                    request.getRequestDispatcher("/login.ftl").forward(request, response);
                }
            }
            // Authentication method not recognized, redirect to standard form
            else
            {
                request.getRequestDispatcher("/login.ftl").forward(request, response);
            }
        }
        // No login attempt. Show form
        else
        {
            request.setAttribute("messageKey", request.getParameter("messageKey"));
            request.setAttribute("errorMessageKey", request.getParameter("errorMessageKey"));
            request.getRequestDispatcher("/login.ftl").forward(request, response);
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
