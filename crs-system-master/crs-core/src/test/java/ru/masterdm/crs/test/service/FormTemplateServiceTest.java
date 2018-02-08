package ru.masterdm.crs.test.service;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.masterdm.crs.domain.form.FormTemplate.FormTemplateAttributeMeta;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.masterdm.crs.domain.FileInfo;
import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.entity.attribute.FileInfoAttribute;
import ru.masterdm.crs.domain.entity.attribute.MultilangAttribute;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.domain.form.CreateOption;
import ru.masterdm.crs.domain.form.FileFormat;
import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.domain.form.RepeatType;
import ru.masterdm.crs.domain.form.TemplateType;
import ru.masterdm.crs.domain.form.mapping.Mapper;
import ru.masterdm.crs.domain.form.mapping.MappingField;
import ru.masterdm.crs.domain.form.mapping.MappingObject;
import ru.masterdm.crs.domain.form.mapping.Range;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.FormTemplateService;

/**
 * Form template service tests.
 * @author Pavel Masalov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FormTemplateServiceTest {

    @Autowired
    private EntityService entityService;
    @Autowired
    private EntityMetaService entityMetaService;
    @Autowired
    private FormTemplateService formTemplateService;

    private EntityMeta formTemplateMeta;
    private EntityMeta testEntityMeta;
    private String jsonMapper1;

    /**
     * Test attrribute and member synchronisation.
     */
    @Test
    public void test01EmbeddedFormTemplate() {
        // set attribute, read member
        FormTemplate formTemplate1 = (FormTemplate) entityService.newEmptyEntity(FormTemplate.METADATA_KEY);
        formTemplate1.setAttributeValue(FormTemplateAttributeMeta.BOOK.getKey(), "file-name");
        assertThat(formTemplate1.getBook().getName()).isEqualTo("file-name");
        formTemplate1.setAttributeValue(FormTemplateAttributeMeta.DRAFT.getKey(), true);
        assertThat(formTemplate1.isDraft()).isEqualTo(true);
        formTemplate1.setAttributeValue(FormTemplateAttributeMeta.FORMAT.getKey(), FileFormat.EXCEL.name());
        assertThat(formTemplate1.getFormat()).isEqualTo(FileFormat.EXCEL);
        formTemplate1.setAttributeValue(FormTemplateAttributeMeta.MAPPER_CONFIG.getKey(), "{json}");
        assertThat(formTemplate1.getMapperConfig()).isEqualTo("{json}");
        ((MultilangAttribute) formTemplate1.getAttribute(FormTemplateAttributeMeta.NAME.getKey())).getMultilangDescription()
                                                                                                  .setDescriptionEn("testtest");
        assertThat(formTemplate1.getName().getDescriptionEn()).isEqualTo("testtest");
        formTemplate1.setAttributeValue(FormTemplateAttributeMeta.TYPE.getKey(), TemplateType.EXPORT.name());
        assertThat(formTemplate1.getType()).isEqualTo(TemplateType.EXPORT);

        // set member, read attribute
        FormTemplate formTemplate2 = (FormTemplate) entityService.newEmptyEntity(FormTemplate.METADATA_KEY);
        formTemplate2.setBook(new FileInfo("name-file", "mime-type"));
        assertThat(formTemplate2.getAttributeValue(FormTemplateAttributeMeta.BOOK.getKey())).isEqualTo("name-file");
        assertThat(((FileInfoAttribute) formTemplate2.getAttribute(FormTemplateAttributeMeta.BOOK.getKey())).getMimeType()).isEqualTo("mime-type");
        formTemplate2.setDraft(false);
        assertThat(formTemplate2.getAttributeValue(FormTemplateAttributeMeta.DRAFT.getKey())).isEqualTo(false);
        formTemplate2.setFormat(FileFormat.DBF);
        assertThat(formTemplate2.getAttributeValue(FormTemplateAttributeMeta.FORMAT.getKey())).isEqualTo(FileFormat.DBF.name());
        formTemplate2.setMapperConfig("{\"json\":\"nosj\"}");
        assertThat(formTemplate2.getAttributeValue(FormTemplateAttributeMeta.MAPPER_CONFIG.getKey())).isEqualTo("{\"json\":\"nosj\"}");
        formTemplate2.getName().setDescriptionEn("stepstep");
        assertThat(((MultilangAttribute) formTemplate2.getAttribute(FormTemplateAttributeMeta.NAME.getKey())).getMultilangDescription()
                                                                                                             .getDescriptionEn())
                .isEqualTo("stepstep");
        formTemplate2.setType(TemplateType.IMPORT);
        assertThat(formTemplate2.getAttributeValue(FormTemplateAttributeMeta.TYPE.getKey())).isEqualTo(TemplateType.IMPORT.name());
    }

    /**
     * Test form template persists.
     */
    @Test
    public void test02PersistsAndGet() {
        FormTemplate formTemplate = (FormTemplate) entityService.newEmptyEntity(FormTemplate.METADATA_KEY);
        formTemplate.setKey("FT02");
        formTemplate.getName().setDescriptionEn("nnnnn");
        formTemplate.setFormat(FileFormat.EXCEL);
        formTemplate.setType(TemplateType.FORM);
        formTemplateService.persistFormTemplate(formTemplate);

        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(formTemplateMeta.getKeyAttribute(), Operator.EQ, formTemplate.getKey()));
        List<FormTemplate> l = formTemplateService.getFormTemplates(criteria, null, null);
        assertThat(l).hasSize(1);
        FormTemplate check = l.get(0);

        assertThat(check.getName().getDescriptionEn()).isEqualTo(formTemplate.getName().getDescriptionEn());
        assertThat(check.getType()).isEqualTo(formTemplate.getType());
        assertThat(check.getFormat()).isEqualTo(formTemplate.getFormat());
        assertThat(check.isDraft()).isEqualTo(formTemplate.isDraft());
    }

    /**
     * Test deserialise json config to mapper object.
     */
    @Test
    public void test03JsonToMapper() {
        final int four = 4;
        FormTemplate formTemplate = (FormTemplate) entityService.newEmptyEntity(FormTemplate.METADATA_KEY);
        formTemplate.setMapperConfig(jsonMapper1);
        formTemplateService.jsonConfigToMapper(formTemplate);

        Mapper mapper = formTemplate.getMapper();

        assertThat(mapper.getObjects()).hasSize(2);
        assertThat(mapper.getObjects().get(0).getName()).isEqualTo("name-name-name");
        assertThat(mapper.getObjects().get(0).getFields()).hasSize(2);
        assertThat(mapper.getObjects().get(1).getName()).isEqualTo("name-name-name2");
        assertThat(mapper.getObjects().get(1).getFields()).hasSize(four);
    }

    /**
     * Test serialise mapper object to json config.
     */
    @Test
    public void test04MapperToJson() {
        Mapper mapper = new Mapper();
        MappingObject mappingObject = new MappingObject(testEntityMeta);
        mappingObject.setRepeat(RepeatType.DOWN);
        mappingObject.setParent(mapper);
        mapper.getObjects().add(mappingObject);
        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setMapper(mapper);
        formTemplateService.mapperToJsonConfig(formTemplate);
        String json = formTemplate.getMapperConfig();

        formTemplateService.jsonConfigToMapper(formTemplate);
        assertThat(formTemplate.getMapper().getObjects().get(0).getEntityMeta()).isEqualTo(mapper.getObjects().get(0).getEntityMeta());

        assertThat(formTemplate.getMapper().getObjects().get(0).getFields())
                .extracting("attributeMeta").containsExactlyInAnyOrder((Object[]) mapper.getObjects().get(0).getFields().stream()
                                                                                        .map(f -> f.getAttributeMeta())
                                                                                        .collect(Collectors.toList())
                                                                                        .toArray(new AttributeMeta[] {}));
    }

    /**
     * Test get input forms from template.
     */
    @Test
    public void test05getInputForms() {
        String key = "INFOR01";

        EntityMeta calculationMeta = ((Calculation) entityService.newEmptyEntity(Calculation.METADATA_KEY)).getMeta();
        EntityMeta entityMeta = entityMetaService.getEntityMetaPrototypeFactory(EntityType.INPUT_FORM).create(key);

        Mapper mapper = new Mapper();

        MappingObject calculationObject = new MappingObject(calculationMeta);
        mapper.addObject(calculationObject);
        calculationObject.addObject(new MappingObject(entityMeta));

        calculationObject = new MappingObject(calculationMeta);
        mapper.addObject(calculationObject);
        calculationObject.addObject(new MappingObject(entityMeta));

        FormTemplate formTemplate = new FormTemplate();
        formTemplate.setMapper(mapper);

        List<EntityMeta> inputForms = formTemplateService.getInputForms(Arrays.asList(formTemplate));
        assertThat(inputForms).hasSize(1);
        assertThat(inputForms.get(0).getKey()).isEqualTo(key);
    }

    /**
     * Test creation form template for custom entity meta.
     * @throws Exception exception
     */
    @Test
    public void test06ExportImportEntitiesTemplate() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        EntityMeta entityMeta = objectMapper.readValue(EntityMetaServiceTest.class.getResourceAsStream("dv_service_test_entity_meta.json"),
                                                       EntityMeta.class);
        FormTemplate formTemplate = formTemplateService.prepareFormTemplateForEntities(entityMeta, TemplateType.IMPORT);
        Mapper mapper = formTemplate.getMapper();
        assertThat(mapper).isNotNull();
        assertThat(mapper.getObjects()).isNotNull();
        assertThat(mapper.getObjects().size()).isNotEqualTo(0);

        MappingObject mappingObject = mapper.getObjects().get(0);
        assertThat(mappingObject.getEntityMeta().getKey()).isEqualTo(entityMeta.getKey());
        assertThat(mappingObject.getRepeat()).isEqualTo(RepeatType.DOWN);
        assertThat(mappingObject.getCreateOption()).isEqualTo(CreateOption.IF_NOT_EXISTS);
        assertThat(mappingObject.getUpdateOption()).isEqualTo(true);
        assertThat(mappingObject.getFields()).isNotNull();
        assertThat(mappingObject.getFields().size()).isEqualTo(entityMeta.getAttributes().size() + 1);

        MappingField mappingField = mappingObject.getFields().get(0);
        assertThat(mappingField.getAttributeMeta().getKey()).isEqualTo(entityMeta.getKeyAttribute().getKey());
        assertThat(mappingField.isKey()).isEqualTo(true);
        Range range = mappingField.getRange();
        assertThat(range.getSheet()).isEqualTo(entityMeta.getKey());
        assertThat(range.getColumn()).isEqualTo(0);
        assertThat(range.getRow()).isEqualTo(1);

        mappingField = mappingObject.getFields().get(1);
        assertThat(mappingField.getAttributeMeta().getKey()).isEqualTo(entityMeta.getAttributes().get(0).getKey());
        assertThat(mappingField.isKey()).isEqualTo(false);
        range = mappingField.getRange();
        assertThat(range.getSheet()).isEqualTo(entityMeta.getKey());
        assertThat(range.getColumn()).isEqualTo(1);
        assertThat(range.getRow()).isEqualTo(1);
    }

    /**
     * Init common data for tests.
     * @throws Exception initialise error
     */
    @Before
    public void setup() throws Exception {
        formTemplateMeta = entityMetaService.getEntityMetaByKey(FormTemplate.METADATA_KEY, null);
        jsonMapper1 = IOUtils.toString(EntityServicePersistTest.class.getResourceAsStream("form-template-mapper1.json"),
                                       StandardCharsets.UTF_8);

        ObjectMapper objectMapper = new ObjectMapper();
        testEntityMeta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_TEST_CHILD_META_KEY, null);
        if (testEntityMeta == null) {
            EntityMeta entityMetaForPersistChild = objectMapper
                    .readValue(EntityServicePersistTest.class.getResourceAsStream("entity_meta_persist_test_child.json"), EntityMeta.class);
            entityMetaService.persistEntityMeta(entityMetaForPersistChild);
            assertThat(entityMetaForPersistChild.getId()).isNotNull();
            testEntityMeta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, null);
        }
    }
}