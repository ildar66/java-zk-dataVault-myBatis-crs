package ru.masterdm.crs.dao.calc;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import ru.masterdm.crs.dao.calc.dto.FormulaResultParameter;
import ru.masterdm.crs.dao.calc.dto.FormulaResultMultiLinkDto;
import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.calc.FormulaResult;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.criteria.Criteria;

/**
 * Calc DAO.
 * @author Alexey Chalov
 */
public interface CalcDao {

    /**
     * Persists formula.
     * @param formula {@link Formula} instance
     */
    @Transactional
    void persistFormula(@Param("formula") Formula formula);

    /**
     * Removes formula.
     * @param formula {@link Formula} instance
     */
    @Transactional
    void removeFormula(@Param("formula") Formula formula);

    /**
     * Returns formula key by hub id.
     * @param id hub id of formula
     * @return formula key
     */
    String getFormulaKeyById(@Param("id") Long id);

    /**
     * Searches formula by key.
     * @param key formula key
     * @param ldts load date
     * @return {@link Formula} instance
     */
    @Transactional(readOnly = true)
    Formula getFormulaByKey(@Param("key") String key, @Param("ldts") LocalDateTime ldts);

    /**
     * Get formulas by query criteria.
     * @param criteria query criteria
     * @param ldts load datetime
     * @return list of formulas
     */
    List<Formula> getFormulas(@Param("criteria") Criteria criteria, @Param("ldts") LocalDateTime ldts);

    /**
     * Returns list of formulas with cyclic dependencies, that was stored during {@link #persistFormula(Formula)} execution.
     * @return list of formulas with cyclic dependencies
     */
    @Transactional(readOnly = true)
    List<Formula> getCyclicDependencyFormulas();

    /**
     * Returns flattened tree of formulas starting with root one, defined by <em>formula</em> parameter.
     * @param formula {@link Formula} instance
     * @param actualityTs actuality timestamp
     * @return list of formulas
     */
    @Transactional(readOnly = true)
    List<Formula> getFormulaFlattenedTree(@Param("formula") Formula formula, @Param("actualityTs") LocalDateTime actualityTs);

    /**
     * Prepares list of flattened formula trees, found by filtering parameters.
     * @param criteria {@link Criteria} instance for filtering
     * @param calculation calculation. Can be null if no needs to filter by calculation
     * @param ldts load date timestamp
     */
    @Transactional
    void prepareFilteredFlattenedFormulaTrees(@Param("criteria") Criteria criteria, @Param("calculation") Calculation calculation,
                                              @Param("ldts") LocalDateTime ldts);

    /**
     * Returns list of flattened formula trees, found by filtering parameters.
     * @param criteria criteria
     * @return list of flattened formula trees
     */
    @Transactional(readOnly = true)
    List<Formula> getFilteredFlattenedFormulaTrees(@Param("criteria") Criteria criteria);

    /**
     * Get formula id by its key.
     * @param key formula key
     * @return formula id, null if formula not found
     */
    Long getFormulaIdByKey(@Param("key") String key);

    /**
     * Returns flag indicates calculated calculation.
     * @param id satellite id
     * @return <code>true</code> if calculation is calculated
     */
    boolean isCalculationCalculated(@Param("id") Long id);

    /**
     * Returns flag indicates published calculation.
     * @param id satellite id
     * @return <code>true</code> if calculation is published
     */
    boolean isCalculationPublished(@Param("id") Long id);

    /**
     * Returns latest model version calculated by published records of model.
     * @param key model key
     * @param ldts load datetime
     * @return latest version, null if model does not exists or never was published
     */
    Long getModelLastVersion(@Param("key") String key, @Param("ldts") LocalDateTime ldts);

    /**
     * Return model publish flag.
     * @param hubId model hub id
     * @param ldts load datetime
     * @return flag value
     */
    boolean isModelPublished(@Param("hubId") Long hubId, @Param("ldts") LocalDateTime ldts);

    /**
     * Write trinity link for formula results.
     * @param calculation calculation object
     * @param formula formula object with result
     * @param formulaResult formula result for formula-calculation
     * @param profile calculation profile
     * @param removed removed flag
     * @param ldts load datetime
     */
    @Transactional
    void writeLinkCalcFormulaResult(@Param("calculation") Calculation calculation, @Param("formula") Formula formula,
                                    @Param("formulaResult") FormulaResult formulaResult, @Param("profile") Entity profile,
                                    @Param("removed") boolean removed, @Param("ldts") LocalDateTime ldts);

    /**
     * Read calc formula result trinity link by calcs and formulas.
     * @param params calculation, calculation profile and formula
     * @param ldts load datetime
     * @return link data access for formula result link
     */
    @MapKey("formulaResultId")
    Map<Long, FormulaResultMultiLinkDto> readLinkCalcFormulaResult(@Param("params") Collection<FormulaResultParameter> params,
                                                                   @Param("ldts") LocalDateTime ldts);
}