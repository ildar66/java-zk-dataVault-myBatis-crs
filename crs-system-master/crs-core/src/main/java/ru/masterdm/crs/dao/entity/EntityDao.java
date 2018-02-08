
package ru.masterdm.crs.dao.entity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import ru.masterdm.crs.dao.entity.dto.EntityAttributeDto;
import ru.masterdm.crs.dao.entity.dto.FileInfoAttributeDto;
import ru.masterdm.crs.dao.entity.dto.MultilangAttributeDto;
import ru.masterdm.crs.domain.entity.AbstractDvEntity;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.attribute.EntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.FileInfoAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkAttribute;
import ru.masterdm.crs.domain.entity.attribute.MultilangAttribute;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.meta.DsqlNames;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;

/**
 * Data access object for entity data.
 * @author Pavel Masalov
 */
public interface EntityDao {

    /**
     * Get entity id by its key.
     * @param hubTableName entity table name
     * @param key entity key
     * @return entity id, null if entity not found
     */
    Long getEntityIdByKey(@Param("hubTableName") String hubTableName, @Param("key") String key);

    /**
     * Select Entity data stored at hub and satellite tables.
     * @param entityMeta entity metadata
     * @param hubTableName hub table name
     * @param satelliteTableName satellite table name
     * @param ldts load datetime
     * @param criteria query criteria
     * @param rowRange range of rows should be returned, may be null to extract full result
     * @return list of mapped entity data
     */
    List<Map<String, Object>> selectEntity(@Param("entityMeta") EntityMeta entityMeta, @Param("hubTableName") String hubTableName,
                                           @Param("satelliteTableName") String satelliteTableName, @Param("ldts") LocalDateTime ldts,
                                           @Param("criteria") Criteria criteria,
                                           @Param("rowRange") RowRange rowRange);

    /**
     * Insert entity hub row.
     * @param entity entity object
     * @param hubTableName entity hub table name
     * @param hubSequenceName entity hub sequence name
     */
    @Transactional
    void insertEntityHub(@Param("entity") Entity entity, @Param("hubTableName") String hubTableName,
                         @Param("hubSequenceName") String hubSequenceName);

    /**
     * Insert entity satellite table row.
     * @param entity entity object
     * @param satelliteTableName entity satellite table name
     * @param satelliteSequenceName entiry satellite sequence name
     */
    @Transactional
    void insertEntitySatellite(@Param("entity") Entity entity, @Param("satelliteTableName") String satelliteTableName,
                               @Param("satelliteSequenceName") String satelliteSequenceName);

    /**
     * Insert new record to satellite with remove=1.
     * @param dsqlNames DSQL namse
     * @param entity entity object
     * @param ldts load datetime
     */
    @Transactional
    void insertRemoveEntity(@Param("dsqlName") DsqlNames dsqlNames, @Param("entity") Entity entity, @Param("ldts") LocalDateTime ldts);

    /**
     * Select map of {@link MultilangAttributeDto} mapped by hubId.
     * DTO store real {@link MultilangAttribute} values.
     * @param dsqlNames names of parts of DSQL
     * @param entities list of master entities
     * @param ldts load datetime
     * @return map of attributes
     */
    @MapKey("mainHubId")
    Map<Long, MultilangAttributeDto> selectMultilangAttributeBulk(@Param("dsqlNames") List<DsqlNames> dsqlNames,
                                                                  @Param("entities") List<Entity> entities,
                                                                  @Param("ldts") LocalDateTime ldts);

    /**
     * Select map of {@link FileInfoAttributeDto} mapped by hubId.
     * DTO store real {@link FileInfoAttribute} values.
     * @param dsqlNames names of parts of DSQL
     * @param entities list of master entities
     * @param ldts load datetime
     * @return map of attributes
     */
    @MapKey("mainHubId")
    Map<Long, FileInfoAttributeDto> selectFileInfoAttributeBulk(@Param("dsqlNames") List<DsqlNames> dsqlNames,
                                                                @Param("entities") List<Entity> entities,
                                                                @Param("ldts") LocalDateTime ldts);

    /**
     * Select map of {@link EntityAttributeDto} mapped by hubId.
     * DTO store real {@link EntityAttribute} values.
     * @param dsqlNames names of parts of DSQL
     * @param entities list of master entities
     * @param ldts load datetime
     * @param useEntityLdts use entities id and ldts pair (instead of id only)
     * @return map of attributes
     */
    @MapKey("mainHubId")
    Map<Long, EntityAttributeDto> selectEntityAttributeBulk(@Param("dsqlNames") List<DsqlNames> dsqlNames,
                                                            @Param("entities") List<? extends AbstractDvEntity> entities,
                                                            @Param("ldts") LocalDateTime ldts, @Param("useEntityLdts") boolean useEntityLdts);

