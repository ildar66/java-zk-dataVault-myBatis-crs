package ru.masterdm.crs.web.model.form.constructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.ListModelList;

import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.form.FileFormat;
import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.domain.form.TemplateType;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;

/**
 * View model for object dialog.
 * @author Vladimir Shvets
 */
public class PropertiesDialogViewModel {

    @WireVariable("pages")
    private Properties pages;
    @WireVariable
    private EntityService entityService;
    @WireVariable
    private EntityMetaService entityMetaService;

    private FormTemplate template;
    private boolean newTemplate;
    private ListModelList<FileFormat> formats;
    private EntityMeta entityMeta;

    /**
     * Initialization.
     * @param template form template
     * @param type type
     */
    @Init
    public void initTemplate(@ExecutionArgParam("template") FormTemplate template, @ExecutionArgParam("type") TemplateType type) {
        if (template == null) {
            this.template = (FormTemplate) entityService.newEmptyEntity(getEntityMeta());
            this.template.setDraft(true);
            this.template.setFormat(FileFormat.EXCEL);
            this.template.setType(type);
            newTemplate = true;
        } else {
            this.template = template;
        }
        refreshFormatsList();
    }

    /**
     * Returns entity meta.
     * @return entity meta
     */
    public EntityMeta getEntityMeta() {
        if (entityMeta == null) {
            entityMeta = entityMetaService.getEntityMetaByKey(FormTemplate.METADATA_KEY, null);
        }
        return entityMeta;
    }

    /**
     * Closes window by button.
     * @param view view
     */
    @Command
    public void save(@ContextParam(ContextType.VIEW) Component view) {
        if (newTemplate) {
            Map<String, Object> map = new HashMap<>();
            map.put("targetPage", "form.constructor.constructor");
            Executions.getCurrent().setAttribute("template", template);
            view.detach();
            BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
        } else {
            view.detach();
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
     * Sets form template.
     * @param template form template
     */
    public void setTemplate(FormTemplate template) {
        this.template = template;
    }

    /**
     * Draft onCheck action.
     * @param draft is draft
     */
    @Command
    public void checkDraft(@BindingParam("draft") boolean draft) {
        template.setDraft(draft);
    }

    /**
     * Gets file formats.
     * @return file formats
     */
    public ListModelList<FileFormat> getFormats() {
        return formats;
    }

    /**
     * Gets template types.
     * @return template types
     */
    public ListModelList<TemplateType> getTypes() {
        ListModelList<TemplateType> types = new ListModelList<>();
        Stream.of(TemplateType.values()).forEach(v -> types.add(v));
        return types;
    }

    /**
     * Template type description.
     * @param type type
     * @return type description
     */
    public String getTypeDescription(TemplateType type) {
        switch (type) {
            case IMPORT:
                return Labels.getLabel("properties_dialog_type_import");
            case EXPORT:
                return Labels.getLabel("properties_dialog_type_export");
            case FORM:
                return Labels.getLabel("properties_dialog_type_form");
            default:
                return "";
        }
    }

    /**
     * Refreshes list of file formats after type changing.
     */
    @Command
    @NotifyChange({"formats", "template"})
    public void refreshFormatsList() {
        formats = new ListModelList<>();
        if (template.getType().equals(TemplateType.IMPORT)) {
            Stream.of(FileFormat.values()).forEach(v -> formats.add(v));
        } else {
            template.setFormat(FileFormat.EXCEL);
            formats.add(FileFormat.EXCEL);
        }
    }

    /**
     * Returns is new template.
     * @return is new template
     */
    public boolean getNewTemplate() {
        return this.newTemplate;
    }
}