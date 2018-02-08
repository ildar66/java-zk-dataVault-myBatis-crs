package ru.masterdm.crs.service;

import static ru.masterdm.crs.domain.entity.meta.CommonColumn.DIGEST;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import ru.masterdm.crs.dao.entity.meta.MetadataDao;
import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.FormulaResult;
import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.entity.AbstractDvEntity;
import ru.masterdm.crs.domain.entity.BusinessAction;
import ru.masterdm.crs.domain.entity.Department;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.Permission;
import ru.masterdm.crs.domain.entity.Role;
import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.attribute.EntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.FileInfoAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.MultilangAttribute;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.CommonAttribute;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMetaGroup;
import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.service.entity.EntityDbService;
import ru.masterdm.crs.util.CollectionUtils;
import ru.masterdm.crs.util.annotation.CurrentTimeStamp;

/**
 * Data Vault interaction service implementation.
 * @author Sergey Valiev
 */
@Validated
@Service("entityService")
public class EntityServiceImpl implements EntityService {

    @Autowired
    private MetadataDao metadataDao;
    @Autowired
    private EntityDbService entityDbService;
    @Autowired
    private EntityMetaService entityMetaService;
    @Autowired
    private SecurityService securityService;

    @Override
    public Entity newEmptyEntity(@NotNull @P("entityMeta") EntityMeta entityMeta) {
        // TODO create annotation based entity creator
        Entity entity = null;
        switch (entityMeta.getKey()) {
            case FormulaResult.METADATA_KEY:
                entity = new FormulaResult();
                break;
            case Model.METADATA_KEY:
                entity = new Model();
                break;
            case Calculation.METADATA_KEY:
                entity = new Calculation();
                break;
            case User.METADATA_KEY:
                entity = new User();
                break;
            case Department.METADATA_KEY:
                entity = new Department();
                break;
            case FormTemplate.METADATA_KEY:
                entity = new FormTemplate();
                break;
            case EntityMetaGroup.METADATA_KEY:
                entity = new EntityMetaGroup();
                break;
            case Role.METADATA_KEY:
                entity = new Role();
                break;
            case BusinessAction.METADATA_KEY:
                entity = new BusinessAction();
                break;
            case Permission.METADATA_KEY:
                entity = new Permission();
                break;
            default:
                entity = new Entity();
        }

        entity.setMeta(entityMeta);
        return entity;
    }

    @Override
    public Entity newEmptyEntity(@NotNull @P("entityMeta") String entityMetaKey) {
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(entityMetaKey, metadataDao.getSysTimestamp());
        if (entityMeta == null)
            throw new CrsException("Can't load entity metadata for key '" + entityMetaKey + "'");
        return newEmptyEntity(entityMeta);
    }

    @Override
    public Long getEntityIdByKey(@NotNull EntityMeta entityMeta, @NotNull String key) {
        return entityDbService.getEntityIdByKey(entityMeta, key);
    }

    @Override
    public Entity getEntity(@NotNull @P("entityMeta") EntityMeta entityMeta, @NotNull Long id, @CurrentTimeStamp LocalDateTime ldts) {
        Criteria criteria = new Criteria();
        criteria.setHubIds(Collections.singleton(id));
        List<Entity> le = (List<Entity>) getEntities(entityMeta, criteria, null, ldts);
        if (le.size() == 0)
            return null;
        return le.get(0);
    }

