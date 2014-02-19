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

package no.bioforsk.vips.logic.util;

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
        "/test/testloginsuccess.jsp",
        "/test/mock.kml",
        "/public"
    };
    
    // Point of interest type IDs
    public static final Integer POI_TYPE_WEATHERSTATION=1;
    
    // User roles
    public static final Integer ROLE_SUPERUSER = 1;
    public static final Integer ROLE_ORGANIZATION_ADMINISTRATOR = 2;
    
    // User statuses
    public static Integer USER_STATUS_AWAITING_EMAIL_VERIFICATION = 1;
    public static Integer USER_STATUS_AWAITING_APPROVAL = 2;
    public static Integer USER_STATUS_REJECTED = 3;
    public static Integer USER_STATUS_APPROVED = 4;
    public static Integer USER_STATUS_DISABLED = 5;

    public static String defaultTimestampFormat = "yyyy-MM-dd HH:mm:ssXXX";
    public static String defaultDateFormat = "yyyy-MM-dd";
    
}
