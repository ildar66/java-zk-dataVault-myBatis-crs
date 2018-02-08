package ru.masterdm.crs.exception.calc;

import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.exception.ErrorCodeSupport;

/**
 * Error raised on invalid model operation.
 * @author Pavel Masalov
 */
public class ModelException extends CrsException implements ErrorCodeSupport<ModelErrorCode> {

    private ModelErrorCode code;

    /**
     * Construct object with code and message.
     * @param code error code
     * @param msg error message
     */
    public ModelException(ModelErrorCode code, String msg) {
        super(msg);
        this.code = code;
    }

    /**
     * Construct error with code.
     * @param code error code
     */
    public ModelException(ModelErrorCode code) {
        super(code.name());
        this.code = code;
    }

    @Override
    public ModelErrorCode getErrorCode() {
        return code;
    }
}
