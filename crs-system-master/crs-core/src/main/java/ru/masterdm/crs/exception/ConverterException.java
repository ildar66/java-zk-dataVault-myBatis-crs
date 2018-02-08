package ru.masterdm.crs.exception;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;

/**
 * Error raised on invalid converter operation.
 * @author Pavel Masalov
 */
public class ConverterException extends CrsException implements ErrorCodeSupport<ConverterErrorCode> {

    private ConverterErrorCode code;
    private AttributeMeta attributeMeta;
    private Object sourceValue;

    /**
     * Construct error with code.
     * @param code error code
     * @param attributeMeta attribute metadata
     * @param sourceValue value input to converter
     */
    public ConverterException(ConverterErrorCode code, AttributeMeta attributeMeta, Object sourceValue) {
        super(code.name());
        this.code = code;
        this.attributeMeta = attributeMeta;
        this.sourceValue = sourceValue;
    }

    /**
     * Construct error with code.
     * @param cause error that cause this exception
     * @param code error code
     * @param attributeMeta attribute metadata
     * @param sourceValue value input to converter
     */
    public ConverterException(Throwable cause, ConverterErrorCode code, AttributeMeta attributeMeta, Object sourceValue) {
        super(code.name() + ": " + cause.getMessage(), cause);
        this.code = code;
        this.attributeMeta = attributeMeta;
        this.sourceValue = sourceValue;
    }

    @Override
    public ConverterErrorCode getErrorCode() {
        return code;
    }

    /**
     * Returns attribute metadata.
     * @return attribute metadata
     */
    public AttributeMeta getAttributeMeta() {
        return attributeMeta;
    }

    /**
     * Returns attribute metadata.
     * @return attribute metadata
     */
    public Object getSourceValue() {
        return sourceValue;
    }

    /**
     * Get input value class type.
     * @return class type
     */
    public Class getSourceClass() {
        if (sourceValue == null)
            return null;
        return sourceValue.getClass();
    }
}
