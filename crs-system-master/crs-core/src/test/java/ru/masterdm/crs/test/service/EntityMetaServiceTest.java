package ru.masterdm.crs.test.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.masterdm.crs.dao.entity.meta.MetadataDao;
import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.ClassifierAttributeMeta;
import ru.masterdm.crs.domain.calc.FormulaResult;
import ru.masterdm.crs.domain.calc.InputFormAttributeMeta;
import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.OrderItem;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMetaGroup;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.domain.entity.meta.EntityTypeAttributeMeta;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.SecurityService;
import ru.masterdm.crs.util.converter.StringToLocalDateConverter;

/**
 * Tests Data Vault interaction service.
 * @author Sergey Valiev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml",
                                   "classpath:META-INF/spring/crs-security-test.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EntityMetaServiceTest {

    private static final String FORM = "newForm";
    private static final String INVALID_KEY = "123QWE_";

    private static EntityMeta entityMeta;
    private static AttributeMeta attributeMeta;

    @Autowired
    private EntityMetaService entityMetaService;
    @Autowired
    private EntityService entityService;
    @Autowired
    private SecurityService securityService;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private MetadataDao metadataDao;
    @Autowired
    @Qualifier("config")
    private Properties config;

    /**
     * Tests input data.
     */
    @Test
    public void test01CheckInputData() {
        assertThatThrownBy(() -> entityMetaService.persistEntityMeta(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> entityMetaService.persistEntityMeta(new EntityMeta()))
                .isInstanceOf(CrsException.class);

        EntityMeta entityMeta = new EntityMeta();
        entityMeta.setKey(INVALID_KEY);
        assertThatThrownBy(() -> entityMetaService.persistEntityMeta(entityMeta))
                .isInstanceOf(CrsException.class);
    }

    /**
     * Tests persist entity metadata.
     */
    @Test
    public void test02PersistEntity() {
        assertNotNull(entityMeta.getAttributes().get(0).getNativeColumn());
        entityMetaService.persistEntityMeta(entityMeta);

        assertNotNull(entityMeta.getId());
        assertNotNull(entityMeta.getLdts());
        assertNotNull(entityMeta.getHubId());
        assertNull(entityMeta.getAttributes().get(0).getNativeColumn());
        assertThat(catchThrowable(() -> metadataDao.execute(String.format("select 1 from %s%s",
                                                                          config.getProperty("ddl.hub.prefix"),
                                                                          entityMeta.getKey()))))
                .isNull();
        assertThat(catchThrowable(() -> metadataDao.execute(String.format("select 1 from %s%s",
                                                                          config.getProperty("ddl.satellite.prefix"),
                                                                          entityMeta.getKey()))))
                .isNull();

        Long entityId = entityMeta.getId();
        Long entityHubId = entityMeta.getHubId();

        entityMeta.setForm(FORM);
        entityMetaService.persistEntityMeta(entityMeta);

        assertFalse(entityMeta.getId().equals(entityId));
        assertTrue(entityMeta.getHubId().equals(entityHubId));

        entityMeta.getAttributes().add(attributeMeta);
        entityMetaService.persistEntityMeta(entityMeta);

        attributeMeta = entityMeta.getAttributes()
                                  .stream()
                                  .filter(attribute -> attribute.getKey().equals(attributeMeta.getKey()))
                                  .findFirst()
                                  .get();
        assertNotNull(attributeMeta.getId());
        assertThat(catchThrowable(() -> metadataDao.execute(String.format("select %s from %s%s",
                                                                          attributeMeta.getNativeColumn(),
                                                                          config.getProperty("ddl.satellite.prefix"),
                                                                          entityMeta.getKey()))))
                .isNull();
    }

    /**
     * Tests entity existence.
     */
    @Test
    public void test03CheckEntityExistence() {
        assertThatThrownBy(() -> entityMetaService.isEntityMetaExists(null)).isInstanceOf(IllegalArgumentException.class);
        assertTrue(entityMetaService.isEntityMetaExists(entityMeta.getKey()));
    }

    /**
     * Test adding new entity meta to default group.
     */
    @Test
    public void test03x2CheckEntityInGroup() {
        EntityMeta meta = entityMetaService.getEntityMetaByKey(EntityMetaGroup.METADATA_KEY, null);
        Criteria criteria = new Criteria();
        criteria.getWhere()
                .addItem(new WhereItem(meta.getKeyAttribute(), Operator.EQ, EntityMetaGroup.DefaultGroup.DEFAULT_DICTIONARY_GROUP.name()));
        List<EntityMetaGroup> entityMetaGroups = entityMetaService.getEntityMetaGroups(criteria, null, null);
        assertThat(entityMetaGroups).isNotEmpty();
        List<EntityMeta> entityMetas = entityMetaGroups.get(0).getElements();
        assertThat(entityMetas).contains(entityMeta);
        assertThat(IntStream.range(0, entityMetas.size() - 1)
                            .allMatch(i -> entityMetas.get(i).getViewOrder().compareTo(entityMetas.get(i + 1).getViewOrder()) <= 0))
                .as("must be sort ascending by view order").isTrue();
    }

    /**
     * Tests attriute existence.
     */
    @Test
    public void test04CheckAttributeExistence() {
        assertThatThrownBy(() -> entityMetaService.isAttributeMetaExists(null)).isInstanceOf(IllegalArgumentException.class);
        assertTrue(entityMetaService.isAttributeMetaExists(attributeMeta.getKey()));
    }

    /**
     * Create input form entity metadata and structure.
     */
    @Test
    public void test05CreateInputFormEntityMeta() {
        String key = "INFOR04";

        EntityMeta entityMeta = entityMetaService.getEntityMetaPrototypeFactory(EntityType.INPUT_FORM).create(key);
        entityMeta.setName(new MultilangDescription("форма ввода " + key, "input form " + key));
        entityMeta.setComment(new MultilangDescription("комментарий " + key, "comment " + key));

        String calcRefAttributeKey = entityMetaService.getAttributeMetaKey(entityMeta, InputFormAttributeMeta.CALC.name());
        AttributeMeta calculationAttributeMetaRef = entityMeta.getAttributeMetadata(calcRefAttributeKey);
        assertThat(calculationAttributeMetaRef).as("Calc reference attribute key: %s", calcRefAttributeKey).isNotNull();
        calculationAttributeMetaRef.setName(new MultilangDescription("ссылка на calc", "calc reference"));

        String profileRefAttributeKey = entityMetaService.getAttributeMetaKey(entityMeta, InputFormAttributeMeta.CALC_PROFILE.name());
        AttributeMeta profileAttributeMetaRef = entityMeta.getAttributeMetadata(profileRefAttributeKey);
        assertThat(profileAttributeMetaRef).as("Calc reference attribute key: %s", profileRefAttributeKey).isNotNull();
        profileAttributeMetaRef.setName(new MultilangDescription("ссылка на calc profile", "calc profile reference"));

        entityMetaService.persistEntityMeta(entityMeta);
    }

    /**
     * Create classifier entity metadata and structure.
     */
    @Test
    public void test06CreateClassifierEntityMeta() {
        String key = "CLSS04";

        EntityMeta entityMeta = entityMetaService.getEntityMetaPrototypeFactory(EntityType.CLASSIFIER).create(key);
        entityMeta.setName(new MultilangDescription("классификатор " + key, "classifier " + key));
        entityMeta.setComment(new MultilangDescription("комментарий " + key, "comment " + key));

        String calcRefAttributeKey = entityMetaService.getAttributeMetaKey(entityMeta, ClassifierAttributeMeta.CALC.name());
        AttributeMeta calcAttrMeta = entityMeta.getAttributeMetadata(calcRefAttributeKey);
        assertThat(calcRefAttributeKey).as("Calc reference attribute key: %s", calcRefAttributeKey).isNotNull();
        calcAttrMeta.setName(new MultilangDescription("ссылка на calc", "calc reference"));

        String typeAttrKey = entityMetaService.getAttributeMetaKey(entityMeta, ClassifierAttributeMeta.CLASSIFIER_TYPE.name());
        AttributeMeta typeAttrMeta = entityMeta.getAttributeMetadata(typeAttrKey);
        assertThat(typeAttrMeta).as("Type attribute key: %s", typeAttrKey).isNotNull();
        typeAttrMeta.setType(AttributeType.DATE);
        typeAttrMeta.setName(new MultilangDescription("тип", "type"));
        LocalDate localDate = LocalDate.now();
        typeAttrMeta.setDefaultValue(localDate.format(DateTimeFormatter.ofPattern(StringToLocalDateConverter.FORMAT)));

        String commentAttrKey = entityMetaService.getAttributeMetaKey(entityMeta, ClassifierAttributeMeta.CLASSIFIER_COMMENT.name());
        AttributeMeta commentAttrMeta = entityMeta.getAttributeMetadata(commentAttrKey);
        assertThat(typeAttrMeta).as("Comment attribute key: %s", commentAttrKey).isNotNull();
        commentAttrMeta.setName(new MultilangDescription("комментарий", "comment"));

        String profileRefAttributeKey = entityMetaService.getAttributeMetaKey(entityMeta, ClassifierAttributeMeta.CALC_PROFILE.name());
        AttributeMeta profileAttributeMetaRef = entityMeta.getAttributeMetadata(profileRefAttributeKey);
        assertThat(profileAttributeMetaRef).as("Calc reference attribute key: %s", profileRefAttributeKey).isNotNull();
        profileAttributeMetaRef.setName(new MultilangDescription("ссылка на calc profile", "calc profile reference"));

        entityMetaService.persistEntityMeta(entityMeta);

        //read classifier entity
        EntityMeta entityMetaClass = entityMetaService.getEntityMetaByKey(key, null);
        assertThat(entityMetaClass.getAttributeMetadata(typeAttrKey).getDefaultValue())
                .isEqualTo(localDate.format(DateTimeFormatter.ofPattern(StringToLocalDateConverter.FORMAT)));
    }

    /**
     * Read attribute attributes (for REFERENCE).
     */
    @Test
    public void test07AttributeAttributes() {
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(Calculation.METADATA_KEY, null);
        AttributeMeta attributeMeta = entityMeta.getAttributeMetadata(Calculation.CalculationAttributeMeta.MODEL.getKey());
        assertThat(attributeMeta).isNotNull();
        assertThat(attributeMeta.getAttributeAttributes()).isNotNull();
        assertThat(attributeMeta.getAttributeAttributes()).hasSize(1);
    }

    /**
     * Read attribute attributes (for REFERENCE).
     */
    @Test
    public void test08KeyName() {
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(User.METADATA_KEY, null);
        assertThat(entityMeta).isNotNull();
        assertThat(entityMeta.getKeyName()).isNotNull();
        assertThat(entityMeta.getKeyName().getDescriptionEn()).isEqualTo("Login");
        assertThat(entityMeta.getKeyName().getDescriptionRu()).isEqualTo("Логин");
        AttributeMeta attributeMeta = entityMeta.getKeyAttribute();
        assertThat(attributeMeta).isNotNull();
        assertThat(attributeMeta.getName()).isNotNull();
        assertThat(attributeMeta.getName().getDescriptionEn()).isEqualTo("Login");
        assertThat(attributeMeta.getName().getDescriptionRu()).isEqualTo("Логин");
    }

    /**
     * Load entity metas by query criteria.
     */
    @Test
    public void test09EntityMetas() {
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(EntityMeta.METADATA_KEY, null);
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(entityMeta.getKeyAttribute(), Operator.IN, Calculation.METADATA_KEY, User.METADATA_KEY));
        criteria.getOrder().addItem(new OrderItem(entityMeta.getKeyAttribute()));
        List<EntityMeta> entityMetaList = entityMetaService.getEntityMetas(criteria, null, null, EntityType.EMBEDDED_OBJECT);
        assertThat(entityMetaList).hasSize(2);
        // CALC first
        EntityMeta calculationEntityMeta = entityMetaList.get(0);
        assertThat(calculationEntityMeta.getKey()).isEqualTo(Calculation.METADATA_KEY);
        AttributeMeta nameAttributeMeta = calculationEntityMeta.getAttributeMetadata(Calculation.CalculationAttributeMeta.NAME.getKey());
        assertThat(nameAttributeMeta).isNotNull();
        assertThat(nameAttributeMeta.getName().getDescriptionEn()).isEqualTo("Name");

        // USER second
        EntityMeta userEntityMeta = entityMetaList.get(1);
        nameAttributeMeta = userEntityMeta.getAttributeMetadata(User.UserAttributeMeta.SURNAME.getKey());
        assertThat(nameAttributeMeta).isNotNull();
        assertThat(nameAttributeMeta.getName().getDescriptionEn()).isEqualTo("Surname");

        criteria = new Criteria();
        criteria.setHubIds(Arrays.asList(calculationEntityMeta.getHubId(), userEntityMeta.getHubId()));
        criteria.getOrder().addItem(new OrderItem(entityMeta.getKeyAttribute()));
        entityMetaList = entityMetaService.getEntityMetas(criteria, null, null, EntityType.EMBEDDED_OBJECT);
        assertThat(entityMetaList).hasSize(2);
        // CALC first
        calculationEntityMeta = entityMetaList.get(0);
        assertThat(calculationEntityMeta.getKey()).isEqualTo(Calculation.METADATA_KEY);
        nameAttributeMeta = calculationEntityMeta.getAttributeMetadata(Calculation.CalculationAttributeMeta.NAME.getKey());
        assertThat(nameAttributeMeta).isNotNull();
        assertThat(nameAttributeMeta.getName().getDescriptionEn()).isEqualTo("Name");

        // USER second
        userEntityMeta = entityMetaList.get(1);
        nameAttributeMeta = userEntityMeta.getAttributeMetadata(User.UserAttributeMeta.SURNAME.getKey());
        assertThat(nameAttributeMeta).isNotNull();
        assertThat(nameAttributeMeta.getName().getDescriptionEn()).isEqualTo("Surname");

        LocalDateTime ldts = metadataDao.getSysTimestamp();
        criteria = new Criteria();
        criteria.setHubIdsAndLdts(
                Arrays.asList(new ImmutablePair(calculationEntityMeta.getHubId(), ldts), new ImmutablePair(userEntityMeta.getHubId(), ldts)));
        criteria.getOrder().addItem(new OrderItem(entityMeta.getKeyAttribute()));
        entityMetaList = entityMetaService.getEntityMetas(criteria, null, null, EntityType.EMBEDDED_OBJECT);
        assertThat(entityMetaList).hasSize(2);
        // CALC first
        calculationEntityMeta = entityMetaList.get(0);
        assertThat(calculationEntityMeta.getKey()).isEqualTo(Calculation.METADATA_KEY);
        nameAttributeMeta = calculationEntityMeta.getAttributeMetadata(Calculation.CalculationAttributeMeta.NAME.getKey());
        assertThat(nameAttributeMeta).isNotNull();
        assertThat(nameAttributeMeta.getName().getDescriptionEn()).isEqualTo("Name");

        // USER second
        userEntityMeta = entityMetaList.get(1);
        nameAttributeMeta = userEntityMeta.getAttributeMetadata(User.UserAttributeMeta.SURNAME.getKey());
        assertThat(nameAttributeMeta).isNotNull();
        assertThat(nameAttributeMeta.getName().getDescriptionEn()).isEqualTo("Surname");
    }

    /**
     * Test metadatas read with paging.
     */
    @Test
    public void test10EntityMetasPaging() {
        final int four = 4;
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(EntityMeta.METADATA_KEY, null);
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(
                new WhereItem(entityMeta.getKeyAttribute(), Operator.IN, Calculation.METADATA_KEY, User.METADATA_KEY, Model.METADATA_KEY,
                              FormulaResult.METADATA_KEY));
        RowRange rowRange = RowRange.newAsPageAndSize(0, 2);
        List<EntityMeta> entityMetaList = entityMetaService.getEntityMetas(criteria, rowRange, null, EntityType.EMBEDDED_OBJECT);
        assertThat(entityMetaList).hasSize(2);
        assertThat(rowRange.getTotalCount()).isEqualTo(four);
    }

    /**
     * Test get simple entity meta group.
     */
    @Test
    public void test11GetEntityMetaGroupSimple() {
        EntityMeta entityMetaGroupMeta = entityMetaService.getEntityMetaByKey(EntityMetaGroup.METADATA_KEY, null);
        String key = "TEST_GROUP";
        if (entityService.getEntityIdByKey(entityMetaGroupMeta, key) == null) {
            EntityMetaGroup entityMetaGroup = (EntityMetaGroup) entityService.newEmptyEntity(entityMetaGroupMeta);
            entityMetaGroup.setKey(key);
            entityMetaGroup.getName().setDescriptionEn("test group");
            entityMetaGroup.getName().setDescriptionRu("тест");
            entityMetaGroup.setType(EntityType.DICTIONARY);
            entityMetaService.persistEntityMetaGroup(entityMetaGroup);
        }

        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(entityMetaGroupMeta.getKeyAttribute(), Operator.EQ, key));
        List<EntityMetaGroup> entityMetaGroupList = entityMetaService.getEntityMetaGroups(criteria, null, null);
        assertThat(entityMetaGroupList).hasSize(1);
        assertThat(entityMetaGroupList.get(0).getType()).isNotNull();
    }

    /**
     * Test get simple entity meta group.
     */
    @Test
    public void test12GetEntityMetaGroupWithEntity() {
        EntityMeta calcEntityMeta = entityMetaService.getEntityMetaByKey(Calculation.METADATA_KEY, null);

        EntityMeta entityMetaGroupMeta = entityMetaService.getEntityMetaByKey(EntityMetaGroup.METADATA_KEY, null);
        String key = "TEST_ENTITY_GROUP12";
        if (entityService.getEntityIdByKey(entityMetaGroupMeta, key) == null) {
            EntityMetaGroup entityMetaGroup = (EntityMetaGroup) entityService.newEmptyEntity(entityMetaGroupMeta);
            entityMetaGroup.setKey(key);
            entityMetaGroup.getName().setDescriptionEn("test group");
            entityMetaGroup.getName().setDescriptionRu("тест");
            entityMetaGroup.setElements(Arrays.asList(calcEntityMeta));
            entityMetaGroup.setType(EntityType.EMBEDDED_OBJECT);
            entityMetaService.persistEntityMetaGroup(entityMetaGroup);
        }

        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(entityMetaGroupMeta.getKeyAttribute(), Operator.EQ, key));
        List<EntityMetaGroup> entityMetaGroupList = entityMetaService.getEntityMetaGroups(criteria, null, null);
        assertThat(entityMetaGroupList).hasSize(1);
        EntityMetaGroup entityMetaGroup = entityMetaGroupList.get(0);
        assertThat(entityMetaGroup.getType()).isEqualTo(EntityType.EMBEDDED_OBJECT);
        assertThat(entityMetaGroup.getElements()).isNotNull();
        assertThat(entityMetaGroup.getElements()).containsExactlyInAnyOrder(calcEntityMeta);

        // criteria by type
        EntityMeta entityTypeMeta = entityMetaService.getEntityMetaByKey(EntityTypeAttributeMeta.METADATA_KEY, null);
        AttributeMeta groupEntityTypeMeta =
                entityMetaGroupMeta.getAttributeMetadata(EntityMetaGroup.EntityMetaGroupAttributeMeta.ENTITY_TYPE.getKey());
        criteria = new Criteria();
        criteria.getWhere().addReferenceItem(groupEntityTypeMeta,
                                             new WhereItem(entityTypeMeta.getKeyAttribute(), Operator.EQ, EntityType.EMBEDDED_OBJECT.name()));
        entityMetaGroupList = entityMetaService.getEntityMetaGroups(criteria, null, null);
        entityMetaGroup = entityMetaGroupList.get(0);
        assertThat(entityMetaGroup.getElements()).contains(calcEntityMeta);
    }

    /**
     * Check impossibility to delete default group.
     */
    @Test
    public void test13DeleteDefaultEntityGroup() {
        EntityMeta entityMetaGroupMeta = entityMetaService.getEntityMetaByKey(EntityMetaGroup.METADATA_KEY, null);
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(
                new WhereItem(entityMetaGroupMeta.getKeyAttribute(), Operator.EQ, EntityMetaGroup.DefaultGroup.DEFAULT_INPUT_FORM_GROUP.name()));
        List<EntityMetaGroup> entityMetaGroupList = entityMetaService.getEntityMetaGroups(criteria, null, null);
        assertThat(entityMetaGroupList).hasSize(1);
        EntityMetaGroup entityMetaGroup = entityMetaGroupList.get(0);
        assertThat(catchThrowable(() -> entityMetaService.removeEntityMetaGroup(entityMetaGroup)))
                .hasMessage("Cant delete default group " + EntityMetaGroup.DefaultGroup.DEFAULT_INPUT_FORM_GROUP.name());
    }

    /**
     * Check if moving to default group from regular.
     */
    @Test
    public void test14MoveToDefaultGroup() {
        EntityMeta entityMetaGroupMeta = entityMetaService.getEntityMetaByKey(EntityMetaGroup.METADATA_KEY, null);
        EntityMeta meta = entityMetaService.getEntityMetaByKey(entityMeta.getKey(), null);

        String key = "TEST_ENTITY_GROUPxxx14";
        EntityMetaGroup entityMetaGroup = (EntityMetaGroup) entityService.newEmptyEntity(entityMetaGroupMeta);
        entityMetaGroup.setKey(key);
        entityMetaGroup.getName().setDescriptionEn("test group");
        entityMetaGroup.getName().setDescriptionRu("тест");
        entityMetaGroup.setElements(Arrays.asList(meta));
        entityMetaGroup.setType(EntityType.EMBEDDED_OBJECT);
        entityMetaService.persistEntityMetaGroup(entityMetaGroup);
        entityMetaService.removeEntityMetaGroup(entityMetaGroup);
        assertThat(entityMetaGroup.getElements()).isEmpty();

        // check deletion
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(entityMetaGroupMeta.getKeyAttribute(), Operator.EQ, key));
        List<EntityMetaGroup> entityMetaGroupList = entityMetaService.getEntityMetaGroups(criteria, null, null);
        assertThat(entityMetaGroupList).isEmpty();

        // check moving to default group
        criteria = new Criteria();
        criteria.getWhere().addItem(
                new WhereItem(entityMetaGroupMeta.getKeyAttribute(), Operator.EQ, EntityMetaGroup.DefaultGroup.DEFAULT_DICTIONARY_GROUP.name()));
        entityMetaGroupList = entityMetaService.getEntityMetaGroups(criteria, null, null);
        assertThat(entityMetaGroupList).isNotEmpty();
        EntityMetaGroup entityMetaGroupDict = entityMetaGroupList.get(0);
        List<EntityMeta> entityMetas = entityMetaGroupDict.getElements();
        assertThat(entityMetas).contains(meta);
        assertThat(IntStream.range(0, entityMetas.size() - 1)
                            .allMatch(i -> entityMetas.get(i).getViewOrder().compareTo(entityMetas.get(i + 1).getViewOrder()) <= 0))
                .as("must be sort ascending by view order").isTrue();
    }

    /**
     * Test metadata caching.
     */
    @Test
    public void test15MetaCache() {
        EntityMeta meta = entityMetaService.getEntityMetaByKey(entityMeta.getKey(), metadataDao.getSysTimestamp());
        EntityMeta meta2 = entityMetaService.getEntityMetaByKey(entityMeta.getKey(), metadataDao.getSysTimestamp());
        // should be same object
        assertThat(meta == meta2).isTrue();

        final String newMetaName = "newMetaName";
        assertThat(meta.getName().getDescriptionRu()).isNotEqualTo(newMetaName);
        meta.getName().setDescriptionRu(newMetaName);
        entityMetaService.persistEntityMeta(meta);
        EntityMeta meta3 = entityMetaService.getEntityMetaByKey(entityMeta.getKey(), metadataDao.getSysTimestamp());
        assertThat(meta2 == meta3).isFalse();
        assertThat(meta2.equals(meta3)).isTrue();
    }

    /**
     * Prepares initial test data.
     * @throws Exception exception
     */
    @BeforeClass
    public static void setup() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        entityMeta = objectMapper.readValue(EntityMetaServiceTest.class.getResourceAsStream("dv_service_test_entity_meta.json"),
                                            EntityMeta.class);
        attributeMeta = objectMapper.readValue(EntityMetaServiceTest.class.getResourceAsStream("dv_service_test_attribute_meta.json"),
                                               AttributeMeta.class);
        assertNotNull(entityMeta);
        assertNotNull(attributeMeta);
    }
}
