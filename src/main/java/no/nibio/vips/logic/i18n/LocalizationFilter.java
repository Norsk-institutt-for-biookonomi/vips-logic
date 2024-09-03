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

package no.nibio.vips.logic.i18n;

import com.ibm.icu.util.ULocale;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import no.nibio.vips.i18n.LanguageUtil;

/**
 * This filter checks if there are any changes in locale requests, and
 * changes servlet session settings accordingly. It also keeps
 * track of available languages
 * @copyright 2016 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class LocalizationFilter implements Filter{
    private List<ULocale> availableLocales;

    private static final Map<String, String> LOCALE_MAPPING = new HashMap<>();
    static {
        LOCALE_MAPPING.put("no", "nb");
        LOCALE_MAPPING.put("nn", "nb");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        this.availableLocales = LanguageUtil.getAvailableLocalesWithDistinctLanguage();

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

        ULocale currentLocale = SessionLocaleUtil.getCurrentLocale((HttpServletRequest)request);
        ULocale browserRequestedLocale = getBrowserRequestedLocale(request);
        String userRequestedLocaleStr = request.getParameter("userRequestedLocale");
        ULocale userRequestedLocale = userRequestedLocaleStr != null ? new ULocale(userRequestedLocaleStr) : null;

        /*
         * If currentLocale is not set, use either userRequestedLocale (1st priority)
         * or browserRequestedLocale (2nd priority) or JVM's default locale (fallback)
         */
        if(currentLocale == null)
        {
            SessionLocaleUtil.setCurrentLocale((HttpServletRequest)request, userRequestedLocale != null ? userRequestedLocale : browserRequestedLocale != null ? browserRequestedLocale : ULocale.getDefault());
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
            ULocale currentUserRequestedLocale = (ULocale) ((HttpServletRequest)request).getSession().getAttribute("currentUserRequestedLocale");
            // If there is no userRequested locale, browserrequested locale trumps currentLocale
            if(currentUserRequestedLocale == null)
            {
                if(!browserRequestedLocale.equals(currentLocale))
                {
                    SessionLocaleUtil.setCurrentLocale((HttpServletRequest)request,browserRequestedLocale);
                }
            }
        }

        // Set the available locales
        ((HttpServletRequest)request).getSession().setAttribute("availableLocales", this.availableLocales);

        // We always make the system time available for FreeMarker templates
        //request.setAttribute("systemTime", SystemTime.getSystemTime());

        chain.doFilter(request, response);

    }

    /**
     * Get locale from request. Map from unsupported 'no' and 'nn', to 'nb'.
     * If locale or language is null or blank, null is returned.
     *
     * @param request The request from which to get the locale
     * @return A locale which is supported by the system, or null
     */
    private static ULocale getBrowserRequestedLocale(ServletRequest request) {
        Locale requestLocale = request.getLocale();
        if (requestLocale == null) {
            return null;
        }
        ULocale locale = ULocale.forLocale(requestLocale);
        String language = locale.getLanguage();
        if(language == null || language.isBlank()) {
            return null;
        }
        String mappedLanguage = LOCALE_MAPPING.getOrDefault(language, language);
        return new ULocale(mappedLanguage);
    }

    @Override
    public void destroy() {

    }


}
