package ru.masterdm.crs.web.model.entity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.ValidationContext;
import org.zkoss.bind.Validator;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.bind.validator.AbstractValidator;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zss.api.Importers;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.model.SSheet;
import org.zkoss.zul.AbstractTreeModel;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Treerow;
import org.zkoss.zul.Window;

import ru.masterdm.crs.domain.FavoritesChecker;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.attribute.EntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.FileInfoAttribute;
import ru.masterdm.crs.domain.entity.attribute.MultilangAttribute;
import ru.masterdm.crs.domain.entity.criteria.Conjunction;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.criteria.Where;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.domain.form.TemplateType;
import ru.masterdm.crs.domain.form.mapping.ImportObject;
import ru.masterdm.crs.domain.form.mapping.Mapper;
import ru.masterdm.crs.domain.form.mapping.MappingField;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.service.CalcService;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.FavoritesService;
import ru.masterdm.crs.service.FormTemplateService;
import ru.masterdm.crs.web.domain.DataSlice;
import ru.masterdm.crs.web.domain.UserProfile;
import ru.masterdm.crs.web.domain.entity.EntityFilter;
import ru.masterdm.crs.web.domain.entity.EntityStatus;
import ru.masterdm.crs.web.service.CriteriaBuilderUiService;
import ru.masterdm.crs.web.service.EntityMetaUiService;
import ru.masterdm.crs.web.service.FormTemplateUiService;

