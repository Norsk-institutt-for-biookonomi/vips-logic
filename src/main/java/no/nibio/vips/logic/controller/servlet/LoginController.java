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

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.nibio.vips.logic.entity.UserAuthenticationType;
import no.nibio.vips.logic.entity.UserUuid;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.util.ServletUtil;

/**
 * Logs a user in or out
 * @copyright 2013-2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class LoginController extends HttpServlet {
    //private static final String CLOSE_AND_RELOAD_PARENT = "close_and_reload_parent";
    
    private static final String RETURN_UUID_PARAMETER_NAME = "returnUUID";
    
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
        String nextPage = getNextPage(request);
        request.setAttribute("nextPage", nextPage);
        // We remove the session attribute, so it doesn't stick
        request.getSession().removeAttribute("nextPage");
        
        Boolean returnUUID = getReturnUUID(request);
        request.setAttribute(LoginController.RETURN_UUID_PARAMETER_NAME, returnUUID);
        // We remove the session attribute, so it doesn't stick
        request.getSession().removeAttribute(LoginController.RETURN_UUID_PARAMETER_NAME);
        
        // This means that an OpenId authentication has returned the user to this URL
        // See code below
        if(request.getServletPath().contains("oauth2callback"))
        {
            // Is it an authorization response?
            if(request.getParameter("code") != null)
            {
                String authorizationCode = request.getParameter("code");
                // Verify state
                String storedState = (String) request.getSession().getAttribute("state");
                String receivedState = request.getParameter("state");
                if(receivedState == null || storedState == null || ! storedState.equals(receivedState))
                {
                    request.setAttribute("errorMessageKey", "invalidcredentials");
                    request.getRequestDispatcher("/login.ftl").forward(request, response);
                    return;
                }
                
                // Use code, make request to Google for getting token with user information
                GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                        new NetHttpTransport(), 
                        new JacksonFactory(),
                        System.getProperty("no.nibio.vips.logic.GOOGLE_OPENID_CLIENT_ID"),
                        System.getProperty("no.nibio.vips.logic.GOOGLE_OPENID_CLIENT_SECRET"),
                        authorizationCode,
                        Globals.PROTOCOL + "://" + ServletUtil.getServerName(request) + "/oauth2callback" 
                ).execute();
                GoogleIdToken idToken = GoogleIdToken.parse(new JacksonFactory(), tokenResponse.getIdToken());
                Payload payload = idToken.getPayload();
                
                // Try to find the user
                VipsLogicUser user = SessionControllerGetter.getUserBean().getUser(payload.getSubject(), UserAuthenticationType.TYPE_OPENID_GOOGLE);
                if(user != null)
                {
                    request.getSession().setAttribute("user", user);
                    UUID uUUID = this.handleRememberUser(request, response, user);
                    if(returnUUID)
                    {
                        nextPage += (nextPage.contains("?") ? "&": "?") + "returnUUID=" + uUUID.toString();
                    }
                    if(nextPage.indexOf(Globals.PROTOCOL) == 0)
                    {
                        System.out.println("nextPage=" + nextPage);
                        response.sendRedirect(nextPage);
                    }
                    else
                    {
                        response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://").append(ServletUtil.getServerName(request)).append(nextPage).toString());
                    }
                }
                else
                {
                    // This might be 
                    // * a new user
                    // * an existing user logging in with OpenId/Google for the first time.
                    // Sending user to form asking this question
                    request.setAttribute("userAuthenticationTypeId", UserAuthenticationType.TYPE_OPENID_GOOGLE);
                    request.getSession().setAttribute("openId", payload.getSubject());
                    request.getRequestDispatcher("/registerOpenIdForm.ftl").forward(request, response);
                }
            }
        }
        // A log out request
        else if(request.getServletPath().contains("logout"))
        {
            request.getSession().removeAttribute("user");
            Cookie rememberedUser = ServletUtil.getCookie(request, "rememberedUser");
            if(rememberedUser != null)
            {
                rememberedUser.setMaxAge(0);
                response.addCookie(rememberedUser);
                SessionControllerGetter.getUserBean().deleteUserUuid(UUID.fromString(rememberedUser.getValue()));
            }
            request.setAttribute("messageKey","logoutsuccess");
            request.getRequestDispatcher("/login.ftl").forward(request, response);
        }
        // A login attempt
        else if(request.getServletPath().contains("loginsubmit"))
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

                    if(user != null && user.getUserStatusId().equals(Globals.USER_STATUS_APPROVED))
                    {
                        request.getSession().setAttribute("user", user);
                        UUID uUUID = this.handleRememberUser(request, response, user);
                        if(returnUUID)
                        {
                            nextPage += (nextPage.contains("?") ? "&": "?") + "returnUUID=" + uUUID.toString();
                        }
                        if(nextPage.indexOf(Globals.PROTOCOL) == 0)
                        {
                            System.out.println("nextPage=" + nextPage);
                            response.sendRedirect(nextPage);
                        }
                        else
                        {
                            response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://").append(ServletUtil.getServerName(request)).append(nextPage).toString());
                        }
                    }
                    else if(user != null && user.getUserStatusId().equals(Globals.USER_STATUS_AWAITING_EMAIL_VERIFICATION))
                    {
                        request.setAttribute("errorMessageKey", "emailNotVerified");
                        request.getRequestDispatcher("/login.ftl").forward(request, response);
                    }
                    else if(user != null && user.getUserStatusId().equals(Globals.USER_STATUS_AWAITING_APPROVAL))
                    {
                        request.setAttribute("errorMessageKey", "pleaseAwaitApproval");
                        request.getRequestDispatcher("/login.ftl").forward(request, response);
                    }
                    else
                    {
                        request.setAttribute("errorMessageKey", "invalidcredentials");
                        request.getRequestDispatcher("/login.ftl").forward(request, response);
                    }


                }
            }
            /*  Login with Google OpenConnect/OAuth2
                For documentation about how this is done, see:
                https://developers.google.com/accounts/docs/OAuth2WebServer
                and https://developers.google.com/accounts/docs/OpenIDConnect 
                ClientID, ClientSecret, callbacks etc. has been created by logging
                in to https://console.developers.google.com as tor-einar.skog@nibio.no
            */
            else if(userAuthenticationTypeId.equals(UserAuthenticationType.TYPE_OPENID_GOOGLE))
            {
                    // configure the return_to URL where your application will receive
                    // the authentication responses from the OpenID Connect provider
                    String serverName = ServletUtil.getServerName(request);
                    String callbackUrl = Globals.PROTOCOL + "://" + serverName + "/oauth2callback" ;
                    // We store the information about the next page in a session
                    // as Google does not accept to forward it
                    request.getSession().setAttribute("nextPage", URLEncoder.encode(nextPage, "UTF-8"));
                    request.getSession().setAttribute(LoginController.RETURN_UUID_PARAMETER_NAME, returnUUID);
                    request.getSession().setAttribute("rememberUser", request.getParameter("rememberUser"));
                    // Token to check for security (avoid man-in-the-middle)
                    String state = new BigInteger(130, new SecureRandom()).toString(32);
                    request.getSession().setAttribute("state", state);
                    String url =
                        new GoogleAuthorizationCodeRequestUrl(
                                System.getProperty("no.nibio.vips.logic.GOOGLE_OPENID_CLIENT_ID"),
                                callbackUrl,
                                Arrays.asList(
                                "https://www.googleapis.com/auth/userinfo.email")
                        )
                        .setState(state).build();
                    
                    // Redirect to Google for authentication
                    response.sendRedirect(url);
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
            if(request.getParameter("nextPage") != null)
            {
                request.setAttribute("checkRemember", request.getParameter("nextPage").indexOf(Globals.PROTOCOL) == 0);
            }
            request.setAttribute("messageKey", request.getParameter("messageKey"));
            request.setAttribute("errorMessageKey", request.getParameter("errorMessageKey"));
            request.getRequestDispatcher("/login.ftl").forward(request, response);
        }
    }
    
    /**
     * Utility method (hiding noisy code)
     * @param request
     * @return
     * @throws UnsupportedEncodingException 
     */
    private String getNextPage(HttpServletRequest request) throws UnsupportedEncodingException
    {
        String nextPage = request.getParameter("nextPage") != null ? 
                request.getParameter("nextPage")
                : request.getSession().getAttribute("nextPage") != null ?
                    URLDecoder.decode((String)request.getSession().getAttribute("nextPage"),"UTF-8")
                    :null;
        if(nextPage == null)
        {
            nextPage="/";
        }
        
        return nextPage;
    }
    
    /**
     * Should the generated UUID be returned to login client?
     * @param request
     * @return 
     */
    private Boolean getReturnUUID(HttpServletRequest request)
    {
        return request.getParameter(LoginController.RETURN_UUID_PARAMETER_NAME) != null ?
                request.getParameter(LoginController.RETURN_UUID_PARAMETER_NAME).equals("true")
                : request.getSession().getAttribute(LoginController.RETURN_UUID_PARAMETER_NAME) != null ?
                    (Boolean) request.getSession().getAttribute(LoginController.RETURN_UUID_PARAMETER_NAME)
                : false;
    }
    
    /**
     * 
     * @param request
     * @param response
     * @param user 
     */
    private UUID handleRememberUser(HttpServletRequest request, HttpServletResponse response, VipsLogicUser user)
    {
        
        String rememberUser = request.getParameter("rememberUser") != null ? 
                request.getParameter("rememberUser") 
                : (String) request.getSession().getAttribute("rememberUser");
        request.getSession().removeAttribute("rememberUser");
        if(rememberUser != null && rememberUser.equals("on"))
        {
            UserUuid uUUID = SessionControllerGetter.getUserBean().createAndPersistUserUuid(user);
            Cookie rememberedUser = new Cookie("rememberedUser", uUUID.getUserUuidPK().getUserUuid().toString());
            rememberedUser.setPath("/");
            rememberedUser.setMaxAge(Globals.DEFAULT_UUID_VALIDITY_DURATION_DAYS * 24 * 60 * 60);
            response.addCookie(rememberedUser);
            return uUUID.getUserUuidPK().getUserUuid();
        }
        else
        {
            Cookie rememberedUser = ServletUtil.getCookie(request, "rememberedUser");
            if(rememberedUser != null)
            {
                rememberedUser.setMaxAge(0);
                response.addCookie(rememberedUser);
                SessionControllerGetter.getUserBean().deleteUserUuid(UUID.fromString(rememberedUser.getValue()));
            }
            return null;
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
