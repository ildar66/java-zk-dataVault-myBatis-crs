package ru.masterdm.crs.web.model.entity.meta;

import org.zkoss.bind.annotation.ExecutionParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;

import ru.masterdm.crs.domain.entity.meta.EntityType;

/**
 * Entity meta view group model class.
 * @author Alexey Kirilchev
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class FormMetaGroupViewModel extends EntityMetaGroupViewModel {

    @Init
    @Override
    public void initSetup(@ExecutionParam("entityMetaGroupKey") String entityMetaGroupKey) {
        if (entityMetaGroupKey == null) {
            this.entityMetaGroupKey = (String) Executions.getCurrent().getAttribute("key");
        } else
            this.entityMetaGroupKey = entityMetaGroupKey;
    }

    @Override
    protected EntityType getEntityType() {
        return EntityType.INPUT_FORM;
    }

    @Override
    protected String getTargetPageKey() {
        return "entity.meta.form_meta_group";
    }

    @Override
    protected String getReturnPageKey() {
        return "entity.meta.form_meta_list";
    }

    @Override
    public String getMetaListTitle() {
        return Labels.getLabel("form_meta_list_title");
    }

    @Override
    public String getMetaListTitleAdd() {
        return Labels.getLabel("add_form_meta_group_title");
    }

    @Override
    public String getMetaListTitleEdit() {
        return Labels.getLabel("edit_form_meta_group_title");
    }

    @Override
    public String getListTitle() {
        return Labels.getLabel("layout_form_meta_list_title");
    }
}
