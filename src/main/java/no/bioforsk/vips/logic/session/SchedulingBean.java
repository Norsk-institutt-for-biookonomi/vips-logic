package no.bioforsk.vips.logic.session;

import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.SchedulingPattern;
import it.sauronsoftware.cron4j.TaskCollector;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import no.bioforsk.vips.logic.scheduling.SchedulingTest;
import no.bioforsk.vips.logic.scheduling.VIPSLogicTaskCollector;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
@Stateless
public class SchedulingBean {
    
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
        List<TaskCollector> definedTasks = new ArrayList<>();
        VIPSLogicTaskCollector testCollector = new VIPSLogicTaskCollector(-1);
        SchedulingPattern schedulingPattern = new SchedulingPattern("* * * * *");
        testCollector.getTasks().add(schedulingPattern, new SchedulingTest());
        definedTasks.add(testCollector);
        return definedTasks;
    }

}
