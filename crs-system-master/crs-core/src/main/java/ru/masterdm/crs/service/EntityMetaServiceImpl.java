package ru.masterdm.crs.service;

import static ru.masterdm.crs.domain.entity.meta.EntityMetaGroup.EntityMetaGroupAttributeMeta;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import ru.masterdm.crs.dao.entity.meta.MetadataDao;
import ru.masterdm.crs.dao.entity.meta.dto.AttributeAttributesDto;
import ru.masterdm.crs.dao.entity.meta.dto.EntityAttributeMetaDto;
import ru.masterdm.crs.dao.entity.meta.dto.EntityMetaDto;
import ru.masterdm.crs.domain.AuditAction;
import ru.masterdm.crs.domain.entity.attribute.EntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.DsqlNames;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMetaGroup;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.exception.calc.NoPrototypeFactoryException;
import ru.masterdm.crs.exception.meta.RemoveReferencedMetaException;
import ru.masterdm.crs.service.calc.EntityMetaPrototypeFactory;
import ru.masterdm.crs.service.entity.EntityDbService;
import ru.masterdm.crs.service.entity.meta.DvStructureManagement;
import ru.masterdm.crs.service.entity.meta.ddl.DsqlNamesService;
import ru.masterdm.crs.util.annotation.Audit;
import ru.masterdm.crs.util.annotation.CurrentTimeStamp;

/**
 * Metadata operations implementation.
 * @author Pavel Masalov
 */
@Validated
@Service("entityMetaService")
public class EntityMetaServiceImpl implements EntityMetaService {

    @Autowired
    private EntityDbService entityDbService;
    @Autowired
    private EntityService entityService; // temporally references, before VTBCRS-351
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private MetadataDao metadataDao;
    @Autowired
    private DvStructureManagement dvStructureManagement;
    @Autowired
    private DsqlNamesService dsqlNamesService;
    @Autowired
    @Qualifier("inputFormPrototypeFactory")
    private EntityMetaPrototypeFactory inputFormPrototypeFactory;
    @Autowired
    @Qualifier("classifierPrototypeFactory")
    private EntityMetaPrototypeFactory classifierPrototypeFactory;

    @Value("#{config['db.entity.meta.key.regexp']}")
    private String entityKeyRegexp;
    @Value("#{config['db.attribute.meta.native.column.regexp']}")
    private String attributeNativeColumnRegexp;

    private Pattern entityMetaKeyPattern;
    private Pattern attributeNativeColumnPattern;

    @Override
    public LocalDateTime getSysTimestamp() {
        return metadataDao.getSysTimestamp();
    }

    @Override
    public boolean isEntityMetaExists(@NotNull String key) {
        return metadataDao.getEntityHubIdByKey(key) != null;
    }

    @Override
    public boolean isEntityMetaGroupExists(@NotNull String key) {
        return metadataDao.getEntityMetaGroupHubIdByKey(key) != null;
    }

    @Override
    public boolean isAttributeMetaExists(@NotNull String key) {
        return metadataDao.getAttributeHubIdByKey(key) != null;
    }

    @Transactional
    @Audit(action = AuditAction.CREATE_ENTITY_META)
    @Override
    @CacheEvict(cacheNames = "entity-meta-time-slices", key = "#p0.key")
    public void persistEntityMeta(@NotNull EntityMeta entityMeta) {
        boolean add = entityMeta.getId() == null;
        doPersistEntityMeta(entityMeta);
        if (add) {
            EntityType type = entityMeta.getType();
            EntityMetaGroup.DefaultGroup defaultGroup = EntityMetaGroup.DefaultGroup.getDefaultGroupKey(type);
            if (defaultGroup != null)
                addToGroup(entityMeta, defaultGroup.name());
        }
    }

    @Transactional
    @Override
    @CacheEvict(cacheNames = "entity-meta-time-slices", key = "#p0.key")
    public void persistEntityMeta(@NotNull EntityMeta entityMeta, @NotNull String entityMetaGroupKey) {
        doPersistEntityMeta(entityMeta);
        addToGroup(entityMeta, entityMetaGroupKey);
    }

