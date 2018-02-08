package ru.masterdm.crs.integration.client.portal.test.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ru.masterdm.crs.dao.entity.meta.MetadataDao;
import ru.masterdm.crs.domain.entity.Department;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.MultilangAttribute;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.ClientAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientCategoryAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientCountryAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientCurrencyAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientGroupAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientIndustryAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientInnAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientOgrnAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientOpfAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientSegmentAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientTypeAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.integration.client.portal.dao.ClientPortalIntegrationDao;
import ru.masterdm.crs.integration.client.portal.dao.CustomSqlDao;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;

/**
 * Test collection for {@link ClientPortalIntegrationDao} service.
 * @author Alexey Chalov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:META-INF/spring/crs-core-config.xml",
        "classpath:META-INF/spring/crs-datasource-config-test.xml",
        "classpath:META-INF/spring/crs-client-portal-integration-datasource-config.xml"
})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClientPortalIntegrationDaoTest {

    private static final Logger LOG = LoggerFactory.getLogger(ClientPortalIntegrationDaoTest.class);

    private static final String MAX_ID_LOG_SQL = "select max(id_log) from crs_cpi_log";
    private static final String TABLE_COUNT_SQL = "select count(*) from %s";
    private static final String MAX_LAST_SYNC_DATE_SQL = "select to_char(max(last_sync_date), 'hh24:mi:ss.ff9') from crs_cpi_last_sync";
    private static final String FIRST_ERROR_SQL = "select (select error_text from (select error_text, row_number() over(order by last_sync_date) rn "
                                                  + "from crs_cpi_log where id_log > %s) z where z.rn = 1) from dual";
    private static final String IS_VTB_DAUGHTER = "T";
    private static final String ENTITY_KEY = "909010";
    private static final String CLIENT_KEY = "332210";
    private static final String CLIENT2_KEY = "332212";
    private static final String DEPARTMENT_ID = "555310";
    private static final String CATEGORY_ID = "4315";
    private static final String CURRENCY_ID = "4305";
    private static final String SEGMENT_ID = "4314";
    private static final String INDUSTRY_ID = "4312";
    private static final String OPF_ID = "4313";
    private static final String COUNTRY_ID = "4310";
    private static final String NEW_UNIQUE_NAME1_RU = "newUniqueName1Ru";
    private static final String NEW_UNIQUE_NAME1_EN = "newUniqueName1En";
    private static final String NEW_UNIQUE_NAME2_RU = "newUniqueName2Ru";
    private static final String NEW_UNIQUE_NAME2_EN = "newUniqueName2En";
    private static final String NEW_UNIQUE_NAME3_RU = "newUniqueName3Ru";
    private static final String NEW_UNIQUE_NAME3_EN = "newUniqueName3En";
    private static final String NOT_IN_CRS_DEPARTMENT_ID = "555340";
    private static final String CLIENT_ACTIVE_SQL = "delete crs_cpi_client_deleted where vtb_legalentityid = " + CLIENT_KEY;
    private static final String
            TEST15_RESET = "insert into crs_l_client_department(id, ldts, removed, client_id, department_id) "
                           + "select crs_l_client_department_seq.nextval, sysdate, 1, l.client_id, l.department_id "
                           + "from ( select l.client_id, l.department_id, l.removed, "
                           + "row_number() over(partition by l.client_id, l.department_id order by l.ldts desc) rn "
                           + "from crs_l_client_department l) l join crs_h_client c on c.id = l.client_id "
                           + "where l.removed = 0 and l.rn = 1 and c.key in ('%s', '%s')";
    private static final String TEST16_TEXT =
            "update crs_cpi_department set dep_name = dep_full_name|| ' ' || '"
            + UUID.randomUUID().toString()
            + "', last_update = systimestamp where departmentid = " + DEPARTMENT_ID;
    private static final String CLIENT_NOT_ACTIVE_SQL =
            "insert into crs_cpi_client_deleted(vtb_legalentityid, del_date) select " + CLIENT_KEY + ", sysdate from dual";
    public static final String GET_CLIENT_DEPARTMENT_KEYS_SQL =
            "select LISTAGG(to_char(d.key), ',') WITHIN GROUP(ORDER BY to_number(d.key)) "
            + "from (select l.client_id, l.department_id, l.removed, "
            + "row_number() over(partition by l.client_id, l.department_id order by l.ldts desc) rn "
            + "from crs_l_client_department l) l join crs_h_client c on c.id = l.client_id "
            + "join crs_h_department d on d.id = l.department_id where l.removed = 0 and l.rn = 1 and c.key = '%s'";

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private MetadataDao metadataDao;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private CustomSqlDao customSqlDao;
    @Autowired
    @SuppressWarnings("SpringJavaAutowiringInspection")
    private ClientPortalIntegrationDao clientPortalIntegrationDao;
    @Autowired
    private EntityMetaService entityMetaService;
    @Autowired
    private EntityService entityService;

    /**
     * Test for {@link ClientPortalIntegrationDao#startSynchronization()}.
     */
    @Test
    public void test01FirstSync() {
        syncAndCheckErrors();
    }

    /**
     * Changes client portal and syncs.
     * @param sql sql code
     */
    private void changeClientPortalAndSync(String sql) {
        metadataDao.execute(sql);

        String beforeMaxSyncDate = customSqlDao.getValue(MAX_LAST_SYNC_DATE_SQL);

        syncAndCheckErrors();

        String afterMaxSyncDate = customSqlDao.getValue(MAX_LAST_SYNC_DATE_SQL);
        assertThat(afterMaxSyncDate).isNotEqualTo(beforeMaxSyncDate);
    }

    /**
     * Syncs and check errors.
     */
    private void syncAndCheckErrors() {
        String beforeLogMaxId = customSqlDao.getValue(MAX_ID_LOG_SQL);

        clientPortalIntegrationDao.startSynchronization();

        Long lastLogId = (beforeLogMaxId != null && !beforeLogMaxId.isEmpty()) ? new Long(beforeLogMaxId) : 0L;
        String firstErrorText = customSqlDao.getValue(String.format(FIRST_ERROR_SQL, lastLogId));
        assertThat(firstErrorText).isNull();

        String afterLogMaxId = customSqlDao.getValue(MAX_ID_LOG_SQL);
        assertThat(afterLogMaxId).isEqualTo(beforeLogMaxId);
    }

    /**
     * Returns entity filterd by key value.
     * @param entityMeta entity meta
     * @param keyValue entity key value
     * @param ldts load date
     * @return entity
     */
    private Entity getEntity(EntityMeta entityMeta, String keyValue, LocalDateTime ldts) {
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(entityMeta.getKeyAttribute(), Operator.EQ, keyValue));
        List<? extends Entity> entities = entityService.getEntities(entityMeta, criteria, null, ldts);
        assertThat(entities).isNotEmpty();
        return entities.get(0);
    }

    /**
     * Test sync client category.
     */
    @Test
    public void test02SyncClientCategory() {
        LocalDateTime sysTimestamp = metadataDao.getSysTimestamp();
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(ClientCategoryAttributeMeta.METADATA_KEY, sysTimestamp);
        Entity entity = getEntity(entityMeta, ENTITY_KEY, sysTimestamp);

        changeClientPortalAndSync("update crs_cpi_client_category set last_update = systimestamp");

        Entity updatedEntity = getEntity(entityMeta, ENTITY_KEY, metadataDao.getSysTimestamp());
        ldtsEquals(entity, updatedEntity);
        multilangSatelliteLdtsDiffrerent(entity, updatedEntity, ClientCategoryAttributeMeta.NAME.getKey());
    }

    /**
     * Test sync client currency.
     */
    @Test
    public void test03SyncClientCurrency() {
        LocalDateTime sysTimestamp = metadataDao.getSysTimestamp();
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(ClientCurrencyAttributeMeta.METADATA_KEY, sysTimestamp);
        Entity entity = getEntity(entityMeta, ENTITY_KEY, sysTimestamp);

        changeClientPortalAndSync("update crs_cpi_client_currency set last_update = systimestamp");

        Entity updatedEntity = getEntity(entityMeta, ENTITY_KEY, metadataDao.getSysTimestamp());
        ldtsDifferent(entity, updatedEntity);
        multilangSatelliteLdtsDiffrerent(entity, updatedEntity, ClientCurrencyAttributeMeta.NAME.getKey());
    }

    /**
     * Test sync client country.
     */
    @Test
    public void test04SyncClientCountry() {
        LocalDateTime sysTimestamp = metadataDao.getSysTimestamp();
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(ClientCountryAttributeMeta.METADATA_KEY, sysTimestamp);
        Entity entity = getEntity(entityMeta, ENTITY_KEY, sysTimestamp);

        changeClientPortalAndSync("update crs_cpi_client_country set last_update = systimestamp");

        Entity updatedEntity = getEntity(entityMeta, ENTITY_KEY, metadataDao.getSysTimestamp());
        ldtsDifferent(entity, updatedEntity);
        multilangSatelliteLdtsDiffrerent(entity, updatedEntity, ClientCountryAttributeMeta.NAME.getKey());
    }

    /**
     * Test sync client industry.
     */
    @Test
    public void test05SyncClientIndustry() {
        LocalDateTime sysTimestamp = metadataDao.getSysTimestamp();
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(ClientIndustryAttributeMeta.METADATA_KEY, sysTimestamp);
        Entity entity = getEntity(entityMeta, ENTITY_KEY, sysTimestamp);

        changeClientPortalAndSync("update crs_cpi_client_industry set last_update = systimestamp");

        Entity updatedEntity = getEntity(entityMeta, ENTITY_KEY, metadataDao.getSysTimestamp());
        ldtsEquals(entity, updatedEntity);
        multilangSatelliteLdtsDiffrerent(entity, updatedEntity, ClientIndustryAttributeMeta.NAME.getKey());
    }

    /**
     * Test sync client type.
     */
    @Test
    public void test06SyncClientType() {
        LocalDateTime sysTimestamp = metadataDao.getSysTimestamp();
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(ClientTypeAttributeMeta.METADATA_KEY, sysTimestamp);
        Entity entity = getEntity(entityMeta, ENTITY_KEY, sysTimestamp);

        changeClientPortalAndSync("update crs_cpi_client_type_dict set last_update = systimestamp");

        Entity updatedEntity = getEntity(entityMeta, ENTITY_KEY, metadataDao.getSysTimestamp());
        ldtsDifferent(entity, updatedEntity);
        multilangSatelliteLdtsDiffrerent(entity, updatedEntity, ClientTypeAttributeMeta.NAME.getKey());
    }

    /**
     * Test sync client opf.
     */
    @Test
    public void test07SyncClientOpf() {
        LocalDateTime sysTimestamp = metadataDao.getSysTimestamp();
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(ClientOpfAttributeMeta.METADATA_KEY, sysTimestamp);
        Entity entity = getEntity(entityMeta, ENTITY_KEY, sysTimestamp);

        changeClientPortalAndSync(String.format("update crs_cpi_client_opf set last_update = systimestamp,countryid='%s'", COUNTRY_ID));

        Entity updatedEntity = getEntity(entityMeta, ENTITY_KEY, metadataDao.getSysTimestamp());
        ldtsEquals(entity, updatedEntity);
        multilangSatelliteLdtsDiffrerent(entity, updatedEntity, ClientOpfAttributeMeta.NAME.getKey());
        linkIdsDiffrerent(entity, updatedEntity, ClientOpfAttributeMeta.COUNTRY.getKey());
    }

    /**
     * Test sync client ogrn.
     */
    @Test
    public void test08SyncClientOgrn() {
        LocalDateTime sysTimestamp = metadataDao.getSysTimestamp();
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(ClientOgrnAttributeMeta.METADATA_KEY, sysTimestamp);
        Entity entity = getEntity(entityMeta, ENTITY_KEY, sysTimestamp);

        changeClientPortalAndSync(
                String.format("update crs_cpi_client_ogrn set last_update = systimestamp,reg_num='newvalue',countryid='%s'", COUNTRY_ID));

        Entity updatedEntity = getEntity(entityMeta, ENTITY_KEY, metadataDao.getSysTimestamp());
        ldtsDifferent(entity, updatedEntity);
        linkIdsDiffrerent(entity, updatedEntity, ClientOgrnAttributeMeta.COUNTRY.getKey());
    }

    /**
     * Test sync client inn.
     */
    @Test
    public void test09SyncClientInn() {
        LocalDateTime sysTimestamp = metadataDao.getSysTimestamp();
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(ClientInnAttributeMeta.METADATA_KEY, sysTimestamp);
        Entity entity = getEntity(entityMeta, ENTITY_KEY, sysTimestamp);

        changeClientPortalAndSync(
                String.format("update crs_cpi_client_inn set last_update = systimestamp,taxid='newvalue',countryid='%s'", COUNTRY_ID));

        Entity updatedEntity = getEntity(entityMeta, ENTITY_KEY, metadataDao.getSysTimestamp());
        ldtsDifferent(entity, updatedEntity);
        linkIdsDiffrerent(entity, updatedEntity, ClientInnAttributeMeta.COUNTRY.getKey());
    }

    /**
     * Test sync client segment.
     */
    @Test
    public void test10SyncClientSegment() {
        LocalDateTime sysTimestamp = metadataDao.getSysTimestamp();
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(ClientSegmentAttributeMeta.METADATA_KEY, sysTimestamp);
        Entity entity = getEntity(entityMeta, ENTITY_KEY, sysTimestamp);

        changeClientPortalAndSync(String.format("update crs_cpi_client_segment set last_update = systimestamp,currencyid='%s'", CURRENCY_ID));

        Entity updatedEntity = getEntity(entityMeta, ENTITY_KEY, metadataDao.getSysTimestamp());
        ldtsDifferent(entity, updatedEntity);
        linkIdsDiffrerent(entity, updatedEntity, ClientSegmentAttributeMeta.CURRENCY.getKey());
    }

    /**
     * Test sync client group.
     */
    @Test
    public void test11SyncClientGroup() {
        LocalDateTime sysTimestamp = metadataDao.getSysTimestamp();
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(ClientGroupAttributeMeta.METADATA_KEY, sysTimestamp);
        Entity entity = getEntity(entityMeta, ENTITY_KEY, sysTimestamp);

        changeClientPortalAndSync(
                String.format("update crs_cpi_client_group set last_update = systimestamp,is_vtb_daughter='%s',countryid=%s,"
                              + "industryid=%s,segmentid=%s,name_en='%s',name='%s',full_name_en='%s',full_name='%s',"
                              + "description_en='%s',description='%s'",
                              IS_VTB_DAUGHTER, COUNTRY_ID,
                              INDUSTRY_ID, SEGMENT_ID,
                              NEW_UNIQUE_NAME1_EN, NEW_UNIQUE_NAME1_RU, NEW_UNIQUE_NAME2_EN, NEW_UNIQUE_NAME2_RU, NEW_UNIQUE_NAME3_EN,
                              NEW_UNIQUE_NAME3_RU));

        Entity updatedEntity = getEntity(entityMeta, ENTITY_KEY, metadataDao.getSysTimestamp());
        ldtsDifferent(entity, updatedEntity);
        linkIdsDiffrerent(entity, updatedEntity, ClientGroupAttributeMeta.SEGMENT.getKey());
        linkIdsDiffrerent(entity, updatedEntity, ClientGroupAttributeMeta.INDUSTRY.getKey());
        linkIdsDiffrerent(entity, updatedEntity, ClientGroupAttributeMeta.COUNTRY.getKey());
        multilangSatelliteLdtsDiffrerent(entity, updatedEntity, ClientGroupAttributeMeta.NAME.getKey());
        multilangSatelliteLdtsDiffrerent(entity, updatedEntity, ClientGroupAttributeMeta.FULL_NAME.getKey());
        multilangSatelliteLdtsDiffrerent(entity, updatedEntity, ClientGroupAttributeMeta.DESCRIPTION.getKey());
    }

    /**
     * Test sync client.
     */
    @Test
    public void test12SyncClient() {
        changeClientPortalAndSync(CLIENT_ACTIVE_SQL);
        changeClientPortalAndSync("update crs_cpi_client set last_update = systimestamp");

        LocalDateTime sysTimestamp = metadataDao.getSysTimestamp();
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(ClientAttributeMeta.METADATA_KEY, sysTimestamp);
        Entity entity = getEntity(entityMeta, CLIENT_KEY, sysTimestamp);

        changeClientPortalAndSync(
                String.format("update crs_cpi_client "
                              + "set last_update = systimestamp,categoryid=%s,countryid=%s,industryid=%s,opfid=%s,segmentid=%s,"
                              + "name_en='%s',name='%s',full_name_en='%s',full_name='%s'",
                              CATEGORY_ID, COUNTRY_ID, INDUSTRY_ID, OPF_ID, SEGMENT_ID, NEW_UNIQUE_NAME1_EN, NEW_UNIQUE_NAME1_RU, NEW_UNIQUE_NAME2_EN,
                              NEW_UNIQUE_NAME2_RU
                ));

        Entity updatedEntity = getEntity(entityMeta, CLIENT_KEY, metadataDao.getSysTimestamp());
        ldtsEquals(entity, updatedEntity);
        //ogrn, inn, client type, client group syncs when syncs other client portal view
        linkIdsDiffrerent(entity, updatedEntity, ClientAttributeMeta.CATEGORY.getKey());
        linkIdsDiffrerent(entity, updatedEntity, ClientAttributeMeta.COUNTRY.getKey());
        linkIdsDiffrerent(entity, updatedEntity, ClientAttributeMeta.INDUSTRY.getKey());
        linkIdsDiffrerent(entity, updatedEntity, ClientAttributeMeta.OPF.getKey());
        linkIdsDiffrerent(entity, updatedEntity, ClientAttributeMeta.SEGMENT.getKey());
        multilangSatelliteLdtsDiffrerent(entity, updatedEntity, ClientAttributeMeta.NAME.getKey());
        multilangSatelliteLdtsDiffrerent(entity, updatedEntity, ClientAttributeMeta.FULL_NAME.getKey());
    }

    /**
     * Test sync client removing.
     */
    @Test
    public void test13SyncClientRemoving() {
        changeClientPortalAndSync(CLIENT_ACTIVE_SQL);
        changeClientPortalAndSync("update crs_cpi_client set last_update = systimestamp");

        LocalDateTime sysTimestamp = metadataDao.getSysTimestamp();
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(ClientAttributeMeta.METADATA_KEY, sysTimestamp);
        Entity entity = getEntity(entityMeta, CLIENT_KEY, sysTimestamp);

        changeClientPortalAndSync(CLIENT_NOT_ACTIVE_SQL);

        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(entityMeta.getKeyAttribute(), Operator.EQ, CLIENT_KEY));
        List<? extends Entity> entities = entityService.getEntities(entityMeta, criteria, null, metadataDao.getSysTimestamp());
        assertThat(entities).isEmpty();

        changeClientPortalAndSync(CLIENT_ACTIVE_SQL);
        changeClientPortalAndSync("update crs_cpi_client set last_update = systimestamp");

        Entity updatedEntity = getEntity(entityMeta, CLIENT_KEY, metadataDao.getSysTimestamp());
        assertThat(updatedEntity).isNotNull();
    }

    /**
     * Test sync client removing.
     */
    @Test
    public void test14ClientParticipant() {
        String countBeforeSync = customSqlDao.getValue(String.format(TABLE_COUNT_SQL, "crs_l_client_group"));
        changeClientPortalAndSync("update crs_cpi_group_participant set last_update = systimestamp");
        String countAfterSync = customSqlDao.getValue(String.format(TABLE_COUNT_SQL, "crs_l_client_group"));
        assertThat(countAfterSync).isNotEqualTo(countBeforeSync);
    }

    /**
     * Test sync client department.
     */
    @Test
    public void test15ClientDepartment() {
        metadataDao.execute(String.format(TEST15_RESET, CLIENT_KEY, CLIENT2_KEY));

        assertThat(customSqlDao.getValue(String.format(GET_CLIENT_DEPARTMENT_KEYS_SQL, CLIENT_KEY))).isNullOrEmpty();
        assertThat(customSqlDao.getValue(String.format(GET_CLIENT_DEPARTMENT_KEYS_SQL, CLIENT2_KEY))).isNullOrEmpty();

        changeClientPortalAndSync("update crs_cpi_client_department set last_update = systimestamp");

        assertThat(customSqlDao.getValue(String.format(GET_CLIENT_DEPARTMENT_KEYS_SQL, CLIENT_KEY))).isEqualTo("9,46");
        assertThat(customSqlDao.getValue(String.format(GET_CLIENT_DEPARTMENT_KEYS_SQL, CLIENT2_KEY))).isEqualTo("9,206");

        metadataDao.execute(String.format("update crs_cpi_client_department set departmentid = %s where vtb_legalentityid = '%s'",
                                          DEPARTMENT_ID, CLIENT_KEY));
        metadataDao.execute(String.format("update crs_cpi_client_department set departmentid = %s where vtb_legalentityid = '%s' "
                                          + "and departmentid = %s", DEPARTMENT_ID, CLIENT2_KEY, NOT_IN_CRS_DEPARTMENT_ID));
        changeClientPortalAndSync("update crs_cpi_client_department set last_update = systimestamp");

        assertThat(customSqlDao.getValue(String.format(GET_CLIENT_DEPARTMENT_KEYS_SQL, CLIENT_KEY))).isEqualTo("46");
        assertThat(customSqlDao.getValue(String.format(GET_CLIENT_DEPARTMENT_KEYS_SQL, CLIENT2_KEY))).isEqualTo("9,46");

        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(ClientAttributeMeta.METADATA_KEY, null);
        List<Department> departments = ((LinkedEntityAttribute<Department>) getEntity(entityMeta, CLIENT_KEY, null)
                .getAttribute(ClientAttributeMeta.DEPARTMENT.getKey())).getEntityList();
        assertThat(departments).isNotEmpty();
        assertThat(departments.size()).isEqualTo(1);
        assertThat(departments.get(0).getKey()).isEqualTo("46");

        String countBeforeSync = customSqlDao.getValue(String.format(TABLE_COUNT_SQL, "crs_l_client_department"));
        changeClientPortalAndSync("update crs_cpi_client_department set last_update = systimestamp");
        String countAfterSync = customSqlDao.getValue(String.format(TABLE_COUNT_SQL, "crs_l_client_department"));
        assertThat(countAfterSync).isEqualTo(countBeforeSync);
    }

    /**
     * Test sync client department.
     */
    @Test
    public void test16ClientDepartment() {
        changeClientPortalAndSync("update crs_cpi_department set last_update = systimestamp");
        String countHubAfterSync = customSqlDao.getValue(String.format(TABLE_COUNT_SQL, "crs_h_client_department"));
        assertThat(Long.parseLong(countHubAfterSync)).isGreaterThan(0);
        String countNameLinkBeforeSync = customSqlDao.getValue(String.format(TABLE_COUNT_SQL, "crs_l_client_department_name"));
        String countFullNameLinkBeforeSync = customSqlDao.getValue(String.format(TABLE_COUNT_SQL, "crs_l_client_department_fullname"));
        changeClientPortalAndSync(TEST16_TEXT);
        String countNameLinkAfterSync = customSqlDao.getValue(String.format(TABLE_COUNT_SQL, "crs_l_client_department_name"));
        String countFullNameLinkAfterSync = customSqlDao.getValue(String.format(TABLE_COUNT_SQL, "crs_l_client_department_fullname"));
        assertThat(countNameLinkBeforeSync).isNotEqualTo(countNameLinkAfterSync);
        assertThat(countFullNameLinkBeforeSync).isEqualTo(countFullNameLinkAfterSync);
    }

    /**
     * Checks, whether LDTS are different for two entities.
     * @param oldEntity old entity
     * @param newEntity new entity
     */
    private void ldtsDifferent(Entity oldEntity, Entity newEntity) {
        assertThat(oldEntity).isNotNull();
        assertThat(newEntity).isNotNull();
        assertThat(newEntity.getLdts()).isNotEqualTo(oldEntity.getLdts());
    }

    /**
     * Checks, whether LDTS are different for two entities.
     * @param oldEntity old entity
     * @param newEntity new entity
     */
    private void ldtsEquals(Entity oldEntity, Entity newEntity) {
        assertThat(oldEntity).isNotNull();
        assertThat(newEntity).isNotNull();
        assertThat(newEntity.getLdts()).isEqualTo(oldEntity.getLdts());
    }

    /**
     * Checks, whether multilanguage link LDTS are different for two entities.
     * @param oldEntity old entity
     * @param newEntity new entity
     * @param attributeKey attribute key
     */
    private void multilangSatelliteLdtsDiffrerent(Entity oldEntity, Entity newEntity, String attributeKey) {
        assertThat(((MultilangAttribute) newEntity.getAttribute(attributeKey)).getSatelliteLdts())
                .isNotEqualTo(((MultilangAttribute) oldEntity.getAttribute(attributeKey)).getSatelliteLdts());
    }

    /**
     * Checks, whether link identifiers are different for two entities.
     * @param oldEntity old entity
     * @param newEntity new entity
     * @param attributeKey attribute key
     */
    private void linkIdsDiffrerent(Entity oldEntity, Entity newEntity, String attributeKey) {
        assertThat(((LinkedEntityAttribute<?>) newEntity.getAttribute(attributeKey)).getEntityAttributeList().get(0).getLinkId())
                .isNotEqualTo(((LinkedEntityAttribute<?>) oldEntity.getAttribute(attributeKey)).getEntityAttributeList().get(0).getLinkId());
    }

}
