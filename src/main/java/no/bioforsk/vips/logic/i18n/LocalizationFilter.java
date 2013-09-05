package no.bioforsk.vips.logic.i18n;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

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

        Locale currentLocale = (Locale) ((HttpServletRequest)request).getSession().getAttribute("currentLocale");
        Locale browserRequestedLocale = ((HttpServletRequest)request).getLocale();
        String userRequestedLocaleStr = ((HttpServletRequest)request).getParameter("userRequestedLocale");
        Locale userRequestedLocale = userRequestedLocaleStr != null ? new Locale(userRequestedLocaleStr) : null;
        
        
        /* 
         * If currentLocale is not set, use either userRequestedLocale (1st priority) 
         * or browserRequestedLocale (2nd priority) or JVM's default locale (fallback)
         */ 
        if(currentLocale == null)
        {
            setSessionLocale((HttpServletRequest)request, userRequestedLocale != null ? userRequestedLocale : browserRequestedLocale != null ? browserRequestedLocale : Locale.getDefault());   
        }
        /*
         * If userRequestedLocale is set and differs from current, activate it
         */
        else if(userRequestedLocale != null && ! userRequestedLocale.equals(currentLocale))
        {
            setSessionLocale((HttpServletRequest)request,userRequestedLocale);
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
                    setSessionLocale((HttpServletRequest)request,browserRequestedLocale);
                }
            }
        }
        
        chain.doFilter(request, response);
        
    }
    

    @Override
    public void destroy() {
        
    }

    private void setSessionLocale(HttpServletRequest request, Locale newLocale)
    {
        ((HttpServletRequest)request).getSession().setAttribute("currentLocale", newLocale);
            ((HttpServletRequest)request).getSession().setAttribute("i18nBundle", ResourceBundle.getBundle("no.bioforsk.vips.logic.i18n.vipslogictexts",newLocale));
    }
}
