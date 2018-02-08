package ru.masterdm.crs.service;

import java.time.LocalDateTime;
import java.util.List;

import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMetaGroup;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.service.calc.EntityMetaPrototypeFactory;

/**
 * Metadata operations.
 * @author Pavel Masalov
 */
public interface EntityMetaService {

    /**
     * Returns database server SYSTIMESTAMP.
     * @return systimestamp
     */
    LocalDateTime getSysTimestamp();

    /**
     * Returns entity existence flag.
     * @param key entity meta key
     * @return <code>true</code> if entity already exists
     */
    boolean isEntityMetaExists(String key);

    /**
     * Returns entity meta group existence flag.
     * @param key entity meta group key
     * @return <code>true</code> if entity meta group already exists
     */
    boolean isEntityMetaGroupExists(String key);

    /**
     * Returns attribute existence flag.
     * @param key attribute meta key
     * @return <code>true</code> if attribute already exists
     */
    boolean isAttributeMetaExists(String key);

    /**
     * Persist entity metadata.
     * @param entityMeta entity metadata
     */
    void persistEntityMeta(EntityMeta entityMeta);

    /**
     * Persist entity metadata and add it to group.
     * @param entityMeta entity metadata
     * @param entityMetaGroupKey group key
     */
    void persistEntityMeta(EntityMeta entityMeta, String entityMetaGroupKey);

    /**
     * Save consistent entity meta list.
     * @param entityMetas entity meta list
     */
    void persistEntityMetas(List<EntityMeta> entityMetas);

    /**
     * Remove entity metadata.
     * @param entityMeta entity metadata
     */
    void removeEntityMeta(EntityMeta entityMeta);

    /**
     * Get list of entity metas that reference pointed one.
     * @param entityMeta entity meta to check
     * @param ldts load datetime
     * @return list of entity metas
     */
    List<EntityMeta> getReferencedBy(EntityMeta entityMeta, LocalDateTime ldts);

    /**
     * Returns entity metadata.
     * Low level meta get function. Dont required ENTITY metadata to work.
     * Do not use cache.
     * @param entityMetaKey entity key
     * @param ldts load date. If <code>null</code> returns actual data
     * @return entity metadata
     */
    EntityMeta getEntityMetaByKeyNoCache(String entityMetaKey, LocalDateTime ldts);

    /**
     * Returns entity metadata.
     * Low level meta get function. Dont required ENTITY metadata to work.
     * Use cache.
     * @param entityMetaKey entity key
     * @param ldts load date. If <code>null</code> returns actual data
     * @return entity metadata
     */
    EntityMeta getEntityMetaByKey(String entityMetaKey, LocalDateTime ldts);

    /**
     * Get time slices for entityMetaKey.
     * @param entityMetaKey meta key
     * @return time slices ordered from new to older
     */
    List<LocalDateTime> getEntityMetaTimeSlices(String entityMetaKey);

    /**
     * Get entity metadata by hub id.
     * @param id hub id
     * @param ldts load datetime
     * @return entity metadata
     */
    EntityMeta getEntityMetaById(Long id, LocalDateTime ldts);

    /**
     * Returns only entities metadata without any attributes.
     * @param criteria query criteria
     * @param rowRange range of rows should be returned, may be null to extract full result
     * @param ldts load date. If <code>null</code> returns actual data
     * @param types entity types
     * @return entities metadata
     */
    List<EntityMeta> getEntityMetas(Criteria criteria, RowRange rowRange, LocalDateTime ldts, EntityType... types);

    /**
     * Returns attribute meta key.
     * @param entityMeta entity meta
     * @param partialAttributeKey partual attribute meta key
     * @return attribute meta key
     */
    String getAttributeMetaKey(EntityMeta entityMeta, String partialAttributeKey);

    /**
     * Get metadata prototype creator.
     * @param entityType entity type to create prototype
     * @return prototype factory
     */
    EntityMetaPrototypeFactory getEntityMetaPrototypeFactory(EntityType entityType);

    /**
     * Get list of entity meta groups.
     * @param criteria filter criteria
     * @param rowRange paging object
     * @param ldts load datetime
     * @return entity meta groups
     */
    List<EntityMetaGroup> getEntityMetaGroups(Criteria criteria, RowRange rowRange, LocalDateTime ldts);

    /**
     * Save entity group.
     * @param entityMetaGroup entity group
     */
    void persistEntityMetaGroup(EntityMetaGroup entityMetaGroup);

    /**
     * Remove entity group.
     * @param entityMetaGroup entity group
     */
    void removeEntityMetaGroup(EntityMetaGroup entityMetaGroup);

    /**
     * Save consistent entity group.
     * @param entityMetaGroups groups
     */
    void persistEntityMetaGroups(List<EntityMetaGroup> entityMetaGroups);

    /**
     * Moves entity meta before another entity meta.
     * @param groups groups
     * @param entityMetaToInsertBefore entity meta before which needs to insert
     * @param movingEntityMeta moving entity meta
     * @param entityType entity type
     */
    void moveEntityMetaBeforeAnotherEntityMeta(List<EntityMetaGroup> groups, EntityMeta entityMetaToInsertBefore,
                                               EntityMeta movingEntityMeta, EntityType entityType);

    /**
     * Change group order.
     * @param groups groups
     * @param movingGroup moving group
     * @param groupForInsertBefore group for insert before
     */
    void changeGroupOrder(List<EntityMetaGroup> groups, EntityMetaGroup movingGroup, EntityMetaGroup groupForInsertBefore);

    /**
     * Moves entity meta to group.
     * @param groups groups
     * @param targetGroup entity meta group
     * @param entityMeta entity meta
     */
    void moveEntityMetaToGroup(List<EntityMetaGroup> groups, EntityMetaGroup targetGroup, EntityMeta entityMeta);
}
