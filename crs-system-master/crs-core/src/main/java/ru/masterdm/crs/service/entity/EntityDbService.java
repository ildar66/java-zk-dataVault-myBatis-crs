package ru.masterdm.crs.service.entity;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;

import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.attribute.EntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.FileInfoAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.MultilangAttribute;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;

/**
 * Provide read write operation for all entity data.
 * @author Pavel Masalov.
 */
public interface EntityDbService {

    /**
     * Get entity id by its key.
     * @param entityMeta entity metadata
     * @param key entity key
     * @return entity id, null if entity not found
     */
    Long getEntityIdByKey(EntityMeta entityMeta, String key);

    /**
     * Load entity data from hub and satellite by criteria and paging.
     * @param entityMeta entity metadata
     * @param criteria filer and sort criteria
     * @param rowRange rows range
     * @param ldts load datetime
     * @return list of mapped entity data, empty list if no data found
     */
    List<Map<String, Object>> readEntityData(EntityMeta entityMeta, Criteria criteria, RowRange rowRange, LocalDateTime ldts);

    /**
     * Read {@link MultilangAttribute multilang attributes} into entities.
     * @param entities list of entities
     * @param ldts load date time
     */
    void readMultilangAttribute(List<Entity> entities, LocalDateTime ldts);

    /**
     * Read {@link ru.masterdm.crs.domain.entity.attribute.FileInfoAttribute file attributes} into entities.
     * @param entities list of entities
     * @param ldts load date time
     */
    void readFileInfoAttributes(List<Entity> entities, LocalDateTime ldts);

    /**
     * Read {@link ru.masterdm.crs.domain.entity.attribute.EntityAttribute entity attributes} into entities.
     * @param entities list of entities
     * @param ldts load date time
     * @param useEntityLdts use entities id and ldts pair (instead of id only)
     * @param addFilter filter reference attribute that should be loaded. id null then all attributes load
     */
    void readEntityAttributes(List<? extends Entity> entities, LocalDateTime ldts, boolean useEntityLdts, Predicate<AttributeMeta> addFilter);

    /**
     * Read link attribute data from satellite.
     * @param entityMeta entiry metadata that own the reference attribute
     * @param attributeMeta reference attribute
     * @param linkIds collection of links primary id load
     * @param linkIdAndLdts collection of link ids and entity load datetime
     * @param ldts load datetime
     * @return list of satellite data value map
     */
    List<Map<String, Object>> readLinkAttributeData(EntityMeta entityMeta, AttributeMeta attributeMeta, Collection<Long> linkIds,
                                                    Collection<Pair<Long, LocalDateTime>> linkIdAndLdts, LocalDateTime ldts);

    /**
     * Read hubIds from reference attribute by single child entity.
     * @param childEntity child entity object
     * @param parentReferenceMetadata reference attribute of parent that
     * @param parentMetadata parent metadata
     * @param ldts load date time
     * @return list of parent hub ids
     */
    List<Long> readEntityAttributeBackLink(Entity childEntity, AttributeMeta parentReferenceMetadata, EntityMeta parentMetadata, LocalDateTime ldts);

    /**
     * Write new multilang attribute data to tables.
     * @param entity attribute entity
     * @param attribute multilang attribute
     * @param ldts load datelime
     */
    void writeMultilangNew(Entity entity, MultilangAttribute attribute, LocalDateTime ldts);

    /**
     * Write new version of multilang attribute value.
     * @param multilangAttribute multilang attribute
     * @param ldts load datetime
     */
    void writeMultilangChange(MultilangAttribute multilangAttribute, LocalDateTime ldts);

    /**
     * Write new multilang file info data to tables.
     * @param entity attribute entity
     * @param attribute file info attribute
     * @param ldts load datelime
     */
    void writeFileInfoNew(Entity entity, FileInfoAttribute attribute, LocalDateTime ldts);

    /**
     * Write new version of file info attribute value.
     * @param entity attribute entity
     * @param attribute file info attribute
     * @param ldts load datetime
     */
    void writeFileInfoChange(Entity entity, FileInfoAttribute attribute, LocalDateTime ldts);

    /**
     * Write new reference links or update existed.
     * @param entity entiry object
     * @param linkedEntityAttribute link entity attribute
     * @param ldts load datetime
     */
    void writeReferenceNewAndChange(Entity entity, LinkedEntityAttribute linkedEntityAttribute, LocalDateTime ldts);

    /**
     * Write reference attribute satellite data.
     * @param entity entiry that own reference attribute
     * @param entityAttribute reference attribute
     * @param ldts load datetime
     */
    void writeReferenceLinkSatellite(Entity entity, EntityAttribute entityAttribute, LocalDateTime ldts);

    /**
     * Write records to entity hub and satellite table.
     * Entity object's LDTS will be refreshed.
     * @param entity entity object
     * @param ldts load datetime
     */
    void writeEntityNew(Entity entity, LocalDateTime ldts);

    /**
     * Write new version to entity satellite table.
     * Entity object's LDTS will be refreshed.
     * @param entity entity object
     * @param ldts load datetime
     */
    void writeEntityChange(Entity entity, LocalDateTime ldts);

    /**
     * Write remove entity record.
     * @param entity entity object
     * @param ldts load datetime
     */
    void writeRemoveEntity(Entity entity, LocalDateTime ldts);

    /**
     * Trying to set remove flag for all links instance for entity and its attribute.
     * @param entity entity with linked attribute
     * @param attributeMeta link based attribute
     * @param ldts load datetime
     */
    void writeRemoveLinkAll(Entity entity, AttributeMeta attributeMeta, LocalDateTime ldts);

    /**
     * Merge entities links attributes into database.
     * @param entity parent entity
     * @param attributes links for reference attribute
     * @param referenceAttributeMeta attribute metadata
     * @param ldts load datetime
     */
    void mergeLink(Entity entity, List<? extends LinkAttribute> attributes, AttributeMeta referenceAttributeMeta, LocalDateTime ldts);

    /**
     * Returns file content.
     * @param fileInfoAttribute file info attribute
     * @param ldts load datetime
     * @return file content
     */
    InputStream getFileContent(FileInfoAttribute fileInfoAttribute, LocalDateTime ldts);
}