    @Transactional
    @Audit(action = AuditAction.DELETE_ENTITY_META)
    @Override
    @CacheEvict(cacheNames = "entity-meta-time-slices", key = "#p0.key")
    public void removeEntityMeta(@NotNull EntityMeta entityMeta) {
        // check remove conditions
        checkReferenced(entityMeta, metadataDao.getSysTimestamp());
        // no need delete from group deleteFromGroup(entityMeta);
        metadataDao.removeEntity(entityMeta.getId());
    }

    @Override
    public List<EntityMeta> getReferencedBy(EntityMeta entityMeta, @CurrentTimeStamp LocalDateTime ldts) {
        List<Long> metaHubIds = metadataDao.getReferencedByHubIds(entityMeta.getKey(), ldts);
        List<EntityMeta> referencedBy = metaHubIds.stream().map((id) -> getEntityMetaById(id, ldts)).collect(Collectors.toList());
        return referencedBy;
    }

    @Override
    public EntityMeta getEntityMetaById(@NotNull Long id, @CurrentTimeStamp LocalDateTime ldts) {
        String key = metadataDao.getEntityKeyById(id);
        return getEntityMetaByKeyNoCache(key, ldts);
    }

    @Override
    public EntityMeta getEntityMetaByKeyNoCache(@NotNull String entityMetaKey, @CurrentTimeStamp LocalDateTime ldts) {
        EntityMeta entityMeta = metadataDao.getEntityByKey(entityMetaKey, ldts);

        if (entityMeta != null) {
            readAttributeAttributes(Collections.singletonList(entityMeta), ldts);
        }
        return entityMeta;
    }

    @Override
    @Cacheable(cacheNames = "entity-meta", keyGenerator = "entityMetaKeyGenerator")
    public EntityMeta getEntityMetaByKey(@NotNull String entityMetaKey, @CurrentTimeStamp LocalDateTime ldts) {
        return getEntityMetaByKeyNoCache(entityMetaKey, ldts);
    }

    @Override
    @Cacheable(cacheNames = "entity-meta-time-slices", key = "#p0")
    public List<LocalDateTime> getEntityMetaTimeSlices(String entityMetaKey) {
        return metadataDao.getEntityTimeSlices(entityMetaKey);
    }

    @Override
    public List<EntityMeta> getEntityMetas(Criteria criteria, RowRange rowRange, @CurrentTimeStamp LocalDateTime ldts, @NotNull EntityType... types) {
        EntityMetaDto entityMetaDto = metadataDao.getEntities(criteria, rowRange, ldts, types);
        if (entityMetaDto == null) {
            if (rowRange != null) {
                rowRange.setTotalCount(0L);
            }
            return Collections.emptyList();
        }

        List<EntityMeta> entityMetaList = entityMetaDto.getEntityMetas();

        if (rowRange != null)
            rowRange.setTotalCount(entityMetaDto.getCc());

        if (!entityMetaList.isEmpty()) {
            readAttribute(entityMetaList, (criteria != null && !criteria.isStrictLatestActualRecord()) ? criteria.getHubIdsAndLdts() : null, ldts);
            readAttributeAttributes(entityMetaList, ldts);
        }
        return entityMetaList;
    }

    @Override
    public String getAttributeMetaKey(@NotNull EntityMeta entityMeta, @NotNull String partialAttributeKey) {
        DsqlNames dsqlNames = dsqlNamesService.getNames(entityMeta);
        return dsqlNames.getAttributeKey(partialAttributeKey);
    }

    @Override
    public EntityMetaPrototypeFactory getEntityMetaPrototypeFactory(EntityType entityType) {
        if (entityType == EntityType.CLASSIFIER)
            return classifierPrototypeFactory;
        else if (entityType == EntityType.INPUT_FORM)
            return inputFormPrototypeFactory;

        throw new NoPrototypeFactoryException(entityType);
    }

    @Override
    public List<EntityMetaGroup> getEntityMetaGroups(Criteria criteria, RowRange rowRange, @CurrentTimeStamp LocalDateTime ldts) {
        EntityMeta entityMeta = getEntityMetaByKeyNoCache(EntityMetaGroup.METADATA_KEY, ldts);
        List<EntityMetaGroup> entityMetaGroupList = (List<EntityMetaGroup>) entityService.getEntitiesBase(entityMeta, criteria, rowRange, ldts);

        if (!entityMetaGroupList.isEmpty())
            readEntityMetaGroupReferences(entityMeta, entityMetaGroupList, ldts);

        return entityMetaGroupList;
    }

