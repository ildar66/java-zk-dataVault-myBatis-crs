package ru.masterdm.crs.domain.entity.criteria;

/**
 * Base class to represent elements of WHERE predicate that can be connected to each other.
 * Element are preceded by conjunction.
 * @author Pavel Masalov
 */
public abstract class Connectable {

    /**
     * SPACE constant.
     */
    static final String SPACE = " ";

    private Conjunction conjunction;

    /**
     * Create without conjunction.
     * Typically this is single element at statement.
     */
    public Connectable() {
        conjunction = null;
    }

    /**
     * Create wlement with conjunction.
     * @param conjunction conjunction
     */
    public Connectable(Conjunction conjunction) {
        this.conjunction = conjunction;
    }

    /**
     * Returns element precede conjunction.
     * @return conjunction
     */
    public Conjunction getConjunction() {
        return conjunction;
    }

    /**
     * Sets element precede conjunction.
     * @param conjunction element precede conjunction
     */
    public void setConjunction(Conjunction conjunction) {
        this.conjunction = conjunction;
    }

    /**
     * Generate text for connectable element.
     * @return generated text
     */
    public abstract String getText();

}
