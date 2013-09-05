package no.bioforsk.vips.logic.authenticate;


import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.bioforsk.vips.logic.util.Globals;
import no.bioforsk.vips.logic.util.ServletUtil;

/**
 * Ensures that user accessing a restricted resource is actually logged in. Redirects to login page if not
 * @copyright 2013 <a href="http://www.bioforsk.no">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class AuthenticationFilter implements Filter{

    // The URLs that do not require login
    private String[] unprivilegedURLs;
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.setUnprivilegedURLs(Globals.UNPRIVILEGED_URLS);
    } 

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        if(isUnprivilegedURL((HttpServletRequest)request))
        {
            chain.doFilter(request, response);
            //return;
        }
        else
        {
            boolean redirectToLogin = ((HttpServletRequest)request).getSession().getAttribute("user") == null;
            if(redirectToLogin)
            {
                //System.out.println("Could not find user");
                ((HttpServletResponse)response).sendRedirect("http://" + ServletUtil.getServerName((HttpServletRequest)request) + "/login");
                //((HttpServletResponse)response).sendError(HttpServletResponse.SC_FORBIDDEN);
            }
            else
            {
                chain.doFilter(request, response);
            }
            //return;
        }
    }
    
    private boolean isUnprivilegedURL(HttpServletRequest request) {
        String path = request.getServletPath();
        for(int i=0;i<this.getUnprivilegedURLs().length;i++)
        {
            if(path.indexOf(this.getUnprivilegedURLs()[i]) >= 0)
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        
    }

   

    

    /**
     * @return the upriviligerteURLer
     */
    public String[] getUnprivilegedURLs() {
        return unprivilegedURLs;
    }

    /**
     * @param upriviligerteURLer the upriviligerteURLer to set
     */
    public void setUnprivilegedURLs(String[] upriviligerteURLer) {
        this.unprivilegedURLs = upriviligerteURLer;
    }

    
    
}
