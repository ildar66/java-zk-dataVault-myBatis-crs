package ru.masterdm.crs.web.exception;

import ru.masterdm.crs.web.domain.UserProfile;

/**
 * Exception handler.
 * @param <T> exception class
 * @author Alexey Kirilchev
 */
public interface ExceptionHandler<T extends Throwable> {

    /**
     * Returns exception envelope.
     * @param exception exception instance.
     * @param userProfile user profile
     * @return exception envelope
     */
    ExceptionEnvelope getExceptionEnvelope(T exception, UserProfile userProfile);
}
