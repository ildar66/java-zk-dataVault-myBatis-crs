package ru.masterdm.crs.service.calc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.script.ScriptContext;
import javax.script.ScriptException;

import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.calc.FormulaResult;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.RowRange;

/**
 * Calc service support interface.
 * @author Alexey Chalov
 */
public interface FormulaService {

    /**
     * Build binldes for script context.
     * @param map context candidate
     * @param calculation calculation object
     * @param profile calculation profile
     * @return bindings with system variables
     */
    Map<String, Object> buildBindings(Map<String, Object> map, Calculation calculation, Entity profile);

    /**
     * Performs asynchronous script evaluation.
     * @param formula {@link Formula} instance
     * @param ctx {@link ScriptContext} instance
     * @return {@link Future} instance
     * @throws ScriptException if script error rise
     */
    Future<Object> eval(Formula formula, ScriptContext ctx) throws ScriptException;

    /**
     * Builds tree of formulas starting with root one.
     * @param root root formula
     * @param actualityDate actuality date
     * @return formula with all children set
     */
    Formula getFormulaTree(Formula root, LocalDateTime actualityDate);

    /**
     * Returns list of formula trees, found by filtering parameters.
     * @param criteria {@link Criteria} filter
     * @param calculation calculation
     * @param ldts load date
     * @return list of formula trees
     */
    List<Formula> getFormulaTrees(Criteria criteria, Calculation calculation, LocalDateTime ldts);

    /**
     * Get formula results by criteria.
     * @param criteria criteria object
     * @param rowRange paging object
     * @param ldts load datetime
     * @return list of formula results
     */
    List<FormulaResult> getFormulaResults(Criteria criteria, RowRange rowRange, LocalDateTime ldts);
}