    @Override
    public Entity getEntity(@NotNull @P("entityMeta") EntityMeta entityMeta, @NotNull String key, @CurrentTimeStamp LocalDateTime ldts) {
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(entityMeta.getKeyAttribute(), Operator.EQ, key));
        List<Entity> le = (List<Entity>) getEntities(entityMeta, criteria, null, ldts);
        if (le.size() == 0)
            return null;
        return le.get(0);
    }

    @Override
    public List<? extends Entity> getEntities(@NotNull @P("entityMeta") EntityMeta entityMeta, Criteria criteria, RowRange rowRange,
                                              @CurrentTimeStamp LocalDateTime ldts) {
        return getEntities(entityMeta, criteria, rowRange, ldts, ldts);
    }

    @Override
    public List<? extends Entity> getEntities(@NotNull @P("entityMeta") EntityMeta entityMeta, Criteria criteria, RowRange rowRange,
                                              @CurrentTimeStamp LocalDateTime ldts,
                                              @CurrentTimeStamp LocalDateTime metadataLdts) {
        // TODO: make get Parameter Object with Builder
        List<Entity> entityList = (List<Entity>) getEntitiesBase(entityMeta, criteria, rowRange, ldts);
        createExternalAttributes(entityList, ldts, metadataLdts);
        return entityList;
    }

    @Override
    public void loadEntityChildren(@NotNull Entity entity, @CurrentTimeStamp LocalDateTime ldts) {
        loadEntityChildren(Collections.singletonList(entity), ldts);
    }

    @Override
    public void loadEntityChildren(@NotNull List<Entity> entities, @CurrentTimeStamp LocalDateTime ldts) {
        if (entities.isEmpty())
            return;

        EntityMeta entityMeta = entities.get(0).getMeta();
        createExternalAttributesEntities(entities, entityMeta.getChildrenReferenceAttribute(), entityMeta, ldts, ldts);
    }

    @Override
    public void loadEntityParent(@NotNull Entity entity, @CurrentTimeStamp LocalDateTime ldts) {
        loadEntityParent(Collections.singletonList(entity), ldts);
    }

    @Override
    public void loadEntityParent(@NotNull List<Entity> entities, @CurrentTimeStamp LocalDateTime ldts) {
        if (entities.isEmpty())
            return;

        EntityMeta entityMeta = entities.get(0).getMeta();
        createExternalAttributesEntities(entities, entityMeta.getParentReferenceAttribute(), entityMeta, ldts, ldts);
    }

    @Override
    public List<? extends Entity> getEntitiesBase(@NotNull @P("entityMeta") EntityMeta entityMeta, Criteria criteria, RowRange rowRange,
                                                  @CurrentTimeStamp LocalDateTime ldts) {
        return createEntityBase(entityMeta, entityDbService.readEntityData(entityMeta, criteria, rowRange, ldts));
    }

    @Transactional
    @Override
    public void persistEntityBase(@NotNull @P("entity") Entity entity, @CurrentTimeStamp LocalDateTime ldts) {
        if (entity.getHubId() == null) {
            entity.setDigest(entity.calcDigest());
            entityDbService.writeEntityNew(entity, ldts);
        } else {
            String newDigest = entity.calcDigest();
            if (!newDigest.equals(entity.getDigest())) {
                entity.setDigest(newDigest);
                entityDbService.writeEntityChange(entity, ldts);
            }
        }
        entity.setLdts(ldts);
    }

    @Transactional
    @Override
    public void persistEntity(@NotNull @P("entity") Entity entity) {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        persistEntity(entity, ldts);
    }

    @Transactional
    @Override
    public void persistEntityConsistent(Entity... entities) {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        for (Entity entity : entities)
            persistEntity(entity, ldts);
    }

    /**
     * Persists entity with predefined load datetime.
     * @param entity entity object
     * @param ldts load datetime
     */
    @Override
    @Transactional
    public void persistEntity(@NotNull @P("entity") Entity entity, @CurrentTimeStamp LocalDateTime ldts) {
        persistEntityBase(entity, ldts);

        for (AttributeMeta attributeMeta : entity.getMeta().getAttributes()) {
            if (attributeMeta.isInTable())
                continue;

            if (attributeMeta.isMultilang()) {
                persistMultilangAttribute(entity, (MultilangAttribute) entity.getAttribute(attributeMeta.getKey()), ldts);

            } else if (attributeMeta.getType() == AttributeType.FILE) {
                persistFileAttribute(entity, (FileInfoAttribute) entity.getAttribute(attributeMeta.getKey()), ldts);
            } else if (attributeMeta.getType() == AttributeType.REFERENCE) {
                persistsReferenceAttribute(entity, (LinkedEntityAttribute) entity.getAttribute(attributeMeta.getKey()), ldts);
            }
        }

        if (entity.getMeta().isHierarchical()) {
            persistsReferenceAttribute(entity, entity.getChildrenReferenceAttribute(), ldts);
        }

        // TODO hard coded only for CALC
        if (CollectionUtils.contains(entity.getMeta().getKey(), Department.METADATA_KEY, Calculation.METADATA_KEY,
                                     User.METADATA_KEY, Role.METADATA_KEY))
            securityService.pendingSecureChange(Calculation.METADATA_KEY, entity);
    }

    @Override
    @Transactional
    public void removeEntity(@NotNull @P("entity") Entity entity, @CurrentTimeStamp LocalDateTime ldts) {
        entity.setRemoved(true);
        // digest not renewed for deletion
        entityDbService.writeRemoveEntity(entity, ldts);
    }

    @Transactional
    @Override
    public void removeEntity(@NotNull @P("entity") Entity entity) {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        removeEntity(entity, ldts);
    }

    /**
     * Manage saving operation of multilang attributes for entity.
     * @param entity entity object
     * @param multilangAttribute multilang attribute
     * @param ldts load datetime
     */
    private void persistMultilangAttribute(Entity entity, MultilangAttribute multilangAttribute, LocalDateTime ldts) {
        // values exists or not, store or change new values
        if (multilangAttribute.getLinkId() != null) {
            String newDigest = multilangAttribute.calcDigest();
            if (!(newDigest.equals(multilangAttribute.getDigest()))) {
                multilangAttribute.setDigest(newDigest);
                entityDbService.writeMultilangChange(multilangAttribute, ldts);
            }

        } else {
            // attribute not exists, it is absolutely new
            multilangAttribute.setDigest(multilangAttribute.calcDigest());
            entityDbService.writeMultilangNew(entity, multilangAttribute, ldts);
        }
    }

    /**
     * Manage saving operation of file attributes for entity.
     * To be implemented.
     * @param entity entity object
     * @param fileInfoAttribute file attribute
     * @param ldts load datetime
     */
    private void persistFileAttribute(Entity entity, FileInfoAttribute fileInfoAttribute, LocalDateTime ldts) {
        if (fileInfoAttribute.getLinkId() != null) {
            // link exists, write changes and unremove link if needed
            entityDbService.writeFileInfoChange(entity, fileInfoAttribute, ldts);
        } else {
            // attribute not exists, it is absolutely new. Create link with new file
            entityDbService.writeFileInfoNew(entity, fileInfoAttribute, ldts);
        }
    }

    @Override
    public InputStream getFileContent(FileInfoAttribute fileInfoAttribute, @CurrentTimeStamp LocalDateTime ldts) {
        return entityDbService.getFileContent(fileInfoAttribute, ldts);
    }

    /**
     * Manage saving operation of reference attributes for entity.
     * @param entity entity object
     * @param attribute reference attribute
     * @param ldts load datetime
     */
    private void persistsReferenceAttribute(Entity entity, LinkedEntityAttribute attribute, LocalDateTime ldts) {
        if (attribute.isEmpty())
            entityDbService.writeRemoveLinkAll(entity, attribute.getMeta(), ldts);
        else
            entityDbService.writeReferenceNewAndChange(entity, attribute, ldts);
    }

    /**
     * Create skeleton of entity objects from loaded data.
     * @param entityMeta entity metadata
     * @param entityData map by hubId of attributes and system column values
     * @return created entity objects
     */
    private List<Entity> createEntityBase(EntityMeta entityMeta, List<Map<String, Object>> entityData) {
        List<Entity> entityList = new ArrayList<>(entityData.size());
        for (Map<String, Object> m : entityData) {
            Entity entity = createEntityBase(entityMeta, m);
            entityList.add(entity);
        }
        return entityList;
    }

    /**
     * Create skeleton of entity object from loaded data.
     * Create intable attributes.
     * @param entityMeta entity metadata
     * @param entityData attributes and system column values
     * @return created entity object
     */
    private Entity createEntityBase(EntityMeta entityMeta, Map<String, Object> entityData) {
        if (entityData == null || entityData.isEmpty())
            return null;

        // get system columns from map
        Entity entity = newEmptyEntity(entityMeta);
        //entity.setMeta(entityMeta);
        entity.setSystemPropertyByMap(entityData);
        entity.setDigest((String) entityData.get(DIGEST.name()));

        // get business attribute values
        for (AttributeMeta attributeMeta : entityMeta.getAttributes()) {
            AbstractAttribute attribute = null;

            if (attributeMeta.isInTable()) {
                attribute = entity.getAttribute(attributeMeta.getKey());
                attribute.setValue(entityData.get(attributeMeta.getKey()));
            }
        }

        return entity;
    }

    /**
     * Create all entity attributes.
     * @param entities list of entities
     * @param ldts load datetime
     * @param metadataLdts metadata ldts
     */
    private void createExternalAttributes(List<Entity> entities, LocalDateTime ldts, LocalDateTime metadataLdts) {
        if (entities.size() == 0)
            return;
        if (metadataLdts == null) metadataLdts = ldts;

        entityDbService.readMultilangAttribute(entities, ldts);
        entityDbService.readFileInfoAttributes(entities, ldts);
        entityDbService.readEntityAttributes(entities, ldts, false, null);

        EntityMeta entityMeta = entities.get(0).getMeta();

        // fill up entity reference attribute
        // TODO load reference by default make now, todo -> impement loading references option
        List<AttributeMeta> entityAttributes = entityMeta.getAttributes().stream()
                                                         .filter((am) -> am.getType() == AttributeType.REFERENCE)
                                                         .collect(Collectors.toList());
        // TODO load self reference by default dont make now, todo -> impement loading references option
        // if (option.isLoad(CHILDREN))
        //     entityAttributes.add(entityMeta.getChildrenReferenceAttribute());

        for (AttributeMeta referAttributeMeta : entityAttributes) {
            createExternalAttributesEntities(entities, referAttributeMeta, null, ldts, metadataLdts);
        }
    }

    /**
     * Add referenced entities into link attributes.
     * @param entities list of main entities
     * @param referAttributeMeta REFERENCE attribute to load
     * @param linkEntityMeta entity for referenced entity type. may be null.
     * @param ldts load datetime
     * @param metadataLdts metadata load date time
     */
    private void createExternalAttributesEntities(List<Entity> entities, AttributeMeta referAttributeMeta, EntityMeta linkEntityMeta,
                                                  LocalDateTime ldts, LocalDateTime metadataLdts) {
        // collect ids
        // EntityAttribute[childHubId], one child entity may be referenced by few parent attribute (many2many, many2one)
        Map<Long, List<EntityAttribute>> childIdToParent =
                entities.stream()
                        .filter(e -> e.isAttributeExists(referAttributeMeta.getKey()))
                        .flatMap(e -> ((LinkedEntityAttribute<AbstractDvEntity>) e.getAttribute(referAttributeMeta.getKey()))
                                .getEntityAttributeList().stream())
                        .collect(Collectors.groupingBy((EntityAttribute ea) -> ea.getLinkedHubId(), Collectors.toList()));

        if (childIdToParent.isEmpty())
            return;

        if (linkEntityMeta == null)
            linkEntityMeta = entityMetaService.getEntityMetaByKey(referAttributeMeta.getEntityKey(), metadataLdts);

        if (linkEntityMeta == null)
            return;

        Criteria childEntityCriteria = new Criteria();
        childEntityCriteria.setHubIds(childIdToParent.keySet());
        List<Entity> childEntities = (List<Entity>) getEntitiesBase(linkEntityMeta, childEntityCriteria, null, ldts);

        entityDbService.readMultilangAttribute(childEntities, ldts);
        entityDbService.readFileInfoAttributes(childEntities, ldts);
        entityDbService.readEntityAttributes(childEntities, ldts, false,
                                             (am) -> am.getKey()
                                                       .equals(CommonAttribute.CHILDREN)); // only children reference read but not entity load

        // distribute child entity into attribute
        for (Entity childEntity : childEntities) {
            List<EntityAttribute> entityAttributesList = childIdToParent.get(childEntity.getHubId());
            if (entityAttributesList != null) {
                for (EntityAttribute ea : entityAttributesList) {
                    ea.setEntity(childEntity);
                }
            }
        }
    }
}
