package ru.masterdm.crs.web.model.calc.formula;

import static ru.masterdm.crs.domain.calc.EvalLang.NASHORN;
import static ru.masterdm.crs.domain.calc.FormulaResultType.NUMBER;
import static ru.masterdm.crs.domain.entity.meta.FormulaType.SYS_LIBRARY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Treerow;

import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.calc.FormulaData;
import ru.masterdm.crs.domain.calc.FormulaDependencyPair;
import ru.masterdm.crs.domain.calc.FormulaResultType;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.FormulaType;
import ru.masterdm.crs.service.CalcService;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.web.domain.UserProfile;

/**
 * Formula view model class.
 * @author Mikhail Kuzmin
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class FormulaViewModel {

    @WireVariable
    private CalcService calcService;
    @WireVariable("pages")
    private Properties pages;
    @WireVariable("config")
    private Properties config;
    @WireVariable("userProfile")
    private UserProfile userProfile;
    @WireVariable
    private EntityMetaService entityMetaService;

    private Pattern formulaVarPattern;
    private Formula formula;
    private List<FormulaResultType> resultTypeList = new ArrayList<>();
    private ListModelList<Formula> childLibraryModel = new ListModelList<>();
    private boolean edit = true;
    private EntityMeta entityMeta;
    private List<Formula> sysLibs;

    /**
     * Initiates context for formula add/edit.
     * @param formula formula
     */
    @Init
    public void initSetup(@ExecutionParam("formula") Formula formula) {
        formulaVarPattern = Pattern.compile(config.getProperty("formula.variable.regexp"), Pattern.CASE_INSENSITIVE);
        childLibraryModel.setMultiple(true);
        if (formula == null) {
            String key = (String) Executions.getCurrent().getAttribute("key");
            if (key != null)
                this.formula = calcService.getFormulaByKey(key, null);
        } else
            this.formula = formula;
        if (this.formula == null) {
            this.formula = new Formula();
            MultilangDescription name = new MultilangDescription();
            MultilangDescription comment = new MultilangDescription();
            FormulaData formulaData = new FormulaData();
            this.formula.setName(name);
            this.formula.setComment(comment);
            this.formula.setFormula(formulaData);
            this.formula.setResultType(NUMBER);
            this.formula.setType(FormulaType.FORMULA);
            this.formula.setEvalLang(NASHORN.name().toLowerCase());
            edit = false;
        } else {
            this.formula = calcService.getFormulaByKey(formula.getKey(), null);
            if (this.formula.getComment() == null) {
                this.formula.setComment(new MultilangDescription());
            }
            this.formula.getChildren().forEach(fm -> {
                if (fm.getRight().isLibrary()) {
                    childLibraryModel.add(fm.getRight());
                    childLibraryModel.addToSelection(fm.getRight());
                }
            });
        }
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
     * Returns formula.
     * @return formula
     */
    public Formula getFormula() {
        return formula;
    }

    /**
     * Returns child library model.
     * @return child library model
     */
    public ListModelList<Formula> getChildLibraryModel() {
        return childLibraryModel;
    }

    /**
     * Returns formula key constraint.
     * @return formula key constraint
     */
    public String getFormulaKeyConstraint() {
        return String.format("no empty,/%s/", config.getProperty("formula.key.regexp"));
    }

    /**
     * Sets formula.
     * @param formula formula
     */
    public void setFormula(Formula formula) {
        this.formula = formula;
    }

    /**
     * Returns result type list.
     * @return result type list
     */
    public final List<FormulaResultType> getResultTypeList() {
        resultTypeList.clear();
        Collections.addAll(resultTypeList, FormulaResultType.values());
        return resultTypeList;
    }

    /**
     * Returns result type label for combobox.
     * @param formulaResultType formula result type
     * @return result type label
     */
    public String getResultTypeLabel(FormulaResultType formulaResultType) {
        return Labels.getLabel(String.valueOf(formulaResultType));
    }

    /**
     * Checks if formula got non library children.
     * @return true|false
     */
    public Boolean isFormulaChildrenNonLibraryExist() {
        return formula.getChildren().stream()
                      .filter(p -> (!p.getRight().isLibrary()))
                      .count() > 0;
    }

    /**
     * Drop parameter to variables grid.
     * @param event DropEvent
     */
    @Command
    @NotifyChange({"formula", "childLibraryModel"})
    public void dropVariable(@ContextParam(ContextType.TRIGGER_EVENT) DropEvent event) {
        Treerow row = (Treerow) event.getDragged();
        Treeitem item = (Treeitem) row.getParent();
        Formula fm = item.getValue();
        if (formula.getKey() != null && formula.getKey().toUpperCase().equals(fm.getKey())) {
            Messagebox.show(Labels.getLabel("formula_duplicate_self_message"), Labels.getLabel("messagebox_validation"),
                            Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }
        formula.getChildren().forEach(i -> {
            if (i.getRight().getKey().equals(fm.getKey())) {
                Messagebox.show(Labels.getLabel("formula_duplicate_variable_message"), Labels.getLabel("messagebox_validation"),
                                Messagebox.OK, Messagebox.EXCLAMATION);
                return;
            }
        });
        if (fm.isLibrary()) {
            childLibraryModel.add(fm);
            childLibraryModel.addToSelection(fm);
            formula.addChild(null, fm);
            return;
        }
        formula.addChild("v_" + fm.getKey().toLowerCase(), fm);
    }

    /**
     * Removes variable from children.
     * @param variable child formula
     */
    @Command
    @NotifyChange({"formula", "childLibraryModel"})
    public void removeVariable(@BindingParam("variable") Formula variable) {
        List<FormulaDependencyPair> children = new ArrayList<>();
        for (FormulaDependencyPair child : formula.getChildren()) {
            if (!child.getRight().getKey().equals(variable.getKey())) {
                children.add(FormulaDependencyPair.of(child.getLeft(), child.getRight()));
            }
        }
        formula.setChildren(children);
    }

    /**
     * Removes library from children.
     */
    @Command
    @NotifyChange({"formula", "childLibraryModel"})
    public void removeLibrary() {
        List<FormulaDependencyPair> children = new ArrayList<>();
        List<String> libraryChildKeys = new ArrayList<>();
        childLibraryModel.getSelection().stream()
                         .filter(Formula::isLibrary)
                         .forEach(child -> libraryChildKeys.add(child.getKey()));
        for (FormulaDependencyPair child : formula.getChildren()) {
            if (!child.getRight().isLibrary() || libraryChildKeys.stream().anyMatch(i -> i.equals(child.getRight().getKey()))) {
                children.add(FormulaDependencyPair.of(child.getLeft(), child.getRight()));
            }
        }
        formula.setChildren(children);
    }

    /**
     * Checks variable name on change.
     * @param variable variable
     */
    @Command
    @NotifyChange({"formula"})
    public void checkVariable(@BindingParam("variable") FormulaDependencyPair variable) {
        if (!formulaVarPattern.matcher(variable.getLeft()).matches()) {
            Messagebox.show("\"" + variable.getKey() + "\": " + Labels.getLabel("exception_formula_variable_name_regexp_message"),
                            Labels.getLabel("messagebox_validation"), Messagebox.OK, Messagebox.EXCLAMATION);
        }
    }

    /**
     * Refreshes formula on library change.
     */
    @Command
    @NotifyChange({"formula"})
    public void refreshFormula() {
    }

    /**
     * Persists formula.
     */
    @Command
    public void persistFormula() {
        if (!edit && calcService.isFormulaExists(getFormula().getKey().toUpperCase())) {
            Messagebox.show(Labels.getLabel("formula_duplicate_key_message"), Labels.getLabel("messagebox_validation"), Messagebox.OK,
                            Messagebox.EXCLAMATION);
            return;
        }
        if (!edit && formula.isLibrary()) {
            formula.setResultType(null);
        }
        if (formula.getFormula().getData().isEmpty()) {
            Messagebox.show(Labels.getLabel("formula_data_is_empty_message"), Labels.getLabel("messagebox_validation"), Messagebox.OK,
                            Messagebox.EXCLAMATION);
            return;
        }
        formula.setKey(getFormula().getKey().toUpperCase());
        calcService.persistFormula(formula);
        navigateFormulas();
    }

    /**
     * Navigate formulas.
     */
    @Command
    public void navigateFormulas() {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "calc.formula.formula_list");
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Returns is edit.
     * @return is edit
     */
    public boolean isEdit() {
        return edit;
    }

    /**
     * Returns formula types map.
     * @return formula types map
     */
    public Map<String, String> getFormulaTypes() {
        Map<String, String> types = Arrays.stream(FormulaType.values())
                                          .filter(ft -> ft != SYS_LIBRARY)
                                          .collect(Collectors.toMap(Enum::name, v -> Labels.getLabel(v.name()),
                                                                    (u, v) -> {
                                                                        return u;
                                                                    },
                                                                    LinkedHashMap::new));
        return types;
    }

    /**
     * Add system library in variables.
     * @param rootFormula root formula
     */
    @GlobalCommand
    @SmartNotifyChange("*")
    public void addSystemLibrary(@BindingParam(value = "rootFormula") Formula rootFormula) {
        if (rootFormula != null) {
            if (rootFormula.getChildren() != null && rootFormula.getChildren().size() != 0) {
                if (sysLibs == null)
                    sysLibs = getSysLibs(rootFormula);

                if (sysLibs.size() != 0) {
                    for (Formula lib : sysLibs) {
                        this.childLibraryModel.add(lib);
                        this.childLibraryModel.addToSelection(lib);
                        this.formula.addChild(null, lib);
                    }

                    BindUtils.postNotifyChange(null, null, this, "formula.children");
                    BindUtils.postNotifyChange(null, null, this, "childLibraryModel");
                }
            }
        }
    }

    /**
     * Gets sys libraries.
     * @param root root
     * @return sys libraries
     */
    private List<Formula> getSysLibs(Formula root) {
        List<Formula> libs = new ArrayList<>();
        for (FormulaDependencyPair fdp : root.getChildren()) {
            Formula formula = fdp.getRight();

            if (formula.getEvalLang().equalsIgnoreCase(this.formula.getEvalLang())
                && formula.isSysLibrary())
                if (!libs.contains(formula))
                    libs.add(formula);

            if (formula.getChildren() != null && formula.getChildren().size() != 0) {
                List<Formula> childLibs = getSysLibs(formula);
                if (childLibs != null && childLibs.size() != 0) {
                    for (Formula lib : childLibs) {
                        if (!libs.contains(lib)) libs.add(lib);
                    }
                }
            }
        }
        return libs;
    }
}
