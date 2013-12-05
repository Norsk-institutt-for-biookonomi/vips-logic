package no.bioforsk.vips.logic.util;

import java.util.Locale;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class Globals {

    /** 
     * Authentication: These url patterns should not be authenticated.
     * The authentication takes place in {@see no.bioforsk.vips.logic.authenticate.AuthenticationFilter}
     * This is configured in web.xml
     */
    public static final String[] UNPRIVILEGED_URLS = {
        "/favicon.ico",
        "/images",
        "/js",
        "/login",
        "/remoteloginsuccess",
        "/css",
        "/403",
        "/404",
        "/rest",
        "/user",
        "/test/testlogin.jsp",
        "/test/testloginsuccess.jsp"
    };
    
    // Point of interest type IDs
    public static final Integer POI_TYPE_WEATHERSTATION=1;
    
    // User roles
    public static final Integer ROLE_SUPERUSER = 1;
    public static final Integer ROLE_ORGANIZATION_ADMINISTRATOR = 2;
    
    // User statuses
    public static Integer USER_STATUS_AWAITING_EMAIL_VERIFICATION = 1;
    public static Integer USER_STATUS_AWATING_APPROVAL = 2;
    public static Integer USER_STATUS_REJECTED = 3;
    public static Integer USER_STATUS_APPROVED = 4;
    public static Integer USER_STATUS_DISABLED = 5;

    
}