    /**
     * Select link attribute's attributes.
     * @param dsqlNames DSQL names
     * @param linkIds collection of links primary id load
     * @param ldts load datetime
     * @return list of satellite data value map
     */
    List<Map<String, Object>> selectAttributeAttributes(@Param("dsqlNames") DsqlNames dsqlNames, @Param("linkIds") List<Long> linkIds,
                                                        @Param("ldts") LocalDateTime ldts);

    /**
     * Insert new record into entity link satellite.
     * @param dsqlNames DSQL names
     * @param entityAttribute entiry attribute
     */
    @Transactional
    void insertAttributeSatellite(@Param("dsqlNames") DsqlNames dsqlNames, @Param("entityAttribute") EntityAttribute entityAttribute);

    /**
     * Insert remove record into link satellite.
     * @param dsqlNames DSQL names
     * @param linkIds collection of links primary id to remove
     * @param ldts load datetime
     */
    @Transactional
    void removeAttributeSatellite(@Param("dsqlNames") DsqlNames dsqlNames, @Param("linkIds") List<Long> linkIds,
                                  @Param("ldts") LocalDateTime ldts);

    /**
     * Execute insert select for link table.
     * Used to clone last link record for specified pair of hub ids.
     * link.id not returned.
     * @param dsqlNames dsqlNames of parts of DSQL
     * @param ldts load datetime
     * @param remove removed flag be inserted to cloned row
     * @param parentHubId main entity hub id
     * @param childHubId child hub id. May be null. then only parent hub id used
     */
    void insertLinkFromExistedByHubPair(@Param("dsqlNames") DsqlNames dsqlNames,
                                        @Param("parentHubId") Long parentHubId, @Param("childHubId") Long childHubId, @Param("remove") boolean remove,
                                        @Param("ldts") LocalDateTime ldts);

    /**
     * Execute insert if hub's ids pair are not found in same remove state as at attribute.
     * @param attribute link attribute to check and insert
     * @param dsqlNames DSQL namse
     * @param ldts load datetime
     */
    void insertLinkHasChangedHubIds(@Param("attribute") LinkAttribute attribute, @Param("dsqlNames") DsqlNames dsqlNames,
                                    @Param("ldts") LocalDateTime ldts);

    /**
     * Insert new link record if it changed.
     * Attribute contains new id if new record inserted.
     * Then linkId and linkLdts contains null if no new record inserted.
     * @param attribute link attribute
     * @param dsqlNames dsqlNames of parts of DSQL
     * @param ldts load datetime
     */
    void insertLinkHasChangedLinkId(@Param("attribute") LinkAttribute attribute, @Param("dsqlNames") DsqlNames dsqlNames,
                                    @Param("ldts") LocalDateTime ldts);

    /**
     * Insert remove record for all link existed at table but not really defined.
     * @param entity entiry object
     * @param references collection of references existed
     * @param dsqlNames dsqlNames of parts of DSQL
     * @param ldts load datetime
     */
    void insertLinkRemoveExclusion(@Param("entity") Entity entity, @Param("references") Collection<? extends LinkAttribute> references,
                                   @Param("dsqlNames") DsqlNames dsqlNames,
                                   @Param("ldts") LocalDateTime ldts);

    /**
     * Insert new record at link table.
     * @param attribute attribute linked by this table
     * @param dsqlNames dsqlNames of parts of DSQL
     */
    void insertLink(@Param("attribute") LinkAttribute attribute, @Param("dsqlNames") DsqlNames dsqlNames);

    /**
     * Insert multilang hub row.
     * @param attribute multilang attribute
     */
    void insertMultilangHub(@Param("attribute") MultilangAttribute attribute);

    /**
     * Insert file info hub row.
     * @param attribute file info attribute
     */
    void insertFileInfoHub(@Param("attribute") FileInfoAttribute attribute);

    /**
     * Insert multilang satellite row for string type multilang.
     * @param attribute multilang attribute
     */
    void insertMultilangSatelliteString(@Param("attribute") MultilangAttribute attribute);

    /**
     * Insert multilang satellite row for text type multilang.
     * @param attribute multilang attribute
     */
    void insertMultilangSatelliteText(@Param("attribute") MultilangAttribute attribute);

    /**
     * Insert file info satellite row.
     * @param attribute file info attribute
     */
    void insertFileInfoSatellite(@Param("attribute") FileInfoAttribute attribute);

    /**
     * Insert file info content satellite row.
     * @param attribute file info attribute
     */
    void insertFileInfoContentSatellite(@Param("attribute") FileInfoAttribute attribute);

    /**
     * Returns map with blob content.
     * @param fileInfoAttribute file info attribute
     * @param ldts load datetime
     * @return map with blob content
     */
    Map getFileContent(@Param("fileInfoAttribute") FileInfoAttribute fileInfoAttribute, @Param("ldts") LocalDateTime ldts);
}
