package ru.masterdm.crs.domain.entity.criteria;

/**
 * Association types of WHERE elements.
 * Joining groups of fields <code>(ex. (FIELD1='some') OR (FIELD1='other' AND FIELD2=32) )</code>
 * @author Pavel Masalov
 */
public enum Conjunction {
    /** AND logical condition. */
    AND("and"),
    /** AND logical condition followed by NOT. */
    AND_NOT("and not"),
    /** OR logical condition. */
    OR("or"),
    /** OR logical condition followed by NOT. */
    OR_NOT("or not");

    private String meaning;

    /**
     * Construct conjunction with meaning.
     * @param meaning meaning that wil bi injected into DSQL
     */
    Conjunction(String meaning) {
        this.meaning = meaning;
    }

    /**
     * Returns conjunction meaning.
     * @return conjunction with meaning
     */
    public String getMeaning() {
        return meaning;
    }
}
