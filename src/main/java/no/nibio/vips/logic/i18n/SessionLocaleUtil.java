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

package no.nibio.vips.logic.i18n;

import com.ibm.icu.util.ULocale;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;

/**
 * Presents a uniform way of getting locale information
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class SessionLocaleUtil {
        // Servlet session constants
    public static String ATTRIBUTE_CURRENT_LOCALE = "currentLocale";
    
    // Default locale for application is en_US
    public static ULocale DEFAULT_LOCALE = ULocale.US;

    /**
     * What is the locale in the current session?
     * @param request
     * @return 
     */
    public static ULocale getCurrentLocale(HttpServletRequest request){
        return (ULocale) request.getSession().getAttribute(ATTRIBUTE_CURRENT_LOCALE);
    }
    /**
     * Sets the current locale
     * @param request
     * @param newLocale 
     */
    public static void setCurrentLocale(HttpServletRequest request, ULocale newLocale)
    {
        request.getSession().setAttribute(ATTRIBUTE_CURRENT_LOCALE, newLocale);
        request.getSession().setAttribute("i18nBundle", ResourceBundle.getBundle("no.nibio.vips.logic.i18n.vipslogictexts",newLocale.toLocale()));
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
