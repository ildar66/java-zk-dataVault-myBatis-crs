package ru.masterdm.crs.dao.entity.meta;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import ru.masterdm.crs.dao.entity.meta.dto.AttributeAttributesDto;
import ru.masterdm.crs.dao.entity.meta.dto.EntityAttributeMetaDto;
import ru.masterdm.crs.dao.entity.meta.dto.EntityMetaDto;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;

/**
 * Metadata DAO.
 * @author Sergey Valiev
 */
public interface MetadataDao {

    /**
     * Executes arbitrary SQL code.
     * @param sql SQL string
     */
    void execute(@Param("sql") String sql);

    /**
     * Returns database server SYSTIMESTAMP.
     * @return systimestamp
     */
    LocalDateTime getSysTimestamp();

    /**
     * Get entity metadata key by hub id.
     * @param id hub id
     * @return metadata key
     */
    String getEntityKeyById(@Param("id") Long id);

    /**
     * Returns entity meta hub id by meta key.
     * @param key entity meta key
     * @return hub id
     */
    Long getEntityHubIdByKey(@Param("key") String key);

    /**
     * Returns entity meta group hub id by meta key.
     * @param key entity meta group key
     * @return hub id
     */
    Long getEntityMetaGroupHubIdByKey(@Param("key") String key);

    /**
     * Returns attribute meta hub id by meta key.
     * @param key attribute meta key
     * @return hub id
     */
    Long getAttributeHubIdByKey(@Param("key") String key);

    /**
     * Persist entity metadata.
     * @param entityMeta entity metadata
     */
    @Transactional
    void persistEntity(@Param("entityMeta") EntityMeta entityMeta);

    /**
     * Remove entity metadata.
     * @param entityId entity id (satellite id)
     */
    @Transactional
    void removeEntity(@Param("entityId") Long entityId);

    /**
     * Returns entity metadata.
     * This is low level procedure for reading metadata for engine usage.
     * This procedure don't use metadata itself.
     * @param entityKey entity key
     * @param ldts load date
     * @return entity metadata
     */
    EntityMeta getEntityByKey(@Param("entityKey") String entityKey, @Param("ldts") LocalDateTime ldts);

    /**
     * Read attribute attributes metadata.
     * @param attributeIds attribute m etadata id.
     * @param ldts load date time
     * @return map of attributes
     */
    @MapKey("attributeId")
    Map<Long, AttributeAttributesDto> getRefAttributeAttributes(@Param("attributeIds") Collection<Long> attributeIds,
                                                                @Param("ldts") LocalDateTime ldts);

    /**
     * Light way metadata reader.
     * Returns only entities metadata without any attributes.
     * @param criteria filter and sort criteria. may be null to get all rows
     * @param rowRange rows range for pagination, mat be null to get all rows
     * @param ldts load date
     * @param types entity types
     * @return entities metadata
     */
    //TODO remove types param. Use criteria instead
    EntityMetaDto getEntities(@Param("criteria") Criteria criteria, @Param("rowRange") RowRange rowRange,
                              @Param("ldts") LocalDateTime ldts, @Param("types") EntityType... types);

    /**
     * Get attribute metadata for entity metadatas.
     * @param entityIds entity metadata primary ids
     * @param entityIdsAndLdts entity metadata primary ids and ldts pairs
     * @param ldts loads datetime
     * @return DTO for attribute metadatas
     */
    @MapKey("entityId")
    Map<Long, EntityAttributeMetaDto> getAttributeMeta(@Param("entityIds") Collection<Long> entityIds,
                                                       @Param("entityIdsAndLdts") Collection<Pair<Long, LocalDateTime>> entityIdsAndLdts,
                                                       @Param("ldts") LocalDateTime ldts);

    /**
     * Get entity type ids mapped to keys.
     * @return entity type ids mapped to keys
     */
    @MapKey("left")
    Map<Long, Pair<Long, String>> getEntityTypeKeyById();

    /**
     * Entity type id by key.
     * @param key entity type key
     * @return entity type id
     */
    Long getEntityTypeIdByKey(@Param("key") String key);

    /**
     * Get list of entity hub ids that reffereed to entity meta pointed by key.
     * @param key entity meta key
     * @param ldts load datetime
     * @return list of hub ids
     */
    List<Long> getReferencedByHubIds(@Param("key") String key, @Param("ldts") LocalDateTime ldts);

    /**
     * Read time slices for metakey.
     * @param entityKey meta key
     * @return time slices ordered from new to older
     */
    List<LocalDateTime> getEntityTimeSlices(@Param("entityKey") String entityKey);

    /**
     * Get business actions hub ids allowed for meta.
     * @param entityMeta entity meta
     * @param ldts load datetime
     * @return list if hub ids
     */
    List<Pair<Long, Boolean>> getEntityMetaActionsIds(@Param("entityMeta") EntityMeta entityMeta, @Param("ldts") LocalDateTime ldts);
}
