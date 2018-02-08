package ru.masterdm.crs.exception.calc;

import java.util.List;

import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.exception.CrsException;

/**
 * Formula cyclic dependency marker exception.
 * @author Alexey Chalov
 */
public class FormulaCyclicDependencyException extends CrsException {

    private final List<Formula> formulas;

    /**
     * Constructor.
     * @param formulas list of formulas, having cyclic dependencies
     */
    public FormulaCyclicDependencyException(List<Formula> formulas) {
        super((String) null);
        this.formulas = formulas;
    }

    /**
     * Constructor.
     * @param formulas list of formulas, having cyclic dependencies
     * @param throwable cause
     */
    public FormulaCyclicDependencyException(List<Formula> formulas, Throwable throwable) {
        super((String) null, throwable);
        this.formulas = formulas;
    }

    /**
     * Returns list of formulas, having cyclic dependencies.
     * @return list of formulas, having cyclic dependencies
     */
    public List<Formula> getFormulas() {
        return formulas;
    }
}
