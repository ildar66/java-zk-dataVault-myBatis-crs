package ru.masterdm.crs.util.aspect.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import ru.masterdm.crs.domain.AuditAction;
import ru.masterdm.crs.domain.AuditLog;
import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.service.AuditService;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.SecurityService;

/**
 * Common methods for audit processors.
 * @author Sergey Valiev
 */
public abstract class AbstractAuditProcessing implements AuditProcessing {

    @Autowired
    protected AuditService auditService;
    @Autowired
    protected SecurityService securityService;
    @Autowired
    protected EntityService entityService;
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    private EntityMetaService entityMetaService;

    /**
     * Writes single log.
     * @param entity {@link Entity} instance
     * @param auditAction {@link AuditAction} constant
     */
    protected void writeLog(Entity entity, AuditAction auditAction) {
        AuditLog auditLog = new AuditLog();
        auditLog.setExecutor(securityService.getCurrentUser());
        auditLog.setEntity(entity);
        auditLog.setAction(auditAction);
        auditService.writeLog(auditLog);
    }

    /**
     * Writes single log.
     * @param entityMeta {@link EntityMeta} instance
     * @param auditAction {@link AuditAction} constant
     */
    protected void writeLog(EntityMeta entityMeta, AuditAction auditAction) {
        AuditLog auditLog = new AuditLog();
        auditLog.setExecutor(securityService.getCurrentUser());
        auditLog.setEntity(createEntityFromMeta(entityMeta));
        auditLog.setAction(auditAction);
        auditService.writeLog(auditLog);
    }

    /**
     * Creates {@link Entity} from {@link EntityMeta}.
     * @param entityMeta {@link EntityMeta} instance
     * @return {@link Entity} instance
     */
    private Entity createEntityFromMeta(EntityMeta entityMeta) {
        Entity entity = entityService.newEmptyEntity(entityMeta);
        entity.setId(entityMeta.getId());
        entity.setKey(entityMeta.getKey());
        entity.setLdts(entityMeta.getLdts());
        return entity;
    }

    /**
     * Writes single log.
     * @param formula {@link Formula} instance
     * @param action {@link AuditAction} constant
     */
    protected void writeLog(Formula formula, AuditAction action) {
        AuditLog auditLog = new AuditLog();
        auditLog.setExecutor(securityService.getCurrentUser());
        auditLog.setEntity(createEntityFromFormula(formula));
        auditLog.setAction(action);
        auditService.writeLog(auditLog);
    }

    /**
     * Creates {@link Entity} from {@link Formula}.
     * @param formula {@link Formula} instance
     * @return {@link Entity} instance
     */
    private Entity createEntityFromFormula(Formula formula) {
        EntityMeta formulaMetadata = entityMetaService.getEntityMetaByKey(Formula.METADATA_KEY, formula.getLdts());
        Entity entity = new Entity();
        entity.setMeta(formulaMetadata);
        entity.setId(formula.getId());
        entity.setKey(formula.getKey());
        entity.setLdts(formula.getLdts());
        return entity;
    }
}
