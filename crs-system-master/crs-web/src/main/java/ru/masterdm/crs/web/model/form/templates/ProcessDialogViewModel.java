package ru.masterdm.crs.web.model.form.templates;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zss.api.Importers;
import org.zkoss.zss.api.model.Book;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.domain.form.ExcelFormat;
import ru.masterdm.crs.domain.form.FileFormat;
import ru.masterdm.crs.domain.form.FormDateType;
import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.domain.form.TemplateType;
import ru.masterdm.crs.domain.form.mapping.MappingField;
import ru.masterdm.crs.domain.form.mapping.MappingObject;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.web.service.FormTemplateUiService;

/**
 * View model for object dialog.
 * @author Vladimir Shvets
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ProcessDialogViewModel {

    private FormTemplate template;
    @WireVariable
    private FormTemplateUiService formTemplateUiService;

    private List<MappingField> fields = new ArrayList<>();
    private String fileName;
    private String command;

    /**
     * Initialization.
     * @param template form template
     * @param command post global command
     */
    @Init
    public void initModel(@ExecutionArgParam("template") FormTemplate template, @ExecutionArgParam("command") String command) {
        this.template = template;
        this.command = command;
        setFields(template.getMapper().getObjects());
    }

    /**
     * Closes window by button.
     * @param window window
     */
    @Command
    public void save(@BindingParam("cmp") Window window) {
        close(window);
    }

    /**
     * Closes dialog.
     * @param window window
     */
    @Command
    public void close(@ContextParam(ContextType.COMPONENT) Window window) {
        String validationError = validate();
        if (validationError.isEmpty()) {
            Map params = new HashMap<>();
            params.put("template", template);
            BindUtils.postGlobalCommand(null, null, command, params);
            window.detach();
        } else {
            Messagebox.show(validationError, Labels.getLabel("process_dialog_validate_title"),
                            Messagebox.OK, Messagebox.EXCLAMATION);
        }
    }

    /**
     * Returns form template.
     * @return form template
     */
    public FormTemplate getTemplate() {
        return template;
    }

    /**
     * Returns fields.
     * @return fields
     */
    public List<MappingField> getFields() {
        return fields;
    }

    /**
     * Sets input fields.
     * @param mappingObjects list of mapping objects
     */
    public void setFields(List<MappingObject> mappingObjects) {
        mappingObjects.forEach(mappingObject -> {
            mappingObject.getFields()
                         .stream()
                         .filter(field -> field.isKey() && !field.isMapped() && (field.getValue() == null || field.getValue().toString().isEmpty()))
                         .forEach(field -> {
                             if (field.getAttributeMeta().getType().equals(AttributeType.DATE)
                                 && mappingObject.getEntityMeta().getType().equals(EntityType.INPUT_FORM)) {
                                 if (field.getFormDateType().equals(FormDateType.CUSTOM))
                                     fields.add(field);
                             } else
                                 fields.add(field);
                         });
            setFields(mappingObject.getObjects());
        });
    }

    /**
     * Uploads file for import.
     * @param event UploadEvent
     */
    @Command
    @NotifyChange({"fileName"})
    public void uploadFile(@ContextParam(ContextType.TRIGGER_EVENT) UploadEvent event) {
        fileName = null;
        Media media = event.getMedia();
        if (template.getFormat().equals(FileFormat.EXCEL)) {
            if (media.getFormat().equals(ExcelFormat.XLS.getFormat())
                || media.getFormat().equals(ExcelFormat.XLSX.getFormat())
                || media.getFormat().equals(ExcelFormat.XLSM.getFormat())) {
                try {
                    Book book = Importers.getImporter().imports(media.getStreamData(), "");
                    if (validateExcel(book, template.getMapper().getObjects())) {
                        template.getBook().setContent(media.getStreamData());
                        fileName = media.getName();
                    } else
                        Messagebox.show(Labels.getLabel("process_dialog_validate_excel"),
                                        Labels.getLabel("process_dialog_validate_title"),
                                        Messagebox.OK, Messagebox.EXCLAMATION);
                } catch (IOException e) {
                    throw new CrsException(e);
                }
            }
        }
    }

    /**
     * Validates imported file.
     * @param book book
     * @param mappingObjects list of mapping objects
     * @return true if book matches mapper
     */
    private boolean validateExcel(Book book, List<MappingObject> mappingObjects) {
        for (MappingObject mappingObject : mappingObjects) {
            if (mappingObject.getFields().stream()
                             .filter(mappingField -> mappingField.isMapped() && mappingField.getRange() != null)
                             .anyMatch(mappingField -> book.getSheet(mappingField.getRange().getSheet()) == null))
                return false;
            if (mappingObject.getObjects() != null && mappingObject.getObjects().size() > 0
                && !validateExcel(book, mappingObject.getObjects()))
                return false;
        }
        return true;
    }

    /**
     * Form Validation.
     * @return String error message
     */

    public String validate() {
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getValue() == null || fields.get(i).getValue().toString().isEmpty())
                return Labels.getLabel("process_dialog_validate_fields");
        }
        if (!template.getType().equals(TemplateType.EXPORT)) {
            if (fileName == null) {
                return Labels.getLabel("process_dialog_validate_file");
            }
        }
        return "";
    }

    /**
     * Returns file name.
     * @return file name
     */
    public String getFileName() {
        return fileName;
    }
}
