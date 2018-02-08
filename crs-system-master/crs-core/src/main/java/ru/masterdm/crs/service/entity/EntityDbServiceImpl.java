package ru.masterdm.crs.service.entity;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.LINK_ID;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.masterdm.crs.dao.entity.EntityDao;
import ru.masterdm.crs.dao.entity.dto.EntityAttributeDto;
import ru.masterdm.crs.dao.entity.dto.EntityAttributeDtoKey;
import ru.masterdm.crs.dao.entity.dto.FileInfoAttributeDto;
import ru.masterdm.crs.dao.entity.dto.FileInfoAttributeDtoKey;
import ru.masterdm.crs.dao.entity.dto.MultilangAttributeDto;
import ru.masterdm.crs.dao.entity.dto.MultilangAttributeDtoKey;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.attribute.EntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.FileInfoAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.MultilangAttribute;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.DsqlNames;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.service.entity.meta.ddl.DsqlNamesService;
import ru.masterdm.crs.util.CollectionUtils;

/**
 * Provide read write operation for all entity data.
 * @author Pavel Masalov.
 */
@Service("entityDbService")
public class EntityDbServiceImpl implements EntityDbService {

    private static final String SELECT_ENTITY_STATEMENT_ID = EntityDao.class.getName() + ".selectEntity";
    private static final String SELECT_ATTRIBUTE_ATTRIBUTES_STATEMENT_ID = EntityDao.class.getName() + ".selectAttributeAttributes";

    private static final Logger LOG = LoggerFactory.getLogger(EntityDbServiceImpl.class);

    private static ReadWriteLock mybatisConfigLock = new ReentrantReadWriteLock();

    @Autowired
    private EntityDao entityDao;
    @Autowired
    @Qualifier("crsSqlSession")
    private SqlSessionTemplate crsSqlSession;
    @Autowired
    private DsqlNamesService dsqlNamesService;

    @Override
    public Long getEntityIdByKey(EntityMeta entityMeta, String key) {
        DsqlNames dsqlNames = dsqlNamesService.getNames(entityMeta);
        return entityDao.getEntityIdByKey(dsqlNames.getHubTableName(), key);
    }

    @Override
    public List<Map<String, Object>> readEntityData(EntityMeta entityMeta, Criteria criteria, RowRange rowRange, LocalDateTime ldts) {
        List<Map<String, Object>> entityData = doReadEntityData(entityMeta, criteria, rowRange, ldts);
        if (entityData == null || entityData.size() == 0) {
            if (rowRange != null) {
                rowRange.setTotalCount(0L);
            }
            return Collections.emptyList();
        }

        // get count or total query rows
        if (rowRange != null && entityData.size() > 0) {
            Map<String, Object> m = entityData.get(0);
            rowRange.setTotalCount((Long) m.get("$cc"));
        }
        return entityData;
    }

    /**
     * Operational method executed just loading.
     * @param entityMeta entity meta
     * @param criteria filer and sort criteria
     * @param rowRange rows range
     * @param ldts load datetime
     * @return list of mapped entity data
     */
    private List<Map<String, Object>> doReadEntityData(EntityMeta entityMeta, Criteria criteria, RowRange rowRange, LocalDateTime ldts) {
        DsqlNames dsqlNames = dsqlNamesService.getNames(entityMeta);

        Map<String, Object> m = new HashMap<>();
        m.put("entityMeta", entityMeta);
        if (criteria != null && criteria.getWhere().isHasMultilangWhere()) {
            List<DsqlNames> multilangNames = criteria.getWhere().getMultilangWheres().keySet().stream()
                                                     .map(a -> dsqlNamesService.getNames(entityMeta, a, ldts))
                                                     .collect(Collectors.toList());
            m.put("multilangNames", multilangNames);
        }
        m.put("hubTableName", dsqlNames.getHubTableName());
        m.put("satelliteTableName", dsqlNames.getSatelliteTableName());
        m.put("ldts", ldts);
        m.put("rowRange", rowRange);
        m.put("criteria", criteria);

        if (criteria != null && !criteria.getReferencedEntities().isEmpty()) {
            Map<Entity, DsqlNames> referencedEntitiesNames = new HashMap();
            for (Entity childEntity : criteria.getReferencedEntities()) {
                String childEntityKey = childEntity.getMeta().getKey();
                AttributeMeta parentReferenceMetadata = entityMeta.getAttributes()
                                                                  .stream()
                                                                  .filter(attr -> attr.getType() == AttributeType.REFERENCE
                                                                                  && childEntityKey.equals(attr.getEntityKey()))
                                                                  .findFirst().orElse(null);
                if (parentReferenceMetadata == null)
                    throw new CrsException("can not find attribute for '" + childEntityKey + "' in '" + entityMeta + "'");
                DsqlNames names = dsqlNamesService.getNames(entityMeta, parentReferenceMetadata, ldts);
                referencedEntitiesNames.put(childEntity, names);
            }
            m.put("referencedEntitiesNames", referencedEntitiesNames);
        }

        // create names for references filter
        if (criteria != null && criteria.getWhere().isHasReferenceWhere()) {
            m.put("referenceNames", createReferenceDsqlNames(entityMeta, criteria, ldts));
        }

        MappedStatement newStatement = getEntityStatement(entityMeta);
        return crsSqlSession.selectList(newStatement.getId(), m);
    }

