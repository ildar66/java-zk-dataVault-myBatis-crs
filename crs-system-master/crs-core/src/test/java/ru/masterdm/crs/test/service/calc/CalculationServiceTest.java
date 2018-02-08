package ru.masterdm.crs.test.service.calc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.masterdm.crs.SecurityContextBeanPostProcessor.ADMINWF_LOGIN;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.masterdm.crs.dao.calc.CalcDao;
import ru.masterdm.crs.dao.entity.meta.MetadataDao;
import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.ClassifierAttributeMeta;
import ru.masterdm.crs.domain.calc.InputFormAttributeMeta;
import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.entity.BusinessAction;
import ru.masterdm.crs.domain.entity.Department;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.Role;
import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.attribute.EntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.MultilangAttribute;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.CalculationProfileAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientGroupAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.domain.form.FileFormat;
import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.domain.form.TemplateType;
import ru.masterdm.crs.domain.form.mapping.Mapper;
import ru.masterdm.crs.domain.form.mapping.MappingObject;
import ru.masterdm.crs.exception.calc.CalculationErrorCode;
import ru.masterdm.crs.exception.calc.CalculationException;
import ru.masterdm.crs.service.CalcService;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.FormTemplateService;
import ru.masterdm.crs.service.SecurityService;
import ru.masterdm.crs.service.UserRoleService;
import ru.masterdm.crs.util.CollectorUtils;

