package ru.masterdm.crs.web.model.form.constructor;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Window;

import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.domain.form.CreateOption;
import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.domain.form.RepeatType;
import ru.masterdm.crs.domain.form.TemplateType;
import ru.masterdm.crs.domain.form.mapping.MappingField;
import ru.masterdm.crs.domain.form.mapping.MappingObject;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.web.domain.UserProfile;

/**
 * View model for object dialog.
 * @author Vladimir Shvets
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ObjectViewModel {

    @WireVariable("userProfile")
    private UserProfile userProfile;

    private FormTemplate template;
    private MappingObject object;
    private boolean isNew;
    private MappingObject parent;
    @WireVariable
    private EntityMetaService entityMetaService;

    /**
     * Initialization.
     * @param template form settings
     * @param object mapping object
     * @param contextObject contextObject
     * @param isNew is object new
     */
    @Init
    public void initObjectViewModel(@ExecutionArgParam("form") FormTemplate template, @ExecutionArgParam("object") MappingObject object,
                                    @ExecutionArgParam("contextObject") MappingObject contextObject,
                                    @ExecutionArgParam("isNew") boolean isNew) {
        this.template = template;
        this.object = object;
        this.isNew = isNew;
        if (isNew) {
            this.object.setName(object.getEntityMeta().getName().getDescription(userProfile.getLocale()));
            if (contextObject == null) {
                this.parent = null;
            } else {
                this.parent = contextObject;
                object.setRepeat(contextObject.getRepeat());
            }
            if ((template.getType().equals(TemplateType.FORM))
                && (object.getEntityMeta().getType().equals(EntityType.INPUT_FORM)
                    || object.getEntityMeta().getType().equals(EntityType.CLASSIFIER))) {
                object.setCreateOption(CreateOption.IF_NOT_EXISTS);
                object.setUpdateOption(true);

            }
        } else {
            if (object.getParent() instanceof MappingObject) {
                this.parent = (MappingObject) object.getParent();
            } else {
                this.parent = null;
            }
        }
    }

    /**
     * Closes window.
     * @param window window
     */
    @Command
    @NotifyChange({"*"})
    public void save(@BindingParam("cmp") Window window) {
        Map<String, Object> params = new HashMap<>();
        params.put("mo", object);
        params.put("isNew", isNew);
        BindUtils.postGlobalCommand(null, null, "saveObject", params);
        window.detach();
    }

    /**
     * Is saving disabled.
     * @return true if saving disabled
     */

    public boolean getSaveDisabled() {
        RepeatType parentRepeat = RepeatType.ONE;
        if (parent != null) {
            parentRepeat = parent.getRepeat();
        }
        if (object.isContext()) return false;
        if ((object.getRepeat().equals(RepeatType.DOWN)) && (parentRepeat != object.getRepeat())) {
            boolean excelFlag = false;
            for (MappingField field : object.getFields()) {
                if (field.isMapped()) {
                    excelFlag = true;
                }
            }
            if (!excelFlag) return true;
        }
        if (template.getType().equals(TemplateType.IMPORT)) {
            if (!object.getCreateOption().equals(CreateOption.ALWAYS)) {
                for (MappingField field : object.getFields()) {
                    if (Boolean.TRUE.equals(field.isKey())) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    /**
     * Returns object.
     * @return object
     */

    public MappingObject getObject() {
        return object;
    }

    /**
     * Sets object.
     * @param object object
     */
    public void setObject(MappingObject object) {
        this.object = object;
    }

    /**
     * Excel checked.
     * @param field field
     * @param checked checked
     */
    @Command
    @NotifyChange({"object", "saveDisabled"})
    public void onCheckExcel(@BindingParam("object") MappingField field, @BindingParam("checked") Boolean checked) {
        if (checked) {
            field.setMapped(true);
            field.setWrite(false);
        } else {
            field.setMapped(false);
        }
    }

    /**
     * Key checked.
     * @param field field
     * @param checked checked
     */
    @Command
    @NotifyChange({"object", "saveDisabled"})
    public void onCheckKey(@BindingParam("object") MappingField field, @BindingParam("checked") Boolean checked) {
        field.setKey(checked);
        if (!checked) {
            field.setWrite(false);
        }
    }

    /**
     * Write checked.
     * @param field field
     * @param checked checked
     */
    @Command
    @NotifyChange({"object", "saveDisabled"})
    public void onCheckWrite(@BindingParam("object") MappingField field, @BindingParam("checked") Boolean checked) {
        field.setWrite(checked);
    }

    /**
     * Context checked.
     * @param checked checked
     */
    @Command
    @NotifyChange({"object", "saveDisabled"})
    public void onCheckContext(@BindingParam("checked") Boolean checked) {
        object.setContext(checked);
        if (checked) {
            object.setRepeat(RepeatType.ONE);
            object.setCreateOption(CreateOption.NEVER);
            object.setUpdateOption(false);
            object.getFields().stream().filter(MappingField::isKey).forEach(mappingField -> mappingField.setKey(false));
        }
    }

    /**
     * Key checked.
     * @param checked checked
     */
    @Command
    public void onCheckUpdate(@BindingParam("checked") Boolean checked) {
        object.setUpdateOption(checked);
    }

    /**
     * Sets repeat property.
     * @param repeat repeat property
     */
    @Command
    @NotifyChange({"saveDisabled"})
    public void setRepeat(@BindingParam("repeat") String repeat) {
        object.setRepeat(RepeatType.valueOf(repeat));
    }

    /**
     * Sets repeat property.
     * @param create repeat property
     */
    @Command
    @NotifyChange({"object", "saveDisabled"})
    public void setCreate(@BindingParam("create") String create) {
        object.setCreateOption(CreateOption.valueOf(create));
        if (!create.equals(CreateOption.IF_NOT_EXISTS.name())) {
            object.setUpdateOption(false);
        }
    }

    /**
     * Returns form settings.
     * @return form settings
     */

    public FormTemplate getTemplate() {
        return template;
    }

    /**
     * Sets form settings.
     * @param template form settings
     */
    public void setTemplate(FormTemplate template) {
        this.template = template;
    }

    /**
     * Returns parent object.
     * @return parent object
     */
    public MappingObject getParent() {
        return parent;
    }

    /**
     * Sets parent object.
     * @param parent parent object
     */
    public void setParent(MappingObject parent) {
        this.parent = parent;
    }

    /**
     * Sets field's children.
     * @param mappingField mapping field
     */
    @Command
    public void setFieldChildren(@BindingParam("field") MappingField mappingField) {
        if (mappingField.getObject() != null)
            return;
        EntityMeta refEntityMeta = entityMetaService.getEntityMetaByKey(mappingField.getAttributeMeta().getEntityKey(), null);
        mappingField.setObject(new MappingObject(refEntityMeta));
        mappingField.getObject().setName(refEntityMeta.getName().getDescription(userProfile.getLocale()));
        BindUtils.postNotifyChange(null, null, mappingField, "*");
    }

}
