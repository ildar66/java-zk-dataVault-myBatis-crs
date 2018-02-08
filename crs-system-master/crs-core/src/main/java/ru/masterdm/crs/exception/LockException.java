package ru.masterdm.crs.exception;

/**
 * Exception raised at when manage global lock.
 * @author Pavel Masalov
 */
public class LockException extends CrsException implements ErrorCodeSupport<LockErrorCode> {

    private final LockErrorCode code;

    /**
     * Constructor.
     * @param code error code
     */
    public LockException(LockErrorCode code) {
        super((String) null);
        this.code = code;
    }

    /**
     * Constructor.
     * @param code error code
     * @param throwable cause
     */
    public LockException(LockErrorCode code, Throwable throwable) {
        super((String) null, throwable);
        this.code = code;
    }

    @Override
    public LockErrorCode getErrorCode() {
        return code;
    }
}
