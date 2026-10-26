/*
 * Copyright (c) 2019 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.util;

/**
 * @copyright 2019 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class Globals {

    /** 
     * Authentication: These url patterns should not be authenticated.
     * The authentication takes place in {@see no.nibio.vips.logic.authenticate.AuthenticationFilter}
     * This is configured in web.xml
     */
    public static final String[] UNPRIVILEGED_URLS = {
        "/favicon.ico",
        "/images",
        "/js",
        "/login",
        "/formdefinitions",
        "/remotelogin",
        "/remoteloginsuccess",
        "/oauth2callback",
        "/css",
        "/403",
        "/404",
        "/400",
        "/rest",
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
    
    public static int DEFAULT_UUID_VALIDITY_DURATION_DAYS = 60;
    
    public static String PROTOCOL = "http";

    public static String SERVER_NAME = "logic.vips.nibio.no";
    
}
