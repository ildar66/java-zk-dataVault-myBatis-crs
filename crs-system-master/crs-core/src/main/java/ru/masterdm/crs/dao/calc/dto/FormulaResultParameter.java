package ru.masterdm.crs.dao.calc.dto;

import org.apache.commons.lang3.tuple.ImmutableTriple;

import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.entity.Entity;

/**
 * Parameter for query formula result.
 * @author Pavel Masalov
 */
public class FormulaResultParameter {

    private ImmutableTriple<Calculation, Entity, Formula> elements;

    /**
     * Create a new elements instance.
     * @param calculation the left value, may be null
     * @param profile calculation profile
     * @param formula the right value, may be null
     */
    public FormulaResultParameter(Calculation calculation, Entity profile, Formula formula) {
        elements = new ImmutableTriple<>(calculation, profile, formula);
    }

    /**
     * Get elements calculation.
     * @return calculation
     */
    public Calculation getCalculation() {
        return elements.getLeft();
    }

    /**
     * Get elements formula.
     * @return formula
     */
    public Formula getFormula() {
        return elements.getRight();
    }

    /**
     * Get calculation profile.
     * @return calculation profile
     */
    public Entity getProfile() {
        return elements.getMiddle();
    }

    /**
     * Get object for calculation and formula.
     * @param calculation calculation object
     * @param profile calculation profile
     * @param formula formula object
     * @return elements object
     */
    public static FormulaResultParameter of(Calculation calculation, Entity profile, Formula formula) {
        return new FormulaResultParameter(calculation, profile, formula);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FormulaResultParameter)) return false;

        FormulaResultParameter that = (FormulaResultParameter) o;

        return elements != null ? elements.equals(that.elements) : that.elements == null;
    }

    @Override
    public int hashCode() {
        return elements != null ? elements.hashCode() : 0;
    }
}
