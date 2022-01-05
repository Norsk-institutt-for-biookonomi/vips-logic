/*
 * Copyright (c) 2022 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.authenticate;


import java.io.IOException;
import java.net.URLEncoder;
import java.util.UUID;
import javax.ejb.EJB;
import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.nibio.vips.logic.controller.session.UserBean;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.util.ServletUtil;

/**
 * Ensures that user accessing a restricted resource is actually logged in. Redirects to login page if not
 * @copyright 2013-2022 <a href="http://www.nibio.no">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class AuthenticationFilter implements Filter{
    
    @EJB
    UserBean userBean;

    // The URLs that do not require login
    private String[] unprivilegedURLs;
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.setUnprivilegedURLs(Globals.UNPRIVILEGED_URLS);
    } 

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest)request;
        /*
        // For debugging
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String line;
        while((line = reader.readLine()) != null)
        {
            System.out.println(line);
        }*/
        if(isUnprivilegedURL(httpRequest))
        {
            chain.doFilter(request, response);
            //return;
        }
        else
        {
            // First: Check for session variable
            boolean clientAuthenticated = (httpRequest.getSession().getAttribute("user") != null &&  httpRequest.getSession().getAttribute("user") instanceof VipsLogicUser);
            // Then for UUID cookie that has not expired
            boolean clientRemembered = false;
            Cookie remembered = ServletUtil.getCookie(httpRequest, "rememberedUser");
            if(remembered != null)
            {
                VipsLogicUser user = userBean.findVipsLogicUser(UUID.fromString(remembered.getValue()));
                if(user != null)
                {
                    httpRequest.getSession().setAttribute("user", user);
                    clientRemembered = true;
                }
            }
            
            if(! clientAuthenticated && ! clientRemembered)
            {
                String nextPageDirective = "";
                if(!httpRequest.getServletPath().equals("/login"))
                {
                    String nextPage = ServletUtil.getFullRequestURI(httpRequest);
                    nextPageDirective= "?nextPage=" + URLEncoder.encode(nextPage, "UTF-8");
                }
                ((HttpServletResponse)response).sendRedirect(Globals.PROTOCOL + "://" + ServletUtil.getServerName(httpRequest) + "/login" + nextPageDirective);
            }
            else
            {
                chain.doFilter(request, response);
            }
            //return;
        }
    }
    
    private boolean isUnprivilegedURL(HttpServletRequest request) {
        String path = request.getServletPath();
        for (String unprivilegedURL : this.getUnprivilegedURLs()) 
        {
            if (path.contains(unprivilegedURL)) 
            {
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