    /**
     * Create list of DSQL names for all references that used at criteria.
     * @param entityMeta parent metadata
     * @param criteria criteria object
     * @param ldts load datetime
     * @return list of DSQL names
     */
    private List<DsqlNames> createReferenceDsqlNames(EntityMeta entityMeta, Criteria criteria, LocalDateTime ldts) {
        return criteria.getWhere().getReferenceWheres().keySet().stream().map((a) -> dsqlNamesService.getNames(entityMeta, a, ldts))
                       .collect(Collectors.toList());
    }

    @Override
    public void readMultilangAttribute(List<Entity> entities, LocalDateTime ldts) {
        if (entities.size() == 0)
            return;

        EntityMeta entityMeta = entities.get(0).getMeta();
        Map<String, AttributeMeta> multilangAttributes = entityMeta.getAttributes().stream().filter(AttributeMeta::isMultilang)
                                                                   .collect(Collectors.toMap(AttributeMeta::getKey, (a) -> a));
        if (multilangAttributes.size() == 0)
            return; // no multilang attributes at entity

        List<DsqlNames> names = multilangAttributes.entrySet().stream().map((e) -> dsqlNamesService.getNames(entityMeta, e.getValue(), ldts))
                                                   .collect(Collectors.toList());

        Map<Long, MultilangAttributeDto> attributes = entityDao.selectMultilangAttributeBulk(names, entities, ldts);
        if (attributes.size() == 0)
            return;

        // distribute of DTO values into entities
        for (Entity entity : entities) {
            MultilangAttributeDto dto = attributes.get(entity.getHubId());
            if (dto != null && dto.getKeys() != null && dto.getKeys().size() > 0) { // attributes value exists for entity
                // distribute attribute value for single entity
                for (MultilangAttributeDtoKey multilangAttributeWithKey : dto.getKeys()) {
                    MultilangAttribute multilangAttribute = multilangAttributeWithKey.getMultilangAttribute();
                    AttributeMeta attributeMeta = multilangAttributes.get(multilangAttributeWithKey.getAttributeMetaKey());
                    multilangAttribute.setMeta(attributeMeta);
                    if (attributeMeta.getType() == AttributeType.TEXT) {
                        multilangAttribute.setValueEn(multilangAttributeWithKey.getTextEn());
                        multilangAttribute.setValueRu(multilangAttributeWithKey.getTextRu());
                    }
                    MultilangAttribute defaultAttr = (MultilangAttribute) entity.getAttribute(attributeMeta.getKey());
                    // replace MultilangDescription instance in entity object and at default initialised attribute by loaded from database instance
                    defaultAttr.setMultilangDescription(multilangAttribute.getMultilangDescription());
                    // and then put loaded from database attribute object as well
                    entity.setAttribute(multilangAttribute);
                }
            }
        }
    }

    @Override
    public void readFileInfoAttributes(List<Entity> entities, LocalDateTime ldts) {
        if (entities.size() == 0)
            return;

        EntityMeta entityMeta = entities.get(0).getMeta();
        Map<String, AttributeMeta> fileInfoAttributes = entityMeta.getAttributes().stream().filter((am) -> am.getType() == AttributeType.FILE)
                                                                  .collect(Collectors.toMap(AttributeMeta::getKey, (a) -> a));
        if (fileInfoAttributes.size() == 0)
            return; // no file attributes at entity

        List<DsqlNames> names = fileInfoAttributes.entrySet().stream().map((e) -> dsqlNamesService.getNames(entityMeta, e.getValue(), ldts))
                                                  .collect(Collectors.toList());

        Map<Long, FileInfoAttributeDto> attributes = entityDao.selectFileInfoAttributeBulk(names, entities, ldts);
        if (attributes.size() == 0)
            return;

        // distribute of DTO values into entities
        for (Entity entity : entities) {
            FileInfoAttributeDto dto = attributes.get(entity.getHubId());
            if (dto != null && dto.getKeys() != null && dto.getKeys().size() > 0) { // attributes value exists for entity
                // distribute attribute value for single entity
                for (FileInfoAttributeDtoKey fileInfoAttributeWithKey : dto.getKeys()) {
                    FileInfoAttribute fileInfoAttribute = fileInfoAttributeWithKey.getFileInfoAttribute();
                    AttributeMeta attributeMeta = fileInfoAttributes.get(fileInfoAttributeWithKey.getAttributeMetaKey());
                    fileInfoAttribute.setMeta(attributeMeta);

                    entity.setAttribute(fileInfoAttribute);
                }
            }
        }
    }

