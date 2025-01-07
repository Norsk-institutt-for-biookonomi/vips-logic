/*
 * Copyright (c) 2022 NIBIO <http://www.nibio.no/>.
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

package no.nibio.vips.logic.authenticate;


import java.io.IOException;
import java.net.URLEncoder;
import java.util.UUID;
import jakarta.ejb.EJB;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.util.ServletUtil;

/**
 * Ensures that user accessing a restricted resource is actually logged in. Redirects to login page if not
 * 
 * @copyright 2013-2022 <a href="http://www.nibio.no">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class AuthenticationFilter implements Filter {

    @EJB
    UserBean userBean;

    // The URLs that do not require login
    private String[] unprivilegedURLs;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.setUnprivilegedURLs(Globals.UNPRIVILEGED_URLS);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        /*
         * // For debugging BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
         * String line; while((line = reader.readLine()) != null) { System.out.println(line); }
         */
        if (isUnprivilegedURL(httpRequest)) {
            chain.doFilter(request, response);
            // return;
        } else {
            // First: Check for session variable
            boolean clientAuthenticated = (httpRequest.getSession().getAttribute("user") != null
                    && httpRequest.getSession().getAttribute("user") instanceof VipsLogicUser);
            // Then for UUID cookie that has not expired
            boolean clientRemembered = false;
            Cookie remembered = ServletUtil.getCookie(httpRequest, "rememberedUser");
            if (remembered != null) {
                VipsLogicUser user = userBean.findVipsLogicUser(UUID.fromString(remembered.getValue()));
                if (user != null) {
                    httpRequest.getSession().setAttribute("user", user);
                    clientRemembered = true;
                }
            }

            if (!clientAuthenticated && !clientRemembered) {
                String nextPageDirective = "";
                if (!httpRequest.getServletPath().equals("/login")) {
                    String nextPage = ServletUtil.getFullRequestURI(httpRequest);
                    nextPageDirective = "?nextPage=" + URLEncoder.encode(nextPage, "UTF-8");
                }
                ((HttpServletResponse) response).sendRedirect(Globals.PROTOCOL + "://"
                        + ServletUtil.getServerName(httpRequest) + "/login" + nextPageDirective);
            } else {
                chain.doFilter(request, response);
            }
            // return;
        }
    }

    private boolean isUnprivilegedURL(HttpServletRequest request) {
        String path = request.getServletPath();
        for (String unprivilegedURL : this.getUnprivilegedURLs()) {
            if (path.contains(unprivilegedURL)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {

    }



    /**
     * @return the upriviligerteURLer
     */
    public String[] getUnprivilegedURLs() {
        return unprivilegedURLs;
    }

    /**
     * @param unprivilegedURLs the upriviligerteURLer to set
     */
    public void setUnprivilegedURLs(String[] unprivilegedURLs) {
        this.unprivilegedURLs = unprivilegedURLs;
    }



}
