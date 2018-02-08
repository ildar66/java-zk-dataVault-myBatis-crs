package ru.masterdm.crs.web.model.entity.meta;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.BooleanUtils;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.calc.ClassifierAttributeMeta;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.OrderItem;
import ru.masterdm.crs.domain.entity.criteria.Where;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMetaGroup;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.domain.entity.meta.EntityTypeAttributeMeta;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.ValueConvertService;
import ru.masterdm.crs.web.domain.UserProfile;

/**
 * Classifier meta view model class.
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ClassifierMetaViewModel {

    @WireVariable
    private EntityMetaService entityMetaService;
    @WireVariable
    private ValueConvertService valueConvertService;
    @WireVariable("config")
    private Properties config;
    @WireVariable("pages")
    private Properties pages;
    @WireVariable("userProfile")
    private UserProfile userProfile;

    private Map<AttributeType, String> attributeTypes;
    private EntityMeta entityMeta;
    private String entityMetaKey;
    private boolean showDefaultValueContainer;
    private LocalDateTime dateTimeDefault;
    private LocalDate dateDefault;
    private Boolean booleanDefault;
    protected String entityMetaGroupName;

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
        if (isAdd()) {
            entityMeta = entityMetaService.getEntityMetaPrototypeFactory(EntityType.CLASSIFIER).create(entityMetaKey);
            entityMeta.setName(new MultilangDescription());
            entityMeta.setComment(new MultilangDescription());

            AttributeMeta calculationAttributeMetaRef =
                    entityMeta.getAttributeMetadata(entityMetaService.getAttributeMetaKey(entityMeta, ClassifierAttributeMeta.CALC.name()));
            calculationAttributeMetaRef.setName(new MultilangDescription(Labels.getLabel("entity_meta_calc_ref_description_ru"),
                                                                         Labels.getLabel("entity_meta_calc_ref_description_en")));

            AttributeMeta attributeMeta =
                    entityMeta.getAttributeMetadata(entityMetaService.getAttributeMetaKey(entityMeta,
                                                                                          ClassifierAttributeMeta.CLASSIFIER_TYPE.name()));
            attributeMeta.setName(new MultilangDescription(Labels.getLabel("classifier_meta_classifier_type_description_ru"),
                                                           Labels.getLabel("classifier_meta_classifier_type_description_en")));

            attributeMeta =
                    entityMeta.getAttributeMetadata(entityMetaService.getAttributeMetaKey(entityMeta,
                                                                                          ClassifierAttributeMeta.CLASSIFIER_COMMENT.name()));
            attributeMeta.setName(new MultilangDescription(Labels.getLabel("classifier_meta_classifier_comment_description_ru"),
                                                           Labels.getLabel("classifier_meta_classifier_comment_description_en")));
            attributeMeta =
                    entityMeta.getAttributeMetadata(entityMetaService.getAttributeMetaKey(entityMeta,
                                                                                          ClassifierAttributeMeta.CALC_PROFILE.name()));
            attributeMeta.setName(new MultilangDescription(Labels.getLabel("classifier_meta_classifier_profile_description_ru"),
                                                           Labels.getLabel("classifier_meta_classifier_profile_description_en")));
        } else {
            entityMeta = entityMetaService.getEntityMetaByKeyNoCache(entityMetaKey, null);
            if (entityMeta.getComment() == null) {
                entityMeta.setComment(new MultilangDescription());
            }
        }
        if (getTypeAttributeMeta() != null && getTypeAttributeMeta().getDefaultValue() != null) {
            showDefaultValueContainer = true;
        }
    }

    /**
     * Returns true if addition form, false otherwise.
     * @return true if addition form, false otherwise
     */
    public boolean isAdd() {
        return entityMetaKey == null;
    }

    /**
     * Returns entity meta.
     * @return entity meta
     */
    public EntityMeta getEntityMeta() {
        return entityMeta;
    }

    /**
     * Returns type attribute meta.
     * @return type attribute meta
     */
    public AttributeMeta getTypeAttributeMeta() {
        String key = ClassifierAttributeMeta.CLASSIFIER_TYPE.name();
        if (!isAdd()) {
            key = entityMetaService.getAttributeMetaKey(entityMeta, key);
        }
        return entityMeta.getAttributeMetadata(key);
    }

    /**
     * Returns comment attribute meta.
     * @return comment attribute meta
     */
    public AttributeMeta getCommentAttributeMeta() {
        String key = ClassifierAttributeMeta.CLASSIFIER_COMMENT.name();
        if (!isAdd()) {
            key = entityMetaService.getAttributeMetaKey(entityMeta, key);
        }
        return entityMeta.getAttributeMetadata(key);
    }

    /**
     * Returns is reference entity deleted.
     * @return is reference entity deleted
     */
    public boolean isRefEntityDeleted() {
        if (getTypeAttributeMeta().getEntityKey() != null) {
            return entityMetaService.getEntityMetaByKeyNoCache(getTypeAttributeMeta().getEntityKey(), null) == null;
        }
        return false;
    }

    /**
     * Persists entity meta.
     */
    @Command
    public void persistEntityMeta() {
        if (isAdd() && entityMetaService.isEntityMetaExists(entityMeta.getKey())) {
            Messagebox.show(Labels.getLabel("entity_meta_duplicate_key_message"), Labels.getLabel("messagebox_validation"),
                            Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        if (getTypeAttributeMeta().getType() == AttributeType.BOOLEAN) {
            if (booleanDefault != null) {
                getTypeAttributeMeta().setDefaultValue(BooleanUtils.toIntegerObject(booleanDefault).toString());
            } else {
                getTypeAttributeMeta().setDefaultValue(null);
            }
        }

        if (getTypeAttributeMeta().getType() == AttributeType.DATE) {
            if (dateDefault != null) {
                getTypeAttributeMeta().setDefaultValue(dateDefault.format(DateTimeFormatter.ofPattern(Labels.getLabel("date_format"))));
            } else {
                getTypeAttributeMeta().setDefaultValue(null);
            }
        }

        if (getTypeAttributeMeta().getType() == AttributeType.DATETIME) {
            if (dateTimeDefault != null) {
                getTypeAttributeMeta().setDefaultValue(dateTimeDefault.format(DateTimeFormatter.ofPattern(Labels.getLabel("date_time_format"))));
            } else {
                getTypeAttributeMeta().setDefaultValue(null);
            }
        }

        entityMeta.setKey(getEntityMeta().getKey().trim().toUpperCase());
        entityMeta.getAttributes().stream()
                  .filter(attribute -> attribute.getId() == null)
                  .forEach(attribute -> {
                      attribute.setNativeColumn(attribute.getKey().trim().toUpperCase());
                      attribute.setKey(entityMetaService.getAttributeMetaKey(entityMeta, attribute.getKey().trim().toUpperCase()));
                  });
        entityMetaService.persistEntityMeta(entityMeta);
        userProfile.setFiltersByKey(getEntityMeta().getKey(), null);
        if (isAdd()) {
            showEditEntityMetaViewModel();
        }
        Clients.showNotification(Labels.getLabel("data_saved"));
    }

    /**
     * Removes entity meta.
     * @param entityMeta entity meta
     */
    @Command
    public void removeEntityMeta(@BindingParam("entityMeta") EntityMeta entityMeta) {
        EventListener<Messagebox.ClickEvent> clickListener = event -> {
            if (Messagebox.Button.YES.equals(event.getButton())) {
                entityMetaService.removeEntityMeta(entityMeta);
                navigateClassifierMetaList();
            }
        };
        Messagebox.show(Labels.getLabel("classifier_meta_remove_classifier_meta_message"),
                        Labels.getLabel("classifier_meta_remove_classifier_meta_title"),
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
     * Returns attribute types.
     * @return attribute types
     */
    public Map<AttributeType, String> getAttributeTypes() {
        if (attributeTypes == null) {
            attributeTypes = Stream.of(AttributeType.values())
                                   .filter(p -> p != AttributeType.FILE && p != AttributeType.STRING && p != AttributeType.TEXT)
                                   .collect(Collectors.toMap(at -> at, at -> Labels.getLabel(at.name())));
        }
        return attributeTypes;
    }

    /**
     * Changes attribute type.
     * @param attribute attribute
     */
    @Command
    public void changeAttributeType(@BindingParam("attribute") AttributeMeta attribute) {
        if (getTypeAttributeMeta().getType() == AttributeType.REFERENCE) {
            getTypeAttributeMeta().setDefaultValue(null);
            Map<String, Object> map = new HashMap<>();
            map.put("attribute", getTypeAttributeMeta());
            if (getEntityMeta().getId() != null) {
                map.put("entityMetaKey", getEntityMeta().getKey());
            }
            Window window = (Window) Executions.createComponents(pages.getProperty("entity.meta.select_referenced_entity"), null, map);
            window.doModal();
        } else {
            getTypeAttributeMeta().setEntityKey(null);
            getTypeAttributeMeta().setAttributeKey(null);
            BindUtils.postNotifyChange(null, null, this, "attributes");
        }
        refreshDefaultValuesPanel();
    }

    /**
     * Refreshes entities.
     */
    @GlobalCommand
    public void attributesRefresh() {
        BindUtils.postNotifyChange(null, null, this, "typeAttributeMeta");
    }

    /**
     * Shows edit entity meta view model page.
     */
    private void showEditEntityMetaViewModel() {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "entity.meta.classifier_meta");
        Executions.getCurrent().setAttribute("entityMetaKey", entityMeta.getKey());
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Navigates classifier meta list.
     */
    @Command
    public void navigateClassifierMetaList() {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "entity.meta.classifier_meta_list");
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Edits entities.
     */
    @Command
    public void editEntities() {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "entity.edit_entities");
        Executions.getCurrent().setAttribute("entityMetaKey", getTypeAttributeMeta().getEntityKey());
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Check show add default value button.
     * @return flag show add default value button
     */
    public boolean showAddDefaultValueButton() {
        return getTypeAttributeMeta().getType() != null && getTypeAttributeMeta().getType() != AttributeType.REFERENCE && !showDefaultValueContainer;
    }

    /**
     * Check show delete default value button.
     * @return flag show delete default value button
     */
    public boolean showDeleteDefaultValueButton() {
        return getTypeAttributeMeta().getType() != null && getTypeAttributeMeta().getType() != AttributeType.REFERENCE && showDefaultValueContainer;
    }

    /**
     * Adds default value.
     */
    @Command
    public void addDefaultValue() {
        showDefaultValueContainer = true;
        getTypeAttributeMeta().setDefaultValue(null);
        refreshDefaultValuesPanel();
    }

    /**
     * Deletes default value.
     */
    @Command
    public void deleteDefaultValue() {
        showDefaultValueContainer = false;
        booleanDefault = null;
        dateDefault = null;
        dateTimeDefault = null;
        getTypeAttributeMeta().setDefaultValue(null);
        refreshDefaultValuesPanel();
    }

    /**
     * Refreshed default values panel.
     */
    private void refreshDefaultValuesPanel() {
        BindUtils.postNotifyChange(null, null, this, "showAddDefaultValueButton");
        BindUtils.postNotifyChange(null, null, this, "showDeleteDefaultValueButton");
        BindUtils.postNotifyChange(null, null, this, "isDefaultValueTypeNumber");
        BindUtils.postNotifyChange(null, null, this, "isDefaultValueTypeBoolean");
        BindUtils.postNotifyChange(null, null, this, "isDefaultValueTypeDateTime");
        BindUtils.postNotifyChange(null, null, this, "isDefaultValueTypeDate");
    }

    /**
     * Check default value type is number.
     * @return flag default value type is number
     */
    public boolean isDefaultValueTypeNumber() {
        return showDeleteDefaultValueButton() && getTypeAttributeMeta().getType() == AttributeType.NUMBER;
    }

    /**
     * Check default value type is boolean.
     * @return flag default value type is boolean
     */
    public boolean isDefaultValueTypeBoolean() {
        return showDeleteDefaultValueButton() && getTypeAttributeMeta().getType() == AttributeType.BOOLEAN;
    }

    /**
     * Check default value type is date time.
     * @return flag default value type is date time
     */
    public boolean isDefaultValueTypeDateTime() {
        return showDeleteDefaultValueButton() && getTypeAttributeMeta().getType() == AttributeType.DATETIME;
    }

    /**
     * Check default value type is date.
     * @return flag default value type is date
     */
    public boolean isDefaultValueTypeDate() {
        return showDeleteDefaultValueButton() && getTypeAttributeMeta().getType() == AttributeType.DATE;
    }

    /**
     * Gets date default.
     * @return date default
     */
    public LocalDate getDateDefault() {
        if (entityMeta != null
            && getTypeAttributeMeta() != null
            && getTypeAttributeMeta().getType() != null
            && getTypeAttributeMeta().getType().equals(AttributeType.DATE)
            && getTypeAttributeMeta().getDefaultValue() != null
            && !getTypeAttributeMeta().getDefaultValue().equals("0")) {
            return (LocalDate) valueConvertService.convert(getTypeAttributeMeta(), getTypeAttributeMeta().getDefaultValue());
        }
        return null;
    }

    /**
     * Sets date default.
     * @param dateDefault dateDefault
     */
    public void setDateDefault(LocalDate dateDefault) {
        this.dateDefault = dateDefault;
    }

    /**
     * Gets date time default.
     * @return date time default
     */
    public LocalDateTime getDateTimeDefault() {
        if (entityMeta != null
            && getTypeAttributeMeta() != null
            && getTypeAttributeMeta().getType() != null
            && getTypeAttributeMeta().getType().equals(AttributeType.DATETIME)
            && getTypeAttributeMeta().getDefaultValue() != null
            && !getTypeAttributeMeta().getDefaultValue().equals("0"))
            return (LocalDateTime) valueConvertService.convert(getTypeAttributeMeta(), getTypeAttributeMeta().getDefaultValue());
        else return null;
    }

    /**
     * Sets date time default.
     * @param dateTimeDefault dateTimeDefault
     */
    public void setDateTimeDefault(LocalDateTime dateTimeDefault) {
        this.dateTimeDefault = dateTimeDefault;
    }

    /**
     * Sets default value.
     * @param value value
     */
    public void setDefaultValue(String value) {
        getTypeAttributeMeta().setDefaultValue(value);
    }

    /**
     * Returns default value.
     * @return default value
     */
    public String getDefaultValue() {
        return getTypeAttributeMeta().getDefaultValue();
    }

    /**
     * Returns boolean default.
     * @return boolean default
     */
    public Boolean getBooleanDefault() {
        Boolean value = null;
        if (entityMeta != null
            && getTypeAttributeMeta() != null
            && getTypeAttributeMeta().getType() != null
            && getTypeAttributeMeta().getType().equals(AttributeType.BOOLEAN)
            && getTypeAttributeMeta().getDefaultValue() != null) {
            value = (Boolean) valueConvertService.convert(getTypeAttributeMeta(), getTypeAttributeMeta().getDefaultValue());
        }
        return value;
    }

    /**
     * Sets boolean default.
     * @param booleanDefault boolean default
     */
    public void setBooleanDefault(Boolean booleanDefault) {
        this.booleanDefault = booleanDefault;
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
                                            EntityMetaGroup.DefaultGroup.getDefaultGroupKey(EntityType.CLASSIFIER).name()));
            } else {
                where.addReferenceItem(groupEntityMeta, new WhereItem(getEntityMeta().getKeyAttribute(), Operator.EQ, entityMetaKey));
            }
            where.addReferenceItem(groupEntityTypeMeta, new WhereItem(entityTypeMeta.getKeyAttribute(), Operator.EQ, EntityType.CLASSIFIER));
            groupCriteria.getOrder().addItem(new OrderItem(groupViewOrderMeta, false));
            List<EntityMetaGroup> entityMetaGroups = entityMetaService.getEntityMetaGroups(groupCriteria, null, null);
            EntityMetaGroup entityMetaGroup = entityMetaGroups.get(0);
            entityMetaGroupName = String.format("%s (%s)",
                                                entityMetaGroup.getName().getDescription(userProfile.getLocale()),
                                                entityMetaGroup.getKey());
        }
        return entityMetaGroupName;
    }
}
