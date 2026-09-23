package no.nibio.vips.logic.util;

import jakarta.servlet.http.HttpServletRequest;
import no.nibio.vips.logic.entity.VipsLogicUser;

public final class ServletRequestUtil {

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
}
