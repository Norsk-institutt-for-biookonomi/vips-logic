package no.bioforsk.vips.logic.authenticate;


import java.io.IOException;
import java.net.URLEncoder;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.bioforsk.vips.logic.util.Globals;
import no.bioforsk.vips.util.ServletUtil;

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

        HttpServletRequest httpRequest = (HttpServletRequest)request;
        if(isUnprivilegedURL(httpRequest))
        {
            chain.doFilter(request, response);
            //return;
        }
        else
        {
            boolean redirectToLogin = httpRequest.getSession().getAttribute("user") == null;
            if(redirectToLogin)
            {
                String nextPageDirective = "";
                if(!httpRequest.getServletPath().equals("/login"));
                {
                    String nextPage = ServletUtil.getFullRequestURI(httpRequest);
                    nextPageDirective= "?nextPage=" + URLEncoder.encode(nextPage, "UTF-8");
                }
                ((HttpServletResponse)response).sendRedirect("http://" + ServletUtil.getServerName(httpRequest) + "/login" + nextPageDirective);
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
     * @param unprivilegedURLs the upriviligerteURLer to set
     */
    public void setUnprivilegedURLs(String[] unprivilegedURLs) {
        this.unprivilegedURLs = unprivilegedURLs;
    }

    
    
}
