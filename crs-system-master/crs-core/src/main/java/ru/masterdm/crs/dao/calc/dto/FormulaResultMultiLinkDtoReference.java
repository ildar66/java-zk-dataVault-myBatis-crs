package ru.masterdm.crs.dao.calc.dto;

/**
 * Data transfer object to get linked part of formula result trinity link.
 * @author Pavel Masalov
 */
public class FormulaResultMultiLinkDtoReference {

    private Long calculationId;
    private Long formulaId;
    private Long calcProfileId;

    /**
     * Returns calculation primary id.
     * @return calculation primary id
     */
    public Long getCalculationId() {
        return calculationId;
    }

    /**
     * Sets calculation primary id.
     * @param calculationId calculation primary id
     */
    public void setCalculationId(Long calculationId) {
        this.calculationId = calculationId;
    }

    /**
     * Returns formula primary id.
     * @return formula primary id
     */
    public Long getFormulaId() {
        return formulaId;
    }

    /**
     * Sets formula primary id.
     * @param formulaId formula primary id
     */
    public void setFormulaId(Long formulaId) {
        this.formulaId = formulaId;
    }

    /**
     * Returns calculation profile primary id.
     * @return calculation profile primary id
     */
    public Long getCalcProfileId() {
        return calcProfileId;
    }

    /**
     * Sets calculation profile primary id.
     * @param calcProfileId calculation profile primary id
     */
    public void setCalcProfileId(Long calcProfileId) {
        this.calcProfileId = calcProfileId;
    }
}
