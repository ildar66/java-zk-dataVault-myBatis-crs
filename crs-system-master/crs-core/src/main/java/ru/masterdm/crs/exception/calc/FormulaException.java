package ru.masterdm.crs.exception.calc;

import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.exception.ErrorCodeSupport;

/**
 * Invalid formula marker exception.
 * @author Alexey Chalov
 */
public class FormulaException extends CrsException implements ErrorCodeSupport<FormulaErrorCode> {

    private final FormulaErrorCode code;

    /**
     * Constructor.
     * @param code error code
     */
    public FormulaException(FormulaErrorCode code) {
        super((String) null);
        this.code = code;
    }

    /**
     * Constructor.
     * @param code error code
     * @param throwable cause
     */
    public FormulaException(FormulaErrorCode code, Throwable throwable) {
        super((String) null, throwable);
        this.code = code;
    }

    @Override
    public FormulaErrorCode getErrorCode() {
        return code;
    }
}

