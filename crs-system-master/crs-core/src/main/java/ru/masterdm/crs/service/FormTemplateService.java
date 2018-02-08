package ru.masterdm.crs.service;

import java.time.LocalDateTime;
import java.util.List;

import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.domain.form.TemplateType;

/**
 * Form template service.
 * @author Pavel Masalov
 */
public interface FormTemplateService {

    /**
     * Persist form template.
     * Save mapper as json object at config.
     * @param formTemplate form template object
     */
    void persistFormTemplate(FormTemplate formTemplate);

    /**
     * Get form templates.
     * @param criteria query criteria
     * @param rowRange paging object
     * @param ldts loadt datetime
     * @return list of form templates
     */
    List<FormTemplate> getFormTemplates(Criteria criteria, RowRange rowRange, LocalDateTime ldts);

    /**
     * Put mapper member data into config json.
     * @param formTemplate form template object
     */
    void mapperToJsonConfig(FormTemplate formTemplate);

    /**
     * Put json config data into mapper member.
     * @param formTemplate form template object
     */
    void jsonConfigToMapper(FormTemplate formTemplate);

    /**
     * Returns list of input forms from templates list.
     * @param formTemplates list of from templates
     * @return list of input forms
     */
    List<EntityMeta> getInputForms(List<FormTemplate> formTemplates);

    /**
     * Prepares form template for importing/exporting data by entity meta.
     * @param entityMeta entity meta
     * @param type template type
     * @return form template
     */
    FormTemplate prepareFormTemplateForEntities(EntityMeta entityMeta, TemplateType type);
}
