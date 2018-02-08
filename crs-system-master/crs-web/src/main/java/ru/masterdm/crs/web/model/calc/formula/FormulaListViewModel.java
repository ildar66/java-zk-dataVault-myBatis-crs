package ru.masterdm.crs.web.model.calc.formula;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
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
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.Where;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeLocale;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.FormulaType;
import ru.masterdm.crs.service.CalcService;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.web.domain.ReferencedAttributeMetaPair;
import ru.masterdm.crs.web.domain.UserProfile;
import ru.masterdm.crs.web.domain.entity.EntityFilter;

/**
 * Formula list view model class.
 * @author Mikhail Kuzmin
 * @author Igor Matushak
 * @author Alexey Kirilchev
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class FormulaListViewModel {

    public static final String FORMULA_EDIT_FILTER_KEY = Formula.METADATA_KEY + "_EDIT";
    public static final String FORMULA_MODEL_FILTER_KEY = Formula.METADATA_KEY + "_MODEL";
    public static final String FORMULA_CALCULATION_FILTER_KEY = Formula.METADATA_KEY + "_CALCULATION";

    @WireVariable
    private CalcService calcService;
    @WireVariable
    private EntityMetaService entityMetaService;
    @WireVariable("pages")
    private Properties pages;
    @WireVariable("userProfile")
    private UserProfile userProfile;

    private List<Formula> selected;
    private ListModelList<EntityFilter> entityFilters;
    private EntityMeta entityMeta;
    private Criteria criteria;
    private String filterKey;
    private FormulaTreeModel treeModel;
    private Calculation calculation;

    protected LocalDateTime actuality;
    private Set<Long> formulaIds;

    /**
     * Returns formula tree model.
     * @return tree model
     */
    public FormulaTreeModel getTreeModel() {
        if (treeModel == null && !(calculation != null && calculation.getId() == null)) {
            treeModel = new FormulaTreeModel(getRootFormula());
            treeModel.setMultiple(true);
            Map<String, Object> args = new HashMap<>();
            args.put("rootFormula", treeModel.getRoot());
            BindUtils.postGlobalCommand(null, null, "addSystemLibrary", args);
        }
        return treeModel;
    }

    /**
     * Sets tree model.
     * @param treeModel tree model
     */
    public void setTreeModel(FormulaTreeModel treeModel) {
        this.treeModel = treeModel;
    }

    /**
     * Returns selected Formula.
     * @return selected Formula
     */
    public List<Formula> getSelected() {
        if (selected == null)
            selected = new ArrayList<>();
        return selected;
    }

    /**
     * Sets selectedFormula.
     * @param selected selected formula
     */
    public void setSelected(List<Formula> selected) {
        this.selected = selected;
    }

    /**
     * Returns root formula.
     * @return root formula
     */
    private Formula getRootFormula() {
        Formula rootFormula = new Formula();

        List<Formula> formulas = null;
        if (calculation != null)
            formulas = calcService.getFormulaTrees(criteria, calculation, null);
        else
            formulas = calcService.getFormulaTrees(criteria, actuality);
        formulas.stream().forEach(f -> rootFormula.addChild(null, f));
        return rootFormula;
    }

    /**
     * Adds formula.
     */
    @Command
    public void addFormula() {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "calc.formula.formula");
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Navigates to formula edit.
     * @param formula formula
     */
    @Command
    public void editFormula(@BindingParam("formula") Formula formula) {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "calc.formula.formula");
        Executions.getCurrent().setAttribute("formula", formula);
        Executions.getCurrent().setAttribute("key", formula.getKey());
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Removes selected formula.
     */
    @Command
    @NotifyChange({"treeModel"})
    public void removeFormula() {
        EventListener<Messagebox.ClickEvent> clickListener = event -> {
            if (Messagebox.Button.YES.equals(event.getButton())) {
                selected.forEach(formula -> {
                    Formula fm = calcService.getFormulaByKey(formula.getKey(), null);
                    calcService.removeFormula(fm);
                });
                Map<String, Object> map = new HashMap<>();
                map.put("targetPage", "calc.formula.formula_list");
                BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
            }
        };
        Messagebox.show(String.format("%s (%d)?", Labels.getLabel("formula_list_remove_formula_message"), selected.size()),
                        Labels.getLabel("formula_list_remove_formula_title"),
                        new Messagebox.Button[] {Messagebox.Button.YES, Messagebox.Button.NO}, Messagebox.QUESTION, clickListener);
    }

    /**
     * Returns entity filters.
     * @return entity filters
     */
    public ListModelList<EntityFilter> getEntityFilters() {
        return entityFilters;
    }

    /**
     * Edits calculations filter.
     */
    @Command
    public void editCalculationsFilter() {
        Map<String, Object> map = new HashMap<>();
        map.put("entityMeta", getEntityMeta());
        map.put("restrictedAttributes", (Predicate<AttributeMeta>) attributeMeta -> {
            String key = attributeMeta.getKey();
            return key.equals(Formula.FormulaAttributeMeta.TYPE.getKey())
                   || (userProfile.getLocale().equals(AttributeLocale.RU) ? key.equals(Formula.FormulaAttributeMeta.NAME_RU.getKey())
                                                                          : key.equals(Formula.FormulaAttributeMeta.NAME_EN.getKey()));
        });
        map.put("filterKey", filterKey);
        map.put("resetFilterName", "resetFormulasFilter");
        Window window = (Window) Executions.createComponents(pages.getProperty("entity.edit_entities_filter"), null, map);
        window.doModal();
    }

    /**
     * Changes actuality.
     * @param actuality actuality
     */
    @GlobalCommand
    @NotifyChange("treeModel")
    public void actualityChanged(@BindingParam(value = "actuality") LocalDateTime actuality) {
        if (FORMULA_MODEL_FILTER_KEY.equals(filterKey) || FORMULA_CALCULATION_FILTER_KEY.equals(filterKey)) {
            this.actuality = actuality;
            treeModel = null;
        }
    }

    /**
     * Calculation formula results changed.
     * @param actuality actuality
     * @param calculation calculation
     */
    @GlobalCommand
    @NotifyChange("treeModel")
    public void calculationFormulaResultsChanged(@BindingParam(value = "actuality") LocalDateTime actuality,
                                                 @BindingParam(value = "calculation") Calculation calculation) {
        this.calculation = calculation;
        treeModel = null;
        formulaIds = null;
    }

    /**
     * Deletes calculations filter.
     */
    @Command
    @SmartNotifyChange("*")
    public void deleteCalculationsFilter() {
        userProfile.setFiltersByKey(filterKey, new ArrayList<>(entityFilters.getSelection()));
        resetFormulasFilter();
    }

    /**
     * Returns entity meta.
     * @return entity meta
     */
    public EntityMeta getEntityMeta() {
        if (entityMeta == null) {
            entityMeta = entityMetaService.getEntityMetaByKey(Formula.METADATA_KEY, null);
        }
        return entityMeta;
    }

    /**
     * Initiates context.
     * @param filterKeyParam filter key. Not null if view model embedded in form with other view model
     * @param library true if library filter, false otherwise
     * @param actuality actuality
     * @param calculation calculation
     */
    @Init
    public void initSetup(@BindingParam("filterKey") String filterKeyParam, @BindingParam("library") Boolean library,
                          @BindingParam("actuality") LocalDateTime actuality, @BindingParam("calculation") Calculation calculation) {
        this.actuality = actuality;
        this.calculation = calculation;
        filterKey = Formula.METADATA_KEY + (filterKeyParam != null ? filterKeyParam : "");
        if (filterKeyParam != null) {
            userProfile.setFiltersByKey(filterKey, getDefaultFilters(filterKey, library));
        }

        List<EntityFilter> filtersByKey = userProfile.getFiltersByKey(filterKey);
        entityFilters = new ListModelList<>(filtersByKey);
        entityFilters.setMultiple(true);
        filtersByKey.stream().forEach(ef -> entityFilters.addToSelection(ef));
        updateCriteria();
    }

    /**
     * Returns default filters.
     * @param filterKey filter key
     * @param library formula is library
     * @return default filters
     */
    private List<EntityFilter> getDefaultFilters(String filterKey, Boolean library) {
        List<EntityFilter> results = new ArrayList<>();
        if (filterKey.equals(FORMULA_EDIT_FILTER_KEY) && Boolean.TRUE.equals(library)) {
            EntityFilter entityFilter = new EntityFilter();
            entityFilter.setOperator(Operator.EQ);
            entityFilter.setDateFormat(Labels.getLabel("date_format"));
            entityFilter.setDateTimeFormat(Labels.getLabel("date_time_format"));
            AttributeMeta libraryAttribute = getEntityMeta().getAttributes().stream()
                                                            .filter(p -> p.getKey().equals(Formula.FormulaAttributeMeta.TYPE.getKey()))
                                                            .findFirst().orElse(null);
            entityFilter.setReferencedAttributeMetaPair(ReferencedAttributeMetaPair.of(libraryAttribute, null));
            entityFilter.setLocale(userProfile.getLocale());
            entityFilter.setValue(FormulaType.LIBRARY);
            results.add(entityFilter);
        }
        return results;
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
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(Formula.METADATA_KEY, null);

        if (FORMULA_CALCULATION_FILTER_KEY.equals(filterKey)) {
            where.addItem(new WhereItem(entityMeta.getAttributeMetadata(Formula.FormulaAttributeMeta.TYPE.getKey()), Operator.NOT_IN,
                                        FormulaType.LIBRARY, FormulaType.SYS_LIBRARY));
        }
        if (FORMULA_MODEL_FILTER_KEY.equals(filterKey) || FORMULA_CALCULATION_FILTER_KEY.equals(filterKey)) {
            where.addItem(new WhereItem(entityMeta.getAttributeMetadata(Formula.FormulaAttributeMeta.TYPE.getKey()), Operator.NOT_IN,
                                        FormulaType.LIBRARY, FormulaType.SYS_LIBRARY, FormulaType.PRECALCULATED_FORMULA));
        }
        criteria.getOrder().addItem(entityMeta.getAttributeMetadata(userProfile.getLocale().equals(AttributeLocale.RU)
                                                                    ? Formula.FormulaAttributeMeta.NAME_RU.getKey()
                                                                    : Formula.FormulaAttributeMeta.NAME_EN.getKey()),
                                    false);
        criteria.getOrder().addItem(entityMeta.getKeyAttribute(), false);
    }

    /**
     * Resets models filter.
     */
    @GlobalCommand
    @SmartNotifyChange("*")
    public void resetFormulasFilter() {
        entityFilters = new ListModelList<>(userProfile.getFiltersByKey(filterKey));
        entityFilters.setMultiple(true);
        updateCriteria();
        entityFilters.stream().forEach(ef -> entityFilters.addToSelection(ef));
        resetModels();
    }

    /**
     * Resets models filter.
     */
    @GlobalCommand
    @SmartNotifyChange("*")
    public void resetFormulas() {
        entityFilters = new ListModelList<>(userProfile.getFiltersByKey(filterKey));
        entityFilters.setMultiple(true);
        updateCriteria();
        entityFilters.stream().forEach(ef -> entityFilters.addToSelection(ef));
        resetModels();
    }

    /**
     * Resets models.
     */
    private void resetModels() {
        treeModel = null;
        BindUtils.postNotifyChange(null, null, this, "treeModel");
    }

    /**
     * Returns is formula exists in the model.
     * @param formula formula
     * @return is formula exists in the Model
     */
    public Boolean getIsFormulaExistsInModel(Formula formula) {
        if (formulaIds == null) {
            formulaIds = calculation.getModel().getFormulas().stream().map(Formula::getHubId).collect(Collectors.toSet());
        }
        return formulaIds.contains(formula.getHubId());
    }

    /**
     * Returns short formula exception short message.
     * @param formula formula
     * @param profile profile
     * @return short formula exception short message
     */
    public String getFormulaExceptionShortMessage(Formula formula, Entity profile) {
        String result = formula.getFormulaResult(calculation, profile) != null
                        ? formula.getFormulaResult(calculation, profile).getException()
                        : null;
        int newLineIndex = -1;
        if (result != null && (newLineIndex = result.indexOf("\n")) > 0) {
            result = result.substring(0, newLineIndex);
            result = result.replaceAll("java.util.concurrent.ExecutionException: ", "");
            result = result.replaceAll("javax.script.ScriptException: ", "");
        }
        return result;
    }

    /**
     * Show detail info.
     * @param text text
     * @param title title
     */
    @Command
    public void showDetailInfo(@BindingParam("text") String text,
                               @BindingParam("title") String title) {
        Map<String, Object> map = new HashMap<>();
        map.put("text", text);
        map.put("title", title);
        map.put("isEdit", false);
        Window window = (Window) Executions.createComponents(pages.getProperty("edit_text"), null, map);
        window.doModal();
    }
}
