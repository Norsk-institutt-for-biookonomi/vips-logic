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
import java.util.Locale;
import java.util.ResourceBundle;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

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

    private static final String I18N_BUNDLE_BASE_NAME = "no.nibio.vips.logic.i18n.vipslogictexts";

    /**
     * What is the locale in the current session?
     * @param request
     * @return 
     */
    public static ULocale getCurrentLocale(HttpServletRequest request){
        return resolveLocale(request);
    }

    public static ULocale getStoredLocale(HttpServletRequest request) {
        return getSessionAttribute(request, ATTRIBUTE_CURRENT_LOCALE, ULocale.class);
    }
    /**
     * Sets the current locale
     * @param request
     * @param newLocale 
     */
    public static void setCurrentLocale(HttpServletRequest request, ULocale newLocale)
    {
        HttpSession session = getSession(request, true);
        if (session == null) {
            return;
        }
        session.setAttribute(ATTRIBUTE_CURRENT_LOCALE, newLocale);
        session.setAttribute("i18nBundle", ResourceBundle.getBundle(I18N_BUNDLE_BASE_NAME, newLocale.toLocale()));
    }
    
    public static String getI18nText(HttpServletRequest request, String key)
    {
        return getI18nBundle(request).getString(key);
    }
    
    public static ResourceBundle getI18nBundle(HttpServletRequest request)
    {
        ResourceBundle sessionBundle = getSessionAttribute(request, "i18nBundle", ResourceBundle.class);
        if (sessionBundle != null) {
            return sessionBundle;
        }
        return ResourceBundle.getBundle(I18N_BUNDLE_BASE_NAME, resolveLocale(request).toLocale());
    }

    private static ULocale resolveLocale(HttpServletRequest request) {
        if (request == null) {
            return DEFAULT_LOCALE;
        }

        try {
            ULocale sessionLocale = getStoredLocale(request);
            if (sessionLocale != null) {
                return sessionLocale;
            }

            Locale requestLocale = request.getLocale();
            if (requestLocale != null && requestLocale.getLanguage() != null && !requestLocale.getLanguage().isBlank()) {
                return ULocale.forLocale(requestLocale);
            }
        } catch (IllegalStateException ex) {
            // Quarkus REST: no active servlet request/session
        }

        return DEFAULT_LOCALE;
    }

    private static HttpSession getSession(HttpServletRequest request, boolean create) {
        if (request == null) {
            return null;
        }
        try {
            return request.getSession(create);
        } catch (IllegalStateException ex) {
            return null;
        }
    }

    private static <T> T getSessionAttribute(HttpServletRequest request, String attributeName, Class<T> type) {
        HttpSession session = getSession(request, false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(attributeName);
        return type.isInstance(value) ? type.cast(value) : null;
    }
}
