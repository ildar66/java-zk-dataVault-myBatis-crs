package ru.masterdm.crs.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import ru.masterdm.crs.domain.AuditAction;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.util.annotation.Audit;
import ru.masterdm.crs.util.annotation.CurrentTimeStamp;

/**
 * Blank service implementation for Audit aspect test.
 * @author Kuzmin Mikhail
 */
@Validated
@Service("auditTestService")
public class AuditTestService {

    /**
     * Method for default audit processor test (ex: EVAL_CALCULATION).
     * @param entity {@link Entity} instance
     */
    @Transactional
    @Audit(action = AuditAction.EVAL_CALCULATION)
    public void defaultEvalCalculation(Entity entity) {
    }

    /**
     * Method for custom audit processor test (CREATE_CALCULATION).
     * @param entity {@link Entity} instance
     * @param entityId entity identifier to be set
     */
    @Transactional
    @Audit(action = AuditAction.CREATE_CALCULATION)
    public void customCreateCalculation(Entity entity, Long entityId) {
        entity.setId(entityId);
    }

    /**
     * Method for custom audit processor test (DELETE_ENTITY_META).
     * @param entityMeta {@link EntityMeta} instance
     */
    @Transactional
    @Audit(action = AuditAction.DELETE_ENTITY_META)
    public void customDeleteEntityMeta(EntityMeta entityMeta) {
    }

    /**
     * Method for custom audit processor test (CREATE_ENTITY_META).
     * @param entityMeta {@link EntityMeta} instance
     * @param entityMetaId entity meta identifier to be set
     */
    @Transactional
    @Audit(action = AuditAction.CREATE_ENTITY_META)
    public void customCreateEntityMeta(EntityMeta entityMeta, Long entityMetaId) {
        entityMeta.setId(entityMetaId);
    }

    /**
     * Method for {@link Audit} annotation in conjunction with {@link CurrentTimeStamp} one.
     * @param ts {@link LocalDateTime} instance
     * @return {@link LocalDateTime} instance
     */
    @Audit(action = AuditAction.EVAL_CALCULATION)
    public LocalDateTime auditWithCurrentTimestamp(@CurrentTimeStamp LocalDateTime ts) {
        return ts;
    }

    /**
     * Method for transaction rollback testing.
     */
    @Audit(action = AuditAction.EVAL_CALCULATION)
    public void transactionRollback() {
        throw new CrsException("");
    }
}
