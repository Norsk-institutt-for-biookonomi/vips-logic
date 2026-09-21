package no.nibio.vips.logic.user;

public class DeleteMeResult {

    public enum Kind {
        UNAUTHORIZED,
        BAD_REQUEST,
        NO_CONTENT,
        SERVER_ERROR
    }

    private final Kind kind;
    private final String message;

    public DeleteMeResult(Kind kind, String message) {
        this.kind = kind;
        this.message = message;
    }

    public Kind getKind() {
        return kind;
    }

    public String getMessage() {
        return message;
    }
}
