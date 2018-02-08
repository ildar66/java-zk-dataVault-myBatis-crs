package ru.masterdm.crs.util.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ru.masterdm.crs.domain.AuditAction;

/**
 * Audit marker annotation.
 * @author Kuzmin Mikhail
 * @author Alexey Chalov
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {

    /**
     * Returns audit action.
     * @return audit action
     */
    AuditAction action();
}
