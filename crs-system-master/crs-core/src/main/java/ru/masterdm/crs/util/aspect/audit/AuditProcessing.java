package ru.masterdm.crs.util.aspect.audit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Common interface for audit processors.
 * @author Kuzmin Mikhail
 * @author Alexey Chalov
 */
public interface AuditProcessing {

    /**
     * Default log writing method.
     * Calls custom log writers if audit action, decoded from <em>joinPoint</em> parameter equals CUSTOM_DEFINED.
     * @param joinPoint {@link JoinPoint} instance
     * @return method invocation result
     * @throws Throwable if error rise
     */
    Object log(ProceedingJoinPoint joinPoint) throws Throwable;
}