    @Override
    @Transactional
    public void persistEntityMetaGroup(@NotNull EntityMetaGroup entityMetaGroup) {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        entityService.persistEntityBase(entityMetaGroup, ldts);

        LinkedEntityAttribute entity = ((LinkedEntityAttribute) entityMetaGroup.getAttribute(EntityMetaGroupAttributeMeta.ENTITY.getKey()));
        entityDbService.mergeLink(entityMetaGroup, entity.getEntityAttributeList(), entity.getMeta(), ldts);

        Long typeHubId = null;
        if (entityMetaGroup.getType() != null) {
            typeHubId = metadataDao.getEntityTypeIdByKey(entityMetaGroup.getType().name());
        }
        LinkedEntityAttribute types = ((LinkedEntityAttribute) entityMetaGroup.getAttribute(EntityMetaGroupAttributeMeta.ENTITY_TYPE.getKey()));
        types.getEntityAttributeList().clear();
        if (typeHubId != null) {
            EntityAttribute typeLink = new EntityAttribute<>();
            typeLink.setMainHubId(entityMetaGroup.getHubId());
            typeLink.setLinkedHubId(typeHubId);
            types.getEntityAttributeList().add(typeLink);
        }
        entityDbService.mergeLink(entityMetaGroup, types.getEntityAttributeList(), types.getMeta(), ldts);
    }

    @Override
    @Transactional
    public void removeEntityMetaGroup(@NotNull EntityMetaGroup entityMetaGroup) {
        entityMetaGroup.setRemoved(true);
        // cant delete default group
        if (EntityMetaGroup.DefaultGroup.isHasValue(entityMetaGroup.getKey())) {
            throw new CrsException("Cant delete default group " + entityMetaGroup.getKey());
        }

        LocalDateTime ldts = metadataDao.getSysTimestamp();
        // move elements to default group
        if (!entityMetaGroup.getElements().isEmpty()) {
            EntityType entityType = entityMetaGroup.getElements().get(0).getTypes().get(0);
            EntityMetaGroup.DefaultGroup defaultGroup = EntityMetaGroup.DefaultGroup.getDefaultGroupKey(entityType);
            if (defaultGroup != null) {
                Criteria criteria = new Criteria();
                criteria.getWhere().addItem(new WhereItem(entityMetaGroup.getMeta().getKeyAttribute(), Operator.EQ, defaultGroup.name()));
                List<EntityMetaGroup> entityMetaGroupList = getEntityMetaGroups(criteria, null, ldts);
                if (!entityMetaGroupList.isEmpty()) {
                    EntityMetaGroup defaultEntityMetaGroup = entityMetaGroupList.get(0);
                    defaultEntityMetaGroup.getElements().addAll(entityMetaGroup.getElements());
                    persistEntityMetaGroup(defaultEntityMetaGroup);
                }
            }
            entityMetaGroup.getElements().clear();
        }

        entityService.removeEntity(entityMetaGroup);
    }

    @Override
    @Transactional
    public void persistEntityMetaGroups(@NotNull List<EntityMetaGroup> entityMetaGroups) {
        for (EntityMetaGroup emg : entityMetaGroups)
            persistEntityMetaGroup(emg);
    }

    @Override
    @Transactional
    public void persistEntityMetas(@NotNull List<EntityMeta> entityMetas) {
        for (EntityMeta em : entityMetas)
            persistEntityMeta(em);
    }

    /**
     * Setup.
     */
    @PostConstruct
    private void setup() {
        entityMetaKeyPattern = Pattern.compile(entityKeyRegexp, Pattern.CASE_INSENSITIVE);
        attributeNativeColumnPattern = Pattern.compile(attributeNativeColumnRegexp, Pattern.CASE_INSENSITIVE);
    }

