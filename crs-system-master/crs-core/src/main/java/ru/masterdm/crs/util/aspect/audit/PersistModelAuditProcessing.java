package ru.masterdm.crs.util.aspect.audit;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ru.masterdm.crs.domain.AuditAction;
import ru.masterdm.crs.domain.calc.Model;

/**
 * Custom create/edit {@link Model} audit processor.
 * @author Alexander Kamordin
 */
@Component("persistModelAudit")
public class PersistModelAuditProcessing extends AbstractAuditProcessing {

    private static final Logger LOG = LoggerFactory.getLogger(PersistModelAuditProcessing.class);

    @Override
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            List<Object> createCandidates =
                    Arrays.stream(joinPoint.getArgs())
                          .filter(arg -> ((arg instanceof Model) && ((Model) arg).getId() == null))
                          .collect(Collectors.toList());
            List<Object> editCandidates =
                    Arrays.stream(joinPoint.getArgs())
                          .filter(arg -> ((arg instanceof Model) && ((Model) arg).getId() != null))
                          .collect(Collectors.toList());

            Object result = joinPoint.proceed(joinPoint.getArgs());

            createCandidates.forEach(arg -> writeLog((Model) arg, AuditAction.CREATE_MODEL));
            editCandidates.forEach(arg -> writeLog((Model) arg, AuditAction.EDIT_MODEL));

            return result;
        } catch (Throwable e) {
            LOG.error("Error caught in persist model audit processor", e);
            throw e;
        }
    }
}
