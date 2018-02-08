package ru.masterdm.crs.web.exception;

import org.zkoss.util.resource.Labels;

import ru.masterdm.crs.exception.calc.FormulaException;
import ru.masterdm.crs.web.domain.UserProfile;

/**
 * Formula exception handler.
 * @author Alexey Kirilchev
 */
public class FormulaExceptionHandler implements ExceptionHandler<FormulaException> {

    @Override
    public ExceptionEnvelope getExceptionEnvelope(FormulaException e, UserProfile userProfile) {
        boolean hideStackTrace = true;
        String messageKey = null;
        switch (e.getErrorCode()) {
            case LIBRARY_INVALID_CHILDREN:
                messageKey = "exception_formula_library_children_message";
                break;
            case VARIABLE_INVALID_NAME:
                messageKey = "exception_formula_variable_name_regexp_message";
                break;
            case DUPLICATE_ATTRIBUTE_NAME:
                messageKey = "exception_formula_duplicate_attribute_name_message";
                break;
            case MULTIPLE_LIBRARY_EVAL_LANGS:
                messageKey = "exception_formula_multiple_library_eval_lang_message";
                break;
            default:
                hideStackTrace = false;
        }
        String errorMessage = (messageKey != null) ? Labels.getLabel(messageKey) : String.valueOf(e.getErrorCode());
        return new ExceptionEnvelope(errorMessage, hideStackTrace);
    }
}
