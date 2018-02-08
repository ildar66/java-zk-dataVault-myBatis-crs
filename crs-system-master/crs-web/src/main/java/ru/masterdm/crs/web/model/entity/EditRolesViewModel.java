package ru.masterdm.crs.web.model.entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zul.Window;

import ru.masterdm.crs.domain.entity.Role;
import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.criteria.Conjunction;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.Where;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.web.domain.entity.EntityStatus;

/**
 * Edit roles view model class.
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditRolesViewModel extends EditEntitiesViewModel {

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
                                .filter(e -> !e.getKey().equals(Role.RoleAttributeMeta.EMBEDDED.getKey()))
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
            return !attribute.getMeta().getKey().equals(Role.RoleAttributeMeta.EMBEDDED.getKey());
        });
        Window window = (Window) Executions.createComponents(pages.getProperty("entity.edit_entity"), null, map);
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
            where.addItem(new WhereItem(Conjunction.OR, getEntityMeta().getAttributeMetadata(Role.RoleAttributeMeta.NAME.getKey()),
                                        Operator.LIKE, filter));
            where.addItem(new WhereItem(Conjunction.OR, getEntityMeta().getAttributeMetadata(Role.RoleAttributeMeta.DESCRIPTION.getKey()),
                                        Operator.LIKE, filter));
            userProfile.setEntityMetaFilterByKey(entityMetaKey, filter);
        } else {
            userProfile.setEntityMetaFilterByKey(entityMetaKey, null);
        }
        activePage = 0;
    }
}
