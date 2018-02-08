package ru.masterdm.crs.web.exception;

import org.zkoss.util.resource.Labels;

import ru.masterdm.crs.exception.integration.IntegrationException;
import ru.masterdm.crs.web.domain.UserProfile;

/**
 * Formula exception handler.
 * @author Alexey Kirilchev
 */
public class IntegrationExceptionHandler implements ExceptionHandler<IntegrationException> {

    @Override
    public ExceptionEnvelope getExceptionEnvelope(IntegrationException e, UserProfile userProfile) {
        boolean hideStackTrace = true;
        String errorMessage = null;
        switch (e.getErrorCode()) {
            case EXECUTION_EXCEPTION:
                errorMessage = Labels.getLabel("exception_integration_execution_exception", new Object[] {e.getIntegrationModule()});
                break;
            case INTEGRATION_SYSTEM_UNAVAILABLE:
                errorMessage = Labels.getLabel("exception_integration_integration_system_unavailable");
                break;
            case NO_SUCH_INTEGRATION_MODULE:
                errorMessage = Labels.getLabel("exception_integration_no_such_integration_module", new Object[] {e.getIntegrationModule()});
                break;
            default:
                errorMessage = String.valueOf(e.getErrorCode());
                hideStackTrace = false;
        }
        return new ExceptionEnvelope(errorMessage, hideStackTrace);
    }
}
