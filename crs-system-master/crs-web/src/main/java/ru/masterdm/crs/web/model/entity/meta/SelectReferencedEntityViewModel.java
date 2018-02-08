package ru.masterdm.crs.web.model.entity.meta;

import java.util.List;
import java.util.stream.Collectors;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;

import ru.masterdm.crs.domain.entity.criteria.Conjunction;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeLocale;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.web.domain.UserProfile;

/**
 * Select attribute reference view model class.
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SelectReferencedEntityViewModel {

    @WireVariable
    private EntityService entityService;
    @WireVariable
    private EntityMetaService entityMetaService;
    @WireVariable("userProfile")
    private UserProfile userProfile;

    private AttributeMeta attributeMeta;
    private ListModelList<EntityMeta> entityMetaList;
    private EntityMeta selectedEntityMeta;
    private AttributeMeta selectedAttributeMeta;
    private String entityMetaKey;

    private String entityMetaFilter;
    private String attributeMetaFilter;

    /**
     * Initiates context.
     * @param attributeMeta attribute meta
     * @param entityMetaKey entity meta key
     */
    @Init
    public void initSetup(@ExecutionArgParam("attribute") AttributeMeta attributeMeta, @ExecutionArgParam("entityMetaKey") String entityMetaKey) {
        this.attributeMeta = attributeMeta;
        this.entityMetaKey = entityMetaKey;
    }

    /**
     * Detaches window.
     * @param view view
     */
    @Command
    public void detachWindow(@ContextParam(ContextType.VIEW) Component view) {
        view.detach();
        resetAttributeType();
    }

    /**
     * Resets attribute type to empty.
     */
    private void resetAttributeType() {
        attributeMeta.setType(null);
        BindUtils.postGlobalCommand(null, null, "attributesRefresh", null);
    }

    /**
     * Selects referenced entity.
     * @param view view
     */
    @Command
    public void selectReferencedEntity(@ContextParam(ContextType.VIEW) Component view) {
        if (selectedEntityMeta == null) {
            Messagebox.show(Labels.getLabel("select_referenced_entity_not_selected_entity_meta_message"),
                            Labels.getLabel("messagebox_validation"), Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }
        if (selectedAttributeMeta == null) {
            Messagebox.show(Labels.getLabel("select_referenced_entity_not_selected_attribute_meta_message"),
                            Labels.getLabel("messagebox_validation"), Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }
        attributeMeta.setEntityKey(selectedEntityMeta.getKey());
        attributeMeta.setAttributeKey(selectedAttributeMeta.getKey());
        BindUtils.postGlobalCommand(null, null, "attributesRefresh", null);
        view.detach();
    }

    /**
     * Returns entity meta list.
     * @return entity meta list
     */
    public List<EntityMeta> getEntityMetaList() {
        if (entityMetaList == null) {
            entityMetaList = new ListModelList<>();
            EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(EntityMeta.METADATA_KEY, null);
            Criteria criteria = new Criteria();

            AttributeMeta viewOrderMetadata = entityMeta.getAttributeMetadata(EntityMeta.EntityMetaAttributeMeta.VIEW_ORDER.getKey());
            String nameKey = userProfile.getLocale().equals(AttributeLocale.RU) ? EntityMeta.EntityMetaAttributeMeta.NAME_RU.getKey()
                                                                                : EntityMeta.EntityMetaAttributeMeta.NAME_EN.getKey();

            if (entityMetaFilter != null && !entityMetaFilter.isEmpty()) {
                criteria.getWhere().addItem(new WhereItem(entityMeta.getKeyAttribute(), Operator.LIKE, "%" + entityMetaFilter.toUpperCase() + "%"));
                criteria.getWhere().addItem(new WhereItem(Conjunction.OR, entityMeta.getAttributeMetadata(nameKey), Operator.LIKE,
                                                          "%" + entityMetaFilter + "%"));
            }

            if (entityMetaKey != null && !entityMetaKey.isEmpty()) {
                criteria.getWhere().addItem(new WhereItem(entityMeta.getKeyAttribute(), Operator.NOT_IN, entityMetaKey));
            }
            criteria.getOrder().addItem(viewOrderMetadata, false);
            entityMetaList = new ListModelList<>(entityMetaService.getEntityMetas(criteria, null, null, EntityType.DICTIONARY));
        }
        return entityMetaList;
    }

    /**
     * Returns attributes.
     * @return attributes
     */
    public List<AttributeMeta> getAttributes() {
        List<AttributeMeta> attributeMetaList = null;
        if (selectedEntityMeta != null) {
            attributeMetaList = entityMetaService.getEntityMetaByKey(selectedEntityMeta.getKey(), null)
                                                 .getAttributes()
                                                 .stream()
                                                 .filter(p -> p.getType() != AttributeType.REFERENCE)
                                                 .collect(Collectors.toList());

            if (attributeMetaFilter != null && !attributeMetaFilter.isEmpty()) {
                attributeMetaList = attributeMetaList.stream()
                                                     .filter(p -> p.getKey().contains(attributeMetaFilter.toUpperCase())
                                                                  || p.getName().getDescription(userProfile.getLocale()).toUpperCase()
                                                                      .contains(attributeMetaFilter.toUpperCase()))
                                                     .collect(Collectors.toList());
            }
        }
        return attributeMetaList;
    }

    /**
     * Selects entity meta.
     * @param entityMeta entity meta
     */
    @Command
    public void selectEntityMeta(@BindingParam("entityMeta") EntityMeta entityMeta) {
        selectedEntityMeta = entityMeta;
        BindUtils.postNotifyChange(null, null, this, "attributes");
    }

    /**
     * Selects attribute meta.
     * @param attributeMeta attribute meta
     */
    @Command
    public void selectAttributeMeta(@BindingParam("attributeMeta") AttributeMeta attributeMeta) {
        selectedAttributeMeta = attributeMeta;
    }

    /**
     * Changes entity meta filter.
     */
    @Command
    public void changeEntityMetaFilter() {
        entityMetaList = null;
        selectedEntityMeta = null;
        selectedAttributeMeta = null;
        BindUtils.postNotifyChange(null, null, this, "entityMetaList");
        BindUtils.postNotifyChange(null, null, this, "attributes");
    }

    /**
     * Changes attribute meta filter.
     */
    @Command
    @NotifyChange("attributes")
    public void changeAttributeMetaFilter() {
        selectedAttributeMeta = null;
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

    /**
     * Returns attribute meta filter.
     * @return attribute meta filter
     */
    public String getAttributeMetaFilter() {
        return attributeMetaFilter;
    }

    /**
     * Sets attribute meta filter.
     * @param attributeMetaFilter attribute meta filter
     */
    public void setAttributeMetaFilter(String attributeMetaFilter) {
        this.attributeMetaFilter = attributeMetaFilter;
    }
}
