package ru.masterdm.crs.util.aspect.audit;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import ru.masterdm.crs.domain.AuditAction;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;

/**
 * Custom delete {@link EntityMeta} audit processor.
 * @author Kuzmin Mikhail
 * @author Alexey Chalov
 */
@Component("deleteEntityMetaAudit")
public class DeleteEntityMetaAuditProcessing extends AbstractAuditProcessing {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteEntityMetaAuditProcessing.class);

    @Override
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed(joinPoint.getArgs());
            Arrays.stream(joinPoint.getArgs())
                  .filter(arg -> (arg instanceof EntityMeta))
                  .forEach(arg -> writeLog((EntityMeta) arg, AuditAction.DELETE_ENTITY_META));
            return result;
        } catch (Throwable e) {
            LOG.error("Error caught in delete entity meta audit processor", e);
            throw e;
        }
    }
}