    /**
     * Read attribute metadatas for entity metadatas.
     * Entityes id or id-ldts pairs used to load data
     * @param entityMetaList entity metadata list
     * @param idAndLdts list of id and ldts. may be null
     * @param ldts load datetimes
     */
    private void readAttribute(List<EntityMeta> entityMetaList, Collection<Pair<Long, LocalDateTime>> idAndLdts, LocalDateTime ldts) {
        List<Long> entityIds = null;
        if (idAndLdts == null)
            entityIds = entityMetaList.stream().map(em -> em.getHubId()).collect(Collectors.toList());
        Map<Long, EntityAttributeMetaDto> attributeMetaDtoMap = metadataDao.getAttributeMeta(entityIds, idAndLdts, ldts);

        for (EntityMeta entityMeta : entityMetaList) {
            EntityAttributeMetaDto entityAttributeMetaDto = attributeMetaDtoMap.get(entityMeta.getHubId());
            if (entityAttributeMetaDto != null && !entityAttributeMetaDto.getAttributes().isEmpty())
                entityMeta.setAttributes(entityAttributeMetaDto.getAttributes());
        }
    }

    /**
     * Read attributes metadata self attributes.
     * @param entityMetaList entity metadatas
     * @param ldts load datetime
     */
    private void readAttributeAttributes(List<EntityMeta> entityMetaList, LocalDateTime ldts) {
        List<Long> attributeIds = new ArrayList<>();
        List<AttributeMeta> referenceAttributes = entityMetaList.stream().flatMap(
                (em) -> em.getAttributes().stream().filter((am) -> am.getType() == AttributeType.REFERENCE))
                                                                .peek(em -> attributeIds.add(em.getHubId())).collect(Collectors.toList());

        if (!referenceAttributes.isEmpty()) {
            Map<Long, AttributeAttributesDto> attributeAttributesDtoMap = metadataDao.getRefAttributeAttributes(attributeIds, ldts);
            if (!attributeAttributesDtoMap.isEmpty()) {
                for (AttributeMeta attributeMeta : referenceAttributes) {
                    AttributeAttributesDto attributeAttributesDto = attributeAttributesDtoMap.get(attributeMeta.getHubId());
                    if (attributeAttributesDto != null && !attributeAttributesDto.getAttributes().isEmpty())
                        attributeMeta.setAttributeAttributes(attributeAttributesDto.getAttributes());
                }
            }
        }
    }

    /**
     * Perform entity meta save operations.
     * @param entityMeta entity meta operations
     */
    private void doPersistEntityMeta(EntityMeta entityMeta) {
        if (entityMeta.getAttributes() == null || entityMeta.getAttributes().size() == 0) {
            throw new CrsException("entity should contain at least one attribute");
        }

        if (!entityMetaKeyPattern.matcher(entityMeta.getKey()).matches()) {
            throw new CrsException("entity key doesn't match regexp '" + entityKeyRegexp + "'");
        }

        List<String> wrongKeys = entityMeta.getAttributes()
                                           .stream()
                                           .filter(attribute -> !StringUtils.isEmpty(attribute.getNativeColumn()))
                                           .filter(attribute -> !attributeNativeColumnPattern.matcher(attribute.getNativeColumn()).matches())
                                           .map(AttributeMeta::getNativeColumn).collect(Collectors.toList());
        if (wrongKeys.size() > 0) {
            throw new CrsException(
                    "Attribute native column" + (wrongKeys.size() == 1 ? " " : "'s ") + wrongKeys.stream().collect(Collectors.joining(", "))
                    + " doesn't match regexp '" + attributeNativeColumnRegexp
                    + "'");
        }

        if (entityMeta.getId() != null) {
            EntityMeta entityMetaOrig = getEntityMetaByKeyNoCache(entityMeta.getKey(), metadataDao.getSysTimestamp());
            metadataDao.persistEntity(entityMeta);
            dvStructureManagement.update(entityMetaOrig, entityMeta);
            return;
        }
        metadataDao.persistEntity(entityMeta);
        dvStructureManagement.create(entityMeta);
    }

