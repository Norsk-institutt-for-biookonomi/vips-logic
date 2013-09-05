package no.bioforsk.vips.logic.util;

import javax.servlet.http.HttpServletRequest;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class ServletUtil {
    /**
     * Because we may be behind a reverse proxy, we might have to read Apache headers
     * to resolve the actual called server name
     * @param request
     * @return 
     */
    public static String getServerName(HttpServletRequest request)
    {
        // request.getServerName() is fallback if we're not begubd a reverse proxy
        return request.getHeader("X-Forwarded-Host") != null ? request.getHeader("X-Forwarded-Host") : request.getServerName();
    }
    
}
