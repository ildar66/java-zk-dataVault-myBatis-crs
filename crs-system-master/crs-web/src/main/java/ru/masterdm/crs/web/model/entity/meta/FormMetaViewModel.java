package ru.masterdm.crs.web.model.entity.meta;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ExecutionParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.calc.InputFormAttributeMeta;
import ru.masterdm.crs.domain.entity.BusinessAction;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.web.service.EntityMetaUiService;

/**
 * Form meta view model class.
 * @author Igor Matushak
 * @author Alexey Kirilchev
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class FormMetaViewModel extends EntityMetaViewModel {

    @WireVariable
    private EntityMetaUiService entityMetaUiService;

    @Init
    @Override
    public void initSetup(@ExecutionParam("entityMetaKey") String entityMetaKey) {
        if (entityMetaKey == null) {
            this.entityMetaKey = (String) Executions.getCurrent().getAttribute("key");
        } else
            this.entityMetaKey = entityMetaKey;
    }

    /**
     * Returns entity type.
     * @return entity type
     */
    @Override
    protected EntityType getEntityType() {
        return EntityType.INPUT_FORM;
    }

    @Override
    protected String getTargetPageKey() {
        return "entity.meta.form_meta";
    }

    @Override
    protected String getReturnPageKey() {
        return "entity.meta.form_meta_list";
    }

    @Override
    public boolean isNotFormFieldHidden() {
        return true;
    }

    /**
     * Returns entity meta.
     * @return entity meta
     */
    @Override
    public EntityMeta getEntityMeta() {
        if (entityMeta == null) {
            if (isAdd()) {
                entityMeta = entityMetaService.getEntityMetaPrototypeFactory(EntityType.INPUT_FORM).create(entityMetaKey);
                entityMeta.setName(new MultilangDescription());
                entityMeta.setComment(new MultilangDescription());

                AttributeMeta calculationAttributeMetaRef =
                        entityMeta.getAttributeMetadata(entityMetaService.getAttributeMetaKey(entityMeta, InputFormAttributeMeta.CALC.name()));
                calculationAttributeMetaRef.setName(new MultilangDescription(Labels.getLabel("entity_meta_calc_ref_description_ru"),
                                                                             Labels.getLabel("entity_meta_calc_ref_description_en")));

                AttributeMeta calculationProfileAttributeMetaRef =
                        entityMeta
                                .getAttributeMetadata(entityMetaService.getAttributeMetaKey(entityMeta, InputFormAttributeMeta.CALC_PROFILE.name()));
                calculationProfileAttributeMetaRef.setName(new MultilangDescription(Labels.getLabel("entity_meta_calc_profile_ref_description_ru"),
                                                                                    Labels.getLabel("entity_meta_calc_profile_ref_description_en")));

                addAttributeMeta();
            } else {
                entityMeta = entityMetaService.getEntityMetaByKeyNoCache(entityMetaKey, null);
                if (entityMeta.getComment() == null) {
                    entityMeta.setComment(new MultilangDescription());
                }
            }
        }
        return entityMeta;
    }

    /**
     * Navigates form meta list.
     */
    @Command
    public void navigateFormMetaList() {
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "entity.meta.form_meta_list");
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    @Override
    public boolean isEntityMetaEditAllowed() {
        return securityService.isPermittedForEntityType(securityService.getCurrentUser(),
                                                        entityMetaUiService.getPermissionEntity(EntityType.INPUT_FORM),
                                                        BusinessAction.Action.EDIT);
    }
}
