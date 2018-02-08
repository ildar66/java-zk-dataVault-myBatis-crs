package ru.masterdm.crs.util.aspect.audit;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ru.masterdm.crs.domain.AuditAction;
import ru.masterdm.crs.domain.calc.Formula;

/**
 * Custom create/edit {@link Formula} audit processor.
 * @author Alexander Kamordin
 */
@Component("persistFormulaAudit")
public class PersistFormulaAuditProcessing extends AbstractAuditProcessing {

    private static final Logger LOG = LoggerFactory.getLogger(PersistFormulaAuditProcessing.class);

    @Override
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            List<Object> createCandidates =
                    Arrays.stream(joinPoint.getArgs())
                          .filter(arg -> ((arg instanceof Formula) && ((Formula) arg).getId() == null))
                          .collect(Collectors.toList());
            List<Object> editCandidates =
                    Arrays.stream(joinPoint.getArgs())
                          .filter(arg -> ((arg instanceof Formula) && ((Formula) arg).getId() != null))
                          .collect(Collectors.toList());

            Object result = joinPoint.proceed(joinPoint.getArgs());

            createCandidates.forEach(arg -> writeLog((Formula) arg, AuditAction.CREATE_FORMULA));
            editCandidates.forEach(arg -> writeLog((Formula) arg, AuditAction.EDIT_FORMULA));

            return result;
        } catch (Throwable e) {
            LOG.error("Error caught in persist model audit processor", e);
            throw e;
        }
    }
}
