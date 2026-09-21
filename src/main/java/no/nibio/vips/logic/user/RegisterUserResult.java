package no.nibio.vips.logic.user;

import java.util.List;
import no.nibio.vips.logic.entity.VipsLogicUser;

public class RegisterUserResult {

    private final List<String> errorMessages;
    private final VipsLogicUser user;

    public RegisterUserResult(List<String> errorMessages) {
        this.errorMessages = errorMessages;
        this.user = null;
    }

    public RegisterUserResult(VipsLogicUser user) {
        this.errorMessages = List.of();
        this.user = user;
    }

    public boolean hasErrors() {
        return errorMessages != null && !errorMessages.isEmpty();
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public VipsLogicUser getUser() {
        return user;
    }
}
