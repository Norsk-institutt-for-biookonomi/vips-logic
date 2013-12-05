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
