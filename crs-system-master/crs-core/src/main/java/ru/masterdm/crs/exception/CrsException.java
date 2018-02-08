package ru.masterdm.crs.exception;

/**
 * Base exception class for CRS application.
 * @author Alexey Chalov
 */
@SuppressWarnings("serial")
public class CrsException extends RuntimeException {

    /**
     * Constructor.
     * @param msg detail message
     */
    public CrsException(String msg) {
        this(msg, null);
    }

    /**
     * Constructor.
     * @param throwable underlying cause
     */
    public CrsException(Throwable throwable) {
        this(null, throwable);
    }

    /**
     * Constructor.
     * @param msg detail message
     * @param throwable underlying cause
     */
    public CrsException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
