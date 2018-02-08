package ru.masterdm.crs.web.model.calc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Default;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.OrderItem;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.CalculationProfileAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.FormulaType;
import ru.masterdm.crs.service.CalcService;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.web.domain.UserProfile;
import ru.masterdm.crs.web.domain.entity.EntityFilter;
import ru.masterdm.crs.web.service.CriteriaBuilderUiService;

/**
 * Calculations view model class.
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class CalculationsViewModel {

    @WireVariable
    private EntityService entityService;
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
    @WireVariable("criteriaBuilderUiService")
    private CriteriaBuilderUiService criteriaBuilderUiService;

    private int pageSize;

    private int publishedActivePage = 0;
    private long publishedTotalSize;

    private int activePage = 0;
    private long totalSize;

    private int draftActivePage = 0;
    private long draftTotalSize;

    private ListModelList<EntityFilter> entityFilters;
    private Criteria criteria;

    private ListModelList<Calculation> publishedCalculations;
    private ListModelList<Calculation> calculations;
    private ListModelList<Calculation> draftCalculations;

    private EntityMeta entityMeta;
    private List<Calculation> selected;
    private int selectedIndex;
    private Entity entity;
    private List<? extends Entity> profiles;

    /**
     * Initiates context.
     * @param entity entity for default filter
     */
    @Init
    public void initSetup(@BindingParam("entity") Entity entity) {
        pageSize = Integer.parseInt(webConfig.getProperty("pageSize"));
        entityFilters = new ListModelList<>(userProfile.getFiltersByKey(Calculation.METADATA_KEY));
        entityFilters.setMultiple(true);
        userProfile.getFiltersByKey(Calculation.METADATA_KEY).forEach(ef -> entityFilters.addToSelection(ef));
        selected = new ArrayList<>();
        if (entity != null)
            this.entity = entity;
        else
            this.entity = (Entity) Executions.getCurrent().getAttribute("entity");
        updateCriteria();
    }

    /**
     * Returns published total size.
     * @return published total size
     */
    public long getPublishedTotalSize() {
        return publishedTotalSize;
    }

    /**
     * Returns total size.
     * @return total size
     */
    public long getTotalSize() {
        return totalSize;
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
     * Sets published active page.
     * @param publishedActivePage published active page
     */
    @NotifyChange({"publishedModel", "selected"})
    public void setPublishedActivePage(int publishedActivePage) {
        this.publishedActivePage = publishedActivePage;
        publishedCalculations = null;
        selected.clear();
    }

    /**
     * Sets active page.
     * @param activePage active page
     */
    @NotifyChange({"model", "selected"})
    public void setActivePage(int activePage) {
        this.activePage = activePage;
        calculations = null;
        selected.clear();
    }

    /**
     * Sets draft active page.
     * @param draftActivePage draft active page
     */
    @NotifyChange({"draftModel", "selected"})
    public void setDraftActivePage(int draftActivePage) {
        this.draftActivePage = draftActivePage;
        draftCalculations = null;
        selected.clear();
    }

    /**
     * Edits calculations filter.
     */
    @Command
    public void editCalculationsFilter() {
        Map<String, Object> map = new HashMap<>();
        map.put("entityMeta", getEntityMeta());
        map.put("restrictedAttributes",
                (Predicate<AttributeMeta>) attributeMeta -> !attributeMeta.getKey().equals(Calculation.CalculationAttributeMeta.PUBLISHED.getKey()));
        Window window = (Window) Executions.createComponents(pages.getProperty("entity.edit_entities_filter"), null, map);
        window.doModal();
    }

    /**
     * Adds calculation.
     * @param entity Client entity
     */
    @Command
    public void addCalculation(@BindingParam("entity") Entity entity) {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "calc.edit_calculation");
        Executions.getCurrent().setAttribute("entity", entity);
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Copies calculation.
     */
    @Command
    public void copyCalculation() {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "calc.edit_calculation");
        Executions.getCurrent().setAttribute("copiedCalculation", selected.get(0));
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Edits calculation.
     * @param calculation selected calculation
     */
    @Command
    public void editCalculation(@BindingParam("calculation") Calculation calculation) {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "calc.edit_calculation");
        Executions.getCurrent().setAttribute("calculation", calculation);
        Executions.getCurrent().setAttribute("key", calculation.getKey());
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Resets entities filter.
     */
    @SmartNotifyChange("*")
    @GlobalCommand
    public void resetEntitiesFilter() {
        entityFilters = new ListModelList<>(userProfile.getFiltersByKey(Calculation.METADATA_KEY));
        entityFilters.setMultiple(true);

        updateCriteria();

        entityFilters.stream().forEach(ef -> entityFilters.addToSelection(ef));
        resetCalculations();
        selected.clear();
    }

    /**
     * Deletes calculations filter.
     */
    @Command
    @SmartNotifyChange("*")
    public void deleteCalculationsFilter() {
        userProfile.setFiltersByKey(Calculation.METADATA_KEY, new ArrayList<>(entityFilters.getSelection()));
        resetEntitiesFilter();
    }

    /**
     * Deletes calculation.
     */
    @Command
    public void deleteCalculation() {
        EventListener<Messagebox.ClickEvent> clickListener = event -> {
            if (Messagebox.Button.YES.equals(event.getButton())) {
                selected.forEach(calculation -> calcService.removeCalculation(calculation));
                selected.clear();
                BindUtils.postGlobalCommand(null, null, "resetCalculations", null);
            }
        };
        Messagebox.show(String.format("%s (%d)", Labels.getLabel("calculations_remove_calculation_message"), selected.size()),
                        Labels.getLabel("calculations_remove_calculation_title"),
                        new Messagebox.Button[] {Messagebox.Button.YES, Messagebox.Button.NO}, Messagebox.QUESTION, clickListener);

    }

    /**
     * Publishes calculation.
     */
    @Command
    public void publishCalculation() {
        if (!isSelectedCalculated()) {
            Messagebox.show(Labels.getLabel("exception_calc_publish_not_calculated"),
                            Labels.getLabel("calculations_publish_calculation_title"),
                            new Messagebox.Button[] {Messagebox.Button.OK},
                            Messagebox.INFORMATION, null);
            return;
        }
        EventListener<Messagebox.ClickEvent> clickListener = event -> {
            if (Messagebox.Button.YES.equals(event.getButton())) {
                selected.forEach(calculation -> calcService.publishCalculation(calculation));
                selected.clear();
                BindUtils.postGlobalCommand(null, null, "resetCalculations", null);
            }
        };
        Messagebox.show(String.format("%s (%d)", Labels.getLabel("calculations_publish_calculation_message"), selected.size()),
                        Labels.getLabel("calculations_publish_calculation_title"),
                        new Messagebox.Button[] {Messagebox.Button.YES, Messagebox.Button.NO}, Messagebox.QUESTION, clickListener);

    }

    /**
     * Clears calculation selections.
     */
    @Command
    public void selectTab() {
        selected = new ArrayList<>();
        calculationsRefresh();
    }

    /**
     * Returns published model.
     * @return published model
     */
    public ListModelList<Calculation> getPublishedModel() {
        if (publishedCalculations == null && selectedIndex == 1) {
            publishedCalculations = new ListModelList(getPublishedCalculations());
            publishedCalculations.setMultiple(true);
        }
        return publishedCalculations;
    }

    /**
     * Returns published calculations.
     * @return published calculations
     */
    public List<Calculation> getPublishedCalculations() {
        RowRange rowRange = RowRange.newAsPageAndSize(publishedActivePage, pageSize);
        List<Calculation> calculations = calcService.getPublishedCalculations(criteria, rowRange, null);
        publishedTotalSize = rowRange.getTotalCount();
        return calculations;
    }

    /**
     * Returns model.
     * @return model
     */
    public ListModelList<Calculation> getModel() {
        if (calculations == null && selectedIndex == 2) {
            calculations = new ListModelList(getCalculations());
            calculations.setMultiple(true);
        }
        return calculations;
    }

    /**
     * Returns calculations.
     * @return calculations
     */
    public List<Calculation> getCalculations() {
        RowRange rowRange = RowRange.newAsPageAndSize(activePage, pageSize);
        List<Calculation> calculations = calcService.getCalculations(criteria, rowRange, null);
        totalSize = rowRange.getTotalCount();
        return calculations;
    }

    /**
     * Returns draft model.
     * @return draft model
     */
    public ListModelList<Calculation> getDraftModel() {
        if (draftCalculations == null && selectedIndex == 0) {
            draftCalculations = new ListModelList(getDraftCalculations());
            draftCalculations.setMultiple(true);
        }
        return draftCalculations;
    }

    /**
     * Returns draft calculations.
     * @return draft calculations
     */
    public List<Calculation> getDraftCalculations() {
        RowRange rowRange = RowRange.newAsPageAndSize(draftActivePage, pageSize);
        List<Calculation> calculations = calcService.getDraftCalculations(criteria, rowRange, null);
        draftTotalSize = rowRange.getTotalCount();
        return calculations;
    }

    /**
     * Refreshes calculations.
     */
    public void calculationsRefresh() {
        BindUtils.postNotifyChange(null, null, this, "publishedModel");
        BindUtils.postNotifyChange(null, null, this, "model");
        BindUtils.postNotifyChange(null, null, this, "draftModel");
        BindUtils.postNotifyChange(null, null, this, "publishedTotalSize");
        BindUtils.postNotifyChange(null, null, this, "totalSize");
        BindUtils.postNotifyChange(null, null, this, "draftTotalSize");
    }

    /**
     * Resets calculations.
     */
    @GlobalCommand
    @SmartNotifyChange("*")
    public void resetCalculations() {
        selected.clear();
        publishedCalculations = null;
        calculations = null;
        draftCalculations = null;
        calculationsRefresh();
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
    private void updateCriteria() {
        criteria = criteriaBuilderUiService.getCriteriaByEntityFilters(entityFilters, entityMetaService);
        if (entity != null) {
            AttributeMeta attributeMeta = getEntityMeta().getAttributes()
                                                         .stream()
                                                         .filter(a -> a.getType().equals(AttributeType.REFERENCE))
                                                         .filter(a -> a.getEntityKey().equals(entity.getMeta().getKey()))
                                                         .findFirst().orElse(null);
            if (attributeMeta != null)
                criteria.addReferencedEntity(entity);
        }
        criteria.getOrder().addItem(getEntityMeta().getHubLdtsAttribute(), true);
        publishedActivePage = 0;
        activePage = 0;
        draftActivePage = 0;
    }

    /**
     * Returns published active page.
     * @return published active page
     */
    public int getPublishedActivePage() {
        return publishedActivePage;
    }

    /**
     * Returns active page.
     * @return active page
     */
    public int getActivePage() {
        return activePage;
    }

    /**
     * Returns draft active page.
     * @return draft active page
     */
    public int getDraftActivePage() {
        return draftActivePage;
    }

    /**
     * Returns entity meta.
     * @return entity meta
     */
    public EntityMeta getEntityMeta() {
        if (entityMeta == null) {
            entityMeta = entityMetaService.getEntityMetaByKey(Calculation.METADATA_KEY, null);
        }
        return entityMeta;
    }

    /**
     * Returns attribute label.
     * @param key key
     * @return attribute label
     */
    public String getAttributeLabel(String key) {
        return getEntityMeta().getAttributeMetadata(Calculation.CalculationAttributeMeta.valueOf(key).getKey())
                              .getName().getDescription(userProfile.getLocale());
    }

    /**
     * Returns selected calculations.
     * @return selected calculations
     */
    public List<Calculation> getSelected() {
        return selected;
    }

    /**
     * Sets selected calculations.
     * @param selected selected calculations
     */
    public void setSelected(List<Calculation> selected) {
        this.selected = selected;
    }

    /**
     * Disables onClick event for list item when clicking on 'check' icon.
     */
    @Command
    @SmartNotifyChange("selectedHavePublished")
    public void selectCalculation() {
    }

    /**
     * Are selected calculations have published.
     * @return true if selected calculations have published
     */
    public boolean isSelectedHavePublished() {
        return selected.stream().anyMatch(Calculation::isPublished);
    }

    /**
     * Are all selected calculations calculated.
     * @return true if all selected calculations were calculated
     */
    public boolean isSelectedCalculated() {
        return selected.stream().allMatch(Calculation::isCalculated);
    }

    /**
     * Returns selected index.
     * @return selected index
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Sets selected index.
     * @param selectedIndex selected index
     */
    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    /**
     * Gets entity.
     * @return entity
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Navigates client.
     * @param client Client
     * @param mode mode
     */
    @Command
    public void navigateClient(@BindingParam("client") Entity client, @BindingParam("mode") @Default("false") Boolean mode) {
        Executions.getCurrent().setAttribute("client", client);
        Executions.getCurrent().setAttribute("key", client.getKey());
        Executions.getCurrent().setAttribute("mode", mode);
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "entity.client");
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Navigates group.
     * @param clientGroup Group
     * @param mode mode
     */
    @Command
    public void navigateClientGroup(@BindingParam("clientGroup") Entity clientGroup, @BindingParam("mode") @Default("false") Boolean mode) {
        Executions.getCurrent().setAttribute("clientGroup", clientGroup);
        Executions.getCurrent().setAttribute("key", clientGroup.getKey());
        Executions.getCurrent().setAttribute("mode", mode);
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "entity.client_group");
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Returns master formulas.
     * @param calculation calculation
     * @return master formulas
     */
    public List<Formula> getMasterFormulas(Calculation calculation) {
        return (calculation.getModel() != null)
               ? calculation.getModel().getFormulas().stream().filter(formula -> formula.getType().equals(FormulaType.MASTER_FORMULA))
                            .collect(Collectors.toList())
               : null;
    }

    /**
     * Returns list of available profiles.
     * @return list of profiles
     */
    public List<? extends Entity> getProfiles() {
        if (profiles != null)
            return profiles;
        EntityMeta profileMeta = entityMetaService.getEntityMetaByKey(CalculationProfileAttributeMeta.METADATA_KEY, null);
        Criteria criteria = new Criteria();
        criteria.getOrder().addItem(new OrderItem(profileMeta.getKeyAttribute(), true));
        profiles = entityService.getEntities(profileMeta, criteria, null, null);
        return profiles;
    }

    /**
     * Returns calculation profile attribute meta key.
     * @param key key
     * @return calculation profile attribute meta key
     */
    public String getCalculationProfileAttributeKey(String key) {
        return CalculationProfileAttributeMeta.valueOf(key).getKey();
    }
}
