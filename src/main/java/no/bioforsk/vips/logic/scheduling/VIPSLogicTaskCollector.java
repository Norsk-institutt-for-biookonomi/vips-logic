package no.bioforsk.vips.logic.scheduling;

import it.sauronsoftware.cron4j.TaskCollector;
import it.sauronsoftware.cron4j.TaskTable;

/**
 * Idea: One Task Collector per organization
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class VIPSLogicTaskCollector implements TaskCollector{
    private TaskTable taskTable;
    private Integer organizationId;
    
    public VIPSLogicTaskCollector(Integer organizationId)
    {
        this.organizationId = organizationId;
    }

    @Override
    public TaskTable getTasks() {
        if(this.taskTable == null)
            this.taskTable = new TaskTable();
        return this.taskTable;
    }
    
    /**
     * Intention: Reveal which organization this task collector is for.
     * @return 
     */
    public Integer getOrganizationId(){
        return -1;
    }
    
    

}
