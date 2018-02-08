package ru.masterdm.crs.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import ru.masterdm.crs.domain.entity.Department;
import ru.masterdm.crs.domain.entity.meta.AttributeLocale;
import ru.masterdm.crs.domain.integration.CpiDepartment;
import ru.masterdm.crs.integration.ModuleMetadata;

/**
 * Included sub-modules integration service.
 * @author Alexey Chalov
 */
public interface IntegrationService {

    /**
     * Returns collection of registered integration modules.
     * Actually call is performed through integration route.
     * @return collection of registered integration modules
     */
    Collection<ModuleMetadata> getModules();

    /**
     * Returns true, if integration module available, false otherwise.
     * @param moduleName integration module name
     * @return boolean
     */
    boolean isModuleAvailable(String moduleName);

    /**
     * Returns 'client-portal-integration' module latest synchronization date.
     * @return 'client-portal-integration' module latest synchronization date
     */
    LocalDateTime getCpiLatestSyncDate();

    /**
     * Returns list of departments, found by name pattern and locale.
     * If name pattern is no set full list of departments will return neglecting locale parameter.
     * @param namePattern name pattern
     * @param locale locale
     * @return list of department
     */
    List<CpiDepartment> getCpiDepartments(String namePattern, AttributeLocale locale);

    /**
     * Returns client portal departments to CRS ones mappings.
     * @return list of pairs where left is CRS department identifier and right is client portal department identifier
     */
    List<Pair<Department, List<CpiDepartment>>> getCpiDepartmentMappings();

    /**
     * Persists CRS department to client portal one mappings.
     * @param crsDepartment CRS department
     * @param cpiDepartments list of client portal department
     */
    void persistDepartmentMappings(Department crsDepartment, List<CpiDepartment> cpiDepartments);
}
