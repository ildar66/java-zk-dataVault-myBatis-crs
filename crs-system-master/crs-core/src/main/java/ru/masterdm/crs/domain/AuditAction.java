package ru.masterdm.crs.domain;

/**
 * Enum of actions to audit.
 * @author Kuzmin Mikhail
 * @author Alexey Chalov
 */
public enum AuditAction {

    /** Calculation create. */
    CREATE_CALCULATION("createCalculationAudit"),
    /** Calculation copy. */
    COPY_CALCULATION,
    /** Calculation delete. */
    REMOVE_CALCULATION,
    /** Calculation evaluation. */
    EVAL_CALCULATION,
    /** Calculation publish. */
    PUBLISH_CALCULATION,
    /** Entity meta create. */
    CREATE_ENTITY_META("persistEntityMetaAudit"),
    /** Entity meta edit. */
    EDIT_ENTITY_META,
    /** Entity meta delete. */
    DELETE_ENTITY_META("deleteEntityMetaAudit"),
    /** Create model. */
    CREATE_MODEL("persistModelAudit"),
    /** Edit model. */
    EDIT_MODEL,
    /** Publish model. */
    PUBLISH_MODEL,
    /** Remove model. */
    REMOVE_MODEL,
    /** Formula create. */
    CREATE_FORMULA("persistFormulaAudit"),
    /** Formula edit. */
    EDIT_FORMULA,
    /** Formula delete. */
    REMOVE_FORMULA("removeFormulaAudit"),
    /** To be implemented. */
    EXPORT_DATA,
    /** To be implemented. */
    IMPORT_DATA;

    private final String customLogic;

    /**
     * Constructor.
     */
    AuditAction() {
        this("");
    }

    /**
     * Constructor.
     * @param customLogic custom logic bean processor
     */
    AuditAction(String customLogic) {
        this.customLogic = customLogic;
    }

    /**
     * Returns custom logic bean processor.
     * @return custom logic bean processor
     */
    public String customLogic() {
        return customLogic;
    }
}
