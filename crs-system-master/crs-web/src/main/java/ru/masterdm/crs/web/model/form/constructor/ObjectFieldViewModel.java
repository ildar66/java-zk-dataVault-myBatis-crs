package ru.masterdm.crs.web.model.form.constructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModelList;

import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.ClientAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientGroupAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.form.FormDateType;
import ru.masterdm.crs.domain.form.mapping.MappingField;
import ru.masterdm.crs.service.CalcService;
import ru.masterdm.crs.service.ClientService;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.web.service.FormTemplateUiService;

/**
 * View model for object dialog.
 * @author Vladimir Shvets
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ObjectFieldViewModel {

    @WireVariable
    private CalcService calcService;
    @WireVariable
    private EntityService entityService;
    @WireVariable
    private EntityMetaService entityMetaService;
    @WireVariable
    private FormTemplateUiService formTemplateUiService;
    @WireVariable("webConfig")
    protected Properties webConfig;
    @WireVariable
    protected ClientService clientService;

    private String searchString = "";
    private MappingField field;
    private ListModelList entitiesModel;

    protected int pageSize;
    private int symbols;

    /**
     * Initialization.
     * @param field mapping field
     */
    @Init
    public void init(@BindingParam("field") MappingField field) {
        setField(field);
        pageSize = Integer.parseInt(webConfig.getProperty("pageSize"));
        symbols = Integer.parseInt(webConfig.getProperty("symbolsToSearch"));
    }

    /**
     * Gets model for key combobox.
     * @param field mapping field.
     * @return mapping field
     */
    public ListModelList getComboModel(MappingField field) {
        if (field.getAttributeMeta().getType().equals(AttributeType.REFERENCE)) {
            if (field.getAttributeMeta().getKey().equals(Calculation.CalculationAttributeMeta.MODEL.getKey())) {
                List<Model> models = calcService.getModels(null, null, null);
                return new ListModelList(models);
            } else {
                List<? extends Entity> entities = entityService.getEntities(
                        entityMetaService.getEntityMetaByKey(field.getAttributeMeta().getEntityKey(), null),
                        null, null, null);
                return new ListModelList(entities);
            }
        }

        return new ListModelList();
    }

    /**
     * Get clients model.
     * @return clients
     */
    public ListModelList getClientsModel() {
        EntityMeta clientEntityMeta = entityMetaService.getEntityMetaByKey(ClientAttributeMeta.METADATA_KEY, null);
        return getEntitiesModel(clientEntityMeta);
    }

    /**
     * Refreshes client model.
     * @param event on changing event
     */
    @Command
    @NotifyChange("clientsModel")
    public void refreshClientsModel(@ContextParam(ContextType.TRIGGER_EVENT) InputEvent event) {
        searchString = event.getValue();
        entitiesModel = null;
    }

    /**
     * Get client groups model.
     * @return client groups
     */
    public ListModelList getClientGroupsModel() {
        EntityMeta clientGroupEntityMeta = entityMetaService.getEntityMetaByKey(ClientGroupAttributeMeta.METADATA_KEY, null);
        return getEntitiesModel(clientGroupEntityMeta);
    }

    /**
     * Get entities model.
     * @param entityMeta entity meta
     * @return entity meta
     */
    private ListModelList getEntitiesModel(EntityMeta entityMeta) {
        if (entitiesModel == null) {
            if (searchString.isEmpty()) {
                entitiesModel = new ListModelList();
                if (field != null && field.getValue() != null && !field.getValue().toString().isEmpty()) {
                    Entity entity = entityService.getEntity(entityMeta, field.getValue().toString(), null);
                    if (entity != null)
                        entitiesModel = new ListModelList(Collections.singletonList(entity));
                }
            } else if (searchString.length() < symbols) {
                entitiesModel = new ListModelList();
            } else {
                RowRange rowRange = new RowRange(0, pageSize);
                LocalDateTime clientLdts = clientService.getClientsEntityRequestLdts();
                List<Long> entityHubIds;
                if (entityMeta.getKey().equals(ClientGroupAttributeMeta.METADATA_KEY))
                    entityHubIds = clientService.getClientGroupIdsBySearchString(searchString, rowRange);
                else
                    entityHubIds = clientService.getClientIdsBySearchString(searchString, rowRange);
                if (entityHubIds.isEmpty()) {
                    entitiesModel = new ListModelList();
                } else {
                    Criteria criteria = new Criteria();
                    criteria.setResultCache(true);
                    criteria.setHubIds(entityHubIds);
                    entitiesModel = new ListModelList(entityService.getEntities(entityMeta, criteria, null, clientLdts));
                }
            }
        }
        return entitiesModel;
    }

    /**
     * Refreshes group model.
     * @param event on changing event
     */
    @Command
    @NotifyChange("clientGroupsModel")
    public void refreshClientGroupsModel(@ContextParam(ContextType.TRIGGER_EVENT) InputEvent event) {
        searchString = event.getValue();
        entitiesModel = null;
    }

    /**
     * Set selected item.
     * @param field field
     * @param value value
     */
    @Command
    public void selectItem(@BindingParam("field") MappingField field, @BindingParam("val") String value) {
        field.setValue(value);
    }

    /**
     * Gets form's date type.
     * @return date types
     */
    public ListModelList<FormDateType> getDateTypes() {
        return formTemplateUiService.getDateTypes();
    }

    /**
     * Gets offset type.
     * @return offset types
     */
    public ListModelList<FormDateType.OffsetType> getOffsetTypes() {
        return formTemplateUiService.getOffsetTypes();
    }

    /**
     * Form's date type description.
     * @param dateType date type
     * @return date type description
     */
    public String getDateTypeDescription(FormDateType dateType) {
        return formTemplateUiService.getDateTypeDescription(dateType);
    }

    /**
     * Date's offset type description.
     * @param offsetType offset type
     * @return offset type description
     */
    public String getOffsetTypeDescription(FormDateType.OffsetType offsetType) {
        return formTemplateUiService.getOffsetTypeDescription(offsetType);
    }

    /**
     * Gets entity by key.
     * @param entities entities list
     * @param key key
     * @return entity
     */
    public Entity getSelectedItem(List<Entity> entities, String key) {
        return entities.stream().filter(entity -> entity.getKey().equals(key)).findFirst().orElse(null);
    }

    /**
     * Returns field.
     * @return field
     */
    public MappingField getField() {
        return field;
    }

    /**
     * Sets field.
     * @param field field
     */
    public void setField(MappingField field) {
        this.field = field;
    }
}
