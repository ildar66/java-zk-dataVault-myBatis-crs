package ru.masterdm.crs.web.model.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.criteria.Conjunction;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.Where;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.web.domain.entity.EntityStatus;

/**
 * Edit users view model class.
 * @author Alexey Kirilchev
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditUsersViewModel extends EditEntitiesViewModel {

    /**
     * Initiates context.
     */
    @Override
    @Init
    public void initSetup() {
        super.initSetup();
    }

    /**
     * Returns attributes description.
     * @return attributes description
     */
    @Override
    public List<AttributeMeta> getAttributes() {
        return getEntityMeta() != null
               ? getEntityMeta().getAttributes().stream()
                                .filter(e -> !e.getKey().equals(User.UserAttributeMeta.FAVORITES.getKey()))
                                .collect(Collectors.toList())
               : Collections.emptyList();
    }

    /**
     * Edits entity.
     * @param entityStatus entity status
     * @param editable editable
     */
    @Override
    @Command
    public void editEntity(@BindingParam("entityStatus") EntityStatus entityStatus, @BindingParam("editable") boolean editable) {
        Map<String, Object> map = new HashMap<>();
        map.put("entityStatus", entityStatus);
        map.put("editable", editable);
        map.put("restrictedAttributes", (Predicate<AbstractAttribute>) attribute -> {
            return !attribute.getMeta().getKey().equals(User.UserAttributeMeta.FAVORITES.getKey());
        });
        Window window = (Window) Executions.createComponents(pages.getProperty("entity.edit_entity"), null, map);
        window.doModal();
    }

    /**
     * Edits entity ref.
     * @param entityStatus entity status
     * @param attributeMetaKey attribute meta key
     * @param isEdit isEdit
     */
    @Override
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
        map.put("singleSelect", attributeMetaKey.equals(User.UserAttributeMeta.DEPARTMENT.getKey()));
        Window window = (Window) Executions.createComponents(pages.getProperty("entity.edit_entity_ref"), null, map);
        window.doModal();
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

    /**
     * Updates criteria.
     */
    @Override
    protected void updateCriteria() {
        criteria = new Criteria();
        if (entityFilter != null && !entityFilter.trim().isEmpty()) {
            String filter = entityFilter.trim().toUpperCase();
            Where where = criteria.getWhere();
            where.addItem(new WhereItem(getEntityMeta().getKeyAttribute(),
                                        Operator.LIKE, filter));
            where.addItem(new WhereItem(Conjunction.OR, getEntityMeta().getAttributeMetadata(User.UserAttributeMeta.SURNAME.getKey()),
                                        Operator.LIKE, filter));
            where.addItem(new WhereItem(Conjunction.OR, getEntityMeta().getAttributeMetadata(User.UserAttributeMeta.NAME.getKey()),
                                        Operator.LIKE, filter));
            where.addItem(new WhereItem(Conjunction.OR, getEntityMeta().getAttributeMetadata(User.UserAttributeMeta.PATRONYMIC.getKey()),
                                        Operator.LIKE, filter));
            userProfile.setEntityMetaFilterByKey(entityMetaKey, filter);
        } else {
            userProfile.setEntityMetaFilterByKey(entityMetaKey, null);
        }
        activePage = 0;
    }

    /**
     * Returns references attribute meta keys.
     * @return references attribute meta keys
     */
    @Override
    public List<String> getReferencesAttributeMetaKeys() {
        List<String> referencesAttrubuteMetaKeys = new ArrayList<>();
        referencesAttrubuteMetaKeys.add(User.UserAttributeMeta.TEL_NUMBER.getKey());
        referencesAttrubuteMetaKeys.add(User.UserAttributeMeta.EMAIL.getKey());
        return referencesAttrubuteMetaKeys;
    }
}
