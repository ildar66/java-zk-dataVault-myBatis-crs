package ru.masterdm.crs.domain.entity.criteria;

/**
 * Where item for reference child where.
 * @author Pavel Masalov
 */
public class ReferenceWhere extends Where {

    private Conjunction conjunction;

    /**
     * Returns conjunction.
     * @return conjunction
     */
    public Conjunction getConjunction() {
        return conjunction;
    }

    /**
     * Sets conjunction.
     * @param conjunction conjunction
     */
    public void setConjunction(Conjunction conjunction) {
        this.conjunction = conjunction;
    }
}
