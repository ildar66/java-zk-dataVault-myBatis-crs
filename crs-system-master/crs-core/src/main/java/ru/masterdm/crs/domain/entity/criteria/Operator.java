package ru.masterdm.crs.domain.entity.criteria;

/**
 * SQL operators in WHERE conditions.
 * @author Pavel Masalov
 */
public enum Operator {
    /** = operator. */
    EQ("="),
    /** &gt; operator. */
    GT(">"),
    /** &lt; operator. */
    LT("<"),
    /** &gt;= operator. */
    GT_EQ(">="),
    /** &lt;= operator. */
    LT_EQ("<="),
    /** like 'something%' operator. */
    LIKE("like"),
    /** in (value1,value2) operator. */
    IN("in"),
    /** not in (value1,value2) operator. */
    NOT_IN("not in"),
    /** is null operator. */
    IS_NULL("is null"),
    /** is not null operator. */
    IS_NOT_NULL("is not null");

    private String meaning;

    /**
     * Create operator with meaning.
     * @param meaning meaning that will be injected into DSQL
     */
    Operator(String meaning) {
        this.meaning = meaning;
    }

    /**
     * Returns operator meaning.
     * @return operator meaning
     */
    public String getMeaning() {
        return meaning;
    }
}
