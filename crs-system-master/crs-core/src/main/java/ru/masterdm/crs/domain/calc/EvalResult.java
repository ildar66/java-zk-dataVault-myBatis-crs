package ru.masterdm.crs.domain.calc;

/**
 * Represents formula evaluation result.
 * @author Alexey Chalov
 */
public class EvalResult {

    private final Object result;
    private final String output;
    private final Throwable exception;

    /**
     * Constructor.
     * @param result evaluation result
     * @param output script output
     * @param exception {@link Throwable} instance, that raised during script evaluation
     */
    public EvalResult(Object result, String output, Throwable exception) {
        this.result = result;
        this.output = output;
        this.exception = exception;
    }

    /**
     * Returns script execution result.
     * @return execution result
     */
    public Object getResult() {
        return result;
    }

    /**
     * Returns script console output.
     * @return console output
     */
    public String getOutput() {
        return output;
    }

    /**
     * Returns exception, if any, that raised during script execution.
     * @return {@link Throwable} instance
     */
    public Throwable getException() {
        return exception;
    }
}
