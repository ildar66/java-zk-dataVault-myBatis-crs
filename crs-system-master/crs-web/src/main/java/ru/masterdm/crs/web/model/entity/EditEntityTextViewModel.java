package ru.masterdm.crs.web.model.entity;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import ru.masterdm.crs.domain.entity.BusinessAction;
import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.attribute.MultilangAttribute;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.SecurityService;
import ru.masterdm.crs.web.domain.UserProfile;
import ru.masterdm.crs.web.domain.entity.EntityStatus;
import ru.masterdm.crs.web.service.EntityMetaUiService;

/**
 * Edit entity text view model class.
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditEntityTextViewModel {

    @WireVariable("userProfile")
    private UserProfile userProfile;
    @WireVariable
    private EntityService entityService;
    @WireVariable
    private SecurityService securityService;
    @WireVariable
    private EntityMetaUiService entityMetaUiService;

    private EntityStatus entityStatus;
    private String attributeMetaKey;
    private boolean isEdit;
    private String value;

    /**
     * Initiates context.
     * @param entityStatus entity status
     * @param attributeMetaKey attribute meta key
     * @param isEdit is edit
     */
    @Init
    public void initSetup(@ExecutionArgParam("entityStatus") EntityStatus entityStatus,
                          @ExecutionArgParam("attributeMetaKey") String attributeMetaKey,
                          @ExecutionArgParam("isEdit") Boolean isEdit) {
        this.entityStatus = entityStatus;
        this.attributeMetaKey = attributeMetaKey;
        this.isEdit = isEdit;
        AbstractAttribute attribute = entityStatus.getEntity().getAttribute(attributeMetaKey);
        value = attribute.getMeta().isMultilang() ? ((MultilangAttribute) attribute).getValue(userProfile.getLocale())
                                                  : (String) attribute.getValue();
    }

    /**
     * Detaches window.
     * @param view view
     */
    @Command
    public void detachWindow(@ContextParam(ContextType.VIEW) Component view) {
        view.detach();
    }

    /**
     * Saves entity text.
     * @param view view
     */
    @Command
    public void saveEntityText(@ContextParam(ContextType.VIEW) Component view) {
        if (getEntityStatus().getEntity().getAttribute(attributeMetaKey).getMeta().isMultilang()) {
            ((MultilangAttribute) getEntityStatus().getEntity().getAttribute(attributeMetaKey)).setValue(value, userProfile.getLocale());
        } else {
            entityStatus.getEntity().getAttribute(attributeMetaKey).setValue(value);
        }
        entityService.persistEntity(entityStatus.getEntity());
        BindUtils.postGlobalCommand(null, null, "entityRefresh", null);
        BindUtils.postGlobalCommand(null, null, "entitiesRefresh", null);
        view.detach();
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
     * Returns attribute meta key.
     * @return attribute meta key
     */
    public String getAttributeMetaKey() {
        return attributeMetaKey;
    }

    /**
     * Returns is edit.
     * @return is edit
     */
    public boolean isEdit() {
        return isEdit;
    }

    /**
     * Returns value.
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets value.
     * @param value value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns is entity edit allowed.
     * @return is entity edit allowed
     */
    public boolean isEntityEditAllowed() {
        return securityService.isPermittedForEntityType(securityService.getCurrentUser(),
                                                        entityMetaUiService.getPermissionEntity(EntityType.DICTIONARY),
                                                        BusinessAction.Action.EDIT);
    }
}
