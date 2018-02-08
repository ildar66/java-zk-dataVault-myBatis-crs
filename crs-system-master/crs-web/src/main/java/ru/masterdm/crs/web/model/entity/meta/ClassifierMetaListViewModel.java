package ru.masterdm.crs.web.model.entity.meta;

import java.time.LocalDateTime;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.VariableResolver;

import ru.masterdm.crs.domain.entity.BusinessAction;
import ru.masterdm.crs.domain.entity.meta.EntityType;

/**
 * Classifier meta list view model class.
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ClassifierMetaListViewModel extends EntityMetaListViewModel {

    /**
     * Initiates context.
     * @param actuality actuality
     */
    @Init
    public void initSetup(@BindingParam("actuality") LocalDateTime actuality) {
        entityMetaFilter = userProfile.getEntityMetaFilterByKey(getEntityType().name());
        this.actuality = actuality;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.CLASSIFIER;
    }

    @Override
    protected String getTargetPageKey() {
        return "entity.meta.classifier_meta";
    }

    @Override
    protected String getEntityMetaGroupPageKey() {
        return "entity.meta.classifier_meta_group";
    }

    /**
     * Refreshes classifier entity metas.
     */
    @GlobalCommand
    public void refreshClassifierEntityMetas() {
        BindUtils.postNotifyChange(null, null, this, "entityMetaTreeModel");
    }

    /**
     * Resets classifier entity metas.
     * @param actuality actuality
     */
    @GlobalCommand
    public void resetClassifierEntityMetas(@BindingParam(value = "actuality") LocalDateTime actuality) {
        this.actuality = actuality;
        entityMetaGroups = null;
        entityMetaTreeModel = null;
        refreshClassifierEntityMetas();
    }

    @Override
    public boolean isEntityMetaCreateAllowed() {
        return securityService.isPermittedForEntityType(securityService.getCurrentUser(),
                                                        entityMetaUiService.getPermissionEntity(EntityType.CLASSIFIER),
                                                        BusinessAction.Action.CREATE_NEW);
    }
}
