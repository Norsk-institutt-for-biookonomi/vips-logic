/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package no.nibio.vips.logic.controller.session;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import io.quarkus.scheduler.Scheduler;
import no.nibio.vips.logic.entity.Organization;
import no.nibio.vips.logic.entity.TaskHistory;
import no.nibio.vips.logic.entity.TaskHistoryStatus;
import no.nibio.vips.logic.entity.VipsLogicUser;
import no.nibio.vips.logic.scheduling.VIPSLogicTaskCollector;
import no.nibio.vips.logic.scheduling.VipsLogicTask;
import no.nibio.vips.logic.scheduling.VipsLogicTask.TaskExecutionContext;
import no.nibio.vips.logic.scheduling.VipsLogicTask.TaskExecutor;
import no.nibio.vips.logic.scheduling.VipsLogicTaskFactory;
import no.nibio.vips.logic.util.Globals;
import no.nibio.vips.logic.util.SystemTime;

/**
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@ApplicationScoped
@Transactional
public class SchedulingBean {
    
    @Inject
    EntityManager em;
    
    @Inject
    UserBean userBean;
    @Inject
    ForecastBean forecastBean;
    @Inject
    Scheduler scheduler;

    private final List<VIPSLogicTaskCollector> systemTaskCollectors = new ArrayList<>();
    private final List<String> scheduledJobIds = new ArrayList<>();
    private final List<VipsLogicTask> scheduledTasks = new ArrayList<>();
    private final Set<TaskExecutor> runningTasks = ConcurrentHashMap.newKeySet();
    private final ExecutorService taskRunner = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "vipslogic-task");
        t.setDaemon(true);
        return t;
    });

    private volatile boolean systemSchedulerStarted = false;
    
    public boolean isSystemSchedulerStarted() {
        return systemSchedulerStarted;
    }

    public void launchSystemSchedulerTask(VipsLogicTask task) {
        taskRunner.submit(() -> executeTask(copyTask(task)));
    }
    
    /**
     * Initiates and starts the system scheduler
     */
    public void initSystemScheduler(){
        for (String jobId : scheduledJobIds) {
            scheduler.unscheduleJob(jobId);
        }
        scheduledJobIds.clear();
        systemTaskCollectors.clear();
        scheduledTasks.clear();

        for(VIPSLogicTaskCollector taskCollector: this.getDefinedTasks())
        {
            systemTaskCollectors.add(taskCollector);
            VIPSLogicTaskCollector.TaskTable taskTable = taskCollector.getTasks();
            for (int i = 0; i < taskTable.size(); i++) {
                String cron = taskTable.getCronPattern(i);
                VipsLogicTask templateTask = taskTable.getTask(i);
                String jobId = "vipslogic-" + templateTask.getFactoryId() + "-" + scheduledJobIds.size();
                final int factoryId = templateTask.getFactoryId();
                final Organization organization = templateTask.getOrganization();
                scheduler.newJob(jobId)
                        .setCron(cron)
                        .setTask(executionContext -> executeTask(createTask(factoryId, organization)))
                        .schedule();
                scheduledJobIds.add(jobId);
                scheduledTasks.add(templateTask);
            }
        }

        if (systemSchedulerStarted) {
            startSystemScheduler();
        } else {
            stopSystemScheduler();
        }
    }
    
    /**
     * Stops scheduling of all tasks
     */
    public void stopSystemScheduler(){
        for (String jobId : scheduledJobIds) {
            scheduler.pause(jobId);
        }
        systemSchedulerStarted = false;
    }
    
    /**
     * Start scheduling of tasks. No initiating,
     * starts same schedule as when it was stopped
     */
    public void startSystemScheduler(){
        for (String jobId : scheduledJobIds) {
            scheduler.resume(jobId);
        }
        systemSchedulerStarted = true;
    }
    
    private List<VIPSLogicTaskCollector> getDefinedTasks(){
        String halfPastEveryThreeHoursPattern = toQuarkusCron("30 */3 * * *");
        String onTheHourPattern = toQuarkusCron("0 * * * *");
        String everyNightPattern = toQuarkusCron("0 6 * * *");
        String morningAndAfternoonPattern = toQuarkusCron("15 6,12 * * *");
        
        VIPSLogicTaskCollector modelRunCollector = new VIPSLogicTaskCollector(-1);
        
        List<Organization> organizations = userBean.getOrganizationsWithActiveForecastConfigurations(SystemTime.getSystemTime());
        if(organizations != null && !organizations.isEmpty())
        {
            for(Organization org:organizations)
            {
                VipsLogicTask runAllForecastsTask = VipsLogicTaskFactory.createVipsLogicTask(VipsLogicTaskFactory.RUN_ALL_FORECAST_CONFIGURATIONS_TASK);
                runAllForecastsTask.setOrganization(org);
                modelRunCollector.getTasks().add(halfPastEveryThreeHoursPattern, runAllForecastsTask);
            }
        }
        else
        {
            VipsLogicTask runAllForecastsTask = VipsLogicTaskFactory.createVipsLogicTask(VipsLogicTaskFactory.RUN_ALL_FORECAST_CONFIGURATIONS_TASK);
            modelRunCollector.getTasks().add(halfPastEveryThreeHoursPattern, runAllForecastsTask);
        }
        modelRunCollector.getTasks().add(halfPastEveryThreeHoursPattern, VipsLogicTaskFactory.createVipsLogicTask(VipsLogicTaskFactory.UPDATE_MODEL_INFORMATION_TASK));
        
        VIPSLogicTaskCollector cacheHandlerCollector = new VIPSLogicTaskCollector(-1);
        cacheHandlerCollector.getTasks().add(onTheHourPattern, VipsLogicTaskFactory.createVipsLogicTask(VipsLogicTaskFactory.UPDATE_FORECAST_RESULT_CACHE_TABLE_TASK));
        
        VIPSLogicTaskCollector summariesCollector = new VIPSLogicTaskCollector(-1);
        summariesCollector.getTasks().add(onTheHourPattern, VipsLogicTaskFactory.createVipsLogicTask(VipsLogicTaskFactory.UPDATE_FORECAST_SUMMARY_TABLE_TASK));
        
        VIPSLogicTaskCollector deleteAllExpiredUserUuidsCollector = new VIPSLogicTaskCollector(-1);
        deleteAllExpiredUserUuidsCollector.getTasks().add(everyNightPattern, VipsLogicTaskFactory.createVipsLogicTask(VipsLogicTaskFactory.DELETE_ALL_EXPIRED_UUIDS_TASK));
        deleteAllExpiredUserUuidsCollector.getTasks().add(everyNightPattern, VipsLogicTaskFactory.createVipsLogicTask(VipsLogicTaskFactory.DISABLE_USERS_INACTIVE_FOR_TWO_YEARS_TASK));
        
        VIPSLogicTaskCollector sendForecastNotificationsCollector = new VIPSLogicTaskCollector(-1);
        sendForecastNotificationsCollector.getTasks().add(morningAndAfternoonPattern, VipsLogicTaskFactory.createVipsLogicTask(VipsLogicTaskFactory.SEND_FORECAST_EVENT_NOTIFICATIONS_TASK));
        
        List<VIPSLogicTaskCollector> definedTasks = new ArrayList<>();
        definedTasks.add(modelRunCollector);
        definedTasks.add(cacheHandlerCollector);
        definedTasks.add(summariesCollector);
        definedTasks.add(deleteAllExpiredUserUuidsCollector);
        definedTasks.add(sendForecastNotificationsCollector);
        return definedTasks;
    }
    
    public TaskExecutor[] getRunningTasks(){
        return runningTasks.toArray(new TaskExecutor[0]);
    }
    
    public SortedMap<String, List<VipsLogicTask>> getOrderedScheduledTasks()
    {
        SimpleDateFormat format = new SimpleDateFormat(Globals.defaultTimestampFormat);
        SortedMap<String, List<VipsLogicTask>> sortedTasks = new TreeMap<>();
        for (int i = 0; i < scheduledTasks.size(); i++) {
            VipsLogicTask task = scheduledTasks.get(i);
            Date nextExecutionTime = new Date();
            if (i < scheduledJobIds.size()) {
                Instant nextFire = scheduler.getScheduledJob(scheduledJobIds.get(i)).getNextFireTime();
                if (nextFire != null) {
                    nextExecutionTime = Date.from(nextFire);
                }
            }
            List<VipsLogicTask> tasksAtSameTime = sortedTasks.get(format.format(nextExecutionTime));
            if(tasksAtSameTime == null)
            {
                tasksAtSameTime = new ArrayList<>();
                sortedTasks.put(format.format(nextExecutionTime), tasksAtSameTime);
            }
            tasksAtSameTime.add(task);
        }
        return sortedTasks;
    }
    
    public SortedMap<String, List<VipsLogicTask>> getOrderedScheduledTasks(VipsLogicUser user)
    {
        if(user.isSuperUser())
        {
            return this.getOrderedScheduledTasks();
        }
        
        SimpleDateFormat format = new SimpleDateFormat(Globals.defaultTimestampFormat);
        SortedMap<String, List<VipsLogicTask>> sortedTasks = new TreeMap<>();
        for (int i = 0; i < scheduledTasks.size(); i++) {
            VipsLogicTask task = scheduledTasks.get(i);
            if(task.getOrganization() == null || task.getOrganization().equals(user.getOrganizationId()))
            {
                Date nextExecutionTime = new Date();
                if (i < scheduledJobIds.size()) {
                    Instant nextFire = scheduler.getScheduledJob(scheduledJobIds.get(i)).getNextFireTime();
                    if (nextFire != null) {
                        nextExecutionTime = Date.from(nextFire);
                    }
                }
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
    }
    
    public List<VipsLogicTask> getVipsLogicTasks(VipsLogicUser user)
    {
        if(user.isSuperUser())
        {
            return VipsLogicTaskFactory.getAllVipsLogicTasks();
        }
        else if(user.isOrganizationAdmin())
        {
            return VipsLogicTaskFactory.getOrganizationAdminVipsLogicTasks();
        }
        else
        {
            return null;
        }
    }
    
    public void logTaskLaunching(TaskExecutor taskExecutor)
    {
        VipsLogicTask task = taskExecutor.getTask();
        
        String organizationName = "all organizations";
        
        if(task.getOrganization() != null)
        {
            organizationName = task.getOrganization().getOrganizationName();
        }
        System.out.println("############ Task " + task.getName() + " launched for " + organizationName + " at " + new Date(taskExecutor.getStartTime()).toString() + "!!! ############");
    }
    
    public void logTaskSucceeded(TaskExecutor taskExecutor)
    {
        VipsLogicTask task = taskExecutor.getTask();
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
        VipsLogicTask task = taskExecutor.getTask();
        TaskHistory taskHistory = new TaskHistory();
        taskHistory.setTaskFactoryId(task.getFactoryId());
        
        taskHistory.setStartTime(new Date(taskExecutor.getStartTime()));
        taskHistory.setFinishTime(new Date());
        taskHistory.setCompletenessAtFinish(taskExecutor.getCompleteness());
        taskHistory.setOrganization(task.getOrganization());
        String taskHistoryStatusId = taskHistory.getCompletenessAtFinish() > 0 ? TaskHistoryStatus.STATUS_FAILED_PARTLY : TaskHistoryStatus.STATUS_FAILED_COMPLETELY;
        taskHistory.setTaskHistoryStatusId(em.find(TaskHistoryStatus.class, taskHistoryStatusId));
        
        taskHistory.setMessage(taskExecutor.getStatusMessage());
        em.persist(taskHistory);
        
        String organizationName = "all organizations";
        
        if(task.getOrganization() != null)
        {
            organizationName = task.getOrganization().getOrganizationName();
        }
        
        String message = taskHistory.getMessage();
        System.out.println("############ Task " + task.getName() + 
                "for " + organizationName +
                " failed at + " + taskExecutor.getStartTime() + 
                ", finished at " + new Date() + 
                ". Status is " + taskHistory.getTaskHistoryStatusId() + 
                ". Error was " + (message != null ? message.substring(0, Math.min(400, message.length())) : throwable.getMessage())  + "[...]" +
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

    private void executeTask(VipsLogicTask task) {
        TaskExecutor taskExecutor = new TaskExecutor(task);
        runningTasks.add(taskExecutor);
        TaskExecutionContext context = new TaskExecutionContext(taskExecutor);
        try {
            logTaskLaunching(taskExecutor);
            task.execute(context);
            logTaskSucceeded(taskExecutor);
        } catch (Throwable throwable) {
            logTaskFailed(taskExecutor, throwable);
        } finally {
            runningTasks.remove(taskExecutor);
        }
    }

    private VipsLogicTask createTask(int factoryId, Organization organization) {
        VipsLogicTask task = VipsLogicTaskFactory.createVipsLogicTask(factoryId);
        if (organization != null) {
            task.setOrganization(organization);
        }
        return task;
    }

    private VipsLogicTask copyTask(VipsLogicTask task) {
        VipsLogicTask copy = VipsLogicTaskFactory.createVipsLogicTask(task.getFactoryId());
        copy.setOrganization(task.getOrganization());
        copy.setConfiguration(task.getConfiguration());
        return copy;
    }

    private static String toQuarkusCron(String cron4jPattern) {
        String[] parts = cron4jPattern.split(" ");
        return "0 " + parts[0] + " " + parts[1] + " " + parts[2] + " " + parts[3] + " ?";
    }
}
