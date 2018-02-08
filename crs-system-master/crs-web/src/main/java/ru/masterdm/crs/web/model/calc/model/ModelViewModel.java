package ru.masterdm.crs.web.model.calc.model;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Row;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Treerow;

import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.attribute.EntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.criteria.Where;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.domain.entity.meta.FormulaType;
import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.domain.form.TemplateType;
import ru.masterdm.crs.service.CalcService;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.FormTemplateService;
import ru.masterdm.crs.web.domain.UserProfile;
import ru.masterdm.crs.web.domain.entity.EntityMetaStatus;

/**
 * Model view model class.
 * @author Mikhail Kuzmin
 * @author Alexey Kirilchev
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ModelViewModel {

    @WireVariable
    private CalcService calcService;
    @WireVariable
    private EntityService entityService;
    @WireVariable
    private EntityMetaService entityMetaService;
    @WireVariable("pages")
    private Properties pages;
    @WireVariable("userProfile")
    private UserProfile userProfile;
    @WireVariable("config")
    private Properties config;
    @WireVariable
    private FormTemplateService formTemplateService;

    private Model model;
    private EntityMeta entityMeta;
    private EntityMeta formEntityMeta;
    private EntityMeta formulaEntityMeta;
    private EntityMeta classifierEntityMeta;
    private List<EntityMeta> formEntityMetas;
    private boolean edit = true;

    private String classifierFilter;
    private String formTemplateFilter;

    private List<Formula> selectedFormulas;

    /**
     * Initiates context for model add/edit.
     * @param model model
     */
    @Init
    public void initSetup(@ExecutionParam("model") Model model) {
        if (model == null) {
            String key = (String) Executions.getCurrent().getAttribute("key");
            if (key != null) {
                Criteria criteria = new Criteria();
                criteria.getWhere().addItem(new WhereItem(getEntityMeta().getKeyAttribute(), Operator.EQ, key));
                List<Model> models = calcService.getModels(criteria, null, null);
                this.model = (models.size() > 0) ? models.get(0) : null;
            }
        } else
            this.model = model;
        this.selectedFormulas = new ArrayList<>();
        if (this.model == null) {
            this.model = (Model) entityService.newEmptyEntity(Model.METADATA_KEY);
            MultilangDescription name = new MultilangDescription();
            MultilangDescription comment = new MultilangDescription();
            this.model.setName(name);
            this.model.setComment(comment);
            this.model.setActuality(truncateDateToFormat(entityMetaService.getSysTimestamp(), getDateTimeFormat()));
            edit = false;
        } else {
            selectedFormulas.addAll(this.model.getFormulas());
        }
    }

    /**
     * Returns date and time format.
     * @return date and time format
     */
    public String getDateTimeFormat() {
        return Labels.getLabel("date_time_format");
    }

    /**
     * Returns truncated date using format.
     * @param dateTime date time
     * @param dateTimeFormat date time format
     * @return truncated date using format
     */
    private LocalDateTime truncateDateToFormat(LocalDateTime dateTime, String dateTimeFormat) {
        String date = dateTime.format(DateTimeFormatter.ofPattern(getDateTimeFormat()));
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern(getDateTimeFormat()));
    }

    /**
     * Returns model.
     * @return model
     */
    public Model getModel() {
        return model;
    }

    /**
     * Sets model.
     * @param model model
     */
    public void setModel(Model model) {
        this.model = model;
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
     * Drops formula to formula grid.
     * @param event drop event
     */
    @Command
    @NotifyChange({"model", "selectedFormulas"})
    public void dropFormula(@ContextParam(ContextType.TRIGGER_EVENT) DropEvent event) {
        Treerow row = (Treerow) event.getDragged();
        Treeitem item = (Treeitem) row.getParent();
        Formula fm = item.getValue();
        if (model.getFormulas().stream().anyMatch(e -> e.getKey().equals(fm.getKey()))) {
            Messagebox.show(Labels.getLabel("model_duplicate_formula_message"), Labels.getLabel("messagebox_validation"),
                            Messagebox.OK, Messagebox.EXCLAMATION);
        } else {
            model.getFormulas().add(fm);
            selectedFormulas.add(fm);
        }
    }

    /**
     * Drops template to template grid.
     * @param event drop event
     */
    @Command
    @NotifyChange({"model", "formTemplatesFiltered", "inputForms"})
    public void dropTemplate(@ContextParam(ContextType.TRIGGER_EVENT) DropEvent event) {
        Row row = (Row) event.getDragged();
        Entity template = row.getValue();
        if (model.getFormTemplates().stream().anyMatch(e -> e.getKey().equals(template.getKey()))) {
            Messagebox.show(Labels.getLabel("model_duplicate_template_message"), Labels.getLabel("messagebox_validation"),
                            Messagebox.OK, Messagebox.EXCLAMATION);
        } else {
            FormTemplate formTemplate = (FormTemplate) template;
            model.getFormTemplates().add(formTemplate);
            if (!formTemplate.getType().equals(TemplateType.FORM))
                return;
            List<EntityMeta> newForms = formTemplateService.getInputForms(Collections.singletonList(formTemplate));
            newForms.stream()
                    .filter(inputForm -> !model.getInputForms().contains(inputForm))
                    .forEach(inputForm -> model.getInputForms().add(inputForm));
        }
    }

    /**
     * Removes formula from model children.
     * @param formula child formula
     */
    @Command
    @NotifyChange({"model", "selectedFormulas"})
    public void removeFormula(@BindingParam("formula") Formula formula) {
        model.getFormulas().remove(formula);
        selectedFormulas.remove(formula);
    }

    /**
     * Drops classifier to classifier grid.
     * @param event drop event
     */
    @Command
    @NotifyChange({"model"})
    public void dropClassifier(@ContextParam(ContextType.TRIGGER_EVENT) DropEvent event) {
        Treerow row = (Treerow) event.getDragged();
        Treeitem item = (Treeitem) row.getParent();
        EntityMetaStatus entityMetaStatus = item.getValue();
        if (entityMetaStatus.getGroup() == null) {
            EntityMeta classifier = entityMetaStatus.getEntityMeta();
            if (model.getClassifiers().stream().anyMatch(e -> e.getKey().equals(classifier.getKey()))) {
                Messagebox.show(Labels.getLabel("model_duplicate_classifier_message"), Labels.getLabel("messagebox_validation"),
                                Messagebox.OK, Messagebox.EXCLAMATION);
            } else {
                model.getClassifiers().add(classifier);
            }
        } else {
            entityMetaStatus.getChildren().stream().forEach(ems -> {
                EntityMeta classifier = ems.getEntityMeta();
                if (!model.getClassifiers().stream().anyMatch(e -> e.getKey().equals(classifier.getKey()))) {
                    model.getClassifiers().add(classifier);
                }
            });
        }
        BindUtils.postGlobalCommand(null, null, "refreshClassifierEntityMetas", null);
    }

    /**
     * Drops classifier group to classifier grid.
     * @param event drop event
     */
    @Command
    @NotifyChange({"model"})
    public void dropClassifierGroup(@ContextParam(ContextType.TRIGGER_EVENT) DropEvent event) {
        Treerow row = (Treerow) event.getDragged();
        Treeitem item = (Treeitem) row.getParent();
        EntityMetaStatus entityMetaStatus = item.getValue();
        EntityMeta classifier = entityMetaStatus.getEntityMeta();
        if (model.getClassifiers().stream().anyMatch(e -> e.getKey().equals(classifier.getKey()))) {
            Messagebox.show(Labels.getLabel("model_duplicate_classifier_message"), Labels.getLabel("messagebox_validation"),
                            Messagebox.OK, Messagebox.EXCLAMATION);
        } else {
            model.getClassifiers().add(classifier);
        }
        BindUtils.postGlobalCommand(null, null, "refreshClassifierEntityMetas", null);
    }

    /**
     * Drops form to form grid.
     * @param event drop event
     */
    @Command
    @NotifyChange({"model", "formEntityMetasFiltered"})
    public void dropForm(@ContextParam(ContextType.TRIGGER_EVENT) DropEvent event) {
        Row row = (Row) event.getDragged();
        EntityMeta form = row.getValue();
        if (model.getInputForms().stream().anyMatch(e -> e.getKey().equals(form.getKey()))) {
            Messagebox.show(Labels.getLabel("model_duplicate_form_message"), Labels.getLabel("messagebox_validation"),
                            Messagebox.OK, Messagebox.EXCLAMATION);
        } else {
            model.getInputForms().add(row.getValue());
        }
    }

    /**
     * Removes classifier from model children.
     * @param classifier classifier
     */
    @Command
    @NotifyChange({"model"})
    public void removeClassifier(@BindingParam("classifier") EntityMeta classifier) {
        BindUtils.postGlobalCommand(null, null, "resetClassifierEntityMetas", null);
        model.getClassifiers().remove(classifier);
    }

    /**
     * Removes template from model children.
     * @param template template
     */
    @Command
    @NotifyChange({"model", "formTemplatesFiltered", "inputForms"})
    public void removeTemplate(@BindingParam("template") FormTemplate template) {
        model.getFormTemplates().remove(template);
        if (!template.getType().equals(TemplateType.FORM))
            return;
        List<EntityMeta> formsInModel =
                formTemplateService.getInputForms(model.getFormTemplates().stream()
                                                       .filter(formTemplate -> formTemplate.getType().equals(TemplateType.FORM))
                                                       .collect(Collectors.toList()));
        List<EntityMeta> formsToDelete = new ArrayList<>();
        model.getInputForms().stream().filter(form -> !formsInModel.contains(form)).forEach(formsToDelete::add);
        formsToDelete.forEach(form -> model.getInputForms().remove(form));
    }

    /**
     * Persists model.
     */
    @Command
    public void persistModel() {
        if (!edit && isModelExists()) {
            Messagebox.show(Labels.getLabel("model_duplicate_message"),
                            Labels.getLabel("messagebox_validation"),
                            Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }
        calcService.persistModel(model);
        navigateModels();
    }

    /**
     * Publishes model.
     */
    @Command
    public void publishModel() {
        if (!edit && isModelExists()) {
            Messagebox.show(Labels.getLabel("model_duplicate_message"),
                            Labels.getLabel("messagebox_validation"),
                            Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }
        calcService.publishModel(model);
        navigateModels();
    }

    /**
     * Returns if model exists.
     * @return if model exists
     */
    private boolean isModelExists() {
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(getEntityMeta().getKeyAttribute(), Operator.EQ, model.getKey()));
        List<Model> modelList = calcService.getModels(criteria, new RowRange(1, 1), null);
        return modelList.size() > 0;
    }

    /**
     * Navigates models.
     */
    @Command
    public void navigateModels() {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "calc.model.model_list");
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Changes actuality.
     * @param event event
     */
    @Command
    public void changeActuality(@ContextParam(ContextType.TRIGGER_EVENT) InputEvent event) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Labels.getLabel("date_time_format"));
        String formattedString = model.getActuality().format(formatter);
        String message = MessageFormat.format(Labels.getLabel("model_actality_change_message"), formattedString);
        Messagebox.show(message, Labels.getLabel("messagebox_validation"),
                        Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                        boxEvent -> {
                            if (Messagebox.ON_OK.equals(boxEvent.getName())) {

                                Map<String, Object> map = new HashMap<>();
                                map.put("actuality", model.getActuality());
                                BindUtils.postGlobalCommand(null, null, "actualityChanged", map);
                                BindUtils.postGlobalCommand(null, null, "resetClassifierEntityMetas", map);
                                BindUtils.postNotifyChange(null, null, ModelViewModel.this, "*");

                                List<Formula> formulas = new ArrayList<>();
                                setupFlattenedFormulaTree(getRootFormula(), formulas);

                                model.setFormulas(model.getFormulas().stream()
                                                       .filter(p -> formulas.contains(p))
                                                       .collect(Collectors.toList()));

                                model.setClassifiers(model.getClassifiers().stream()
                                                          .filter(p -> getClassifierEntityMetas().contains(p))
                                                          .collect(Collectors.toList()));

                                model.setFormTemplates(model.getFormTemplates().stream()
                                                            .filter(p -> getFormTemplates().contains(p))
                                                            .collect(Collectors.toList()));
                                model.setInputForms(model.getInputForms().stream()
                                                         .filter(p -> getFormEntityMetas().contains(p))
                                                         .collect(Collectors.toList()));
                            } else {
                                Date previousValue = (Date) event.getPreviousValue();
                                model.setActuality(
                                        truncateDateToFormat(LocalDateTime.ofInstant(previousValue.toInstant(), ZoneId.systemDefault()),
                                                             getDateTimeFormat()));
                                BindUtils.postNotifyChange(null, null, ModelViewModel.this, "model");
                            }
                        });
    }

    /**
     * Returns classifier entity meta list.
     * @return classifier entity meta list
     */
    private List<EntityMeta> getClassifierEntityMetas() {
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(EntityMeta.METADATA_KEY, null);
        Criteria criteria = new Criteria();
        AttributeMeta viewOrderMetadata = entityMeta.getAttributeMetadata(EntityMeta.EntityMetaAttributeMeta.VIEW_ORDER.getKey());
        criteria.getOrder().addItem(viewOrderMetadata, false);
        return entityMetaService.getEntityMetas(criteria, null, model.getActuality(), EntityType.CLASSIFIER);
    }

    /**
     * Returns is edit.
     * @return is edit
     */
    public boolean isEdit() {
        return edit;
    }

    /**
     * Returns form templates list filtered.
     * @return form templates list filtered
     */
    public List<FormTemplate> getFormTemplatesFiltered() {
        return getFormTemplates().stream().filter(p -> !getModel().getFormTemplates().contains(p)).collect(Collectors.toList());
    }

    /**
     * Returns form entity meta list.
     * @return form entity meta list
     */
    private List<EntityMeta> getFormEntityMetas() {
        if (formEntityMetas == null) {
            EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(EntityMeta.METADATA_KEY, null);
            Criteria criteria = new Criteria();
            AttributeMeta viewOrderMetadata = entityMeta.getAttributeMetadata(EntityMeta.EntityMetaAttributeMeta.VIEW_ORDER.getKey());
            criteria.getOrder().addItem(viewOrderMetadata, false);
            formEntityMetas = entityMetaService.getEntityMetas(criteria, null, model.getActuality(), EntityType.INPUT_FORM);
        }
        return formEntityMetas;
    }

    /**
     * Returns classifier filter.
     * @return classifier filter
     */
    public String getClassifierFilter() {
        return classifierFilter;
    }

    /**
     * Sets classifier filter.
     * @param classifierFilter classifier filter
     */
    public void setClassifierFilter(String classifierFilter) {
        this.classifierFilter = classifierFilter;
    }

    /**
     * Returns form filter.
     * @return form filter
     */
    public String getFormTemplateFilter() {
        return formTemplateFilter;
    }

    /**
     * Sets form filter.
     * @param formTemplateFilter form filter
     */
    public void setFormTemplateFilter(String formTemplateFilter) {
        this.formTemplateFilter = formTemplateFilter;
    }

    /**
     * Changes form filter.
     */
    @Command
    public void changeFormTemplateFilter() {
        BindUtils.postNotifyChange(null, null, this, "formTemplatesFiltered");
    }

    /**
     * Recursively setups flattened formula tree.
     * @param root root {@link Formula} instance
     * @param formulas list of formula flattened tree
     */
    private void setupFlattenedFormulaTree(Formula root, List<Formula> formulas) {
        root.getChildren().forEach(f -> {
            setupFlattenedFormulaTree(f.getRight(), formulas);
            formulas.add(f.getRight());
        });
    }

    /**
     * Returns root formula.
     * @return root formula
     */
    private Formula getRootFormula() {
        Criteria criteria = new Criteria();
        Where where = criteria.getWhere();
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(Formula.METADATA_KEY, null);
        where.addItem(new WhereItem(entityMeta.getAttributeMetadata(Formula.FormulaAttributeMeta.TYPE.getKey()), Operator.NOT_IN,
                                    FormulaType.LIBRARY, FormulaType.PRECALCULATED_FORMULA));

        Formula rootFormula = new Formula();
        List<Formula> formulas = calcService.getFormulaTrees(criteria, model.getActuality());
        formulas.stream().forEach(f -> rootFormula.addChild(null, f));
        return rootFormula;
    }

    /**
     * Returns entity meta key constraint.
     * @return entity meta key constraint
     */
    public String getEntityKeyConstraint() {
        return String.format("no empty,/%s/", config.getProperty("db.entity.meta.key.regexp"));
    }

    /**
     * Returns form entity meta.
     * @return form entity meta
     */
    public EntityMeta getFormEntityMeta() {
        if (formEntityMeta == null) {
            formEntityMeta = entityMetaService.getEntityMetaByKey(EntityMeta.METADATA_KEY, null);
        }
        return formEntityMeta;
    }

    /**
     * Returns formula entity meta.
     * @return formula entity meta
     */
    public EntityMeta getFormulaEntityMeta() {
        if (formulaEntityMeta == null) {
            formulaEntityMeta = entityMetaService.getEntityMetaByKey(Formula.METADATA_KEY, null);
        }
        return formulaEntityMeta;
    }

    /**
     * Returns classifier entity meta.
     * @return classifier entity meta
     */
    public EntityMeta getClassifierEntityMeta() {
        if (classifierEntityMeta == null) {
            classifierEntityMeta = entityMetaService.getEntityMetaByKey(EntityMeta.METADATA_KEY, null);
        }
        return classifierEntityMeta;
    }

    /**
     * Returns form templates.
     * @return form templates
     */
    public List<FormTemplate> getFormTemplates() {
        Criteria criteria = new Criteria();
        if (formTemplateFilter != null && !formTemplateFilter.isEmpty()) {
            EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(FormTemplate.METADATA_KEY, model.getActuality());
            criteria.getWhere().addItem(
                    new WhereItem(entityMeta.getAttributeMetadata(FormTemplate.FormTemplateAttributeMeta.NAME.getKey()),
                                  Operator.LIKE,
                                  "%" + formTemplateFilter + "%"));
        }
        return formTemplateService.getFormTemplates(criteria, null, model.getActuality());
    }

    /**
     * Returns selected formulas.
     * @return selected formuals
     */
    public List<Formula> getSelectedFormulas() {
        return selectedFormulas;
    }

    /**
     * Returns list of periodicities for combobox.
     * @return ListModelList Model.Periodicity
     */
    public ListModelList<Model.Periodicity> getPeriodicities() {
        ListModelList<Model.Periodicity> periodicities = new ListModelList<>();
        periodicities.addAll(Arrays.asList(Model.Periodicity.values()));
        return periodicities;
    }

    /**
     * Return label for periodicity.
     * @param periodicity periodicity
     * @return String label
     */
    public String getPeriodicityLabel(Model.Periodicity periodicity) {
        return Labels.getLabel(String.format("model_periodicity_%s", periodicity.name().toLowerCase()));
    }

    /**
     * Returns form's date attributes.
     * @param form input form
     * @return list of AttributeMeta
     */
    public List<AttributeMeta> getDateAttributes(EntityMeta form) {
        List<AttributeMeta> attributeMetas = form.getAttributes().stream()
                                                 .filter(attr -> attr.getType().equals(AttributeType.DATE)
                                                                 || attr.getType().equals(AttributeType.DATETIME)).collect(Collectors.toList());
        if (attributeMetas.size() > 0)
            attributeMetas.add(0, null);
        return attributeMetas;
    }

    /**
     * Returns list of input forms attributes.
     * @return input forms attributes
     */
    public List<EntityAttribute<EntityMeta>> getInputForms() {
        LinkedEntityAttribute<EntityMeta> modelInputFormsAttribute =
                (LinkedEntityAttribute<EntityMeta>) model.getAttribute(Model.ModelAttributeMeta.INPUT_FORMS.getKey());
        return modelInputFormsAttribute.getEntityAttributeList();
    }

    /**
     * Returns period count attribute key.
     * @return period count attribute key
     */
    public String getPeriodCountKey() {
        return Model.ModelInputFormAttributeMeta.PERIOD_COUNT.getKey();
    }

    /**
     * Returns selected attribute meta.
     * @param entityAttribute entity attribute
     * @return attribute meta
     */
    public AttributeMeta getSelectedInputFormDateAttribute(EntityAttribute<EntityMeta> entityAttribute) {
        String key = (String) entityAttribute.getSatellite().getAttributeValue(Model.ModelInputFormAttributeMeta.INPUT_FORM_DATE_ATTR_KEY.getKey());
        if (key != null)
            return entityAttribute.getEntity().getAttributeMetadata(key);
        return null;
    }

    /**
     * Sets selected  attribute meta.
     * @param entityAttribute entity attribute
     * @param value value
     */
    @Command
    @NotifyChange("inputForms")
    public void selectInputFormDateAttribute(@BindingParam("entityAttribute") EntityAttribute entityAttribute, @BindingParam("val") Object value) {
        entityAttribute.getSatellite().setAttributeValue(Model.ModelInputFormAttributeMeta.INPUT_FORM_DATE_ATTR_KEY.getKey(), value);
    }
}
