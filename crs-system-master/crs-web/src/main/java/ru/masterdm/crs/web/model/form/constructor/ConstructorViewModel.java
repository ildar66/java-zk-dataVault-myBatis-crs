package ru.masterdm.crs.web.model.form.constructor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zss.api.AreaRef;
import org.zkoss.zss.api.Exporters;
import org.zkoss.zss.api.Importers;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zss.api.model.CellData;
import org.zkoss.zss.api.model.Sheet;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zss.ui.event.Events;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Window;

import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.FormulaResult;
import ru.masterdm.crs.domain.entity.attribute.FileInfoAttribute;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.CalculationProfileAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.domain.form.FileFormat;
import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.domain.form.RepeatType;
import ru.masterdm.crs.domain.form.TemplateType;
import ru.masterdm.crs.domain.form.mapping.ImportObject;
import ru.masterdm.crs.domain.form.mapping.MappingField;
import ru.masterdm.crs.domain.form.mapping.MappingObject;
import ru.masterdm.crs.domain.form.mapping.Range;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.FormTemplateService;
import ru.masterdm.crs.web.domain.UserProfile;
import ru.masterdm.crs.web.service.FormTemplateUiService;

/**
 * Form constructor view model.
 * @author Vladimir Shvets
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ConstructorViewModel {

    public static final int MAX_RANGE_SIZE = 65536;

    @Wire("#ss")
    private Spreadsheet ss;
    @WireVariable("pages")
    private Properties pages;
    @WireVariable
    private EntityService entityService;
    @WireVariable
    private EntityMetaService entityMetaService;
    @WireVariable("userProfile")
    private UserProfile userProfile;
    @WireVariable
    private FormTemplateUiService formTemplateUiService;
    @WireVariable
    private FormTemplateService formTemplateService;

    private Book book;
    private FormTemplate template;
    private MappingObject contextObject;
    private List<MappingField> fields = new ArrayList<>();
    private List<List<MappingField>> fieldsObjects;
    private List<MappingField> contextFields = new ArrayList<>();
    private MappingField contextField;
    private List<ImportObject> importObjects = new ArrayList<>();
    private int saveFlag;
    private int openFlag;

    /**
     * Initialization.
     * @param template template
     */
    @Init
    public void init(@ExecutionParam("template") FormTemplate template) {
        if (template == null) {
            String key = (String) Executions.getCurrent().getAttribute("key");
            if (key != null) {
                EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(FormTemplate.METADATA_KEY, null);
                Criteria criteria = new Criteria();
                criteria.getWhere().addItem(new WhereItem(entityMeta.getKeyAttribute(), Operator.EQ, key));
                List<FormTemplate> templates = formTemplateService.getFormTemplates(criteria, null, null);
                this.template = (templates.size() > 0) ? templates.get(0) : null;
            }
        } else
            this.template = template;
        if (this.template != null) {
            if (this.template.getMapper() != null && this.template.getMapper().getObjects() != null
                && this.template.getMapper().getObjects().size() != 0) {
                fields = new ArrayList<>();
                fieldsObjects = new ArrayList<>();
                this.template.getMapper().getObjects().forEach(this::refreshFields);
            }
        } else {
            navigateTemplates();
        }
    }

    /**
     * Exit action.
     */
    @Command
    public void navigateTemplates() {
        if (saveFlag == 1) {
            Messagebox.show(Labels.getLabel("constructor_exit_warning"), Labels.getLabel("constructor_exit_warning_title"),
                            Messagebox.YES | Messagebox.NO, Messagebox.QUESTION,
                            event -> {
                                if (event.getName().equals(Messagebox.ON_YES)) {
                                    if (saveTemplate()) {
                                        close();
                                    }
                                }
                                if (event.getName().equals(Messagebox.ON_NO)) {
                                    close();
                                }
                            });
        } else {
            close();
        }
    }

    /**
     * Save form.
     * @return success
     * @throws Exception IO exception
     */
    private Boolean saveTemplate() throws Exception {
        if ((template == null) || (template.getName() == null) || template.getName().isEmpty()) {
            Messagebox.show(Labels.getLabel("constructor_save_warning"), Labels.getLabel("constructor_save_warning_title"),
                            Messagebox.OK, Messagebox.EXCLAMATION);
            return false;
        }
        if (template.getMapper().getObjects().size() == 0) {
            Messagebox.show(Labels.getLabel("constructor_save_warning1"), Labels.getLabel("constructor_save_warning_title"),
                            Messagebox.OK, Messagebox.EXCLAMATION);
            return false;
        }
        template.getBook().setContent(getBookData(book));
        formTemplateService.persistFormTemplate(template);
        saveFlag = 0;
        Clients.showNotification(Labels.getLabel("data_saved"));
        return true;
    }

    /**
     * Close form constructor.
     */
    public void close() {
        Map<String, Object> map = new HashMap<>();
        switch (template.getType()) {
            case IMPORT:
                map.put("targetPage", "form.templates.templates_import");
                break;
            case EXPORT:
                map.put("targetPage", "form.templates.templates_export");
                break;
            case FORM:
                map.put("targetPage", "form.templates.templates_forms");
                break;
            default:
                break;
        }

        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * After compose.
     * @param view Component view
     */
    @AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, false);
        if (ss != null) {
            ss.addEventListener(Events.ON_STOP_EDITING, event -> saveFlag = 1);
        }
    }

    /**
     * New book.
     */
    @Command
    @NotifyChange({"*"})
    public void newBook() {
        if (template != null) {
            template.getBook().setContent(getBlankBook());
            openFlag = 1;
            saveFlag = 1;
        }
    }

    /**
     * Gets blank workbook.
     * @return InputStream book
     */
    private InputStream getBlankBook() {
        return WebApps.getCurrent().getResourceAsStream("/WEB-INF/books/blank.xls");
    }

    /**
     * Opens book.
     * @param event UploadEvent
     */
    @Command
    @NotifyChange({"*"})
    public void openBook(@ContextParam(ContextType.TRIGGER_EVENT) UploadEvent event) {
        if (template != null) {
            Media media = event.getMedia();
            template.getBook().setContent(media.getStreamData());
            openFlag = 1;
            saveFlag = 1;
        }
    }

    /**
     * Gets book from Excel file.
     * @return Book book
     * @throws IOException exception
     */
    @NotifyChange({"fields"})
    public Book getBook() throws IOException {
        if (template == null || template.getFormat().equals(FileFormat.XML)) {
            return null;
        }
        if (template.getBook().getContent() == null) {
            InputStream bookContent =
                    entityService.getFileContent((FileInfoAttribute) template.getAttribute(FormTemplate.FormTemplateAttributeMeta.BOOK.getKey()),
                                                 null);
            if (bookContent.available() > 0)
                template.getBook().setContent(bookContent);
            else
                template.getBook().setContent(getBlankBook());
        }
        book = Importers.getImporter().imports(template.getBook().getContent(), "");
        if (openFlag == 1) {
            boolean sameSheets = false;
            for (MappingField field : fields) {
                if (field.getRange() != null) {
                    Sheet sh = book.getSheet(field.getRange().getSheet());
                    if (sh == null) {
                        field.setRange(null);
                    } else {
                        sameSheets = true;
                    }
                }
            }
            if (sameSheets) {
                openFileDialog();
            }
            openFlag = 0;
        }
        return book;
    }

    /**
     * Open file dialog.
     */
    private void openFileDialog() {
        if (fields.size() > 0) {
            final Object thisVm = this;
            Messagebox.show(Labels.getLabel("constructor_open_warning"), Labels.getLabel("constructor_open_warning_title"),
                            Messagebox.YES | Messagebox.NO, Messagebox.QUESTION,
                            event -> {
                                if (event.getName().equals(Messagebox.ON_YES)) {
                                    fields.forEach(f -> f.setRange(null));
                                    BindUtils.postNotifyChange(null, null, thisVm, "fields");
                                }
                            });
        }
    }

    /**
     * Creates area from range.
     * @param range range
     * @return area from range
     */
    private AreaRef createAreaFromRange(Range range) {
        return new AreaRef(range.getRow(), range.getColumn(), range.getLastRow(), range.getLastColumn());
    }

    /**
     * Returns template.
     * @return template
     */
    public FormTemplate getTemplate() {
        return template;
    }

    /**
     * Edits object.
     * @param mo mapping object
     */
    @Command
    public void editObject(@BindingParam("mo") MappingObject mo) {
        mo.getEntityMeta().getAttributes().stream()
          .filter(attributeMeta -> mo.getFields().stream().noneMatch(mappingField -> mappingField.getAttributeMeta().equals(attributeMeta)))
          .forEach(mo::addField);
        objectDialog(mo, false);
    }

    /**
     * Opens object dialog.
     * @param mo mapping object
     * @param isNew is object new
     */
    private void objectDialog(MappingObject mo, boolean isNew) {
        Map<String, Object> params = new HashMap<>();
        params.put("form", template);
        params.put("object", mo);
        params.put("contextObject", contextObject);
        params.put("isNew", isNew);
        Window window = (Window) Executions.createComponents(pages.getProperty("form.constructor.object"), null, params);
        window.doModal();
    }

    /**
     * Deletes object.
     * @param mo mapping object
     */
    @Command
    @NotifyChange({"template", "fields", "fieldsObjects"})
    public void deleteObject(@BindingParam("mo") MappingObject mo) {
        mo.getParent().getObjects().remove(mo);
        refreshFields();
    }

    /**
     * Saves object.
     * @param mo mapping object
     * @param isNew is object new
     */
    @GlobalCommand
    @NotifyChange({"template", "fields", "fieldsObjects"})
    public void saveObject(@BindingParam("mo") MappingObject mo, @BindingParam("isNew") Boolean isNew) {
        if (isNew) {
            if (contextObject == null) {
                template.getMapper().addObject(mo);
            } else {
                contextObject.addObject(mo);
            }
        } else {
            if (!mo.getRepeat().equals(RepeatType.ONE)) setRepeat(mo);
        }
        refreshFields();
    }

    /**
     * Clone object action.
     */
    @Command
    public void cloneObjectDialog() {
        Window window = (Window) Executions.createComponents(pages.getProperty("form.constructor.clone_dialog"), null, null);
        window.doModal();
    }

    /**
     * Clones object and refresh fields.
     * @param count count of clones
     */
    @GlobalCommand
    @NotifyChange({"template", "fields", "fieldsObjects"})
    public void cloneObjectAndRefresh(@BindingParam("count") int count) {
        if (contextObject == null) return;
        for (int i = 0; i < count; i++) {
            cloneObjectFromRoot(contextObject, null);
        }
        refreshFields();
    }

    /**
     * Gets context object.
     * @return MappingObject context object
     */

    public MappingObject getContextObject() {
        return contextObject;
    }

    /**
     * Sets context object.
     * @param mo mapping object
     */
    @Command
    @NotifyChange({"contextObject"})
    public void setContextObject(@BindingParam("mo") MappingObject mo) {
        contextObject = mo;
    }

    /**
     * Gets fields.
     * @return list of fields
     */
    public List<MappingField> getFields() {
        return fields;
    }

    /**
     * Maps area.
     * @param field mapping field
     */
    @Command
    @NotifyChange({"fields", "xmlDoc"})
    public void map(@BindingParam("field") MappingField field) {
        if (template.getFormat().equals(FileFormat.EXCEL)) {
            AreaRef area = ss.getSelection();
            Range range = createRangeFromArea(area);
            if (range.getSize() > MAX_RANGE_SIZE)
                return;
            if (range.getSize() != 1
                && field.getMappingObject().getRangeSize() != 1
                && range.getSize() != field.getMappingObject().getRangeSize()) {

                Messagebox.show(Labels.getLabel("constructor_mapping_warning"),
                                Labels.getLabel("constructor_mapping_warning_title"),
                                Messagebox.OK, Messagebox.EXCLAMATION);
                return;
            }
            field.setRange(range);
            field.getRange().setSheet(ss.getSelectedSheet().getSheetName());
            field.getRange().setSheetIndex(book.getSheetIndex(ss.getSelectedSheet()));
            org.zkoss.zss.api.Range selection = Ranges.range(ss.getSelectedSheet(), area);
        }
        saveFlag = 1;
    }

    /**
     * Creates range from area.
     * @param area area
     * @return Range
     */
    private Range createRangeFromArea(AreaRef area) {
        return new Range(area.getRow(), area.getColumn(), area.getLastRow(), area.getLastColumn(),
                         Ranges.getAreaRefString(ss.getSelectedSheet(), area));

    }

    /**
     * Unbinds area.
     * @param field Mapping field
     */
    @Command
    @NotifyChange({"fields", "xmlDoc"})
    public void unmap(@BindingParam("field") MappingField field) {
        if (template.getFormat() == FileFormat.EXCEL) {
            field.setRange(null);
        }
        saveFlag = 1;
    }

    /**
     * Gets field object names.
     * @return field object names
     */
    public List<List<MappingField>> getFieldsObjects() {
        return fieldsObjects;
    }

    /**
     * Save.
     * @throws Exception IO exception
     */
    @Command
    public void save() throws Exception {
        saveTemplate();
    }

    /**
     * Sets repeat property for all children objects.
     * @param mo MappingObject
     */
    private void setRepeat(MappingObject mo) {
        mo.getObjects().forEach(object -> {
            object.setRepeat(mo.getRepeat());
            setRepeat(object);
        });
    }

    /**
     * Refreshes fields.
     */
    private void refreshFields() {
        fields = new ArrayList<>();
        fieldsObjects = new ArrayList<>();
        for (MappingObject object : template.getMapper().getObjects()) {
            refreshFields(object);
        }
        saveFlag = 1;
    }

    /**
     * Refreshes fields.
     * @param object mapping object
     */
    private void refreshFields(MappingObject object) {
        List<MappingField> fo = new ArrayList<>();
        for (MappingField field : object.getFields()) {
            if (field.isMapped() || field.isWrite()) {
                fo.add(field);
                fields.add(field);
            }
            if (field.getObject() != null)
                refreshFields(field.getObject());
        }
        if (fo.size() > 0) {
            fieldsObjects.add(fo);
        }
        for (MappingObject objectChild : object.getObjects()) {
            refreshFields(objectChild);
        }

    }

    /**
     * Clones object from root.
     * @param mo mapping object
     * @param parent parent object
     */
    private void cloneObjectFromRoot(MappingObject mo, MappingObject parent) {
        EntityMeta calcEntityMeta = null;
        if (contextObject.getParent() instanceof MappingObject)
            calcEntityMeta = ((MappingObject) contextObject.getParent()).getEntityMeta();
        MappingObject moClone = createObjectFromMeta(mo.getEntityMeta(), calcEntityMeta);
        if (parent == null) {
            mo.getParent().addObject(moClone);
        } else {
            parent.addObject(moClone);
        }
        moClone.setName(mo.getName());
        moClone.setRepeat(mo.getRepeat());
        moClone.setUpdateOption(mo.getUpdateOption());
        moClone.setCreateOption(mo.getCreateOption());
        moClone.getFields().forEach(mfClone -> {
            MappingField mf = mo.getFields()
                                .stream()
                                .filter(f -> f.getAttributeMeta().equals(mfClone.getAttributeMeta()))
                                .findFirst().orElse(null);
            if (mf != null) {
                mfClone.setKey(mf.isKey());
                mfClone.setMapped(mf.isMapped());
                mfClone.setValue(mf.getValue());
                mfClone.setFormDateType(mf.getFormDateType());
                mfClone.setDateOffsetType(mf.getDateOffsetType());
                mfClone.setDateOffset(mf.getDateOffset());
            }

        });
        mo.getObjects().forEach(object -> cloneObjectFromRoot(object, moClone));
    }

    /**
     * Gets bookData InputStream.
     * @param book book
     * @return InputStream
     * @throws Exception exception
     */
    private InputStream getBookData(Book book) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Exporters.getExporter().export(book, bos);
        return new ByteArrayInputStream(bos.toByteArray());
    }

    /**
     * Open properties dialog.
     */
    @Command()
    public void openProperties() {
        Map<String, Object> map = new HashMap<>();
        map.put("template", template);
        Window window = (Window) Executions.createComponents(pages.getProperty("form.constructor.properties_dialog"), null, map);
        saveFlag = 1;
        window.doModal();
    }

    /**
     * Returns context field.
     * @return context field
     */
    public List<MappingField> getContextFields() {
        return contextFields;
    }

    /**
     * Sets context field.
     */
    @Command
    @NotifyChange({"contextFields"})
    public void setContextFields() {
        contextFields = new ArrayList<>();
        AreaRef area = ss.getSelection();
        for (int i = 0; i < fields.size(); i++) {
            MappingField field = fields.get(i);
            if (field.getRange() != null) {
                Range range = field.getRange();
                if (range.getSheet().equals(ss.getSelectedSheetName())
                    && range.getColumn() == area.getColumn()
                    && range.getRow() == area.getRow()) {
                    contextFields.add(field);
                }
            }
        }
    }

    /**
     * Searching for the binded cell.
     * @param tree Tree
     */
    @Command
    @NotifyChange({"xmlDoc"})
    public void cellSearch(@ContextParam(ContextType.COMPONENT) Tree tree) {
        Treeitem item = tree.getSelectedItem();
        contextField = (MappingField) item.getValue();
        if (contextField == null) return;
        if (template.getFormat().equals(FileFormat.EXCEL)) {
            if (contextField.getRange() != null) {
                Range range = contextField.getRange();
                ss.setSelectedSheet(range.getSheet());
                AreaRef area = createAreaFromRange(range);
                ss.setSelection(area);
            }
        }
    }

    /**
     * Gets context field for XML searching.
     * @return contextField
     */
    public MappingField getContextField() {
        return contextField;
    }

    /**
     * Returns attribute label.
     * @param mappingField mapping field
     * @return attribute label
     */
    public String getFieldLabel(MappingField mappingField) {
        return (mappingField.getAttributeMeta() == null)
               ? Labels.getLabel("object_formula_result")
               : mappingField.getAttributeMeta().getName().getDescription(userProfile.getLocale());
    }

    /**
     * Retrns object and field name point separated.
     * @param mappingField mapping field
     * @return field's full name
     */
    public String getFieldNameWithObject(MappingField mappingField) {
        return String.format("%s.%s", mappingField.getMappingObject().getName(), getFieldLabel(mappingField));
    }

    /**
     * Returns list of entity meta.
     * @param typeString type of entity meta
     * @return list of entity meta
     */
    public List<EntityMeta> getEntityMetaList(String typeString) {
        EntityType entityType = Stream.of(EntityType.values())
                                      .filter(v -> v.name().equals(typeString))
                                      .findFirst().orElse(EntityType.DICTIONARY);
        return entityMetaService.getEntityMetas(null, null, null, entityType);
    }

    /**
     * Create new object.
     * @param key entity meta key
     */
    @Command
    public void newMappingObject(@BindingParam("key") String key) {
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(key, null);
        if (entityMeta == null)
            return;
        MappingObject mappingObject;
        if (contextObject == null)
            mappingObject = createObjectFromMeta(entityMeta, null);
        else
            mappingObject = createObjectFromMeta(entityMeta, contextObject.getEntityMeta());
        if (!entityMeta.getKey().equals(Calculation.METADATA_KEY))
            mappingObject.getFields()
                         .stream()
                         .filter(mappingField -> mappingField.getAttributeMeta().getType().equals(AttributeType.REFERENCE))
                         .filter(mappingField -> mappingField.getAttributeMeta().getEntityKey().equals(CalculationProfileAttributeMeta.METADATA_KEY))
                         .forEach(mappingField -> mappingField.setKey(true));

        objectDialog(mappingObject, true);
    }

    /**
     * Creates object from entity meta.
     * @param entityMeta entity meta
     * @param calcEntityMeta calculation entity meta
     * @return mapping object
     */
    private MappingObject createObjectFromMeta(EntityMeta entityMeta, EntityMeta calcEntityMeta) {
        MappingObject mappingObject = new MappingObject(entityMeta);
        if (entityMeta.getKey().equals(FormulaResult.METADATA_KEY) && calcEntityMeta != null) {
            String profileAttributeMetaKey = entityMetaService.getAttributeMetaKey(calcEntityMeta, CalculationProfileAttributeMeta.METADATA_KEY);
            mappingObject.addField(calcEntityMeta.getAttributeMetadata(profileAttributeMetaKey));
        }
        return mappingObject;
    }

    /**
     * Export debug.
     */
    @Command
    public void debugExport() {
        importObjects = formTemplateUiService.prepareFormMap(book, template.getMapper(), TemplateType.EXPORT);
        formTemplateUiService.exportForm(book, importObjects);
    }

    /**
     * Import debug.
     */
    @Command
    public void debugImport() {
        importObjects = formTemplateUiService.prepareFormMap(book, template.getMapper(), TemplateType.IMPORT);
        formTemplateUiService.importForm(book, importObjects);
    }

    /**
     * Cleans form after debugging.
     */
    @Command
    public void cleanForm() {
        fields.stream()
              .filter(field -> ((field.isMapped() && !field.isKey()) || (field.isWrite())) && field.getRange() != null)
              .forEach(field -> {
                  Sheet sheet = book.getSheet(field.getRange().getSheet());
                  IntStream.rangeClosed(field.getRange().getRow(), field.getRange().getLastRow())
                           .forEach(r -> IntStream.rangeClosed(field.getRange().getColumn(), field.getRange().getLastColumn())
                                                  .forEach(c -> {
                                                      org.zkoss.zss.api.Range range = Ranges.range(sheet, r, c);
                                                      if (!range.getCellData().getType().equals(CellData.CellType.FORMULA))
                                                          range.setCellValue(null);
                                                  })
                           );
              });
    }

    /**
     * Gets title of parent page.
     * @return title of parent page
     */
    public String getParentPageTitle() {
        switch (template.getType()) {
            case IMPORT:
                return Labels.getLabel("templates_import_title");
            case EXPORT:
                return Labels.getLabel("templates_export_title");
            case FORM:
                return Labels.getLabel("templates_forms_title");
            default:
                return "";
        }
    }
}