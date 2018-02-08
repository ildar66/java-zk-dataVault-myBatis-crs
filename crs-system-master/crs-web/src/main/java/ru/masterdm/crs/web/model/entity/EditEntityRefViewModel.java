package ru.masterdm.crs.web.model.entity;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModelList;

import ru.masterdm.crs.domain.entity.BusinessAction;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.attribute.EntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.MultilangAttribute;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.criteria.Where;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.SecurityService;
import ru.masterdm.crs.web.domain.UserProfile;
import ru.masterdm.crs.web.domain.entity.EntityStatus;
import ru.masterdm.crs.web.service.EntityMetaUiService;

/**
 * Edit entity ref view model class.
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditEntityRefViewModel {

    @WireVariable
    private EntityService entityService;
    @WireVariable
    private EntityMetaService entityMetaService;
    @WireVariable("webConfig")
    private Properties webConfig;
    @WireVariable("userProfile")
    private UserProfile userProfile;
    @WireVariable
    private SecurityService securityService;
    @WireVariable
    private EntityMetaUiService entityMetaUiService;
    private int pageSize;

    private int candidateActivePage = 0;
    private long candidateTotalSize;

    private EntityStatus entityStatus;
    private String attributeMetaKey;
    private boolean isEdit;
    private EntityMeta linkedEntityMeta;
    private AttributeMeta linkedAttributeMeta;
    private List<EntityAttribute> entityAttributes;
    private LinkedEntityAttribute linkedEntityAttribute;

    private ListModelList<Entity> candidateModel;
    private ListModelList<EntityAttribute> chosenModel;

    private Object attributeValue;
    private Criteria criteria;
    private Boolean singleSelect;
    private boolean noSave;

    /**
     * Initiates context.
     * @param entityStatus entity status
     * @param attributeMetaKey attribute meta key
     * @param isEdit is edit
     * @param singleSelect single select
     * @param noSave no save
     */
    @Init
    public void initSetup(@ExecutionArgParam("entityStatus") EntityStatus entityStatus,
                          @ExecutionArgParam("attributeMetaKey") String attributeMetaKey,
                          @ExecutionArgParam("isEdit") Boolean isEdit, @ExecutionArgParam("singleSelect") Boolean singleSelect,
                          @ExecutionArgParam("noSave") Boolean noSave) {
        pageSize = Integer.parseInt(webConfig.getProperty("pageSize"));
        this.entityStatus = entityStatus;
        this.attributeMetaKey = attributeMetaKey;
        this.isEdit = isEdit;
        this.linkedEntityAttribute = ((LinkedEntityAttribute) getEntityStatus().getEntity().getAttribute(attributeMetaKey));
        this.entityAttributes = new ArrayList<>(linkedEntityAttribute.getValue());
        this.singleSelect = singleSelect;
        if (noSave != null) {
            this.noSave = noSave;
        }
    }

    /**
     * Returns candidate entities.
     * @return candidate entities
     */
    protected List<Entity> getCandidateEntities() {
        RowRange rowRange = RowRange.newAsPageAndSize(candidateActivePage, pageSize);
        List<Entity> entities = (List<Entity>) entityService.getEntities(getLinkedEntityMeta(), criteria, rowRange, null);
        candidateTotalSize = rowRange.getTotalCount();
        return entities;
    }

    /**
     * Returns chosen entity attributes.
     * @return chosen entity attributes
     */
    public List<EntityAttribute> getChosenEntityAttributes() {
        return entityAttributes;
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
    }

    /**
     * Saves entity.
     * @param view view
     */
    @Command
    public void saveEntity(@ContextParam(ContextType.VIEW) Component view) {
        linkedEntityAttribute.getEntityAttributeList().clear();
        linkedEntityAttribute.getEntityAttributeList().addAll(entityAttributes);
        if (!noSave) {
            entityService.persistEntity(entityStatus.getEntity());
        }
        BindUtils.postGlobalCommand(null, null, "entitiesRefresh", null);
        BindUtils.postGlobalCommand(null, null, "entityRefresh", null);
        view.detach();
    }

    /**
     * Chooses.
     */
    @Command
    @SmartNotifyChange("*")
    public void choose() {
        for (Entity entity : (Set<Entity>) getCandidateModel().getSelection()) {
            if (entityAttributes.stream().filter(p -> p.getEntity().equals(entity)).count() == 0) {
                EntityAttribute entityAttribute = new EntityAttribute();
                entityAttribute.setEntity(entity);
                entityAttributes.add(entityAttribute);
            }
        }
        chosenModel = null;
    }

    /**
     * Removes all.
     */
    @SmartNotifyChange("*")
    @Command
    public void removeAll() {
        entityAttributes.clear();
        chosenModel = null;
    }

    /**
     * Removes.
     */
    @SmartNotifyChange("*")
    @Command
    public void remove() {
        entityAttributes.removeAll(entityAttributes
                                           .stream()
                                           .filter(ea -> ((Set<EntityAttribute>) getChosenModel().getSelection())
                                                   .stream()
                                                   .anyMatch(s -> s.getEntity().equals(ea.getEntity())))
                                           .collect(Collectors.toList()));
        chosenModel = null;
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
     * Returns is edit.
     * @return is edit
     */
    public boolean isEdit() {
        return isEdit;
    }

    /**
     * Returns candidate total size.
     * @return candidate total size
     */
    public long getCandidateTotalSize() {
        return candidateTotalSize;
    }

    /**
     * Returns page size.
     * @return page size
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * Changes filter.
     */
    @Command
    @SmartNotifyChange("candidateModel")
    public void changeFilter() {
        candidateModel = null;
        updateCriteria();
    }

    /**
     * Sets candidate active page.
     * @param candidateActivePage candidate active page
     */
    @SmartNotifyChange("candidateModel")
    public void setCandidateActivePage(int candidateActivePage) {
        this.candidateActivePage = candidateActivePage;
        candidateModel = null;
    }

    /**
     * Returns candidate model.
     * @return candidate model
     */
    public ListModelList getCandidateModel() {
        if (candidateModel == null) {
            candidateModel = new ListModelList(getCandidateEntities());
            candidateModel.setMultiple(singleSelect == null ? true : !singleSelect);
        }
        return candidateModel;
    }

    /**
     * Returns chosen model.
     * @return chosen model
     */
    public ListModelList getChosenModel() {
        if (chosenModel == null) {
            chosenModel = new ListModelList(getChosenEntityAttributes());
            chosenModel.setMultiple(singleSelect == null ? true : !singleSelect);
        }
        return chosenModel;
    }

    /**
     * Returns attribute value.
     * @return attribute value
     */
    public Object getAttributeValue() {
        return attributeValue;
    }

    /**
     * Sets attribute value.
     * @param attributeValue value
     */
    public void setAttributeValue(Object attributeValue) {
        this.attributeValue = attributeValue;
    }

    /**
     * Updates criteria.
     */
    private void updateCriteria() {
        Operator operator = Operator.EQ;
        if (getLinkedAttributeMeta().getType().equals(AttributeType.STRING) || getLinkedAttributeMeta().getType().equals(AttributeType.TEXT)) {
            operator = Operator.LIKE;
            if (attributeValue != null && isEmpty((String) attributeValue)) {
                attributeValue = null;
            }
        }
        if (attributeValue == null) {
            criteria = null;
        } else {
            criteria = new Criteria();
            Where where = criteria.getWhere();
            where.addItem(new WhereItem(getLinkedAttributeMeta(), operator, attributeValue));
        }
        candidateActivePage = 0;
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
     * Returns is select disabled.
     * @return is select disabled
     */
    public boolean isSelectDisabled() {
        return (!isEdit() || (singleSelect != null && singleSelect && getChosenModel().getSize() == 1));
    }

    /**
     * Returns is entity edit allowed.
     * @return is entity edit allowed
     */
    public boolean isEntityEditAllowed() {
        return securityService.isPermittedForEntityType(securityService.getCurrentUser(),
                                                        entityMetaUiService.getPermissionEntity(EntityType.DICTIONARY),
                                                        BusinessAction.Action.EDIT);
    }
}