    /**
     * Add entity meta to group.
     * @param entityMeta entity meta
     * @param groupKey group key
     */
    private void addToGroup(EntityMeta entityMeta, String groupKey) {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        EntityMeta groupEntityMeta = getEntityMetaByKeyNoCache(EntityMetaGroup.METADATA_KEY, ldts);
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(groupEntityMeta.getKeyAttribute(), Operator.EQ, groupKey));
        List<EntityMetaGroup> tgtGroup = getEntityMetaGroups(criteria, null, ldts);
        if (!tgtGroup.isEmpty()) {
            EntityMetaGroup entityMetaGroup = tgtGroup.get(0);
            entityMetaGroup.getElements().add(entityMeta);
            persistEntityMetaGroup(entityMetaGroup);
        }
    }

    /**
     * Delete entity meta from group it belong.
     * @param entityMeta entity meta
     */
    private void deleteFromGroup(EntityMeta entityMeta) {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        EntityMeta groupEntityMeta = getEntityMetaByKeyNoCache(EntityMetaGroup.METADATA_KEY, ldts);
        EntityMeta entityEntityMeta = getEntityMetaByKeyNoCache(EntityMeta.METADATA_KEY, ldts);
        Criteria criteria = new Criteria();
        criteria.getWhere().addReferenceItem(groupEntityMeta.getAttributeMetadata(EntityMetaGroupAttributeMeta.ENTITY.getKey()),
                                             new WhereItem(entityEntityMeta.getKeyAttribute(), Operator.EQ, entityMeta.getKey()));
        List<EntityMetaGroup> entityMetaGroups = getEntityMetaGroups(criteria, null, ldts);
        for (EntityMetaGroup group : entityMetaGroups) {
            group.getElements().remove(entityMeta);
            persistEntityMetaGroup(group);
        }
    }

    /**
     * Check if entity is referenced by other entity.
     * @param entityMeta checked entity
     * @param ldts load time
     * @throws RemoveReferencedMetaException if reference found
     */
    private void checkReferenced(EntityMeta entityMeta, LocalDateTime ldts) {
        List<EntityMeta> referencedBy = getReferencedBy(entityMeta, ldts);
        if (!referencedBy.isEmpty())
            throw new RemoveReferencedMetaException(entityMeta, referencedBy);
    }

    /**
     * Reads entity meta group references.
     * @param entityMeta entity meta
     * @param entityMetaGroups entity meta groups
     * @param ldts load time
     */
    private void readEntityMetaGroupReferences(EntityMeta entityMeta, List<EntityMetaGroup> entityMetaGroups, LocalDateTime ldts) {
        entityDbService.readEntityAttributes(entityMetaGroups, ldts, false, null);
        // ENTITY_TYPE read to enum
        readEntityMetaGroupType(entityMetaGroups, entityMeta.getAttributeMetadata(EntityMetaGroupAttributeMeta.ENTITY_TYPE.getKey()), ldts);
        readEntityMetaGroupEntityMeta(entityMetaGroups, entityMeta.getAttributeMetadata(EntityMetaGroupAttributeMeta.ENTITY.getKey()), ldts);
    }

    /**
     * Read entity meta group type.
     * @param entityMetaGroupList entity meta group list
     * @param entityAttributeMeta entity attribute meta
     * @param ldts load time
     */
    private void readEntityMetaGroupType(List<EntityMetaGroup> entityMetaGroupList, AttributeMeta entityAttributeMeta, LocalDateTime ldts) {
        Map<Long, EntityMetaGroup> entityMetaGroupMap = new HashMap<>();
        Map<Long, List<EntityAttribute<EntityMeta>>> childIdToParent =
                entityMetaGroupList.stream()
                                   .filter((emg) -> emg.isAttributeExists(entityAttributeMeta.getKey()))
                                   .peek((emg) -> entityMetaGroupMap.put(emg.getHubId(), emg))
                                   .flatMap((e) -> ((LinkedEntityAttribute<EntityMeta>) e.getAttribute(entityAttributeMeta.getKey()))
                                           .getEntityAttributeList().stream())
                                   .collect(Collectors.groupingBy((EntityAttribute<EntityMeta> ea) -> ea.getLinkedHubId(), Collectors.toList()));

        if (!childIdToParent.isEmpty()) {
            Map<Long, Pair<Long, String>> types = metadataDao.getEntityTypeKeyById();

            for (Map.Entry<Long, Pair<Long, String>> type : types.entrySet()) {
                EntityType eType = EntityType.valueOf(type.getValue().getValue());
                if (eType != null) {
                    List<EntityAttribute<EntityMeta>> entityAttributesList = childIdToParent.get(type.getKey());
                    if (entityAttributesList != null) {
                        for (EntityAttribute ea : entityAttributesList) {
                            EntityMetaGroup entityMetaGroup = entityMetaGroupMap.get(ea.getMainHubId());
                            if (entityMetaGroup != null)
                                entityMetaGroup.setType(eType);
                        }
                    }
                }
            }
        }
    }

    /**
     * Read entity meta group entity meta.
     * @param entityMetaGroups entity meta group list
     * @param entityAttributeMeta entity attribute meta
     * @param ldts load time
     */
    private void readEntityMetaGroupEntityMeta(List<EntityMetaGroup> entityMetaGroups, AttributeMeta entityAttributeMeta, LocalDateTime ldts) {
        Criteria criteria = new Criteria();
        criteria.setHubIds(new ArrayList<>());
        EntityMeta entityMeta = getEntityMetaByKeyNoCache(EntityMeta.METADATA_KEY, ldts);
        criteria.getOrder().addItem(entityMeta.getAttributeMetadata(EntityMeta.EntityMetaAttributeMeta.VIEW_ORDER.getKey()), false);

        // EntityAttribute[childHubId], one child entity may be referenced by few parent attribute (many2many, many2one)
        Set<EntityType> types = new HashSet<>();
        Map<Long, List<EntityAttribute<EntityMeta>>> childIdToParent =
                entityMetaGroups.stream()
                                .filter((e) -> e.isAttributeExists(entityAttributeMeta.getKey()))
                                .peek((e) -> {
                                    if (e.getType() != null) types.add(e.getType());
                                })
                                .flatMap((e) -> ((LinkedEntityAttribute<EntityMeta>) e.getAttribute(entityAttributeMeta.getKey()))
                                        .getEntityAttributeList().stream())
                                .peek(e -> criteria.getHubIds().add(e.getLinkedHubId()))
                                .collect(Collectors.groupingBy((EntityAttribute<EntityMeta> ea) -> ea.getLinkedHubId(),
                                                               Collectors.toList()));

        if (!childIdToParent.isEmpty()) {
            List<EntityMeta> entities = getEntityMetas(criteria, null, ldts, types.toArray(new EntityType[] {}));
            for (EntityMeta entity : entities) {
                List<EntityAttribute<EntityMeta>> entityAttributesList = childIdToParent.get(entity.getHubId());
                if (entityAttributesList != null) {
                    for (EntityAttribute ea : entityAttributesList) {
                        ea.setEntity(entity);
                    }
                }
            }
        }
    }

    @Override
    @Transactional
    public void moveEntityMetaBeforeAnotherEntityMeta(@NotNull List<EntityMetaGroup> groups, @NotNull EntityMeta entityMetaToInsertBefore,
                                                      @NotNull EntityMeta movingEntityMeta, @NotNull EntityType entityType) {
        EntityMetaGroup targetGroup = groups.stream()
                                            .filter(gr -> gr.getElements().stream()
                                                            .anyMatch(e -> e.getHubId().equals(entityMetaToInsertBefore.getHubId())))
                                            .findFirst().get();
        moveEntityMetaToGroup(groups, targetGroup, movingEntityMeta);
        List<EntityMeta> changedEntityMetaList = changeEntityMetaOrder(movingEntityMeta, entityMetaToInsertBefore, targetGroup.getElements());
        persistEntityMetas(changedEntityMetaList);
    }

    /**
     * Change group order.
     * @param movingEntityMeta moving entity meta
     * @param entityMetaToInsertBefore entity meta before which needs to insert
     * @param entityMetaList entity meta list for changing order
     * @return changed entity meta list
     */
    private List<EntityMeta> changeEntityMetaOrder(@NotNull EntityMeta movingEntityMeta, @NotNull EntityMeta entityMetaToInsertBefore,
                                                   @NotNull List<EntityMeta> entityMetaList) {
        List<EntityMeta> changedEntityMeta = new ArrayList<>();
        Long oldOrder = movingEntityMeta.getViewOrder();
        Long orderForInsertBefore = entityMetaToInsertBefore.getViewOrder();
        if (oldOrder.equals(orderForInsertBefore))
            return changedEntityMeta;
        if (oldOrder.compareTo(orderForInsertBefore) < 0) {
            changedEntityMeta = entityMetaList.stream()
                                              .filter(em -> em.getViewOrder().compareTo(oldOrder) >= 0
                                                            && em.getViewOrder().compareTo(orderForInsertBefore - 1) <= 0)
                                              .collect(Collectors.toList());
            changedEntityMeta.forEach(em -> {
                Long viewOrder = (em.getViewOrder().equals(oldOrder)) ? (orderForInsertBefore - 1) : em.getViewOrder() - 1;
                em.setViewOrder(viewOrder);
            });
        } else {
            changedEntityMeta = entityMetaList.stream()
                                              .filter(em -> em.getViewOrder().compareTo(orderForInsertBefore) >= 0
                                                            && em.getViewOrder().compareTo(oldOrder) <= 0).collect(Collectors.toList());
            changedEntityMeta.forEach(em -> {
                Long viewOrder = (em.getViewOrder().equals(oldOrder)) ? (orderForInsertBefore) : em.getViewOrder() + 1;
                em.setViewOrder(viewOrder);
            });
        }
        return changedEntityMeta;
    }

    @Override
    @Transactional
    public void moveEntityMetaToGroup(@NotNull List<EntityMetaGroup> groups, @NotNull EntityMetaGroup targetGroup, @NotNull EntityMeta entityMeta) {
        List<EntityMetaGroup> existsInGroups = groups.stream()
                                                     .filter(gr -> gr.getElements().stream()
                                                                     .anyMatch(e -> e.getHubId().equals(entityMeta.getHubId())))
                                                     .collect(Collectors.toList());
        if (existsInGroups.stream().anyMatch(gr -> gr.getHubId().equals(targetGroup.getHubId())))
            return;
        existsInGroups.stream().forEach(gr -> {
            Iterator<EntityMeta> it = gr.getElements().iterator();
            while (it.hasNext()) {
                EntityMeta next = it.next();
                if (next.getHubId().equals(entityMeta.getHubId())) {
                    gr.getElements().remove(entityMeta);
                    break;
                }
            }
        });
        List<EntityMeta> entityMetas = targetGroup.getElements();
        long maxViewOrder = entityMetas.stream().mapToLong(em -> em.getViewOrder()).max().orElseGet(() -> 0L);
        entityMeta.setViewOrder(maxViewOrder + 1);
        persistEntityMeta(entityMeta);
        entityMetas.add(entityMeta);

        List<EntityMetaGroup> persistList = new ArrayList<>(existsInGroups);
        persistList.add(targetGroup);
        persistEntityMetaGroups(persistList);
    }

    @Override
    @Transactional
    public void changeGroupOrder(@NotNull List<EntityMetaGroup> groups, @NotNull EntityMetaGroup movingGroup,
                                 @NotNull EntityMetaGroup groupForInsertBefore) {
        Long oldOrder = movingGroup.getViewOrder();
        Long orderForInsertBefore = groupForInsertBefore.getViewOrder();
        if (oldOrder.equals(orderForInsertBefore))
            return;
        List<EntityMetaGroup> changedEntityMetaGroups = null;
        if (oldOrder.compareTo(orderForInsertBefore) < 0) {
            changedEntityMetaGroups = groups.stream()
                                            .filter(em -> em.getViewOrder().compareTo(oldOrder) >= 0
                                                          && em.getViewOrder().compareTo(orderForInsertBefore - 1) <= 0)
                                            .collect(Collectors.toList());
            changedEntityMetaGroups.forEach(em -> {
                Long viewOrder = (em.getViewOrder().equals(oldOrder)) ? (orderForInsertBefore - 1) : em.getViewOrder() - 1;
                em.setViewOrder(viewOrder);
            });
        } else {
            changedEntityMetaGroups = groups.stream()
                                            .filter(em -> em.getViewOrder().compareTo(orderForInsertBefore) >= 0
                                                          && em.getViewOrder().compareTo(oldOrder) <= 0)
                                            .collect(Collectors.toList());
            changedEntityMetaGroups.forEach(em -> {
                Long viewOrder = (em.getViewOrder().equals(oldOrder)) ? (orderForInsertBefore) : em.getViewOrder() + 1;
                em.setViewOrder(viewOrder);
            });
        }

        persistEntityMetaGroups(changedEntityMetaGroups);
    }
}
