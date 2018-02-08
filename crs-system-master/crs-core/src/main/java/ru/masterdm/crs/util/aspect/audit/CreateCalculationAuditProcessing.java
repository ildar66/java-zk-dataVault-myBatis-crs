package ru.masterdm.crs.util.aspect.audit;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ru.masterdm.crs.domain.AuditAction;
import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.entity.Entity;

/**
 * Custom create {@link Calculation} audit processor.
 * @author Kuzmin Mikhail
 * @author Alexey Chalov
 */
@Component("createCalculationAudit")
public class CreateCalculationAuditProcessing extends AbstractAuditProcessing {

    private static final Logger LOG = LoggerFactory.getLogger(CreateCalculationAuditProcessing.class);

    @Override
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            List<Object> candidates =
                    Arrays.stream(joinPoint.getArgs())
                          .filter(arg -> ((arg instanceof Entity) && ((Entity) arg).getId() == null))
                          .collect(Collectors.toList());

            Object result = joinPoint.proceed(joinPoint.getArgs());
            candidates.forEach(arg -> writeLog((Entity) arg, AuditAction.CREATE_CALCULATION));
            return result;
        } catch (Throwable e) {
            LOG.error("Error caught in create calculation audit processor", e);
            throw e;
        }
    }
}
