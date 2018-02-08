package ru.masterdm.crs.service;

import java.util.List;
import java.util.Map;

import ru.masterdm.crs.domain.AuditLog;
import ru.masterdm.crs.domain.entity.criteria.RowRange;

/**
 * Audit log service interface.
 * @author Kuzmin Mikhail
 */
public interface AuditService {

    /**
     * Get audit logs by filter and row range.
     * @param filter query filter parameters
     * @param rowRange row range
     * @return list of audit logs
     */
    List<AuditLog> getLogs(Map<String, Object> filter, RowRange rowRange);

    /**
     * Writes Audit log to database.
     * @param auditLog {@link AuditLog} instance
     */
    void writeLog(AuditLog auditLog);
}
