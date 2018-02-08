package ru.masterdm.crs.web.model.entity;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.io.input.ReaderInputStream;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.util.media.Media;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.attribute.EntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.FileInfoAttribute;
import ru.masterdm.crs.domain.entity.attribute.MultilangAttribute;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.web.domain.DataSlice;
import ru.masterdm.crs.web.domain.UserProfile;
import ru.masterdm.crs.web.domain.entity.EntityStatus;
import ru.masterdm.crs.web.service.EntityMetaUiService;

/**
 * Edit entity view model class.
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditEntityViewModel {

    @WireVariable
    private EntityService entityService;
    @WireVariable("pages")
    private Properties pages;
    @WireVariable("dataSlice")
    protected DataSlice dataSlice;
    @WireVariable("userProfile")
    protected UserProfile userProfile;
    @WireVariable("entityMetaUiService")
    private EntityMetaUiService entityMetaUiService;

    private EntityStatus entityStatus;
    private boolean editable;
    private Predicate<AbstractAttribute> restrictedAttributes;

    /**
     * Initiates context.
     * @param entityStatus entity status
     * @param editable editable
     * @param restrictedAttributes restricted attributes
     */
    @Init
    public void initSetup(@ExecutionArgParam("entityStatus") EntityStatus entityStatus, @ExecutionArgParam("editable") boolean editable,
                          @ExecutionArgParam("restrictedAttributes") Predicate<AbstractAttribute> restrictedAttributes) {
        this.entityStatus = entityStatus;
        this.editable = editable;
        this.restrictedAttributes = restrictedAttributes == null ? attribute -> true : restrictedAttributes;
    }

    /**
     * Detaches window.
     * @param view view
     */
    @Command
    public void detachWindow(@ContextParam(ContextType.VIEW) Component view) {
        view.detach();
        entityStatus.setEditingStatus(false);
        BindUtils.postGlobalCommand(null, null, "entitiesRefresh", null);
    }

    /**
     * Changes editable status.
     * @param view view
     */
    @Command
    @SmartNotifyChange("*")
    public void changeEditableStatus(@ContextParam(ContextType.VIEW) Window view) {
        view.setAction("");
        view.detach();
        entityStatus.setEditingStatus(!entityStatus.isEditingStatus());
        Map<String, Object> map = new HashMap<>();
        map.put("entityStatus", entityStatus);
        map.put("editable", editable);
        map.put("restrictedAttributes", restrictedAttributes);
        Window window = (Window) Executions.createComponents(pages.getProperty("entity.edit_entity"), null, map);
        window.setAction("");
        window.doModal();
    }

    /**
     * Edits entity text.
     * @param entityStatus entity status
     * @param attributeMetaKey attribute meta key
     * @param isEdit isEdit
     */
    @Command
    public void editEntityText(@BindingParam("entityStatus") EntityStatus entityStatus,
                               @BindingParam("attributeMetaKey") String attributeMetaKey,
                               @BindingParam("isEdit") boolean isEdit) {
        Map<String, Object> map = new HashMap<>();
        map.put("entityStatus", entityStatus);
        map.put("attributeMetaKey", attributeMetaKey);
        map.put("isEdit", isEdit);
        Window window = (Window) Executions.createComponents(pages.getProperty("entity.edit_entity_text"), null, map);
        window.doModal();
    }

    /**
     * Edits entity ref.
     * @param entityStatus entity status
     * @param attributeMetaKey attribute meta key
     * @param isEdit isEdit
     */
    @Command
    public void editEntityRef(@BindingParam("entityStatus") EntityStatus entityStatus,
                              @BindingParam("attributeMetaKey") String attributeMetaKey,
                              @BindingParam("isEdit") boolean isEdit) {
        if (!entityMetaUiService.getReferenceValid(entityStatus, attributeMetaKey)) {
            Messagebox.show(Labels.getLabel("edit_entities_referenced_entity_removed_message"));
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("entityStatus", entityStatus);
        map.put("attributeMetaKey", attributeMetaKey);
        map.put("isEdit", isEdit);
        Window window = (Window) Executions.createComponents(pages.getProperty("entity.edit_entity_ref"), null, map);
        window.doModal();
    }

    /**
     * Saves entity.
     * @param view view
     */
    @Command({"saveEntity"})
    public void saveEntity(@ContextParam(ContextType.VIEW) Component view) {
        view.detach();
        entityStatus.setEditingStatus(false);
        entityService.persistEntity(entityStatus.getEntity());
        BindUtils.postGlobalCommand(null, null, "entitiesRefresh", null);
    }

    /**
     * Refreshes entity.
     */
    @GlobalCommand
    public void entityRefresh() {
        BindUtils.postNotifyChange(null, null, this, "entityStatus");
        BindUtils.postNotifyChange(null, null, this, "attributes");
    }

    /**
     * Returns entity status.
     * @return entity status
     */
    public EntityStatus getEntityStatus() {
        return entityStatus;
    }

    /**
     * Sets entity status.
     * @param entityStatus entity status
     */
    public void setEntityStatus(EntityStatus entityStatus) {
        this.entityStatus = entityStatus;
    }

    /**
     * Returns title.
     * @return title
     */
    public String getTitle() {
        String title = null;
        String key = getEntityStatus().getEntity().getKey();
        LocalDateTime dataSliceDate = dataSlice.getDataSliceDateByKey(getEntityStatus().getEntity().getMeta().getKey());
        if (dataSliceDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Labels.getLabel("date_time_format"));
            String formattedString = dataSliceDate.format(formatter);
            title = MessageFormat.format(Labels.getLabel("edit_entity_data_slice_title"), key, formattedString);
        } else {
            if (getEntityStatus().isEditingStatus()) {
                title = MessageFormat.format(Labels.getLabel("edit_entity_title"), key);
            } else {
                title = MessageFormat.format(Labels.getLabel("view_entity_title"), key);
            }
        }
        return title;
    }

    /**
     * File upload.
     * @param uploadEvent file upload event
     * @param fileInfoAttribute file info attribute
     */
    @Command
    public void fileUpload(@ContextParam(ContextType.TRIGGER_EVENT) UploadEvent uploadEvent,
                           @BindingParam("fileInfoAttribute") FileInfoAttribute fileInfoAttribute) {
        Media media = uploadEvent.getMedia();
        InputStream inputStream = getInputStream(media);
        try {
            fileInfoAttribute.setContent(inputStream);
        } catch (Exception e) {
            throw new CrsException(e.getMessage(), e);
        }
        fileInfoAttribute.setMimeType(media.getContentType());
        fileInfoAttribute.setName(media.getName());
        fileInfoAttribute.setLinkRemoved(false);
        BindUtils.postNotifyChange(null, null, this, "entityStatus");
    }

    /**
     * File download.
     * @param fileInfoAttribute file info attribute
     */
    @Command
    public void fileDownload(@BindingParam("fileInfoAttribute") FileInfoAttribute fileInfoAttribute) {
        InputStream fileContent = entityService.getFileContent(fileInfoAttribute, null);
        Filedownload.save(fileContent, fileInfoAttribute.getMimeType(), fileInfoAttribute.getName());
    }

    /**
     * Unattach file.
     * @param fileInfoAttribute file info attribute
     */
    @Command
    public void unattachFile(@BindingParam("fileInfoAttribute") FileInfoAttribute fileInfoAttribute) {
        EventListener<Messagebox.ClickEvent> clickListener = event -> {
            if (Messagebox.Button.YES.equals(event.getButton())) {
                fileInfoAttribute.setLinkRemoved(true);
                BindUtils.postNotifyChange(null, null, EditEntityViewModel.this, "entityStatus");
            }
        };
        Messagebox.show(Labels.getLabel("edit_entities_unattach_file_message"), Labels.getLabel("edit_entities_unattach_file_title"),
                        new Messagebox.Button[] {Messagebox.Button.YES, Messagebox.Button.NO}, Messagebox.QUESTION, clickListener);
    }

    /**
     * Returns media input stream.
     * @param media meda
     * @return media input stream
     */
    private InputStream getInputStream(Media media) {
        try {
            return media.getStreamData();
        } catch (Exception e1) {
            return new ReaderInputStream(media.getReaderData(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Returns true if can change attribute meta key, false otherwise.
     * @param attributeMeta attribute meta
     * @return true if can change attribute meta key, false otherwise
     */
    public boolean getReferenceAttributeDisabled(AttributeMeta attributeMeta) {
        return (attributeMeta.getType().equals(AttributeType.REFERENCE)
                && attributeMeta.getEntityKey() != null
                && attributeMeta.getEntityKey().equals(Calculation.METADATA_KEY));
    }

    /**
     * Returns editable.
     * @return editable
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Returns attribute value.
     * @param attribute attribute
     * @return attribute value
     */
    public Object getAttributeValue(EntityAttribute attribute) {
        Entity entity = (Entity) attribute.getEntity();
        if (entity == null || attribute.getMeta().getAttributeKey() == null) {
            return null;
        }
        AbstractAttribute linkedAttribute = entity.getAttribute(attribute.getMeta().getAttributeKey());
        return linkedAttribute.getMeta().isMultilang()
               ? ((MultilangAttribute) linkedAttribute).getValue(userProfile.getLocale()) : linkedAttribute.getValue();
    }

    /**
     * Returns attributes.
     * @return attributes
     */
    public List<AbstractAttribute> getAttributes() {
        return entityStatus.getEntity().getSortedAttributes().stream()
                           .filter(restrictedAttributes)
                           .collect(Collectors.toList());
    }

    /**
     * Returns references attribute meta keys.
     * @return references attribute meta keys
     */
    public List<String> getReferencesAttributeMetaKeys() {
        if (!entityStatus.getEntity().getMeta().getKey().equals(User.METADATA_KEY))
            return new ArrayList<>();
        List<String> referencesAttrubuteMetaKeys = new ArrayList<>();
        referencesAttrubuteMetaKeys.add(User.UserAttributeMeta.TEL_NUMBER.getKey());
        referencesAttrubuteMetaKeys.add(User.UserAttributeMeta.EMAIL.getKey());
        return referencesAttrubuteMetaKeys;
    }

    /**
     * Edits entity references.
     * @param entityStatus entity status
     * @param attributeMetaKey attribute meta key
     * @param editable editable
     */
    @Command
    public void editEntityReferences(@BindingParam("entityStatus") EntityStatus entityStatus,
                                     @BindingParam("attributeMetaKey") String attributeMetaKey,
                                     @BindingParam("editable") boolean editable) {
        if (!entityMetaUiService.getReferenceValid(entityStatus, attributeMetaKey)) {
            Messagebox.show(Labels.getLabel("edit_entities_referenced_entity_removed_message"));
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("entityStatus", entityStatus);
        map.put("attributeMetaKey", attributeMetaKey);
        map.put("editable", editable);
        Window window = (Window) Executions.createComponents(pages.getProperty("entity.edit_entity_references"), null, map);
        window.doModal();
    }
}
