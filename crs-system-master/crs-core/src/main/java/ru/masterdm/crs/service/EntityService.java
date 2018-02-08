package ru.masterdm.crs.service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.attribute.FileInfoAttribute;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;

/**
 * Data Vault interaction service.
 * @author Sergey Valiev
 */
public interface EntityService {

    /**
     * Create new empty entity with NULLed "in table" and "multilang" attributes.
     * Reference and file attributes should be created by demand.
     * @param entityMeta type of entity to be created
     * @return new instance of entity
     */
    Entity newEmptyEntity(EntityMeta entityMeta);

    /**
     * Create new empty entity by meta key.
     * @param entityMetaKey entity meta key
     * @return new instance of entity
     */
    Entity newEmptyEntity(String entityMetaKey);

    /**
     * Get entity id by its key.
     * @param entityMeta entity metadata
     * @param key entity key
     * @return entity id, null if entity not found
     */
    Long getEntityIdByKey(EntityMeta entityMeta, String key);

    /**
     * Get entity by primary id.
     * @param meta entity metadata
     * @param id entity primary id
     * @param ldts load date. If <code>null</code> returns actual data
     * @return entity object
     */
    Entity getEntity(EntityMeta meta, Long id, LocalDateTime ldts);

    /**
     * Get entity by business key.
     * @param meta entity metadata
     * @param key entity business key
     * @param ldts load date. If <code>null</code> returns actual data
     * @return entity object
     */
    Entity getEntity(EntityMeta meta, String key, LocalDateTime ldts);

    /**
     * Get list of entity confirmed criteria and rows range.
     * @param meta entity metadata
     * @param criteria filter and sort criteria. may be null to get all rows
     * @param rowRange rows range for pagination, mat be null to get all rows
     * @param ldts load date. If <code>null</code> returns actual data
     * @return entity list
     */
    List<? extends Entity> getEntities(EntityMeta meta, Criteria criteria, RowRange rowRange, LocalDateTime ldts);

    /**
     * Get list of entity confirmed criteria and rows range.
     * @param meta entity metadata
     * @param criteria filter and sort criteria. may be null to get all rows
     * @param rowRange rows range for pagination, mat be null to get all rows
     * @param ldts load date. If <code>null</code> returns actual data
     * @param metadataLdts metadata load datetime
     * @return entity list
     */
    List<? extends Entity> getEntities(EntityMeta meta, Criteria criteria, RowRange rowRange, LocalDateTime ldts, LocalDateTime metadataLdts);

    /**
     * Return list of entity with laded intable attributes.
     * @param meta entity metadata
     * @param criteria filter criteria
     * @param rowRange paging object
     * @param ldts load datetime
     * @return list of entity
     */
    List<? extends Entity> getEntitiesBase(EntityMeta meta, Criteria criteria, RowRange rowRange, LocalDateTime ldts);

    /**
     * Load childrens into entity.
     * @param entity parent entity
     * @param ldts load datetime
     */
    void loadEntityChildren(Entity entity, LocalDateTime ldts);

    /**
     * Load children into entity.
     * @param entities list of parent entity
     * @param ldts load datetime
     */
    void loadEntityChildren(List<Entity> entities, LocalDateTime ldts);

    /**
     * Load parent into entity.
     * @param entity list of children entity
     * @param ldts load datetime
     */
    void loadEntityParent(Entity entity, LocalDateTime ldts);

    /**
     * Load parent into entity.
     * @param entities list of children entity
     * @param ldts load datetime
     */
    void loadEntityParent(List<Entity> entities, LocalDateTime ldts);

    /**
     * Save changes at entity.
     * For new entity generate IDs, and if key is null set it from hub id.
     * @param entity entity
     */
    void persistEntity(Entity entity);

    /**
     * Save changes at entity for pointed moment.
     * @param entity entity object
     * @param ldts load datetime
     */
    void persistEntity(Entity entity, LocalDateTime ldts);

    /**
     * Save changes at entity in single transaction.
     * @param entities saved entities
     */
    void persistEntityConsistent(Entity... entities);

    /**
     * Save changes at intable attributes.
     * @param entity entity object
     * @param ldts load datetime
     */
    void persistEntityBase(Entity entity, LocalDateTime ldts);

    /**
     * Remove entity.
     * @param entity entity
     */
    void removeEntity(Entity entity);

    /**
     * remove entity at pointed moment.
     * @param entity entity object
     * @param ldts load datetime
     */
    void removeEntity(Entity entity, LocalDateTime ldts);

    /**
     * Returns file content.
     * @param fileInfoAttribute file info attribute
     * @param ldts load date. If <code>null</code> returns actual data
     * @return file content
     */
    InputStream getFileContent(FileInfoAttribute fileInfoAttribute, LocalDateTime ldts);
}
