package ru.masterdm.crs.web.exception;

import org.zkoss.util.resource.Labels;

import ru.masterdm.crs.exception.calc.CalculationException;
import ru.masterdm.crs.web.domain.UserProfile;

/**
 * Calculation exception handler.
 * @author Pavel Masalov
 */
public class CalculationExceptionHandler implements ExceptionHandler<CalculationException> {

    @Override
    public ExceptionEnvelope getExceptionEnvelope(CalculationException e, UserProfile userProfile) {
        boolean hideStackTrace = true;
        String messageKey = null;
        switch (e.getErrorCode()) {
            case PUBLISH_NOT_CALCULATED:
                messageKey = "exception_calc_publish_not_calculated";
                break;

            case PERSIST_PUBLISHED:
                messageKey = "exception_calc_persists_published";
                break;

            case REMOVE_PUBLISHED:
                messageKey = "exception_calc_remove_published";
                break;

            case CALCULATE_PUBLISHED:
                messageKey = "exception_calc_calculate_published";
                break;

            case PUBLISH_WITHOUT_CLIENT_OR_GROUP:
                messageKey = "exception_calc_publish_without_client_or_group";
                break;

            case PUBLISH_WITH_OBSOLETE_MODEL:
                messageKey = "exception_calc_publish_with_obsolete_model";
                break;

            case PUBLISH_WITH_EXCEPTION:
                messageKey = "exception_calc_publish_with_exception";
                break;

            default:
                hideStackTrace = false;
        }
        String errorMessage = (messageKey != null) ? Labels.getLabel(messageKey) : String.valueOf(e.getErrorCode());
        return new ExceptionEnvelope(errorMessage, hideStackTrace);
    }
}
