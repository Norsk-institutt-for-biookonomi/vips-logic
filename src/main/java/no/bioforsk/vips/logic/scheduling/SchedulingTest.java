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
    }

}
