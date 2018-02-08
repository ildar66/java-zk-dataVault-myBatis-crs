package ru.masterdm.crs.web.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;

import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.attribute.EntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.MultilangAttribute;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.web.domain.UserProfile;
import ru.masterdm.crs.web.domain.entity.EntityStatus;

/**
 * Edit entity references view model class.
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditEntityReferencesViewModel {

    @WireVariable
    private EntityService entityService;
    @WireVariable
    private EntityMetaService entityMetaService;
    @WireVariable("userProfile")
    private UserProfile userProfile;
    @WireVariable("webConfig")
    private Properties webConfig;

    private EntityStatus entityStatus;
    private String attributeMetaKey;
    private EntityMeta linkedEntityMeta;
    private AttributeMeta linkedAttributeMeta;

    private ListModelList<EntityStatus> entityStatuses;
    private boolean editable;

    private int stringMaxLength;

    /**
     * Initiates context.
     * @param entityStatus entity status
     * @param attributeMetaKey attribute meta key
     * @param editable editable
     */
    @Init
    public void initSetup(@ExecutionArgParam("entityStatus") EntityStatus entityStatus,
                          @ExecutionArgParam("attributeMetaKey") String attributeMetaKey,
                          @ExecutionArgParam("editable") Boolean editable) {
        this.entityStatus = entityStatus;
        this.attributeMetaKey = attributeMetaKey;
        this.editable = editable;
        stringMaxLength = Integer.parseInt(webConfig.getProperty("stringMaxLength"));
    }

    /**
     * Returns linked entity meta.
     * @return linked entity meta
     */
    public EntityMeta getLinkedEntityMeta() {
        if (linkedEntityMeta == null) {
            String linkedEntityKey = entityStatus.getEntity().getMeta().getAttributes()
                                                 .stream()
                                                 .filter(attribute -> attributeMetaKey.equals(attribute.getKey()))
                                                 .findFirst().get().getEntityKey();
            linkedEntityMeta = entityMetaService.getEntityMetaByKey(linkedEntityKey, null);
        }
        return linkedEntityMeta;
    }

    /**
     * Returns linked attribute meta.
     * @return linked attribute meta
     */
    public AttributeMeta getLinkedAttributeMeta() {
        if (linkedAttributeMeta == null) {
            String linkedAttributeKey = entityStatus.getEntity().getMeta().getAttributes()
                                                    .stream()
                                                    .filter(attribute -> attributeMetaKey.equals(attribute.getKey()))
                                                    .findFirst().get().getAttributeKey();
            linkedAttributeMeta = getLinkedEntityMeta().getAttributeMetadata(linkedAttributeKey);
        }
        return linkedAttributeMeta;
    }

    /**
     * Detaches window.
     * @param view view
     */
    @Command
    public void detachWindow(@ContextParam(ContextType.VIEW) Component view) {
        view.detach();
        BindUtils.postGlobalCommand(null, null, "entityRefresh", null);
        BindUtils.postGlobalCommand(null, null, "entitiesRefresh", null);
    }

    /**
     * Returns entity status.
     * @return entity status
     */
    public EntityStatus getEntityStatus() {
        return entityStatus;
    }

    /**
     * Sets entity status.
     * @param entityStatus entity status
     */
    public void setEntityStatus(EntityStatus entityStatus) {
        this.entityStatus = entityStatus;
    }

    /**
     * Returns attribute meta key.
     * @return attribute meta key
     */
    public String getAttributeMetaKey() {
        return attributeMetaKey;
    }

    /**
     * Returns attribute value.
     * @param entity entity
     * @return attribute value
     */
    public Object getAttributeValue(Entity entity) {
        AbstractAttribute attribute = entity.getAttribute(getLinkedAttributeMeta().getKey());
        return attribute.getMeta().isMultilang() ? ((MultilangAttribute) attribute).getValue(userProfile.getLocale()) : attribute.getValue();
    }

    /**
     * Returns entity statuses.
     * @return entity statuses
     */
    public ListModelList<EntityStatus> getEntityStatuses() {
        if (entityStatuses == null) {
            entityStatuses = new ListModelList<>();
            getEntities().stream().forEach(entity -> entityStatuses.add(new EntityStatus(entity, false)));
        }
        return entityStatuses;
    }

    /**
     * Returns entities.
     * @return entities
     */
    public List<Entity> getEntities() {
        LinkedEntityAttribute linkedEntityAttribute = ((LinkedEntityAttribute) getEntityStatus().getEntity().getAttribute(attributeMetaKey));
        List<EntityAttribute> entityAttributes = linkedEntityAttribute.getValue();
        List<Entity> entities = new ArrayList<>();
        entityAttributes.stream().forEach(entityAttribute -> {
            entities.add((Entity) entityAttribute.getEntity());
        });
        return entities;
    }

    /**
     * Returns attributes description.
     * @return attributes description
     */
    public List<AttributeMeta> getAttributes() {
        return getLinkedEntityMeta().getAttributes();
    }

    /**
     * Returns editable.
     * @return editable
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Changes editable status.
     * @param entityStatus entity status
     */
    @Command
    public void changeEditableStatus(@BindingParam("entityStatus") EntityStatus entityStatus) {
        entityStatus.setEditingStatus(!entityStatus.isEditingStatus());
        entitiesRefresh();
    }

    /**
     * Declines entity editing.
     * @param entityStatus entity status
     */
    @Command
    public void decline(@BindingParam("entityStatus") EntityStatus entityStatus) {
        if (entityStatus.getEntity().getId() == null) {
            deleteEntityStatus(entityStatus);
        }
        changeEditableStatus(entityStatus);
    }

    /**
     * Confirms entity editing.
     * @param entityStatus entity status
     */
    @Command
    public void confirm(@BindingParam("entityStatus") EntityStatus entityStatus) {
        Long distinctKeySize = entityStatuses.stream().map(p -> p.getEntity().getKey()).distinct().count();
        if (distinctKeySize != entityStatuses.size()) {
            Messagebox.show(Labels.getLabel("edit_entity_references_duplicate_entity_keys_message"), Labels.getLabel("messagebox_validation"),
                            Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }
        changeEditableStatus(entityStatus);
        entityStatus.getEntity().setKey(entityStatus.getEntity().getKey() == null ? null : entityStatus.getEntity().getKey().toUpperCase());
        persistEntity(entityStatus.getEntity());

        LinkedEntityAttribute linkedEntityAttribute =
                ((LinkedEntityAttribute) getEntityStatus().getEntity().getAttribute(attributeMetaKey));
        List<EntityAttribute> entityAttributes = linkedEntityAttribute.getValue();
        List<EntityAttribute> entityAttributesFiltered = entityAttributes.stream().filter(e -> e.getEntity().equals(entityStatus.getEntity()))
                                                                         .collect(Collectors.toList());
        if (entityAttributesFiltered.isEmpty()) {
            ((LinkedEntityAttribute) getEntityStatus().getEntity().getAttribute(attributeMetaKey)).getEntityList().add(entityStatus.getEntity());
            entityService.persistEntity(getEntityStatus().getEntity());
        }
    }

    /**
     * Removes entity status.
     * @param entityStatus entity status
     */
    @Command
    public void removeEntityStatus(@BindingParam("entityStatus") EntityStatus entityStatus) {
        EventListener<Messagebox.ClickEvent> clickListener = event -> {
            if (Messagebox.Button.YES.equals(event.getButton())) {
                if (entityStatus.getEntity().getId() != null) {
                    LinkedEntityAttribute linkedEntityAttribute =
                            ((LinkedEntityAttribute) getEntityStatus().getEntity().getAttribute(attributeMetaKey));
                    linkedEntityAttribute.remove(entityStatus.getEntity());
                    //TODO: should be in a transaction
                    entityService.persistEntity(getEntityStatus().getEntity());
                    entityService.removeEntity(entityStatus.getEntity());
                    //TODO: should be in a transaction
                }
                deleteEntityStatus(entityStatus);
                resetEntityStatuses();
            }
        };
        Messagebox
                .show(Labels.getLabel("edit_entity_references_remove_entity_message"), Labels.getLabel("edit_entity_references_remove_entity_title"),
                      new Messagebox.Button[] {Messagebox.Button.YES, Messagebox.Button.NO}, Messagebox.QUESTION, clickListener);

    }

    /**
     * Deletes entity status.
     * @param entityStatus entity status
     */
    private void deleteEntityStatus(EntityStatus entityStatus) {
        List<EntityStatus> filtered = getEntityStatuses()
                .stream()
                .filter(b -> b.getEntity().hashCode() != entityStatus.getEntity().hashCode())
                .collect(Collectors.toList());
        entityStatuses.clear();
        entityStatuses.addAll(filtered);
    }

    /**
     * Persists entity.
     * @param entity entity
     */
    private void persistEntity(Entity entity) {
        entityService.persistEntity(entity);
    }

    /**
     * Refreshes entities.
     */
    public void entitiesRefresh() {
        BindUtils.postNotifyChange(null, null, this, "entityStatuses");
    }

    /**
     * Adds entity.
     */
    @Command
    public void addEntity() {
        Entity entity = entityService.newEmptyEntity(getLinkedEntityMeta());
        entityStatuses.add(0, new EntityStatus(entity, true));
        entitiesRefresh();
    }

    /**
     * Resets entity statuses.
     */
    public void resetEntityStatuses() {
        entityStatuses = null;
        entitiesRefresh();
    }

    /**
     * Returns key validator.
     * @return key validator
     */
    public Validator getKeyValidator() {
        return new AbstractValidator() {

            @Override
            public void validate(ValidationContext ctx) {
                if (getEntityStatuses() == null) {
                    return;
                }
                String value = (String) ctx.getProperty().getValue();
                if (getEntityStatuses().stream().noneMatch(p -> p.getEntity().getKey() == null)) {
                    return;
                }

                if (getEntityStatuses().stream().anyMatch(p -> value.equals(p.getEntity().getKey()))
                    || entityService.getEntityIdByKey(getLinkedEntityMeta(), value) != null) {
                    throw new WrongValueException(Labels.getLabel("edit_entity_references_duplicate_entity_keys_message"));
                }
            }
        };
    }

    /**
     * Returns string max length.
     * @return string max length
     */
    public int getStringMaxLength() {
        return stringMaxLength;
    }
}
