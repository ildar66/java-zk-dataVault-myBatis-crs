package ru.masterdm.crs.web.exception;

import ru.masterdm.crs.web.domain.UserProfile;

/**
 * Expected exception handler.
 * @author Alexey Kirilchev
 */
public class ExpectedExceptionHandler implements ExceptionHandler<Throwable> {

    @Override
    public ExceptionEnvelope getExceptionEnvelope(Throwable exception, UserProfile userProfile) {
        return new ExceptionEnvelope(exception.getMessage(), true);
    }
}
