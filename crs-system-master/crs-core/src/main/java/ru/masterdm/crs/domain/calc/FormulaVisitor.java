package ru.masterdm.crs.domain.calc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Formula visitor.
 * @author Alexey Chalov
 */
public class FormulaVisitor {

    private List<Pair<String, Formula>> calculatedFormulas = new ArrayList<>();

    /**
     * Visitor method.
     * @param attributeName name of result attribute
     * @param formula {@link Formula} instance
     */
    public void visit(String attributeName, Formula formula) {
        formula.getChildren().forEach(p -> p.getRight().accept(p.getLeft(), this));
        calculatedFormulas.add(FormulaDependencyPair.of(attributeName, formula));
    }

    /**
     * Returns list of formulas in order of their calculation.
     * @return list of formulas in order of their calculation
     */
    public List<Pair<String, Formula>> getCalcFormulas() {
        return calculatedFormulas;
    }
}
