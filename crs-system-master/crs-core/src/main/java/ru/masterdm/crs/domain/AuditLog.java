package ru.masterdm.crs.domain;

import java.time.LocalDateTime;

import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.User;

/**
 * Audit log domain object.
 * @author Kuzmin Mikhail
 */
public class AuditLog implements AbstractEntity<Long> {

    private Long id;
    private Entity entity;
    private User executor;
    private AuditAction action;
    private LocalDateTime recordTimestamp;

    /**
     * Audit log filter enumeration.
     * @author Alexey Chalov
     */
    public enum AuditLogFilter {
        ACTION, DATE_FROM, DATE_TO, AUTHOR, OBJECT
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns entity.
     * @return entity
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Sets entity.
     * @param entity entity
     */
    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    /**
     * Returns executor.
     * @return executor
     */
    public User getExecutor() {
        return executor;
    }

    /**
     * Sets executor.
     * @param executor executor
     */
    public void setExecutor(User executor) {
        this.executor = executor;
    }

    /**
     * Returns audit action.
     * @return audit action
     */
    public AuditAction getAction() {
        return action;
    }

    /**
     * Sets audit action.
     * @param action Audit action
     */
    public void setAction(AuditAction action) {
        this.action = action;
    }

    /**
     * Returns audit log write time.
     * @return audit log write time
     */
    public LocalDateTime getRecordTimestamp() {
        return recordTimestamp;
    }

    /**
     * Sets audit log write time.
     * @param recordTimestamp audit log write time
     */
    public void setRecordTimestamp(LocalDateTime recordTimestamp) {
        this.recordTimestamp = recordTimestamp;
    }
}
