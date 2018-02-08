package ru.masterdm.crs.web.exception;

import java.util.stream.Stream;

import org.zkoss.zk.ui.WrongValueException;

import ru.masterdm.crs.exception.TooShortSearchStringException;
import ru.masterdm.crs.exception.calc.CalculationException;
import ru.masterdm.crs.exception.calc.FormulaCyclicDependencyException;
import ru.masterdm.crs.exception.calc.FormulaException;
import ru.masterdm.crs.exception.calc.ModelException;
import ru.masterdm.crs.exception.integration.IntegrationException;
import ru.masterdm.crs.exception.meta.RemoveReferencedMetaException;

/**
 * Intercepted exceptions.
 * @author Alexey Kirilchev
 * @author Igor Matushak
 */
public enum ExceptionHandlerClassifier {
    /** Wrong value UI exception. */
    WRONG_VALUE_EXCEPTION(WrongValueException.class, new ExpectedExceptionHandler()),
    /** Formula cyclic dependency exception. */
    FORMULA_CYCLIC_DEPENDENCY(FormulaCyclicDependencyException.class, new FormulaCyclicDependencyExceptionHandler()),
    /** Formula exception. */
    FORMULA_EXCEPTION(FormulaException.class, new FormulaExceptionHandler()),
    /** Model exception. */
    MODEL_EXCEPTION(ModelException.class, new ModelExceptionHandler()),
    /** Calculation exception. */
    CALC_EXCEPTION(CalculationException.class, new CalculationExceptionHandler()),
    /** Remove reference meta exception. */
    REMOVE_REFERENCED_META_EXCEPTION(RemoveReferencedMetaException.class, new RemoveReferencedMetaExceptionHandler()),
    /** Too short search string exception. */
    TOO_SHORT_SEARCH_STRING_EXCEPTION(TooShortSearchStringException.class, new TooShortSearchStringExceptionHandler()),
    /** Integration exception. */
    INTEGRATION_EXCEPTION(IntegrationException.class, new IntegrationExceptionHandler());

    private Class<? extends Throwable> clazz;
    private ExceptionHandler handler;

    /**
     * Constructor.
     * @param clazz exception class
     * @param handler exception handler
     */
    ExceptionHandlerClassifier(Class<? extends Throwable> clazz, ExceptionHandler handler) {
        this.clazz = clazz;
        this.handler = handler;
    }

    /**
     * Returns exception class.
     * @return exception class
     */
    public Class getExceptionClass() {
        return clazz;
    }

    /**
     * Returns exception handler.
     * @return exception handler
     */
    public ExceptionHandler getHandler() {
        return handler;
    }

    /**
     * Finds {@link ExceptionHandler} by exception instance.
     * @param exception exception instance
     * @return {@link ExceptionHandlerClassifier}
     */
    public static ExceptionHandler getByException(Throwable exception) {
        ExceptionHandlerClassifier exceptionHandlerClassifier = Stream.of(ExceptionHandlerClassifier.values())
                                                                      .filter(ie -> ie.getExceptionClass().isInstance(exception))
                                                                      .findFirst().orElse(null);
        return exceptionHandlerClassifier != null ? exceptionHandlerClassifier.getHandler() : null;
    }
}
