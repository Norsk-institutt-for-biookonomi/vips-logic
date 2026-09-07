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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;

import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.UserAuthenticationType;
import no.nibio.vips.logic.entity.UserUuid;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.i18n.SessionLocaleUtil;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.util.ServletUtil;

/**
 * Logs a user in or out
 * 
 * @copyright 2013-2022 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class LoginController extends HttpServlet {
    // private static final String CLOSE_AND_RELOAD_PARENT = "close_and_reload_parent";

    private static final String RETURN_UUID_PARAMETER_NAME = "returnUUID";

    @Inject
    UserBean userBean;

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
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

        // A log out request
        if (request.getServletPath().contains("logout")) {
            VipsLogicUser user = request.getSession().getAttribute("user") != null ? (VipsLogicUser) request.getSession().getAttribute("user") : null;
            // Make sure we delete the current user and their UUID
            userBean.deleteUserUuid(user.getUserUuid());
            request.getSession().removeAttribute("user");

            // Check if we have a cookie to delete as well
            Cookie rememberedUser = ServletUtil.getCookie(request, "rememberedUser");
            if (rememberedUser != null) {
                rememberedUser.setMaxAge(0);
                response.addCookie(rememberedUser);
                // This is likely duplication(?) - or are there cases where this makes sense?
                userBean.deleteUserUuid(UUID.fromString(rememberedUser.getValue()));
            }
            request.setAttribute("messageKey", "logoutsuccess");
            request.getRequestDispatcher("/login.ftl").forward(request, response);
        }
        // A login attempt
        else if (request.getServletPath().contains("loginsubmit")) {
            // Which login method?
            Integer userAuthenticationTypeId = -1;
            try {
                userAuthenticationTypeId = Integer.valueOf(request.getParameter("userAuthenticationTypeId"));
            }
            // No method found, redirect to form again
            catch (NumberFormatException | NullPointerException ex) {
                request.getRequestDispatcher("/login.ftl").forward(request, response);
                return;
            }

            // Standard username/password login
            if (userAuthenticationTypeId.equals(UserAuthenticationType.TYPE_PASSWORD)) {
                String username = request.getParameter("username");
                String password = request.getParameter("password");
                try (PrintWriter out = response.getWriter()) {

                    Map<String, String> creds = new HashMap<>();
                    creds.put("username", username);
                    creds.put("password", password);

                    // Check user credentials
                    VipsLogicUser user = userBean.authenticateUser(creds);

                    if (user != null && user.getUserStatusId().equals(Globals.USER_STATUS_APPROVED)) {
                        UserUuid uUUID = userBean.createAndPersistUserUuid(user);
                        user.setUserUuid(uUUID.getUserUuidPK().getUserUuid());
                        request.getSession().setAttribute("user", user);
                        this.handleRememberUser(request, response, user, returnUUID);
                        if (returnUUID) {
                            nextPage += (nextPage.contains("?") ? "&" : "?") + "returnUUID=" + uUUID.getUserUuidPK().getUserUuid().toString();
                        }
                        if (nextPage.indexOf(Globals.PROTOCOL) == 0) {
                            // System.out.println("nextPage=" + nextPage);
                            response.sendRedirect(nextPage);
                        } else {
                            response.sendRedirect(new StringBuilder(Globals.PROTOCOL + "://").append(ServletUtil.getServerName(request)).append(nextPage).toString());
                        }
                    } else if (user != null && user.getUserStatusId().equals(Globals.USER_STATUS_AWAITING_EMAIL_VERIFICATION)) {
                        request.setAttribute("errorMessageKey", "emailNotVerified");
                        request.getRequestDispatcher("/login.ftl").forward(request, response);
                    } else if (user != null && user.getUserStatusId().equals(Globals.USER_STATUS_AWAITING_APPROVAL)) {
                        request.setAttribute("errorMessageKey", "pleaseAwaitApproval");
                        request.getRequestDispatcher("/login.ftl").forward(request, response);
                    } else {
                        request.setAttribute("errorMessageKey", "invalidcredentials");
                        request.getRequestDispatcher("/login.ftl").forward(request, response);
                    }

                }
            }
            // Authentication method not recognized, redirect to standard form
            else {
                request.getRequestDispatcher("/login.ftl").forward(request, response);
            }
        }
        // Login from a remote resource, e.g. an app. Return UUID
        else if (request.getServletPath().contains("remotelogin")) {
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            Map<String, String> creds = new HashMap<>();
            creds.put("username", username);
            creds.put("password", password);

            VipsLogicUser user = userBean.authenticateUser(creds);
            PrintWriter out = response.getWriter();
            if (user != null && user.getUserStatusId().equals(Globals.USER_STATUS_APPROVED)) {
                request.getSession().setAttribute("user", user);
                UserUuid uUUID = userBean.createAndPersistUserUuid(user);
                user.setUserUuid(uUUID.getUserUuidPK().getUserUuid());
                this.handleRememberUser(request, response, user, returnUUID);
                // All is well, return object
                ObjectMapper mapper = new ObjectMapper();
                mapper.writeValue(out, user);
                out.close();
            } else {
                response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());

                if (user != null && user.getUserStatusId().equals(Globals.USER_STATUS_AWAITING_EMAIL_VERIFICATION)) {
                    out.print(SessionLocaleUtil.getI18nText(request, "emailNotVerified"));
                } else if (user != null && user.getUserStatusId().equals(Globals.USER_STATUS_AWAITING_APPROVAL)) {
                    out.print(SessionLocaleUtil.getI18nText(request, "pleaseAwaitApproval"));
                    request.getRequestDispatcher("/login.ftl").forward(request, response);
                } else {
                    out.print(SessionLocaleUtil.getI18nText(request, "invalidcredentials"));
                }
            }
        }
        // No login attempt. Show form
        else {
            if (request.getParameter("nextPage") != null) {
                request.setAttribute("checkRemember", request.getParameter("nextPage").indexOf(Globals.PROTOCOL) == 0);
            }
            request.setAttribute("messageKey", request.getParameter("messageKey"));
            request.setAttribute("errorMessageKey", request.getParameter("errorMessageKey"));
            request.getRequestDispatcher("/login.ftl").forward(request, response);
        }
    }

    /**
     * Utility method (hiding noisy code)
     * 
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     */
    private String getNextPage(HttpServletRequest request) throws UnsupportedEncodingException {
        String nextPage = request.getParameter("nextPage") != null ? request.getParameter("nextPage")
                : request.getSession().getAttribute("nextPage") != null ? URLDecoder.decode((String) request.getSession().getAttribute("nextPage"), "UTF-8")
                        : null;
        if (nextPage == null) {
            nextPage = "/";
        }

        return nextPage;
    }

    /**
     * Should the generated UUID be returned to login client?
     * 
     * @param request
     * @return
     */
    private Boolean getReturnUUID(HttpServletRequest request) {
        return request.getParameter(LoginController.RETURN_UUID_PARAMETER_NAME) != null ? request.getParameter(LoginController.RETURN_UUID_PARAMETER_NAME).equals("true")
                : request.getSession().getAttribute(LoginController.RETURN_UUID_PARAMETER_NAME) != null ? (Boolean) request.getSession().getAttribute(LoginController.RETURN_UUID_PARAMETER_NAME)
                        : false;
    }

    /**
     * 
     * @param request
     * @param response
     * @param user       the VIPS user
     * @param returnUUID has a UUID been requested to be returned to the client?
     * @return
     */
    private void handleRememberUser(HttpServletRequest request, HttpServletResponse response, VipsLogicUser user, Boolean returnUUID) {
        // This is from the login form, the checkbox that you tick off to save your login
        String rememberUser = request.getParameter("rememberUser") != null ? request.getParameter("rememberUser")
                : (String) request.getSession().getAttribute("rememberUser");
        request.getSession().removeAttribute("rememberUser");
        if (returnUUID || (rememberUser != null && rememberUser.equals("on"))) {

            if (rememberUser != null && rememberUser.equals("on")) {
                Cookie rememberedUser = new Cookie("rememberedUser", user.getUserUuid().toString());
                rememberedUser.setPath("/");
                rememberedUser.setMaxAge(Globals.DEFAULT_UUID_VALIDITY_DURATION_DAYS * 24 * 60 * 60);
                response.addCookie(rememberedUser);
            }
            // return uUUID.getUserUuidPK().getUserUuid();
        }
        // Unremember the user both server side and browser side
        else {
            Cookie rememberedUser = ServletUtil.getCookie(request, "rememberedUser");
            if (rememberedUser != null) {
                rememberedUser.setMaxAge(0);
                response.addCookie(rememberedUser);
                userBean.deleteUserUuid(UUID.fromString(rememberedUser.getValue()));
            }
            // return null;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
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
     * Handles the HTTP
     * <code>POST</code> method.
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
