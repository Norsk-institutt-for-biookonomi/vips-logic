package no.nibio.vips.logic.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.HttpHeaders;
import no.nibio.vips.logic.entity.VipsLogicUser;

public final class ServletRequestUtil {

    public static final String USER_UUID_COOKIE = "userUuid";
    public static final String REMEMBERED_USER_COOKIE = "rememberedUser";

    private ServletRequestUtil() {
    }

    public static VipsLogicUser getSessionUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        try {
            return (VipsLogicUser) request.getSession().getAttribute("user");
        } catch (IllegalStateException ex) {
            return null;
        }
    }

    public static String getAuthorizationHeader(HttpServletRequest request, HttpHeaders headers) {
        if (headers != null) {
            String value = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        if (request == null) {
            return null;
        }
        try {
            return request.getHeader(HttpHeaders.AUTHORIZATION);
        } catch (IllegalStateException ex) {
            return null;
        }
    }

    public static String getUserUuidCookie(HttpServletRequest request, HttpHeaders headers) {
        String value = getCookieValue(request, headers, USER_UUID_COOKIE);
        if (value != null) {
            return value;
        }
        return getCookieValue(request, headers, REMEMBERED_USER_COOKIE);
    }

    public static String getCookieValue(HttpServletRequest request, HttpHeaders headers, String name) {
        if (headers != null) {
            var cookies = headers.getCookies();
            if (cookies != null) {
                jakarta.ws.rs.core.Cookie cookie = cookies.get(name);
                if (cookie != null && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                    return cookie.getValue();
                }
            }
            String fromHeader = parseCookieHeader(headers.getHeaderString("Cookie"), name);
            if (fromHeader != null) {
                return fromHeader;
            }
        }
        if (request == null) {
            return null;
        }
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                return null;
            }
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                    return cookie.getValue();
                }
            }
            return null;
        } catch (IllegalStateException ex) {
            return null;
        }
    }

    static String parseCookieHeader(String cookieHeader, String name) {
        if (cookieHeader == null || cookieHeader.isBlank() || name == null) {
            return null;
        }
        for (String part : cookieHeader.split(";")) {
            String[] nameValue = part.trim().split("=", 2);
            if (nameValue.length == 2 && name.equals(nameValue[0]) && !nameValue[1].isBlank()) {
                return nameValue[1];
            }
        }
        return null;
    }
}