/**
 * Calculation tests.
 * @author Pavel Masalov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml",
                                   "classpath:META-INF/spring/crs-security-test.xml",
                                   "classpath:META-INF/spring/crs-security-config.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CalculationServiceTest {

    private static final String DEFAULT_CALC_PROFILE = "RATED";
    public static final String DEPARTMENT_ONE_KEY = "1";

    private boolean setupDone = false;

    @Autowired
    private CalcService calcService;
    @Autowired
    private EntityService entityService;
    @Autowired
    private EntityMetaService entityMetaService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private FormTemplateService formTemplateService;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private MetadataDao metadataDao;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private CalcDao calcDao;
    @Autowired
    private UserRoleService userRoleService;

    private EntityMeta calculationEntityMeta;
    private EntityMeta modelEntityMeta;
    private EntityMeta entityMetaClassifier01;
    private EntityMeta entityMetaClassifier02;
    private EntityMeta entityMetaInputForm01;
    private EntityMeta entityMetaInputForm02;
    private String errorCode;

    /**
     * Test create and reload of calculations.
     */
    @Test
    public void test01CalculationPersistsAndGet() {
        String key = "C01";
        createTestCalculationIfNotexists(key, true, false);
        String key2 = "C02";
        createTestCalculationIfNotexists(key2, false, false);

        // no any published calculations and 2 draft
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(calculationEntityMeta.getKeyAttribute(), Operator.IN, key, key2));
        List<Calculation> check = calcService.getPublishedCalculations(criteria, null, null);
        assertThat(check).hasSize(0);
        check = calcService.getDraftCalculations(criteria, null, null);
        assertThat(check).hasSize(2);

        check = calcService.getCalculations(criteria, null, null);

        Calculation c01 = check.stream().filter(c -> c.getKey().equals(key)).collect(CollectorUtils.singletonCollector());
        assertThat(c01).isNotNull();
        Calculation c02 = check.stream().filter(c -> c.getKey().equals(key2)).collect(CollectorUtils.singletonCollector());
        assertThat(c02).isNotNull();

        // publish first
        assertThat(c01.getModel()).isNull();
        c01.setModel(createTestModelIfNotexists("model01"));
        assertThat(c01.getClient()).isNull();
        c01.setClient(createTestClient("client01", createTestDepartment(DEPARTMENT_ONE_KEY)));
        calcService.publishCalculation(c01);

        // only second is draft
        check = calcService.getDraftCalculations(criteria, null, null);
        assertThat(check).hasSize(1);
        c02 = check.stream().filter(c -> c.getKey().equals(key2)).collect(CollectorUtils.singletonCollector());
        assertThat(c02).isNotNull();

        // first are published and second are drafts
        check = calcService.getDraftCalculations(criteria, null, null);
        assertThat(check).hasSize(1);
        //c01 = check.stream().filter(c -> c.getKey().equals(key)).collect(CollectorUtils.singletonCollector());
        //assertThat(c01).isNotNull();
        c02 = check.stream().filter(c -> c.getKey().equals(key2)).collect(CollectorUtils.singletonCollector());
        assertThat(c02).isNotNull();

        // first are published too
        check = calcService.getPublishedCalculations(criteria, null, null);
        assertThat(check).hasSize(1);
        c01 = check.stream().filter(c -> c.getKey().equals(key)).collect(CollectorUtils.singletonCollector());
        assertThat(c01).isNotNull();
    }

    /**
     * Test create and remove calculations.
     */
    @Test
    public void test02CalculationPersistsAndRemove() {
        String key = "CR01";
        Calculation calculation = createTestCalculationIfNotexists(key, false, true);

        Long id = calculation.getId();
        calcService.removeCalculation(calculation); // has new satellite
        assertThat(calculation.getId()).isNotEqualTo(id);

        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(calculationEntityMeta.getKeyAttribute(), Operator.EQ, key));
        List<Calculation> check = calcService.getCalculations(criteria, null, null);
        assertThat(check).hasSize(0);
    }

    /**
     * Test create, publish and remove calculations.
     */
    @Test
    public void test0201CalculationPersistsPublishPersistRemove() {
        String key = "CR02";
        Calculation calculation = createTestCalculationIfNotexists(key, true, true);

        Long id = calculation.getId();

        assertThat(calculation.getModel()).isNull();
        calculation.setModel(createTestModelIfNotexists("model02"));
        assertThat(calculation.getClient()).isNull();
        calculation.setClient(createTestClient("client02", createTestDepartment(DEPARTMENT_ONE_KEY)));
        calcService.publishCalculation(calculation);
        assertThat(calculation.getId()).isNotEqualTo(id); // has new satellite

        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(calculationEntityMeta.getKeyAttribute(), Operator.EQ, key));
        List<Calculation> check = calcService.getCalculations(criteria, null, null);
        assertThat(check).hasSize(1);
        calculation = check.get(0);
        assertThat(calculation.isPublished()).isTrue();
        assertThat(calcDao.isCalculationPublished(calculation.getId())).isTrue();

        calculation.setName(calculation.getName() + " changed");
        Calculation finalCalculation = calculation;
        assertThatThrownBy(() -> calcService.persistCalculation(finalCalculation))
                .isInstanceOf(CalculationException.class).hasFieldOrPropertyWithValue("errorCode", CalculationErrorCode.PERSIST_PUBLISHED);

        assertThatThrownBy(() -> calcService.removeCalculation(finalCalculation))
                .isInstanceOf(CalculationException.class).hasFieldOrPropertyWithValue("errorCode", CalculationErrorCode.REMOVE_PUBLISHED);
    }

    /**
     * Test create, publish and remove calculations.
     */
    @Test
    public void test0202CalculationPersistsPublishCalculatePersists() {
        String key = "CR03";
        Calculation calculation = createTestCalculationIfNotexists(key, false, true);
        assertThat(calculation.getModel()).isNull();
        calculation.setModel(createTestModelIfNotexists("model0202"));
        assertThat(calculation.getClient()).isNull();
        calculation.setClient(createTestClient("client0202", createTestDepartment(DEPARTMENT_ONE_KEY)));

        Calculation finalCalculation = calculation;
        assertThatThrownBy(() -> calcService.publishCalculation(finalCalculation))
                .isInstanceOf(CalculationException.class).hasFieldOrPropertyWithValue("errorCode", CalculationErrorCode.PUBLISH_NOT_CALCULATED);
    }

    /**
     * Get calculation classifier.
     */
    @Test
    public void test03ClassifierValue() {
        final int classValSize = 4;
        String key = "C03";
        Calculation calculation = createTestCalculationIfNotexists(key, false, true);

        Model model = (Model) entityService.newEmptyEntity(Model.METADATA_KEY);
        model.getClassifiers().add(entityMetaClassifier01);
        model.getClassifiers().add(entityMetaClassifier02);
        calculation.setModel(model);
        List<Entity> classifierValues = calcService.getClassifierValues(calculation, null);

        Entity classifierValue0101, classifierValue0102;
        classifierValue0101 = createEntityValue(entityMetaClassifier01, "CLSSVAL0101", null);
        classifierValue0102 = createEntityValue(entityMetaClassifier01, "CLSSVAL0102", calculation);

        Entity classifierValue0201, classifierValue0202;
        classifierValue0201 = createEntityValue(entityMetaClassifier02, "CLSSVAL0201", calculation);
        classifierValue0202 = createEntityValue(entityMetaClassifier02, "CLSSVAL0202", null);

        calcService.persistsClassifierValues(calculation,
                                             Arrays.asList(classifierValue0101, classifierValue0102, classifierValue0201, classifierValue0202));

        classifierValues = calcService.getClassifierValues(calculation, null);
        assertThat(classifierValues).hasSize(classValSize);
        assertThat(classifierValues).containsExactlyInAnyOrder(classifierValue0101, classifierValue0102, classifierValue0201, classifierValue0202);
    }

    /**
     * Get calculation classifier.
     */
    @Test
    public void test04InputFormValue() {
        final int inputFormValSize = 2;
        String key = "C03";
        Calculation calculation = createTestCalculationIfNotexists(key, false, true);

        Model model = (Model) entityService.newEmptyEntity(Model.METADATA_KEY);
        model.getInputForms().add(entityMetaInputForm01);
        model.getInputForms().add(entityMetaInputForm02);
        calculation.setModel(model);
        entityMetaService.persistEntityMeta(entityMetaInputForm01);
        entityMetaService.persistEntityMeta(entityMetaInputForm02);

        Entity inputFormValue0101, inputFormValue0102;
        inputFormValue0101 = createEntityValue(entityMetaInputForm01, "INPUTFORM0101", null);
        inputFormValue0102 = createEntityValue(entityMetaInputForm01, "INPUTFORM0102", calculation);

        Entity inputFormValue0201, inputFormValue0202;
        inputFormValue0201 = createEntityValue(entityMetaInputForm02, "INPUTFORM0201", calculation);
        inputFormValue0202 = createEntityValue(entityMetaInputForm02, "INPUTFORM0202", null);

        calcService.persistsInputFormValues(calculation, entityMetaInputForm01, Arrays.asList(inputFormValue0101, inputFormValue0102));
        calcService.persistsInputFormValues(calculation, entityMetaInputForm02, Arrays.asList(inputFormValue0201, inputFormValue0202));

        List<Entity> inputFormValues1 = calcService.getInputFormValues(calculation, entityMetaInputForm01, null, null, null);
        List<Entity> inputFormValues2 = calcService.getInputFormValues(calculation, entityMetaInputForm02, null, null, null);
        assertThat(inputFormValues1).hasSize(inputFormValSize);
        assertThat(inputFormValues1).containsExactly(inputFormValue0101, inputFormValue0102);
        assertThat(inputFormValues2).hasSize(inputFormValSize);
        assertThat(inputFormValues2).containsExactly(inputFormValue0201, inputFormValue0202);
    }

    /**
     * Test for reset 'calculated' flag into false.
     */
    @Test
    public void test05ResetCalculatedFlag() {
        String key = "C05";
        Calculation calculation = createTestCalculationIfNotexists(key, true, true);
        assertThat(calculation.isCalculated()).isTrue();

        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(calculationEntityMeta.getKeyAttribute(), Operator.EQ, key));

        /* actuality */
        LocalDateTime now = entityMetaService.getSysTimestamp();
        calculation.setActuality(now.toLocalDate().plusDays(1));
        calcService.persistCalculation(calculation);
        Calculation check = calcService.getCalculations(criteria, null, null).get(0);
        assertThat(check.isCalculated()).isFalse();

        calculation = check;
        calculation.setCalculated(true);
        calcService.persistCalculation(calculation);
        check = calcService.getCalculations(criteria, null, null).get(0);
        assertThat(check.isCalculated()).isTrue();

        /* create model */
        Model model = (Model) entityService.newEmptyEntity(Model.METADATA_KEY);
        model.setKey("C05_MODEL_KEY");
        MultilangDescription desc = new MultilangDescription();
        desc.setDescriptionEn("MODEL_DESC_EN");
        desc.setDescriptionRu("MODEL_DESC_RU");
        model.setName(desc);
        model.setActuality(entityMetaService.getSysTimestamp());
        calcService.persistModel(model);

        /* model key */
        calculation.setModel(model);

        calcService.persistCalculation(calculation);
        check = calcService.getCalculations(criteria, null, null).get(0);
        assertThat(check.isCalculated()).isFalse();

        /* model version */
        calculation.setCalculated(true);
        calcService.persistCalculation(calculation);
        check = calcService.getCalculations(criteria, null, null).get(0);
        assertThat(check.isCalculated()).isTrue();

        // set client
        calcService.persistModel(model);
        calculation.setCalculated(true);
        calcService.persistCalculation(calculation);
        check = calcService.getCalculations(criteria, null, null).get(0);
        assertThat(check.isCalculated()).isTrue();
        Entity client = createTestClient("CALC_CL05");
        calculation.setClient(client);
        calcService.persistCalculation(calculation);
        check = calcService.getCalculations(criteria, null, null).get(0);
        assertThat(check.isCalculated()).isFalse();

        // set client group
        calculation.setCalculated(true);
        calcService.persistCalculation(calculation);
        check = calcService.getCalculations(criteria, null, null).get(0);
        assertThat(check.isCalculated()).isTrue();
        Entity clientGroup = createTestClientGroup("CALC_CL05");
        calculation.setClientGroup(clientGroup);
        calcService.persistCalculation(calculation);
        check = calcService.getCalculations(criteria, null, null).get(0);
        assertThat(check.isCalculated()).isFalse();
    }

    /**
     * Test assenting model version for calc-model link.
     */
    @Test
    public void test06CalcAndModelVersion() {
        String calcKey = "C06";
        String modelKey = "M06";
        Calculation calculation = createTestCalculationIfNotexists(calcKey, false, true);
        Model model = createTestModelIfNotexists(modelKey);

        // VERSION 1
        model.setVersion(1L);
        calcService.persistModel(model);
        calculation.setModel(model);
        calcService.persistCalculation(calculation);
        Criteria criteria = new Criteria();
        criteria.setHubIds(Collections.singleton(calculation.getHubId()));
        calculation = calcService.getCalculations(criteria, null, null).get(0);
        assertThat(calculation.getModel().getVersion()).isEqualTo(1);
        assertThat(calculation.getModel()).isEqualTo(model);

        // VERSION 2
        model.setVersion(2L);
        calcService.persistModel(model);
        calculation.setModel(model);
        calcService.persistCalculation(calculation);
        criteria = new Criteria();
        criteria.setHubIds(Collections.singleton(calculation.getHubId()));
        calculation = calcService.getCalculations(criteria, null, null).get(0);
        assertThat(calculation.getModel().getVersion()).isEqualTo(2);
        assertThat(calculation.getModel()).isEqualTo(model);
        calculation.setName(calculation.getName() + " (edited)");
        calcService.persistCalculation(calculation);

        // other MODEL, VERSION 1
        String modelKey2 = "M06_2";
        Model model2 = createTestModelIfNotexists(modelKey2);
        model2.setVersion(1L);
        calcService.persistModel(model2);
        calculation.setModel(model2);
        calcService.persistCalculation(calculation);

        criteria = new Criteria();
        criteria.setHubIds(Collections.singleton(calculation.getHubId()));
        calculation = calcService.getCalculations(criteria, null, null).get(0);
        assertThat(calculation.getModel().getVersion()).isEqualTo(1);
        assertThat(calculation.getModel()).isEqualTo(model2);
        assertThat(calculation.getModel()).isNotEqualTo(model);
    }

    /**
     * Read all calculation.
     * Just to get performance imagionation. Ignore for auto-tests
     */
    @Test
    @Ignore
    public void test07ReadAllCalculations() {
        final int sz = 7;
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(
                new WhereItem(calculationEntityMeta.getKeyAttribute(), Operator.IN, "C01", "C02", "C03", "C05", "C06", "CR02", "CR03"));

        List<Calculation> calculationList = calcService.getCalculations(criteria, null, null);
        assertThat(calculationList).hasSize(sz);
    }

    /**
     * Read calculation hierarchy.
     */
    @Test
    public void test08ReadCalculationHierarchy() {
        LocalDateTime currentTime = entityMetaService.getSysTimestamp();
        Calculation c1 = (Calculation) entityService.newEmptyEntity(Calculation.METADATA_KEY);
        c1.setKey("CALC0801");
        c1.setName("testName1");
        c1.setActuality(currentTime.toLocalDate());
        c1.setDataActuality(currentTime);
        c1.setAuthor(securityService.getCurrentUser());
        entityService.persistEntity(c1);

        Calculation c2 = (Calculation) entityService.newEmptyEntity(Calculation.METADATA_KEY);
        c2.setKey("CALC0802");
        c2.setName("testName2");
        c2.setActuality(currentTime.toLocalDate());
        c2.setDataActuality(currentTime);
        c2.setAuthor(securityService.getCurrentUser());
        entityService.persistEntity(c2);

        Calculation c3 = (Calculation) entityService.newEmptyEntity(Calculation.METADATA_KEY);
        c3.setKey("CALC0803");
        c3.setName("testName3");
        c3.setActuality(currentTime.toLocalDate());
        c3.setDataActuality(currentTime);
        c3.setAuthor(securityService.getCurrentUser());
        entityService.persistEntity(c3);

        c1.getChildrenReferenceAttribute().getEntityList().add(c2);
        c1.getChildrenReferenceAttribute().getEntityList().add(c3);
        entityService.persistEntity(c1);

        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(calculationEntityMeta.getKeyAttribute(), Operator.IN, c1.getKey()));
        c1 = calcService.getCalculations(criteria, null, null).get(0);
        entityService.loadEntityChildren(c1, null);
        assertThat(c1.getChildrenReferenceAttribute().getEntityList()).containsExactlyInAnyOrder(c2, c3);
    }

    /**
     * Test calculation profiles with calculation.
     */
    @Test
    public void test09CalculationProfiles() {
        EntityMeta profileEntity = entityMetaService.getEntityMetaByKey(CalculationProfileAttributeMeta.METADATA_KEY, null);
        List<Entity> profiles = (List<Entity>) entityService.getEntities(profileEntity, null, RowRange.newAsPageAndSize(0, 2), null);

        String calcKey = "C09";
        Calculation calculation = createTestCalculationIfNotexists(calcKey, false, false);
        assertThat(calculation).isNotNull();
        calculation.setProfiles(profiles);
        calcService.persistCalculation(calculation);

        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(calculationEntityMeta.getKeyAttribute(), Operator.EQ, calcKey));
        List<Calculation> calculations = calcService.getCalculations(criteria, null, null);
        assertThat(calculations).hasSize(1);
        calculation = calculations.get(0);
        assertThat(calculation.getProfiles()).containsExactlyInAnyOrder(profiles.toArray(new Entity[] {}));
    }

    /**
     * Test calculation copy.
     */
    @Test
    public void test10CalculationCopy() {
        LocalDateTime now = LocalDateTime.now();
        final Long modelVersion = 2L;
        User userNotRight = createTestUser("CALCU1001", null, null);

        EntityMeta profileEntity = entityMetaService.getEntityMetaByKey(CalculationProfileAttributeMeta.METADATA_KEY, null);
        List<Entity> allProfiles = (List<Entity>) entityService.getEntities(profileEntity, null, null, null);
        Calculation origCalculation1 = getCalculation(allProfiles);

        Model model = (Model) entityService.newEmptyEntity(Model.METADATA_KEY);
        model.getClassifiers().add(entityMetaClassifier01);
        model.getClassifiers().add(entityMetaClassifier02);
        model.getInputForms().add(entityMetaInputForm01);
        model.getInputForms().add(entityMetaInputForm02);
        model.setName(new MultilangDescription("testRu", "testEn"));
        model.setActuality(now);
        model.setVersion(modelVersion);
        calcService.persistModel(model);

        Entity clientGroup = createClientGroupWithClients("CLGROUP11");

        Calculation copiedCalculation1 = (Calculation) entityService.newEmptyEntity(Calculation.METADATA_KEY);
        copiedCalculation1.setModel(model);
        copiedCalculation1.setName("copy test");
        copiedCalculation1.setActuality(LocalDate.from(now));
        copiedCalculation1.setDataActuality(now);
        copiedCalculation1.getParentReferenceAttribute().add(origCalculation1);
        copiedCalculation1.setProfiles(allProfiles);
        copiedCalculation1.setClientGroup(clientGroup);
        securityService.defineSecurityContext(userNotRight.getLogin());
        assertThat(securityService.isPermitted(userNotRight, calculationEntityMeta, BusinessAction.Action.CREATE_COPY)).isFalse();
        assertThatThrownBy(() -> calcService.copyCalculation(copiedCalculation1)).isInstanceOf(AccessDeniedException.class);
        securityService.defineSecurityContext(ADMINWF_LOGIN);
        calcService.copyCalculation(copiedCalculation1);
        checkCopiedCalculationClassifiers(origCalculation1, copiedCalculation1, model);
        checkCopiedCalculationInputForms(origCalculation1, copiedCalculation1, model);
        checkCopiedCalculationClientSatelliteLink(origCalculation1, copiedCalculation1);
        LinkedEntityAttribute modelAttribute1 = (LinkedEntityAttribute) copiedCalculation1
                .getAttribute(Calculation.CalculationAttributeMeta.MODEL.getKey());
        assertThat(((EntityAttribute) modelAttribute1.getEntityAttributeList().get(0)).getSatellite().getAttributeValue(
                Calculation.CalculationModelAttributeMeta.VERSION.getKey())).isEqualTo(new BigDecimal(modelVersion));

        Criteria profileCriteria = new Criteria();
        EntityMeta profileMeta = entityMetaService.getEntityMetaByKey(CalculationProfileAttributeMeta.METADATA_KEY, null);
        profileCriteria.getWhere().addItem(new WhereItem(profileMeta.getKeyAttribute(), Operator.EQ, DEFAULT_CALC_PROFILE));
        List<Entity> ratedProfileList = (List<Entity>) entityService.getEntities(profileEntity, profileCriteria, null, null);
        Calculation origCalculation2 = getCalculation(ratedProfileList);

        Calculation copiedCalculation2 = (Calculation) entityService.newEmptyEntity(Calculation.METADATA_KEY);
        copiedCalculation2.setModel(model);
        copiedCalculation2.setName("copy test");
        copiedCalculation2.setActuality(LocalDate.from(now));
        copiedCalculation2.setDataActuality(now);
        copiedCalculation2.getParentReferenceAttribute().add(origCalculation2);
        copiedCalculation2.setProfiles(allProfiles);
        copiedCalculation2.setClientGroup(clientGroup);
        calcService.copyCalculation(copiedCalculation2);
        checkCopiedCalculationClassifiers(origCalculation2, copiedCalculation2, model);
        checkCopiedCalculationInputForms(origCalculation2, copiedCalculation2, model);
        checkCopiedCalculationClientSatelliteLink(origCalculation2, copiedCalculation2);
        LinkedEntityAttribute modelAttribute2 = (LinkedEntityAttribute) copiedCalculation1
                .getAttribute(Calculation.CalculationAttributeMeta.MODEL.getKey());
        assertThat(((EntityAttribute) modelAttribute2.getEntityAttributeList().get(0)).getSatellite().getAttributeValue(
                Calculation.CalculationModelAttributeMeta.VERSION.getKey())).isEqualTo(new BigDecimal(modelVersion));
    }

    /**
     * Calculation client link satellite read-write test.
     */
    @Test
    public void test11CalculationClientLinkSatellite() {
        final Boolean participant = true;
        final Boolean excluded = true;
        final String status = Calculation.CalculationClientStatus.MOTHERHOOD.name();
        final String comment = "comment text";

        LocalDateTime currentTime = entityMetaService.getSysTimestamp();
        Calculation calculation = (Calculation) entityService.newEmptyEntity(Calculation.METADATA_KEY);
        calculation.setName("testName");
        calculation.setActuality(currentTime.toLocalDate());
        calculation.setDataActuality(currentTime);
        Entity client = createTestClient("CALC_CL11");
        calculation.setClient(client);
        calculation.setAuthor(securityService.getCurrentUser());

        LinkedEntityAttribute<EntityMeta> calculationClientAttribute =
                (LinkedEntityAttribute<EntityMeta>) calculation.getAttribute(Calculation.CalculationAttributeMeta.CLIENT.getKey());
        assertThat(calculationClientAttribute.getEntityList().size()).isEqualTo(1);
        assertThat(calculationClientAttribute.getEntityAttributeList()).isNotEmpty();
        assertThat(calculationClientAttribute.getEntityAttributeList().size()).isEqualTo(1);
        EntityAttribute<EntityMeta> entityAttribute = calculationClientAttribute.getEntityAttributeList().get(0);

        entityAttribute.getSatellite().setAttributeValue(Calculation.CalculationClientAttributeMeta.PARTICIPANT.getKey(), participant);
        entityAttribute.getSatellite().setAttributeValue(Calculation.CalculationClientAttributeMeta.EXCLUDED.getKey(), excluded);
        entityAttribute.getSatellite().setAttributeValue(Calculation.CalculationClientAttributeMeta.STATUS.getKey(), status);
        entityAttribute.getSatellite().setAttributeValue(Calculation.CalculationClientAttributeMeta.COMMENT.getKey(), comment);
        calcService.persistCalculation(calculation);

        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(calculationEntityMeta.getKeyAttribute(), Operator.EQ, calculation.getKey()));
        List<Calculation> calculations = calcService.getCalculations(criteria, null, null);
        assertThat(calculations.size()).isEqualTo(1);
        Calculation calculation2 = calculations.get(0);
        assertThat(calculation2.getClient()).isNotNull();

        LinkedEntityAttribute<EntityMeta> calculationClientAttribute2 =
                (LinkedEntityAttribute<EntityMeta>) calculation2.getAttribute(Calculation.CalculationAttributeMeta.CLIENT.getKey());
        assertThat(calculationClientAttribute2.getEntityList().size()).isEqualTo(1);
        assertThat(calculationClientAttribute2.getEntityAttributeList()).isNotEmpty();
        EntityAttribute<EntityMeta> entityAttribute2 = calculationClientAttribute2.getEntityAttributeList().get(0);
        assertThat(entityAttribute2.isSatelliteDefined()).isTrue();
        assertThat(entityAttribute2.getSatellite()
                                   .getAttributeValue(Calculation.CalculationClientAttributeMeta.PARTICIPANT.getKey())).isEqualTo(participant);
        assertThat(entityAttribute2.getSatellite()
                                   .getAttributeValue(Calculation.CalculationClientAttributeMeta.EXCLUDED.getKey())).isEqualTo(excluded);
        assertThat(entityAttribute2.getSatellite()
                                   .getAttributeValue(Calculation.CalculationClientAttributeMeta.STATUS.getKey())).isEqualTo(status);
        assertThat(entityAttribute2.getSatellite()
                                   .getAttributeValue(Calculation.CalculationClientAttributeMeta.COMMENT.getKey())).isEqualTo(comment);
    }

    /**
     * Fills calculation clients on calculation createion if client group is not empty. Dont change calculation clients on calculation save.
     */
    @Test
    public void test12CalculationPersistFillGroupClients() {
        final String clientAttrKey = Calculation.CalculationAttributeMeta.CLIENT.getKey();
        Calculation calculation = (Calculation) entityService.newEmptyEntity(Calculation.METADATA_KEY);
        calculation.setName("draft test c12");
        LocalDateTime now = LocalDateTime.now();
        calculation.setActuality(now.toLocalDate());
        calculation.setDataActuality(now);
        calculation.setAuthor(securityService.getCurrentUser());
        calculation.setCalculated(false);
        Entity clientGroup1 = createClientGroupWithClients("CLGROUP12_1");
        calculation.setClientGroup(clientGroup1);
        assertThat(((LinkedEntityAttribute<EntityMeta>) calculation.getAttribute(clientAttrKey)).getEntityList()).isEmpty();
        calcService.persistCalculation(calculation);
        List clients1 = ((LinkedEntityAttribute<EntityMeta>) calculation.getAttribute(clientAttrKey)).getEntityList();
        assertThat(clients1).isNotEmpty();
        calcService.persistCalculation(calculation);
        List clients2 = ((LinkedEntityAttribute<EntityMeta>) calculation.getAttribute(clientAttrKey)).getEntityList();
        assertThat(clients2).isNotEmpty();
        assertThat(clients2.toArray(new Object[clients2.size()])).containsExactlyInAnyOrder(clients1.toArray(new Object[clients1.size()]));
        ((LinkedEntityAttribute<EntityMeta>) calculation.getAttribute(clientAttrKey)).getEntityList().clear();
        calcService.persistCalculation(calculation);
        assertThat(((LinkedEntityAttribute<EntityMeta>) calculation.getAttribute(clientAttrKey)).getEntityList()).isEmpty();

        calculation.setClientGroup(createClientGroupWithClients("CLGROUP12_2"));
        calcService.persistCalculation(calculation);
        List clients3 = ((LinkedEntityAttribute<EntityMeta>) calculation.getAttribute(clientAttrKey)).getEntityList();
        assertThat(clients3).isNotEmpty();
    }

    /**
     * Checks copied calculation client satellite link.
     * @param origCalculation original calculation
     * @param copiedCalculation copied calculation
     */
    private void checkCopiedCalculationClientSatelliteLink(Calculation origCalculation, Calculation copiedCalculation) {
        final String participantKey = Calculation.CalculationClientAttributeMeta.PARTICIPANT.getKey();
        final String statusKey = Calculation.CalculationClientAttributeMeta.STATUS.getKey();
        final String commentKey = Calculation.CalculationClientAttributeMeta.COMMENT.getKey();
        final String excludedKey = Calculation.CalculationClientAttributeMeta.EXCLUDED.getKey();

        LinkedEntityAttribute<Entity> origClientAttribute
                = (LinkedEntityAttribute<Entity>) origCalculation.getAttribute(Calculation.CalculationAttributeMeta.CLIENT.getKey());
        LinkedEntityAttribute<Entity> clientAttribute
                = (LinkedEntityAttribute<Entity>) copiedCalculation.getAttribute(Calculation.CalculationAttributeMeta.CLIENT.getKey());

        assertThat(clientAttribute.getEntityList()).isNotEmpty();
        assertThat(clientAttribute.getEntityList().size()).isEqualTo(origClientAttribute.getEntityList().size());

        assertThat(clientAttribute.getEntityAttributeList()).isNotEmpty();
        assertThat(clientAttribute.getEntityAttributeList().size()).isEqualTo(origClientAttribute.getEntityAttributeList().size());

        for (EntityAttribute<Entity> entityAttribute : clientAttribute.getEntityAttributeList()) {
            Entity entity = origClientAttribute.getEntityList().stream()
                                               .filter(e -> e.getHubId().equals(entityAttribute.getLinkedHubId()))
                                               .findFirst()
                                               .orElse(null);
            assertThat(entity).isNotNull();
            assertThat(entityAttribute.isSatelliteDefined()).isTrue();
            EntityAttribute origEntityAttribute = origClientAttribute.getAttributeByEntity(entity);
            assertThat(entityAttribute.getSatellite().getAttributeValue(participantKey))
                    .isEqualTo(origEntityAttribute.getSatellite().getAttributeValue(participantKey));
            assertThat(entityAttribute.getSatellite().getAttributeValue(statusKey))
                    .isEqualTo(origEntityAttribute.getSatellite().getAttributeValue(statusKey));
            assertThat(entityAttribute.getSatellite().getAttributeValue(commentKey))
                    .isEqualTo(origEntityAttribute.getSatellite().getAttributeValue(commentKey));
            assertThat(entityAttribute.getSatellite().getAttributeValue(excludedKey))
                    .isEqualTo(origEntityAttribute.getSatellite().getAttributeValue(excludedKey));
            assertThat(entityAttribute.getSatellite().getId()).isNotEqualTo(origEntityAttribute.getSatellite().getId());
            assertThat(entityAttribute.getLinkId()).isNotEqualTo(origEntityAttribute.getLinkId());
        }
    }

    /**
     * Checks copied calculation classifiers.
     * @param origCalculation original calculation
     * @param copiedCalculation copied calculation
     * @param model model
     */
    private void checkCopiedCalculationClassifiers(Calculation origCalculation, Calculation copiedCalculation, Model model) {
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(calculationEntityMeta.getKeyAttribute(), Operator.EQ, copiedCalculation.getKey()));
        copiedCalculation = calcService.getCalculations(criteria, null, null).get(0);

        assertThat(copiedCalculation.isParentExists()).isTrue();

        List<Entity> oldClassifierValues = calcService.getClassifierValues(origCalculation, null);
        List<Entity> classifierValues = calcService.getClassifierValues(copiedCalculation, null);
        assertThat(classifierValues).isNotEmpty();
        if (origCalculation.getProfiles().size() > 1) {
            assertThat(classifierValues.size()).isEqualTo(oldClassifierValues.size());
            assertThat(classifierValues.stream().map(e -> e.getMeta().getHubId()).collect(Collectors.toList()).toArray())
                    .containsExactlyInAnyOrder(oldClassifierValues.stream().map(e -> e.getMeta().getHubId()).collect(Collectors.toList()).toArray());
        } else
            assertThat(classifierValues.size()).isEqualTo(oldClassifierValues.size() * copiedCalculation.getProfiles().size());
        assertThat(classifierValues.stream().map(Entity::getId).collect(Collectors.toList()))
                .doesNotContainAnyElementsOf(oldClassifierValues.stream().map(Entity::getId).collect(Collectors.toList()));
        for (Entity classifierValue : classifierValues) {
            EntityMeta meta = classifierValue.getMeta();
            String attributeMetaKey = entityMetaService.getAttributeMetaKey(meta, ClassifierAttributeMeta.CALC_PROFILE.name());
            List<Entity> classifierValueProfiles = ((LinkedEntityAttribute<Entity>) classifierValue.getAttribute(attributeMetaKey)).getEntityList();
            assertThat(classifierValueProfiles.size()).as("profiles size must be 1").isEqualTo(1);

            attributeMetaKey = entityMetaService.getAttributeMetaKey(meta, ClassifierAttributeMeta.CLASSIFIER_COMMENT.name());
            assertThat(classifierValue.getAttribute(attributeMetaKey).getValue()).as("comment must be not empty").isNotNull();

            attributeMetaKey = entityMetaService.getAttributeMetaKey(meta, ClassifierAttributeMeta.CLASSIFIER_TYPE.name());
            assertThat(classifierValue.getAttribute(attributeMetaKey).getValue()).as("value must be not empty").isNotNull();
        }
    }

    /**
     * Checks copied calculation input forms.
     * @param origCalculation original calculation
     * @param copiedCalculation copied calculation
     * @param model model
     */
    private void checkCopiedCalculationInputForms(Calculation origCalculation, Calculation copiedCalculation, Model model) {
        List<EntityMeta> inputForms = formTemplateService.getInputForms(model.getFormTemplates());
        EntityMeta profileMeta = entityMetaService.getEntityMetaByKey(CalculationProfileAttributeMeta.METADATA_KEY, null);

        Set<String> profileKeys = copiedCalculation.getProfiles().stream().map(Entity::getKey).collect(Collectors.toSet());
        Object[] profileArray = new String[profileKeys.size()];
        profileArray = profileKeys.toArray(profileArray);
        for (EntityMeta inputForm : inputForms) {
            Criteria inputFormCriteria = new Criteria();
            String profileAttrKey = entityMetaService.getAttributeMetaKey(inputForm, InputFormAttributeMeta.CALC_PROFILE.name());
            AttributeMeta profileRefAttr = inputForm.getAttributeMetadata(profileAttrKey);
            inputFormCriteria.getWhere().addReferenceItem(profileRefAttr, new WhereItem(profileMeta.getKeyAttribute(), Operator.IN, profileArray));
            List<Entity> inputFormValues = calcService.getInputFormValues(copiedCalculation, inputForm, inputFormCriteria, null, null);
            List<Entity> origInputFormValues = calcService.getInputFormValues(origCalculation, inputForm, inputFormCriteria, null, null);
            assertThat(inputFormValues).isNotEmpty();
            if (origCalculation.getProfiles().size() > 1)
                assertThat(inputFormValues.size()).isEqualTo(origInputFormValues.size());
            else
                assertThat(inputFormValues.size()).isEqualTo(origInputFormValues.size() * copiedCalculation.getProfiles().size());

            for (Entity inputFormValue : inputFormValues) {
                EntityMeta meta = inputFormValue.getMeta();
                String attributeMetaKey = entityMetaService.getAttributeMetaKey(meta, InputFormAttributeMeta.CALC_PROFILE.name());
                List<Entity> inputFormValueProfiles = ((LinkedEntityAttribute<Entity>) inputFormValue.getAttribute(attributeMetaKey))
                        .getEntityList();
                assertThat(inputFormValueProfiles.size()).as("profiles size must be 1").isEqualTo(1);
            }
        }
    }

    /**
     * Returns test calculation.
     * @param profiles profiles
     * @return test calculation
     */
    private Calculation getCalculation(List<Entity> profiles) {
        final Integer clientsSize = 3;

        Calculation calculation = (Calculation) entityService.newEmptyEntity(Calculation.METADATA_KEY);
        calculation.setName("draft test ");
        LocalDateTime now = LocalDateTime.now();
        calculation.setActuality(LocalDate.from(now));
        calculation.setDataActuality(now);
        calculation.setAuthor(securityService.getCurrentUser());
        calculation.setCalculated(false);
        calculation.setProfiles(profiles);

        Entity clientGroup = createTestClientGroup("CLGROUP11");
        entityService.persistEntity(clientGroup);
        createTestClient("CLGROUP11CL01", clientGroup);
        createTestClient("CLGROUP11CL02", clientGroup);
        createTestClient("CLGROUP11CL03", clientGroup);
        calculation.setClientGroup(clientGroup);

        Model model = (Model) entityService.newEmptyEntity(Model.METADATA_KEY);
        model.getClassifiers().add(entityMetaClassifier01);
        model.getClassifiers().add(entityMetaClassifier02);
        model.setName(new MultilangDescription("testRu", "testEn"));
        model.setActuality(now);

        model.setFormTemplates(Arrays.asList(getNewFormTemplate()));

        calcService.persistModel(model);
        calculation.setModel(model);
        assertThat(calculation.getClient()).isNull();
        calcService.persistCalculation(calculation);

        LinkedEntityAttribute<Entity> clientAttribute = (LinkedEntityAttribute<Entity>) calculation
                .getAttribute(Calculation.CalculationAttributeMeta.CLIENT.getKey());
        assertThat(clientAttribute.getEntityList().size()).isEqualTo(clientsSize);
        assertThat(clientAttribute.getEntityAttributeList().size()).isEqualTo(clientsSize);
        for (EntityAttribute<Entity> entityAttribute : clientAttribute.getEntityAttributeList()) {
            entityAttribute.getSatellite().setAttributeValue(Calculation.CalculationClientAttributeMeta.PARTICIPANT.getKey(), true);
            entityAttribute.getSatellite().setAttributeValue(Calculation.CalculationClientAttributeMeta.EXCLUDED.getKey(), true);
            entityAttribute.getSatellite().setAttributeValue(Calculation.CalculationClientAttributeMeta.STATUS.getKey(),
                                                             Calculation.CalculationClientStatus.MOTHERHOOD.name());
            entityAttribute.getSatellite().setAttributeValue(Calculation.CalculationClientAttributeMeta.COMMENT.getKey(),
                                                             "comment text" + entityAttribute.getEntity().getKey());
        }
        calcService.persistCalculation(calculation);

        List<Entity> classifierValues = new ArrayList<>();
        for (Entity profile : profiles)
            classifierValues.add(createEntityValueWithProfile(entityMetaClassifier01, calculation, profile));
        for (Entity profile : profiles)
            classifierValues.add(createEntityValueWithProfile(entityMetaClassifier02, calculation, profile));
        calcService.persistsClassifierValues(calculation, classifierValues);

        List<Entity> inputForm01Values = new ArrayList<>();
        for (Entity profile : profiles)
            inputForm01Values.add(createEntityValueWithProfile(entityMetaInputForm01, calculation, profile));
        calcService.persistsInputFormValues(calculation, entityMetaInputForm01, inputForm01Values);

        List<Entity> inputForm02Values = new ArrayList<>();
        for (Entity profile : profiles)
            inputForm02Values.add(createEntityValueWithProfile(entityMetaInputForm02, calculation, profile));
        calcService.persistsInputFormValues(calculation, entityMetaInputForm02, inputForm02Values);

        return calculation;
    }

    /**
     * Returns new form template.
     * @return new form template
     */
    private FormTemplate getNewFormTemplate() {
        EntityMeta calculationMeta = ((Calculation) entityService.newEmptyEntity(Calculation.METADATA_KEY)).getMeta();
        Mapper mapper = new Mapper();
        MappingObject calculationObject = new MappingObject(calculationMeta);
        calculationObject.addObject(new MappingObject(entityMetaInputForm01));
        mapper.addObject(calculationObject);

        calculationObject = new MappingObject(calculationMeta);
        calculationObject.addObject(new MappingObject(entityMetaInputForm02));
        mapper.addObject(calculationObject);

        FormTemplate formTemplate = (FormTemplate) entityService.newEmptyEntity(FormTemplate.METADATA_KEY);
        formTemplate.getName().setDescriptionEn("form-template ");
        formTemplate.setFormat(FileFormat.EXCEL);
        formTemplate.setType(TemplateType.FORM);
        formTemplate.setMapper(mapper);
        formTemplateService.persistFormTemplate(formTemplate);
        return formTemplate;
    }

    /**
     * Create classifier value instance.
     * @param entityMeta classifier metadata
     * @param key entity key
     * @param calculation calculation
     * @return classifier value entity
     */
    private Entity createEntityValue(EntityMeta entityMeta, String key, Calculation calculation) {
        Entity entity = entityService.newEmptyEntity(entityMeta);
        entity.setKey(key);
        entity.setAttributeValue(entityMetaService.getAttributeMetaKey(entityMeta, "STRING"), "string value " + key);
        if (calculation != null) {
            AttributeMeta attributeMeta = entityMeta.getAttributeMetadata(entityMetaService.getAttributeMetaKey(entityMeta, "CALC"));
            entity.setAttribute(new LinkedEntityAttribute(attributeMeta, Collections.singletonList(calculation)));
        }
        return entity;
    }

    /**
     * Create classifier value instance.
     * @param entityMeta classifier metadata
     * @param calculation linked calculation
     * @param profile profile
     * @return classifier value entity
     */
    private Entity createEntityValueWithProfile(EntityMeta entityMeta, Calculation calculation, Entity profile) {
        final BigDecimal someNumber = new BigDecimal("1234");
        final String someString = "someValue";
        Entity entity = entityService.newEmptyEntity(entityMeta);
        entity.setAttributeValue(entityMetaService.getAttributeMetaKey(entityMeta, "STRING"), "string value ");
        if (calculation != null) {
            AttributeMeta attributeMeta = entityMeta.getAttributeMetadata(entityMetaService.getAttributeMetaKey(entityMeta, "CALC"));
            entity.setAttribute(new LinkedEntityAttribute(attributeMeta, Collections.singletonList(calculation)));
        }
        if (profile != null) {
            LinkedEntityAttribute calcProfile = (LinkedEntityAttribute) entity
                    .getAttribute(entityMetaService.getAttributeMetaKey(entityMeta, ClassifierAttributeMeta.CALC_PROFILE.name()));
            calcProfile.getEntityList().clear();
            calcProfile.getEntityList().add(profile);
        }
        if (entity.getMeta().getType() == EntityType.CLASSIFIER) {
            AbstractAttribute commentAttr = entity
                    .getAttribute(entityMetaService.getAttributeMetaKey(entityMeta, ClassifierAttributeMeta.CLASSIFIER_COMMENT.name()));
            commentAttr.setValue(someString);

            AbstractAttribute typeAttr = entity
                    .getAttribute(entityMetaService.getAttributeMetaKey(entityMeta, ClassifierAttributeMeta.CLASSIFIER_TYPE.name()));
            typeAttr.setValue(someNumber);
        }
        return entity;
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
        }
        return entityMetaService.getEntityMetaByKey(entityMeta.getKey(), null);
    }

    /**
     * Create test data at database.
     * @param key test object key
     * @param calculated calculated flag
     * @param doLoadExisted load calculation if it exists
     * @return return created of found calculation
     */
    private Calculation createTestCalculationIfNotexists(String key, boolean calculated, boolean doLoadExisted) {
        if (entityService.getEntityIdByKey(calculationEntityMeta, key) == null) {
            LocalDateTime currentTime = entityMetaService.getSysTimestamp();

            Calculation calculation = (Calculation) entityService.newEmptyEntity(Calculation.METADATA_KEY);
            calculation.setKey(key);
            calculation.setName("draft test " + key);
            calculation.setActuality(currentTime.toLocalDate());
            calculation.setDataActuality(currentTime);
            calculation.setAuthor(securityService.getCurrentUser());
            calculation.setCalculated(calculated);

            calcService.persistCalculation(calculation);
            return calculation;
        } else if (doLoadExisted) {
            Criteria criteria = new Criteria();
            criteria.getWhere().addItem(new WhereItem(calculationEntityMeta.getKeyAttribute(), Operator.EQ, key));
            Calculation calculation = calcService.getCalculations(criteria, null, null).get(0);
            return calculation;
        }
        return null;
    }

    /**
     * Create test data at database.
     * @param key test object key
     * @return return created or found model
     */
    private Model createTestModelIfNotexists(String key) {
        if (entityService.getEntityIdByKey(modelEntityMeta, key) == null) {
            Model model = (Model) entityService.newEmptyEntity(Model.METADATA_KEY);
            model.setKey(key);
            model.setName(new MultilangDescription("черновик тест " + key, "draft test " + key));
            model.setPublished(false);
            model.setActuality(metadataDao.getSysTimestamp());

            calcService.persistModel(model);
            return model;
        } else {
            Criteria criteria = new Criteria();
            criteria.getWhere().addItem(new WhereItem(modelEntityMeta.getKeyAttribute(), Operator.EQ, key));
            Model model = calcService.getModels(criteria, null, null).get(0);
            return model;
        }
    }

    /**
     * Creates client group with clients.
     * @param clientGroupKey client group key
     * @return client group
     */
    private Entity createClientGroupWithClients(String clientGroupKey) {
        Entity clientGroup = createTestClientGroup(clientGroupKey);
        entityService.persistEntity(clientGroup);
        createTestClient(String.format("%sCL01", clientGroupKey), clientGroup);
        createTestClient(String.format("%sCL02", clientGroupKey), clientGroup);
        createTestClient(String.format("%sCL03", clientGroupKey), clientGroup);
        return clientGroup;
    }

    /**
     * Create or load entity.
     * @param entityMeta entity metadata
     * @param key key
     * @param ldts load datetime
     * @return entity loaded or empty
     */
    private Entity createTestEntity(EntityMeta entityMeta, String key, LocalDateTime ldts) {
        Entity entity;
        if (entityService.getEntityIdByKey(entityMeta, key) == null)
            entity = entityService.newEmptyEntity(entityMeta);
        else
            entity = entityService.getEntity(entityMeta, key, ldts);

        assertThat(entity).isNotNull();
        entity.setKey(key);
        return entity;
    }

    /**
     * Create entity of any type if not exists.
     * @param entityMetaKey type of entity
     * @param key entity key
     * @param ldts load datetime
     * @return new or loaded entity
     */
    private Entity createTestEntity(String entityMetaKey, String key, LocalDateTime ldts) {
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(entityMetaKey, ldts);
        return createTestEntity(entityMeta, key, ldts);
    }

    /**
     * Create or load client entity.
     * @param key client key
     * @return client entity
     */
    private Entity createTestClient(String key) {
        return createTestClient(key, null);
    }

    /**
     * Create or load client entity.
     * @param key client key
     * @param clientGroup client group
     * @return client entity
     */
    private Entity createTestClient(String key, Entity clientGroup) {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        Entity client = createTestEntity(ClientAttributeMeta.METADATA_KEY, key, ldts);
        if (client.getHubId() == null) {
            ((MultilangAttribute) client.getAttribute(ClientAttributeMeta.FULL_NAME.getKey())).getMultilangDescription()
                                                                                              .setDescriptionEn("Name " + key);
            ((MultilangAttribute) client.getAttribute(ClientAttributeMeta.NAME.getKey())).getMultilangDescription().setDescriptionEn("Name " + key);
            if (clientGroup != null)
                ((LinkedEntityAttribute) client.getAttribute(ClientAttributeMeta.CLIENT_GROUP.getKey())).add(clientGroup);
            entityService.persistEntity(client);
        }
        return client;
    }

    /**
     * Create or load client entity.
     * @param key client key
     * @param department department for client
     * @return client entity
     */
    private Entity createTestClient(String key, Department department) {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        Entity client = createTestEntity(ClientAttributeMeta.METADATA_KEY, key, ldts);
        if (client.getHubId() == null) {
            ((MultilangAttribute) client.getAttribute(ClientAttributeMeta.FULL_NAME.getKey())).getMultilangDescription()
                                                                                              .setDescriptionEn("Name " + key);
            ((MultilangAttribute) client.getAttribute(ClientAttributeMeta.NAME.getKey())).getMultilangDescription()
                                                                                         .setDescriptionEn("Name " + key);

            if (department != null)
                ((LinkedEntityAttribute) client.getAttribute(ClientAttributeMeta.DEPARTMENT.getKey())).getEntityList().add(department);

            entityService.persistEntity(client);
        }
        return client;
    }

    /**
     * Create or load client group entity.
     * @param key client group key
     * @return client group key
     */
    private Entity createTestClientGroup(String key) {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        Entity clientGroup = createTestEntity(ClientGroupAttributeMeta.METADATA_KEY, key, ldts);
        if (clientGroup.getHubId() == null) {
            ((MultilangAttribute) clientGroup.getAttribute(ClientGroupAttributeMeta.FULL_NAME.getKey())).getMultilangDescription()
                                                                                                        .setDescriptionEn("Name " + key);
            ((MultilangAttribute) clientGroup.getAttribute(ClientGroupAttributeMeta.NAME.getKey())).getMultilangDescription()
                                                                                                   .setDescriptionEn("Name " + key);
            entityService.persistEntity(clientGroup);
        }
        return clientGroup;
    }

    /**
     * Create test user.
     * @param key user key
     * @param role user role
     * @param department department for user
     * @return user object created
     */
    private User createTestUser(String key, Role role, Department department) {
        EntityMeta userEntityMeta = entityMetaService.getEntityMetaByKey(User.METADATA_KEY, null);
        if (entityService.getEntityIdByKey(userEntityMeta, key) == null) {
            User user = (User) entityService.newEmptyEntity(userEntityMeta);
            user.setKey(key);
            user.setName(key);
            if (role != null)
                user.getRoles().add(role);
            if (department != null)
                user.setDepartment(department);
            userRoleService.persistUser(user);
            return user;

        } else {
            Criteria criteria = new Criteria();
            criteria.getWhere().addItem(new WhereItem(userEntityMeta.getKeyAttribute(), Operator.EQ, key));
            return userRoleService.getUser(key);
        }
    }

    /**
     * Prepares initial test data.
     * @throws Exception exception
     */
    @Before
    public void setup() throws Exception {
        securityService.defineSecurityContext(ADMINWF_LOGIN);
        if (!setupDone) {
            calculationEntityMeta = entityMetaService.getEntityMetaByKey(Calculation.METADATA_KEY, null);
            modelEntityMeta = entityMetaService.getEntityMetaByKey(Model.METADATA_KEY, null);

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

    /**
     * Restore secure state.
     */
    @After
    public void finish() {
        securityService.defineSecurityContext(ADMINWF_LOGIN);
    }

    /**
     * Create or test department.
     * @param key department key
     * @return department object
     */
    private Department createTestDepartment(String key) {
        EntityMeta departmentMeta = entityMetaService.getEntityMetaByKey(Department.METADATA_KEY, null);
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(departmentMeta.getKeyAttribute(), Operator.EQ, key));
        List<Department> departments = (List<Department>) entityService.getEntities(departmentMeta, criteria, null, null);
        assertThat(departments).as("Department key = " + key).isNotEmpty();
        return departments.get(0);
    }
}
