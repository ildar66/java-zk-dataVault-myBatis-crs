package ru.masterdm.crs.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.EvalResult;
import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.calc.FormulaResult;
import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;

/**
 * Calc service interface.
 * @author Alexey Chalov
 */
public interface CalcService {

    /**
     * Stores formula to database.
     * @param formula {@link Formula} instance
     */
    void persistFormula(Formula formula);

    /**
     * Removes formula.
     * @param formula {@link Formula} instance
     */
    void removeFormula(Formula formula);

    /**
     * Returns formula found by key.
     * @param key formula key
     * @param ldts load date. If <code>null</code> returns actual data
     * @return {@link Formula} instance
     */
    Formula getFormulaByKey(String key, LocalDateTime ldts);

    /**
     * Get formulas by query criteria.
     * @param criteria query criteria
     * @param ldts load datetime
     * @return list of formulas
     */
    List<Formula> getFormulas(Criteria criteria, LocalDateTime ldts);

    /**
     * Performs script evaluation.
     * @param calculation {@link Calculation} instance
     * @return {@link Future} instance, containing finish calculation marker
     */
    Future<Void> eval(Calculation calculation);

    /**
     * Performs script evaluation.
     * @param formula {@link Formula} instance
     * @param attrs map of attributes
     * @return {@link EvalResult} instance
     */
    EvalResult eval(Formula formula, Map<String, Object> attrs);

    /**
     * Returns list of formula trees, found by filtering parameters.
     * @param criteria {@link Criteria} filter
     * @param ldts load date
     * @return list of formula trees
     */
    List<Formula> getFormulaTrees(Criteria criteria, LocalDateTime ldts);

    /**
     * Returns list of formula trees, found by filtering parameters.
     * @param criteria {@link Criteria} filter
     * @param calculation calculation
     * @param ldts load date
     * @return list of formula trees
     */
    List<Formula> getFormulaTrees(Criteria criteria, Calculation calculation, LocalDateTime ldts);

    /**
     * Returns formula existence flag.
     * @param key formula key
     * @return true if formula already exists
     */
    boolean isFormulaExists(String key);

    /**
     * Returns models found by criteria from database.
     * @param criteria criteria
     * @param rowRange paging object
     * @param ldts load datetime
     * @return models
     */
    List<Model> getModels(Criteria criteria, RowRange rowRange, LocalDateTime ldts);

    /**
     * Save model instance state to database.
     * @param model model
     */
    void persistModel(Model model);

    /**
     * Publish model.
     * Set next version number.
     * @param model model object
     */
    void publishModel(Model model);

    /**
     * Remove model.
     * @param model model object
     */
    void removeModel(Model model);

    /**
     * Returns list of published models found by criteria.
     * @param criteria criteria
     * @param rowRange paging object
     * @param ldts load datetime
     * @return published models
     */
    List<Model> getPublishedModels(Criteria criteria, RowRange rowRange, LocalDateTime ldts);

    /**
     * Returns list of draft models fount by criteria.
     * @param criteria filter criteria
     * @param rowRange paging objects
     * @param ldts load datetime
     * @return draft models
     */
    List<Model> getDraftModels(Criteria criteria, RowRange rowRange, LocalDateTime ldts);

    /**
     * Returns list of calculations, found by filtering parameters.
     * @param criteria criteria
     * @param rowRange paging object
     * @param ldts load datetime
     * @return calculations
     */
    List<Calculation> getCalculations(Criteria criteria, RowRange rowRange, LocalDateTime ldts);

    /**
     * Returns list of actual published calculations, found by filtering parameters.
     * @param criteria filter criteria
     * @param rowRange paging object
     * @param ldts load datetime
     * @return published calculations
     */
    List<Calculation> getPublishedCalculations(Criteria criteria, RowRange rowRange, LocalDateTime ldts);

    /**
     * Returns list of draft calculations, found by filtering parameters.
     * @param criteria filter criteria
     * @param rowRange paging object
     * @param ldts load datetime
     * @return draft calculations
     */
    List<Calculation> getDraftCalculations(Criteria criteria, RowRange rowRange, LocalDateTime ldts);

    /**
     * Returns true if calculation has exceptions.
     * @param calculation calculation
     * @return true if calculation has exceptions
     */
    boolean isCalculationHasException(Calculation calculation);

    /**
     * Publish calculation.
     * @param calculation calculation
     */
    void publishCalculation(Calculation calculation);

    /**
     * Persist calculation object.
     * @param calculation calculation object
     */
    void persistCalculation(Calculation calculation);

    /**
     * Copy calculation object.
     * @param calculation calculation object
     */
    void copyCalculation(Calculation calculation);

    /**
     * Remove calculation.
     * Only draft calculation can be removed.
     * @param calculation calculation object
     */
    void removeCalculation(Calculation calculation);

    /**
     * Get formula results by criteria.
     * @param criteria criteria object
     * @param rowRange paging object
     * @param ldts load datetime
     * @return list of formula results
     */
    List<FormulaResult> getFormulaResults(Criteria criteria, RowRange rowRange, LocalDateTime ldts);

    /**
     * Get classifier values for calculation.
     * @param calculation calculation object
     * @param ldts load datetime
     * @return list of classifier values
     */
    List<Entity> getClassifierValues(Calculation calculation, LocalDateTime ldts);

    /**
     * Save calculation's classifier values.
     * @param calculation calculation object
     * @param classifierValues classifier values
     */
    void persistsClassifierValues(Calculation calculation, List<Entity> classifierValues);

    /**
     * Get input form values for calculation.
     * @param calculation calculation object
     * @param inputForm input form
     * @param criteria filter and sort criteria. May be null to get all rows
     * @param rowRange rows range for pagination. May be null to get all rows
     * @param ldts load datetime
     * @return list of input form values
     */
    List<Entity> getInputFormValues(Calculation calculation, EntityMeta inputForm, Criteria criteria, RowRange rowRange, LocalDateTime ldts);

    /**
     * Save input form's values.
     * @param calculation calculation object
     * @param inputForm input form
     * @param inputFormValues input form values
     */
    void persistsInputFormValues(Calculation calculation, EntityMeta inputForm, List<Entity> inputFormValues);
}
