package no.nibio.vips.logic.scheduling.tasks;

import no.nibio.vips.logic.controller.session.SessionControllerGetter;
import no.nibio.vips.logic.scheduling.VipsLogicTask;

public class DisableUsersInactiveForTwoYearsTask extends VipsLogicTask {

    @Override
    public String getConfigFormDefinition(String language) {
        return "{\"fields\":[]}";
    }

    @Override
    public void execute(TaskExecutionContext tec) throws RuntimeException {
        tec.setCompleteness(0d);
        SessionControllerGetter.getUserBean().disableUsersInactiveForTwoYears();
        tec.setCompleteness(1d);
    }

    @Override
    public boolean supportsStatusTracking() {
        return true;
    }

    @Override
    public boolean supportsCompletenessTracking() {
        return true;
    }
}
