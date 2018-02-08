package ru.masterdm.crs.web.model.form.templates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import ru.masterdm.crs.domain.FileInfo;
import ru.masterdm.crs.domain.entity.attribute.FileInfoAttribute;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.Where;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.domain.form.TemplateType;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.FormTemplateService;
import ru.masterdm.crs.web.domain.UserProfile;

/**
 * Templates model.
 * @author Vladimir Shvets
 */
public class TemplatesViewModel {

    @WireVariable("pages")
    private Properties pages;
    @WireVariable
    private FormTemplateService formTemplateService;
    @WireVariable
    private EntityMetaService entityMetaService;
    @WireVariable
    private EntityService entityService;
    @WireVariable("userProfile")
    private UserProfile userProfile;
    private TemplateType type;
    private List<FormTemplate> selected;

    /**
     * Initialization.
     * @param strType template type
     */
    @Init
    public void init(@BindingParam("type") String strType) {
        selected = new ArrayList<>();
        setType(TemplateType.valueOf(strType));
    }

    /**
     * Open properties dialog.
     */
    @Command
    public void newFormTemplate() {
        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        Window window = (Window) Executions.createComponents(pages.getProperty("form.constructor.properties_dialog"), null, params);
        window.doModal();
    }

    /**
     * Edits tempalte.
     * @param template form template
     */
    @Command
    public void editFormTemplate(@BindingParam("template") FormTemplate template) {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "form.constructor.constructor");
        Executions.getCurrent().setAttribute("template", template);
        Executions.getCurrent().setAttribute("key", template.getKey());
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Returns teplates.
     * @return list templates
     */
    public List<FormTemplate> getFormTemplates() {
        Criteria criteria = null;
        if (type != null) {
            criteria = new Criteria();
            Where where = criteria.getWhere();
            where.addItem(new WhereItem(entityMetaService.getEntityMetaByKey(FormTemplate.METADATA_KEY, null)
                                                         .getAttributeMetadata(FormTemplate.FormTemplateAttributeMeta.TYPE.getKey()),
                                        Operator.EQ, type));
        }
        return formTemplateService.getFormTemplates(criteria, null, null);
    }

    /**
     * Gets selected templates.
     * @return selected items
     */
    public List<FormTemplate> getSelected() {
        return this.selected;
    }

    /**
     * Sets template selected items.
     * @param selected selected templates
     */
    public void setSelected(List<FormTemplate> selected) {
        this.selected = selected;
    }

    /**
     * Delete template.
     */
    @Command
    public void deleteFormTemplate() {
        final Object thisVm = this;
        if (selected.size() == 0)
            return;
        Messagebox.show(String.format("%s (%d)", Labels.getLabel("templates_delete_warning"), selected.size()),
                        Labels.getLabel("templates_delete_warning_title"),
                        Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, event -> {
                    if (event.getName().equals(Messagebox.ON_YES)) {
                        selected.forEach(formTemplate -> entityService.removeEntity(formTemplate));
                        selected.clear();
                        BindUtils.postNotifyChange(null, null, thisVm, "formTemplates");
                    }
                });
    }

    /**
     * Copy template.
     */
    @Command
    @NotifyChange({"formTemplates"})
    public void copyFormTemplate() {
        if (selected.size() == 0) return;
        selected.forEach(template -> {
            FormTemplate copyTemplate =
                    (FormTemplate) entityService.newEmptyEntity(entityMetaService.getEntityMetaByKey(FormTemplate.METADATA_KEY, null));
            copyTemplate.setName(template.getName());
            copyTemplate.setFormat(template.getFormat());
            copyTemplate.setDraft(template.isDraft());
            copyTemplate.setType(template.getType());
            copyTemplate.setMapperConfig(template.getMapperConfig());
            copyTemplate.setBook(new FileInfo());
            copyTemplate.getBook().setContent(entityService.getFileContent((FileInfoAttribute) template
                    .getAttribute(FormTemplate.FormTemplateAttributeMeta.BOOK.getKey()), null));
            entityService.persistEntity(copyTemplate);
        });
        selected.clear();
    }

    /**
     * Save template.
     */
    @Command
    @NotifyChange({"formTemplates"})
    public void saveFormTemplate() {
    }

    /**
     * Load form template.
     * @param event event
     */
    @Command
    @NotifyChange({"formTemplates"})
    public void loadFormTemplate(@ContextParam(ContextType.TRIGGER_EVENT) UploadEvent event) {
    }

    /**
     * Get forms list.
     * @param template template
     * @return forms list in template
     */
    public List<String> getForms(FormTemplate template) {
        List<EntityMeta> inputForms = formTemplateService.getInputForms(Collections.singletonList(template));
        List<String> forms = new ArrayList<>();
        inputForms.forEach(
                inputForm -> forms.add(String.format("%s | %s", inputForm.getName().getDescription(userProfile.getLocale()), inputForm.getKey())));
        if (forms.size() > 0)
            return forms;
        else
            return null;
    }

    /**
     * Returns templates type.
     * @return templates type
     */
    public TemplateType getType() {
        return type;
    }

    /**
     * Sets templates type.
     * @param type templates type
     */
    public void setType(TemplateType type) {
        this.type = type;
    }
}
