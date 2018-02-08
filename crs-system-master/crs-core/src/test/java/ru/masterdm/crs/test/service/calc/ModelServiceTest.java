package ru.masterdm.crs.test.service.calc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.masterdm.crs.dao.entity.meta.MetadataDao;
import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.entity.attribute.EntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.Where;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.form.FileFormat;
import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.domain.form.TemplateType;
import ru.masterdm.crs.exception.calc.ModelErrorCode;
import ru.masterdm.crs.exception.calc.ModelException;
import ru.masterdm.crs.service.CalcService;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.FormTemplateService;

/**
 * Tests model service.
 * @author Pavel Masalov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml",
                                   "classpath:META-INF/spring/crs-security-test.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ModelServiceTest {

    private boolean setupDone = false;
    private final Long version = 3L;

    @Autowired
    private CalcService calcService;
    @Autowired
    private EntityService entityService;
    @Autowired
    private EntityMetaService entityMetaService;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private MetadataDao metadataDao;
    @Autowired
    private FormTemplateService formTemplateService;

    private EntityMeta modelEntityMeta;
    private EntityMeta formTemplateEntityMeta;
    private EntityMeta entityMetaClassifier01;
    private EntityMeta entityMetaClassifier02;
    private EntityMeta entityMetaInputForm01;
    private EntityMeta entityMetaInputForm02;

    /**
     * Simple persists model test and then read it. No references.
     */
    @Test
    public void test01SimpleCreateAndRetrieveModel() {
        String key = "M01";
        createTestModelIfNotexists(key);

        Criteria criteria = new Criteria();
        Where where = criteria.getWhere();
        where.addItem(new WhereItem(modelEntityMeta.getKeyAttribute(), Operator.EQ, key));
        List<Model> models = calcService.getModels(criteria, null, null);
        assertThat(models).isNotNull();
        assertThat(models.size()).isEqualTo(1);
    }

    /**
     * Simple persists model test and then read it. No references.
     */
    @Test
    public void test02RetrieveDraftAndPublishedModel() {
        String key = "M02";
        createTestModelIfNotexists(key);

        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(modelEntityMeta.getKeyAttribute(), Operator.EQ, key));
        List<Model> models = calcService.getModels(criteria, null, null);
        assertThat(models).isNotNull();
        assertThat(models).hasSize(1);
        Model model = models.get(0);
        assertThat(model.isPublished()).isFalse();
        assertThat(model.getName().getDescriptionEn()).isEqualTo("draft test " + key);
        assertThat(model.getPeriodicity()).isEqualTo(Model.Periodicity.QUARTER);

        // publish
        model.setName(new MultilangDescription("опубликованный тест " + key, "published test " + key));
        model.setPeriodicity(Model.Periodicity.YEAR);
        calcService.publishModel(model);
        assertThat(model.getVersion()).isEqualTo(1);

        // publish is last actual record technically
        models = calcService.getModels(criteria, null, null);
        assertThat(models).isNotNull();
        assertThat(models).hasSize(1);
        model = models.get(0);
        assertThat(model.isPublished()).isTrue();
        assertThat(model.getName().getDescriptionEn()).isEqualTo("published test " + key);
        assertThat(model.getPeriodicity()).isEqualTo(Model.Periodicity.YEAR);

        // persists changed model should create draft ("hide" published)
        model.setName(new MultilangDescription("черновик тест " + key, "draft test " + key));
        calcService.persistModel(model);
        assertThat(model.getVersion()).isNull();
        assertThat(model.isPublished()).isFalse();

        // anyway published record are available
        models = calcService.getPublishedModels(criteria, null, null);
        assertThat(models).isNotNull();
        assertThat(models).hasSize(1);
        model = models.get(0);
        // and draft record are available to as it is technicaly last
        models = calcService.getDraftModels(criteria, null, null);
        assertThat(models).isNotNull();
        assertThat(models).hasSize(1);
        model = models.get(0);
        assertThat(model.isPublished()).isFalse();
        assertThat(model.getVersion()).isNull();
        assertThat(model.getName().getDescriptionEn()).isEqualTo("draft test " + key);

        // publish it again
        model.setName(new MultilangDescription("опубликованный тест " + key, "published test " + key));
        calcService.publishModel(model);
        assertThat(model.getVersion()).isEqualTo(2);

        int countClassifiers = (model.getClassifiers() != null ? model.getClassifiers().size() : 0);
        //check persist change model to draft
        model.getClassifiers().add(entityMetaClassifier01);
        calcService.persistModel(model);
        assertThat(model.getVersion()).isNull();
        assertThat(model.isPublished()).isFalse();
        Model storedModel = calcService.getPublishedModels(criteria, null, null).get(0);
        assertThat(storedModel.getClassifiers() != null ? storedModel.getClassifiers().size() : 0).isEqualTo(countClassifiers);

        // publish it again
        calcService.publishModel(model);
        assertThat(model.getVersion()).isEqualTo(version);

        // draft record are unavailable
        models = calcService.getDraftModels(criteria, null, null);
        assertThat(models).isNotNull();
        assertThat(models).hasSize(0);
    }

    /**
     * Simple persists model test and then read it. No references.
     */
    @Test
    public void test03ModelWithReferences() {
        String key = "M03";
        createTestModelIfNotexists(key);

        Criteria criteria = new Criteria();
        Where where = criteria.getWhere();
        where.addItem(new WhereItem(modelEntityMeta.getKeyAttribute(), Operator.EQ, key));
        List<Model> models = calcService.getModels(criteria, null, null);
        assertThat(models).isNotNull();
        assertThat(models.size()).isEqualTo(1);
        Model model = models.get(0);

        // add references and check
        FormTemplate formTemplate = createTestFormTemplate("FTM03");
        model.getClassifiers().add(entityMetaClassifier01);
        model.getInputForms().add(entityMetaInputForm01);
        model.setActuality(metadataDao.getSysTimestamp().plusDays(1));
        model.getFormTemplates().add(formTemplate);
        calcService.persistModel(model);

        models = calcService.getModels(criteria, null, null);
        assertThat(models).isNotNull();
        assertThat(models.size()).isEqualTo(1);
        model = models.get(0);
        assertThat(model.getClassifiers()).containsExactly(entityMetaClassifier01);
        assertThat(model.getInputForms()).containsExactly(entityMetaInputForm01);
        assertThat(model.getFormTemplates()).containsExactly(formTemplate);

        // remove references and check
        model.getClassifiers().clear();
        model.getInputForms().clear();
        model.getFormTemplates().clear();
        calcService.persistModel(model);
        models = calcService.getModels(criteria, null, null);
        assertThat(models).isNotNull();
        assertThat(models.size()).isEqualTo(1);
        model = models.get(0);
        assertThat(model.getClassifiers()).hasSize(0);
        assertThat(model.getInputForms()).hasSize(0);
        assertThat(model.getFormTemplates()).hasSize(0);

        // add reference again but now twice to both links
        FormTemplate formTemplate2 = createTestFormTemplate("FTM03-2");
        model.getInputForms().add(entityMetaInputForm01);
        model.getInputForms().add(entityMetaInputForm02);
        model.getClassifiers().add(entityMetaClassifier01);
        model.getClassifiers().add(entityMetaClassifier02);
        model.getFormTemplates().add(formTemplate);
        model.getFormTemplates().add(formTemplate2);
        calcService.persistModel(model);

        models = calcService.getModels(criteria, null, null);
        assertThat(models).isNotNull();
        assertThat(models.size()).isEqualTo(1);
        model = models.get(0);
        model.getClassifiers().remove(entityMetaClassifier01);
        model.getInputForms().remove(entityMetaInputForm01);
        model.getFormTemplates().remove(formTemplate);
        calcService.persistModel(model);

        models = calcService.getModels(criteria, null, null);
        assertThat(models).isNotNull();
        assertThat(models.size()).isEqualTo(1);
        model = models.get(0);
        assertThat(model.getClassifiers()).containsExactly(entityMetaClassifier02);
        assertThat(model.getInputForms()).containsExactly(entityMetaInputForm02);
        assertThat(model.getFormTemplates()).containsExactly(formTemplate2);
    }

    /**
     * Model removing test.
     */
    @Test
    public void test04CreatePublishRemove() {
        String key1 = "M04-1";
        createTestModelIfNotexists(key1);
        String key2 = "M04-2";
        createTestModelIfNotexists(key2);

        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(modelEntityMeta.getKeyAttribute(), Operator.IN, key1, key2));
        List<Model> models = calcService.getModels(criteria, null, metadataDao.getSysTimestamp());
        assertThat(models).hasSize(2);

        Model removeOnly = models.get(0);
        Model publishThenRemove = models.get(1);
        calcService.removeModel(removeOnly);

        calcService.publishModel(publishThenRemove);
        assertThatThrownBy(() -> calcService.removeModel(publishThenRemove))
                .isInstanceOf(ModelException.class)
                .hasFieldOrPropertyWithValue("errorCode", ModelErrorCode.REMOVE_PUBLISHED);

        models = calcService.getModels(criteria, null, metadataDao.getSysTimestamp());
        assertThat(models).hasSize(1);

        models = calcService.getDraftModels(criteria, null, metadataDao.getSysTimestamp());
        assertThat(models).hasSize(0);
        models = calcService.getPublishedModels(criteria, null, metadataDao.getSysTimestamp());
        assertThat(models).hasSize(1);
    }

    /**
     * Model input form link satellite read-write test.
     */
    @Test
    public void test05ModelInputFormLinkSatellite() {
        final String inputFormKey = "MOTEST5";
        final BigDecimal periodCount = new BigDecimal("123");
        final String inputFormAttrKey = "someKey";

        Model model = (Model) entityService.newEmptyEntity(Model.METADATA_KEY);
        model.setName(new MultilangDescription("testRu", "testEn"));
        LocalDateTime currentTime = entityMetaService.getSysTimestamp();
        model.setActuality(currentTime);
        model.getInputForms().add(entityMetaInputForm01);

        LinkedEntityAttribute<EntityMeta> modelInputFormsAttribute =
                (LinkedEntityAttribute<EntityMeta>) model.getAttribute(Model.ModelAttributeMeta.INPUT_FORMS.getKey());
        assertThat(modelInputFormsAttribute.getEntityList().size()).isEqualTo(1);
        assertThat(modelInputFormsAttribute.getEntityAttributeList()).isNotEmpty();
        assertThat(modelInputFormsAttribute.getEntityAttributeList().size()).isEqualTo(1);
        EntityAttribute<EntityMeta> entityAttribute = modelInputFormsAttribute.getEntityAttributeList().get(0);

        entityAttribute.getSatellite().setAttributeValue(Model.ModelInputFormAttributeMeta.PERIOD_COUNT.getKey(), periodCount);
        entityAttribute.getSatellite().setAttributeValue(Model.ModelInputFormAttributeMeta.INPUT_FORM_DATE_ATTR_KEY.getKey(), inputFormAttrKey);
        calcService.persistModel(model);

        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(modelEntityMeta.getKeyAttribute(), Operator.EQ, model.getKey()));
        List<Model> models = calcService.getModels(criteria, null, null);
        assertThat(models.size()).isEqualTo(1);
        Model model2 = models.get(0);
        assertThat(model2.getInputForms()).containsExactly(entityMetaInputForm01);

        LinkedEntityAttribute<EntityMeta> modelInputFormsAttribute2 =
                (LinkedEntityAttribute<EntityMeta>) model2.getAttribute(Model.ModelAttributeMeta.INPUT_FORMS.getKey());
        assertThat(modelInputFormsAttribute2.getEntityList().size()).isEqualTo(1);
        assertThat(modelInputFormsAttribute2.getEntityAttributeList()).isNotEmpty();
        EntityAttribute<EntityMeta> entityAttribute2 = modelInputFormsAttribute2.getEntityAttributeList().get(0);
        assertThat(entityAttribute2.isSatelliteDefined()).isTrue();
        assertThat(entityAttribute2.getSatellite().getAttributeValue(Model.ModelInputFormAttributeMeta.PERIOD_COUNT.getKey())).isEqualTo(periodCount);
        assertThat(entityAttribute2.getSatellite().getAttributeValue(Model.ModelInputFormAttributeMeta.INPUT_FORM_DATE_ATTR_KEY.getKey()))
                .isEqualTo(inputFormAttrKey);
    }

    /**
     * Create test data at database.
     * @param key test object key
     * @return model object
     */
    private Model createTestModelIfNotexists(String key) {
        Model model;
        if (entityService.getEntityIdByKey(modelEntityMeta, key) == null) {
            model = (Model) entityService.newEmptyEntity(Model.METADATA_KEY);
            model.setKey(key);
            model.setName(new MultilangDescription("черновик тест " + key, "draft test " + key));
            model.setPublished(false);
            model.setActuality(metadataDao.getSysTimestamp());

            calcService.persistModel(model);
        } else {
            Criteria criteria = new Criteria();
            criteria.getWhere().addItem(new WhereItem(modelEntityMeta.getKeyAttribute(), Operator.EQ, key));
            model = calcService.getModels(criteria, null, null).get(0);
        }
        return model;
    }

    /**
     * Create test data for form template.
     * @param key form template key
     * @return form gtemplate
     */
    private FormTemplate createTestFormTemplate(String key) {
        FormTemplate formTemplate;
        if (entityService.getEntityIdByKey(formTemplateEntityMeta, key) == null) {
            formTemplate = (FormTemplate) entityService.newEmptyEntity(FormTemplate.METADATA_KEY);
            formTemplate.setKey(key);
            formTemplate.getName().setDescriptionEn("form-template " + key);
            formTemplate.setFormat(FileFormat.EXCEL);
            formTemplate.setType(TemplateType.FORM);
            formTemplateService.persistFormTemplate(formTemplate);
        } else {
            Criteria criteria = new Criteria();
            criteria.getWhere().addItem(new WhereItem(formTemplateEntityMeta.getKeyAttribute(), Operator.EQ, key));
            formTemplate = formTemplateService.getFormTemplates(criteria, null, null).get(0);
        }
        return formTemplate;
    }

    /**
     * Create entity metadata.
     * @param entityMeta entity object
     * @return created/persisted metadata
     */
    private EntityMeta createEntityMeta(EntityMeta entityMeta) {
        EntityMeta m = entityMetaService.getEntityMetaByKeyNoCache(entityMeta.getKey(), null);
        if (m == null) {
            entityMetaService.persistEntityMeta(entityMeta);
            return m;
        }
        return entityMetaService.getEntityMetaByKey(entityMeta.getKey(), null);
    }

    /**
     * Prepares initial test data.
     * @throws Exception exception
     */
    @Before
    public void setup() throws Exception {
        if (!setupDone) {
            modelEntityMeta = entityMetaService.getEntityMetaByKey(Model.METADATA_KEY, null);
            formTemplateEntityMeta = entityMetaService.getEntityMetaByKey(FormTemplate.METADATA_KEY, null);

            ObjectMapper objectMapper = new ObjectMapper();
            entityMetaClassifier01 = objectMapper.readValue(CalculationServiceTest.class.getResourceAsStream("Classifier01.json"),
                                                            EntityMeta.class);
            entityMetaClassifier01 = createEntityMeta(entityMetaClassifier01);
            entityMetaClassifier02 = objectMapper.readValue(CalculationServiceTest.class.getResourceAsStream("Classifier02.json"),
                                                            EntityMeta.class);
            entityMetaClassifier02 = createEntityMeta(entityMetaClassifier02);
            entityMetaInputForm01 = objectMapper.readValue(CalculationServiceTest.class.getResourceAsStream("input-form01.json"),
                                                           EntityMeta.class);
            entityMetaInputForm01 = createEntityMeta(entityMetaInputForm01);
            entityMetaInputForm02 = objectMapper.readValue(CalculationServiceTest.class.getResourceAsStream("input-form02.json"),
                                                           EntityMeta.class);
            entityMetaInputForm02 = createEntityMeta(entityMetaInputForm02);
            setupDone = true;
        }
    }
}
