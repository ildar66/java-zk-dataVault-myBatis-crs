package ru.masterdm.crs.exception;

/**
 * Too short search string exception.
 * @author Igor Matushak
 */
@SuppressWarnings("serial")
public class TooShortSearchStringException extends CrsException {

    /**
     * Constructor.
     * @param throwable throwable
     */
    public TooShortSearchStringException(Throwable throwable) {
        super(throwable);
    }
}
