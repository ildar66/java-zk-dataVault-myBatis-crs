package ru.masterdm.crs.domain.entity.meta;

/**
 * Available formula types.
 * @author Alexey Kirilchev
 */
public enum FormulaType {
    /** Formula. */
    FORMULA,
    /** Master formula. */
    MASTER_FORMULA,
    /** Library. */
    LIBRARY,
    /** System library. */
    SYS_LIBRARY,
    /** Precalculated formula. */
    PRECALCULATED_FORMULA
}