    @Override
    public void readEntityAttributes(List<? extends Entity> entities, LocalDateTime ldts, boolean useEntityLdts, Predicate<AttributeMeta> addFilter) {
        if (entities.size() == 0)
            return;

        EntityMeta entityMeta = entities.get(0).getMeta();
        Map<String, AttributeMeta> entityAttributesByKey = entityMeta.getAttributes()
                                                                     .stream()
                                                                     .filter((am) -> am.getType() == AttributeType.REFERENCE)
                                                                     .filter((addFilter != null ? addFilter : (am) -> true))
                                                                     .collect(Collectors.toMap(AttributeMeta::getKey, (a) -> a));
        if (entityMeta.isHierarchical()) {
            AttributeMeta childrenRefMeta = entityMeta.getChildrenReferenceAttribute();
            entityAttributesByKey.put(childrenRefMeta.getKey(), childrenRefMeta);
            AttributeMeta parentRefMeta = entityMeta.getParentReferenceAttribute();
            entityAttributesByKey.put(parentRefMeta.getKey(), parentRefMeta);
        }
        if (entityAttributesByKey.size() == 0)
            return; // no reference attributes at entity

        List<DsqlNames> names = entityAttributesByKey.entrySet().stream().map((e) -> dsqlNamesService.getNames(entityMeta, e.getValue(), ldts))
                                                     .collect(Collectors.toList());

        // by main hub id
        Map<Long, EntityAttributeDto> attributes = entityDao.selectEntityAttributeBulk(names, entities, ldts, useEntityLdts);
        if (attributes.size() == 0)
            return;

        Map<AttributeMeta, Map<Long, Pair<EntityAttribute, LocalDateTime>>> attributedAttribute = new HashMap<>();
        // distribute of DTO values into entities
        for (Entity entity : entities) {
            EntityAttributeDto dto = attributes.get(entity.getHubId());
            if (dto != null && dto.getKeys() != null && dto.getKeys().size() > 0) { // attributes value exists for entity
                // distribute attribute value for single entity
                for (EntityAttributeDtoKey entityAttributeWithKey : dto.getKeys()) {
                    EntityAttribute entityAttribute = entityAttributeWithKey.getEntityAttribute();
                    if (useEntityLdts && (entityAttribute.getLinkLdts().compareTo(entity.getLdts()) > 0
                                          || (!entityAttributeWithKey.getTs().equals(entity.getLdts()))))
                        continue;

                    AttributeMeta attributeMeta = entityAttributesByKey.get(entityAttributeWithKey.getAttributeMetaKey());
                    if (entityAttribute.getLinkedEntityAttribute() != null) {
                        entityAttribute = ObjectUtils.clone(entityAttribute);
                        entityAttribute.setLinkedEntityAttribute(null);
                    }
                    entityAttribute.setMeta(attributeMeta);

                    LinkedEntityAttribute linkedEntityAttribute = (LinkedEntityAttribute) entity.getAttribute(attributeMeta.getKey());
                    linkedEntityAttribute.addAttribute(entityAttribute);

                    if (attributeMeta.isHasAttributeAttributes()) {
                        appendAttribute(attributedAttribute, entityAttribute, entity.getLdts());
                    }
                }
            }
        }

        // load attribute's attribute for references
        for (Map.Entry<AttributeMeta, Map<Long, Pair<EntityAttribute, LocalDateTime>>> entry : attributedAttribute.entrySet()) {
            AttributeMeta attributeMeta = entry.getKey();
            Map<Long, Pair<EntityAttribute, LocalDateTime>> entityAttributeMap = entry.getValue();
            List<Map<String, Object>> attributeAttributeData =
                    useEntityLdts
                    ? readLinkAttributeData(entityMeta, attributeMeta, null,
                                            entityAttributeMap.entrySet()
                                                              .stream()
                                                              .map(e -> Pair.of(e.getKey(), e.getValue().getRight()))
                                                              .collect(Collectors.toList()),
                                            ldts)
                    : readLinkAttributeData(entityMeta, attributeMeta, entityAttributeMap.keySet(), null, ldts);
            fillToEntityAttribute(entityAttributeMap, attributeAttributeData);
        }
    }

