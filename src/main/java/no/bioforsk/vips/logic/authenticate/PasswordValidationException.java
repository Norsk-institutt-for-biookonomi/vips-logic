package no.bioforsk.vips.logic.authenticate;

/**
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class PasswordValidationException extends Exception {

    /**
     * Creates a new instance of <code>PasswordValidationException</code>
     * without detail message.
     */
    public PasswordValidationException() {
    }

    /**
     * Constructs an instance of <code>PasswordValidationException</code> with
     * the specified detail message.
     *
     * @param msg the detail message.
     */
    public PasswordValidationException(String msg) {
        super(msg);
    }
}
