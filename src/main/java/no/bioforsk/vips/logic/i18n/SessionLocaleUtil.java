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

import java.util.Locale;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;

/**
 * Presents a uniform way of getting locale information
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class SessionLocaleUtil {
        // Servlet session constants
    public static String ATTRIBUTE_CURRENT_LOCALE = "currentLocale";
    
    // Default locale for application is en_US
    public static Locale DEFAULT_LOCALE = Locale.US;

    /**
     * What is the locale in the current session?
     * @param request
     * @return 
     */
    public static Locale getCurrentLocale(HttpServletRequest request){
        return (Locale) request.getSession().getAttribute(ATTRIBUTE_CURRENT_LOCALE);
    }
    /**
     * Sets the current locale
     * @param request
     * @param newLocale 
     */
    public static void setCurrentLocale(HttpServletRequest request, Locale newLocale)
    {
        request.getSession().setAttribute(ATTRIBUTE_CURRENT_LOCALE, newLocale);
        request.getSession().setAttribute("i18nBundle", ResourceBundle.getBundle("no.bioforsk.vips.logic.i18n.vipslogictexts",newLocale));
    }
    
    public static String getI18nText(HttpServletRequest request, String key)
    {
        ResourceBundle i18nBundle = (ResourceBundle) request.getSession().getAttribute("i18nBundle");
        return i18nBundle.getString(key);
    }
    
    public static ResourceBundle getI18nBundle(HttpServletRequest request)
    {
        return (ResourceBundle) request.getSession().getAttribute("i18nBundle");
    }
}
