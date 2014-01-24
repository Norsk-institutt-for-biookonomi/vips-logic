package no.bioforsk.vips.logic.controller.session;

import it.sauronsoftware.cron4j.Predictor;
import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.SchedulingPattern;
import it.sauronsoftware.cron4j.TaskCollector;
import it.sauronsoftware.cron4j.TaskExecutor;
import it.sauronsoftware.cron4j.TaskTable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import no.bioforsk.vips.coremanager.service.ManagerResource;
import no.bioforsk.vips.entity.ModelConfiguration;
import no.bioforsk.vips.entity.ModelRunRequest;
import no.bioforsk.vips.entity.Result;
import no.bioforsk.vips.logic.scheduling.VIPSLogicTaskCollector;
import no.bioforsk.vips.logic.entity.ForecastConfiguration;
import no.bioforsk.vips.logic.entity.TaskHistory;
import no.bioforsk.vips.logic.entity.TaskHistoryStatus;
import no.bioforsk.vips.logic.scheduling.TaskLoggerSchedulerListener;
import no.bioforsk.vips.logic.scheduling.VipsLogicTask;
import no.bioforsk.vips.logic.scheduling.VipsLogicTaskFactory;
import no.bioforsk.vips.logic.scheduling.model.PreprocessorException;
import no.bioforsk.vips.logic.scheduling.model.preprocessor.NaerstadModelPreprocessor;
import no.bioforsk.vips.logic.util.Globals;
import no.bioforsk.vips.logic.util.SessionControllerGetter;
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
    
    // But there may be one or more one-off schedulers
    private static Set<Scheduler> oneOffSchedulers;
    //private SchedulerListener TaskLoggerSchedulerListener;
    
    /**
     * Get the system scheduler
     * @return 
     */
    public Scheduler getSystemScheduler(){
        if(systemScheduler == null)
        {
            systemScheduler = new Scheduler();
            systemScheduler.addSchedulerListener(new TaskLoggerSchedulerListener());
        }
        return systemScheduler;
    }
    
    public Set<Scheduler> getOneOffSchedulers()
    {
        if(oneOffSchedulers == null)
        {
            oneOffSchedulers = new HashSet<>();
        }
        return oneOffSchedulers;
    }
    
    /**
     * Initiates and starts the system scheduler
     */
    public void initSystemScheduler(){

        // In case this is called for a second (or later) time after system boot
        // we remove all existing tasks (in task collectors) before we add the
        // ones that are configured for starting.
        TaskCollector[] existingTaskCollectors = getSystemScheduler().getTaskCollectors();
        if(existingTaskCollectors != null)
        {
            for(TaskCollector existingTaskCollector:existingTaskCollectors)
            {
                getSystemScheduler().removeTaskCollector(existingTaskCollector);
            }
        }
        
        // Getting tasks from database (?)
        for(TaskCollector taskCollector: this.getDefinedTasks())
        {
            getSystemScheduler().addTaskCollector(taskCollector);
        }
    }
    
    /**
     * Stops scheduling of all tasks
     */
    public void stopSystemScheduler(){
        if(getSystemScheduler().isStarted())
        {
            getSystemScheduler().stop();
        }
    }
    
    /**
     * Start scheduling of tasks. No initiating,
     * starts same schedule as when it was stopped
     */
    public void startSystemScheduler(){
        if(!getSystemScheduler().isStarted())
        {
            getSystemScheduler().start();
        }
    }
    
    private List<TaskCollector> getDefinedTasks(){
        // Real life: Get stuff from database
        // Idea: One TaskCollector per organization? Then we can
        // manipulate tasks per org pretty neatly (?)
        // We keep it at one task collector for now.
        // In order to run same tasks for different organizations,
        // VipsLogicTask must have a configuration interface
        
        // Also: the Task may be configured _here_ (since we use the Tasks constructor), 
        // at creation time.
        
        VIPSLogicTaskCollector testCollector = new VIPSLogicTaskCollector(-1);
        SchedulingPattern schedulingPattern = new SchedulingPattern("30 * * * *");
        testCollector.getTasks().add(schedulingPattern, VipsLogicTaskFactory.createVipsLogicTask(VipsLogicTaskFactory.RUN_ALL_FORECAST_CONFIGURATIONS_TASK));
        
        List<TaskCollector> definedTasks = new ArrayList<>();
        definedTasks.add(testCollector);
        return definedTasks;
    }
    
    public TaskExecutor[] getRunningTasks(){
        List<TaskExecutor> runningTaskExecutors = new ArrayList<>();
        for(Scheduler oneOffScheduler:this.getOneOffSchedulers())
        {
            runningTaskExecutors.addAll(Arrays.asList(oneOffScheduler.getExecutingTasks()));
        }
        if(this.getSystemScheduler().isStarted())
        {
            runningTaskExecutors.addAll(Arrays.asList(this.getSystemScheduler().getExecutingTasks()));
        }
        return runningTaskExecutors.toArray(new TaskExecutor[runningTaskExecutors.size()]);
    }
    
    public List<Result> testPreprocessor() throws PreprocessorException
    {
            //AppleScabModelPreprocessor p = new AppleScabModelPreprocessor();
            NaerstadModelPreprocessor p = new NaerstadModelPreprocessor();
            ForecastConfiguration c = em.find(ForecastConfiguration.class, 7l);
            ModelConfiguration config = p.getModelConfiguration(c);

            ModelRunRequest request = new ModelRunRequest(config);
            Map<String,String> loginInfo = new HashMap<>();
            loginInfo.put("username","testuser");
            loginInfo.put("password","testpass");
            request.setLoginInfo(loginInfo);
            
            Response resp = this.getManagerResource().runModel(p.getModelId(), request);
            List<Result> results = resp.readEntity(new GenericType<List<Result>>(){});
            
            // We delete all former results before we store the new ones
            SessionControllerGetter.getForecastBean().storeResults(c,results);
            
            
            
            return results;
    }
    
    /**
     * 
     * @return List of scheduled tasks, ordered chronologically by next predicted
     * execution time
     */
    public SortedMap<String, List<VipsLogicTask>> getOrderedScheduledTasks()
    {
        SimpleDateFormat format = new SimpleDateFormat(Globals.defaultTimestampFormat);
        // Need to do some bucket sorting (date for next execution time as bucket)
        SortedMap<String, List<VipsLogicTask>> sortedTasks = new TreeMap<>();
        for(TaskCollector taskCollector: this.getSystemScheduler().getTaskCollectors())
        {
            TaskTable taskTable = taskCollector.getTasks();
            for(int i=0;i<taskTable.size();i++)
            {
                SchedulingPattern pattern = taskTable.getSchedulingPattern(i);
                Predictor p = new Predictor(pattern);
                Date nextExecutionTime = p.nextMatchingDate();
                VipsLogicTask task = (VipsLogicTask) taskTable.getTask(i);
                List<VipsLogicTask> tasksAtSameTime = sortedTasks.get(format.format(nextExecutionTime));
                if(tasksAtSameTime == null)
                {
                    tasksAtSameTime = new ArrayList<>();
                    sortedTasks.put(format.format(nextExecutionTime), tasksAtSameTime);
                }
                tasksAtSameTime.add(task);
            }
        }
        return sortedTasks;
        /*
        // Then we sort the buckets
        List<Date> executionTimes = new ArrayList(sortedTasks.keySet());
        Collections.sort(executionTimes);
        
        // Finally, we iterate over the ordered dates and add the tasks to the result list
        List<VipsLogicTask> scheduledTasks = new ArrayList<>();
        for(Date executionTime:executionTimes)
        {
            scheduledTasks.addAll(sortedTasks.get(executionTime));
        }
        
        return scheduledTasks;
                */
    }
    
    public List<VipsLogicTask> getAllVipsLogicTasks()
    {
        return VipsLogicTaskFactory.getAllVipsLogicTasks();
    }
    
    public ManagerResource getManagerResource()
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(System.getProperty("no.bioforsk.vips.logic.VIPSCOREMANAGER_URL"));
        ResteasyWebTarget rTarget = (ResteasyWebTarget) target;
        ManagerResource resource = rTarget.proxy(ManagerResource.class);
        return resource;
    }

    
    public void logTaskLaunching(TaskExecutor taskExecutor)
    {
        VipsLogicTask task = (VipsLogicTask) taskExecutor.getTask();
        
        System.out.println("############ Task " + task.getName() + " launched at + " + taskExecutor.getStartTime() + "!!! ############");
    }
    
    public void logTaskSucceeded(TaskExecutor taskExecutor)
    {
        VipsLogicTask task = (VipsLogicTask) taskExecutor.getTask();
        TaskHistory taskHistory = new TaskHistory();
        taskHistory.setTaskFactoryId(task.getFactoryId());
        taskHistory.setTaskHistoryStatusId(em.find(TaskHistoryStatus.class, TaskHistoryStatus.STATUS_OK));
        taskHistory.setStartTime(new Date(taskExecutor.getStartTime()));
        taskHistory.setFinishTime(new Date());
        taskHistory.setCompletenessAtFinish(taskExecutor.getCompleteness());
        em.persist(taskHistory);
        //System.out.println("############ Task " + task.getName() + " finished OK at " + taskExecutor.getStartTime() + ", finished at " + new Date() + "!!! ############");
    }
    
    public void logTaskFailed(TaskExecutor taskExecutor, Throwable throwable)
    {
        VipsLogicTask task = (VipsLogicTask) taskExecutor.getTask();
        TaskHistory taskHistory = new TaskHistory();
        taskHistory.setTaskFactoryId(task.getFactoryId());
        taskHistory.setTaskHistoryStatusId(em.find(TaskHistoryStatus.class, TaskHistoryStatus.STATUS_FAILED));
        taskHistory.setStartTime(new Date(taskExecutor.getStartTime()));
        taskHistory.setFinishTime(new Date());
        taskHistory.setCompletenessAtFinish(taskExecutor.getCompleteness());
        taskHistory.setMessage(throwable.getMessage());
        em.persist(taskHistory);
        
        
        System.out.println("############ Task " + task.getName() + 
                " failed at + " + taskExecutor.getStartTime() + 
                ", finished at " + new Date() + 
                ". Status is " + taskExecutor.getStatusMessage() + 
                ". Error was " + throwable.getMessage() +
                " ############");
    }
    
    public List<TaskHistory> getTaskHistory(Date historyDate)
    {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.setTime(historyDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        
        Date periodStart = cal.getTime();
        cal.add(Calendar.DATE, 1);
        Date periodEnd = cal.getTime();
        
        Query q = em.createNamedQuery("TaskHistory.findByPeriod");
        q.setParameter("periodStart", periodStart);
        q.setParameter("periodEnd", periodEnd);
        return q.getResultList();
    }
    
}
