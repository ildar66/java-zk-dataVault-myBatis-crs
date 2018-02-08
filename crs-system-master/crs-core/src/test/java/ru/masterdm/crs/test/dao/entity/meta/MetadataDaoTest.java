package ru.masterdm.crs.test.dao.entity.meta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.masterdm.crs.dao.MetadataCheckingDao;
import ru.masterdm.crs.dao.entity.meta.MetadataDao;
import ru.masterdm.crs.dao.entity.meta.dto.EntityMetaDto;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.service.EntityMetaService;

/**
 * Tests MetadataDao service.
 * @author Sergey Valiev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MetadataDaoTest {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataDaoTest.class);

    private static final String FORM = "newForm";
    private static final String DESCRIPTION = "new_description";
    private static final String COMMENT_EN = "comment_en";
    private static final String NAME_EN = "name_en";

    private static EntityMeta entityMeta;
    private static AttributeMeta attributeMeta;

    private static LocalDateTime t0;
    private static LocalDateTime t1;
    private static LocalDateTime t2;
    private static LocalDateTime t3;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private MetadataDao metadataDao;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private MetadataCheckingDao metadataCheckingDao;
    @Autowired
    private EntityMetaService entityMetaService;

    /**
     * Tests persist entity.
     */
    @Test
    public void test01PersistEntity() {
        t0 = metadataDao.getSysTimestamp();
        metadataDao.persistEntity(entityMeta);
        assertNotNull(entityMeta.getId());
        assertNotNull(entityMeta.getLdts());
        assertNotNull(entityMeta.getHubId());
        assertNotNull(entityMeta.getComment());

        assertThat(entityMeta.getAttributeMetadata("TESTENTITYMETA1#ATTRIBUTE2").getDefaultValue()).isNull();

        Long entityId = entityMeta.getId();
        Long entityHubId = entityMeta.getHubId();

        metadataDao.persistEntity(entityMeta);
        t1 = metadataDao.getSysTimestamp();

        assertTrue(entityId.equals(entityMeta.getId()));
        assertTrue(entityHubId.equals(entityMeta.getHubId()));

        String newLinkTable = "new_link_table";
        boolean newMultilang = !entityMeta.getAttributes().get(0).isMultilang();
        AttributeType newAttributeType = Stream.of(AttributeType.values())
                                               .filter(type -> !type.equals(entityMeta.getAttributes().get(0).getType()))
                                               .findFirst().get();

        entityMeta.setForm(FORM);
        entityMeta.setLinkTable(newLinkTable);
        entityMeta.getAttributes().get(0).setMultilang(newMultilang);
        entityMeta.getAttributes().get(0).getName().setDescriptionEn(DESCRIPTION);
        entityMeta.getAttributes().get(0).setType(newAttributeType);
        entityMeta.getAttributes().add(attributeMeta);
        entityMeta.getComment().setDescriptionEn(COMMENT_EN);
        entityMeta.getName().setDescriptionEn(NAME_EN);
        metadataDao.persistEntity(entityMeta);
        t2 = metadataDao.getSysTimestamp();

        assertNotNull(attributeMeta.getId());
        assertNotNull(attributeMeta.getLdts());
        assertFalse(newAttributeType == attributeMeta.getType());

        assertFalse(entityId.equals(entityMeta.getId()));
        assertTrue(entityHubId.equals(entityMeta.getHubId()));
        assertFalse(newLinkTable.equals(entityMeta.getLinkTable()));
        assertFalse(newMultilang == entityMeta.getAttributes().get(0).isMultilang());

        entityMeta.getAttributes().remove(0);
        metadataDao.persistEntity(entityMeta);
    }

    /**
     * Tests remove entity.
     */
    @Test
    public void test02RemoveEntity() {
        t3 = metadataDao.getSysTimestamp();
        metadataDao.removeEntity(entityMeta.getId());
        assertTrue(metadataDao.getEntityByKey(entityMeta.getKey(), metadataDao.getSysTimestamp()) == null);
    }

    /**
     * Tests read entity.
     */
    @Test
    public void test03ReadEntity() {
        assertTrue(getEntities(null, null, t1, EntityType.DICTIONARY).size() > 0);
        assertTrue(metadataDao.getEntityByKey(entityMeta.getKey(), t0) == null);

        entityMeta = metadataDao.getEntityByKey(entityMeta.getKey(), t1);
        assertNull(entityMeta.getForm());
        assertFalse(entityMeta.getAttributes().get(0).getName().getDescriptionEn().equals(DESCRIPTION));
        assertFalse(entityMeta.getAttributes().stream().anyMatch(attribute -> attribute.getKey().equals(attributeMeta.getKey())));

        EntityMeta entityMetaMeta = entityMetaService.getEntityMetaByKey(EntityMeta.METADATA_KEY, null);
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(entityMetaMeta.getAttributeMetadata(EntityMeta.EntityMetaAttributeMeta.NAME_EN.getKey()),
                                                  Operator.EQ, NAME_EN));
        assertTrue(getEntities(criteria, null, t2, EntityType.DICTIONARY).size() > 0);

        Criteria criteria2 = new Criteria();
        criteria2.getWhere().addItem(new WhereItem(entityMetaMeta.getAttributeMetadata(EntityMeta.EntityMetaAttributeMeta.NAME_EN.getKey()),
                                                   Operator.EQ, DESCRIPTION));
        assertTrue(getEntities(criteria2, null, t2, EntityType.DICTIONARY).size() == 0);

        entityMeta = metadataDao.getEntityByKey(entityMeta.getKey(), t2);
        int attributeCount = entityMeta.getAttributes().size();
        assertTrue(entityMeta.getForm().equals(FORM));
        assertTrue(entityMeta.getAttributes().get(0).getName().getDescriptionEn().equals(DESCRIPTION));
        assertTrue(entityMeta.getAttributes().stream().anyMatch(attribute -> attribute.getKey().equals(attributeMeta.getKey())));
        assertTrue(entityMeta.getComment().getDescriptionEn().equals(COMMENT_EN));
        entityMeta = metadataDao.getEntityByKey(entityMeta.getKey(), t3);
        assertTrue(attributeCount > entityMeta.getAttributes().size());
    }

    /**
     * Helper to get metadata list from dto object.
     * @param criteria filter and sort criteria. may be null to get all rows
     * @param rowRange rows range for pagination, mat be null to get all rows
     * @param ldts load date
     * @param types entity types
     * @return entities metadata
     */
    private List<EntityMeta> getEntities(Criteria criteria, RowRange rowRange, LocalDateTime ldts, EntityType... types) {
        if (ldts == null)
            ldts = metadataDao.getSysTimestamp();
        EntityMetaDto dto = metadataDao.getEntities(criteria, rowRange, ldts, types);
        if (dto == null)
            return Collections.emptyList();
        return dto.getEntityMetas();
    }

    /**
     * Tests read entity's attributes native column.
     */
    @Test
    public void test04ReadNativeColumn() {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        EntityMeta entityMeta = metadataDao.getEntityByKey("CLIENT_INN", ldts);

        AttributeMeta attributeMeta = entityMeta.getAttributeMetadata("CLIENT_INN#TAX_ID");
        assertThat(attributeMeta).isNotNull();
        assertThat(attributeMeta.getNativeColumn()).isEqualTo("TAX_ID");
    }

    /**
     * Tests existence of entity types.
     */
    @Test
    public void test05EntityTypes() {
        Arrays.stream(EntityType.values()).forEach(et -> assertTrue(metadataCheckingDao.isEntityTypeExists(et)));
    }

    /**
     * Prepares initial test data.
     * @throws Exception exception
     */
    @BeforeClass
    public static void setup() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        entityMeta = objectMapper.readValue(MetadataDaoTest.class.getResourceAsStream("dao_dv_test_entity_meta.json"), EntityMeta.class);
        attributeMeta = objectMapper.readValue(MetadataDaoTest.class.getResourceAsStream("dao_dv_test_attribute_meta.json"),
                                               AttributeMeta.class);

        assertNotNull(entityMeta);
        assertNotNull(attributeMeta);
    }
}
