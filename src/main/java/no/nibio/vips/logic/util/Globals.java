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

package no.nibio.vips.logic.util;

/**
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
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
        "/remoteloginsuccess",
        "/oauth2callback",
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
    
    public static String[] availableTimeZones = {
        "UTC",
        "Etc/GMT-1",
        "Europe/Oslo",
        "Europe/Sarajevo",
        "Europe/Stockholm",
        "Europe/Helsinki",
        "Europe/Sofia"
    };
    
}
