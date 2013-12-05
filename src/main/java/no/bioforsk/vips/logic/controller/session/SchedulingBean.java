package no.bioforsk.vips.logic.controller.session;

import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.SchedulingPattern;
import it.sauronsoftware.cron4j.TaskCollector;
import it.sauronsoftware.cron4j.TaskExecutor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import no.bioforsk.vips.coremanager.service.ManagerResource;
import no.bioforsk.vips.entity.ModelConfiguration;
import no.bioforsk.vips.entity.ModelRunRequest;
import no.bioforsk.vips.entity.Result;
import no.bioforsk.vips.logic.entity.ForecastResult;
import no.bioforsk.vips.logic.scheduling.SchedulingTest;
import no.bioforsk.vips.logic.scheduling.VIPSLogicTaskCollector;
import no.bioforsk.vips.logic.scheduling.model.AppleScabModelPreprocessor;
import no.bioforsk.vips.logic.scheduling.model.ForecastConfiguration;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Stateless
public class SchedulingBean {
    
    @PersistenceContext(unitName="VIPSLogic-PU")
    EntityManager em;
    
    // There can be only one systemScheduler!
    private static Scheduler systemScheduler;
    
    private Scheduler getSystemScheduler(){
        if(systemScheduler == null)
            systemScheduler = new Scheduler();
        return systemScheduler;
    }
    
    public void startSystemScheduler(){
        initSystemScheduler();
        getSystemScheduler().schedule("* * * * *", new SchedulingTest());
        getSystemScheduler().start();
    }
    
    public void stopSystemScheduler(){
        getSystemScheduler().stop();
    }
    
    private void initSystemScheduler(){
        
    }
    
    private List<TaskCollector> getDefinedTasks(){
        // Real life: Get stuff from database
        // Idea: One TaskCollector per organization? Then we can
        // manipulate tasks per org pretty neatly (?)
        
        // Also: the Task may be configured _here_ (since we use the Tasks constructor), 
        // at creation time.
        List<TaskCollector> definedTasks = new ArrayList<>();
        VIPSLogicTaskCollector testCollector = new VIPSLogicTaskCollector(-1);
        SchedulingPattern schedulingPattern = new SchedulingPattern("* * * * *");
        testCollector.getTasks().add(schedulingPattern, new SchedulingTest());
        definedTasks.add(testCollector);
        return definedTasks;
    }
    
    public TaskExecutor[] getRunningTasks(){
        return this.getSystemScheduler().getExecutingTasks();
    }
    
    public List<Result> testPreprocessor()
    {

            AppleScabModelPreprocessor p = new AppleScabModelPreprocessor();
            ForecastConfiguration c = em.find(ForecastConfiguration.class, 1);
            ModelConfiguration config = p.getModelConfiguration(c);

            ModelRunRequest request = new ModelRunRequest(config);
            Map<String,String> loginInfo = new HashMap<>();
            loginInfo.put("username","testuser");
            loginInfo.put("password","testpass");
            request.setLoginInfo(loginInfo);
            
            Response resp = this.getManagerResource().runModel("APPLESCABM", request);
            List<Result> results = resp.readEntity(new GenericType<List<Result>>(){});
            for(Result result:results)
            {
                ForecastResult fResult = new ForecastResult(result);
                em.persist(fResult);
            }
            return results;
    }
    
    private ManagerResource getManagerResource()
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(System.getProperty("no.bioforsk.vips.coremanager.VIPSCOREMANAGER_URL"));
        ResteasyWebTarget rTarget = (ResteasyWebTarget) target;
        ManagerResource resource = rTarget.proxy(ManagerResource.class);
        return resource;
    }
}
