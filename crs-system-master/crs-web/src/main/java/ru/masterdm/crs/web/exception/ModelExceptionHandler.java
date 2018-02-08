package ru.masterdm.crs.web.exception;

import org.zkoss.util.resource.Labels;

import ru.masterdm.crs.exception.calc.ModelException;
import ru.masterdm.crs.web.domain.UserProfile;

/**
 * Model exception handler.
 * @author Sergey Valiev
 */
public class ModelExceptionHandler implements ExceptionHandler<ModelException> {

    @Override
    public ExceptionEnvelope getExceptionEnvelope(ModelException e, UserProfile userProfile) {
        boolean hideStackTrace = true;
        String messageKey = null;
        switch (e.getErrorCode()) {
            case REMOVE_PUBLISHED:
                messageKey = "exception_model_remove_published";
                break;
            default:
                hideStackTrace = false;
        }
        String errorMessage = (messageKey != null) ? Labels.getLabel(messageKey) : String.valueOf(e.getErrorCode());
        return new ExceptionEnvelope(errorMessage, hideStackTrace);
    }
}
