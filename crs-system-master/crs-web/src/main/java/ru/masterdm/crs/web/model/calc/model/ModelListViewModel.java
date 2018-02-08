package ru.masterdm.crs.web.model.calc.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.criteria.Where;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.service.CalcService;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.web.domain.UserProfile;
import ru.masterdm.crs.web.domain.entity.EntityFilter;

/**
 * Model list view model class.
 * @author Mikhail Kuzmin
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ModelListViewModel {

    @WireVariable
    private EntityMetaService entityMetaService;
    @WireVariable
    private CalcService calcService;
    @WireVariable("pages")
    private Properties pages;
    @WireVariable("webConfig")
    private Properties webConfig;
    @WireVariable("userProfile")
    private UserProfile userProfile;

    private int pageSize;

    private int publishedActivePage = 0;
    private long publishedTotalSize;

    private int draftActivePage = 0;
    private long draftTotalSize;

    private ListModelList<EntityFilter> entityFilters;
    private Criteria criteria;

    private ListModelList<Model> publishedModels;
    private ListModelList<Model> draftModels;

    private EntityMeta entityMeta;
    private List<Model> selected;
    private Boolean draftTabSelected = true;

    /**
     * Initiates context.
     */
    @Init
    public void initSetup() {
        pageSize = Integer.parseInt(webConfig.getProperty("pageSize"));
        entityFilters = new ListModelList<>(userProfile.getFiltersByKey(Model.METADATA_KEY));
        entityFilters.setMultiple(true);
        userProfile.getFiltersByKey(Model.METADATA_KEY).stream().forEach(ef -> entityFilters.addToSelection(ef));
        selected = new ArrayList<>();
        updateCriteria();
    }

    /**
     * Returns selected Model.
     * @return selected Model
     */
    public List<Model> getSelected() {
        return selected;
    }

    /**
     * Sets selected Model.
     * @param selected selected Model
     */
    public void setSelected(List<Model> selected) {
        this.selected = selected;
    }

    /**
     * Returns published total size.
     * @return published total size
     */
    public long getPublishedTotalSize() {
        return publishedTotalSize;
    }

    /**
     * Returns draft total size.
     * @return draft total size
     */
    public long getDraftTotalSize() {
        return draftTotalSize;
    }

    /**
     * Returns page size.
     * @return page size
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * Returns published active page.
     * @return published active page
     */
    public int getPublishedActivePage() {
        return publishedActivePage;
    }

    /**
     * Sets published active page.
     * @param publishedActivePage published active page
     */
    @SmartNotifyChange("*")
    public void setPublishedActivePage(int publishedActivePage) {
        this.publishedActivePage = publishedActivePage;
        publishedModels = null;
    }

    /**
     * Returns draft active page.
     * @return draft active page
     */
    public int getDraftActivePage() {
        return draftActivePage;
    }

    /**
     * Sets draft active page.
     * @param draftActivePage draft active page
     */
    @SmartNotifyChange("*")
    public void setDraftActivePage(int draftActivePage) {
        this.draftActivePage = draftActivePage;
        draftModels = null;
    }

    /**
     * Returns published model.
     * @return published model
     */
    public ListModelList<Model> getPublishedModel() {
        if (publishedModels == null) {
            publishedModels = new ListModelList(getPublishedModels());
            publishedModels.setMultiple(true);
        }
        return publishedModels;
    }

    /**
     * Returns published models.
     * @return published models
     */
    public List<Model> getPublishedModels() {
        RowRange rowRange = RowRange.newAsPageAndSize(publishedActivePage, pageSize);
        List<Model> models = calcService.getPublishedModels(criteria, rowRange, null);
        publishedTotalSize = rowRange.getTotalCount();
        return models;
    }

    /**
     * Returns draft model.
     * @return draft model
     */
    public ListModelList<Model> getDraftModel() {
        if (draftModels == null) {
            draftModels = new ListModelList(getDraftModels());
            draftModels.setMultiple(true);
        }
        return draftModels;
    }

    /**
     * Returns draft models.
     * @return draft models
     */
    public List<Model> getDraftModels() {
        RowRange rowRange = RowRange.newAsPageAndSize(draftActivePage, pageSize);
        List<Model> models = calcService.getDraftModels(criteria, rowRange, null);
        draftTotalSize = rowRange.getTotalCount();
        return models;
    }

    /**
     * Returns entity filters.
     * @return entity filters
     */
    public ListModelList<EntityFilter> getEntityFilters() {
        return entityFilters;
    }

    /**
     * Returns entity meta.
     * @return entity meta
     */
    public EntityMeta getEntityMeta() {
        if (entityMeta == null) {
            entityMeta = entityMetaService.getEntityMetaByKey(Model.METADATA_KEY, null);
        }
        return entityMeta;
    }

    /**
     * Returns attribute label.
     * @param key key
     * @return attribute label
     */
    public String getAttributeLabel(String key) {
        return getEntityMeta().getAttributeMetadata(Model.ModelAttributeMeta.valueOf(key).getKey()).getName().getDescription(userProfile.getLocale());
    }

    /**
     * Returns draft tab selected flag.
     * @return draft tab selection
     */
    public Boolean getDraftTabSelected() {
        return draftTabSelected;
    }

    /**
     * Clears models selections.
     * @param draftTabSelect draft tab select
     */
    @Command
    @SmartNotifyChange("*")
    public void selectTab(@BindingParam("draftTabSelect") Boolean draftTabSelect) {
        selected.clear();
        draftTabSelected = draftTabSelect;
    }

    /**
     * Edits models filter.
     */
    @Command
    public void editModelsFilter() {
        Map<String, Object> map = new HashMap<>();
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(Model.METADATA_KEY, null);
        map.put("entityMeta", entityMeta);
        Window window = (Window) Executions.createComponents(pages.getProperty("entity.edit_entities_filter"), null, map);
        window.doModal();
    }

    /**
     * Resets models filter.
     */
    @GlobalCommand
    @SmartNotifyChange("*")
    public void resetEntitiesFilter() {
        entityFilters = new ListModelList<>(userProfile.getFiltersByKey(Model.METADATA_KEY));
        entityFilters.setMultiple(true);
        updateCriteria();
        entityFilters.stream().forEach(ef -> entityFilters.addToSelection(ef));
        resetModels();
    }

    /**
     * Deletes models filter.
     */
    @Command
    @SmartNotifyChange("*")
    public void deleteModelsFilter() {
        userProfile.setFiltersByKey(Model.METADATA_KEY, new ArrayList<>(entityFilters.getSelection()));
        resetEntitiesFilter();
    }

    /**
     * Adds model.
     */
    @Command
    public void addModel() {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "calc.model.model");
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Edits model.
     * @param model model
     */
    @Command
    public void editModel(@BindingParam("model") Model model) {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "calc.model.model");
        Executions.getCurrent().setAttribute("model", model);
        Executions.getCurrent().setAttribute("key", model.getKey());
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Publishes selected model.
     */
    @Command
    @SmartNotifyChange("*")
    public void publishModel() {
        EventListener<Messagebox.ClickEvent> clickListener = event -> {
            if (Messagebox.Button.YES.equals(event.getButton())) {
                selected.forEach(selectedModel -> calcService.publishModel(selectedModel));
                BindUtils.postGlobalCommand(null, null, "resetModels", null);
            }
        };
        Messagebox.show(String.format("%s (%d)", Labels.getLabel("model_list_publish_model_message"), selected.size()),
                        Labels.getLabel("model_list_publish_model_title"),
                        new Messagebox.Button[] {Messagebox.Button.YES, Messagebox.Button.NO}, Messagebox.QUESTION, clickListener);

    }

    /**
     * Publishes selected model.
     */
    @Command
    @SmartNotifyChange("*")
    public void deleteModel() {
        EventListener<Messagebox.ClickEvent> clickListener = event -> {
            if (Messagebox.Button.YES.equals(event.getButton())) {
                selected.forEach(selectedModel -> calcService.removeModel(selectedModel));
                BindUtils.postGlobalCommand(null, null, "resetModels", null);
            }
        };
        Messagebox.show(String.format("%s (%d)", Labels.getLabel("model_list_delete_model_message"), selected.size()),
                        Labels.getLabel("model_list_delete_model_title"),
                        new Messagebox.Button[] {Messagebox.Button.YES, Messagebox.Button.NO}, Messagebox.QUESTION, clickListener);

    }

    /**
     * Refreshes models.
     */
    @GlobalCommand
    public void modelsRefresh() {
        BindUtils.postNotifyChange(null, null, this, "publishedModel");
        BindUtils.postNotifyChange(null, null, this, "draftModel");
    }

    /**
     * Resets models.
     */
    @GlobalCommand
    public void resetModels() {
        publishedModels = null;
        draftModels = null;
        modelsRefresh();
    }

    /**
     * Updates criteria.
     */
    private void updateCriteria() {
        criteria = new Criteria();
        Where where = criteria.getWhere();
        for (EntityFilter entityFilter : entityFilters) {
            where.addItem(new WhereItem(entityFilter.getReferencedAttributeMetaPair().getAttributeMeta(), entityFilter.getOperator(),
                                        entityFilter.getValue()));
        }
        publishedActivePage = 0;
        draftActivePage = 0;
    }

    /**
     * Are selected models have published.
     * @return true if selected models have published
     */
    public boolean isSelectedHavePublished() {
        return selected.stream().anyMatch(Model::isPublished);
    }
}