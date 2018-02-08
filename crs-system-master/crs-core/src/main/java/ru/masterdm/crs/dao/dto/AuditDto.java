package ru.masterdm.crs.dao.dto;

import java.util.List;

import ru.masterdm.crs.domain.AuditLog;

/**
 * Data transfer object to get "total count" with list of audit logs.
 * @author Kuzmin Mikhail
 */
public class AuditDto {

    private Long cc;
    private List<AuditLog> auditLogs;

    /**
     * Returns "count" data.
     * @return "count" data
     */
    public Long getCc() {
        return cc;
    }

    /**
     * Sets "count" data.
     * @param cc "count" data
     */
    public void setCc(Long cc) {
        this.cc = cc;
    }

    /**
     * Returns list of audit logs.
     * @return list of entity meta
     */
    public List<AuditLog> getAuditLogs() {
        return auditLogs;
    }

    /**
     * Sets list of audit logs.
     * @param auditLogs list of audit logs
     */
    public void setAuditLogs(List<AuditLog> auditLogs) {
        this.auditLogs = auditLogs;
    }
}
