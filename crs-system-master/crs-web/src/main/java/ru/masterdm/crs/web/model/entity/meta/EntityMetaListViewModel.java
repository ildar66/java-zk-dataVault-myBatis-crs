package ru.masterdm.crs.web.model.entity.meta;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import ru.masterdm.crs.domain.entity.BusinessAction;
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
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.SecurityService;
import ru.masterdm.crs.web.domain.UserProfile;
import ru.masterdm.crs.web.domain.entity.EntityMetaStatus;
import ru.masterdm.crs.web.model.entity.EntityMetaTreeModel;
import ru.masterdm.crs.web.service.EntityMetaUiService;

/**
 * Entity meta list view model class.
 * @author Igor Matushak
 * @author Alexey Kirilchev
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EntityMetaListViewModel {

    @WireVariable("userProfile")
    protected UserProfile userProfile;
    @WireVariable
    protected SecurityService securityService;

    @WireVariable
    private EntityMetaService entityMetaService;
    @WireVariable
    private EntityService entityService;
    @WireVariable("pages")
    private Properties pages;
    @WireVariable("webConfig")
    private Properties webConfig;
    @WireVariable
    protected EntityMetaUiService entityMetaUiService;

    protected EntityMetaTreeModel entityMetaTreeModel;
    protected String entityMetaFilter;
    protected List<EntityMetaGroup> entityMetaGroups;
    private EntityMeta entityMetaGroupMeta;
    protected LocalDateTime actuality;

    /**
     * Initiates context.
     */
    @Init
    public void initSetup() {
        entityMetaFilter = userProfile.getEntityMetaFilterByKey(getEntityType().name());
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
    protected EntityMetaStatus getRootEntityMetaStatus() {
        List<EntityMeta> entityMetaList = getEntityMetaList();

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
                         .addReferenceItem(groupEntityTypeMeta, new WhereItem(entityTypeMeta.getKeyAttribute(), Operator.EQ, getEntityType()));
            groupCriteria.getOrder().addItem(new OrderItem(groupViewOrderMeta, false));
            entityMetaGroups = entityMetaService.getEntityMetaGroups(groupCriteria, null, actuality);
        }
        return entityMetaGroups;
    }

    /**
     * Get entity meta list.
     * @return entity meta list
     */
    private List<EntityMeta> getEntityMetaList() {
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(EntityMeta.METADATA_KEY, null);
        Criteria criteria = new Criteria();
        String nameKey = userProfile.getLocale().equals(AttributeLocale.RU) ? EntityMeta.EntityMetaAttributeMeta.NAME_RU.getKey()
                                                                            : EntityMeta.EntityMetaAttributeMeta.NAME_EN.getKey();
        if (entityMetaFilter != null && !entityMetaFilter.trim().isEmpty()) {
            AttributeMeta attributeMetadata = entityMeta.getAttributeMetadata(nameKey);
            String filter = entityMetaFilter.trim().toUpperCase();
            criteria.getWhere().addItem(new WhereItem(entityMeta.getKeyAttribute(), Operator.LIKE, "%" + filter + "%"));
            criteria.getWhere().addItem(new WhereItem(Conjunction.OR, attributeMetadata, Operator.LIKE, "%" + filter + "%"));
        }

        addSort(criteria);
        return entityMetaService.getEntityMetas(criteria, null, actuality, getEntityType());
    }

    /**
     * Add entity meta list sort.
     * @param criteria criteria
     */
    protected void addSort(Criteria criteria) {
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(EntityMeta.METADATA_KEY, null);
        AttributeMeta viewOrderMetadata = entityMeta.getAttributeMetadata(EntityMeta.EntityMetaAttributeMeta.VIEW_ORDER.getKey());
        criteria.getOrder().addItem(viewOrderMetadata, false);
    }

    /**
     * Edits entity meta.
     * @param entityMetaStatus entity meta status
     */
    @Command
    public void editEntityMeta(@BindingParam("entityMetaStatus") EntityMetaStatus entityMetaStatus) {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", getTargetPageKey());
        String key = entityMetaStatus != null ? entityMetaStatus.getEntityMeta().getKey() : null;
        Executions.getCurrent().setAttribute("entityMetaKey", key);
        Executions.getCurrent().setAttribute("key", key);
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Edits entity meta group.
     * @param entityMetaStatus entity meta status
     */
    @Command
    public void editEntityMetaGroup(@BindingParam("entityMetaStatus") EntityMetaStatus entityMetaStatus) {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", getEntityMetaGroupPageKey());
        String key = entityMetaStatus != null ? entityMetaStatus.getGroup().getKey() : null;
        Executions.getCurrent().setAttribute("entityMetaGroupKey", key);
        Executions.getCurrent().setAttribute("key", key);
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Changes filter.
     */
    @Command
    public void changeFilter() {
        entityMetaTreeModel = null;
        userProfile.setEntityMetaFilterByKey(getEntityType().name(), entityMetaFilter);
        BindUtils.postNotifyChange(null, null, this, "entityMetaTreeModel");
        BindUtils.postNotifyChange(null, null, this, "totalSize");
    }

    /**
     * Returns entity meta name.
     * @return entity meta name
     */

    public String getEntityMetaFilter() {
        return entityMetaFilter;
    }

    /**
     * Sets entity meta name.
     * @param entityMetaFilter entity meta name
     */
    public void setEntityMetaFilter(String entityMetaFilter) {
        this.entityMetaFilter = entityMetaFilter;
    }

    /**
     * Returns entity type.
     * @return entity type
     */
    public EntityType getEntityType() {
        return EntityType.DICTIONARY;
    }

    /**
     * Returns target page.
     * @return target page
     */
    protected String getTargetPageKey() {
        return "entity.meta.entity_meta";
    }

    /**
     * Returns target page.
     * @return target page
     */
    protected String getEntityMetaGroupPageKey() {
        return "entity.meta.entity_meta_group";
    }

    /**
     * Change element group or group order.
     * @param dropEvent drop event
     * @param groupDraggedTo group dragged to
     * @param entityMetaToDraggedBefore entity meta to dragged before
     */
    @Command
    @NotifyChange("*")
    public void changeElementGroupOrGroupOrder(@ContextParam(ContextType.TRIGGER_EVENT) DropEvent dropEvent,
                                               @BindingParam("groupDraggedTo") EntityMetaGroup groupDraggedTo,
                                               @BindingParam("entityMetaToDraggedBefore") EntityMeta entityMetaToDraggedBefore) {
        Component dragged = dropEvent.getDragged();
        EntityMetaStatus entityMetaStatus = (EntityMetaStatus) dragged.getAttribute("entityMetaStatus");
        if (entityMetaStatus == null)
            throw new CrsException("entity meta status attribute is empty");
        List<EntityMetaGroup> groups = getEntityMetaGroups(getEntityMetaGroupMeta());
        if (entityMetaStatus.getGroup() != null) {
            if (groupDraggedTo == null)
                throw new CrsException("groupDraggedTo attribute is empty");
            entityMetaService.changeGroupOrder(groups, entityMetaStatus.getGroup(), groupDraggedTo);
        } else if (entityMetaStatus.getEntityMeta() != null) {
            if (groupDraggedTo != null)
                entityMetaService.moveEntityMetaToGroup(groups, groupDraggedTo, entityMetaStatus.getEntityMeta());
            else if (entityMetaToDraggedBefore != null)
                entityMetaService.moveEntityMetaBeforeAnotherEntityMeta(groups, entityMetaToDraggedBefore,
                                                                        entityMetaStatus.getEntityMeta(), getEntityType());
            else
                throw new CrsException("both group dragged to and entity meta dragged before are null");
        } else
            throw new CrsException("entity meta and entity meta group attributes are both empty");
        entityMetaGroups = null;
        entityMetaTreeModel = null;
    }

    /**
     * Returns is entity meta create allowed.
     * @return is entity meta create allowed
     */
    public boolean isEntityMetaCreateAllowed() {
        return securityService.isPermitted(securityService.getCurrentUser(),
                                           entityMetaService.getEntityMetaByKey(EntityMeta.METADATA_KEY, null),
                                           BusinessAction.Action.CREATE_NEW);
    }
}