    /**
     * Fill reference attribute satellite info by data.
     * @param entityAttribute reference attribute
     * @param data data from result set
     */
    private void fillToEntityAttribute(EntityAttribute entityAttribute, Map<String, Object> data) {
        EntityAttribute.Satellite satellite = entityAttribute.getSatellite();
        satellite.setSystemPropertyByMap(data);
        for (AttributeMeta attributeMeta : entityAttribute.getMeta().getAttributeAttributes()) {
            satellite.setAttribute(AttributeFactory.newAttribute(attributeMeta, data.get(attributeMeta.getKey())));
        }
    }

    /**
     * Fill reference attributes satellite info by data.
     * @param entityAttributeMap reference attributes
     * @param attributeAttributeData datas from result set
     */
    private void fillToEntityAttribute(Map<Long, Pair<EntityAttribute, LocalDateTime>> entityAttributeMap,
                                       List<Map<String, Object>> attributeAttributeData) {
        for (Map<String, Object> data : attributeAttributeData) {
            Long linkId = (Long) data.get(LINK_ID.name());
            Pair<EntityAttribute, LocalDateTime> entityAttributeAndLdts = entityAttributeMap.get(linkId);
            if (entityAttributeAndLdts != null) {
                EntityAttribute entityAttribute = entityAttributeAndLdts.getLeft();
                fillToEntityAttribute(entityAttribute, data);
            }
        }
    }

    /**
     * Append reference attribute to proper slot.
     * @param m attribute meta to reference attribute index
     * @param entityAttribute reference attribute to append
     * @param ldts load datetime
     */
    private static void appendAttribute(Map<AttributeMeta, Map<Long, Pair<EntityAttribute, LocalDateTime>>> m, EntityAttribute entityAttribute,
                                        LocalDateTime ldts) {
        Map<Long, Pair<EntityAttribute, LocalDateTime>> entityAttributeList = m.get(entityAttribute.getMeta());
        if (entityAttributeList == null) {
            entityAttributeList = new HashMap<>();
            m.put(entityAttribute.getMeta(), entityAttributeList);
        }
        entityAttributeList.put(entityAttribute.getLinkId(), Pair.of(entityAttribute, ldts));
    }

    @Override
    public List<Map<String, Object>> readLinkAttributeData(EntityMeta entityMeta, AttributeMeta attributeMeta, Collection<Long> linkIds,
                                                           Collection<Pair<Long, LocalDateTime>> linkIdAndLdts, LocalDateTime ldts) {
        DsqlNames dsqlNames = dsqlNamesService.getNames(entityMeta, attributeMeta, ldts);

        Map<String, Object> m = new HashMap<>();
        m.put("dsqlNames", dsqlNames);
        m.put("linkIds", linkIds);
        m.put("linkIdsAndLdts", linkIdAndLdts);
        m.put("ldts", ldts);

        MappedStatement newStatement = getLinkAttributeStatement(attributeMeta);
        return crsSqlSession.selectList(newStatement.getId(), m);
    }

