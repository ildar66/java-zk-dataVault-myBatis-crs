package ru.masterdm.crs.integration.client.portal.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.annotations.Param;

import ru.masterdm.crs.domain.integration.CpiDepartment;

/**
 * Client portal operations dao.
 * @author Alexey Chalov
 */
public interface ClientPortalIntegrationDao {

    /**
     * Returns cron expression for scheduling.
     * @return cron expression
     */
    String getCron();

    /**
     * Starts data synchronization process.
     */
    void startSynchronization();

    /**
     * Returns latest synchronization date.
     * @return latest synchronization date
     */
    LocalDateTime getLatestSyncDate();

    /**
     * Returns list of departments, found by name pattern and locale or department identifier list.
     * If name pattern is no set full list of departments will return neglecting locale parameter.
     * @param namePattern name pattern
     * @param locale locale
     * @param departmentIds department identifier list
     * @return list of department
     */
    List<CpiDepartment> getDepartments(@Param("namePattern") String namePattern, @Param("locale") String locale,
                                       @Param("departmentIds") List<Long> departmentIds);

    /**
     * Returns CRS departments to client portal ones mappings.
     * @return list of pairs where left is CRS department identifier and right is client portal department identifier
     */
    List<Pair<Long, Long>> getDepartmentMappings();

    /**
     * Persists CRS department to client portal one mappings.
     * @param crsDepartmentId CRS department identifier
     * @param cpiDepartmentIds list of client portal department identifiers
     */
    void persistDepartmentMappings(@Param("crsDepartmentId") Long crsDepartmentId, @Param("cpiDepartmentIds") List<Long> cpiDepartmentIds);
}
