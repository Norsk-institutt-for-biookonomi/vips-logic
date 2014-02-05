/*
 * Copyright (c) 2013-2014 Bioforsk <http://www.bioforsk.no/>. 
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

package no.bioforsk.vips.logic.i18n;

import java.io.IOException;
import java.util.Locale;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import no.bioforsk.vips.logic.util.SystemTime;

/**
 * This filter checks if there are any changes in locale requests, and
 * changes servlet session settings accordingly
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class LocalizationFilter implements Filter{
    

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        /*
        // For debugging!!!
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        String line;
        while((line = reader.readLine()) != null)
        {
            System.out.println(line);
        }*/

        Locale currentLocale = SessionLocaleUtil.getCurrentLocale((HttpServletRequest)request);
        Locale browserRequestedLocale = ((HttpServletRequest)request).getLocale();
        String userRequestedLocaleStr = ((HttpServletRequest)request).getParameter("userRequestedLocale");
        Locale userRequestedLocale = userRequestedLocaleStr != null ? new Locale(userRequestedLocaleStr) : null;
        
        
        /* 
         * If currentLocale is not set, use either userRequestedLocale (1st priority) 
         * or browserRequestedLocale (2nd priority) or JVM's default locale (fallback)
         */ 
        if(currentLocale == null)
        {
            SessionLocaleUtil.setCurrentLocale((HttpServletRequest)request, userRequestedLocale != null ? userRequestedLocale : browserRequestedLocale != null ? browserRequestedLocale : Locale.getDefault());   
        }
        /*
         * If userRequestedLocale is set and differs from current, activate it
         */
        else if(userRequestedLocale != null && ! userRequestedLocale.equals(currentLocale))
        {
            SessionLocaleUtil.setCurrentLocale((HttpServletRequest)request,userRequestedLocale);
            // Store it for comparison with browserRequestedLocale
            ((HttpServletRequest)request).getSession().setAttribute("currentUserRequestedLocale", userRequestedLocale);
        }
        /*
         * I
         */
        else if(browserRequestedLocale != null)
        {
            // We check if there exists a currentUserRequestedLocale
            Locale currentUserRequestedLocale = (Locale) ((HttpServletRequest)request).getSession().getAttribute("currentUserRequestedLocale");
            // If there is no userRequested locale, browserrequested locale trumps currentLocale
            if(currentUserRequestedLocale == null)
            {
                if(!browserRequestedLocale.equals(currentLocale))
                {
                    SessionLocaleUtil.setCurrentLocale((HttpServletRequest)request,browserRequestedLocale);
                }
            }
        }
        
        // We always make the system time available for FreeMarker templates
        request.setAttribute("systemTime", SystemTime.getSystemTime());
        
        chain.doFilter(request, response);
        
    }
    

    @Override
    public void destroy() {
        
    }

    
}
