/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with VIPSLogic.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.authenticate;


import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.bioforsk.vips.logic.entity.VipsLogicUser;
import no.bioforsk.vips.logic.util.Globals;
import no.bioforsk.vips.util.ServletUtil;

/**
 * Ensures that user accessing a restricted resource is actually logged in. Redirects to login page if not
 * @copyright 2013 <a href="http://www.bioforsk.no">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class AuthenticationFilter implements Filter{

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
            boolean redirectToLogin = (httpRequest.getSession().getAttribute("user") == null || !( httpRequest.getSession().getAttribute("user")  instanceof VipsLogicUser));
            if(redirectToLogin)
            {
                String nextPageDirective = "";
                if(!httpRequest.getServletPath().equals("/login"))
                {
                    String nextPage = ServletUtil.getFullRequestURI(httpRequest);
                    nextPageDirective= "?nextPage=" + URLEncoder.encode(nextPage, "UTF-8");
                }
                ((HttpServletResponse)response).sendRedirect("http://" + ServletUtil.getServerName(httpRequest) + "/login" + nextPageDirective);
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
        for(int i=0;i<this.getUnprivilegedURLs().length;i++)
        {
            if(path.indexOf(this.getUnprivilegedURLs()[i]) >= 0)
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
