package no.bioforsk.vips.logic.service;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import java.util.List;
import java.util.Locale;
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
import no.bioforsk.vips.logic.authenticate.PasswordValidationException;
import no.bioforsk.vips.logic.entity.ForecastResult;
import no.bioforsk.vips.logic.i18n.SessionLocaleUtil;
import no.bioforsk.vips.logic.entity.ForecastConfiguration;
import no.bioforsk.vips.logic.entity.ForecastModelConfiguration;
import no.bioforsk.vips.logic.util.SessionControllerGetter;
import no.bioforsk.vips.logic.util.SystemTime;
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
    @Path("forecastresults/{forecastConfigurationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getForecastResults(@PathParam("forecastConfigurationId") Long forecastConfigurationId)
    {
        List<ForecastResult> results = SessionControllerGetter.getForecastBean().getForecastResults(forecastConfigurationId);
        return Response.ok().entity(results).build();
    }
    
    /**
     * Returns the requested forecast configuration
     * @param forecastConfigurationId
     * @return 
     */
    @GET
    @Path("forecastconfigurations/{forecastConfigurationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getForecastConfiguration(@PathParam("forecastConfigurationId") Long forecastConfigurationId)
    {
        ForecastConfiguration forecastConfiguration = SessionControllerGetter.getForecastBean().getForecastConfiguration(forecastConfigurationId);
        return Response.ok().entity(forecastConfiguration).build();
    }
    
    /**
     * Returns a list of forecasts for given organization
     * @param organizationId
     * @return 
     */
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
    
    
    
    @GET
    @Path("evaluatepassword/{password}")
    @Produces("text/plain;charset=UTF-8")
    public Response evaluatePassord(@PathParam("password") String password)
    {
        Locale currentLocale = SessionLocaleUtil.getCurrentLocale(httpServletRequest);
        try
        {
            boolean isPasswordValid = SessionControllerGetter.getUserBean().isPasswordValid(password, currentLocale);
            return Response.ok().entity("true").build();
            
        }
        catch(PasswordValidationException ex)
        {
            return Response.ok().entity(ex.getMessage()).build();
        }
    }
    
    @GET
    @Path("forecastmodelconfiguration/{forecastConfigurationId}")
    @Produces("application/json;charset=UTF-8")
    public Response getForecastModelConfiguration(@PathParam("forecastConfigurationId") Long forecastConfigurationId)
    {
        List<ForecastModelConfiguration> forecastModelConfigurations = SessionControllerGetter.getForecastBean().getForecastModelConfigurations(forecastConfigurationId);
        return Response.ok().entity(forecastModelConfigurations).build();
    }
    
    @GET
    @Path("forecastresults/aggregate")
    @Produces("application/vnd.google-earth.kml+xml;charset=utf-8")
    public Response getForecastResultsAggregate()
    {
            Kml retVal = SessionControllerGetter.getForecastBean().getForecastsAggregateKml(SystemTime.getSystemTime());
            return Response.ok().entity(retVal).build();
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