/**
 * Edit entities view model class.
 * @author Igor Matushak
 * @author Alexey Kirilchev
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditEntitiesViewModel {

    @WireVariable
    protected EntityService entityService;
    @WireVariable
    protected EntityMetaService entityMetaService;
    @WireVariable
    protected CalcService calcService;
    @WireVariable
    protected FavoritesService favoritesService;
    @WireVariable("pages")
    protected Properties pages;
    @WireVariable("webConfig")
    protected Properties webConfig;
    @WireVariable("userProfile")
    protected UserProfile userProfile;
    @WireVariable("dataSlice")
    protected DataSlice dataSlice;
    @WireVariable("criteriaBuilderUiService")
    protected CriteriaBuilderUiService criteriaBuilderUiService;
    @WireVariable("entityMetaUiService")
    protected EntityMetaUiService entityMetaUiService;
    @WireVariable
    protected FormTemplateUiService formTemplateUiService;
    @WireVariable
    protected FormTemplateService formTemplateService;

    protected String entityMetaKey;
    protected ListModelList<EntityStatus> entityStatuses;
    protected int pageSize;
    protected int activePage = 0;
    protected long totalSize;
    protected EntityMeta entityMeta;
    protected ListModelList<EntityFilter> entityFilters;
    protected Criteria criteria;
    protected String entityFilter;

    protected String entityMetaName;
    protected EntityTreeModel entityTreeModel;
    protected EntityStatus draggedEntityStatus;
    protected boolean sameTree = false;

    protected FavoritesChecker favoritesChecker;
    protected Boolean entitySupportsFavorites;

    /**
     * Initiates context.
     */
    @Init
    public void initSetup() {
        pageSize = Integer.parseInt(webConfig.getProperty("pageSize"));
        entityMetaKey = (String) Executions.getCurrent().getAttribute("entityMetaKey");
        entityMetaName = (String) Executions.getCurrent().getAttribute("entityMetaName");
        if (entityMetaKey == null) {
            entityMetaKey = (String) Executions.getCurrent().getAttribute("key");
            entityMetaName = getEntityMeta().getName().getDescription(userProfile.getLocale());
        }
        entityFilters = new ListModelList<>(userProfile.getFiltersByKey(entityMetaKey));
        entityFilters.setMultiple(true);
        userProfile.getFiltersByKey(entityMetaKey).stream().forEach(ef -> entityFilters.addToSelection(ef));
        entityFilter = userProfile.getEntityMetaFilterByKey(entityMetaKey);
        updateCriteria();
    }

    /**
     * Returns entity meta.
     * @return entity meta
     */
    public EntityMeta getEntityMeta() {
        if (entityMeta == null && entityMetaKey != null) {
            entityMeta = entityMetaService.getEntityMetaByKey(entityMetaKey, getActuality());
        }
        return entityMeta;
    }

    /**
     * Returns actuality.
     * @return actuality
     */
    protected LocalDateTime getActuality() {
        return dataSlice.getDataSliceDateByKey(entityMetaKey);
    }

    /**
     * Sets entity meta.
     * @param entityMeta entity meta
     */
    public void setEntityMeta(EntityMeta entityMeta) {
        this.entityMeta = entityMeta;
    }

    /**
     * Returns total size.
     * @return total size
     */
    public long getTotalSize() {
        return totalSize;
    }

    /**
     * Returns page size.
     * @return page size
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * Sets active page.
     * @param activePage active page
     */
    @NotifyChange("entityStatuses")
    public void setActivePage(int activePage) {
        this.activePage = activePage;
        entityStatuses = null;
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
            Messagebox.show(Labels.getLabel("edit_entities_duplicate_entity_keys_message"), Labels.getLabel("messagebox_validation"),
                            Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }
        changeEditableStatus(entityStatus);
        entityStatus.getEntity().setKey(entityStatus.getEntity().getKey() == null ? null : entityStatus.getEntity().getKey().toUpperCase());
        persistEntity(entityStatus.getEntity());
    }

    /**
     * Persists entity.
     * @param entity entity
     */
    protected void persistEntity(Entity entity) {
        entityService.persistEntity(entity);
    }

    /**
     * Edits entity text.
     * @param entityStatus entity status
     * @param attributeMetaKey attribute meta key
     * @param isEdit isEdit
     */
    @Command
    public void editEntityText(@BindingParam("entityStatus") EntityStatus entityStatus,
                               @BindingParam("attributeMetaKey") String attributeMetaKey,
                               @BindingParam("isEdit") boolean isEdit) {
        Map<String, Object> map = new HashMap<>();
        map.put("entityStatus", entityStatus);
        map.put("attributeMetaKey", attributeMetaKey);
        map.put("isEdit", isEdit);
        Window window = (Window) Executions.createComponents(pages.getProperty("entity.edit_entity_text"), null, map);
        window.doModal();
    }

    /**
     * Edits entity ref.
     * @param entityStatus entity status
     * @param attributeMetaKey attribute meta key
     * @param isEdit isEdit
     */
    @Command
    public void editEntityRef(@BindingParam("entityStatus") EntityStatus entityStatus,
                              @BindingParam("attributeMetaKey") String attributeMetaKey,
                              @BindingParam("isEdit") boolean isEdit) {
        if (!entityMetaUiService.getReferenceValid(entityStatus, attributeMetaKey)) {
            Messagebox.show(Labels.getLabel("edit_entities_referenced_entity_removed_message"));
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("entityStatus", entityStatus);
        map.put("attributeMetaKey", attributeMetaKey);
        map.put("isEdit", isEdit);
        Window window = (Window) Executions.createComponents(pages.getProperty("entity.edit_entity_ref"), null, map);
        window.doModal();
    }

    /**
     * Adds entity.
     */
    @Command
    public void addEntity() {
        if (entityMetaKey != null) {
            Entity entity = entityService.newEmptyEntity(getEntityMeta());
            if (getEntityMeta().isHierarchical()) {
                EntityStatus root = ((AbstractTreeModel<EntityStatus>) getEntityTreeModel()).getRoot();
                root.getChildren().add(0, new EntityStatus(entity, true));
            } else {
                entityStatuses.add(0, new EntityStatus(entity, true));
            }
            entitiesRefresh();
        }
    }

    /**
     * Edits data slice date.
     */
    @Command
    public void editDataSliceDate() {
        Map<String, Object> map = new HashMap<>();
        map.put("entityMetaKey", entityMetaKey);
        Window window = (Window) Executions.createComponents(pages.getProperty("entity.edit_data_slice_date"), null, map);
        window.doModal();
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
                    entityService.removeEntity(entityStatus.getEntity());
                }
                deleteEntityStatus(entityStatus);
                resetEntityStatuses();
            }
        };
        Messagebox.show(Labels.getLabel("edit_entities_title_remove_entity_message"), Labels.getLabel("edit_entities_title_remove_entity_title"),
                        new Messagebox.Button[] {Messagebox.Button.YES, Messagebox.Button.NO}, Messagebox.QUESTION, clickListener);

    }

    /**
     * Edits entity.
     * @param entityStatus entity status
     * @param editable editable
     */
    @Command
    public void editEntity(@BindingParam("entityStatus") EntityStatus entityStatus, @BindingParam("editable") boolean editable) {
        Map<String, Object> map = new HashMap<>();
        map.put("entityStatus", entityStatus);
        map.put("editable", editable);
        Window window = (Window) Executions.createComponents(pages.getProperty("entity.edit_entity"), null, map);
        window.doModal();
    }

    /**
     * Edits entity filter.
     */
    @Command
    public void editEntitiesFilter() {
        Map<String, Object> map = new HashMap<>();
        map.put("entityMeta", getEntityMeta());
        Window window = (Window) Executions.createComponents(pages.getProperty("entity.edit_entities_filter"), null, map);
        window.doModal();
    }

    /**
     * Resets entities filter.
     */
    @SmartNotifyChange("*")
    @GlobalCommand
    public void resetEntitiesFilter() {
        String key = entityMeta.getKey();
        entityFilters = new ListModelList<>(userProfile.getFiltersByKey(key));
        entityFilters.setMultiple(true);

        updateCriteria();

        entityFilters.stream().forEach(ef -> entityFilters.addToSelection(ef));
        resetEntityStatuses();
    }

    /**
     * Resets entities filter.
     */
    @SmartNotifyChange("*")
    @GlobalCommand
    public void resetDateSliceDate() {
        resetEntityStatuses();
        resetEntityMeta();
    }

    /**
     * Deletes entities filter.
     */
    @Command
    @SmartNotifyChange("*")
    public void deleteEntitiesFilter() {
        userProfile.setFiltersByKey(entityMeta.getKey(), new ArrayList<>(entityFilters.getSelection()));
        resetEntitiesFilter();
    }

    /**
     * Deletes entity status.
     * @param entityStatus entity status
     */
    private void deleteEntityStatus(EntityStatus entityStatus) {
        if (getEntityMeta().isHierarchical()) {
            EntityStatus root = ((AbstractTreeModel<EntityStatus>) getEntityTreeModel()).getRoot();
            removeTreeElement(root, entityStatus);
        } else {
            List<EntityStatus> filtered = getEntityStatuses()
                    .stream()
                    .filter(b -> b.getEntity().hashCode() != entityStatus.getEntity().hashCode())
                    .collect(Collectors.toList());
            entityStatuses.clear();
            entityStatuses.addAll(filtered);
        }
    }

    /**
     * Refreshes row template row template.
     * @param entityStatus entity status
     */
    private void refreshRowTemplate(EntityStatus entityStatus) {
        if (getEntityMeta().isHierarchical()) {
            EntityStatus root = ((AbstractTreeModel<EntityStatus>) getEntityTreeModel()).getRoot();
            updateTreeElement(root, entityStatus);
        } else {
            //replace the element in the collection by itself to trigger a model update
            getEntityStatuses().set(getEntityStatuses().indexOf(entityStatus), entityStatus);
        }
    }

    /**
     * Removes tree element.
     * @param root root
     * @param removeElement remove element
     */
    private void removeTreeElement(EntityStatus root, EntityStatus removeElement) {
        Iterator<EntityStatus> it = root.getChildren().iterator();
        while (it.hasNext()) {
            EntityStatus parent = it.next();
            if (parent.getEntity().hashCode() == removeElement.getEntity().hashCode()) {
                it.remove();
            } else {
                removeTreeElement(parent, removeElement);
            }
        }
    }

    /**
     * Updates tree element.
     * @param root root
     * @param updateElement update element
     */
    private void updateTreeElement(EntityStatus root, EntityStatus updateElement) {
        Iterator<EntityStatus> it = root.getChildren().iterator();
        while (it.hasNext()) {
            EntityStatus parent = it.next();
            if (parent.getEntity().hashCode() == updateElement.getEntity().hashCode()) {
                parent.setEditingStatus(updateElement.isEditingStatus());
            } else {
                updateTreeElement(parent, updateElement);
            }
        }
    }

    /**
     * Returns entities.
     * @return entities
     */
    protected List<Entity> getEntities() {
        if (getEntityMeta() != null) {
            RowRange rowRange = null;
            boolean hierarchical = getEntityMeta().isHierarchical();
            if (!hierarchical)
                rowRange = RowRange.newAsPageAndSize(activePage, pageSize);
            if (hierarchical && isFiltersEmpty()) {
                criteria.getWhere().setReferenceExists(getEntityMeta().getParentReferenceAttribute(), Conjunction.AND_NOT);
            }
            List<Entity> entities = (List<Entity>) entityService.getEntities(getEntityMeta(), criteria, rowRange, getActuality());
            if (hierarchical) {
                totalSize = entities.size();
                pageSize = entities.size();
            } else
                totalSize = rowRange.getTotalCount();

            return entities;
        }
        return new ArrayList<>();
    }

    /**
     * Refreshes entities.
     */
    @GlobalCommand
    public void entitiesRefresh() {
        if (getEntityMeta() != null) {
            if (getEntityMeta().isHierarchical()) {
                BindUtils.postNotifyChange(null, null, this, "entityTreeModel");
            } else {
                BindUtils.postNotifyChange(null, null, this, "entityStatuses");
            }
        }
    }

    /**
     * Resets entity statuses.
     */
    public void resetEntityStatuses() {
        if (getEntityMeta() != null) {
            if (getEntityMeta().isHierarchical()) {
                entityTreeModel = null;
            } else {
                entityStatuses = null;
            }
            entitiesRefresh();
        }
    }

    /**
     * Resets entity meta.
     */
    public void resetEntityMeta() {
        entityMeta = null;
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
                    || entityService.getEntityIdByKey(getEntityMeta(), value) != null) {
                    throw new WrongValueException(Labels.getLabel("edit_entities_duplicate_entity_keys_message"));
                }
            }
        };
    }

    /**
     * Returns entity filters.
     * @return entity filters
     */
    public ListModelList<EntityFilter> getEntityFilters() {
        return entityFilters;
    }

    /**
     * Updates criteria.
     */
    protected void updateCriteria() {
        criteria = new Criteria();
        if (entityFilter != null && !entityFilter.trim().isEmpty()) {
            String filter = entityFilter.trim().toUpperCase();
            Where where = criteria.getWhere();
            filterByText(where, filter);
            userProfile.setEntityMetaFilterByKey(entityMetaKey, filter);
        } else {
            criteria = criteriaBuilderUiService.getCriteriaByEntityFilters(entityFilters, entityMetaService);
            userProfile.setEntityMetaFilterByKey(entityMetaKey, null);
        }
        activePage = 0;
    }

    /**
     * Filters by string filter textbox value.
     * @param where where condition
     * @param filter filter text
     */
    protected void filterByText(Where where, String filter) {
        where.addItem(new WhereItem(getEntityMeta().getKeyAttribute(), Operator.LIKE, "%" + filter + "%"));
    }

    /**
     * Returns attributes description.
     * @return attributes description
     */
    public List<AttributeMeta> getAttributes() {
        return getEntityMeta() != null ? getEntityMeta().getAttributes() : Collections.emptyList();
    }

    /**
     * Returns title.
     * @return title
     */
    public String getTitle() {
        String title = null;
        LocalDateTime dataSliceDate = dataSlice.getDataSliceDateByKey(entityMetaKey);
        if (dataSliceDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Labels.getLabel("date_time_format"));
            String formattedString = dataSliceDate.format(formatter);
            title = MessageFormat.format(Labels.getLabel("edit_entities_data_slice_title"), entityMetaName, formattedString);
        } else {
            title = MessageFormat.format(Labels.getLabel("edit_entities_title_full"), entityMetaName);
        }
        return title;
    }

    /**
     * File upload.
     * @param uploadEvent file upload event
     * @param fileInfoAttribute file info attribute
     */
    @Command
    public void fileUpload(@ContextParam(ContextType.TRIGGER_EVENT) UploadEvent uploadEvent,
                           @BindingParam("fileInfoAttribute") FileInfoAttribute fileInfoAttribute) {
        Media media = uploadEvent.getMedia();
        InputStream inputStream = getInputStream(media);
        try {
            fileInfoAttribute.setContent(inputStream);
        } catch (Exception e) {
            throw new CrsException(e.getMessage(), e);
        }
        fileInfoAttribute.setMimeType(media.getContentType());
        fileInfoAttribute.setName(media.getName());
        fileInfoAttribute.setLinkRemoved(false);
        entitiesRefresh();
    }

    /**
     * File download.
     * @param fileInfoAttribute file info attribute
     */
    @Command
    public void fileDownload(@BindingParam("fileInfoAttribute") FileInfoAttribute fileInfoAttribute) {
        InputStream fileContent = entityService.getFileContent(fileInfoAttribute, null);
        Filedownload.save(fileContent, fileInfoAttribute.getMimeType(), fileInfoAttribute.getName());
    }

    /**
     * Unattach file.
     * @param fileInfoAttribute file info attribute
     */
    @Command
    public void unattachFile(@BindingParam("fileInfoAttribute") FileInfoAttribute fileInfoAttribute) {
        EventListener<Messagebox.ClickEvent> clickListener = event -> {
            if (Messagebox.Button.YES.equals(event.getButton())) {
                fileInfoAttribute.setLinkRemoved(true);
                BindUtils.postNotifyChange(null, null, EditEntitiesViewModel.this, "entityStatuses");
            }
        };
        Messagebox.show(Labels.getLabel("edit_entities_unattach_file_message"), Labels.getLabel("edit_entities_unattach_file_title"),
                        new Messagebox.Button[] {Messagebox.Button.YES, Messagebox.Button.NO}, Messagebox.QUESTION, clickListener);
    }

    /**
     * Returns media input stream.
     * @param media meda
     * @return media input stream
     */
    private InputStream getInputStream(Media media) {
        try {
            return media.getStreamData();
        } catch (Exception e1) {
            return new ReaderInputStream(media.getReaderData(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Navigates entity meta list.
     */
    @Command
    public void navigateEntityMetaList() {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "entity.meta.entity_list");
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Returns attribute value.
     * @param attribute attribute
     * @return attribute value
     */
    public Object getAttributeValue(EntityAttribute attribute) {
        Entity entity = (Entity) attribute.getEntity();
        if (entity == null || attribute.getMeta().getAttributeKey() == null) {
            return null;
        }
        AbstractAttribute linkedAttribute = entity.getAttribute(attribute.getMeta().getAttributeKey());
        return linkedAttribute.getMeta().isMultilang()
               ? ((MultilangAttribute) linkedAttribute).getValue(userProfile.getLocale()) : linkedAttribute.getValue();
    }

    /**
     * Returns entity statuses.
     * @return entity statuses
     */
    public ListModelList<EntityStatus> getEntityStatuses() {
        if (entityStatuses == null) {
            entityStatuses = new ListModelList<>();
            if (entityMetaKey != null) {
                getEntities().stream().forEach(entity -> entityStatuses.add(new EntityStatus(entity, false)));
            }
        }
        return entityStatuses;
    }

    /**
     * Returns entity tree model.
     * @return entity tree model
     */
    public EntityTreeModel getEntityTreeModel() {
        if (entityTreeModel == null) {
            if (entityMetaKey != null) {
                Entity entity = entityService.newEmptyEntity(getEntityMeta());
                EntityStatus rootEntityStatus = new EntityStatus(entity, false);
                getEntities().stream().forEach(e -> {
                    EntityStatus entityStatus = new EntityStatus(e, false);
                    rootEntityStatus.getChildren().add(entityStatus);
                    if (isFiltersEmpty() && e.isChildrenExists())
                        entityStatus.getChildren().add(new EntityStatus(e, false));
                });
                entityTreeModel = new EntityTreeModel(rootEntityStatus);
            }
        }
        return entityTreeModel;
    }

    /**
     * Sets entity tree model.
     * @param entityTreeModel entity tree model
     */
    public void setEntityTreeModel(EntityTreeModel entityTreeModel) {
        this.entityTreeModel = entityTreeModel;
    }

    /**
     * Returns dragged entity status.
     * @return dragged entity status
     */
    public EntityStatus getDraggedEntityStatus() {
        return draggedEntityStatus;
    }

    /**
     * Sets dragged entity status.
     * @param draggedEntityStatus dragged entity status
     */
    public void setDraggedEntityStatus(EntityStatus draggedEntityStatus) {
        this.draggedEntityStatus = draggedEntityStatus;
    }

    /**
     * Drops entity.
     * @param event event
     * @param receiver receiver
     */
    @Command
    public void dropEntity(@ContextParam(ContextType.TRIGGER_EVENT) DropEvent event, @BindingParam("entityStatus") EntityStatus receiver) {
        Treerow row = (Treerow) event.getDragged();
        Treeitem item = (Treeitem) row.getParent();
        EntityStatus entityStatus = item.getValue();

        if (receiver != null) {
            sameTree = false;
            checkParent(entityStatus, receiver);
            if (sameTree) {
                return;
            }
        }

        entityService.loadEntityParent(entityStatus.getEntity(), null);

        if (!entityStatus.getEntity().getParentReferenceAttribute().getEntityList().isEmpty()) {
            Entity parent = entityStatus.getEntity().getParentReferenceAttribute().getEntityList().get(0);
            if (receiver != null && (parent.hashCode() == receiver.getEntity().hashCode())) {
                return;
            }
            entityService.loadEntityChildren(parent, null);
            parent.getChildrenReferenceAttribute().getEntityList().remove(entityStatus.getEntity());
            entityService.persistEntityConsistent(parent);
        }

        if (receiver != null) {
            receiver.getEntity().getChildrenReferenceAttribute().getEntityList().add(entityStatus.getEntity());
            entityService.persistEntityConsistent(receiver.getEntity());
        }

        resetEntityStatuses();
    }

    /**
     * Removes tree element.
     * @param root root
     * @param child child
     */
    private void checkParent(EntityStatus root, EntityStatus child) {
        Iterator<EntityStatus> it = root.getChildren().iterator();
        while (it.hasNext()) {
            EntityStatus parent = it.next();
            if (parent.getEntity().hashCode() == child.getEntity().hashCode()) {
                sameTree = true;
            } else {
                checkParent(parent, child);
            }
        }
    }

    /**
     * Returns is filters empty.
     * @return true if filters empty, false otherwise
     */
    protected boolean isFiltersEmpty() {
        return getEntityFilters().isEmpty() && StringUtils.isEmpty(entityFilter);
    }

    /**
     * Returns entity status.
     * @param entityStatus entity status
     */
    @Command
    @SmartNotifyChange("*")
    public void openLeaf(@BindingParam("entityStatus") EntityStatus entityStatus) {
        if (!isFiltersEmpty()) {
            return;
        }
        entityService.loadEntityChildren(entityStatus.getEntity(), null);
        entityStatus.getChildren().clear();
        entityStatus.getEntity().getChildrenReferenceAttribute().getEntityList().stream()
                    .forEach(e -> {
                        EntityStatus status = new EntityStatus(e, false);
                        entityStatus.getChildren().add(status);
                        if (status.getEntity().isChildrenExists())
                            status.getChildren().add(new EntityStatus(e, false));
                    });
    }

    /**
     * Exports entities.
     */
    @Command
    public void exportEntities() {
        FormTemplate formTemplate = formTemplateService.prepareFormTemplateForEntities(getEntityMeta(), TemplateType.EXPORT);
        InputStream bookContent = WebApps.getCurrent().getResourceAsStream("/WEB-INF/books/blank.xls");
        try {
            Book book = Importers.getImporter().imports(bookContent, entityMetaName);
            Mapper mapper = formTemplate.getMapper();
            mapper.getObjects().forEach(mappingObject -> {
                SSheet sSheet;
                if (mapper.getObjects().get(0).equals(mappingObject)) {
                    sSheet = book.getInternalBook().getSheet(0);
                    book.getInternalBook().setSheetName(sSheet, entityMetaKey);
                } else {
                    sSheet = book.getInternalBook().getSheetByName(mappingObject.getEntityMeta().getKey());
                    if (sSheet == null)
                        sSheet = book.getInternalBook().createSheet(mappingObject.getEntityMeta().getKey());
                }
                for (int i = 0; i < mappingObject.getFields().size(); i++) {
                    MappingField mappingField = mappingObject.getFields().get(i);
                    Sheet sheet = book.getSheet(sSheet.getSheetName());
                    org.zkoss.zss.api.Range range = Ranges.range(sheet, 0, i);
                    range.setCellEditText(mappingField.getAttributeMeta().getKey());
                }
            });
            List<ImportObject> importObjects = formTemplateUiService.prepareFormMap(book, mapper, TemplateType.EXPORT);
            formTemplateUiService.exportForm(book, importObjects);
            formTemplateUiService.exportFile(book);
        } catch (IOException e) {
            throw new CrsException(e);
        }
    }

    /**
     * Import entities.
     * @param command command
     */
    @Command
    public void importCommand(@BindingParam("command") String command) {
        FormTemplate formTemplate = formTemplateService.prepareFormTemplateForEntities(getEntityMeta(), TemplateType.IMPORT);
        formTemplateUiService.process(formTemplate, command);
    }

    /**
     * Imports data from file.
     * @param template template
     */
    @GlobalCommand
    @SmartNotifyChange("entityStatuses")
    public void importFile(@BindingParam("template") FormTemplate template) {
        formTemplateUiService.importFile(template);
        entityStatuses = null;
    }

    /**
     * Returns entity filter.
     * @return entity filter
     */

    public String getEntityFilter() {
        return entityFilter;
    }

    /**
     * Sets entity filter.
     * @param entityFilter entity filter
     */
    public void setEntityFilter(String entityFilter) {
        this.entityFilter = entityFilter;
    }

    /**
     * Returns true if entity added in favorites, false otherwise.
     * @param entity entity
     * @return true if entity added in favorites, false otherwise
     */
    public boolean isFavorite(Entity entity) {
        return getFavoritesChecker().isFavorite(entity);
    }

    /**
     * Returns favorites checker.
     * @return favorites checker
     */
    protected FavoritesChecker getFavoritesChecker() {
        if (favoritesChecker == null) {
            List<Entity> entities = getEntityStatuses().getInnerList().stream().map(es -> es.getEntity()).collect(Collectors.toList());
            favoritesChecker = favoritesService.findFavorites(entities);
        }
        return favoritesChecker;
    }

    /**
     * Adds favorite.
     * @param entityStatus entity status
     */
    @Command
    public void addFavorite(@BindingParam("entityStatus") EntityStatus entityStatus) {
        favoritesService.addFavorite(entityStatus.getEntity(), getFavoritesChecker());
        BindUtils.postNotifyChange(null, null, entityStatus, "entity");
    }

    /**
     * Removes favorite.
     * @param entityStatus entity status
     */
    @Command
    public void removeFavorite(@BindingParam("entityStatus") EntityStatus entityStatus) {
        favoritesService.removeFavorite(entityStatus.getEntity(), getFavoritesChecker());
        BindUtils.postNotifyChange(null, null, entityStatus, "entity");
    }

    /**
     * Returns is show favorites buttons.
     * @return true if show favorites buttons, false otherwise
     */
    public Boolean getShowFavorites() {
        if (entitySupportsFavorites == null)
            entitySupportsFavorites = favoritesService.isEntitySupportsFavorites(getEntityMeta());
        return entitySupportsFavorites;
    }

    /**
     * Returns references attribute meta keys.
     * @return references attribute meta keys
     */
    public List<String> getReferencesAttributeMetaKeys() {
        return new ArrayList<>();
    }

    /**
     * Returns entity supports department mapping.
     * @return entity supports department mapping
     */
    public Boolean getEntitySupportsDepartmentMapping() {
        return Boolean.FALSE;
    }
}
