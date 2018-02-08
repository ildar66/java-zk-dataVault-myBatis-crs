package ru.masterdm.crs.exception.calc;

import ru.masterdm.crs.exception.ErrorCode;

/**
 * Invalid formula error codes enumeration.
 * @author Alexey Chalov
 */
public enum FormulaErrorCode implements ErrorCode {

    /** Cyclic dependency error. */
    CYCLIC_DEPENDENCY(20000L),
    /** Duplicate attributes name. */
    DUPLICATE_ATTRIBUTE_NAME(null),
    /** Library children can contain only other libraries. */
    LIBRARY_INVALID_CHILDREN(null),
    /** Library children can contain other libraries the same eval lang. */
    MULTIPLE_LIBRARY_EVAL_LANGS(null),
    /** Variable name doesn't match regexp. */
    VARIABLE_INVALID_NAME(null);

    private Long code;

    /**
     * Constructor.
     * @param code error code
     */
    FormulaErrorCode(Long code) {
        this.code = code;
    }

    @Override
    public Long getCode() {
        return code;
    }
}
