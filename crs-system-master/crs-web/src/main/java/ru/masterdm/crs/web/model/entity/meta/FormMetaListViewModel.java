package ru.masterdm.crs.web.model.entity.meta;

import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.VariableResolver;

import ru.masterdm.crs.domain.entity.BusinessAction;
import ru.masterdm.crs.domain.entity.meta.EntityType;

/**
 * Form meta list view model class.
 * @author Alexey Kirilchev
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class FormMetaListViewModel extends EntityMetaListViewModel {

    /**
     * Initiates context.
     */
    @Init
    public void initSetup() {
        entityMetaFilter = userProfile.getEntityMetaFilterByKey(getEntityType().name());
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.INPUT_FORM;
    }

    @Override
    protected String getTargetPageKey() {
        return "entity.meta.form_meta";
    }

    @Override
    protected String getEntityMetaGroupPageKey() {
        return "entity.meta.form_meta_group";
    }

    @Override
    public boolean isEntityMetaCreateAllowed() {
        return securityService.isPermittedForEntityType(securityService.getCurrentUser(),
                                                        entityMetaUiService.getPermissionEntity(EntityType.INPUT_FORM),
                                                        BusinessAction.Action.CREATE_NEW);
    }
}
