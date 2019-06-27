/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the NIBIO Open Source License as published by 
 * NIBIO, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * NIBIO Open Source License for more details.
 * 
 * You should have received a copy of the NIBIO Open Source License
 * along with VIPSLogic.  If not, see <http://www.nibio.no/licenses/>.
 * 
 */

package no.nibio.vips.logic.controller.session;

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
import no.nibio.vips.coremanager.service.ManagerResource;
import no.nibio.vips.entity.ModelConfiguration;
import no.nibio.vips.entity.ModelRunRequest;
import no.nibio.vips.entity.Result;
import no.nibio.vips.logic.scheduling.VIPSLogicTaskCollector;
import no.nibio.vips.logic.entity.ForecastConfiguration;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.TaskHistory;
import no.nibio.vips.logic.entity.TaskHistoryStatus;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.scheduling.TaskLoggerSchedulerListener;
import no.nibio.vips.logic.scheduling.VipsLogicTask;
import no.nibio.vips.logic.scheduling.VipsLogicTaskFactory;
import no.nibio.vips.logic.scheduling.model.PreprocessorException;
import no.nibio.vips.logic.scheduling.model.preprocessor.NaerstadModelPreprocessor;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.logic.util.SessionControllerGetter;
import no.nibio.vips.logic.util.SystemTime;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

