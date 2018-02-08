package ru.masterdm.crs.test.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ru.masterdm.crs.dao.entity.meta.MetadataDao;
import ru.masterdm.crs.domain.AuditAction;
import ru.masterdm.crs.domain.AuditLog;
import ru.masterdm.crs.domain.AuditLog.AuditLogFilter;
import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.calc.EvalLang;
import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.calc.FormulaData;
import ru.masterdm.crs.domain.calc.FormulaResultType;
import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.service.AuditService;
import ru.masterdm.crs.service.AuditTestService;
import ru.masterdm.crs.service.CalcService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.SecurityService;
import ru.masterdm.crs.util.annotation.Audit;
import ru.masterdm.crs.util.annotation.CurrentTimeStamp;

/**
 * Test collection for audit.
 * @author Kuzmin Mikhail
 * @author Alexey Chalov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml",
                                   "classpath:META-INF/spring/crs-security-test.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuditServiceTest {

    @Autowired
    private MetadataDao metadataDao;
    @Autowired
    private AuditService auditService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private AuditTestService auditTestService;
    @Autowired
    private EntityService entityService;
    @Autowired
    private CalcService calcService;

    private User userEntity;

    /**
     * Test for audit creation.
     */
    @Test
    public void test01AuditCreate() {
        Map<String, Object> filter = new HashMap<>();
        RowRange rowRange = new RowRange(1, 1);

        Long entityId = userEntity.getId();
        EntityMeta userEntityMeta = userEntity.getMeta();
        Long entityMetaId = userEntityMeta.getId();

        /* default */
        auditTestService.defaultEvalCalculation(userEntity);
        filter.put(AuditLogFilter.ACTION.name(), AuditAction.EVAL_CALCULATION.name());
        List<AuditLog> logs = auditService.getLogs(filter, rowRange);
        assertThat(logs.size()).isEqualTo(1);

        /* custom: create calculation */
        userEntity.setId(null);
        auditTestService.customCreateCalculation(userEntity, entityId);
        filter.put(AuditLogFilter.ACTION.name(), AuditAction.CREATE_CALCULATION.name());
        logs = auditService.getLogs(filter, rowRange);
        assertThat(logs.size()).isEqualTo(1);

        /* custom: delete entity meta */
        auditTestService.customDeleteEntityMeta(userEntityMeta);
        filter.put(AuditLogFilter.ACTION.name(), AuditAction.DELETE_ENTITY_META.name());
        logs = auditService.getLogs(filter, rowRange);
        assertThat(logs.size()).isEqualTo(1);

        /* custom: persist entity meta (existing one) */
        auditTestService.customCreateEntityMeta(userEntityMeta, entityMetaId);
        filter.put(AuditLogFilter.ACTION.name(), AuditAction.EDIT_ENTITY_META.name());
        logs = auditService.getLogs(filter, rowRange);
        assertThat(logs.size()).isEqualTo(1);

        /* custom: persist entity meta (existing one) */
        userEntityMeta.setId(null);
        auditTestService.customCreateEntityMeta(userEntityMeta, entityMetaId);
        filter.put(AuditLogFilter.ACTION.name(), AuditAction.CREATE_ENTITY_META.name());
        logs = auditService.getLogs(filter, rowRange);
        assertThat(logs.size()).isEqualTo(1);
    }

    /**
     * Test for audit record content.
     */
    @Test
    public void test02AuditContent() {
        Long entityId = userEntity.getId();
        userEntity.setId(null);
        auditTestService.customCreateCalculation(userEntity, entityId);

        Map<String, Object> filter = new HashMap<>();
        filter.put(AuditLogFilter.ACTION.name(), AuditAction.CREATE_CALCULATION);
        RowRange rowRange = new RowRange(1, 1);
        List<AuditLog> logs = auditService.getLogs(filter, rowRange);

        assertThat(logs.get(0).getEntity().getMeta().getKey()).isEqualTo(userEntity.getMeta().getKey());
        assertThat(logs.get(0).getEntity().getId()).isEqualTo(userEntity.getId());
        assertThat(logs.get(0).getExecutor().getId()).isEqualTo(userEntity.getHubId());
        assertThat(logs.get(0).getAction()).isEqualTo(AuditAction.CREATE_CALCULATION);

        filter.put(AuditLogFilter.AUTHOR.name(), userEntity.getFullName());
        logs = auditService.getLogs(filter, rowRange);
        assertThat(logs.get(0).getExecutor().getName()).isEqualTo(userEntity.getName());

        filter.put(AuditLogFilter.DATE_TO.name(), LocalDateTime.now());
        logs = auditService.getLogs(filter, rowRange);
        assertThat(logs.size()).isEqualTo(1);

        filter.put(AuditLogFilter.DATE_TO.name(), LocalDateTime.now().minusDays(1));
        logs = auditService.getLogs(filter, rowRange);
        assertThat(logs.size()).isEqualTo(0);

        filter = new HashMap<>();
        filter.put(AuditLogFilter.DATE_FROM.name(), LocalDate.now());
        logs = auditService.getLogs(filter, rowRange);
        assertThat(logs.size()).isEqualTo(1);

        filter.put(AuditLogFilter.DATE_FROM.name(), LocalDate.now().plusDays(1));
        logs = auditService.getLogs(filter, rowRange);
        assertThat(logs.size()).isEqualTo(0);

        filter = new HashMap<>();
        filter.put(AuditLogFilter.OBJECT.name(), userEntity.getKey());
        logs = auditService.getLogs(filter, rowRange);
        assertThat(logs.get(0).getEntity().getKey()).isEqualTo(userEntity.getKey());
    }

    /**
     * Test for {@link Audit} annotation in conjunction with {@link CurrentTimeStamp} one.
     */
    @Test
    public void test03CurrentTimestampWithAudit() {
        LocalDateTime ts = auditTestService.auditWithCurrentTimestamp(null);
        assertThat(ts).isNotNull();
    }

    /**
     * Test for transaction rollback when method marked for audit throws exception.
     */
    @Test
    public void test04RollbackTransaction() {
        try {
            auditTestService.transactionRollback();
        } catch (Exception e) {
            /* expected, done by intent */
            e = null;
        }
        RowRange rowRange = new RowRange(1, 1);
        List<AuditLog> logs = auditService.getLogs(null, rowRange);
        assertThat(logs.isEmpty()).isTrue();
    }

    /**
     * Test for audit for model operations.
     */
    @Test
    public void test05AuditModel() {
        Map<String, Object> filter = new HashMap<>();
        RowRange rowRange = new RowRange(1, 1);
        List<AuditLog> logs;
        String modelKey = "MT_" + UUID.randomUUID();
        Model model = (Model) entityService.newEmptyEntity(Model.METADATA_KEY);
        model.setKey(modelKey);
        model.setName(new MultilangDescription("Модель для теста " + modelKey, "Model for test " + modelKey));
        model.setPublished(false);
        model.setActuality(metadataDao.getSysTimestamp());
        calcService.persistModel(model);

        /* custom: create model */
        filter.put(AuditLogFilter.ACTION.name(), AuditAction.CREATE_MODEL.name());
        logs = auditService.getLogs(filter, rowRange);
        assertThat(logs.size()).isEqualTo(1);

        /* custom: publish model */
        calcService.publishModel(model);
        filter.put(AuditLogFilter.ACTION.name(), AuditAction.PUBLISH_MODEL.name());
        logs = auditService.getLogs(filter, rowRange);
        assertThat(logs.size()).isEqualTo(1);

        /* custom: edit model */
        model.setName(new MultilangDescription("Новая модель для теста" + modelKey, "New model for test " + modelKey));
        calcService.persistModel(model);
        filter.put(AuditLogFilter.ACTION.name(), AuditAction.EDIT_MODEL.name());
        logs = auditService.getLogs(filter, rowRange);
        assertThat(logs.size()).isEqualTo(1);

        /* custom: delete model */
        calcService.removeModel(model);
        filter.put(AuditLogFilter.ACTION.name(), AuditAction.REMOVE_MODEL.name());
        logs = auditService.getLogs(filter, rowRange);
        assertThat(logs.size()).isEqualTo(1);
    }

    /**
     * Test for audit for formula operations.
     */
    @Test
    public void test06AuditFormula() {
        Map<String, Object> filter = new HashMap<>();
        RowRange rowRange = new RowRange(1, 1);
        List<AuditLog> logs;
        String formulaKey = "FT_" + UUID.randomUUID();
        Formula formula = new Formula();
        formula.setKey(formulaKey);
        formula.setName(new MultilangDescription("Формула для теста " + formulaKey, "Formula for test " + formulaKey));
        formula.setFormula(new FormulaData());
        formula.getFormula().setData("2*2");
        formula.setEvalLang(EvalLang.NASHORN.name().toLowerCase());
        formula.setResultType(FormulaResultType.STRING);
        formula.setLdts(metadataDao.getSysTimestamp());
        calcService.persistFormula(formula);
        long  formulaId = formula.getId();

        /* custom: create formula */
        filter.put(AuditLogFilter.ACTION.name(), AuditAction.CREATE_FORMULA.name());
        logs = auditService.getLogs(filter, rowRange);
        assertThat(logs.size()).isEqualTo(1);

        /* custom: edit formula */
        formula.setId(formulaId);
        calcService.persistFormula(formula);
        filter.put(AuditLogFilter.ACTION.name(), AuditAction.EDIT_FORMULA.name());
        logs = auditService.getLogs(filter, rowRange);
        assertThat(logs.size()).isEqualTo(1);

        /* custom: remove formula */
        calcService.removeFormula(formula);
        filter.put(AuditLogFilter.ACTION.name(), AuditAction.REMOVE_FORMULA.name());
        logs = auditService.getLogs(filter, rowRange);
        assertThat(logs.size()).isEqualTo(1);
    }

    /**
     * Prepares initial test data.
     * @throws Exception exception
     */
    @Before
    public void setup() throws Exception {
        userEntity = securityService.getCurrentUser();
        metadataDao.execute("truncate table crs_audit_log");
    }
}
