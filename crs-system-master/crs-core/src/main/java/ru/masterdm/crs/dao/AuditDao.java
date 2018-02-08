package ru.masterdm.crs.dao;

import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import ru.masterdm.crs.dao.dto.AuditDto;
import ru.masterdm.crs.domain.AuditLog;
import ru.masterdm.crs.domain.entity.criteria.RowRange;

/**
 * Audit changes log.
 * @author Kuzmin Mikhail
 */
public interface AuditDao {

    /**
     * Returns list of audit logs by filter and row range.
     * @param filter filter map
     * @param rowRange range of rows should be returned, may be null to extract full result
     * @return list of audit log
     */
    AuditDto getLogs(@Param("filter") Map<String, Object> filter, @Param("rowRange") RowRange rowRange);

    /**
     * Writes log.
     * @param auditLog audit log object
     */
    @Transactional
    void writeLog(@Param("auditLog") AuditLog auditLog);
}
