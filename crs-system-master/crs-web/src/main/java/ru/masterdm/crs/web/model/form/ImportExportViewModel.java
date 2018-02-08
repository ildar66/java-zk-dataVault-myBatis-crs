package ru.masterdm.crs.web.model.form;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zss.api.Importers;
import org.zkoss.zss.api.model.Book;

import ru.masterdm.crs.domain.entity.attribute.FileInfoAttribute;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.Where;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.domain.form.TemplateType;
import ru.masterdm.crs.domain.form.mapping.ImportObject;
import ru.masterdm.crs.domain.form.mapping.MappingObject;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.FormTemplateService;
import ru.masterdm.crs.web.domain.UserProfile;
import ru.masterdm.crs.web.service.FormTemplateUiService;

/**
 * Import-export user interface view model.
 * @author Vladimir Shvets
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ImportExportViewModel {

    @WireVariable
    private FormTemplateService formTemplateService;
    @WireVariable
    private EntityMetaService entityMetaService;
    @WireVariable
    private FormTemplateUiService formTemplateUiService;
    @WireVariable("userProfile")
    private UserProfile userProfile;
    @WireVariable
    private EntityService entityService;

    /**
     * Returns teplates.
     * @param type template type
     * @return list templates
     */
    public List<FormTemplate> getFormTemplates(String type) {
        Criteria criteria = null;
        if (type != null) {
            criteria = new Criteria();
            Where where = criteria.getWhere();
            where.addItem(new WhereItem(entityMetaService.getEntityMetaByKey(FormTemplate.METADATA_KEY, null)
                                                         .getAttributeMetadata(FormTemplate.FormTemplateAttributeMeta.TYPE.getKey()),
                                        Operator.EQ, TemplateType.valueOf(type)));
        }
        List<FormTemplate> formTemplates = formTemplateService.getFormTemplates(criteria, null, null);
        return formTemplates
                .stream()
                .filter(formTemplate -> formTemplate.getMapper().getObjects().stream().noneMatch(MappingObject::isContext))
                .collect(Collectors.toList());
    }

    /**
     * Opens import/export dialog.
     * @param template template
     * @param command global command
     */
    @Command
    public void processCommand(@BindingParam("template") FormTemplate template, @BindingParam("command") String command) {
        formTemplateUiService.process(template, command);
    }

    /**
     * Imports data from file.
     * @param template template
     */
    @GlobalCommand
    public void importFile(@BindingParam("template") FormTemplate template) {
        formTemplateUiService.importFile(template);
    }

    /**
     * Exports data to file.
     * @param template template
     */
    @GlobalCommand
    public void exportFile(@BindingParam("template") FormTemplate template) {
        InputStream bookContent = entityService.getFileContent(
                (FileInfoAttribute) template.getAttribute(FormTemplate.FormTemplateAttributeMeta.BOOK.getKey()), null);
        try {
            Book book = Importers.getImporter().imports(bookContent, template.getName().getDescription(userProfile.getLocale()));
            List<ImportObject> importObjects = formTemplateUiService.prepareFormMap(book, template.getMapper(), TemplateType.EXPORT);
            formTemplateUiService.exportForm(book, importObjects);
            formTemplateUiService.exportFile(book);
        } catch (IOException e) {
            throw new CrsException(e);
        }
    }

    /**
     * Get forms list.
     * @param template template
     * @return forms list in template
     */
    public List<String> getForms(FormTemplate template) {
        List<EntityMeta> inputForms = formTemplateService.getInputForms(Arrays.asList(template));
        List<String> forms = new ArrayList<>();
        inputForms.forEach(inputForm -> forms.add(inputForm.getName().getDescription(userProfile.getLocale())));
        if (forms.size() > 0)
            return forms;
        else
            return null;
    }
}
