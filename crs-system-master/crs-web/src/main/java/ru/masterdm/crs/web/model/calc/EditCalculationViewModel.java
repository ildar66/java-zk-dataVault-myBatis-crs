package ru.masterdm.crs.web.model.calc;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.Importers;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.CellData;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Window;

import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.ClassifierAttributeMeta;
import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.entity.AbstractDvEntity;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.attribute.EntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.FileInfoAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.MultilangAttribute;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.OrderItem;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.CalculationProfileAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientCategoryAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientGroupAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMetaGroup;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.domain.entity.meta.EntityTypeAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.FormulaType;
import ru.masterdm.crs.domain.form.FormDateType;
import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.domain.form.TemplateType;
import ru.masterdm.crs.domain.form.mapping.ImportObject;
import ru.masterdm.crs.domain.form.mapping.MappingField;
import ru.masterdm.crs.domain.form.mapping.MappingObject;
import ru.masterdm.crs.domain.form.mapping.Range;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.service.CalcService;
import ru.masterdm.crs.service.ClientService;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.FormTemplateService;
import ru.masterdm.crs.service.LockService;
import ru.masterdm.crs.service.SecurityService;
import ru.masterdm.crs.service.ValueConvertService;
import ru.masterdm.crs.service.entity.AttributeFactory;
import ru.masterdm.crs.web.domain.UserProfile;
import ru.masterdm.crs.web.domain.entity.ClassifierValue;
import ru.masterdm.crs.web.domain.entity.form.UiForm;
import ru.masterdm.crs.web.domain.entity.form.UiFormInstance;
import ru.masterdm.crs.web.service.FormTemplateUiService;

