package ru.masterdm.crs.web.exception;

/**
 * Exception message.
 * @author Alexey Kirilchev
 */
public class ExceptionEnvelope {

    private String message;
    private boolean hideStackTrace;

    /**
     * Constructor.
     * @param message exception message
     */
    public ExceptionEnvelope(String message) {
        this(message, false);
    }

    /**
     * Constructor.
     * @param message exception message
     * @param hideStackTrace hide stack trace
     */
    public ExceptionEnvelope(String message, boolean hideStackTrace) {
        this.message = message;
        this.hideStackTrace = hideStackTrace;
    }

    /**
     * Returns exception message.
     * @return exception message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns true if needs hide stack trace, false otherwise.
     * @return true if needs hide stack trace, false otherwise
     */
    public boolean isHideStackTrace() {
        return hideStackTrace;
    }

}
