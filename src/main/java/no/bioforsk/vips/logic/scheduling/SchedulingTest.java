package no.bioforsk.vips.logic.scheduling;

import it.sauronsoftware.cron4j.TaskExecutionContext;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class SchedulingTest extends it.sauronsoftware.cron4j.Task{

    @Override
    public void execute(TaskExecutionContext tec) throws RuntimeException {
        System.out.println("Another minute ticking by...");
        tec.setStatusMessage("Oh yes, we're running!!!");
        tec.setCompleteness(0d);
        try {
			Thread.sleep(1000L * 10L );
        } catch (InterruptedException e) {
                return;
        }
        
        double completeness = (double) 10/60;
        System.out.println("double completeness = " + completeness);
        tec.setCompleteness(completeness);
        System.out.println("Completeness=" + tec.getTaskExecutor().getCompleteness());
        
        try {
			Thread.sleep(1000L * 30L );
        } catch (InterruptedException e) {
                return;
        }
        completeness = (double) 40/60;
        tec.setCompleteness(completeness);
        System.out.println("Completeness=" + tec.getTaskExecutor().getCompleteness());
        try {
			Thread.sleep(1000L * 30L );
        } catch (InterruptedException e) {
                return;
        }
        System.out.println("Exit job after 70 seconds...");
    }
    
    @Override
    public boolean supportsStatusTracking()
    {
        return true;
    }
    
    @Override
    public boolean supportsCompletenessTracking()
    {
        return true;
    }

}
