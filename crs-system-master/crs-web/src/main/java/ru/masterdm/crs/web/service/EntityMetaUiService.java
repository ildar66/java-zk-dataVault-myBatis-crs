package ru.masterdm.crs.web.service;

import java.util.List;

import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.meta.AttributeLocale;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMetaGroup;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.web.domain.entity.EntityMetaStatus;
import ru.masterdm.crs.web.domain.entity.EntityStatus;

/**
 * Criteria builder service.
 * @author Igor Matushak
 */
public interface EntityMetaUiService {

    /**
     * Prepares entity meta groups.
     * @param entityMetaGroups entity meta groups.
     * @param entityMetaList entity meta list
     * @param entityMetaFilter entity meta filter
     * @param rootEntityMetaStatus root entity meta status
     * @param locale user profile locale
     */
    void prepareEntityMetaGroups(List<EntityMetaGroup> entityMetaGroups, List<EntityMeta> entityMetaList, String entityMetaFilter,
                                 EntityMetaStatus rootEntityMetaStatus, AttributeLocale locale);

    /**
     * Returns is reference valid.
     * @param entityStatus entity status
     * @param attributeMetaKey attribute meta key
     * @return is reference valid
     */
    boolean getReferenceValid(EntityStatus entityStatus, String attributeMetaKey);

    /**
     * Returns permission entity.
     * @param entityType entity type
     * @return permission entity
     */
    Entity getPermissionEntity(EntityType entityType);
}
