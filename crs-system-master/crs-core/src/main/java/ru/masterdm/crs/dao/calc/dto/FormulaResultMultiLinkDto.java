package ru.masterdm.crs.dao.calc.dto;

import java.util.List;

/**
 * Data transfer object to get formula result trinity links.
 * @author Pavel Masalov
 */
public class FormulaResultMultiLinkDto {

    private Long formulaResultId;
    private List<FormulaResultMultiLinkDtoReference> reference;

    /**
     * Returns formula result primary id.
     * @return formula result primary id
     */
    public Long getFormulaResultId() {
        return formulaResultId;
    }

    /**
     * Sets formula result primary id.
     * @param formulaResultId formula result primary id
     */
    public void setFormulaResultId(Long formulaResultId) {
        this.formulaResultId = formulaResultId;
    }

    /**
     * Returns DTO with references ids.
     * @return DTO with references ids
     */
    public List<FormulaResultMultiLinkDtoReference> getReference() {
        return reference;
    }

    /**
     * Sets DTO with references ids.
     * @param reference DTO with references ids
     */
    public void setReference(List<FormulaResultMultiLinkDtoReference> reference) {
        this.reference = reference;
    }
}
