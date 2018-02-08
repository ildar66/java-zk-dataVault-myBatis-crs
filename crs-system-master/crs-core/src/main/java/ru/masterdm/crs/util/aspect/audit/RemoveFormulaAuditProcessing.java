package ru.masterdm.crs.util.aspect.audit;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ru.masterdm.crs.domain.AuditAction;
import ru.masterdm.crs.domain.calc.Formula;

/**
 * Custom remove {@link Formula} audit processor.
 * @author Kamordin Alexander
 */
@Component("removeFormulaAudit")
public class RemoveFormulaAuditProcessing extends AbstractAuditProcessing {

    private static final Logger LOG = LoggerFactory.getLogger(RemoveFormulaAuditProcessing.class);

    @Override
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed(joinPoint.getArgs());
            Arrays.stream(joinPoint.getArgs())
                  .filter(arg -> (arg instanceof Formula))
                  .forEach(arg -> writeLog((Formula) arg, AuditAction.REMOVE_FORMULA));
            return result;
        } catch (Throwable e) {
            LOG.error("Error caught in delete entity meta audit processor", e);
            throw e;
        }
    }
}

