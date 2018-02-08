package ru.masterdm.crs.integration;

import java.io.Serializable;

/**
 * Execution command, that is sent to integration module.
 * @author Alexey Chalov
 */
@SuppressWarnings("serial")
public class ExecutionCommand implements Serializable {

    private String module;
    private String operation;
    private Object context;

    /**
     * Returns integration module name.
     * @return integration module name
     */
    public String getModule() {
        return module;
    }

    /**
     * Sets integration module name.
     * @param module integration module name
     */
    public void setModule(String module) {
        this.module = module;
    }

    /**
     * Returns operation name.
     * @return operation name
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Sets operation name.
     * @param operation operation name
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * Returns command context.
     * @return command context
     */
    public Object getContext() {
        return context;
    }

    /**
     * Sets command context.
     * @param context command context
     */
    public void setContext(Object context) {
        this.context = context;
    }
}
