package ru.masterdm.crs.exception.calc;

/**
 * Calculation's exception codes.
 * @author Pavel Masalov
 */
public enum CalculationErrorCode {
    /** Can't publish calculation because it doesn't calculated. */
    PUBLISH_NOT_CALCULATED,
    /** Can't persist calculation because it's published already. */
    PERSIST_PUBLISHED,
    /** Can't remove calculation because it's published already. */
    REMOVE_PUBLISHED,
    /** Can't calculate because calculation is published already. */
    CALCULATE_PUBLISHED,
    /** Can't publish calculation without client or client group. */
    PUBLISH_WITHOUT_CLIENT_OR_GROUP,
    /** Can't publish calculation with obsolete model. */
    PUBLISH_WITH_OBSOLETE_MODEL,
    /** Can't publish calculation with exception. */
    PUBLISH_WITH_EXCEPTION
}
