package ru.masterdm.crs.domain.form;

/**
 * @author Vladimir Shvets
 */
public enum FormDateType {

    /** Calculation's date. */
    CURRENT,
    /** As of Jan 1 of the current year. */
    THIS_YEAR_BEGINNING,
    /** As of Jan 1 of the previous year. */
    LAST_YEAR_BEGINNING,
    /** User can set the offset in years, months, days. */
    OFFSET,
    /** Any date. */
    CUSTOM;

    /**
     * Offset types.
     */
    public enum OffsetType {
        DAYS, MONTHS, QUARTERS, YEARS
    }
}
