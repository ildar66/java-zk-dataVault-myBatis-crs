package ru.masterdm.crs.util.aspect.audit;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ru.masterdm.crs.domain.AuditAction;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;

/**
 * Custom create/edit {@link EntityMeta} audit processor.
 * @author Kuzmin Mikhail
 * @author Alexey Chalov
 */
@Component("persistEntityMetaAudit")
public class PersistEntityMetaAuditProcessing extends AbstractAuditProcessing {

    private static final Logger LOG = LoggerFactory.getLogger(PersistEntityMetaAuditProcessing.class);

    @Override
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            List<Object> createCandidates =
                    Arrays.stream(joinPoint.getArgs())
                          .filter(arg -> ((arg instanceof EntityMeta) && ((EntityMeta) arg).getId() == null))
                          .collect(Collectors.toList());
            List<Object> editCandidates =
                    Arrays.stream(joinPoint.getArgs())
                          .filter(arg -> ((arg instanceof EntityMeta) && ((EntityMeta) arg).getId() != null))
                          .collect(Collectors.toList());

            Object result = joinPoint.proceed(joinPoint.getArgs());

            createCandidates.forEach(arg -> writeLog((EntityMeta) arg, AuditAction.CREATE_ENTITY_META));
            editCandidates.forEach(arg -> writeLog((EntityMeta) arg, AuditAction.EDIT_ENTITY_META));

            return result;
        } catch (Throwable e) {
            LOG.error("Error caught in persist entity meta audit processor", e);
            throw e;
        }
    }
}