/**
 * Calculations view model class.
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditCalculationViewModel {

    /**
     * Profile keys.
     */
    public enum ProfileKey {
        /** Rated profile. */
        RATED,
        /** Expert profile. */
        EXPERT
    }

    @WireVariable
    private CalcService calcService;
    @WireVariable
    private EntityService entityService;
    @WireVariable
    private EntityMetaService entityMetaService;
    @WireVariable
    private SecurityService securityService;
    @WireVariable
    private LockService lockService;
    @WireVariable
    private ValueConvertService valueConvertService;
    @WireVariable("pages")
    private Properties pages;
    @WireVariable("userProfile")
    private UserProfile userProfile;
    @WireVariable("webConfig")
    private Properties webConfig;
    @WireVariable
    private FormTemplateUiService formTemplateUiService;
    @WireVariable
    private FormTemplateService formTemplateService;
    @WireVariable
    protected ClientService clientService;

    private Calculation calculation;
    private boolean edit = true;
    private boolean copy;
    private List<Model> publishedModels;
    private boolean running;
    private EntityMeta formulaEntityMeta;
    private EntityMeta classifierEntityMeta;

    private Map<EntityMeta, Map<String, ClassifierValue>> classifierValueMap;
    private List<Entity> classifierValues;
    private boolean classifierTabLoaded;

    private List<UiForm> excelForms;
    private List<UiForm> excelReports;
    private UiForm selectedForm;
    private UiForm selectedReport;
    private org.zkoss.zss.api.Range copiedRange;

    private List<Entity> clientGroupClients;
    private List<Entity> selectedClientGroupClients = new ArrayList<>();
    private String clientGroupClientsSearchString = "";

    private String clientType;
    private List<String> clientTypes;
    private List<? extends Entity> profiles;
    private List<EntityMetaGroup> entityMetaGroups;
    private ListModelList entitiesModel;
    private String searchString = "";

    private int actualityYear;
    private int actualityQuarter;
    private int actualityMonth;

    private static final int MONTHS_IN_QUARTER = 3;
    private static final int MONTHS_IN_YEAR = 12;
    private static final int KEY_DELETE = 46;
    private static final int KEY_COPY = 67;
    private static final int KEY_PASTE = 86;

    private int pageSize;

    /**
     * Initiates context.
     * @param calculation calculation
     * @param entity Client entity
     * @param copiedCalculation copied calculation
     */
    @Init
    public void initSetup(@ExecutionParam("calculation") final Calculation calculation, @ExecutionParam("entity") Entity entity,
                          @ExecutionParam("copiedCalculation") Calculation copiedCalculation) {
        if (calculation == null) {
            String key = (String) Executions.getCurrent().getAttribute("key");
            if (key != null) {
                EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(Calculation.METADATA_KEY, null);
                Criteria criteria = new Criteria();
                criteria.getWhere().addItem(new WhereItem(entityMeta.getKeyAttribute(), Operator.EQ, key));
                this.calculation = calcService.getCalculations(criteria, null, null).get(0);
            }
        } else
            this.calculation = calculation;
        clientType = ClientAttributeMeta.METADATA_KEY;
        EntityMeta profileMeta = entityMetaService.getEntityMetaByKey(CalculationProfileAttributeMeta.METADATA_KEY, null);
        Criteria criteria = new Criteria();
        criteria.getOrder().addItem(new OrderItem(profileMeta.getKeyAttribute(), true));
        profiles = entityService.getEntities(profileMeta, criteria, null, null);
        if (this.calculation == null) {
            this.calculation = (Calculation) entityService.newEmptyEntity(Calculation.METADATA_KEY);
            this.calculation.setDataActuality(entityMetaService.getSysTimestamp());
            this.calculation.getProfiles().addAll(profiles);
            if (copiedCalculation != null) {
                copy = true;
                this.calculation.setName(copiedCalculation.getName());
                this.calculation.setModel(copiedCalculation.getModel());
                this.calculation.setActuality(copiedCalculation.getActuality());
                this.calculation.setDataActuality(copiedCalculation.getDataActuality());
                this.calculation.setProfiles(copiedCalculation.getProfiles());
                this.calculation.getParentReferenceAttribute().getEntityList().clear();
                this.calculation.getParentReferenceAttribute().add(copiedCalculation);
                if (copiedCalculation.getClientGroup() != null)
                    this.calculation.setClientGroup(copiedCalculation.getClientGroup());
                else
                    this.calculation.setClient(copiedCalculation.getClient());
            }
            if (entity != null) {
                AttributeMeta attributeMeta = this.calculation.getMeta().getAttributes()
                                                              .stream()
                                                              .filter(a -> a.getType().equals(AttributeType.REFERENCE))
                                                              .filter(a -> a.getEntityKey().equals(entity.getMeta().getKey()))
                                                              .findFirst().orElse(null);
                if (attributeMeta != null) {
                    LinkedEntityAttribute linkedEntityAttribute = (LinkedEntityAttribute) this.calculation.getAttribute(attributeMeta.getKey());
                    if (linkedEntityAttribute != null) {
                        linkedEntityAttribute.add(entity);
                        if (this.calculation.getClient() != null) {
                            clientType = ClientAttributeMeta.METADATA_KEY;
                        }
                        if (this.calculation.getClientGroup() != null) {
                            clientType = ClientGroupAttributeMeta.METADATA_KEY;
                        }
                    }
                }
            }
        } else if (this.calculation.isPublished()) {
            edit = false;
        }
        if (this.calculation.getClientGroup() != null) {
            clientType = ClientGroupAttributeMeta.METADATA_KEY;
        }

        LocalDate date = (this.calculation.getActuality() == null)
                         ? this.calculation.getDataActuality().toLocalDate() : this.calculation.getActuality();
        actualityYear = date.getYear();
        actualityMonth = date.getMonthValue();
        if (actualityMonth == 1) {
            actualityYear--;
            actualityMonth = MONTHS_IN_YEAR;
        } else
            actualityMonth--;
        actualityQuarter = (actualityMonth / MONTHS_IN_QUARTER > 0) ? actualityMonth / MONTHS_IN_QUARTER : MONTHS_IN_YEAR / MONTHS_IN_QUARTER;

        if (!isCalculationAllowed() && this.calculation.getId() != null) {
            running = true;
        }
        pageSize = Integer.parseInt(webConfig.getProperty("pageSize"));
    }

    /**
     * Returns is edit.
     * @return is edit
     */
    public boolean isEdit() {
        return edit;
    }

    /**
     * Returns is copy.
     * @return is copy
     */
    public boolean isCopy() {
        return copy;
    }

    /**
     * Returns calculation.
     * @return calculation
     */
    public Calculation getCalculation() {
        return calculation;
    }

    /**
     * Saves calculation.
     */
    @Command
    @SmartNotifyChange("*")
    public void saveCalculation() {
        User author = securityService.getCurrentUser();
        calculation.setAuthor(author);
        boolean isNew = calculation.getId() == null;
        if (isNew) {
            switch (calculation.getModel().getPeriodicity()) {
                case QUARTER:
                    if (actualityQuarter < MONTHS_IN_YEAR / MONTHS_IN_QUARTER)
                        calculation.setActuality(LocalDate.of(actualityYear, actualityQuarter * MONTHS_IN_QUARTER + 1, 1));
                    else
                        calculation.setActuality(LocalDate.of(actualityYear + 1, 1, 1));
                    break;
                case MONTH:
                    if (actualityMonth < MONTHS_IN_YEAR)
                        calculation.setActuality(LocalDate.of(actualityYear, actualityMonth + 1, 1));
                    else
                        calculation.setActuality(LocalDate.of(actualityYear + 1, 1, 1));
                    break;
                case YEAR:
                    calculation.setActuality(LocalDate.of(actualityYear + 1, 1, 1));
                    break;
                default:
            }
            if (copy) {
                calcService.copyCalculation(calculation);
                copy = false;
            } else
                calcService.persistCalculation(calculation);
            String bookmark = Executions.getCurrent().getDesktop().getBookmark();
            Executions.getCurrent().getDesktop().setBookmark(bookmark.concat(calculation.getKey()));
        } else {
            saveClassifierValues();
            saveFormData();
            calcService.persistCalculation(calculation);
        }
        publishedModels = null;
        if (clientType.equals(ClientGroupAttributeMeta.METADATA_KEY) && calculation.getClientGroup() != null)
            searchString = getEntityLabel(calculation.getClientGroup());
        else if (clientType.equals(ClientAttributeMeta.METADATA_KEY) && calculation.getClient() != null)
            searchString = getEntityLabel(calculation.getClient());
        Clients.showNotification(Labels.getLabel("data_saved"));
    }

    /**
     * Saves classifier values.
     */
    private void saveClassifierValues() {
        List<Entity> classifierValuesToPersist = new ArrayList<>();
        calculation.getProfiles().forEach(profile -> {
            List<Entity> classifierValues = getClassifierValues()
                    .stream()
                    .filter(cv -> {
                        String attributeMetaKey = entityMetaService.getAttributeMetaKey(cv.getMeta(), ClassifierAttributeMeta.CALC_PROFILE.name());
                        LinkedEntityAttribute attr = (LinkedEntityAttribute) cv.getAttribute(attributeMetaKey);
                        return attr.getValue().size() > 0 && ((EntityAttribute) attr.getValue().get(0)).getEntity().equals(profile);
                    }).collect(Collectors.toList());

            if (classifierValues.isEmpty()) {
                getClassifierValueMap().forEach((k, v) -> {
                    if (v.containsKey(profile.getKey())) {
                        Entity classifierValue = entityService.newEmptyEntity(k);
                        classifierValue.setAttribute(v.get(profile.getKey()).getType());
                        classifierValue.setAttribute(v.get(profile.getKey()).getComment());
                        classifierValue.setAttribute(v.get(profile.getKey()).getProfile());
                        getClassifierValues().add(classifierValue);
                        classifierValuesToPersist.add(classifierValue);
                    }
                });
            } else {
                classifierValues.forEach(classifierValue -> {
                    ClassifierValue classifierNewValue = getClassifierValueMap().get(classifierValue.getMeta()).get(profile.getKey());
                    if (classifierNewValue != null && classifierNewValue.isChanged()) {
                        classifierValue.setAttribute(classifierNewValue.getType());
                        classifierValue.setAttribute(classifierNewValue.getComment());
                        classifierValue.setAttribute(classifierNewValue.getProfile());
                        classifierValuesToPersist.add(classifierValue);
                    }
                });
            }
        });

        calcService.persistsClassifierValues(calculation, classifierValuesToPersist);

        getClassifierValueMap().forEach((k, v) -> {
            v.values().forEach(cv -> cv.setChanged(false));
        });
    }

    /**
     * Calculates.
     */
    @Command
    @SmartNotifyChange("*")
    public void calculate() {
        if (isCalculationAllowed()) {
            if (calculation.getModel().getFormulas().isEmpty()) {
                Messagebox.show(Labels.getLabel("edit_calculation_calculation_no_formulas"));
                return;
            }
            saveCalculation();
            calcService.eval(calculation);
            running = true;
            Clients.showNotification(Labels.getLabel("edit_calculation_calculation_started"));
        } else {
            Clients.showNotification(Labels.getLabel("edit_calculation_calculation_already_started"));
        }
    }

    /**
     * On timer event.
     */
    @Command
    @SmartNotifyChange("*")
    public void onTimer() {
        Criteria criteria = new Criteria();
        criteria.setHubIds(Arrays.asList(calculation.getHubId()));
        List<Calculation> calculationList = calcService.getCalculations(criteria, null, null);
        if (calculationList.isEmpty())
            return;

        calculation = calculationList.get(0);
        if (isCalculationAllowed()) {
            running = false;
            Clients.showNotification(Labels.getLabel("edit_calculation_calculation_finished"));
            BindUtils.postNotifyChange(null, null, this, "calculation");
            BindUtils.postNotifyChange(null, null, calculation, "calculated");
            Map<String, Object> map = new HashMap<>();
            map.put("calculation", calculation);
            BindUtils.postGlobalCommand(null, null, "calculationFormulaResultsChanged", map);
        }
    }

    /**
     * Returns is calculation allowed.
     * @return is calculation allowed
     */

    public boolean isCalculationAllowed() {
        return !lockService.isLocked(calculation);
    }

    /**
     * Navigates calculations.
     */
    private void navigateCalculations() {
        BindUtils.postGlobalCommand(null, null, "navigateBackward", null);
    }

    /**
     * Returns published models.
     * @return published models
     */
    public List<Model> getPublishedModels() {
        if (publishedModels == null) {
            publishedModels = calcService.getPublishedModels(null, null, null);
            if (getCalculation() != null && getCalculation().getModel() != null && !publishedModels.contains(getCalculation().getModel())) {
                publishedModels.add(getCalculation().getModel());
            }
        }
        return publishedModels;
    }

    /**
     * Returns running.
     * @return running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Returns delay.
     * @return delay
     */
    public Long getDelay() {
        return Long.parseLong(webConfig.getProperty("calculation.timer.delay"));
    }

    /**
     * Returns classifier value map.
     * @return classifier value map
     */
    public Map<EntityMeta, Map<String, ClassifierValue>> getClassifierValueMap() {
        if (classifierValueMap == null && calculation.getId() != null && calculation.getModel() != null) {
            classifierValueMap = new LinkedHashMap<>();
            calculation.getProfiles().forEach(profile -> {
                List<Entity> classifierValues = getClassifierValues()
                        .stream()
                        .filter(cv -> {
                            String attributeMetaKey = entityMetaService
                                    .getAttributeMetaKey(cv.getMeta(), ClassifierAttributeMeta.CALC_PROFILE.name());
                            LinkedEntityAttribute attr = (LinkedEntityAttribute) cv.getAttribute(attributeMetaKey);
                            return attr.getValue().size() > 0 && ((EntityAttribute) attr.getValue().get(0)).getEntity().equals(profile);
                        }).collect(Collectors.toList());
                if (classifierValues.isEmpty()) {
                    List<EntityMeta> classifierMetas = calculation.getModel().getClassifiers();
                    classifierMetas.forEach(cm -> {
                        String typeMetaKey = entityMetaService.getAttributeMetaKey(cm, ClassifierAttributeMeta.CLASSIFIER_TYPE.name());
                        String commentMetaKey = entityMetaService.getAttributeMetaKey(cm, ClassifierAttributeMeta.CLASSIFIER_COMMENT.name());
                        String profileMetaKey = entityMetaService.getAttributeMetaKey(cm, ClassifierAttributeMeta.CALC_PROFILE.name());
                        LinkedEntityAttribute attr = (LinkedEntityAttribute) AttributeFactory.newAttribute(cm.getAttributeMetadata(profileMetaKey));
                        attr.add(profile);
                        ClassifierValue classifierValue = new ClassifierValue(AttributeFactory.newAttribute(cm.getAttributeMetadata(typeMetaKey)),
                                                                              AttributeFactory.newAttribute(cm.getAttributeMetadata(commentMetaKey)),
                                                                              attr);
                        classifierValue.setChanged(true);
                        initDefaultClassifierValue(classifierValue);

                        if (!classifierValueMap.containsKey(cm))
                            classifierValueMap.put(cm, new HashMap<>());
                        classifierValueMap.get(cm).put(profile.getKey(), classifierValue);
                    });
                } else {
                    classifierValues.forEach(cv -> {
                        String typeMetaKey = entityMetaService.getAttributeMetaKey(cv.getMeta(), ClassifierAttributeMeta.CLASSIFIER_TYPE.name());
                        String commentMetaKey = entityMetaService
                                .getAttributeMetaKey(cv.getMeta(), ClassifierAttributeMeta.CLASSIFIER_COMMENT.name());
                        String profileMetaKey = entityMetaService.getAttributeMetaKey(cv.getMeta(), ClassifierAttributeMeta.CALC_PROFILE.name());
                        ClassifierValue classifierValue = new ClassifierValue(cv.getAttribute(typeMetaKey), cv.getAttribute(commentMetaKey),
                                                                              cv.getAttribute(profileMetaKey));
                        initDefaultClassifierValue(classifierValue);

                        if (!classifierValueMap.containsKey(cv.getMeta()))
                            classifierValueMap.put(cv.getMeta(), new HashMap<>());
                        classifierValueMap.get(cv.getMeta()).put(profile.getKey(), classifierValue);
                    });
                }
            });
        }
        return classifierValueMap;
    }

    /**
     * Inits default classifier value.
     * @param classifierValue classifier value
     */
    private void initDefaultClassifierValue(ClassifierValue classifierValue) {
        AttributeMeta attributeMeta = classifierValue.getType().getMeta();
        String defaultValue = attributeMeta.getDefaultValue();
        if (defaultValue != null && classifierValue.getType().getValue() == null) {
            classifierValue.getType().setValue(valueConvertService.convert(attributeMeta, defaultValue));
        }
    }

    /**
     * Returns referenced entities.
     * @param linkedEntityAttribute linked entity attribute
     * @return referenced entities
     */
    public List<Entity> getReferencedEntities(LinkedEntityAttribute linkedEntityAttribute) {
        EntityMeta linkedEntityMeta = entityMetaService.getEntityMetaByKey(linkedEntityAttribute.getMeta().getEntityKey(), getActuality());
        if (linkedEntityMeta == null)
            return Collections.emptyList();
        List<Entity> entities = (List<Entity>) entityService.getEntities(linkedEntityMeta, null, null, null);
        return entities;
    }

    /**
     * Selects classifier group.
     * @param radiogroup radio group component
     */
    @Command
    public void selectClassifierGroup(@ContextParam(ContextType.COMPONENT) Radiogroup radiogroup) {
        radiogroup.setSelectedItem(null);
    }

    /**
     * Selects classifier item.
     * @param radio radio item component
     * @param entityMeta entity meta
     * @param profile profile
     */
    @Command
    public void selectClassifierReference(@ContextParam(ContextType.COMPONENT) Radio radio, @BindingParam("entityMeta") EntityMeta entityMeta,
                                          @BindingParam("profile") String profile) {
        boolean checked = radio.isChecked();
        List<Radio> radios = radio.getRadiogroup().getItems();
        if (radios != null) {
            radios.forEach(otherRadio -> {
                if (!radio.equals(otherRadio)) {
                    otherRadio.setSelected(false);
                }
            });
        }
        if (checked) {
            radio.getRadiogroup().setSelectedItem(null);
            radio.setSelected(false);
        } else {
            radio.getRadiogroup().setSelectedItem(radio);
            radio.setSelected(true);
        }

        ClassifierValue classifierValue = getClassifierValueMap().get(entityMeta).get(profile);
        classifierValue.setChanged(true);
        LinkedEntityAttribute linkedEntityAttribute = (LinkedEntityAttribute) classifierValue.getType();
        linkedEntityAttribute.getValue().clear();
        AbstractDvEntity value = radio.getValue();
        if (!checked) {
            linkedEntityAttribute.add(value);
            ClassifierValue expertClassifierValue = getClassifierValueMap().get(entityMeta).get(ProfileKey.EXPERT.name());
            if (profile.equals(ProfileKey.RATED.name()) && expertClassifierValue != null) {
                LinkedEntityAttribute linkedEntityAttributeExpert = (LinkedEntityAttribute) expertClassifierValue.getType();
                if (linkedEntityAttributeExpert.getValue().isEmpty()) {
                    linkedEntityAttributeExpert.add(value);
                    expertClassifierValue.setChanged(true);
                    BindUtils.postNotifyChange(null, null, entityMeta, ".");
                }
            }
        }
    }

    /**
     * Returns actuality.
     * @return actuality
     */
    private LocalDateTime getActuality() {
        return calculation.getModel().getActuality();
    }

    /**
     * Returns model label.
     * @param model model
     * @return model label
     */
    public String getModelLabel(Model model) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Labels.getLabel("date_time_format"));
        String formattedString = model.getActuality().format(formatter);
        return MessageFormat.format(Labels.getLabel("edit_calculation_model_label"),
                                    model.getName().getDescription(userProfile.getLocale()),
                                    model.getVersion(), model.getKey(), formattedString);
    }

    /**
     * Edit classifier comment.
     * @param commandOnClose command to execute on close
     * @param text text
     * @param title title
     * @param isEdit is edit
     * @param rowElement row element, which field value changed
     */
    @Command
    public void editText(@BindingParam("commandOnClose") String commandOnClose,
                         @BindingParam("text") String text,
                         @BindingParam("title") String title,
                         @BindingParam("isEdit") boolean isEdit,
                         @BindingParam("rowElement") Object rowElement) {
        Map<String, Object> map = new HashMap<>();
        map.put("commandOnClose", commandOnClose);
        map.put("text", text);
        map.put("title", title);
        map.put("isEdit", isEdit);
        map.put("rowElement", rowElement);
        Window window = (Window) Executions.createComponents(pages.getProperty("edit_text"), null, map);
        window.doModal();
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

    /**
     * Change comment of classifier.
     * @param text text
     * @param classifierValue classifierValue, which comment changed
     */
    @GlobalCommand
    public void classifierCommentChanged(@BindingParam("text") String text, @BindingParam("rowElement") ClassifierValue classifierValue) {
        classifierValue.setCommentValue(text);
        BindUtils.postNotifyChange(null, null, this, "classifierValueMap");
    }

    /**
     * Change comment of calculation group client.
     * @param text text
     * @param client calculation group client, which comment changed
     */
    @GlobalCommand
    public void calculationGroupClientCommentChanged(@BindingParam("text") String text, @BindingParam("rowElement") Entity client) {
        getCalculationClientSatelliteAttribute(client, Calculation.CalculationClientAttributeMeta.COMMENT.name()).setValue(text);
        BindUtils.postNotifyChange(null, null, client, ".");
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
     * Returns excel list.
     * @param templateType template type
     * @return excel list
     */
    private List<UiForm> getExcelList(TemplateType templateType) {
        List<UiForm> excelList = new LinkedList<>();
        if (calculation.getId() == null || calculation.getModel() == null || calculation.getModel().getFormTemplates().size() == 0)
            return null;
        calculation.getModel().getFormTemplates()
                   .stream()
                   .filter(template -> template.getType().equals(templateType))
                   .forEach(template -> {
                       List<MappingField> dateFields = getDateFields(template.getMapper().getObjects());
                       List<LocalDate> dateList = getDateList(template);
                       List<UiFormInstance> formInstances = new LinkedList<>();
                       if (dateFields.size() > 0) {
                           dateList.forEach(date -> formInstances.add(new UiFormInstance(date, new LinkedList<>())));
                           dateFields.forEach(field -> field.setValue(calculation.getActuality()));
                       } else
                           formInstances.add(new UiFormInstance(calculation.getActuality(), new LinkedList<>()));
                       excelList.add(new UiForm(template, dateFields, formInstances, calculation.getActuality()));
                   });
        return excelList;
    }

    /**
     * Updates excel list.
     * @param excelList excel list
     * @param type template type
     */
    private void updateExcelList(List<UiForm> excelList, TemplateType type) {
        FormTemplate template;
        UiForm selectedExcel = (type.equals(TemplateType.FORM)) ? selectedForm : selectedReport;
        Iterator it = excelList.iterator();
        if (selectedExcel == null && it.hasNext()) {
            selectedExcel = (UiForm) it.next();
            if (type.equals(TemplateType.FORM))
                selectedForm = selectedExcel;
            else
                selectedReport = selectedExcel;
        }
        if (selectedExcel != null) {
            template = selectedExcel.getFormTemplate();
            if (selectedExcel.getFormInstance().getBook() == null) {
                try {
                    InputStream bookContent = entityService.getFileContent(
                            (FileInfoAttribute) template.getAttribute(FormTemplate.FormTemplateAttributeMeta.BOOK.getKey()), null);
                    Book book = Importers.getImporter().imports(bookContent, template.getName().getDescription(userProfile.getLocale()));
                    if (calculation.isPublished())
                        for (int i = 0; i < book.getNumberOfSheets(); i++) {
                            Sheet sheet = book.getSheetAt(i);
                            org.zkoss.zss.api.Range range = Ranges.range(sheet);
                            range.protectSheet(null,
                                               false, false, false, false, false, false, false,
                                               false, false, false, false, false, false, false, false);

                        }
                    selectedExcel.getFormInstance().setBook(book);
                } catch (IOException e) {
                    throw new CrsException(e);
                }
            }
            if (selectedExcel.getFormInstance().getImportObjects().size() == 0) {
                Book book = selectedExcel.getFormInstance().getBook();
                List<ImportObject> ios = formTemplateUiService.prepareFormMap(book, template.getMapper(), calculation, TemplateType.FORM);
                selectedExcel.getFormInstance().setImportObjects(ios);
                formTemplateUiService.exportForm(book, ios);
            }
        }
    }

    /**
     * Returns excel forms list.
     * @return excel forms list
     */
    public List<UiForm> getExcelForms() {
        if (excelForms == null)
            excelForms = getExcelList(TemplateType.FORM);
        return excelForms;
    }

    /**
     * Returns excel forms list.
     * @return excel forms list
     */
    public List<UiForm> getExcelReports() {
        if (excelReports == null)
            excelReports = getExcelList(TemplateType.EXPORT);
        return excelReports;
    }

    /**
     * Submit Excel forms.
     */
    private void saveFormData() {
        if (excelForms != null) {
            excelForms.forEach(form -> {
                form.getFormInstances().stream().filter(UiFormInstance::isChanged)
                    .forEach(formInstance -> {
                        form.getDateFields().forEach(field -> field.setValue(formInstance.getDate()));
                        formTemplateUiService.importForm(formInstance.getBook(), formInstance.getImportObjects());
                        formInstance.setChanged(false);
                    });
            });
            excelForms.forEach(form -> form.getFormInstances().forEach(formInstance -> formInstance.getImportObjects().clear()));
        }
    }

    /**
     * Publishes calculation.
     */
    @Command
    public void publishCalculation() {
        if (!calculation.isCalculated()) {
            Messagebox.show(Labels.getLabel("exception_calc_publish_not_calculated"),
                            Labels.getLabel("edit_calculation_publish_calculation_title"),
                            new Messagebox.Button[] {Messagebox.Button.OK},
                            Messagebox.INFORMATION, null);
            return;
        }
        if (calculation.getClient() == null && calculation.getClientGroup() == null) {
            Messagebox.show(Labels.getLabel("exception_calc_publish_without_client_or_group"),
                            Labels.getLabel("edit_calculation_publish_calculation_title"),
                            new Messagebox.Button[] {Messagebox.Button.OK},
                            Messagebox.INFORMATION, null);
            return;
        }
        EventListener<Messagebox.ClickEvent> clickListener = event -> {
            if (Messagebox.Button.YES.equals(event.getButton())) {
                calcService.publishCalculation(calculation);
                navigateCalculations();
            }
        };
        Messagebox.show(Labels.getLabel("edit_calculation_publish_calculation_message"),
                        Labels.getLabel("edit_calculation_publish_calculation_title"),
                        new Messagebox.Button[] {Messagebox.Button.YES, Messagebox.Button.NO}, Messagebox.QUESTION, clickListener);
    }

    /**
     * Returns value constraint.
     * @param nullable nullable
     * @return value constraint
     */
    public String checkConstraint(Boolean nullable) {
        return (nullable != null && !nullable) ? "no empty" : "";
    }

    /**
     * Returns attribute value.
     * @param attribute attribute
     * @return attribute value
     */
    public Object getAttributeValue(AbstractAttribute attribute) {
        return attribute.getMeta().isMultilang() ? ((MultilangAttribute) attribute).getValue(userProfile.getLocale()) : attribute.getValue();
    }

    /**
     * Returns client type.
     * @return client type
     */
    public String getClientType() {
        return clientType;
    }

    /**
     * Sets client type.
     * @param clientType client type
     */
    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    /**
     * Returns client types.
     * @return client types
     */
    public List<String> getClientTypes() {
        if (clientTypes == null) {
            clientTypes = new ArrayList<>();
            clientTypes.add(ClientAttributeMeta.METADATA_KEY);
            clientTypes.add(ClientGroupAttributeMeta.METADATA_KEY);
        }
        return clientTypes;
    }

    /**
     * Returns client type label.
     * @param clientType client type
     * @return client type label
     */
    public String getClientTypeLabel(String clientType) {
        return Labels.getLabel("edit_calculation_" + clientType.toLowerCase());
    }

    /**
     * Returns client types icon class.
     * @param clientType client type
     * @return client types icon class
     */
    public String getClientTypeIconClass(String clientType) {
        return Labels.getLabel("edit_calculation_client_type_icon_class_" + clientType.toLowerCase());
    }

    /**
     * Returns client attribute meta key.
     * @return client attribute meta key
     */
    public String getClientAttributeMetaKey() {
        return ClientAttributeMeta.METADATA_KEY;
    }

    /**
     * Returns client group attribute meta key.
     * @return client group attribute meta key
     */
    public String getClientGroupAttributeMetaKey() {
        return ClientGroupAttributeMeta.METADATA_KEY;
    }

    /**
     * Opens import dialog.
     * @param template template
     * @param command global command
     */
    @Command
    public void importCommand(@BindingParam("template") FormTemplate template, @BindingParam("command") String command) {
        formTemplateUiService.process(template, command);
    }

    /**
     * Gets available profiles.
     * @return profiles list
     */
    public List<? extends Entity> getProfiles() {
        return profiles;
    }

    /**
     * Return value label.
     * @param profile profile
     * @return value label
     */
    public String getValueLabel(Entity profile) {
        return Labels.getLabel(profile.getKey().toLowerCase() + "_value");
    }

    /**
     * Select calculation profile.
     * @param profile chosen profile
     */
    @Command
    public void changeProfile(@BindingParam("selected") Entity profile) {
        calculation.getProfiles().clear();
        if (profile.getKey().equals(ProfileKey.RATED.name()))
            calculation.getProfiles().add(profile);
        else {
            calculation.getProfiles().addAll(profiles);
        }
        calculation.getProfiles().sort(Comparator.comparing(Entity::getKey).reversed());
        classifierValueMap = null;
        BindUtils.postNotifyChange(null, null, this, "*");
    }

    /**
     * Imports data from file.
     * @param template template
     */
    @GlobalCommand
    @SmartNotifyChange("excelForms")
    public void importFileCalculation(@BindingParam("template") FormTemplate template) {
        try {
            Book book = Importers.getImporter().imports(template.getBook().getContent(), "");
            InputStream bookContent = entityService.getFileContent(
                    (FileInfoAttribute) template.getAttribute(FormTemplate.FormTemplateAttributeMeta.BOOK.getKey()), null);
            Book uiBook = Importers.getImporter().imports(bookContent, template.getName().getDescription(userProfile.getLocale()));

            copyBookData(template.getMapper().getObjects(), book, uiBook);
            book = uiBook;
            List<ImportObject> importObjects = formTemplateUiService.prepareFormMap(book, template.getMapper(), calculation, TemplateType.FORM);
            formTemplateUiService.importForm(book, importObjects);
            excelForms.forEach(form -> {
                form.getFormInstances().forEach(formInstance -> formInstance.getImportObjects().clear());
                formTemplateService.jsonConfigToMapper(form.getFormTemplate());
                form.setDateFields(getDateFields(form.getFormTemplate().getMapper().getObjects()));
            });
            if (selectedForm != null) {
                selectedForm.getDateFields().forEach(field -> field.setValue(selectedForm.getFormInstance().getDate()));
                updateExcelList(excelForms, TemplateType.FORM);
            }
        } catch (IOException e) {
            throw new CrsException(e);
        }
    }

    /**
     * Copies data from imported book to the form.
     * @param mappingObjects mapping objects
     * @param srcBook source book
     * @param dstBook destination book
     */
    private void copyBookData(List<MappingObject> mappingObjects, Book srcBook, Book dstBook) {
        if (mappingObjects == null || mappingObjects.size() == 0)
            return;
        mappingObjects.forEach(mappingObject -> {
            mappingObject.getFields().stream()
                         .filter(mappingField -> !mappingField.isKey() && mappingField.isMapped() && mappingField.getRange() != null)
                         .forEach(mappingField -> {
                             Range range = mappingField.getRange();
                             Sheet srcSheet = srcBook.getSheet(range.getSheet());
                             Sheet dstSheet = dstBook.getSheet(range.getSheet());
                             for (int r = range.getRow(); r <= range.getLastRow(); r++)
                                 for (int c = range.getColumn(); c <= range.getLastColumn(); c++) {
                                     org.zkoss.zss.api.Range srcRange = Ranges.range(srcSheet, r, c);
                                     org.zkoss.zss.api.Range dstRange = Ranges.range(dstSheet, r, c);
                                     if (!dstRange.getCellData().getType().equals(CellData.CellType.FORMULA))
                                         dstRange.setCellValue(srcRange.getCellValue());
                                 }

                         });
            copyBookData(mappingObject.getObjects(), srcBook, dstBook);
        });
    }

    /**
     * Gets import templates linked with calculation's model.
     * @return import templates
     */
    public List<FormTemplate> getImportTemplates() {
        if (calculation.getId() != null && calculation.getModel() != null && calculation.getModel().getFormTemplates().size() > 0) {
            return calculation.getModel().getFormTemplates()
                              .stream()
                              .filter(template -> template.getType().equals(TemplateType.IMPORT))
                              .collect(Collectors.toList());
        } else
            return new ArrayList<>();
    }

    /**
     * Returns calculation profile attribute meta key.
     * @param key key
     * @return calculation profile attribute meta key
     */
    public String getCalculationProfileAttributeKey(String key) {
        return CalculationProfileAttributeMeta.valueOf(key).getKey();
    }

    /**
     * Returns entity meta groups.
     * @return entity meta groups
     */
    public List<EntityMetaGroup> getEntityMetaGroups() {
        if (classifierTabLoaded && entityMetaGroups == null && calculation.getId() != null && calculation.getModel() != null) {
            List<EntityMetaGroup> groups = getFullEntityMetaGroups(EntityType.CLASSIFIER);
            List<EntityMetaGroup> resultGroups = new ArrayList<>();
            groups.forEach(g -> {
                Set<Long> ids = g.getElements().stream().map(EntityMeta::getHubId).collect(Collectors.toSet());
                List<EntityMeta> entityMetaList = new ArrayList<>();
                getClassifierValueMap().keySet().stream().filter(em -> ids.contains(em.getHubId())).forEachOrdered(entityMetaList::add);
                if (!entityMetaList.isEmpty()) {
                    resultGroups.add(g);
                    g.setElements(entityMetaList);
                }
            });
            entityMetaGroups = resultGroups;
        }
        return entityMetaGroups;
    }

    /**
     * Returns not filtered list of entity meta groups.
     * @param entityType entity type
     * @return not filtered list of entity meta groups
     */
    private List<EntityMetaGroup> getFullEntityMetaGroups(EntityType entityType) {
        EntityMeta entityMetaGroupMeta = entityMetaService.getEntityMetaByKey(EntityMetaGroup.METADATA_KEY, null);
        EntityMeta entityTypeMeta = entityMetaService.getEntityMetaByKey(EntityTypeAttributeMeta.METADATA_KEY, null);
        AttributeMeta groupEntityTypeMeta =
                entityMetaGroupMeta.getAttributeMetadata(EntityMetaGroup.EntityMetaGroupAttributeMeta.ENTITY_TYPE.getKey());
        AttributeMeta groupViewOrderMeta =
                entityMetaGroupMeta.getAttributeMetadata(EntityMetaGroup.EntityMetaGroupAttributeMeta.VIEW_ORDER.getKey());
        Criteria groupCriteria = new Criteria();
        groupCriteria.getWhere()
                     .addReferenceItem(groupEntityTypeMeta, new WhereItem(entityTypeMeta.getKeyAttribute(), Operator.EQ, entityType));
        groupCriteria.getOrder().addItem(new OrderItem(groupViewOrderMeta, false));
        return entityMetaService.getEntityMetaGroups(groupCriteria, null, getActuality());
    }

    /**
     * Exports forms and reports to Excel file.
     * @param template template
     */
    @Command
    public void exportFile(@BindingParam("template") FormTemplate template) {
        InputStream bookContent = entityService.getFileContent(
                (FileInfoAttribute) template.getAttribute(FormTemplate.FormTemplateAttributeMeta.BOOK.getKey()), null);
        try {
            Book book = Importers.getImporter().imports(bookContent, template.getName().getDescription(userProfile.getLocale()));
            List<ImportObject> ios = formTemplateUiService.prepareFormMap(book, template.getMapper(), calculation, TemplateType.FORM);
            formTemplateUiService.exportForm(book, ios);
            formTemplateUiService.exportFile(book);
        } catch (IOException e) {
            throw new CrsException(e);
        }
    }

    /**
     * Get entities model.
     * @return entity meta
     */
    public ListModelList getEntitiesModel() {
        if (entitiesModel == null)
            refreshEntitiesModel(null);
        return entitiesModel;
    }

    /**
     * Refreshes entities model.
     * @param bandbox bandbox
     */
    @Command
    @SmartNotifyChange({"entitiesModel", "searchString"})
    public void refreshEntitiesModel(@BindingParam("bandbox") Bandbox bandbox) {
        if (bandbox == null) {
            entitiesModel = new ListModelList();
            if (clientType.equals(ClientGroupAttributeMeta.METADATA_KEY) && calculation.getClientGroup() != null) {
                entitiesModel = new ListModelList(Collections.singletonList(calculation.getClientGroup()));
            } else if (clientType.equals(ClientAttributeMeta.METADATA_KEY) && calculation.getClient() != null) {
                entitiesModel = new ListModelList(Collections.singletonList(calculation.getClient()));
            }
            if (entitiesModel.size() > 0) {
                searchString = getEntityLabel((Entity) entitiesModel.get(0));
                BindUtils.postNotifyChange(null, null, this, "searchString");
            }
        } else {
            entitiesModel = new ListModelList();
            if (searchString.isEmpty())
                return;
            EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(clientType, null);
            RowRange rowRange = new RowRange(0, pageSize);
            LocalDateTime clientLdts = clientService.getClientsEntityRequestLdts();
            List<Long> entityHubIds;
            if (clientType.equals(ClientGroupAttributeMeta.METADATA_KEY))
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
                if (entitiesModel.size() > 0)
                    bandbox.open();
            }
        }
    }

    /**
     * Sets search string.
     * @param searchString search string
     */
    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    /**
     * Returns search string.
     * @return search string
     */
    public String getSearchString() {
        return searchString;
    }

    /**
     * Sets client or group.
     * @param entity client or group entity
     * @param bandbox bandbox
     */
    @Command
    @SmartNotifyChange("headerLabel")
    public void setClient(@BindingParam("entity") Entity entity, @BindingParam("bandbox") Bandbox bandbox) {
        if (entity != null) {
            if (clientType.equals(ClientGroupAttributeMeta.METADATA_KEY))
                calculation.setClientGroup(entity);
            else
                calculation.setClient(entity);
            bandbox.setValue(getEntityLabel(entity));
            clientGroupClients = null;
            searchString = getEntityLabel(entity);
            bandbox.close();
        }
    }

    /**
     * Changes client type model.
     * @param bandbox bandbox component to be cleared
     */
    @SmartNotifyChange({"calculation", "entitiesModel", "clientType", "headerLabel"})
    @Command
    public void changeClientType(@BindingParam("bandbox") Bandbox bandbox) {
        if (clientType.equals(ClientGroupAttributeMeta.METADATA_KEY))
            calculation.setClient(null);
        else
            calculation.setClientGroup(null);
        searchString = "";
        clientGroupClients = null;
        entitiesModel = null;
        bandbox.setValue(null);
    }

    /**
     * Returns entity label.
     * @param entity entity
     * @return entity label
     */
    public String getEntityLabel(Entity entity) {
        if (entity == null)
            return null;
        if (clientType.equals(ClientGroupAttributeMeta.METADATA_KEY)) {
            return MessageFormat.format(Labels.getLabel("edit_calculation_client_group_label"),
                                        ((MultilangAttribute) entity.getAttribute(ClientGroupAttributeMeta.NAME.getKey()))
                                                .getValue(userProfile.getLocale()));
        }
        if (clientType.equals(ClientAttributeMeta.METADATA_KEY)) {
            return MessageFormat.format(Labels.getLabel("edit_calculation_client_label"),
                                        ((MultilangAttribute) entity.getAttribute(ClientAttributeMeta.NAME.getKey()))
                                                .getValue(userProfile.getLocale()));
        }
        return null;
    }

    /**
     * Returns selected entity.
     * @return selected entity
     */
    public Entity getSelectedEntity() {
        if (clientType.equals(ClientGroupAttributeMeta.METADATA_KEY))
            return calculation.getClientGroup();
        else
            return calculation.getClient();
    }

    /**
     * Sets expert value for classifier.
     * @param entityMeta entity meta
     * @param profile profile
     */
    @Command
    public void changeClassifier(@BindingParam("entityMeta") EntityMeta entityMeta,
                                 @BindingParam("profile") String profile) {
        if (!profile.equals(ProfileKey.RATED.name()) || !getClassifierValueMap().get(entityMeta).containsKey(ProfileKey.EXPERT.name()))
            return;
        ClassifierValue expertClassifierValue = classifierValueMap.get(entityMeta).get(ProfileKey.EXPERT.name());
        AbstractAttribute expertValue = expertClassifierValue.getType();
        if (expertValue.getValue() == null) {
            AbstractAttribute ratedValue = classifierValueMap.get(entityMeta).get(ProfileKey.RATED.name()).getType();
            expertClassifierValue.setClassifierValue(ratedValue.getValue());
            BindUtils.postNotifyChange(null, null, classifierValueMap.get(entityMeta), "*");
        }
    }

    /**
     * Returns page size.
     * @return page size
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Returns actuality year.
     * @return actuality year
     */
    public int getActualityYear() {
        return actualityYear;
    }

    /**
     * Sets actuality year.
     * @param actualityYear actuality year
     */
    public void setActualityYear(int actualityYear) {
        this.actualityYear = actualityYear;
    }

    /**
     * Returns actuality quarter.
     * @return actuality quarter
     */
    public int getActualityQuarter() {
        return actualityQuarter;
    }

    /**
     * Sets actuality quarter.
     * @param actualityQuarter actuality quarter
     */
    public void setActualityQuarter(int actualityQuarter) {
        this.actualityQuarter = actualityQuarter;
    }

    /**
     * Returns actuality month.
     * @return actuality month
     */
    public int getActualityMonth() {
        return actualityMonth;
    }

    /**
     * Sets actuality month.
     * @param actualityMonth actuality month
     */
    public void setActualityMonth(int actualityMonth) {
        this.actualityMonth = actualityMonth;
    }

    /**
     * Sets selected tab.
     * @param selectedTab selected tab
     */
    @SmartNotifyChange({"excelForms", "excelReports"})
    public void setSelectedTab(String selectedTab) {
        if (Labels.getLabel("edit_calculation_card_form_data").equals(selectedTab) && excelForms != null) {
            updateExcelList(excelForms, TemplateType.FORM);
        }
        if (Labels.getLabel("edit_calculation_reports").equals(selectedTab) && excelReports != null) {
            if (selectedReport != null)
                selectedReport.getFormInstances().forEach(formInstance -> formInstance.getImportObjects().clear());
            updateExcelList(excelReports, TemplateType.EXPORT);
        }
        if (!classifierTabLoaded && Labels.getLabel("edit_calculation_classifiers").equals(selectedTab)) {
            classifierTabLoaded = true;
            BindUtils.postNotifyChange(null, null, this, "entityMetaGroups");
        }
    }

    /**
     * Sets selected form.
     * @param selectedForm selected form
     */
    @SmartNotifyChange("excelForms")
    public void setSelectedForm(UiForm selectedForm) {
        this.selectedForm = selectedForm;
        this.selectedForm.getDateFields().forEach(field -> field.setValue(this.selectedForm.getSelectedDate()));
        updateExcelList(excelForms, TemplateType.FORM);
    }

    /**
     * Sets selected report.
     * @param selectedReport selected report
     */
    @SmartNotifyChange("excelReports")
    public void setSelectedReport(UiForm selectedReport) {
        this.selectedReport = selectedReport;
        selectedReport.getFormInstances().forEach(formInstance -> formInstance.getImportObjects().clear());
        updateExcelList(excelReports, TemplateType.EXPORT);

    }

    /**
     * Returns header for calculation card.
     * @return String header
     */
    public String getHeaderLabel() {
        Entity entity = clientType.equals(ClientGroupAttributeMeta.METADATA_KEY) ? calculation.getClientGroup() : calculation.getClient();
        if (entity == null)
            return null;
        return String.format("%s (%s)", getEntityLabel(entity), entity.getKey());
    }

    /**
     * Returns max row count.
     * @param book book
     * @return max row
     */
    public int getMaxRows(Book book) {
        return formTemplateUiService.getMaxRows(book);
    }

    /**
     * Returns max column count.
     * @param book book
     * @return max column
     */
    public int getMaxColumns(Book book) {
        return formTemplateUiService.getMaxColumns(book);
    }

    /**
     * Returns master formulas.
     * @return master formulas
     */
    public List<Formula> getMasterFormulas() {
        return (calculation.getModel() != null)
               ? calculation.getModel().getFormulas().stream().filter(formula -> formula.getType().equals(FormulaType.MASTER_FORMULA))
                            .collect(Collectors.toList())
               : null;
    }

    /**
     * Returns classifier values from database.
     * @return classifier values from database
     */
    protected List<Entity> getClassifierValues() {
        if (classifierValues == null && calculation.getId() != null) {
            classifierValues = calcService.getClassifierValues(calculation, null);
        }
        return classifierValues;
    }

    /**
     * Handles delete and copy-paste events on the form's spreadsheet.
     * @param event key event
     * @param ss spreadsheet
     */
    @Command
    public void onCtrlKey(@ContextParam(ContextType.TRIGGER_EVENT) KeyEvent event, @ContextParam(ContextType.COMPONENT) Spreadsheet ss) {
        AreaRef ref = ss.getSelection();
        org.zkoss.zss.api.Range srcRange = Ranges.range(ss.getSelectedSheet(), ref);
        switch (event.getKeyCode()) {
            case KEY_DELETE:
                if (ss.getSelectedSheet().isProtected() && !srcRange.isProtected())
                    srcRange.setCellValue(null);
                break;
            case KEY_COPY:
                copiedRange = srcRange;
                break;
            case KEY_PASTE:
                if (copiedRange == null)
                    break;
                AreaRef refToPaste = new AreaRef(srcRange.getRow(), srcRange.getColumn(),
                                                 srcRange.getRow() + copiedRange.getLastRow() - copiedRange.getRow(),
                                                 srcRange.getColumn() + copiedRange.getLastColumn() - copiedRange.getColumn());
                org.zkoss.zss.api.Range rangeToPaste = Ranges.range(ss.getSelectedSheet(), refToPaste);
                if (ss.getSelectedSheet().isProtected() && !rangeToPaste.isProtected() && copiedRange.getSheet().equals(srcRange.getSheet()))
                    copiedRange.paste(srcRange);
                break;
            default:
        }
    }

    /**
     * Returns is classifier selected.
     * @param uiSelectedEntity UI selected entity
     * @param profile profile
     * @param meta meta
     * @return is classifier selected
     */
    public boolean isClassifierSelected(Entity uiSelectedEntity, Entity profile, EntityMeta meta) {
        return Objects.equals(uiSelectedEntity, getClassifierValueMap().get(meta).get(profile.getKey()).getClassifierReferenceValue());
    }

    /**
     * Returns list of dates for form.
     * @param formTemplate form template
     * @return list of dates
     */
    private List<LocalDate> getDateList(FormTemplate formTemplate) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(calculation.getActuality());
        if (calculation.getModel() == null)
            return dateList;
        int maxPeriodCount = 1;
        List<EntityMeta> inputForms = formTemplateService.getInputForms(Collections.singletonList(formTemplate));
        for (EntityMeta inputForm : inputForms) {
            LinkedEntityAttribute<EntityMeta> modelInputFormsAttribute =
                    (LinkedEntityAttribute<EntityMeta>) calculation.getModel().getAttribute(Model.ModelAttributeMeta.INPUT_FORMS.getKey());
            EntityAttribute entityAttribute = modelInputFormsAttribute.getEntityAttributeList().stream()
                                                                      .filter(ea -> ea.getEntity().getKey().equals(inputForm.getKey()))
                                                                      .findFirst().orElse(null);
            if (entityAttribute != null) {
                Object attr = entityAttribute.getSatellite().getAttributeValue(Model.ModelInputFormAttributeMeta.INPUT_FORM_DATE_ATTR_KEY.getKey());
                if (attr != null) {
                    BigDecimal periodCount = (BigDecimal) entityAttribute.getSatellite()
                                                                         .getAttributeValue(Model.ModelInputFormAttributeMeta.PERIOD_COUNT.getKey());
                    if (periodCount != null && periodCount.intValue() > maxPeriodCount)
                        maxPeriodCount = periodCount.intValue();
                }
            }
        }
        for (int i = 1; i < maxPeriodCount; i++) {
            switch (calculation.getModel().getPeriodicity()) {
                case YEAR:
                    dateList.add(dateList.get(i - 1).minusYears(1));
                    break;
                case QUARTER:
                    dateList.add(dateList.get(i - 1).minusMonths(MONTHS_IN_QUARTER));
                    break;
                case MONTH:
                    dateList.add(dateList.get(i - 1).minusMonths(1));
                    break;
                default:
                    dateList.add(dateList.get(i - 1).minusDays(1));
            }
        }
        return dateList;
    }

    /**
     * Sets chosen date.
     * @param form UI form
     * @param date date
     */
    @Command
    public void setFormDate(@BindingParam("form") UiForm form, @BindingParam("date") LocalDate date) {
        form.setSelectedDate(date);
        form.getDateFields().forEach(field -> field.setValue(date));
        if (form.getFormInstance().getImportObjects().size() == 0)
            updateExcelList(excelForms, TemplateType.FORM);
        BindUtils.postNotifyChange(null, null, form, "selectedDate");
        BindUtils.postNotifyChange(null, null, form, "formInstance");
    }

    /**
     * OnStopEditing event handling.
     * @param formInstance form instance
     */
    @Command
    public void onCellEdited(@BindingParam("formInstance") UiFormInstance formInstance) {
        formInstance.setChanged(true);
    }

    /**
     * Returns list of date fields to set value.
     * @param mappingObjects list of mapping objects
     * @return list of fields
     */
    private List<MappingField> getDateFields(List<MappingObject> mappingObjects) {
        List<MappingField> dateFields = new ArrayList<>();
        mappingObjects.forEach(mappingObject -> {
            if (mappingObject.getEntityMeta().getType().equals(EntityType.INPUT_FORM)) {
                mappingObject.getFields()
                             .stream()
                             .filter(field -> field.getAttributeMeta().getType().equals(AttributeType.DATE)
                                              && field.getFormDateType().equals(FormDateType.CUSTOM)
                                              && field.isKey() && !field.isMapped()
                                              && (field.getValue() == null || field.getValue().toString().isEmpty()))
                             .forEach(dateFields::add);
            }
            dateFields.addAll(getDateFields(mappingObject.getObjects()));
        });
        return dateFields;
    }

    /**
     * Returns client group clients.
     * @return client group clients
     */
    public List<Entity> getClientGroupClients() {
        if (clientGroupClients == null && calculation.getId() != null) {
            LinkedEntityAttribute clientAttribute =
                    (LinkedEntityAttribute) calculation.getAttribute(Calculation.CalculationAttributeMeta.CLIENT.getKey());
            List<Entity> clients = clientAttribute.getEntityList();
            clients.sort(Comparator.comparing(
                    cl -> ((MultilangAttribute) cl.getAttribute(ClientAttributeMeta.NAME.getKey())).getMultilangDescription()
                                                                                                   .getDescription(userProfile.getLocale())
                                                                                                   .toLowerCase()));
            final String participantKey = Calculation.CalculationClientAttributeMeta.PARTICIPANT.getKey();
            final String excludedKey = Calculation.CalculationClientAttributeMeta.EXCLUDED.getKey();
            final String statusKey = Calculation.CalculationClientAttributeMeta.STATUS.getKey();
            List<EntityAttribute> entityAttributeList = clientAttribute.getEntityAttributeList();
            for (EntityAttribute entityAttribute : entityAttributeList) {
                if (Objects.isNull(entityAttribute.getSatellite().getAttributeValue(participantKey)))
                    entityAttribute.getSatellite().setAttributeValue(participantKey, false);
                if (Objects.isNull(entityAttribute.getSatellite().getAttributeValue(excludedKey)))
                    entityAttribute.getSatellite().setAttributeValue(excludedKey, false);
                if (Objects.isNull(entityAttribute.getSatellite().getAttributeValue(statusKey)))
                    entityAttribute.getSatellite().setAttributeValue(statusKey, Calculation.CalculationClientStatus.OTHER.name());
            }
            clientGroupClients = clients;
        }
        return clientGroupClients;
    }

    /**
     * Returns selected client group clients.
     * @return selected client group clients
     */
    public List<Entity> getSelectedClientGroupClients() {
        if (clientGroupClientsSearchString.isEmpty())
            selectedClientGroupClients = getClientGroupClients();
        else {
            String mask = String.format(".*%s.*", clientGroupClientsSearchString.toLowerCase());
            selectedClientGroupClients =
                    getClientGroupClients().stream()
                                           .filter(client -> {
                                               if (client.getKey().toLowerCase().matches(mask))
                                                   return true;
                                               String name = ((MultilangAttribute) client.getAttribute(ClientAttributeMeta.NAME.getKey()))
                                                       .getValue(userProfile.getLocale());
                                               if (name.toLowerCase().matches(mask))
                                                   return true;
                                               LinkedEntityAttribute linkedAttribute = (LinkedEntityAttribute) client
                                                       .getAttribute(ClientAttributeMeta.CLIENT_INN.getKey());
                                               if (linkedAttribute.getValue()
                                                                  .stream()
                                                                  .anyMatch(a -> ((EntityAttribute) a).getValue().toString().toLowerCase()
                                                                                                      .matches(mask)))
                                                   return true;
                                               linkedAttribute = (LinkedEntityAttribute) client.getAttribute(ClientAttributeMeta.CATEGORY.getKey());
                                               if (linkedAttribute.getEntityList()
                                                                  .stream()
                                                                  .anyMatch(e -> {
                                                                      String category = ((MultilangAttribute) ((Entity) e)
                                                                              .getAttribute(ClientCategoryAttributeMeta.NAME.getKey()))
                                                                              .getValue(userProfile.getLocale());
                                                                      return category.toLowerCase().matches(mask);
                                                                  })
                                                       )
                                                   return true;
                                               Object o = ((MultilangAttribute) ((Entity) linkedAttribute.getEntityList().get(0))
                                                       .getAttribute(ClientCategoryAttributeMeta.NAME.getKey()))
                                                       .getValue(userProfile.getLocale());
                                               return false;
                                           })
                                           .collect(Collectors.toList());
        }
        return selectedClientGroupClients;
    }

    /**
     * Sets selected client group clients.
     * @param selectedClientGroupClients selected client group clients
     */
    public void setSelectedClientGroupClients(List<Entity> selectedClientGroupClients) {
        this.selectedClientGroupClients = selectedClientGroupClients;
    }

    /**
     * Navigates client.
     * @param client client
     */
    @Command
    public void navigateCalculationClient(@BindingParam("client") Entity client) {
        Executions.getCurrent().setAttribute("client", client);
        Executions.getCurrent().setAttribute("key", client.getKey());
        Executions.getCurrent().setAttribute("mode", false);
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "entity.client");
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Returns calculation client satellite attribute.
     * @param client cliet
     * @param attributeKey satellite attribute key
     * @return calculation client satellite attribute
     */
    public AbstractAttribute getCalculationClientSatelliteAttribute(Entity client, String attributeKey) {
        return ((LinkedEntityAttribute) calculation.getAttribute(Calculation.CalculationAttributeMeta.CLIENT.getKey()))
                .getAttributeByEntity(client).getSatellite().getAttribute(Calculation.CalculationClientAttributeMeta.valueOf(attributeKey).getKey());
    }

    /**
     * Returns calculation client statuses.
     * @return calculation client statuses
     */
    public List<String> getCalculationClientStatuses() {
        return Stream.of(Calculation.CalculationClientStatus.values()).map(e -> e.name()).collect(Collectors.toList());
    }

    /**
     * Returns calculation client status name.
     * @param statusKey calculation client status key
     * @return calculation client status name
     */
    public String getCalculationClientStatusName(String statusKey) {
        return Labels.getLabel("edit_calculation_client_status_" + statusKey.toLowerCase());
    }

    /**
     * Manages client present in calc client group.
     * @param include is include to group. true if needs to include, false otherwise
     */
    @Command
    @SmartNotifyChange("selectedClientGroupClients")
    public void manageClientPresentInCalcClientGroup(@BindingParam("include") Boolean include) {
        selectedClientGroupClients.forEach(client -> includeClient(client, include));
    }

    /**
     * Copies calculation.
     */
    @Command
    public void copyCalculation() {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "calc.edit_calculation");
        Executions.getCurrent().setAttribute("copiedCalculation", calculation);
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Includes/excludes client to group.
     * @param client cleint
     * @param include include flag
     */
    @Command
    public void includeClient(@BindingParam("client") Entity client, @BindingParam("include") boolean include) {
        getCalculationClientSatelliteAttribute(client, Calculation.CalculationClientAttributeMeta.EXCLUDED.name()).setValue(!include);
        BindUtils.postNotifyChange(null, null, client, ".");
    }

    /**
     * Refreshes selected clients.
     */
    @Command
    public void changeClientGroupClientsFilter() {
        BindUtils.postNotifyChange(null, null, this, "selectedClientGroupClients");
    }

    /**
     * Gets search string.
     * @return search string
     */
    public String getClientGroupClientsSearchString() {
        return clientGroupClientsSearchString;
    }

    /**
     * Sets search string.
     * @param clientGroupClientsSearchString search string
     */
    public void setClientGroupClientsSearchString(String clientGroupClientsSearchString) {
        this.clientGroupClientsSearchString = clientGroupClientsSearchString;
    }

    /**
     * Sets participant flag.
     * @param checked participant flag
     */
    @Command
    @SmartNotifyChange("selectedClientGroupClients")
    public void setParticipants(@BindingParam("checked") boolean checked) {
        selectedClientGroupClients
                .forEach(client -> {
                    getCalculationClientSatelliteAttribute(client, Calculation.CalculationClientAttributeMeta.PARTICIPANT.name())
                            .setValue(checked);
                    BindUtils.postNotifyChange(null, null, client, ".");
                });
    }

    /**
     * Sets cleint's status.
     * @param status status
     */
    @Command
    @SmartNotifyChange("selectedClientGroupClients")
    public void setClientsStatus(@BindingParam("status") String status) {
        selectedClientGroupClients
                .forEach(client -> {
                    getCalculationClientSatelliteAttribute(client, Calculation.CalculationClientAttributeMeta.STATUS.name())
                            .setValue(status);
                    BindUtils.postNotifyChange(null, null, client, ".");
                });
    }
}