package ru.masterdm.crs.exception.calc;

import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.exception.ErrorCodeSupport;

/**
 * Error raised on invalid calculation operation.
 * @author Pavel Masalov
 */
public class CalculationException extends CrsException implements ErrorCodeSupport<CalculationErrorCode> {

    private CalculationErrorCode code;

    /**
     * Construct object with code and message.
     * @param code error code
     * @param msg error message
     */
    public CalculationException(CalculationErrorCode code, String msg) {
        super(msg);
        this.code = code;
    }

    /**
     * Construct error with code.
     * @param code error code
     */
    public CalculationException(CalculationErrorCode code) {
        super(code.name());
        this.code = code;
    }

    @Override
    public CalculationErrorCode getErrorCode() {
        return code;
    }
}
