package ru.masterdm.crs.service.calc;

import java.time.LocalDateTime;
import java.util.List;

import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.calc.FormulaResult;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;

/**
 * Helper service to manage additional calculation operations.
 * @author Pavel Masalov
 */
public interface CalculationService {

    /**
     * Persists formula results with trinity link.
     * @param calculation calculation object
     * @param formulaAndResult formula objects with results
     * @param profile calculation profile
     * @param ldts load datetime
     */
    void persistsFormulaResultsAndTrinityLinks(Calculation calculation, Formula formulaAndResult, Entity profile, LocalDateTime ldts);

    /**
     * Save formula result.
     * @param formulaResult formula result object
     * @param ldts load datetime
     */
    void persistsFormulaResult(FormulaResult formulaResult, LocalDateTime ldts);

    /**
     * Get classifier values for calculation.
     * @param calculation calculation object
     * @param ldts load datetime
     * @return list of classifier values
     */
    List<Entity> getClassifierValues(Calculation calculation, LocalDateTime ldts);

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
     * Save calculation's classifier values.
     * @param calculation calculation object
     * @param classifierValues classifier values
     */
    void persistsClassifierValues(Calculation calculation, List<Entity> classifierValues);

    /**
     * Save input form's values.
     * @param calculation calculation object
     * @param inputForm input form
     * @param inputFormValues input form values
     */
    void persistsInputFormValues(Calculation calculation, EntityMeta inputForm, List<Entity> inputFormValues);
}
