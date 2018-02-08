package ru.masterdm.crs.web.model.entity.meta;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.entity.BusinessAction;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.OrderItem;
import ru.masterdm.crs.domain.entity.criteria.Where;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.CalculationProfileAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMetaGroup;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.domain.entity.meta.EntityTypeAttributeMeta;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.SecurityService;
import ru.masterdm.crs.web.domain.UserProfile;

/**
 * Entity meta view model class.
 * @author Igor Matushak
 * @author Alexey Kirilchev
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EntityMetaViewModel {

    @WireVariable
    protected EntityMetaService entityMetaService;
    @WireVariable
    protected EntityService entityService;
    @WireVariable
    protected SecurityService securityService;

    @WireVariable("config")
    private Properties config;
    @WireVariable("pages")
    private Properties pages;
    @WireVariable("userProfile")
    private UserProfile userProfile;

    protected EntityMeta entityMeta;
    protected String entityMetaKey;
    protected String entityMetaGroupName;

    private Map<AttributeType, String> attributeTypes;

    /**
     * Initiates context.
     * @param entityMetaKey entity meta key
     */
    @Init
    public void initSetup(@ExecutionParam("entityMetaKey") String entityMetaKey) {
        if (entityMetaKey == null) {
            this.entityMetaKey = (String) Executions.getCurrent().getAttribute("key");
        } else
            this.entityMetaKey = entityMetaKey;
    }

    /**
     * Returns true if addition form, false otherwise.
     * @return true if addition form, false otherwise
     */
    public boolean isAdd() {
        return entityMetaKey == null;
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
    public EntityMeta getEntityMeta() {
        if (entityMeta == null) {
            if (isAdd()) {
                entityMeta = new EntityMeta();
                entityMeta.setAttributes(new ArrayList<>());
                entityMeta.setTypes(Arrays.asList(getEntityType()));
                entityMeta.setName(new MultilangDescription());
                entityMeta.setComment(new MultilangDescription());

                AttributeMeta attributeMeta = new AttributeMeta();
                attributeMeta.setName(new MultilangDescription());
                attributeMeta.setLdts(LocalDateTime.now().plusNanos(1));
                entityMeta.getAttributes().add(attributeMeta);
            } else {
                entityMeta = entityMetaService.getEntityMetaByKeyNoCache(entityMetaKey, null);
                if (entityMeta.getComment() == null) {
                    entityMeta.setComment(new MultilangDescription());
                }
            }
        }
        return entityMeta;
    }

    /**
     * Returns true if can remove attribute meta, false otherwise.
     * @param attributeMeta attribute meta
     * @return true if can remove attribute meta, false otherwise
     */
    public boolean changeAttributeMetaDisabled(AttributeMeta attributeMeta) {
        return attributeMeta.getEntityKey() != null
               && (attributeMeta.getEntityKey().equals(Calculation.METADATA_KEY)
                   || attributeMeta.getEntityKey().equals(CalculationProfileAttributeMeta.METADATA_KEY));
    }

    /**
     * Returns true if can change attribute meta key, false otherwise.
     * @param attributeMeta attribute meta
     * @return true if can change attribute meta key, false otherwise
     */
    public boolean changeAttributeMetaKeyDisabled(AttributeMeta attributeMeta) {
        return attributeMeta.getId() != null
               || (attributeMeta.getEntityKey() != null
                   && (attributeMeta.getEntityKey().equals(Calculation.METADATA_KEY)
                       || attributeMeta.getEntityKey().equals(CalculationProfileAttributeMeta.METADATA_KEY)));
    }

    /**
     * Shows edit entity meta view model page.
     */
    private void showEditEntityMetaViewModel() {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", getTargetPageKey());
        Executions.getCurrent().setAttribute("entityMetaKey", entityMeta.getKey());
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Persists entity meta.
     */
    @Command
    public void persistEntityMeta() {
        if (isAdd() && entityMetaService.isEntityMetaExists(entityMeta.getKey())) {
            Messagebox.show(Labels.getLabel("entity_meta_duplicate_key_message"), Labels.getLabel("messagebox_validation"), Messagebox.OK,
                            Messagebox.EXCLAMATION);
            return;
        }
        getEntityMeta().setKey(getEntityMeta().getKey().trim().toUpperCase());
        getAttributes().stream()
                       .filter(attribute -> attribute.getId() == null)
                       .forEach(attribute -> {
                           attribute.setNativeColumn(attribute.getKey().trim().toUpperCase());
                           attribute.setKey(entityMetaService.getAttributeMetaKey(getEntityMeta(), attribute.getKey().trim().toUpperCase()));
                       });

        Long distinctKeySize = entityMeta.getAttributes().stream().map(AttributeMeta::getKey).distinct().count();
        if (distinctKeySize != entityMeta.getAttributes().size()) {
            Messagebox.show(Labels.getLabel("entity_meta_duplicate_attribute_keys_message"), Labels.getLabel("messagebox_validation"),
                            Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        entityMetaService.persistEntityMeta(entityMeta);
        userProfile.setFiltersByKey(getEntityMeta().getKey(), null);
        if (!isAdd()) {
            BindUtils.postNotifyChange(null, null, this, "attributes");
        } else {
            showEditEntityMetaViewModel();
        }
        Clients.showNotification(Labels.getLabel("data_saved"));
    }

    /**
     * Removes entity meta.
     * @param entityMeta entity meta
     */
    @Command({"removeEntityMeta"})
    public void removeEntityMeta(@BindingParam("entityMeta") EntityMeta entityMeta) {
        EventListener<Messagebox.ClickEvent> clickListener = event -> {
            if (Messagebox.Button.YES.equals(event.getButton())) {
                entityMetaService.removeEntityMeta(entityMeta);
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
     * Returns attribute meta key constraint.
     * @return attribute meta key constraint
     */
    public String getAttributeNativeColumnConstraint() {
        return String.format("no empty,/%s/", config.getProperty("db.attribute.meta.native.column.regexp"));
    }

    /**
     * Returns attribute types.
     * @return attribute types
     */
    public Map<AttributeType, String> getAttributeTypes() {
        if (attributeTypes == null) {
            attributeTypes = Stream.of(AttributeType.values())
                                   .collect(Collectors.toMap(at -> at, at -> Labels.getLabel(at.name())));
        }
        return attributeTypes;
    }

    /**
     * Returns attributes.
     * @return attributes
     */
    public List<AttributeMeta> getAttributes() {
        return getEntityMeta().getAttributes();
    }

    /**
     * Adds attribute meta.
     */
    @Command
    public void addAttributeMeta() {
        AttributeMeta attributeMeta = new AttributeMeta();
        MultilangDescription name = new MultilangDescription();
        attributeMeta.setName(name);
        attributeMeta.setLdts(LocalDateTime.now());
        entityMeta.getAttributes().add(attributeMeta);
        BindUtils.postNotifyChange(null, null, this, "attributes");
    }

    /**
     * Removes attribute meta.
     * @param attributeMeta attribute meta
     */
    @Command({"removeAttributeMeta"})
    public void removeAttributeMeta(@BindingParam("attributeMeta") AttributeMeta attributeMeta) {
        if (getAttributes().size() == 1) {
            Messagebox.show(Labels.getLabel("entity_meta_attributes_empty_message"), Labels.getLabel("messagebox_validation"), Messagebox.OK,
                            Messagebox.EXCLAMATION);
            return;
        }
        List<AttributeMeta> filtered = getAttributes().stream()
                                                      .filter(am -> attributeMeta.getId() != null
                                                                    ? !(am.getId() != null && am.getId().equals(attributeMeta.getId()))
                                                                    : !attributeMeta.getLdts().equals(am.getLdts()))
                                                      .collect(Collectors.toList());
        getEntityMeta().setAttributes(filtered);
        BindUtils.postNotifyChange(null, null, this, "attributes");
    }

    /**
     * Changes attribute type.
     * @param attribute attribute
     */
    @Command
    public void changeAttributeType(@BindingParam("attribute") AttributeMeta attribute) {
        if (attribute.getType() == AttributeType.REFERENCE) {
            Map<String, Object> map = new HashMap<>();
            map.put("attribute", attribute);
            if (getEntityMeta().getId() != null) {
                map.put("entityMetaKey", getEntityMeta().getKey());
            }
            Window window = (Window) Executions.createComponents(pages.getProperty("entity.meta.select_referenced_entity"), null, map);
            window.doModal();
        } else {
            attribute.setEntityKey(null);
            attribute.setAttributeKey(null);
            BindUtils.postNotifyChange(null, null, this, "attributes");
        }
    }

    /**
     * Reverts reference type.
     */
    @GlobalCommand
    public void attributesRefresh() {
        BindUtils.postNotifyChange(null, null, this, "attributes");
    }

    /**
     * Returns key validator.
     * @return key validator
     */
    public Validator getKeyValidator() {
        return new AbstractValidator() {

            @Override
            public void validate(ValidationContext ctx) {
                String value = (String) ctx.getProperty().getValue();
                if (StringUtils.isEmpty(value)) {
                    return;
                }

                String attributeKey = entityMetaService.getAttributeMetaKey(entityMeta, value);
                if (entityMetaService.isAttributeMetaExists(attributeKey)) {
                    throw new WrongValueException(Labels.getLabel("entity_meta_attribute_key_exists_message"));
                }

                if (getAttributes().stream().noneMatch(p -> p.getKey() == null)) {
                    return;
                }

                if (getAttributes().stream().anyMatch(p -> value.equals(p.getKey()))) {
                    throw new WrongValueException(Labels.getLabel("entity_meta_duplicate_attribute_keys_message"));
                }
            }
        };
    }

    /**
     * Returns target page.
     * @return target page
     */
    protected String getTargetPageKey() {
        return "entity.meta.entity_meta";
    }

    /**
     * Returns return page.
     * @return return page
     */
    protected String getReturnPageKey() {
        return "entity.meta.entity_meta_list";
    }

    /**
     * Returns true if needs hide UI multilang and filter fields, false otherwise.
     * @return true if needs hide UI multilang and filter fields, false otherwise
     */
    public boolean isNotFormFieldHidden() {
        return false;
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
     * Returns entity meta group name.
     * @return entity meta group name
     */
    public String getEntityMetaGroupName() {
        if (entityMetaGroupName == null) {
            EntityMeta groupMeta = entityMetaService.getEntityMetaByKey(EntityMetaGroup.METADATA_KEY, null);
            EntityMeta entityTypeMeta = entityMetaService.getEntityMetaByKey(EntityTypeAttributeMeta.METADATA_KEY, null);
            AttributeMeta groupEntityTypeMeta = groupMeta.getAttributeMetadata(EntityMetaGroup.EntityMetaGroupAttributeMeta.ENTITY_TYPE.getKey());
            AttributeMeta groupEntityMeta = groupMeta.getAttributeMetadata(EntityMetaGroup.EntityMetaGroupAttributeMeta.ENTITY.getKey());
            AttributeMeta groupViewOrderMeta = groupMeta.getAttributeMetadata(EntityMetaGroup.EntityMetaGroupAttributeMeta.VIEW_ORDER.getKey());
            Criteria groupCriteria = new Criteria();
            Where where = groupCriteria.getWhere();
            if (isAdd()) {
                where.addItem(new WhereItem(groupMeta.getKeyAttribute(), Operator.EQ,
                                            EntityMetaGroup.DefaultGroup.getDefaultGroupKey(getEntityType()).name()));
            } else {
                where.addReferenceItem(groupEntityMeta, new WhereItem(getEntityMeta().getKeyAttribute(), Operator.EQ, entityMetaKey));
            }
            where.addReferenceItem(groupEntityTypeMeta, new WhereItem(entityTypeMeta.getKeyAttribute(), Operator.EQ, getEntityType()));
            groupCriteria.getOrder().addItem(new OrderItem(groupViewOrderMeta, false));
            List<EntityMetaGroup> entityMetaGroups = entityMetaService.getEntityMetaGroups(groupCriteria, null, null);
            EntityMetaGroup entityMetaGroup = entityMetaGroups.get(0);
            entityMetaGroupName = String.format("%s (%s)",
                                                entityMetaGroup.getName().getDescription(userProfile.getLocale()),
                                                entityMetaGroup.getKey());
        }
        return entityMetaGroupName;
    }

    /**
     * Returns is entity meta edit allowed.
     * @return is entity meta edit allowed
     */
    public boolean isEntityMetaEditAllowed() {
        return securityService.isPermitted(securityService.getCurrentUser(),
                                           entityMetaService.getEntityMetaByKey(EntityMeta.METADATA_KEY, null),
                                           BusinessAction.Action.EDIT);
    }
}