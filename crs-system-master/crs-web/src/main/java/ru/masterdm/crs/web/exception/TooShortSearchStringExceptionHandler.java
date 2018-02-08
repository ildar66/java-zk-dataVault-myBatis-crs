package ru.masterdm.crs.web.exception;

import org.zkoss.util.resource.Labels;

import ru.masterdm.crs.exception.TooShortSearchStringException;
import ru.masterdm.crs.web.domain.UserProfile;

/**
 * Too short search string exception handler.
 * @author Igor Matushak
 */
public class TooShortSearchStringExceptionHandler implements ExceptionHandler<TooShortSearchStringException> {

    @Override
    public ExceptionEnvelope getExceptionEnvelope(TooShortSearchStringException e, UserProfile userProfile) {
        return new ExceptionEnvelope(Labels.getLabel("exception_too_short_search_string_message"), true);
    }
}