    @Override
    public List<Long> readEntityAttributeBackLink(Entity childEntity, AttributeMeta parentReferenceMetadata, EntityMeta parentMetadata,
                                                  LocalDateTime ldts) {
        AttributeMeta revertReferenceMeta = new AttributeMeta();
        revertReferenceMeta.setLinkTable(parentReferenceMetadata.getLinkTable());
        revertReferenceMeta.setKey(parentReferenceMetadata.getKey());
        revertReferenceMeta.setEntityKey(parentMetadata.getKey());
        revertReferenceMeta.setType(AttributeType.REFERENCE);

        DsqlNames names = dsqlNamesService.getNames(childEntity.getMeta(), revertReferenceMeta, ldts);

        Map<Long, EntityAttributeDto> attributes = entityDao.selectEntityAttributeBulk(Collections.singletonList(names),
                                                                                       Collections.singletonList(childEntity), ldts, false);

        if (attributes.size() == 0)
            return Collections.emptyList();

        EntityAttributeDto dto = attributes.get(childEntity.getHubId());
        return dto.getKeys().stream().map(entityAttributeWithKey -> entityAttributeWithKey.getEntityAttribute().getLinkedHubId())
                  .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void writeMultilangChange(MultilangAttribute attribute, LocalDateTime ldts) {
        attribute.setSatelliteLdts(ldts);
        if (attribute.getMeta().getType() == AttributeType.TEXT) {
            entityDao.insertMultilangSatelliteText(attribute);
        } else {
            entityDao.insertMultilangSatelliteString(attribute);
        }
    }

    @Transactional
    @Override
    public void writeMultilangNew(Entity entity, MultilangAttribute attribute, LocalDateTime ldts) {
        attribute.setSatelliteLdts(ldts);
        attribute.setLinkLdts(ldts);

        entityDao.insertMultilangHub(attribute);
        if (attribute.getMeta().getType() == AttributeType.TEXT) {
            entityDao.insertMultilangSatelliteText(attribute);
        } else {
            entityDao.insertMultilangSatelliteString(attribute);
        }
        attribute.setMainHubId(entity.getHubId());
        DsqlNames dsqlNames = dsqlNamesService.getNames(entity.getMeta(), attribute.getMeta(), ldts);
        entityDao.insertLink(attribute, dsqlNames);
    }

    @Transactional
    @Override
    public void writeFileInfoChange(Entity entity, FileInfoAttribute attribute, LocalDateTime ldts) {
        String newDigest = attribute.calcDigest();
        if (attribute.getContent() == null && !attribute.isLinkRemoved()
            && newDigest.equals(attribute.getDigest())) {
            return;
        }

        attribute.setSatelliteLdts(ldts);
        if (!newDigest.equals(attribute.getDigest()) || attribute.isLinkRemoved()) {
            attribute.setDigest(newDigest);
            entityDao.insertFileInfoSatellite(attribute);
        }
        if (attribute.getContent() != null && !attribute.isLinkRemoved()) {
            entityDao.insertFileInfoContentSatellite(attribute);
        }
        DsqlNames dsqlNames = dsqlNamesService.getNames(entity.getMeta(), attribute.getMeta(), ldts);
        entityDao.insertLinkHasChangedHubIds(attribute, dsqlNames, ldts);

        attribute.setContent(null);
    }

    @Transactional
    @Override
    public void writeFileInfoNew(Entity entity, FileInfoAttribute attribute, LocalDateTime ldts) {
        if (attribute.getContent() == null || attribute.isLinkRemoved()) {
            return;
        }
        attribute.setDigest(attribute.calcDigest());
        attribute.setSatelliteLdts(ldts);
        attribute.setLinkLdts(ldts);
        attribute.setMainHubId(entity.getHubId());
        entityDao.insertFileInfoHub(attribute);
        entityDao.insertFileInfoSatellite(attribute);
        entityDao.insertFileInfoContentSatellite(attribute);
        DsqlNames dsqlNames = dsqlNamesService.getNames(entity.getMeta(), attribute.getMeta(), ldts);
        entityDao.insertLink(attribute, dsqlNames);

        attribute.setContent(null);
    }

    @Transactional
    @Override
    public void writeReferenceNewAndChange(Entity entity, LinkedEntityAttribute linkedEntityAttribute, LocalDateTime ldts) {
        // use only elements with not empty link (link has real "pointer", but entity can be not loaded yet)
        List<EntityAttribute> nonEmptyLinks = linkedEntityAttribute.getEntityAttributeList();
        nonEmptyLinks = nonEmptyLinks.stream().filter(e -> e.getLinkedHubId() != null).collect(Collectors.toList());
        CollectionUtils.Splitted<EntityAttribute> newAndExisted = CollectionUtils.split(nonEmptyLinks,
                                                                                        (EntityAttribute ea) -> ea.getLinkId() == null);

        DsqlNames dsqlNames = dsqlNamesService.getNames(entity.getMeta(), linkedEntityAttribute.getMeta(), ldts);

        // existed link, check if it changed or not
        for (EntityAttribute ea : newAndExisted.getOthers()) {
            Long saveLinkId = ea.getLinkId();
            LocalDateTime saveLinkLdts = ea.getLinkLdts();
            entityDao.insertLinkHasChangedLinkId(ea, dsqlNames, ldts);
            writeReferenceLinkSatellite(dsqlNames, ea, ldts);
            // operation may reset values as OUT parameter, this mean that noting was changed
            if (ea.getLinkId() == null && saveLinkId != null)
                ea.setLinkId(saveLinkId);
            if (ea.getLinkLdts() == null && saveLinkLdts != null)
                ea.setLinkLdts(saveLinkLdts);
        }

        // new attribute
        for (EntityAttribute ea : newAndExisted.getInCondition()) {
            ea.setMainHubId(entity.getHubId());
            ea.setLinkedHubId(ea.getEntity().getHubId());
            ea.setLinkLdts(ldts);
            entityDao.insertLink(ea, dsqlNames);
            writeReferenceLinkSatellite(dsqlNames, ea, ldts);
        }

        // clean up existed but deleted link
        entityDao.insertLinkRemoveExclusion(entity, linkedEntityAttribute.getValue(), dsqlNames, ldts);
    }

    @Transactional
    @Override
    public void writeReferenceLinkSatellite(Entity entity, EntityAttribute entityAttribute, LocalDateTime ldts) {
        DsqlNames dsqlNames = dsqlNamesService.getNames(entity.getMeta(), entityAttribute.getMeta(), ldts);
        writeReferenceLinkSatellite(dsqlNames, entityAttribute, ldts);
    }

    /**
     * Save reference attribute satellite changes.
     * @param dsqlNames DSQL names
     * @param entityAttribute reference attribute with satellite defined
     * @param ldts load datetime
     */
    private void writeReferenceLinkSatellite(DsqlNames dsqlNames, EntityAttribute entityAttribute, LocalDateTime ldts) {
        if (entityAttribute.getMeta().isHasAttributeAttributes() && entityAttribute.isSatelliteDefined()) {
            if (entityAttribute.isSatelliteDefined()) {
                EntityAttribute.Satellite satellite = entityAttribute.getSatellite();
                String digest = satellite.calcDigest();
                if (!digest.equals(satellite.getDigest())) {
                    satellite.setLdts(ldts);
                    satellite.setDigest(digest);
                    entityDao.insertAttributeSatellite(dsqlNames, entityAttribute);
                }
            } else {
                entityDao.removeAttributeSatellite(dsqlNames, Collections.singletonList(entityAttribute.getLinkId()), ldts);
            }
        }
    }

    @Transactional
    @Override
    public void writeRemoveLinkAll(Entity entity, AttributeMeta attributeMeta, LocalDateTime ldts) {
        DsqlNames dsqlNames = dsqlNamesService.getNames(entity.getMeta(), attributeMeta, ldts);
        entityDao.insertLinkFromExistedByHubPair(dsqlNames, entity.getHubId(), null, true, ldts);
    }

    @Override
    public void mergeLink(Entity entity, List<? extends LinkAttribute> attributes, AttributeMeta referenceAttributeMeta, LocalDateTime ldts) {
        DsqlNames dsqlNames = dsqlNamesService.getNames(entity.getMeta(), referenceAttributeMeta);
        for (LinkAttribute linkAttribute : attributes) {
            if (linkAttribute.getMainHubId() == null)
                linkAttribute.setMainHubId(entity.getHubId());
            if (linkAttribute.getLinkedHubId() == null && linkAttribute instanceof EntityAttribute)
                linkAttribute.setLinkedHubId(((EntityAttribute) linkAttribute).getEntity().getHubId());
            entityDao.insertLinkHasChangedHubIds(linkAttribute, dsqlNames, ldts);
        }
        entityDao.insertLinkRemoveExclusion(entity, attributes, dsqlNames, ldts);
    }

    @Override
    public InputStream getFileContent(FileInfoAttribute fileInfoAttribute, LocalDateTime ldts) {
        Map<String, byte[]> map = entityDao.getFileContent(fileInfoAttribute, ldts);
        byte[] bytes = (map == null || map.entrySet().isEmpty())
                       ? new byte[0]
                       : map.entrySet().iterator().next().getValue();
        return new ByteArrayInputStream(bytes);
    }

    @Transactional
    @Override
    public void writeEntityNew(Entity entity, LocalDateTime ldts) {
        DsqlNames dsqlNames = dsqlNamesService.getNames(entity.getMeta());
        entity.setLdts(ldts);
        if (entity.getKey() != null && entity.getKey().isEmpty())
            entity.setKey(null);
        entityDao.insertEntityHub(entity, dsqlNames.getHubTableName(), dsqlNames.getHubSequenceName());
        entityDao.insertEntitySatellite(entity, dsqlNames.getSatelliteTableName(), dsqlNames.getSatelliteSequenceName());
    }

    @Transactional
    @Override
    public void writeEntityChange(Entity entity, LocalDateTime ldts) {
        DsqlNames dsqlNames = dsqlNamesService.getNames(entity.getMeta());
        entity.setLdts(ldts);
        entityDao.insertEntitySatellite(entity, dsqlNames.getSatelliteTableName(), dsqlNames.getSatelliteSequenceName());
    }

    @Transactional
    @Override
    public void writeRemoveEntity(Entity entity, LocalDateTime ldts) {
        DsqlNames dsqlNames = dsqlNamesService.getNames(entity.getMeta());
        entity.setLdts(ldts);
        entityDao.insertRemoveEntity(dsqlNames, entity, ldts);
    }

    /**
     * Make clone of EntityDao.selectEntity.
     * @param config mybatis config
     * @param originalStatement original statement
     * @param newResultMapIdSuffix suffix wil be added to new result map for statements
     * @param newStatementId new statement id
     * @param inTable list of attributes to select as columns from table
     * @return new statement
     */
    private MappedStatement cloneMappedStatement(Configuration config, MappedStatement originalStatement, String newResultMapIdSuffix,
                                                 String newStatementId, List<AttributeMeta> inTable) {
        ResultMap originalResultMap = originalStatement.getResultMaps().get(0);
        List<ResultMapping> newResultMappings = new ArrayList<>();

        for (ResultMapping rmng : originalResultMap.getResultMappings()) {
            ResultMapping.Builder b = new ResultMapping
                    .Builder(config, rmng.getProperty(), rmng.getColumn(), rmng.getTypeHandler())
                    .javaType(rmng.getJavaType())
                    .jdbcType(rmng.getJdbcType());
            newResultMappings.add(b.build());
        }

        // append entity own intable attribute <-> columns mapping
        for (AttributeMeta am : inTable) {
            ResultMapping m = createResultMapping(config, am);
            newResultMappings.add(m);
        }

        final ResultMap rm = new ResultMap.Builder(config, originalResultMap.getId() + KEY_DELIMITER + newResultMapIdSuffix,
                                                   originalResultMap.getType(),
                                                   newResultMappings, true).build();

        return new MappedStatement
                .Builder(config, newStatementId, originalStatement.getSqlSource(), originalStatement.getSqlCommandType())
                .resource(originalStatement.getResource())
                .parameterMap(originalStatement.getParameterMap())
                .resultMaps(Arrays.asList(rm))
                .fetchSize(originalStatement.getFetchSize())
                .timeout(originalStatement.getTimeout())
                .resultSetType(originalStatement.getResultSetType())
                .cache(originalStatement.getCache())
                .flushCacheRequired(originalStatement.isFlushCacheRequired())
                .useCache(originalStatement.isUseCache())
                .resultOrdered(originalStatement.isResultOrdered())
                .keyGenerator(originalStatement.getKeyGenerator())
                //??.keyProperty(originalStatement.getKeyProperties() != null)
                //??.keyColumn(originalStatement.getKeyColumns())
                .databaseId(originalStatement.getDatabaseId())
                .lang(originalStatement.getLang())
                //??.resultSets(originalStatement.getResultSets())
                .build();
    }

    /**
     * Create one result map for attribute.
     * @param config mybatis config
     * @param am attribute metadata
     * @return result map to adding into statement
     */
    private ResultMapping createResultMapping(Configuration config, AttributeMeta am) {
        ResultMapping.Builder b = new ResultMapping
                .Builder(config, am.getKey(), am.getNativeColumn(), AttributeFactory.getJavaClass(am))
                .jdbcType(AttributeFactory.getMybatisJdbcType(am));
        return b.build();
    }

    /**
     * Detect if statement has unmapped attribute.
     * @param listAttributes entity metadata attributes to check
     * @param mappedStatement mapped statement
     * @return true if exists not mapped attribute
     */
    private static boolean hasUnmappedIntableAttributes(Collection<AttributeMeta> listAttributes, MappedStatement mappedStatement) {
        ResultMap resultMap = mappedStatement.getResultMaps().get(0);
        for (AttributeMeta am : listAttributes) {
            if (!contains(am, resultMap)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper to detect if result map contains mapping for attribute.
     * @param attributeMeta attribute metadata
     * @param resultMap result map
     * @return true is contains
     */
    private static boolean contains(AttributeMeta attributeMeta, ResultMap resultMap) {
        for (ResultMapping rm : resultMap.getResultMappings()) {
            if (attributeMeta.getKey().equals(rm.getProperty()))
                return true;
        }
        return false;
    }

    /**
     * Special remover for statement.
     * @param mappedStatements set of mapped statements from configuration.
     * @param mappedStatement mapped statement to remove
     */
    private static void remove(Collection<MappedStatement> mappedStatements, MappedStatement mappedStatement) {
        mappedStatements.removeIf(o -> mappedStatement.getId().equals(o.getId()));
    }

    /**
     * Get hub-sattelite select statement for entry.
     * Check existance or create statement. Add statement to Mybatis config object.
     * New statement created with id EntityDao.selectEntity#{entityKey}
     * Statement created as clone of EntityDao.selectEntity
     * @param entityMeta entity metadata
     * @return statement
     */
    private MappedStatement getEntityStatement(EntityMeta entityMeta) {
        Configuration configuration = crsSqlSession.getConfiguration();

        String originalStatementId = SELECT_ENTITY_STATEMENT_ID;
        String newStatementId = originalStatementId + KEY_DELIMITER + entityMeta.getKey();
        String newResultMapSuffix = entityMeta.getKey();

        return getMappedStatement(configuration, entityMeta.getInTableAttribute(), originalStatementId, newStatementId, newResultMapSuffix);
    }

    /**
     * Get or create private statement to kind of reference attribute.
     * @param referenceAttributeMeta reference attribute metadata
     * @return Mybatis mapped statement
     */
    private MappedStatement getLinkAttributeStatement(AttributeMeta referenceAttributeMeta) {
        Configuration configuration = crsSqlSession.getConfiguration();
        String originalStatementId = SELECT_ATTRIBUTE_ATTRIBUTES_STATEMENT_ID;
        String newStatementId = originalStatementId + KEY_DELIMITER + referenceAttributeMeta.getKey();
        String newResultMapSuffix = referenceAttributeMeta.getKey();
        return getMappedStatement(configuration, referenceAttributeMeta.getAttributeAttributes(), originalStatementId, newStatementId,
                                  newResultMapSuffix);
    }

    /**
     * Get or create new ststements with id. Check if statement has all columns defined by attributes.
     * Statement cloned from original description-defined statement to new statement if it not exists.
     * @param configuration Mybatis configuration
     * @param attributeMetas list of attributes to check column existence
     * @param originalStatementId original statenet id thad defined at static description
     * @param newStatementId new statement id
     * @param newResultMapSuffix suffix to new result map will be defined for new statement
     * @return mapped Mybatis statement
     */
    private MappedStatement getMappedStatement(Configuration configuration, List<AttributeMeta> attributeMetas, String originalStatementId,
                                               String newStatementId, String newResultMapSuffix) {
        MappedStatement ret;
        MappedStatement originalStatement = configuration.getMappedStatement(originalStatementId);
        boolean readLock = true;
        mybatisConfigLock.readLock().lock();
        try {
            if (configuration.hasStatement(newStatementId)) {
                ret = configuration.getMappedStatement(newStatementId);
                if (hasUnmappedIntableAttributes(attributeMetas, ret)) {
                    mybatisConfigLock.readLock().unlock(); // jdk8 don't support direct lock read-to-write upgrade
                    readLock = false;
                    mybatisConfigLock.writeLock().lock();
                    try {
                        if (hasUnmappedIntableAttributes(attributeMetas, ret)) { // double check required
                            remove(configuration.getMappedStatements(), ret);
                            ret = cloneMappedStatement(configuration, originalStatement, newResultMapSuffix, newStatementId, attributeMetas);
                            configuration.addMappedStatement(ret);
                        }
                    } finally {
                        mybatisConfigLock.writeLock().unlock();
                    }
                }

            } else {
                mybatisConfigLock.readLock().unlock(); // jdk8 don't support direct lock read-to-write upgrade
                readLock = false;
                mybatisConfigLock.writeLock().lock();

                try {
                    if (configuration.hasStatement(newStatementId)) { // double check required
                        ret = configuration.getMappedStatement(newStatementId);
                        if (hasUnmappedIntableAttributes(attributeMetas, ret)) {
                            configuration.getMappedStatements().remove(ret);
                            ret = cloneMappedStatement(configuration, originalStatement, newResultMapSuffix, newStatementId, attributeMetas);
                            configuration.addMappedStatement(ret);
                        }
                    } else {
                        ret = cloneMappedStatement(configuration, originalStatement, newResultMapSuffix, newStatementId, attributeMetas);
                        configuration.addMappedStatement(ret);
                    }
                } finally {
                    mybatisConfigLock.writeLock().unlock();
                }
            }

        } finally {
            if (readLock)
                mybatisConfigLock.readLock().unlock();
        }
        return ret;
    }
}
