package no.bioforsk.vips.logic.service;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import no.bioforsk.vips.coremanager.service.ManagerResource;
import no.bioforsk.vips.logic.entity.ForecastResult;
import no.bioforsk.vips.logic.scheduling.model.ForecastConfiguration;
import no.bioforsk.vips.logic.session.SessionControllerGetter;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.spi.HttpRequest;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Path("rest")
public class LogicService {
    private final static String VIPSCOREMANAGER_URL = System.getProperty("no.bioforsk.vips.coremanager.VIPSCOREMANAGER_URL");
    
    @Context
    private HttpRequest httpRequest;
    @Context
    private HttpServletRequest httpServletRequest;

    @GET
    @Path("testforecastresults")
    @Produces("application/json;charset=UTF-8")
    public Response testForecastResults()
    {
        List<ForecastResult> results = SessionControllerGetter.getForecastBean().getForecastResults();
        return Response.ok().entity(results).build();
    }
    
    @GET
    @Path("forecastresults/{forecastResultId}")
    @Produces("application/json;charset=UTF-8")
    public Response getForecastResults(@PathParam("forecastResultId") Long forecastResultId)
    {
        List<ForecastResult> results = SessionControllerGetter.getForecastBean().getForecastResults();
        return Response.ok().entity(results).build();
    }
    
    @GET
    @Path("organizationforecastconfigurations/{organizationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getForecastConfigurationsForOrganization(@PathParam("organizationId") Integer organizationId)
    {
        // First: Get all users for organization
        // TODO: Should be authenticated
        Response resp = this.getManagerResource().getUsersForOrganization(organizationId);
        List<Map> userList = resp.readEntity(List.class);
        // Then: Get forecasts for these users, collate and return
        List<ForecastConfiguration> forecasts = null;
        for(Map user:userList)
        {
            Integer userId = (Integer) user.get("vipsCoreUserId");
            if(forecasts == null)
                forecasts = SessionControllerGetter.getForecastBean().getForecastConfigurationsForUser(userId);
            else
                forecasts.addAll(SessionControllerGetter.getForecastBean().getForecastConfigurationsForUser(userId));
        }
        
        return Response.ok().entity(forecasts).build();
    }
    
    private ManagerResource getManagerResource()
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(VIPSCOREMANAGER_URL);
        ResteasyWebTarget rTarget = (ResteasyWebTarget) target;
        ManagerResource resource = rTarget.proxy(ManagerResource.class);
        return resource;
    }
}
