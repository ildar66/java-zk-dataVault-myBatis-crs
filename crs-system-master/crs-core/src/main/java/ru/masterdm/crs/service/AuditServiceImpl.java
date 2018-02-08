package ru.masterdm.crs.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import ru.masterdm.crs.dao.AuditDao;
import ru.masterdm.crs.dao.dto.AuditDto;
import ru.masterdm.crs.domain.AuditLog;
import ru.masterdm.crs.domain.entity.criteria.RowRange;

/**
 * Audit log service implementation.
 * @author Kuzmin Mikhail
 */
@Validated
@Service("auditService")
public class AuditServiceImpl implements AuditService {

    @Autowired
    private AuditDao auditDao;

    @Override
    public List<AuditLog> getLogs(Map<String, Object> filter, RowRange rowRange) {
        AuditDto auditDto = auditDao.getLogs(filter, rowRange);
        if (auditDto == null) {
            if (rowRange != null) {
                rowRange.setTotalCount(0L);
            }
            return Collections.emptyList();
        }

        List<AuditLog> auditLogList = auditDto.getAuditLogs();

        // get count or total query rows
        if (rowRange != null) {
            rowRange.setTotalCount(auditDto.getCc());
        }
        return auditLogList;
    }

    @Override
    @Transactional
    public void writeLog(@NotNull AuditLog auditLog) {
        auditDao.writeLog(auditLog);
    }
}
