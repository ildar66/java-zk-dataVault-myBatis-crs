package ru.masterdm.crs.test.service.calc;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.masterdm.crs.SecurityContextBeanPostProcessor.ADMINWF_LOGIN;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
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
import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.calc.FormulaResult;
import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.CalculationProfileAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.service.CalcService;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.SecurityService;
import ru.masterdm.crs.service.calc.CalculationService;

/**
 * Formula result service tests.
 * @author Pavel Masalov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml",
                                   "classpath:META-INF/spring/crs-security-test.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FormulaResultServiceTest {

    private static final String CALCULATION_KEY = "FRC01";
    private static final String CALC_KEY_WH_EX = "CALC_WH_EX";
    private static final String CALC_KEY_NO_EX = "CALC_NO_EX";

    private static final String FORMULA_ROOT_KEY = "FRF01";
    private static final String FORMULA_CHILD_KEY = "FRF01_CH";

    private static Formula formula;
    private static Formula childFormula1;
    private static Calculation calculation;
    private Entity profile;

    private static Model model;

    @Autowired
    private CalcService calcService;
    @Autowired
    private EntityService entityService;
    @Autowired
    private EntityMetaService entityMetaService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private MetadataDao metadataDao;

    @Autowired
    private CalculationService calculationService;

    private EntityMeta calculationEntityMeta;
    private EntityMeta modelEntityMeta;
    private EntityMeta profileEntityMeta;

    /**
     * Create test data at database.
     * @param key test object key
     */
    private void createTestCalculationIfNotexists(String key) {
        if (entityService.getEntityIdByKey(calculationEntityMeta, key) == null) {
            calculation = (Calculation) entityService.newEmptyEntity(Calculation.METADATA_KEY);
            calculation.setKey(key);
            calculation.setName("draft test " + key);
            calculation.setPublished(false);
            LocalDateTime now = LocalDateTime.now();
            calculation.setActuality(now.toLocalDate());
            calculation.setDataActuality(now);
            calculation.setAuthor(securityService.getCurrentUser());
            calculation.setModel(model);
            calculation.setProfiles(Collections.singletonList(profile));
            calcService.persistCalculation(calculation);
        } else {
            Criteria criteria = new Criteria();
            criteria.getWhere().addItem(new WhereItem(calculationEntityMeta.getKeyAttribute(), Operator.EQ, key));
            calculation = calcService.getCalculations(criteria, null, null).get(0);
        }
    }

    /**
     * Create test data at database.
     * @param key test object key
     */
    private void createTestModelIfNotexists(String key) {
        if (entityService.getEntityIdByKey(modelEntityMeta, key) == null) {
            model = (Model) entityService.newEmptyEntity(Model.METADATA_KEY);
            model.setKey(key);
            model.setName(new MultilangDescription("черновик тест " + key, "draft test " + key));
            model.setPublished(false);
            model.setActuality(metadataDao.getSysTimestamp().plusDays(1));
            model.setFormulas(Arrays.asList(formula, childFormula1));
            calcService.persistModel(model);
        } else {
            Criteria criteria = new Criteria();
            criteria.getWhere().addItem(new WhereItem(modelEntityMeta.getKeyAttribute(), Operator.EQ, key));
            model = calcService.getModels(criteria, null, null).get(0);
        }
    }

    /**
     * Create test formulas.
     * @param key key of parent
     * @param keyCh key of child
     */
    private void createTestFormulaIfNotExists(String key, String keyCh) {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        formula.setKey(key);
        childFormula1.setKey(keyCh);

        Formula f = calcService.getFormulaByKey(childFormula1.getKey(), ldts);
        if (f == null) {
            calcService.persistFormula(childFormula1);
            ldts = metadataDao.getSysTimestamp();
            childFormula1 = calcService.getFormulaByKey(childFormula1.getKey(), ldts);
        } else {
            childFormula1 = f;
        }

        f = calcService.getFormulaByKey(formula.getKey(), ldts);
        if (f == null) {
            calcService.persistFormula(formula);
            ldts = metadataDao.getSysTimestamp();
            formula = calcService.getFormulaByKey(formula.getKey(), ldts);
        } else {
            formula = f;
        }
    }

    /**
     * Formula result write test.
     * Create calculation.
     * Create 2 formulas.
     * write formulas result with links.
     */
    @Test
    public void test01WriteFormulaResult() {
        final BigDecimal f1 = new BigDecimal(1.1);
        final BigDecimal f2 = new BigDecimal(2.2);
        createTestFormulaIfNotExists(FORMULA_ROOT_KEY, FORMULA_CHILD_KEY);
        createTestModelIfNotexists("FRM01");
        createTestCalculationIfNotexists(CALCULATION_KEY);

        LocalDateTime ldts = metadataDao.getSysTimestamp();
        //Criteria criteria = new Criteria();
        //criteria.getWhere().addItem(new WhereItem(calculationEntityMeta.getKeyAttribute(), Operator.EQ, CALCULATION_KEY));
        //List<Calculation> calculations = calcService.getCalculations(criteria, null, ldts);
        //assertThat(calculations).hasSize(1);

        FormulaResult formulaResult = (FormulaResult) entityService.newEmptyEntity(FormulaResult.METADATA_KEY);
        formulaResult.setNumberResult(f1);
        formulaResult.setOutput("test");
        formula.setFormulaResult(formulaResult, calculation, profile);

        formulaResult = (FormulaResult) entityService.newEmptyEntity(FormulaResult.METADATA_KEY);
        formulaResult.setNumberResult(f2);
        Exception ex = new Exception();
        formulaResult.setException(ExceptionUtils.getStackTrace(ex));
        formulaResult.setOutput(null);
        childFormula1.setFormulaResult(formulaResult, calculation, profile);

        calculationService.persistsFormulaResultsAndTrinityLinks(calculation, formula, profile, ldts);
        calculationService.persistsFormulaResultsAndTrinityLinks(calculation, childFormula1, profile, ldts);
    }

    /**
     * Test read calculation with formula result.
     */
    @Test
    public void test02ReadFormulaResult() {
        //run 02 before
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(calculationEntityMeta.getKeyAttribute(), Operator.EQ, CALCULATION_KEY));
        List<Calculation> calculations = calcService.getCalculations(criteria, null, ldts);
        assertThat(calculations).hasSize(1);

        // has model with 2 formulas
        Calculation calculation = calculations.get(0);
        assertThat(calculation.getModel()).isNotNull();
        assertThat(calculation.getModel().getFormulas()).hasSize(2);
        for (Formula formula : calculation.getModel().getFormulas()) {
            assertThat(formula.getFormulaResult(calculation, profile)).as("Formula key=%s", formula.getKey()).isNotNull();

            if (formula.getKey().equalsIgnoreCase(FORMULA_ROOT_KEY)) {
                Assert.assertEquals("test", formula.getFormulaResult(calculation, profile).getOutput().trim());
            }
            if (formula.getKey().equalsIgnoreCase(FORMULA_CHILD_KEY)) {
                Assert.assertNull(formula.getFormulaResult(calculation, profile).getOutput());
            }
        }
    }

    /**
     * Test if calculations have exceptions.
     */
    @Test
    public void test03CheckFormulaResultExceptions() {
        createTestFormulaIfNotExists(FORMULA_ROOT_KEY, FORMULA_CHILD_KEY);
        createTestModelIfNotexists("TSTMODEL");
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        FormulaResult formulaResult;

        // Create calculation with exception
        createTestCalculationIfNotexists(CALC_KEY_WH_EX);

        formulaResult = (FormulaResult) entityService.newEmptyEntity(FormulaResult.METADATA_KEY);
        formulaResult.setNumberResult(new BigDecimal(Math.PI));
        formulaResult.setOutput(null);
        formulaResult.setException(ExceptionUtils.getStackTrace(new Exception()));
        formula.setFormulaResult(formulaResult, calculation, profile);
        calculationService.persistsFormulaResultsAndTrinityLinks(calculation, formula, profile, ldts);

        formulaResult = (FormulaResult) entityService.newEmptyEntity(FormulaResult.METADATA_KEY);
        formulaResult.setNumberResult(new BigDecimal(Math.E));
        formulaResult.setOutput("E");
        childFormula1.setFormulaResult(formulaResult, calculation, profile);
        calculationService.persistsFormulaResultsAndTrinityLinks(calculation, childFormula1, profile, ldts);

        assertThat(calcService.isCalculationHasException(calculation)).isTrue();

        // Create calculation without exception
        createTestCalculationIfNotexists(CALC_KEY_NO_EX);

        formulaResult = (FormulaResult) entityService.newEmptyEntity(FormulaResult.METADATA_KEY);
        formulaResult.setNumberResult(new BigDecimal(Math.PI));
        formulaResult.setOutput("PI");
        formula.setFormulaResult(formulaResult, calculation, profile);
        calculationService.persistsFormulaResultsAndTrinityLinks(calculation, formula, profile, ldts);

        formulaResult = (FormulaResult) entityService.newEmptyEntity(FormulaResult.METADATA_KEY);
        formulaResult.setNumberResult(new BigDecimal(Math.E));
        formulaResult.setOutput("E");
        childFormula1.setFormulaResult(formulaResult, calculation, profile);
        calculationService.persistsFormulaResultsAndTrinityLinks(calculation, childFormula1, profile, ldts);

        assertThat(calcService.isCalculationHasException(calculation)).isFalse();
    }

    /**
     * Prepares initial test data.
     * @throws Exception exception
     */
    @Before
    public void setup() throws Exception {
        securityService.defineSecurityContext(ADMINWF_LOGIN);
        calculationEntityMeta = entityMetaService.getEntityMetaByKey(Calculation.METADATA_KEY, null);
        modelEntityMeta = entityMetaService.getEntityMetaByKey(Model.METADATA_KEY, null);
        profileEntityMeta = entityMetaService.getEntityMetaByKey(CalculationProfileAttributeMeta.METADATA_KEY, null);

        profile = entityService.getEntity(profileEntityMeta, "RATED", null);
        assertThat(profile).isNotNull();
    }

    /**
     * Prepares initial test data.
     * @throws Exception exception
     */
    @BeforeClass
    public static void setupClass() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        formula = objectMapper.readValue(FormulaResultServiceTest.class.getResourceAsStream("formula.json"), Formula.class);
        childFormula1 = objectMapper.readValue(FormulaResultServiceTest.class.getResourceAsStream("formula_child_1.json"), Formula.class);
        formula.addChild("childFormula1", childFormula1);
    }
}
