package ru.masterdm.crs.web.model.entity.meta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Messagebox;

import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.OrderItem;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMetaGroup;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.domain.entity.meta.EntityTypeAttributeMeta;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.web.domain.UserProfile;

/**
 * Entity meta view group model class.
 * @author Alexey Kirilchev
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EntityMetaGroupViewModel {

    @WireVariable
    protected EntityService entityService;
    @WireVariable("config")
    protected Properties config;
    @WireVariable("pages")
    protected Properties pages;
    @WireVariable("userProfile")
    protected UserProfile userProfile;
    @WireVariable
    protected EntityMetaService entityMetaService;

    protected EntityMetaGroup entityMetaGroup;
    protected String entityMetaGroupKey;
    private EntityMeta entityMetaGroupMeta;

    /**
     * Initiates context.
     * @param entityMetaGroupKey entity meta group key
     */
    @Init
    public void initSetup(@ExecutionParam("entityMetaGroupKey") String entityMetaGroupKey) {
        if (entityMetaGroupKey == null) {
            this.entityMetaGroupKey = (String) Executions.getCurrent().getAttribute("key");
        } else
            this.entityMetaGroupKey = entityMetaGroupKey;
    }

    /**
     * Returns true if addition form, false otherwise.
     * @return true if addition form, false otherwise
     */
    public boolean isAdd() {
        return entityMetaGroupKey == null;
    }

    /**
     * Returns entity type.
     * @return entity type
     */
    protected EntityType getEntityType() {
        return EntityType.DICTIONARY;
    }

    /**
     * Returns entity meta.
     * @return entity meta
     */
    public EntityMetaGroup getEntityMetaGroup() {
        if (entityMetaGroup == null) {
            EntityMeta entityMetaGroupMeta = getEntityMetaGroupMeta();
            if (isAdd()) {
                entityMetaGroup = (EntityMetaGroup) entityService.newEmptyEntity(entityMetaGroupMeta);
                entityMetaGroup.setName(new MultilangDescription());
                entityMetaGroup.setType(getEntityType());
            } else {
                Criteria criteria = new Criteria();
                criteria.getWhere().addItem(new WhereItem(entityMetaGroupMeta.getKeyAttribute(), Operator.EQ, entityMetaGroupKey));
                List<EntityMetaGroup> entityMetaGroupList = entityMetaService.getEntityMetaGroups(criteria, null, null);
                if (entityMetaGroupList.isEmpty())
                    throw new CrsException("can not found entity meta group by key '" + entityMetaGroupKey + "'");
                entityMetaGroup = entityMetaGroupList.get(0);
            }
        }
        return entityMetaGroup;
    }

    /**
     * Shows entity meta group view model page.
     */
    private void showEntityMetaGroupViewModel() {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", getTargetPageKey());
        Executions.getCurrent().setAttribute("entityMetaGroupKey", entityMetaGroup.getKey());
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Returns entity meta group meta.
     * @return entity meta group meta
     */
    private EntityMeta getEntityMetaGroupMeta() {
        if (entityMetaGroupMeta == null) {
            entityMetaGroupMeta = entityMetaService.getEntityMetaByKey(EntityMetaGroup.METADATA_KEY, null);
        }
        return entityMetaGroupMeta;
    }

    /**
     * Persists entity meta.
     */
    @Command
    public void persistEntityMetaGroup() {
        getEntityMetaGroup().setKey(getEntityMetaGroup().getKey().trim().toUpperCase());
        if (isAdd() && entityMetaService.isEntityMetaGroupExists(entityMetaGroup.getKey())) {
            Messagebox.show(Labels.getLabel("entity_meta_group_duplicate_key_message"), Labels.getLabel("messagebox_validation"), Messagebox.OK,
                            Messagebox.EXCLAMATION);
            return;
        }
        if (isAdd()) {
            Long minViewOrder = getEntityMetaGroups().stream().map(EntityMetaGroup::getViewOrder).min(Long::compare).get();
            entityMetaGroup.setViewOrder(--minViewOrder);
        }
        entityMetaService.persistEntityMetaGroup(entityMetaGroup);
        if (isAdd())
            showEntityMetaGroupViewModel();
        Clients.showNotification(Labels.getLabel("data_saved"));
    }

    /**
     * Returns entity meta groups.
     * @return entity meta groups
     */
    private List<EntityMetaGroup> getEntityMetaGroups() {
        EntityMeta entityMetaGroupMeta = getEntityMetaGroupMeta();
        EntityMeta entityTypeMeta = entityMetaService.getEntityMetaByKey(EntityTypeAttributeMeta.METADATA_KEY, null);
        AttributeMeta groupEntityTypeMeta =
                entityMetaGroupMeta.getAttributeMetadata(EntityMetaGroup.EntityMetaGroupAttributeMeta.ENTITY_TYPE.getKey());
        AttributeMeta groupViewOrderMeta =
                entityMetaGroupMeta.getAttributeMetadata(EntityMetaGroup.EntityMetaGroupAttributeMeta.VIEW_ORDER.getKey());
        Criteria groupCriteria = new Criteria();
        groupCriteria.getWhere()
                     .addReferenceItem(groupEntityTypeMeta, new WhereItem(entityTypeMeta.getKeyAttribute(), Operator.EQ, getEntityType()));
        groupCriteria.getOrder().addItem(new OrderItem(groupViewOrderMeta, false));
        return entityMetaService.getEntityMetaGroups(groupCriteria, null, null);
    }

    /**
     * Removes entity meta.
     * @param entityMetaId entity meta identifier
     */
    @Command
    public void removeEntityMetaGroup(@BindingParam("entityMetaId") Long entityMetaId) {
        EventListener<Messagebox.ClickEvent> clickListener = event -> {
            if (Messagebox.Button.YES.equals(event.getButton())) {
                entityMetaService.removeEntityMetaGroup(entityMetaGroup);
                navigateEntityMetaList();
            }
        };
        Messagebox.show(Labels.getLabel("entity_meta_list_remove_entity_meta_message"),
                        Labels.getLabel("entity_meta_list_remove_entity_meta_title"),
                        new Messagebox.Button[] {Messagebox.Button.YES, Messagebox.Button.NO}, Messagebox.QUESTION, clickListener);
    }

    /**
     * Returns entity meta key constraint.
     * @return entity meta key constraint
     */
    public String getEntityKeyConstraint() {
        return String.format("no empty,/%s/", config.getProperty("db.entity.meta.key.regexp"));
    }

    /**
     * Returns target page.
     * @return target page
     */
    protected String getTargetPageKey() {
        return "entity.meta.entity_meta_group";
    }

    /**
     * Returns return page.
     * @return return page
     */
    protected String getReturnPageKey() {
        return "entity.meta.entity_meta_list";
    }

    /**
     * Navigates entity meta list.
     */
    @Command
    public void navigateEntityMetaList() {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", getReturnPageKey());
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Returns meta list title.
     * @return meta list title
     */
    public String getMetaListTitle() {
        return Labels.getLabel("entity_meta_list_title");
    }

    /**
     * Returns list title for additing.
     * @return list title for additing
     */
    public String getMetaListTitleAdd() {
        return Labels.getLabel("add_entity_meta_group_title");
    }

    /**
     * Returning meta list title for editing.
     * @return meta list title for editing
     */
    public String getMetaListTitleEdit() {
        return Labels.getLabel("edit_entity_meta_group_title");
    }

    /**
     * Returns list title.
     * @return list title
     */
    public String getListTitle() {
        return Labels.getLabel("layout_entity_list_title");
    }

    /**
     * Returns true if can remove, false otherwise.
     * @return true if can remove, false otherwise
     */
    public boolean isCanRemove() {
        boolean result = !isAdd();
        if (result && entityMetaGroup != null && entityMetaGroup.getKey() != null)
            result = !EntityMetaGroup.DefaultGroup.isHasValue(entityMetaGroup.getKey());
        return result;
    }
}
