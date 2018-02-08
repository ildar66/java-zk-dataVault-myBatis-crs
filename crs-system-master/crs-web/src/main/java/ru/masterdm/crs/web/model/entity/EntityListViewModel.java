package ru.masterdm.crs.web.model.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import ru.masterdm.crs.domain.entity.criteria.Conjunction;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.OrderItem;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeLocale;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMetaGroup;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.domain.entity.meta.EntityTypeAttributeMeta;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.web.domain.UserProfile;
import ru.masterdm.crs.web.domain.entity.EntityMetaStatus;
import ru.masterdm.crs.web.service.EntityMetaUiService;

/**
 * Entity list view model class.
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EntityListViewModel {

    @WireVariable
    private EntityMetaService entityMetaService;
    @WireVariable("pages")
    private Properties pages;
    @WireVariable("userProfile")
    private UserProfile userProfile;
    @WireVariable("webConfig")
    private Properties webConfig;
    @WireVariable
    protected EntityService entityService;
    @WireVariable
    private EntityMetaUiService entityMetaUiService;

    private List<EntityMeta> entityMetas;
    private String entityMetaFilter;

    private EntityMetaTreeModel entityMetaTreeModel;
    private List<EntityMetaGroup> entityMetaGroups;
    private EntityMeta entityMetaGroupMeta;

    /**
     * Initiates context.
     */
    @Init
    public void initSetup() {
        entityMetaFilter = userProfile.getEntityMetaFilterByKey(EntityType.PREDEFINED_DICTIONARY.name());
    }

    /**
     * Returns entity meta tree model.
     * @return entity meta tree model
     */
    public EntityMetaTreeModel getEntityMetaTreeModel() {
        if (entityMetaTreeModel == null) {
            entityMetaTreeModel = new EntityMetaTreeModel(getRootEntityMetaStatus());
        }
        return entityMetaTreeModel;
    }

    /**
     * Get root entity meta status.
     * @return root entity meta status
     */
    private EntityMetaStatus getRootEntityMetaStatus() {
        List<EntityMeta> entityMetaList = getEntityMetas();

        EntityMeta entityMetaGroupMeta = getEntityMetaGroupMeta();
        List<EntityMetaGroup> entityMetaGroups = getEntityMetaGroups(entityMetaGroupMeta);

        EntityMetaGroup group = (EntityMetaGroup) entityService.newEmptyEntity(entityMetaGroupMeta);
        EntityMetaStatus rootEntityMetaStatus = new EntityMetaStatus(group, false);
        entityMetaUiService.prepareEntityMetaGroups(entityMetaGroups, entityMetaList, entityMetaFilter,
                                                    rootEntityMetaStatus, userProfile.getLocale());
        return rootEntityMetaStatus;
    }

    /**
     * Return entity meta group meta.
     * @return entity meta group meta
     */
    private EntityMeta getEntityMetaGroupMeta() {
        if (entityMetaGroupMeta == null) {
            entityMetaGroupMeta = entityMetaService.getEntityMetaByKey(EntityMetaGroup.METADATA_KEY, null);
        }
        return entityMetaGroupMeta;
    }

    /**
     * Get entity meta groups.
     * @param entityMetaGroupMeta entity meta group meta
     * @return entity meta groups
     */
    private List<EntityMetaGroup> getEntityMetaGroups(EntityMeta entityMetaGroupMeta) {
        if (entityMetaGroups == null) {
            EntityMeta entityTypeMeta = entityMetaService.getEntityMetaByKey(EntityTypeAttributeMeta.METADATA_KEY, null);
            AttributeMeta groupEntityTypeMeta =
                    entityMetaGroupMeta.getAttributeMetadata(EntityMetaGroup.EntityMetaGroupAttributeMeta.ENTITY_TYPE.getKey());
            AttributeMeta groupViewOrderMeta =
                    entityMetaGroupMeta.getAttributeMetadata(EntityMetaGroup.EntityMetaGroupAttributeMeta.VIEW_ORDER.getKey());
            Criteria groupCriteria = new Criteria();
            groupCriteria.getWhere()
                         .addReferenceItem(groupEntityTypeMeta, new WhereItem(entityTypeMeta.getKeyAttribute(), Operator.EQ, EntityType.DICTIONARY));
            groupCriteria.getOrder().addItem(new OrderItem(groupViewOrderMeta, false));
            entityMetaGroups = entityMetaService.getEntityMetaGroups(groupCriteria, null, null);
        }
        return entityMetaGroups;
    }

    /**
     * Returns entity meta list.
     * @return entity meta list
     */
    public List<EntityMeta> getEntityMetas() {
        if (entityMetas == null) {
            EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(EntityMeta.METADATA_KEY, null);
            Criteria criteria = new Criteria();
            String nameKey = userProfile.getLocale().equals(AttributeLocale.RU) ? EntityMeta.EntityMetaAttributeMeta.NAME_RU.getKey()
                                                                                : EntityMeta.EntityMetaAttributeMeta.NAME_EN.getKey();
            AttributeMeta viewOrderMetadata = entityMeta.getAttributeMetadata(EntityMeta.EntityMetaAttributeMeta.VIEW_ORDER.getKey());
            AttributeMeta attributeMetadata = entityMeta.getAttributeMetadata(nameKey);
            if (entityMetaFilter != null && !entityMetaFilter.isEmpty()) {
                criteria.getWhere().addItem(new WhereItem(entityMeta.getKeyAttribute(), Operator.LIKE, "%" + entityMetaFilter.toUpperCase() + "%"));
                criteria.getWhere().addItem(new WhereItem(Conjunction.OR, attributeMetadata, Operator.LIKE, "%" + entityMetaFilter + "%"));
            }
            criteria.getOrder().addItem(viewOrderMetadata, false);
            entityMetas = entityMetaService.getEntityMetas(criteria, null, null, EntityType.DICTIONARY);
        }
        return entityMetas;
    }

    /**
     * Edits entities.
     * @param entityMetaStatus entity meta status
     */
    @Command
    public void editEntities(@BindingParam("entityMetaStatus") EntityMetaStatus entityMetaStatus) {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "entity.edit_entities");
        Executions.getCurrent().setAttribute("entityMetaKey", entityMetaStatus.getEntityMeta().getKey());
        Executions.getCurrent().setAttribute("key", entityMetaStatus.getEntityMeta().getKey());
        Executions.getCurrent().setAttribute("entityMetaName", entityMetaStatus.getEntityMeta().getName().getDescription(userProfile.getLocale()));
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Changes filter.
     */
    @Command
    public void changeFilter() {
        entityMetas = null;
        entityMetaGroups = null;
        entityMetaTreeModel = null;
        userProfile.setEntityMetaFilterByKey(EntityType.PREDEFINED_DICTIONARY.name(), entityMetaFilter);
        BindUtils.postNotifyChange(null, null, this, "entityMetaTreeModel");
    }

    /**
     * Returns entity meta filter.
     * @return entity meta filter
     */
    public String getEntityMetaFilter() {
        return entityMetaFilter;
    }

    /**
     * Sets entity meta filter.
     * @param entityMetaFilter entity meta filter
     */
    public void setEntityMetaFilter(String entityMetaFilter) {
        this.entityMetaFilter = entityMetaFilter;
    }
}
