package ru.masterdm.crs.test.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.jooq.tools.StringUtils;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.masterdm.crs.dao.MetadataCheckingDao;
import ru.masterdm.crs.dao.entity.meta.MetadataDao;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.attribute.EntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.FileInfoAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.MultilangAttribute;
import ru.masterdm.crs.domain.entity.criteria.Conjunction;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.Order;
import ru.masterdm.crs.domain.entity.criteria.OrderItem;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.criteria.Where;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.criteria.WhereParenthesesGroup;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientGroupAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.CommonAttribute;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMetaGroup;
import ru.masterdm.crs.exception.meta.RemoveReferencedMetaException;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.entity.EntityDbService;
import ru.masterdm.crs.util.CollectorUtils;

/**
 * Tests Data Vault interaction service.
 * @author Sergey Valiev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml",
                                   "classpath:META-INF/spring/crs-security-test.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EntityServicePersistTest {

    public static final int SIMLE_INT = 10101;
    public static final int TEST_ENTITIES_TO_CREATE = 10;

    private static EntityMeta entityMetaForPersist;
    private static EntityMeta entityMetaForPersistChild;
    private static EntityMeta entityMetaForPersistNoIntable;
    private static String entityDataForPersist;
    private static AttributeMeta attributeMeta;

    @Autowired
    private EntityService entityService;
    @Autowired
    private EntityMetaService entityMetaService;
    @Autowired
    private EntityDbService entityDbService;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private MetadataDao metadataDao;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private MetadataCheckingDao metadataCheckingDao;

    /**
     * Tests prepare metadata for persist entity.
     */
    @Test
    public void test001PersistCreateEntityMeta() {
        if (!entityMetaService.isEntityMetaExists(PersistTestConstant.PERSIST_TEST_CHILD_META_KEY)) {
            entityMetaService.persistEntityMeta(entityMetaForPersistChild);
            assertThat(entityMetaForPersistChild.getId()).isNotNull();
        }
        if (!entityMetaService.isEntityMetaExists(PersistTestConstant.PERSIST_META_KEY)) {
            entityMetaService.persistEntityMeta(entityMetaForPersist);
            assertThat(entityMetaForPersist.getId()).isNotNull();
        }
    }

    /**
     * Tests create empty entity and set attribute values.
     */
    @Test
    public void test002PersistCreateEmptyEntity() {
        LocalDate d = LocalDate.parse("2009-09-09");
        LocalDateTime dt = LocalDateTime.parse("2009-09-09T09:09:09");
        BigDecimal bd = new BigDecimal(1);
        BigDecimal bd2 = new BigDecimal(2);

        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, metadataDao.getSysTimestamp());

        Entity entity = entityService.newEmptyEntity(meta);
        assertThat(entity).isNotNull();

        // predefined attributes
        entity.setAttributeValue(CommonAttribute.H_ID.name(), bd);
        assertThat(entity.getHubId()).isEqualTo(bd.longValue());
        entity.setAttributeValue(CommonAttribute.KEY.name(), "KEYKEY");
        assertThat(entity.getKey()).isEqualTo("KEYKEY");

        entity.setKey("YEKYEK");
        assertThat(entity.getAttributeValue(CommonAttribute.KEY.name())).isEqualTo("YEKYEK");
        entity.setHubId(bd2.longValue());
        assertThat(entity.getAttributeValue(CommonAttribute.H_ID.name())).isEqualTo(bd2);

        // dynamic attributes
        entity.setAttributeValue(PersistTestConstant.PersistTestFields.STRING, "teststring");
        assertThat(entity.getAttribute(PersistTestConstant.PersistTestFields.STRING).getValue()).isEqualTo("teststring");

        entity.setAttributeValue(PersistTestConstant.PersistTestFields.BOOLEAN, true);
        assertThat(entity.getAttribute(PersistTestConstant.PersistTestFields.BOOLEAN).getValue()).isEqualTo(true);

        entity.setAttributeValue(PersistTestConstant.PersistTestFields.TEXT, "testtext");
        assertThat(entity.getAttribute(PersistTestConstant.PersistTestFields.TEXT).getValue()).isEqualTo("testtext");

        entity.setAttributeValue(PersistTestConstant.PersistTestFields.NUMBER, new BigDecimal(Math.PI));
        assertThat(entity.getAttribute(PersistTestConstant.PersistTestFields.NUMBER).getValue()).isEqualTo(new BigDecimal(Math.PI));

        entity.setAttributeValue(PersistTestConstant.PersistTestFields.DATE, d);
        assertThat(entity.getAttribute(PersistTestConstant.PersistTestFields.DATE).getValue()).isEqualTo(d);

        entity.setAttributeValue(PersistTestConstant.PersistTestFields.DATETIME, dt);
        assertThat(entity.getAttribute(PersistTestConstant.PersistTestFields.DATETIME).getValue()).isEqualTo(dt);

        entity.setAttributeValue(PersistTestConstant.PersistTestFields.FILE, "filename");
        assertThat(entity.getAttribute(PersistTestConstant.PersistTestFields.FILE).getValue()).isEqualTo("filename");

        assertThat(entity.getAttribute(PersistTestConstant.PersistTestFields.REFERENCE)).isNotNull();
        assertThat(((List<EntityAttribute>) entity.getAttribute(PersistTestConstant.PersistTestFields.REFERENCE).getValue()).size()).isEqualTo(0);
    }

    /**
     * Tests create empty entity and set attribute values.
     */
    @Test
    public void test002PersistCreateEmptyEntityNoKey() {
        LocalDate d = LocalDate.parse("2009-09-09");
        LocalDateTime dt = LocalDateTime.parse("2009-09-09T09:09:09");

        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, metadataDao.getSysTimestamp());

        Entity entity = entityService.newEmptyEntity(meta); // no KEY set
        assertThat(entity).isNotNull();
        entity.setAttributeValue(PersistTestConstant.PersistTestFields.DATE, d);
        assertThat(entity.getAttribute(PersistTestConstant.PersistTestFields.DATE).getValue()).isEqualTo(d);

        entity.setAttributeValue(PersistTestConstant.PersistTestFields.DATETIME, dt);
        assertThat(entity.getAttribute(PersistTestConstant.PersistTestFields.DATETIME).getValue()).isEqualTo(dt);
        assertThat(entity.getKey()).isNull();
        entityService.persistEntity(entity);
        assertThat(entity.getKey()).isNotNull();
        assertThat(entity.getKey()).isEqualTo(entity.getHubId().toString());

        entity = entityService.newEmptyEntity(meta); // empty string KEY set
        assertThat(entity).isNotNull();
        entity.setKey("");
        entity.setAttributeValue(PersistTestConstant.PersistTestFields.DATE, d);
        assertThat(entity.getAttribute(PersistTestConstant.PersistTestFields.DATE).getValue()).isEqualTo(d);

        entity.setAttributeValue(PersistTestConstant.PersistTestFields.DATETIME, dt);
        assertThat(entity.getAttribute(PersistTestConstant.PersistTestFields.DATETIME).getValue()).isEqualTo(dt);
        assertThat(entity.getKey()).isEmpty();
        entityService.persistEntity(entity);
        assertThat(entity.getKey()).isNotNull();
        assertThat(entity.getKey()).isEqualTo(entity.getHubId().toString());
    }

    /**
     * Tests create empty entity and set attribute values by wrong types.
     */
    @Test
    public void test002PersistSetEntityWrongValues() {
        final int idate = 20010101;
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, metadataDao.getSysTimestamp());

        Entity entity = entityService.newEmptyEntity(meta);
        assertThat(entity).isNotNull();

        assertThatThrownBy(() -> entity.setAttributeValue(PersistTestConstant.PersistTestFields.BOOLEAN, "truetrue"))
                .isInstanceOf(ClassCastException.class);
        assertThatThrownBy(() -> entity.setAttributeValue(PersistTestConstant.PersistTestFields.STRING, false))
                .isInstanceOf(ClassCastException.class);
        assertThatThrownBy(() -> entity.setAttributeValue(PersistTestConstant.PersistTestFields.TEXT, LocalDate.now()))
                .isInstanceOf(ClassCastException.class);
        assertThatThrownBy(() -> entity.setAttributeValue(PersistTestConstant.PersistTestFields.NUMBER, "testtext"))
                .isInstanceOf(ClassCastException.class);
        assertThatThrownBy(() -> entity.setAttributeValue(PersistTestConstant.PersistTestFields.DATE, new Date()))
                .isInstanceOf(ClassCastException.class);
        assertThatThrownBy(() -> entity.setAttributeValue(PersistTestConstant.PersistTestFields.DATETIME, idate))
                .isInstanceOf(ClassCastException.class);
        assertThatThrownBy(() -> entity.setAttributeValue(PersistTestConstant.PersistTestFields.FILE, SIMLE_INT))
                .isInstanceOf(ClassCastException.class);
        assertThatThrownBy(() -> entity.setAttributeValue(PersistTestConstant.PersistTestFields.REFERENCE, SIMLE_INT))
                .isInstanceOf(ClassCastException.class);
    }

    /**
     * Tests loading from database.
     * Check loading for artificial handmade entity instance
     */
    @Test
    public void test003PersistGetEntity() {
        BigDecimal mainNumber = new BigDecimal(1);
        BigDecimal childNumber = new BigDecimal(2);
        LocalDate mainD = LocalDate.parse("2009-09-09");
        LocalDateTime mainDT = LocalDateTime.parse("2009-09-09T09:09:09");
        LocalDate childD = LocalDate.parse("2008-08-08");
        LocalDateTime childDT = LocalDateTime.parse("2008-08-08T08:08:08");

        LocalDateTime ldts = metadataDao.getSysTimestamp();

        EntityMeta em = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, ldts);

        if (entityService.getEntityIdByKey(em, "T0") == null)
            metadataDao.execute(entityDataForPersist);

        final Entity e = entityService.getEntity(em, "T0", metadataDao.getSysTimestamp());
        assertNotNull(e);

        // check main values
        assertThat(e.getAttributeValue(PersistTestConstant.PersistTestFields.STRING)).isIn("test string", "new string value");
        assertThat(e.getAttributeValue(PersistTestConstant.PersistTestFields.NUMBER)).isEqualTo(mainNumber);
        assertThat(e.getAttributeValue(PersistTestConstant.PersistTestFields.BOOLEAN)).isEqualTo(false);
        assertThat(e.getAttributeValue(PersistTestConstant.PersistTestFields.TEXT)).isEqualTo("test text");
        assertThat(e.getAttributeValue(PersistTestConstant.PersistTestFields.DATE)).isEqualTo(mainD);
        assertThat(e.getAttributeValue(PersistTestConstant.PersistTestFields.DATETIME)).isEqualTo(mainDT);

        assertThatThrownBy(() -> e.getAttributeValue(PersistTestConstant.PersistTestFields.STRINGML))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(((MultilangAttribute) e.getAttribute(PersistTestConstant.PersistTestFields.STRINGML)).getValueEn()).isEqualTo("test string en");
        //assertThat(((MultilangAttribute) e.getAttribute(PersistTestFields.STRINGML)).getValueRu()).isEqualTo("тест строка ru");

        assertThatThrownBy(() -> e.getAttributeValue(PersistTestConstant.PersistTestFields.TEXTML)).isInstanceOf(UnsupportedOperationException.class);
        assertThat(((MultilangAttribute) e.getAttribute(PersistTestConstant.PersistTestFields.TEXTML)).getValueEn()).isEqualTo("test text en");
        //assertThat(((MultilangAttribute) e.getAttribute(PersistTestFields.TEXTML)).getValueRu()).isEqualTo("тест текст ru");

        FileInfoAttribute mainFile = (FileInfoAttribute) e.getAttribute(PersistTestConstant.PersistTestFields.FILE);
        assertThat(mainFile).isNotNull();
        assertThat(mainFile.getValue()).isEqualTo("test-filename.txt");
        assertThat(mainFile.getMimeType()).isEqualTo("text/plain");

        //check link data
        LinkedEntityAttribute<Entity> linkedEntityAttribute = (LinkedEntityAttribute) e.getAttribute(PersistTestConstant.PersistTestFields.REFERENCE);
        assertThat(linkedEntityAttribute).isNotNull();
        assertThat(((List) linkedEntityAttribute.getValue()).size()).isEqualTo(1);

        EntityAttribute ch = linkedEntityAttribute.getValue().get(0);
        assertThat(ch).isNotNull();
        Entity child = (Entity) ch.getEntity();
        assertThat(child).isNotNull();
        assertThat(ch.getValue()).isInstanceOf(BigDecimal.class);
        assertThat(ch.getValue()).isEqualTo(childNumber);

        //check child values
        assertThat(child.getAttributeValue(PersistTestConstant.PersistChildTestFields.STRING)).isEqualTo("TEST_CHILD#STRING");
        assertThat(child.getAttributeValue(PersistTestConstant.PersistChildTestFields.BOOLEAN)).isEqualTo(true);
        assertThat(child.getAttributeValue(PersistTestConstant.PersistChildTestFields.TEXT)).isEqualTo("TEST_CHILD#TEXT");
        assertThat(child.getAttributeValue(PersistTestConstant.PersistChildTestFields.NUMBER)).isEqualTo(childNumber);
        assertThat(child.getAttributeValue(PersistTestConstant.PersistChildTestFields.DATE)).isEqualTo(childD);
        assertThat(child.getAttributeValue(PersistTestConstant.PersistChildTestFields.DATETIME)).isEqualTo(childDT);

        assertThatThrownBy(() -> child.getAttributeValue(
                PersistTestConstant.PersistChildTestFields.STRINGML)).isInstanceOf(UnsupportedOperationException.class);
        assertThat(((MultilangAttribute) child.getAttribute(PersistTestConstant.PersistChildTestFields.STRINGML)).getValueRu())
                .isEqualTo("дочерняя тест строка ru");
        assertThat(((MultilangAttribute) child.getAttribute(
                PersistTestConstant.PersistChildTestFields.STRINGML)).getValueEn()).isEqualTo("child test string en");

        assertThatThrownBy(() -> child.getAttributeValue(
                PersistTestConstant.PersistChildTestFields.TEXTML)).isInstanceOf(UnsupportedOperationException.class);
        assertThat(((MultilangAttribute) child.getAttribute(PersistTestConstant.PersistChildTestFields.TEXTML)).getValueRu())
                .isEqualTo("дочерний тест текст ru");
        assertThat(((MultilangAttribute) child.getAttribute(
                PersistTestConstant.PersistChildTestFields.TEXTML)).getValueEn()).isEqualTo("child test text en");

        FileInfoAttribute childFile = (FileInfoAttribute) child.getAttribute(PersistTestConstant.PersistChildTestFields.FILE);
        assertThat(childFile).isNotNull();
        assertThat(childFile.getValue()).isEqualTo("child-text-filename.txt");
        assertThat(childFile.getMimeType()).isEqualTo("text/plain");
    }

    /**
     * Tests entity existed entity insert/update.
     */
    @Test
    public void test004x01PersistPersistsEntity() {
        // load existed entity 1
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, ldts);
        Entity e = entityService.getEntity(meta, "T0", ldts);
        assertThat(e).isNotNull();

        // change not change intable values and persists
        entityService.persistEntity(e);

        // change intable and multilang values and persists
        e.setAttributeValue(PersistTestConstant.PersistTestFields.STRING, "new string value");
        entityService.persistEntity(e);
    }

    /**
     * Tests entity new save then read data to other instance of object and compare.
     * compare intable and multiland attributes
     */
    @Test
    public void test004x02PersistPersistsEntityAndRetrieve() {
        BigDecimal mainNumber = new BigDecimal(1);
        LocalDate mainD = LocalDate.parse("2009-09-09");
        LocalDateTime mainDT = LocalDateTime.parse("2009-09-09T09:09:09");

        String key = "T3";
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        // load existed entity 1
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, ldts);
        Entity entity = entityService.getEntity(meta, key, ldts);
        if (entity == null) {
            entity = entityService.newEmptyEntity(meta);
            assertThat(entity).isNotNull();
            entity.setKey(key);
        }

        // don't change none of values and persists FOR FIRST version
        entityService.persistEntity(entity); // EMPTY
        // change intable and multilang values and persists again FOR SECOND version
        entity.setAttributeValue(PersistTestConstant.PersistTestFields.STRING, "second T3 string value");
        MultilangAttribute mlAttr = (MultilangAttribute) entity
                .getAttribute(PersistTestConstant.PersistTestFields.STRINGML); // internally create attribute and add empty attr to entity
        ((MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistTestFields.STRINGML)).setValueRu("2я  строка T3");
        ((MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistTestFields.STRINGML)).setValueEn("2d string T3");
        mlAttr = (MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistTestFields.TEXTML);
        ((MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistTestFields.TEXTML)).setValueRu("2й  текст T3");
        ((MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistTestFields.TEXTML)).setValueEn("2d  text value T3");
        entityService.persistEntity(entity);

        // change intable and multilang values and persists for THIRD version
        entity.setAttributeValue(PersistTestConstant.PersistTestFields.STRING, "third test string T3");
        entity.setAttributeValue(PersistTestConstant.PersistTestFields.BOOLEAN, true);
        entity.setAttributeValue(PersistTestConstant.PersistTestFields.TEXT, "third test text T3");
        entity.setAttributeValue(PersistTestConstant.PersistTestFields.NUMBER, mainNumber);
        entity.setAttributeValue(PersistTestConstant.PersistTestFields.DATE, mainD);
        entity.setAttributeValue(PersistTestConstant.PersistTestFields.DATETIME, mainDT);
        ((MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistTestFields.STRINGML)).setValueRu("3я T3 ml строка");
        ((MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistTestFields.STRINGML)).setValueEn("3d T3 string ml value");
        ((MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistTestFields.TEXTML)).setValueRu("3й  текст ml T3");
        ((MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistTestFields.TEXTML)).setValueEn("3d  text value ml T3");
        entityService.persistEntity(entity);

        // read data for same key (last version) to other object and compare values with original
        Entity check = entityService.getEntity(meta, key, metadataDao.getSysTimestamp());
        assertThat(check).isNotNull();
        assertThat(check.getAttributeValue(PersistTestConstant.PersistTestFields.STRING)).isEqualTo(entity.getAttributeValue(
                PersistTestConstant.PersistTestFields.STRING));
        assertThat(check.getAttributeValue(PersistTestConstant.PersistTestFields.BOOLEAN)).isEqualTo(entity.getAttributeValue(
                PersistTestConstant.PersistTestFields.BOOLEAN));
        assertThat(check.getAttributeValue(PersistTestConstant.PersistTestFields.TEXT)).isEqualTo(entity.getAttributeValue(
                PersistTestConstant.PersistTestFields.TEXT));
        assertThat(check.getAttributeValue(PersistTestConstant.PersistTestFields.NUMBER)).isEqualTo(entity.getAttributeValue(
                PersistTestConstant.PersistTestFields.NUMBER));
        assertThat(check.getAttributeValue(PersistTestConstant.PersistTestFields.DATE)).isEqualTo(entity.getAttributeValue(
                PersistTestConstant.PersistTestFields.DATE));
        assertThat(check.getAttributeValue(PersistTestConstant.PersistTestFields.DATETIME)).isEqualTo(entity.getAttributeValue(
                PersistTestConstant.PersistTestFields.DATETIME));
        assertThat(((MultilangAttribute) check.getAttribute(PersistTestConstant.PersistTestFields.STRINGML)).getValueRu())
                .isEqualTo(((MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistTestFields.STRINGML)).getValueRu());
        assertThat(((MultilangAttribute) check.getAttribute(PersistTestConstant.PersistTestFields.STRINGML)).getValueEn())
                .isEqualTo(((MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistTestFields.STRINGML)).getValueEn());
        assertThat(((MultilangAttribute) check.getAttribute(PersistTestConstant.PersistTestFields.TEXTML)).getValueRu())
                .isEqualTo(((MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistTestFields.TEXTML)).getValueRu());
        assertThat(((MultilangAttribute) check.getAttribute(PersistTestConstant.PersistTestFields.TEXTML)).getValueEn())
                .isEqualTo(((MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistTestFields.TEXTML)).getValueEn());
    }

    /**
     * Tests entity new insert/update.
     * Just check that DSQL statements executed without errors
     * including for external multilink attributes
     */
    @Test
    public void test004x03PersistPersistsNewEntity() {
        String key = "T2";
        // load existed entity 1
        LocalDateTime ldts = metadataDao.getSysTimestamp();

        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, ldts);
        Entity entity = entityService.getEntity(meta, key, ldts);
        if (entity == null)
            entity = entityService.newEmptyEntity(meta);
        assertThat(entity).isNotNull();

        // change not change intable values and persists empty entity
        entity.setKey(key);
        entityService.persistEntity(entity);

        // change intable and multilang values and persists again
        entity.setAttributeValue(PersistTestConstant.PersistTestFields.STRING, "new T2 string value");
        ((MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistTestFields.STRINGML)).setValueRu("новая T2 многояз строка");
        ((MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistTestFields.STRINGML)).setValueEn("new T2 ml string");
        entityService.persistEntity(entity);
    }

    /**
     * Tests entity removing.
     */
    @Test
    public void test005PersistRemoveEntity() {
        String key = "T3";
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        // load existed entity 1
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, ldts);
        Entity entity = entityService.getEntity(meta, key, ldts);
        if (entity != null) {
            entityService.removeEntity(entity);
        }
    }

    /**
     * Tests entity creation for paging test.
     */
    @Test
    public void test006PersistPageEntity() {
        final int pageSize = 5;
        final int firstPageEndRow = 5;
        final int secondPageStartRow = 6;
        final int secondPageEndRow = TEST_ENTITIES_TO_CREATE;
        String baseKey = "T4";
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, ldts);
        assertThat(meta).isNotNull();

        createTestData(baseKey, meta, TEST_ENTITIES_TO_CREATE);

        ldts = metadataDao.getSysTimestamp();
        RowRange rowRange = RowRange.newAsPageAndSize(0, pageSize);
        assertThat(rowRange.getStartRow()).isEqualTo(1);
        assertThat(rowRange.getEndRow()).isEqualTo(firstPageEndRow);
        List<Entity> list = (List<Entity>) entityService.getEntities(meta, null, rowRange, ldts);
        assertThat(list.size()).isEqualTo(pageSize);
        assertThat(rowRange.getTotalCount()).isNotNull();
        Long realTotal = getTotalAliveTestEntities();
        assertThat(rowRange.getTotalCount()).isEqualTo(realTotal);

        rowRange = RowRange.newAsPageAndSize(1, pageSize);
        assertThat(rowRange.getStartRow()).isEqualTo(secondPageStartRow);
        assertThat(rowRange.getEndRow()).isEqualTo(secondPageEndRow);
        List<Entity> list2 = (List<Entity>) entityService.getEntities(meta, null, rowRange, ldts);
        assertThat(list2.size()).isEqualTo(pageSize);
        assertThat(rowRange.getTotalCount()).isNotNull();
        assertThat(rowRange.getTotalCount()).isEqualTo(realTotal);
    }

    /**
     * Tests entity creation for paging test.
     */
    @Test
    public void test007PersistFilterEntity() {
        final int resultSize = 2;
        final int pageSize = 3;
        String baseKey = "T4";
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, ldts);
        assertThat(meta).isNotNull();

        createTestData(baseKey, meta, TEST_ENTITIES_TO_CREATE);

        ldts = metadataDao.getSysTimestamp();
        Criteria criteria = new Criteria();
        Where where = criteria.getWhere();
        where.addItem(new WhereItem(meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.STRING), Operator.IN, "test string T40",
                                    "test string T41"));

        List<Entity> list = (List<Entity>) entityService.getEntities(meta, criteria, null, ldts);
        assertThat(list.size()).isEqualTo(resultSize);

        Criteria criteria2 = new Criteria();
        Where where2 = criteria2.getWhere();
        where2.addItem(new WhereItem(meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.STRING), Operator.LIKE, "test string T4"));
        list = (List<Entity>) entityService.getEntities(meta, criteria2, RowRange.newAsPageAndSize(0, pageSize), ldts);
        assertThat(list.size()).isEqualTo(pageSize);
        list = (List<Entity>) entityService.getEntities(meta, criteria2, RowRange.newAsPageAndSize(1, pageSize), ldts);
        assertThat(list.size()).isEqualTo(pageSize);
        list = (List<Entity>) entityService.getEntities(meta, criteria2, RowRange.newAsPageAndSize(2, pageSize), ldts);
        assertThat(list.size()).isEqualTo(pageSize);

        list = (List<Entity>) entityService.getEntities(meta, criteria2, RowRange.newAsShiftAndSize(0, pageSize), ldts);
        assertThat(list.size()).isEqualTo(pageSize);

        // upper case search test
        Criteria criteria3 = new Criteria();
        Where where3 = criteria2.getWhere();
        where3.addItem(new WhereItem(meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.STRING), Operator.LIKE, "TEST sTrInG T4"));
        list = (List<Entity>) entityService.getEntities(meta, criteria2, RowRange.newAsPageAndSize(0, pageSize), ldts);
        assertThat(list.size()).isEqualTo(pageSize);
        list = (List<Entity>) entityService.getEntities(meta, criteria2, RowRange.newAsPageAndSize(1, pageSize), ldts);
        assertThat(list.size()).isEqualTo(pageSize);
        list = (List<Entity>) entityService.getEntities(meta, criteria2, RowRange.newAsPageAndSize(2, pageSize), ldts);
        assertThat(list.size()).isEqualTo(pageSize);
    }

    /**
     * Tests entity filter and sorting.
     */
    @Test
    public void test008PersistOrderEntity() {
        final int resultSize = 2;
        String baseKey = "T4";
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, ldts);
        assertThat(meta).isNotNull();

        createTestData(baseKey, meta, TEST_ENTITIES_TO_CREATE);

        ldts = metadataDao.getSysTimestamp();
        Criteria criteria = new Criteria();
        Where where = criteria.getWhere();
        Order order = criteria.getOrder();
        // test empty where - order
        List<Entity> list = (List<Entity>) entityService.getEntities(meta, criteria, null, ldts);
        assertThat(list).isNotNull();

        order.addItem(meta.getKeyAttribute(), false);
        order.addItem(meta.getHubLdtsAttribute(), true);
        where.addItem(new WhereItem(meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.STRING), Operator.IN, "test string T40",
                                    "test string T41"));

        list = (List<Entity>) entityService.getEntities(meta, criteria, null, ldts);
        assertThat(list.size()).isEqualTo(resultSize);
        assertThat(list.get(0).getKey()).isEqualTo("T40");
    }

    /**
     * Tests entity filter and sorting.
     */
    @Test
    public void test0081PersistWhereClobEntity() {
        String baseKey = "T4";
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, ldts);
        assertThat(meta).isNotNull();

        createTestData(baseKey, meta, TEST_ENTITIES_TO_CREATE);

        ldts = metadataDao.getSysTimestamp();
        Criteria criteria = new Criteria();
        Where where = criteria.getWhere();
        where.addItem(new WhereItem(meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.TEXT), Operator.LIKE, "ttt"));
        where.addItem(new WhereItem(Conjunction.AND, meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.TEXT), Operator.EQ, "ttt"));
        List<Entity> list = (List<Entity>) entityService.getEntities(meta, criteria, null, ldts);
        assertThat(list.size()).isEqualTo(0);
    }

    /**
     * Tests entity creation for paging test.
     */
    @Test
    public void test009PersistFilterEntityWithPage() {
        final int resultSize = 2;
        final int pageSize = 3;
        String baseKey = "T4";
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, ldts);
        assertThat(meta).isNotNull();

        createTestData(baseKey, meta, TEST_ENTITIES_TO_CREATE);

        ldts = metadataDao.getSysTimestamp();
        Criteria criteria = new Criteria();
        Where where = criteria.getWhere();
        where.addItem(new WhereItem(meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.STRING), Operator.LIKE, "test string T4"));
        where.addItem(new WhereItem(meta.getKeyAttribute(), Operator.LIKE, "T4"));
        Order order = criteria.getOrder();
        order.addItem(new OrderItem(meta.getKeyAttribute()));

        List<Entity> list = (List<Entity>) entityService.getEntities(meta, criteria, RowRange.newAsPageAndSize(0, pageSize), ldts);
        assertThat(list.size()).isEqualTo(pageSize);
        assertThat(list.get(0).getKey()).isEqualTo("T40");
        list = (List<Entity>) entityService.getEntities(meta, criteria, RowRange.newAsPageAndSize(1, pageSize), ldts);
        assertThat(list.size()).isEqualTo(pageSize);
        assertThat(list.get(0).getKey()).isEqualTo("T43");
        list = (List<Entity>) entityService.getEntities(meta, criteria, RowRange.newAsPageAndSize(2, pageSize), ldts);
        assertThat(list.size()).isEqualTo(pageSize);
        assertThat(list.get(0).getKey()).isEqualTo("T46");
    }

    /**
     * Tests entity creation for reference read/write test.
     */
    @Test
    public void test010PersistReferenceAttribute() {
        LocalDateTime ldts = metadataDao.getSysTimestamp();

        String baseKey = "T4";
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, ldts);
        assertThat(meta).isNotNull();
        createTestData(baseKey, meta, TEST_ENTITIES_TO_CREATE);

        String baseChildKey = "Tch4";
        EntityMeta metaChild = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_TEST_CHILD_META_KEY, ldts);
        assertThat(metaChild).isNotNull();
        createTestDataChild(baseChildKey, metaChild, TEST_ENTITIES_TO_CREATE);

        ldts = metadataDao.getSysTimestamp();
        Entity parent = entityService.getEntity(meta, "T40", ldts);
        assertThat(parent).isNotNull();
        Entity child40 = entityService.getEntity(metaChild, "Tch40", ldts);
        assertThat(child40).isNotNull();

        // add Tch40
        LinkedEntityAttribute<Entity> linkedEntityAttribute = (LinkedEntityAttribute) parent
                .getAttribute(PersistTestConstant.PersistTestFields.REFERENCE);
        if (!linkedEntityAttribute.isExists(child40))
            linkedEntityAttribute.addAttribute(new EntityAttribute(linkedEntityAttribute.getMeta(), child40));
        entityService.persistEntity(parent);

        // add Tch41
        Entity child41 = entityService.getEntity(metaChild, "Tch41", ldts);
        assertThat(child41).isNotNull();
        if (!linkedEntityAttribute.isExists(child41))
            linkedEntityAttribute.addAttribute(new EntityAttribute(linkedEntityAttribute.getMeta(), child41));
        entityService.persistEntity(parent);

        // recheck from database
        ldts = metadataDao.getSysTimestamp();
        Entity check = entityService.getEntity(meta, "T40", ldts);
        linkedEntityAttribute = (LinkedEntityAttribute) check.getAttribute(PersistTestConstant.PersistTestFields.REFERENCE);
        assertThat(linkedEntityAttribute).isNotNull();
        assertThat(linkedEntityAttribute.getValue().size()).isEqualTo(2);

        //remove Tch40 link;
        linkedEntityAttribute = (LinkedEntityAttribute) parent.getAttribute(PersistTestConstant.PersistTestFields.REFERENCE);
        linkedEntityAttribute.remove(child40);
        assertThat(linkedEntityAttribute.getValue().size()).isEqualTo(1);
        assertThat(linkedEntityAttribute.getValue().get(0).getEntity().getKey()).isEqualToIgnoringCase("Tch41");
        entityService.persistEntity(parent);

        // recheck from database
        ldts = metadataDao.getSysTimestamp();
        check = entityService.getEntity(meta, "T40", ldts);
        linkedEntityAttribute = (LinkedEntityAttribute) parent.getAttribute(PersistTestConstant.PersistTestFields.REFERENCE);
        assertThat(linkedEntityAttribute).isNotNull();
        assertThat(linkedEntityAttribute.getValue().size()).isEqualTo(1);
        assertThat(linkedEntityAttribute.getValue().get(0).getEntity().getKey()).isEqualToIgnoringCase("Tch41");

        // remove Tch41 object
        ldts = metadataDao.getSysTimestamp();
        Entity tch41 = entityService.getEntity(metaChild, "Tch41", ldts);
        entityService.removeEntity(tch41);
        ldts = metadataDao.getSysTimestamp();
        check = entityService.getEntity(meta, "T40", ldts);
        linkedEntityAttribute = (LinkedEntityAttribute) check.getAttribute(PersistTestConstant.PersistTestFields.REFERENCE);
        assertThat(linkedEntityAttribute).isNotNull();
        assertThat(linkedEntityAttribute.getValue().size()).isEqualTo(0);
    }

    /**
     * Tests entity creation for reference read/write test.
     * Test this
     * T400
     * add Tch400
     * add Tch401
     * add Tch403
     * pers
     * read T400
     * remove Tch400
     * add Tch405
     * pers
     * read T400 - check Tch41,42,43,45
     */
    @Test
    public void test010x1PersistReferenceAttribute() {
        final int childSize = 4;
        LocalDateTime ldts = metadataDao.getSysTimestamp();

        String baseKey = "T40";
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, ldts);
        assertThat(meta).isNotNull();
        createTestData(baseKey, meta, 1);

        String baseChildKey = "TCH40";
        EntityMeta metaChild = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_TEST_CHILD_META_KEY, ldts);
        assertThat(metaChild).isNotNull();
        createTestDataChild(baseChildKey, metaChild, childSize);

        ldts = metadataDao.getSysTimestamp();
        Entity parent400 = entityService.getEntity(meta, "T400", ldts);
        assertThat(parent400).isNotNull();
        Entity child400 = entityService.getEntity(metaChild, "TCH400", ldts);
        assertThat(child400).isNotNull();
        Entity child401 = entityService.getEntity(metaChild, "TCH401", ldts);
        assertThat(child401).isNotNull();
        Entity child402 = entityService.getEntity(metaChild, "TCH402", ldts);
        assertThat(child402).isNotNull();
        LinkedEntityAttribute beforeAttribute = (LinkedEntityAttribute) parent400.getAttribute(PersistTestConstant.PersistTestFields.REFERENCE2);
        beforeAttribute.add(child400);
        beforeAttribute.add(child401);
        beforeAttribute.add(child402);
        List<EntityAttribute> beforeList = (List<EntityAttribute>) parent400.getAttributeValue(PersistTestConstant.PersistTestFields.REFERENCE2);
        entityService.persistEntity(parent400);

        parent400 = entityService.getEntity(meta, "T400", null);
        LinkedEntityAttribute currentAttribute = (LinkedEntityAttribute) parent400.getAttribute(PersistTestConstant.PersistTestFields.REFERENCE2);
        List<EntityAttribute> currentList = (List<EntityAttribute>) parent400.getAttributeValue(PersistTestConstant.PersistTestFields.REFERENCE2);
        assertThat(currentList).isNotEmpty().hasSameSizeAs(beforeList);
        assertThat(currentList).containsExactly(beforeList.toArray(new EntityAttribute[] {})); // TCH400  TCH401  TCH402
        assertThat(currentList.stream().map(ea -> ea.getValue()).collect(Collectors.toList())).containsExactly(
                (Object[]) beforeList.stream().map(ea -> ea.getValue()).collect(Collectors.toList()).toArray(new String[] {}));
        for (EntityAttribute ea : currentList) {
            assertThat(ea.getValue()).as("check child entity %s", ea.getEntity().getKey())
                                     .isEqualTo("test string " + ea.getEntity().getKey());
        }

        currentAttribute.remove(child400);
        Entity child403 = entityService.getEntity(metaChild, "TCH403", ldts);
        assertThat(child403).isNotNull();
        currentAttribute.add(child403);
        beforeAttribute = currentAttribute;
        beforeList = currentList;
        entityService.persistEntity(parent400);

        parent400 = entityService.getEntity(meta, "T400", null);
        currentAttribute = (LinkedEntityAttribute) parent400.getAttribute(PersistTestConstant.PersistTestFields.REFERENCE2);
        currentList = (List<EntityAttribute>) parent400.getAttributeValue(PersistTestConstant.PersistTestFields.REFERENCE2);
        assertThat(currentList).isNotEmpty().hasSameSizeAs(beforeList);
        assertThat(currentList).containsExactly(beforeList.toArray(new EntityAttribute[] {})); // TCH401  TCH402 TCH403
        assertThat(currentList.stream().map(ea -> ea.getValue()).collect(Collectors.toList())).containsExactly(
                (Object[]) beforeList.stream().map(ea -> ea.getValue()).collect(Collectors.toList()).toArray(new String[] {}));
        for (EntityAttribute ea : currentList) {
            assertThat(ea.getValue()).as("check child entity %s", ea.getEntity().getKey())
                                     .isEqualTo("test string " + ea.getEntity().getKey());
        }
    }

    /**
     * Tests entity creation for reference read/write test.
     * Test this
     * T500
     * add Tch500
     * add Tch501
     * add Tch503
     * pers
     * T501
     * add Tch500
     * add Tch501
     * add Tch503
     * pers
     * [] read IN T500,T501
     * for e : parent - check Tch500,501,503
     */
    @Test
    public void test010x2PersistReferenceAttribute() {
        final int childSize = 4;
        LocalDateTime ldts = metadataDao.getSysTimestamp();

        String baseKey = "T50";
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, ldts);
        assertThat(meta).isNotNull();
        createTestData(baseKey, meta, 2);

        String baseChildKey = "TCH50";
        EntityMeta metaChild = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_TEST_CHILD_META_KEY, ldts);
        assertThat(metaChild).isNotNull();
        createTestDataChild(baseChildKey, metaChild, childSize);

        ldts = metadataDao.getSysTimestamp();
        Entity parent500 = entityService.getEntity(meta, "T500", ldts);
        assertThat(parent500).isNotNull();
        Entity child500 = entityService.getEntity(metaChild, "TCH500", ldts);
        assertThat(child500).isNotNull();
        Entity child501 = entityService.getEntity(metaChild, "TCH501", ldts);
        assertThat(child501).isNotNull();
        Entity child502 = entityService.getEntity(metaChild, "TCH502", ldts);
        assertThat(child502).isNotNull();
        Entity child503 = entityService.getEntity(metaChild, "TCH503", ldts);
        assertThat(child503).isNotNull();
        LinkedEntityAttribute beforeAttribute500 = (LinkedEntityAttribute) parent500.getAttribute(PersistTestConstant.PersistTestFields.REFERENCE2);
        beforeAttribute500.add(child500);
        beforeAttribute500.add(child501);
        beforeAttribute500.add(child502);
        List<EntityAttribute> beforeList500 = (List<EntityAttribute>) parent500.getAttributeValue(PersistTestConstant.PersistTestFields.REFERENCE2);
        entityService.persistEntity(parent500);

        Entity parent501 = entityService.getEntity(meta, "T501", ldts);
        LinkedEntityAttribute beforeAttribute501 = (LinkedEntityAttribute) parent501.getAttribute(PersistTestConstant.PersistTestFields.REFERENCE2);
        beforeAttribute501.add(child500);
        beforeAttribute501.add(child501);
        beforeAttribute501.add(child503);
        List<EntityAttribute> beforeList501 = (List<EntityAttribute>) parent501.getAttributeValue(PersistTestConstant.PersistTestFields.REFERENCE2);
        entityService.persistEntity(parent501);

        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(meta.getKeyAttribute(), Operator.IN, "T500", "T501"));
        List<Entity> parents = (List<Entity>) entityService.getEntities(meta, criteria, null, null);
        assertThat(parents).isNotEmpty().hasSize(2);

        parent500 = parents.stream().filter(e -> e.getKey().equals("T500")).collect(CollectorUtils.singletonCollector());
        assertThat(parent500).isNotNull();
        LinkedEntityAttribute currentAttribute500 = (LinkedEntityAttribute) parent500.getAttribute(PersistTestConstant.PersistTestFields.REFERENCE2);
        List<EntityAttribute> currentList500 = (List<EntityAttribute>) parent500.getAttributeValue(PersistTestConstant.PersistTestFields.REFERENCE2);
        assertThat(currentList500).isNotEmpty().hasSameSizeAs(beforeList500);
        assertThat(currentList500).containsExactly(beforeList500.toArray(new EntityAttribute[] {})); //  Tch500  Tch501 Tch502
        assertThat(currentList500.stream().map(ea -> ea.getValue()).collect(Collectors.toList())).containsExactly(
                (Object[]) beforeList500.stream().map(ea -> ea.getValue()).collect(Collectors.toList()).toArray(new String[] {}));
        for (EntityAttribute ea : currentList500) {
            assertThat(ea.getValue()).as("check child entity %s", ea.getEntity().getKey())
                                     .isEqualTo("test string " + ea.getEntity().getKey());
        }

        parent501 = parents.stream().filter(e -> e.getKey().equals("T501")).collect(CollectorUtils.singletonCollector());
        assertThat(parent501).isNotNull();
        LinkedEntityAttribute currentAttribute501 = (LinkedEntityAttribute) parent501.getAttribute(PersistTestConstant.PersistTestFields.REFERENCE2);
        List<EntityAttribute> currentList501 = (List<EntityAttribute>) parent501.getAttributeValue(PersistTestConstant.PersistTestFields.REFERENCE2);
        assertThat(currentList501).isNotEmpty().hasSameSizeAs(beforeList501);
        assertThat(currentList501).containsExactly(beforeList501.toArray(new EntityAttribute[] {})); //  TCH500  TCH501 TCH502
        assertThat(currentList501.stream().map(ea -> ea.getValue()).collect(Collectors.toList())).containsExactly(
                (Object[]) beforeList501.stream().map(ea -> ea.getValue()).collect(Collectors.toList()).toArray(new String[] {}));
        for (EntityAttribute ea : currentList501) {
            assertThat(ea.getValue()).as("check child entity %s", ea.getEntity().getKey())
                                     .isEqualTo("test string " + ea.getEntity().getKey());
        }
    }

    /**
     * Test add child only.
     */
    @Test
    public void test010x3PersistChildren() {
        final int objCount = 4;
        String baseKey = "T0103";
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, null);
        assertThat(meta).isNotNull();
        createTestData(baseKey, meta, objCount);
        Entity parent = entityService.getEntity(meta, "T01030", null);
        assertThat(parent).isNotNull();
        Entity child1 = entityService.getEntity(meta, "T01031", null);
        assertThat(child1).isNotNull();
        Entity child2 = entityService.getEntity(meta, "T01032", null);
        assertThat(child2).isNotNull();
        Entity child3 = entityService.getEntity(meta, "T01033", null);
        assertThat(child3).isNotNull();
        LinkedEntityAttribute ch = parent.getChildrenReferenceAttribute();
        ch.add(child1);
        ch.add(child2);
        ch.add(child3);
        entityService.persistEntity(parent);

        Entity check = entityService.getEntity(meta, "T01030", null);
        assertThat(check).isNotNull();
        ch = check.getChildrenReferenceAttribute();
        assertThat(ch.getEntityAttributeList()).hasSize(objCount - 1);
        assertThat(ch.getEntityList()).hasSize(0);
        entityService.loadEntityChildren(check, null);
        assertThat(ch.getEntityList()).containsExactlyInAnyOrder(child1, child2, child3);
    }

    /**
     * Test add child and references.
     */
    @Test
    public void test010x4PersistReferenceAndChildren() {
        final int objCount = 3;
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, null);
        assertThat(meta).isNotNull();
        EntityMeta metaChild = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_TEST_CHILD_META_KEY, null);
        assertThat(metaChild).isNotNull();

        String baseKey = "T0104";
        createTestData(baseKey, meta, objCount);
        String baseChildKey = "TCH0104";
        createTestDataChild(baseChildKey, metaChild, 2);

        Entity parent = entityService.getEntity(meta, "T01040", null);
        assertThat(parent).isNotNull();
        Entity child1 = entityService.getEntity(meta, "T01041", null);
        assertThat(child1).isNotNull();
        Entity child2 = entityService.getEntity(meta, "T01042", null);
        assertThat(child2).isNotNull();
        LinkedEntityAttribute ch = parent.getChildrenReferenceAttribute();
        ch.add(child1);
        ch.add(child2);

        Entity baby1 = entityService.getEntity(metaChild, "TCH01040", null);
        assertThat(baby1).isNotNull();
        Entity baby2 = entityService.getEntity(metaChild, "TCH01041", null);
        assertThat(baby2).isNotNull();
        LinkedEntityAttribute b = (LinkedEntityAttribute) parent.getAttribute(PersistTestConstant.PersistTestFields.REFERENCE2);
        b.add(baby1);
        b.add(baby2);
        entityService.persistEntity(parent);

        Entity check = entityService.getEntity(meta, "T01040", null);
        assertThat(check).isNotNull();
        ch = check.getChildrenReferenceAttribute();
        assertThat(ch.getEntityAttributeList()).hasSize(2);
        assertThat(ch.getEntityList()).hasSize(0);
        entityService.loadEntityChildren(check, null);
        assertThat(ch.getEntityList()).containsExactlyInAnyOrder(child1, child2);
        b = (LinkedEntityAttribute) check.getAttribute(PersistTestConstant.PersistTestFields.REFERENCE2);
        assertThat(b.getEntityList()).containsExactlyInAnyOrder(baby1, baby2);
    }

    /**
     * Test add child only.
     */
    @Test
    public void test010x5PersistChildrenAndRemove() {
        final int objCount = 4;
        String baseKey = "T0105";
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, null);
        assertThat(meta).isNotNull();
        createTestData(baseKey, meta, objCount);
        Entity parent = entityService.getEntity(meta, "T01050", null);
        assertThat(parent).isNotNull();
        Entity child1 = entityService.getEntity(meta, "T01051", null);
        assertThat(child1).isNotNull();
        Entity child2 = entityService.getEntity(meta, "T01052", null);
        assertThat(child2).isNotNull();
        Entity child3 = entityService.getEntity(meta, "T01053", null);
        assertThat(child3).isNotNull();
        LinkedEntityAttribute ch = parent.getChildrenReferenceAttribute();
        ch.add(child1);
        ch.add(child2);
        ch.add(child3);
        entityService.persistEntity(parent);

        Entity check = entityService.getEntity(meta, "T01050", null);
        assertThat(check).isNotNull();
        ch = check.getChildrenReferenceAttribute();
        assertThat(ch.getEntityAttributeList()).hasSize(objCount - 1);
        assertThat(ch.getEntityList()).hasSize(0);
        entityService.loadEntityChildren(check, null);
        assertThat(ch.getEntityList()).containsExactlyInAnyOrder(child1, child2, child3);

        ch.remove(child3);
        entityService.persistEntity(check);
        check = entityService.getEntity(meta, "T01050", null);
        assertThat(check).isNotNull();
        ch = check.getChildrenReferenceAttribute();
        assertThat(ch.getEntityAttributeList()).hasSize(objCount - 2);
        assertThat(ch.getEntityList()).hasSize(0);
        entityService.loadEntityChildren(check, null);
        assertThat(ch.getEntityList()).containsExactlyInAnyOrder(child1, child2);
    }

    /**
     * Test query with child parent attributes.
     */
    @Test
    public void test010x6QuerySelfReference() {
        final int objCount = 4;
        String baseKey = "T0106";
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, null);
        assertThat(meta).isNotNull();
        createTestData(baseKey, meta, objCount);
        Entity p1 = entityService.getEntity(meta, "T01060", null);
        assertThat(p1).isNotNull();
        Entity ch1 = entityService.getEntity(meta, "T01061", null);
        assertThat(ch1).isNotNull();
        p1.getChildrenReferenceAttribute().add(ch1);
        entityService.persistEntity(p1);

        Entity p2 = entityService.getEntity(meta, "T01062", null);
        assertThat(p2).isNotNull();
        Entity ch2 = entityService.getEntity(meta, "T01063", null);
        assertThat(ch2).isNotNull();
        p2.getChildrenReferenceAttribute().add(ch2);
        entityService.persistEntity(p2);

        // has child (T01063)
        Criteria criteria = new Criteria();
        Where where = criteria.getWhere();
        where.addItem(new WhereItem(meta.getKeyAttribute(), Operator.IN, "T01060", "T01061", "T01062", "T01063"));
        where.addReferenceItem(meta.getChildrenReferenceAttribute(), new WhereItem(meta.getKeyAttribute(), Operator.EQ, "T01063"));
        List<Entity> check = (List<Entity>) entityService.getEntities(meta, criteria, null, null);
        assertThat(check).containsExactlyInAnyOrder(p2);

        // has parent
        criteria = new Criteria();
        where = criteria.getWhere();
        where.addItem(new WhereItem(meta.getKeyAttribute(), Operator.IN, "T01060", "T01061", "T01062", "T01063"));
        where.setReferenceExists(meta.getParentReferenceAttribute(), Conjunction.AND);
        check = (List<Entity>) entityService.getEntities(meta, criteria, null, null);
        assertThat(check).containsExactlyInAnyOrder(ch1, ch2);

        // has no parent
        criteria = new Criteria();
        where = criteria.getWhere();
        where.addItem(new WhereItem(meta.getKeyAttribute(), Operator.IN, "T01060", "T01061", "T01062", "T01063"));
        where.setReferenceExists(meta.getParentReferenceAttribute(), Conjunction.AND_NOT);
        check = (List<Entity>) entityService.getEntities(meta, criteria, null, null);
        assertThat(check).containsExactlyInAnyOrder(p1, p2);
    }

    /**
     * Test parent of entity loading.
     */
    @Test
    public void test010x7ParentAndChild() {
        final int objCount = 3;
        String baseKey = "T0107";
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, null);
        assertThat(meta).isNotNull();
        createTestData(baseKey, meta, objCount);

        // T01070 T01071 parent of T01072
        Entity e1 = entityService.getEntity(meta, "T01070", null);
        Entity e2 = entityService.getEntity(meta, "T01071", null);
        Entity e3 = entityService.getEntity(meta, "T01072", null);
        e1.getChildrenReferenceAttribute().add(e3);
        e2.getChildrenReferenceAttribute().add(e3);
        entityService.persistEntity(e1);
        entityService.persistEntity(e2);

        e3 = entityService.getEntity(meta, "T01072", null);
        assertThat(e3.isParentExists()).isTrue();
        entityService.loadEntityParent(e3, null);
        assertThat(e3.getParentReferenceAttribute().getEntityList()).containsExactlyInAnyOrder(e1, e2);

        // remove e3 as child at parent e2
        e2 = e3.getParentReferenceAttribute().getEntityList().stream().filter((e) -> e.getKey().equals("T01071")).findAny().get();
        assertThat(e2).isNotNull();
        entityService.loadEntityChildren(e2, null);
        assertThat(e2.getChildrenReferenceAttribute().getEntityList()).containsExactlyInAnyOrder(e3);
        e2.getChildrenReferenceAttribute().getEntityList().remove(e3);
        entityService.persistEntity(e2);

        e2 = entityService.getEntity(meta, "T01071", null);
        assertThat(e2.getChildrenReferenceAttribute().getEntityAttributeList()).hasSize(0);
    }

    /**
     * Tests entity read save without intable attributes.
     */
    @Test
    public void test011PersistNoIntable() {
        String baseKey = "NT0";
        EntityMeta meta = createEntityMeta(entityMetaForPersistNoIntable);
        Long id = entityService.getEntityIdByKey(meta, baseKey);
        Entity entity;
        if (id == null) {
            entity = entityService.newEmptyEntity(meta);
            entity.setKey(baseKey);
        } else {
            entity = entityService.getEntity(meta, id, null);
        }
        entityService.persistEntity(entity);
    }

    /**
     * Tests entity with changing metamodel.
     * persists entity instance with simple data,
     * then change model by add new attribute,
     * then get instance from database, set value for new
     * then read entity instanceentity read save without intable attributes.
     */
    @Test
    public void test012PersistsAndChangeModel() {
        EntityMeta meta = createEntityMeta(entityMetaForPersist);
        String baseKey = "T12";
        createTestData(baseKey, meta, 1);
        String key = baseKey + "0";
        String testValue = "test new attribute value " + key;

        // read entity to create mapped statement at mybatis
        Entity entity = entityService.getEntity(meta, key, null);
        assertThat(entity).isNotNull();

        //alter metadata
        meta.getAttributes().add(attributeMeta);
        entityMetaService.persistEntityMeta(meta);
        //meta = entityMetaService.getEntityMetaByKeyNoCache(meta.getKey(), null);

        // set new attribute and persists
        //entity = entityMetaService.getEntity(meta, key, null);
        //assertThat(entity).isNotNull();
        entity.setAttributeValue(attributeMeta.getKey(), testValue);
        assertThat(entity.getAttributeValue(attributeMeta.getKey())).isNotNull();
        assertThat(entity.getAttributeValue(attributeMeta.getKey())).isEqualTo(testValue);
        entityService.persistEntity(entity);

        // check value
        entity = entityService.getEntity(meta, key, null);
        assertThat(entity).isNotNull();
        assertThat(entity.getAttributeValue(attributeMeta.getKey())).isNotNull();
        assertThat(entity.getAttributeValue(attributeMeta.getKey())).isEqualTo(testValue);
    }

    /**
     * Test filter query with reference attribute.
     */
    @Test
    public void test013CriteriaWithReference() {
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, null);
        EntityMeta metaChild = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_TEST_CHILD_META_KEY, null);

        Criteria criteria = new Criteria();
        Where where = criteria.getWhere();
        where.addReferenceItem(meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.REFERENCE),
                               new WhereItem(metaChild.getKeyAttribute(), Operator.EQ, "1.1"));

        List<Entity> entities = (List<Entity>) entityService.getEntities(meta, criteria, null, null);
        assertThat(entities).isNotNull();
        assertThat(entities).hasSize(1);

        where.addItem(new WhereItem(meta.getKeyAttribute(), Operator.EQ, "T0"));
        entities = (List<Entity>) entityService.getEntities(meta, criteria, null, null);
        assertThat(entities).isNotNull();
        assertThat(entities).hasSize(1);
    }

    /**
     * Test filter query with reference attribute.
     */
    @Test
    public void test013x1CriteriaWithReference() {
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, null);
        EntityMeta metaChild = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_TEST_CHILD_META_KEY, null);

        Criteria criteria = new Criteria();
        Where where = criteria.getWhere();
        where.addItem(new WhereItem(meta.getKeyAttribute(), Operator.EQ, "T0")); // only to filter out the test row
        where.setReferenceExists(meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.REFERENCE), Conjunction.AND);
        //EXISTS
        List<Entity> entities = (List<Entity>) entityService.getEntities(meta, criteria, null, null);
        assertThat(entities).isNotNull();
        assertThat(entities).hasSize(1);

        //NOT EXISTS
        where.setReferenceExists(meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.REFERENCE), Conjunction.AND_NOT);
        entities = (List<Entity>) entityService.getEntities(meta, criteria, null, null);
        assertThat(entities).isNotNull();
        assertThat(entities).hasSize(0);

        //NOT EXISTS
        where.setReferenceExists(meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.REFERENCE), Conjunction.AND_NOT);
        where.addReferenceItem(meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.REFERENCE),
                               new WhereItem(metaChild.getKeyAttribute(), Operator.EQ, "1.2"));
        entities = (List<Entity>) entityService.getEntities(meta, criteria, null, null);
        assertThat(entities).isNotNull();
        assertThat(entities).hasSize(1);
    }

    /**
     * Tests getting file from database.
     * Check getting file from database
     * @throws IOException IOException
     */
    @Test
    public void test014GetFile() throws IOException {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        EntityMeta em = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, ldts);
        if (entityService.getEntityIdByKey(em, "T0") == null)
            metadataDao.execute(entityDataForPersist);

        final Entity e = entityService.getEntity(em, "T0", ldts);
        assertNotNull(e);

        FileInfoAttribute file = (FileInfoAttribute) e.getAttribute(PersistTestConstant.PersistTestFields.FILE);
        assertThat(file).isNotNull();
        assertThat(file.getValue()).isEqualTo("test-filename.txt");
        assertThat(file.getMimeType()).isEqualTo("text/plain");

        InputStream fileStream = entityService.getFileContent(file, ldts);
        String fileContent = IOUtils.toString(fileStream, StandardCharsets.UTF_8);
        assertThat(fileContent).isEqualTo("filedata");

        //check link data
        LinkedEntityAttribute<Entity> linkedEntityAttribute = (LinkedEntityAttribute) e.getAttribute(PersistTestConstant.PersistTestFields.REFERENCE);
        assertThat(linkedEntityAttribute).isNotNull();
        assertThat(((List) linkedEntityAttribute.getValue()).size()).isEqualTo(1);

        EntityAttribute ch = linkedEntityAttribute.getValue().get(0);
        assertThat(ch).isNotNull();
        Entity child = (Entity) ch.getEntity();
        assertThat(child).isNotNull();

        FileInfoAttribute childFile = (FileInfoAttribute) child.getAttribute(PersistTestConstant.PersistChildTestFields.FILE);
        assertThat(childFile).isNotNull();
        assertThat(childFile.getValue()).isEqualTo("child-text-filename.txt");
        assertThat(childFile.getMimeType()).isEqualTo("text/plain");

        InputStream childFileStream = entityService.getFileContent(childFile, ldts);
        String childFileContent = IOUtils.toString(childFileStream, StandardCharsets.UTF_8);
        assertThat(childFileContent).isEqualTo("childfiledata");
    }

    /**
     * Tests changing file.
     * Check changing file
     * @throws IOException IOException
     */
    @Test
    public void test015ChangeFile() throws IOException {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        EntityMeta em = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, ldts);
        final Entity e = entityService.getEntity(em, "T0", ldts);
        FileInfoAttribute file = (FileInfoAttribute) e.getAttribute(PersistTestConstant.PersistTestFields.FILE);

        String newContent = "newContent";
        file.setContent(IOUtils.toInputStream(newContent, StandardCharsets.UTF_8));
        String newName = "newName";
        file.setName(newName);
        file.setDescription("test_description");
        entityService.persistEntity(e);

        ldts = metadataDao.getSysTimestamp();
        final Entity e2 = entityService.getEntity(em, "T0", ldts);
        FileInfoAttribute file2 = (FileInfoAttribute) e2.getAttribute(PersistTestConstant.PersistTestFields.FILE);
        InputStream fileStream2 = entityService.getFileContent(file2, ldts);
        String content2 = IOUtils.toString(fileStream2, StandardCharsets.UTF_8);
        assertThat(file2.getContent()).isNull();
        assertThat(content2).isEqualTo(newContent);
        assertThat(file2.getName()).isEqualTo(newName);
        assertThat(file2.getDescription()).isEqualTo("test_description");

        file2.setDescription("test_description2");
        entityService.persistEntity(e2);

        ldts = metadataDao.getSysTimestamp();
        final Entity e3 = entityService.getEntity(em, "T0", ldts);
        FileInfoAttribute file3 = (FileInfoAttribute) e3.getAttribute(PersistTestConstant.PersistTestFields.FILE);
        assertThat(file3.getDescription()).isEqualTo("test_description2");
    }

    /**
     * Tests removing file.
     * Check removing file
     * @throws IOException IOException
     */
    @Test
    public void test016RemoveFile() throws IOException {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        EntityMeta em = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, ldts);
        final Entity e = entityService.getEntity(em, "T0", ldts);
        FileInfoAttribute file = (FileInfoAttribute) e.getAttribute(PersistTestConstant.PersistTestFields.FILE);

        file.setLinkRemoved(true);
        entityService.persistEntity(e);

        ldts = metadataDao.getSysTimestamp();
        final Entity e2 = entityService.getEntity(em, "T0", ldts);
        FileInfoAttribute file2 = (FileInfoAttribute) e2.getAttribute(PersistTestConstant.PersistTestFields.FILE);
        InputStream fileStream2 = entityService.getFileContent(file2, ldts);
        assertThat(fileStream2).isNotNull();
        String fileContent2 = IOUtils.toString(fileStream2, StandardCharsets.UTF_8);
        assertThat(fileContent2.length()).isEqualTo(0);
        assertThat(file2.getContent()).isNull();
        assertThat(file2.getName()).isNull();
        assertThat(file2.getMimeType()).isNull();
    }

    /**
     * Tests uploading file after file removing.
     * Check uploading file after file removing
     * @throws IOException IOException
     */
    @Test
    public void test017UploadFileAfterRemove() throws IOException {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        EntityMeta em = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, ldts);
        final Entity e = entityService.getEntity(em, "T0", ldts);
        FileInfoAttribute mainFile = (FileInfoAttribute) e.getAttribute(PersistTestConstant.PersistTestFields.FILE);

        String newContent = "newContent2";
        InputStream newFileStream = IOUtils.toInputStream(newContent, StandardCharsets.UTF_8);
        mainFile.setContent(newFileStream);
        String newFileName = "fileName2";
        mainFile.setName(newFileName);
        String newMime = "mime2";
        mainFile.setMimeType(newMime);
        entityService.persistEntity(e);

        ldts = metadataDao.getSysTimestamp();
        final Entity e2 = entityService.getEntity(em, "T0", ldts);
        FileInfoAttribute file2 = (FileInfoAttribute) e2.getAttribute(PersistTestConstant.PersistTestFields.FILE);
        InputStream fileStream2 = entityService.getFileContent(file2, ldts);
        String fileContent2 = IOUtils.toString(fileStream2, StandardCharsets.UTF_8);
        assertThat(fileContent2).isEqualTo(newContent);
        assertThat(file2.getContent()).isNull();
        assertThat(file2.getName()).isEqualTo(newFileName);
        assertThat(file2.getMimeType()).isEqualTo(newMime);
    }

    /**
     * Tests get entity multilang attributes.
     */
    @Test
    public void test018GetMultilang() {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        EntityMeta em = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, ldts);
        final Entity e = entityService.getEntity(em, "T0", ldts);
        assertThat(((MultilangAttribute) e.getAttribute(PersistTestConstant.PersistTestFields.STRINGML)).getValueRu())
                .isEqualTo("тест строка ru");
        assertThat(((MultilangAttribute) e.getAttribute(PersistTestConstant.PersistTestFields.STRINGML)).getValueEn())
                .isEqualTo("test string en");
        assertThat(((MultilangAttribute) e.getAttribute(PersistTestConstant.PersistTestFields.TEXTML)).getValueRu()).isEqualTo("тест текст ru");
        assertThat(((MultilangAttribute) e.getAttribute(PersistTestConstant.PersistTestFields.TEXTML)).getValueEn()).isEqualTo("test text en");

        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(
                e.getAttribute(PersistTestConstant.PersistTestFields.STRINGML).getMeta(), Operator.EQ, "тест строка ru"));
        List<? extends Entity> entities = entityService.getEntities(em, criteria, null, null);
        assertThat(entities.size()).isEqualTo(1);

        criteria = new Criteria();
        criteria.getWhere().addItem(
                new WhereItem(e.getAttribute(PersistTestConstant.PersistTestFields.STRINGML).getMeta(), Operator.EQ, "test string en"));
        assertThat(entityService.getEntities(em, criteria, null, null).size()).isEqualTo(1);

        criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(
                e.getAttribute(PersistTestConstant.PersistTestFields.STRINGML).getMeta(), Operator.EQ, "test string en: not exist"));
        assertThat(entityService.getEntities(em, criteria, null, null).size()).isEqualTo(0);

        criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(
                e.getAttribute(PersistTestConstant.PersistTestFields.TEXTML).getMeta(), Operator.EQ, "тест текст ru"));
        entities = entityService.getEntities(em, criteria, null, null);
        assertThat(entities.size()).isEqualTo(1);

        criteria = new Criteria();
        criteria.getWhere().addItem(
                new WhereItem(e.getAttribute(PersistTestConstant.PersistTestFields.TEXTML).getMeta(), Operator.EQ, "test text en"));
        assertThat(entityService.getEntities(em, criteria, null, null).size()).isEqualTo(1);

        criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(
                e.getAttribute(PersistTestConstant.PersistTestFields.TEXTML).getMeta(), Operator.EQ, "test text en: not exist"));
        assertThat(entityService.getEntities(em, criteria, null, null).size()).isEqualTo(0);

        criteria = new Criteria();
        criteria.getWhere().addItem(
                new WhereItem(e.getAttribute(PersistTestConstant.PersistTestFields.STRINGML).getMeta(), Operator.LIKE, "test string en"));
        assertThat(entityService.getEntities(em, criteria, null, null).size()).isEqualTo(1);

        criteria = new Criteria();
        criteria.getWhere().addItem(
                new WhereItem(e.getAttribute(PersistTestConstant.PersistTestFields.TEXTML).getMeta(), Operator.LIKE, "test text en"));
        assertThat(entityService.getEntities(em, criteria, null, null).size()).isEqualTo(1);
    }

    /**
     * Tests get entity multilang attributes with OR criteria.
     */
    @Test
    public void test018x2GetMultilangWithOr() {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        EntityMeta em = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, ldts);
        final String baseKey = "T018X2";
        Entity[] testEntity = createTestData(baseKey, em, 2).toArray(new Entity[] {});

        // MULTILANG ATTRIBUTES ONLY, OR bay same multilang attribute (entity T018X21 , T018X22) (STRINGML both)
        Criteria criteria;
        Where where = (criteria = new Criteria()).getWhere();
        where.addItem(new WhereItem(em.getAttributeMetadata(PersistTestConstant.PersistTestFields.STRINGML), Operator.EQ, "ml string value T018X20"));
        where.addItem(new WhereItem(Conjunction.OR, em.getAttributeMetadata(PersistTestConstant.PersistTestFields.STRINGML), Operator.EQ,
                                    "ml string value T018X21"));
        List<Entity> el = (List<Entity>) entityService.getEntities(em, criteria, null, null);
        assertThat(el).containsExactlyInAnyOrder(testEntity);

        // MULTILANG ATTRIBUTES ONLY, OR bay different multilang attributes (entity T018X21 (by STRINGML), T018X22 (by TEXTML))
        where = (criteria = new Criteria()).getWhere();
        where.addItem(new WhereItem(em.getAttributeMetadata(PersistTestConstant.PersistTestFields.STRINGML), Operator.EQ, "ml string value T018X21"));
        where.addItem(new WhereItem(Conjunction.OR, em.getAttributeMetadata(PersistTestConstant.PersistTestFields.TEXTML), Operator.EQ,
                                    "ml text value T018X20"));
        el = (List<Entity>) entityService.getEntities(em, criteria, null, null);
        assertThat(el).containsExactlyInAnyOrder(testEntity);

        // MULTILANG AND NON MULTILANG, OR bay KEY IN + same multilang attribute (entity T018X21 , T018X22) (STRINGML both)
        where = (criteria = new Criteria()).getWhere();
        where.addItem(new WhereItem(em.getKeyAttribute(), Operator.IN, "T018X21", "T018X20"));
        where.addItem(new WhereItem(em.getAttributeMetadata(PersistTestConstant.PersistTestFields.STRINGML), Operator.EQ, "ml string value T018X21"));
        where.addItem(new WhereItem(Conjunction.OR, em.getAttributeMetadata(PersistTestConstant.PersistTestFields.STRINGML), Operator.EQ,
                                    "ml string value T018X20"));
        el = (List<Entity>) entityService.getEntities(em, criteria, null, null);
        assertThat(el).containsExactlyInAnyOrder(testEntity);

        // MULTILANG AND NON MULTILANG, OR bay KEY IN + different multilang attributes (entity T018X21 (by STRINGML), T018X22 (by TEXTML))
        where = (criteria = new Criteria()).getWhere();
        where.addItem(new WhereItem(em.getKeyAttribute(), Operator.IN, "T018X21", "T018X20"));
        where.addItem(new WhereItem(Conjunction.OR, em.getAttributeMetadata(PersistTestConstant.PersistTestFields.STRINGML), Operator.EQ,
                                    "ml string value T018X21"));
        where.addItem(new WhereItem(Conjunction.OR, em.getAttributeMetadata(PersistTestConstant.PersistTestFields.TEXTML), Operator.EQ,
                                    "ml text value T018X20"));
        el = (List<Entity>) entityService.getEntities(em, criteria, null, null);
        assertThat(el).containsExactlyInAnyOrder(testEntity);

        where = (criteria = new Criteria()).getWhere();
        where.addItem(new WhereItem(em.getKeyAttribute(), Operator.IN, "T018X21", "T018X20"));
        where.addItem(new WhereItem(Conjunction.AND_NOT, em.getAttributeMetadata(PersistTestConstant.PersistTestFields.TEXTML), Operator.EQ,
                                    "ml string value T018X21"));
        el = (List<Entity>) entityService.getEntities(em, criteria, null, null);
        assertThat(el).containsExactlyInAnyOrder(testEntity);
    }

    /**
     * Tests get entity reference attributes with OR criteria.
     */
    @Test
    public void test019GetReferenceWithOr() {
        final int objCount = 5;
        String baseKey = "T019";
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, null);
        assertThat(meta).isNotNull();
        createTestData(baseKey, meta, objCount);

        Entity p1 = entityService.getEntity(meta, "T0190", null);
        assertThat(p1).isNotNull();
        Entity ch1 = entityService.getEntity(meta, "T0191", null);
        assertThat(ch1).isNotNull();
        p1.getChildrenReferenceAttribute().add(ch1);
        Entity ch2 = entityService.getEntity(meta, "T0192", null);
        assertThat(ch2).isNotNull();
        p1.getChildrenReferenceAttribute().add(ch2);
        Entity ch3 = entityService.getEntity(meta, "T0193", null);
        assertThat(ch3).isNotNull();
        p1.getChildrenReferenceAttribute().add(ch3);
        entityService.persistEntity(p1);
        Entity ch4 = entityService.getEntity(meta, "T0194", null);
        assertThat(ch4).isNotNull();
        ch3.getChildrenReferenceAttribute().add(ch4);
        entityService.persistEntity(ch3);

        // parent and child meta search
        Criteria criteria = new Criteria();
        Where where = criteria.getWhere();
        where.addItem(new WhereItem(meta.getKeyAttribute(), Operator.EQ, "T0192"));
        where.addReferenceItem(meta.getChildrenReferenceAttribute(),
                               new WhereItem(Conjunction.OR, meta.getKeyAttribute(), Operator.EQ, "T0191"));
        where.addReferenceItem(meta.getParentReferenceAttribute(),
                               new WhereItem(Conjunction.OR, meta.getKeyAttribute(), Operator.EQ, "T0193"));
        List<Entity> check = (List<Entity>) entityService.getEntities(meta, criteria, null, null);
        assertThat(check).containsExactlyInAnyOrder(p1, ch2, ch4);

        // parent and child meta search
        criteria = new Criteria();
        where = criteria.getWhere();
        where.addReferenceItem(meta.getChildrenReferenceAttribute(),
                               new WhereItem(Conjunction.OR, meta.getKeyAttribute(), Operator.EQ, "T0191"));
        where.addReferenceItem(meta.getParentReferenceAttribute(),
                               new WhereItem(Conjunction.OR, meta.getKeyAttribute(), Operator.EQ, "T0193"));
        where.addItem(new WhereItem(Conjunction.OR, meta.getKeyAttribute(), Operator.EQ, "T0192"));
        check = (List<Entity>) entityService.getEntities(meta, criteria, null, null);
        assertThat(check).containsExactlyInAnyOrder(p1, ch2, ch4);

        // test first OR statement
        criteria = new Criteria();
        where = criteria.getWhere();
        where.addReferenceItem(meta.getChildrenReferenceAttribute(),
                               new WhereItem(Conjunction.OR, meta.getKeyAttribute(), Operator.EQ, "T0191"));
        check = (List<Entity>) entityService.getEntities(meta, criteria, null, null);
        assertThat(check).containsExactlyInAnyOrder(p1);

        // multilang and reference search
        criteria = new Criteria();
        where = criteria.getWhere();
        where.addReferenceItem(meta.getChildrenReferenceAttribute(),
                               new WhereItem(Conjunction.OR, meta.getKeyAttribute(), Operator.EQ, "T0194"));
        where.addItem(new WhereItem(Conjunction.OR, meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.TEXTML), Operator.EQ,
                                    "ml text value T0192"));
        check = (List<Entity>) entityService.getEntities(meta, criteria, null, null);
        assertThat(check).containsExactlyInAnyOrder(ch2, ch3);

        // test first AND_NOT statement
        criteria = new Criteria();
        where = criteria.getWhere();
        where.addReferenceItem(meta.getChildrenReferenceAttribute(),
                               new WhereItem(Conjunction.AND_NOT, meta.getKeyAttribute(), Operator.EQ, "T0191"));
        check = (List<Entity>) entityService.getEntities(meta, criteria, null, null);
        assertThat(check.size()).isGreaterThan(1);
    }

    /**
     * Test remove entity meta that referenced by other entity meta.
     * @throws Exception on JSON unmarshalling error
     */
    @Test
    public void test020RemoveChildMeta() throws Exception {
        // prepare metadata
        ObjectMapper objectMapper = new ObjectMapper();
        EntityMeta entityMeta = objectMapper.readValue(EntityServicePersistTest.class.getResourceAsStream("entity_remove_meta_test.json"),
                                                       EntityMeta.class);
        assertNotNull(entityMeta);
        EntityMeta entityMetaChild = objectMapper
                .readValue(EntityServicePersistTest.class.getResourceAsStream("entity_remove_meta_test_child.json"), EntityMeta.class);
        assertNotNull(entityMetaChild);
        entityMetaService.persistEntityMeta(entityMetaChild);
        entityMetaService.persistEntityMeta(entityMeta);
        entityMeta = entityMetaService.getEntityMetaByKey(entityMeta.getKey(), null);
        entityMetaChild = entityMetaService.getEntityMetaByKey(entityMetaChild.getKey(), null);

        // prepare data for both meta
        Entity entityChild = entityService.newEmptyEntity(entityMetaChild);
        entityChild.setKey("REMOVE020_CHILD");
        entityChild.setAttributeValue(PersistTestConstant.RemoveChildTestFields.STRING, "child test string " + entityChild.getKey());
        entityService.persistEntity(entityChild);
        Entity entity = entityService.newEmptyEntity(entityMeta);
        entity.setKey("REMOVE020");
        entity.setAttributeValue(PersistTestConstant.RemoveTestFields.STRING, "test string " + entity.getKey());
        ((LinkedEntityAttribute) entity.getAttribute(PersistTestConstant.RemoveTestFields.REFERENCE)).add(entityChild);
        entityService.persistEntity(entity);

        // try to delete child with parent referenced to it
        AttributeMeta ref = entityMeta.getAttributeMetadata(PersistTestConstant.RemoveTestFields.REFERENCE);
        AttributeMeta ref2 = entityMeta.getAttributeMetadata(PersistTestConstant.RemoveTestFields.REFERENCE2);
        final EntityMeta metaChildFin = entityMetaChild;
        assertThatThrownBy(() -> entityMetaService.removeEntityMeta(metaChildFin))
                .isInstanceOf(RemoveReferencedMetaException.class).hasFieldOrPropertyWithValue("referencedEntityMeta", entityMetaChild)
                .hasFieldOrPropertyWithValue("referenceByEntityMetas", Arrays.asList(entityMeta))
                .hasFieldOrPropertyWithValue("referencedByAttributes", Arrays.asList(ref, ref2));

        // remove reference attribute and delete child meta again
        entityMeta.getAttributes().remove(ref);
        entityMeta.getAttributes().remove(ref2);
        entityMetaService.persistEntityMeta(entityMeta);
        entityMetaService.removeEntityMeta(entityMetaChild);

        // not in group
        EntityMeta entityMetaGroupMeta = entityMetaService.getEntityMetaByKey(EntityMetaGroup.METADATA_KEY, null);
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(
                new WhereItem(entityMetaGroupMeta.getKeyAttribute(), Operator.EQ, EntityMetaGroup.DefaultGroup.DEFAULT_DICTIONARY_GROUP.name()));
        List<EntityMetaGroup> entityMetaGroupList = entityMetaService.getEntityMetaGroups(criteria, null, null);
        assertThat(entityMetaGroupList).hasSize(1);
        EntityMetaGroup entityMetaGroup = entityMetaGroupList.get(0);
        assertThat(entityMetaGroup.getElements()).doesNotContain(entityMetaChild);
    }

    /**
     * Create entity metadata in database.
     * @param entityMeta entity metadata to persists
     * @return persisted metadata
     */
    private EntityMeta createEntityMeta(EntityMeta entityMeta) {
        EntityMeta m = entityMetaService.getEntityMetaByKeyNoCache(entityMeta.getKey(), null);
        if (m == null) {
            if (entityMeta.getKey().equals(PersistTestConstant.PERSIST_META_KEY))
                entityMetaService.persistEntityMeta(entityMetaForPersistChild);
            entityMetaService.persistEntityMeta(entityMeta);
        }
        return entityMetaService.getEntityMetaByKey(entityMeta.getKey(), null);
    }

    /**
     * Massive test data creator for {@code baseKey}.
     * @param baseKey key basic
     * @param meta entity metadata
     * @param cycleInt quantity of entities
     * @return list of test entities
     */
    private List<Entity> createTestData(String baseKey, EntityMeta meta, int cycleInt) {
        List<Entity> result = new ArrayList<>();
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        for (int i = 0; i < cycleInt; i++) {
            String key = baseKey + i;
            Long id = entityDbService.getEntityIdByKey(meta, key);
            if (id == null) {
                // create regular entity
                Entity entity = entityService.newEmptyEntity(meta);
                assertThat(entity).isNotNull();
                entity.setKey(key);
                entity.setAttributeValue(PersistTestConstant.PersistTestFields.BOOLEAN, true);
                entity.setAttributeValue(PersistTestConstant.PersistTestFields.STRING, "test string " + key);
                entity.setAttributeValue(PersistTestConstant.PersistTestFields.FILE, "file-name-" + key);
                entity.setAttributeValue(PersistTestConstant.PersistTestFields.FILE2, "file2-name-" + key);
                ((MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistTestFields.STRINGML)).setValueEn("ml string value " + key);
                ((MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistTestFields.STRINGML2)).setValueEn("ml string2 value " + key);
                ((MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistTestFields.STRINGML3)).setValueEn("ml string3 value " + key);
                ((MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistTestFields.TEXTML)).setValueEn("ml text value " + key);
                ((MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistTestFields.TEXTML2)).setValueEn("ml text2 value " + key);
                ((MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistTestFields.TEXTML3)).setValueEn("ml text3 value " + key);
                entityService.persistEntity(entity);
                result.add(entity);
            } else {
                result.add(entityService.getEntity(meta, key, ldts));
            }
        }
        return result;
    }

    /**
     * Massive test data creator for {@code baseKey}.
     * @param baseKey key basic
     * @param meta entity metadata
     * @param cycleInt number of created children
     */
    private void createTestDataChild(String baseKey, EntityMeta meta, int cycleInt) {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        for (int i = 0; i < cycleInt; i++) {
            String key = baseKey + i;
            Long id = entityDbService.getEntityIdByKey(meta, key);
            if (id == null) {
                // create regular entity
                Entity entity = entityService.newEmptyEntity(meta);
                assertThat(entity).isNotNull();
                entity.setKey(key);
                entity.setAttributeValue(PersistTestConstant.PersistChildTestFields.BOOLEAN, true);
                entity.setAttributeValue(PersistTestConstant.PersistChildTestFields.STRING, "test string " + key);
                entity.setAttributeValue(PersistTestConstant.PersistChildTestFields.FILE, "file-name-" + key);
                ((MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistChildTestFields.STRINGML)).setValueEn("ml string value " + key);
                ((MultilangAttribute) entity.getAttribute(PersistTestConstant.PersistChildTestFields.TEXTML)).setValueEn("ml text value " + key);
                entityService.persistEntity(entity);
            }
        }
    }

    /**
     * Tests entity creation for paging test.
     */
    @Test
    public void test101CriteriaSimpleOrder() {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, ldts);
        assertThat(meta).isNotNull();

        Criteria criteria = new Criteria();
        Order order = criteria.getOrder();
        order.addItem(new OrderItem(meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.STRING)));
        String o1 = order.getText();
        assertThat(o1).isEqualTo("upper(\"PERSIST_TEST_STRING\")");

        order.addItem(new OrderItem(meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.DATE), true));
        String o2 = order.getText();
        assertThat(o2).isEqualTo("upper(\"PERSIST_TEST_STRING\"),\"DATE\" desc");

        order.addItem(new OrderItem(meta.getKeyAttribute(), true));
        String o3 = order.getText();
        assertThat(o3).isEqualTo("upper(\"PERSIST_TEST_STRING\"),\"DATE\" desc,upper(\"KEY\") desc");
    }

    /**
     * Tests entity creation for paging test.
     */
    @Test
    public void test102CriteriaSimpleWhere() {
        LocalDate mainD = LocalDate.parse("2009-09-09");
        LocalDate mainD2 = LocalDate.parse("2019-09-09");
        LocalDateTime mainDT = LocalDateTime.parse("2009-09-09T09:09:09");
        LocalDateTime mainDT2 = LocalDateTime.parse("2019-09-09T09:09:19");
        final int ten = TEST_ENTITIES_TO_CREATE;

        LocalDateTime ldts = metadataDao.getSysTimestamp();
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, ldts);
        assertThat(meta).isNotNull();

        Criteria criteria = new Criteria();
        Where where = criteria.getWhere();
        where.addItem(new WhereItem(meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.STRING), Operator.EQ, "qwerty"));
        String w1 = where.getText();
        assertThat(w1).isEqualTo("upper(\"PERSIST_TEST_STRING\") = upper('qwerty')");

        where.addItem(
                new WhereItem(Conjunction.OR, meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.STRING), Operator.LIKE, "asdfgh"));
        w1 = where.getText();
        assertThat(w1).isEqualTo("upper(\"PERSIST_TEST_STRING\") = upper('qwerty') or upper(\"PERSIST_TEST_STRING\") like upper('asdfgh%')");

        WhereParenthesesGroup g = new WhereParenthesesGroup(Conjunction.AND_NOT);
        g.addItem(new WhereItem(meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.NUMBER), Operator.EQ, new BigDecimal(-1)));
        g.addItem(new WhereItem(Conjunction.OR, meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.NUMBER), Operator.GT_EQ,
                                new BigDecimal(ten)));
        where.addGroup(g);
        where.addItem(new WhereItem(Conjunction.OR, meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.NUMBER), Operator.IS_NOT_NULL,
                                    "fake null"));
        w1 = where.getText();
        assertThat(w1).isEqualTo(
                "upper(\"PERSIST_TEST_STRING\") = upper('qwerty') or upper(\"PERSIST_TEST_STRING\") like upper('asdfgh%') and "
                + "not (\"PERSIST_TEST_NUMBER\" = -1 or"
                + " \"PERSIST_TEST_NUMBER\" >= 10) or \"PERSIST_TEST_NUMBER\" is not null");

        where.addItem(
                new WhereItem(Conjunction.AND, meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.DATE), Operator.IN, mainD, mainD2));
        where.addItem(
                new WhereItem(Conjunction.AND, meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.DATETIME), Operator.NOT_IN, mainDT,
                              mainDT2));
        w1 = where.getText();
        assertThat(w1).isEqualTo(
                "upper(\"PERSIST_TEST_STRING\") = upper('qwerty') or upper(\"PERSIST_TEST_STRING\") like upper('asdfgh%') and "
                + "not (\"PERSIST_TEST_NUMBER\" = -1 or"
                + " \"PERSIST_TEST_NUMBER\" >= 10) or \"PERSIST_TEST_NUMBER\" is not null "
                + "and \"DATE\" in (date '2009-09-09',date '2019-09-09') and"
                + " \"PERSIST_TEST_DATETIME\" not in (to_date('2009-09-09 09:09:09','YYYY-MM-DD HH24:MI:SS'),"
                + "to_date('2019-09-09 09:09:19','YYYY-MM-DD HH24:MI:SS'))");
    }

    /**
     * Tests entity creation for paging test.
     */
    @Test
    public void test103CriteriaClobTest() {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        EntityMeta meta = entityMetaService.getEntityMetaByKey(PersistTestConstant.PERSIST_META_KEY, ldts);
        assertThat(meta).isNotNull();

        Criteria criteria = new Criteria();
        Where where = criteria.getWhere();
        where.addItem(new WhereItem(meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.TEXT), Operator.EQ, "qwerty"));
        String w = where.getText();
        assertThat(w).isEqualTo("upper(dbms_lob.substr(\"PERSIST_TEST_TEXT\",4000,1)) = upper('qwerty')");
        where.addItem(
                new WhereItem(Conjunction.AND_NOT, meta.getAttributeMetadata(PersistTestConstant.PersistTestFields.TEXT), Operator.LIKE, "poi"));
        w = where.getText();
        assertThat(w).isEqualTo(
                "upper(dbms_lob.substr(\"PERSIST_TEST_TEXT\",4000,1)) = upper('qwerty') and "
                + "not dbms_lob.instr(upper(\"PERSIST_TEST_TEXT\"),upper('poi'))!=0");
    }

    /**
     * Tests filter entities via criteria referenced entities param.
     */
    @Test
    public void test104CriteriaReferencedEntities() {
        final Integer clientCount = 3;
        Entity clientGroup = createClientGroupWithClients("CLGROUP104_1", clientCount);
        EntityMeta clientMeta = entityMetaService.getEntityMetaByKey(ClientAttributeMeta.METADATA_KEY, null);
        Criteria criteria = new Criteria();
        criteria.addReferencedEntity(clientGroup);
        List<? extends Entity> clients = entityService.getEntitiesBase(clientMeta, criteria, null, null);
        assertThat(clients).hasSize(clientCount);

        User newUser = (User) entityService.newEmptyEntity(User.METADATA_KEY);
        entityService.persistEntity(newUser);
        Criteria finalCriteria = new Criteria();
        finalCriteria.addReferencedEntity(newUser);
        assertThat(catchThrowable(() -> entityService.getEntitiesBase(clientMeta, finalCriteria, null, null))).isNotNull();
    }

    /**
     * Prepares initial test data.
     * @throws Exception exception
     */
    @BeforeClass
    public static void setup() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        entityMetaForPersist = objectMapper.readValue(EntityServicePersistTest.class.getResourceAsStream("entity_meta_persist_test.json"),
                                                      EntityMeta.class);
        assertNotNull(entityMetaForPersist);

        entityMetaForPersistChild = objectMapper
                .readValue(EntityServicePersistTest.class.getResourceAsStream("entity_meta_persist_test_child.json"), EntityMeta.class);
        assertNotNull(entityMetaForPersistChild);

        entityMetaForPersistNoIntable = objectMapper
                .readValue(EntityServicePersistTest.class.getResourceAsStream("entity_meta_persist_test_no_intable.json"), EntityMeta.class);
        assertNotNull(entityMetaForPersistNoIntable);

        entityDataForPersist = IOUtils.toString(EntityServicePersistTest.class.getResourceAsStream("insert_data_persist_test.sql"),
                                                StandardCharsets.UTF_8);
        assertNotNull(entityDataForPersist);

        attributeMeta = objectMapper.readValue(EntityMetaServiceTest.class.getResourceAsStream("entity_meta_persist_test-new_attribute.json"),
                                               AttributeMeta.class);
        assertNotNull(attributeMeta);
    }

    /**
     * Get total count for alive test entities.
     * @return count
     */
    private Long getTotalAliveTestEntities() {
        return metadataCheckingDao.selectLong(
                "select count(1)\n"
                + "  from (select es.removed,\n"
                + "               row_number() over(partition by es.h_id order by es.ldts desc) rn$\n"
                + "          from crs_h_persist_test eh\n"
                + "          join crs_s_persist_test es on eh.id = es.h_id\n"
                + "                                    and es.ldts <= systimestamp) e\n"
                + " where e.rn$ = 1 and e.removed = 0");
    }

    /**
     * Creates client group with clients.
     * @param clientGroupKey client group key
     * @param clientCount client count
     * @return client group
     */
    private Entity createClientGroupWithClients(String clientGroupKey, Integer clientCount) {
        Entity clientGroup = createTestClientGroup(clientGroupKey);
        entityService.persistEntity(clientGroup);
        for (int i = 0; i < clientCount; i++)
            createTestClient(String.format("%sCL0" + StringUtils.leftPad(String.valueOf(i), clientCount, "0"), clientGroupKey), clientGroup);
        return clientGroup;
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
     * Create entity of any type if not exists.
     * @param entityMetaKey type of entity
     * @param key entity key
     * @param ldts load datetime
     * @return new or loaded entity
     */
    private Entity createTestEntity(String entityMetaKey, String key, LocalDateTime ldts) {
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(entityMetaKey, ldts);
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
}