/**
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
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
        
        // The different scheduling patterns. See http://www.adminschoice.com/crontab-quick-reference 
        SchedulingPattern halfPastPattern = new SchedulingPattern("30 * * * *");
        SchedulingPattern halfPastEveryThreeHoursPattern = new SchedulingPattern("30 */3 * * *");
        SchedulingPattern onTheHourPattern = new SchedulingPattern("0 * * * *");
        SchedulingPattern every10MinsPattern = new SchedulingPattern("*/10 * * * *");
        SchedulingPattern every20MinsPattern = new SchedulingPattern("*/20 * * * *");
        SchedulingPattern everyNightPattern = new SchedulingPattern("0 6 * * *");
        SchedulingPattern morningAndAfternoonPattern = new SchedulingPattern("15 6,12 * * *");
        
        // Running all forecasts every hour:30
        VIPSLogicTaskCollector modelRunCollector = new VIPSLogicTaskCollector(-1);
        
        // Separating the forecasts for each organization
        List<Organization> organizations = SessionControllerGetter.getUserBean().getOrganizationsWithActiveForecastConfigurations(SystemTime.getSystemTime());
        if(organizations != null && !organizations.isEmpty())
        {
            for(Organization org:organizations)
            {
                VipsLogicTask runAllForecastsTask = VipsLogicTaskFactory.createVipsLogicTask(VipsLogicTaskFactory.RUN_ALL_FORECAST_CONFIGURATIONS_TASK);
                runAllForecastsTask.setOrganization(org);
                modelRunCollector.getTasks().add(halfPastEveryThreeHoursPattern, runAllForecastsTask);
            }
        }
        else // We run an empty job for all VIPS systems just to show that we've tried, but there's nothing to do
        {
            VipsLogicTask runAllForecastsTask = VipsLogicTaskFactory.createVipsLogicTask(VipsLogicTaskFactory.RUN_ALL_FORECAST_CONFIGURATIONS_TASK);
            modelRunCollector.getTasks().add(halfPastEveryThreeHoursPattern, runAllForecastsTask);
        }
        modelRunCollector.getTasks().add(halfPastEveryThreeHoursPattern, VipsLogicTaskFactory.createVipsLogicTask(VipsLogicTaskFactory.UPDATE_MODEL_INFORMATION_TASK));
        
        
        // Update forecast cache
        VIPSLogicTaskCollector cacheHandlerCollector = new VIPSLogicTaskCollector(-1);
        
        cacheHandlerCollector.getTasks().add(onTheHourPattern, VipsLogicTaskFactory.createVipsLogicTask(VipsLogicTaskFactory.UPDATE_FORECAST_RESULT_CACHE_TABLE_TASK));
        
        // Update forecast summaries every 20 minutes
        VIPSLogicTaskCollector summariesCollector = new VIPSLogicTaskCollector(-1);
        
        summariesCollector.getTasks().add(onTheHourPattern, VipsLogicTaskFactory.createVipsLogicTask(VipsLogicTaskFactory.UPDATE_FORECAST_SUMMARY_TABLE_TASK));
        
        // Clean up UUIDs (for cookies/remembering client logins) once a day
        VIPSLogicTaskCollector deleteAllExpiredUserUuidsCollector = new VIPSLogicTaskCollector(-1);
        
        deleteAllExpiredUserUuidsCollector.getTasks().add(everyNightPattern, VipsLogicTaskFactory.createVipsLogicTask(VipsLogicTaskFactory.DELETE_ALL_EXPIRED_UUIDS_TASK));
        
        // Send forecast notifications
        VIPSLogicTaskCollector sendForecastNotificationsCollector = new VIPSLogicTaskCollector(-1);
        
        sendForecastNotificationsCollector.getTasks().add(morningAndAfternoonPattern, VipsLogicTaskFactory.createVipsLogicTask(VipsLogicTaskFactory.SEND_FORECAST_EVENT_NOTIFICATIONS_TASK));
        
        // Run grid models
        VIPSLogicTaskCollector runGridModelsCollector = new VIPSLogicTaskCollector(-1);
        runGridModelsCollector.getTasks().add(morningAndAfternoonPattern, VipsLogicTaskFactory.createVipsLogicTask(VipsLogicTaskFactory.RUN_GRID_MODELS_TASK));
        
        List<TaskCollector> definedTasks = new ArrayList<>();
        definedTasks.add(modelRunCollector);
        definedTasks.add(cacheHandlerCollector);
        definedTasks.add(summariesCollector);
        definedTasks.add(deleteAllExpiredUserUuidsCollector);
        definedTasks.add(sendForecastNotificationsCollector);
        definedTasks.add(runGridModelsCollector);
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
    
    /**
     * 
     * @return List of scheduled tasks, ordered chronologically by next predicted
     * execution time
     */
    public SortedMap<String, List<VipsLogicTask>> getOrderedScheduledTasks(VipsLogicUser user)
    {
        if(user.isSuperUser())
        {
            return this.getOrderedScheduledTasks();
        }
        
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
                if(task.getOrganization() == null || task.getOrganization().equals(user.getOrganizationId()))
                {
                    List<VipsLogicTask> tasksAtSameTime = sortedTasks.get(format.format(nextExecutionTime));
                    if(tasksAtSameTime == null)
                    {
                        tasksAtSameTime = new ArrayList<>();
                        sortedTasks.put(format.format(nextExecutionTime), tasksAtSameTime);
                    }
                    tasksAtSameTime.add(task);
                }
            }
        }
        return sortedTasks;
        
    }
    
    public List<VipsLogicTask> getVipsLogicTasks(VipsLogicUser user)
    {
        // Super User
        if(user.isSuperUser())
        {
            return VipsLogicTaskFactory.getAllVipsLogicTasks();
        }
        // Organization admin
        else if(user.isOrganizationAdmin())
        {
            return VipsLogicTaskFactory.getOrganizationAdminVipsLogicTasks();
        }
        else
        {
            return null;
        }
    }
    
    public ManagerResource getManagerResource()
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(System.getProperty("no.nibio.vips.logic.VIPSCOREMANAGER_URL"));
        ResteasyWebTarget rTarget = (ResteasyWebTarget) target;
        ManagerResource resource = rTarget.proxy(ManagerResource.class);
        return resource;
    }

    
    public void logTaskLaunching(TaskExecutor taskExecutor)
    {
        VipsLogicTask task = (VipsLogicTask) taskExecutor.getTask();
        
        String organizationName = "all organizations";
        
        if(task.getOrganization() != null)
        {
            organizationName = task.getOrganization().getOrganizationName();
        }
        System.out.println("############ Task " + task.getName() + " launched for " + organizationName + " at " + new Date(taskExecutor.getStartTime()).toString() + "!!! ############");
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
        taskHistory.setOrganization(task.getOrganization());
        taskHistory.setMessage(taskExecutor.getStatusMessage());
        em.persist(taskHistory);
        
        String organizationName = "all organizations";
        
        if(task.getOrganization() != null)
        {
            organizationName = task.getOrganization().getOrganizationName();
        }
        
        System.out.println("############ Task " + task.getName() + " for " + organizationName + " started at " + new Date(taskExecutor.getStartTime()) + ", finished OK at " + new Date() + "!!! ############");
    }
    
    public void logTaskFailed(TaskExecutor taskExecutor, Throwable throwable)
    {
        VipsLogicTask task = (VipsLogicTask) taskExecutor.getTask();
        TaskHistory taskHistory = new TaskHistory();
        taskHistory.setTaskFactoryId(task.getFactoryId());
        
        taskHistory.setStartTime(new Date(taskExecutor.getStartTime()));
        taskHistory.setFinishTime(new Date());
        taskHistory.setCompletenessAtFinish(taskExecutor.getCompleteness());
        taskHistory.setOrganization(task.getOrganization());
        String taskHistoryStatusId = taskHistory.getCompletenessAtFinish() > 0 ? TaskHistoryStatus.STATUS_FAILED_PARTLY : TaskHistoryStatus.STATUS_FAILED_COMPLETELY;
        taskHistory.setTaskHistoryStatusId(em.find(TaskHistoryStatus.class, taskHistoryStatusId));
        
        // Build stacktrace
        /*
        StringBuilder stackTrace = new StringBuilder();
        stackTrace.append(throwable.getMessage());
        for(StackTraceElement element: throwable.getCause().getStackTrace())
        {
            stackTrace.append(element.toString());
        }*/
        taskHistory.setMessage(taskExecutor.getStatusMessage());
        em.persist(taskHistory);
        
        String organizationName = "all organizations";
        
        if(task.getOrganization() != null)
        {
            organizationName = task.getOrganization().getOrganizationName();
        }
        
        System.out.println("############ Task " + task.getName() + 
                "for " + organizationName +
                " failed at + " + taskExecutor.getStartTime() + 
                ", finished at " + new Date() + 
                ". Status is " + taskHistory.getTaskHistoryStatusId() + 
                ". Error was " + taskHistory.getMessage().substring(0, Math.min(400, taskHistory.getMessage().length()))  + "[...]" +
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
    
    public List<TaskHistory> getTaskHistory(Date historyDate, VipsLogicUser user)
    {
        // SuperUser can see all, hear all, he is OMNIPOTENT
        if(user.isSuperUser())
        {
            return this.getTaskHistory(historyDate);
        }
        
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.setTime(historyDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        
        Date periodStart = cal.getTime();
        cal.add(Calendar.DATE, 1);
        Date periodEnd = cal.getTime();
        
        Query q = em.createNamedQuery("TaskHistory.findByPeriodAndOrganizationIncludingCommon");
        q.setParameter("periodStart", periodStart);
        q.setParameter("periodEnd", periodEnd);
        q.setParameter("organization", user.getOrganizationId());
        return q.getResultList();
    }
    
}
