package ru.masterdm.crs.integration;

import java.io.Serializable;

/**
 * Execution command result.
 * @author Alexey Chalov
 */
@SuppressWarnings("serial")
public class ExecutionCommandResult implements Serializable {

    private Object result;
    private String exception;

    /**
     * Returns result.
     * @return result
     */
    public Object getResult() {
        return result;
    }

    /**
     * Sets result.
     * @param result result
     */
    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * Returns exception if any.
     * @return exception if any
     */
    public String getException() {
        return exception;
    }

    /**
     * Sets exception if any.
     * @param exception exception if any
     */
    public void setException(String exception) {
        this.exception = exception;
    }
}
