package ru.masterdm.crs.web.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeLocale;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMetaGroup;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.domain.entity.meta.EntityTypeAttributeMeta;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.SecurityService;
import ru.masterdm.crs.web.domain.entity.EntityMetaStatus;
import ru.masterdm.crs.web.domain.entity.EntityStatus;

/**
 * Entity meta service implementation.
 * @author Igor Matushak
 */
@Service("entityMetaUiService")
public class EntityMetaUiServiceImpl implements EntityMetaUiService {

    @Autowired
    private EntityMetaService entityMetaService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private EntityService entityService;

    @Override
    public void prepareEntityMetaGroups(List<EntityMetaGroup> entityMetaGroups, List<EntityMeta> entityMetaList, String entityMetaFilter,
                                        EntityMetaStatus rootEntityMetaStatus, AttributeLocale locale) {
        String filter = (entityMetaFilter == null) ? StringUtils.EMPTY : entityMetaFilter.trim().toUpperCase();
        entityMetaGroups.stream().forEach(
                g -> {
                    Set<Long> ids = g.getElements().stream().map(el -> el.getHubId()).collect(Collectors.toSet());
                    if (locale == null)
                        throw new CrsException("locale is null");
                    String groupName = g.getName().getDescription(locale).toUpperCase();
                    boolean groupNameMatch = g.getKey().contains(filter) || groupName.contains(filter);
                    EntityMetaStatus newGroup = new EntityMetaStatus(g, false);
                    if (!groupNameMatch) {
                        entityMetaList.stream()
                                      .filter(em -> ids.contains(em.getHubId()))
                                      .forEachOrdered(em -> {
                                          newGroup.getChildren().add(new EntityMetaStatus(em, false));
                                      });
                    } else {
                        for (EntityMeta entityMeta : g.getElements())
                            newGroup.getChildren().add(new EntityMetaStatus(entityMeta, false));
                    }
                    if (!newGroup.getChildren().isEmpty() || groupName.contains(filter))
                        rootEntityMetaStatus.getChildren().add(newGroup);
                }
        );
    }

    @Override
    public boolean getReferenceValid(EntityStatus entityStatus, String attributeMetaKey) {
        String linkedEntityKey = entityStatus.getEntity().getMeta().getAttributes()
                                             .stream()
                                             .filter(attribute -> attributeMetaKey.equals(attribute.getKey()))
                                             .findFirst().get().getEntityKey();
        EntityMeta linkedEntityMeta = entityMetaService.getEntityMetaByKey(linkedEntityKey, null);

        String linkedAttributeKey = null;
        if (linkedEntityMeta != null) {
            linkedAttributeKey = entityStatus.getEntity().getMeta().getAttributes()
                                             .stream()
                                             .filter(attribute -> attributeMetaKey.equals(attribute.getKey()))
                                             .findFirst().get().getAttributeKey();
        }

        return !(linkedEntityMeta == null || linkedAttributeKey == null);
    }

    @Override
    public Entity getPermissionEntity(EntityType entityType) {
        EntityMeta entityTypeMeta = entityMetaService.getEntityMetaByKey(EntityTypeAttributeMeta.METADATA_KEY, null);
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(
                new WhereItem(entityTypeMeta.getKeyAttribute(), Operator.EQ,
                              entityType.name()));

        List<Entity> entityTypes = (List<Entity>) entityService.getEntities(entityTypeMeta, criteria, null, null);
        return entityTypes.get(0);
    }
}
