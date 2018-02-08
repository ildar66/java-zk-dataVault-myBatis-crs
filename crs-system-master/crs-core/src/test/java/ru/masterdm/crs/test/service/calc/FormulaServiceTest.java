package ru.masterdm.crs.test.service.calc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static ru.masterdm.crs.SecurityContextBeanPostProcessor.ADMINWF_LOGIN;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
import ru.masterdm.crs.domain.calc.ClassifierAttributeMeta;
import ru.masterdm.crs.domain.calc.EvalLang;
import ru.masterdm.crs.domain.calc.EvalResult;
import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.calc.FormulaResultType;
import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.criteria.Conjunction;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeLocale;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.CalculationProfileAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.FormulaType;
import ru.masterdm.crs.exception.calc.CalculationErrorCode;
import ru.masterdm.crs.exception.calc.CalculationException;
import ru.masterdm.crs.exception.calc.FormulaCyclicDependencyException;
import ru.masterdm.crs.exception.calc.FormulaErrorCode;
import ru.masterdm.crs.exception.calc.FormulaException;
import ru.masterdm.crs.service.CalcService;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.SecurityService;
import ru.masterdm.crs.service.calc.FormulaService;
import ru.masterdm.crs.util.CollectorUtils;

/**
 * {@link CalcService} test collection.
 * @author Alexey Chalov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml",
                                   "classpath:META-INF/spring/crs-security-test.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FormulaServiceTest {

    private static final String DEFAULT_CALC_PROFILE = "RATED";

    private static Formula formula;
    private static Formula childFormula1;
    private static Formula childFormula2;

    @Autowired
    private CalcService calcService;
    @Autowired
    private FormulaService formulaService;
    @Autowired
    private SecurityService securityService;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private MetadataDao metadataDao;
    @Autowired
    private EntityService entityService;
    @Autowired
    private EntityMetaService entityMetaService;

    private boolean setupDone = false;

    private EntityMeta calculationEntityMeta;
    private EntityMeta modelEntityMeta;
    private EntityMeta formulaMetadata;
    private EntityMeta profileEntityMeta;
    private Entity profile;

    private EntityMeta entityMetaClassifier01;
    private EntityMeta entityMetaClassifier02;
    private EntityMeta entityMetaClassifier03;
    private EntityMeta entityMetaClassifier01R;

    /**
     * Helper structure to hold ol reference to formula kind.
     */
    private class FormulaBundle {

        private Formula formula;
        private Formula childFormula1;
        private Formula childFormula2;

        /**
         * Persists formula bundle if not exists.
         */
        private void persists() {
            if (!calcService.isFormulaExists(formula.getKey())) {
                calcService.persistFormula(childFormula1);
                calcService.persistFormula(childFormula2);
                calcService.persistFormula(formula);
            }
        }
    }

    public static final String FTS_1 = "FTS1";
    public static final String FTS_2 = "FTS2";
    private static FormulaBundle fts1, fts2; // formula trees test data

    public static final String FCYT_1 = "FCYT1";
    public static final String FCYT_2 = "FCYT2";
    private static FormulaBundle fcyt1, fcyt2; // cycle test
    /*
    A
        C

    B
        D
            C
                +B error
     */

    /**
     * Test for {@link CalcService#persistFormula(Formula)}.
     */
    @Test
    public void test01PersistFormula() {
        AttributeLocale locale = AttributeLocale.RU;

        calcService.persistFormula(childFormula1);
        calcService.persistFormula(childFormula2);
        formula.setEvalLang("nolang");
        calcService.persistFormula(formula);

        Assert.assertNotNull(formula.getId());

        Formula dbFormula = calcService.getFormulaByKey(formula.getKey(), metadataDao.getSysTimestamp());
        Assert.assertEquals(1, dbFormula.getChildren().size());
        Assert.assertEquals(childFormula1.getKey(), dbFormula.getChildren().get(0).getRight().getKey());
        Assert.assertEquals("childFormula1", dbFormula.getChildren().get(0).getLeft());

        Long formulaId = formula.getId();
        Long formulaDataId = formula.getFormula().getId();
        formula.setEvalLang(EvalLang.NASHORN.name().toLowerCase());
        formula.getFormula().setData(formula.getFormula().getData() + "1");
        formula.getChildren().clear();
        formula.addChild("childFormula2", childFormula2);
        calcService.persistFormula(formula);

        Assert.assertNotEquals(formula.getId(), formulaId);
        Assert.assertNotEquals(formula.getFormula().getId(), formulaDataId);
        dbFormula = calcService.getFormulaByKey(formula.getKey(), metadataDao.getSysTimestamp());
        Assert.assertEquals(1, dbFormula.getChildren().size());
        Assert.assertEquals(childFormula2.getKey(), dbFormula.getChildren().get(0).getRight().getKey());
        Assert.assertEquals("childFormula2", dbFormula.getChildren().get(0).getLeft());

        formula.getChildren().clear();
        calcService.persistFormula(formula);

        dbFormula = calcService.getFormulaByKey(formula.getKey(), metadataDao.getSysTimestamp());
        Assert.assertEquals(0, dbFormula.getChildren().size());
        childFormula1.addChild("childFormula2", childFormula2);
        calcService.persistFormula(childFormula1);
        childFormula2.addChild("formula", formula);
        calcService.persistFormula(childFormula2);
        formula.addChild("childFormula1", childFormula1);
        try {
            calcService.persistFormula(formula);
            Assert.fail("Expected " + FormulaCyclicDependencyException.class.getSimpleName() + ", but did not get it.");
        } catch (FormulaCyclicDependencyException e) {
            Assert.assertEquals(Integer.parseInt("3"), e.getFormulas().size());
        }

        formula.getChildren().clear();
        calcService.persistFormula(formula);
        childFormula1.getChildren().clear();
        calcService.persistFormula(childFormula1);
        childFormula2.getChildren().clear();
        calcService.persistFormula(childFormula2);

        dbFormula = calcService.getFormulaByKey(formula.getKey(), metadataDao.getSysTimestamp());
        Assert.assertEquals(0, dbFormula.getChildren().size());
        formula.addChild("childFormula", childFormula1);
        formula.addChild("childFormula", childFormula2);
        assertThatThrownBy(() -> calcService.persistFormula(formula))
                .isInstanceOf(FormulaException.class)
                .hasFieldOrPropertyWithValue("errorCode", FormulaErrorCode.DUPLICATE_ATTRIBUTE_NAME);

        childFormula1.setType(FormulaType.LIBRARY);
        calcService.persistFormula(childFormula1);
        calcService.persistFormula(formula);

        Formula newFormula = calcService.getFormulaByKey(childFormula2.getKey(), metadataDao.getSysTimestamp());
        newFormula.setType(FormulaType.LIBRARY);

        newFormula.getChildren().clear();
        newFormula.addChild("temp", formula);
        assertThatThrownBy(() -> calcService.persistFormula(newFormula))
                .isInstanceOf(FormulaException.class)
                .hasFieldOrPropertyWithValue("errorCode", FormulaErrorCode.LIBRARY_INVALID_CHILDREN);

        formula.getChildren().clear();
        newFormula.setEvalLang("non-existent lang");
        formula.addChild("temp", newFormula);
        assertThatThrownBy(() -> calcService.persistFormula(formula))
                .isInstanceOf(FormulaException.class)
                .hasFieldOrPropertyWithValue("errorCode", FormulaErrorCode.MULTIPLE_LIBRARY_EVAL_LANGS);

        newFormula.setType(FormulaType.MASTER_FORMULA);
        formula.addChild("123_qwe", newFormula);
        assertThatThrownBy(() -> calcService.persistFormula(formula))
                .isInstanceOf(FormulaException.class)
                .hasFieldOrPropertyWithValue("errorCode", FormulaErrorCode.VARIABLE_INVALID_NAME);
    }

    /**
     * Test for {@link CalcService#getFormulaByKey(String, LocalDateTime)}.
     */
    @Test
    public void test02GetFormulaByKey() {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        AttributeLocale locale = AttributeLocale.RU;

        Formula dbFormula = calcService.getFormulaByKey(formula.getKey(), ldts);
        dbFormula.addChild("v_cf1", childFormula1);
        dbFormula = calcService.getFormulaByKey(formula.getKey(), ldts);

        Assert.assertEquals(dbFormula.getDigest(), dbFormula.calcDigest());
        Assert.assertEquals(dbFormula.getFormula().getDigest(), dbFormula.getFormula().calcDigest());
        Assert.assertNotNull(dbFormula.getChildren().get(0).getValue().getEvalLang());
        Assert.assertNotNull(dbFormula.getChildren().get(0).getValue().getResultType());

        dbFormula.getChildren().clear();
        calcService.persistFormula(dbFormula);
    }

    /**
     * Test for {@link FormulaService#getFormulaTree(Formula, LocalDateTime)}.
     */
    @Test
    public void test03GetFormulaTree() {
        formula.getChildren().clear();
        calcService.persistFormula(formula);
        childFormula1.setType(FormulaType.FORMULA);
        childFormula1.getChildren().clear();
        calcService.persistFormula(childFormula1);
        childFormula2.setType(FormulaType.FORMULA);
        childFormula2.getChildren().clear();
        calcService.persistFormula(childFormula2);

        childFormula1.addChild("childFormula2", childFormula2);
        calcService.persistFormula(childFormula1);
        formula.addChild("childFormula1", childFormula1);
        calcService.persistFormula(formula);

        LocalDateTime ldts = metadataDao.getSysTimestamp();
        formula = formulaService.getFormulaTree(formula, ldts);
        Assert.assertEquals(1, formula.getChildren().size());
        Assert.assertEquals(1, formula.getChildren().get(0).getRight().getChildren().size());
    }

    /**
     * Test reading formula trees with filter.
     */
    @Test
    public void test03GetFormulaTrees() {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(formulaMetadata.getKeyAttribute(), Operator.LIKE, FTS_1));
        criteria.getWhere().addItem(new WhereItem(Conjunction.OR, formulaMetadata.getKeyAttribute(), Operator.LIKE, FTS_2));
        List<Formula> roots = calcService.getFormulaTrees(criteria, ldts);
        assertThat(roots).hasSize(2);
        Formula rootFTS1 = roots.stream().filter(f -> f.getKey().equals(fts1.formula.getKey())).collect(CollectorUtils.singletonCollector());
        assertThat(rootFTS1).isNotNull();
        Formula rootFTS2 = roots.stream().filter(f -> f.getKey().equals(fts2.formula.getKey())).collect(CollectorUtils.singletonCollector());
        assertThat(rootFTS2).isNotNull();

        // check formulas
        assertThat(rootFTS1.getChildren()).extracting("right")
                                          .contains(fts1.childFormula1, fts1.childFormula2, fts2.childFormula1)
                                          .doesNotContain(fts2.formula, fts2.childFormula2);
        assertThat(rootFTS2.getChildren()).extracting("right")
                                          .contains(fts2.childFormula1, fts2.childFormula2)
                                          .doesNotContain(fts1.formula, fts1.childFormula1, fts1.childFormula2);

        // check result figure names
        //assertThat(rootFTS1.getChildren()).extracting("left")
        //                                  .contains(FTS1.formula.getChildren().stream().map(FormulaDependencyPair::getLeft).toArray());
        //assertThat(rootFTS2.getChildren()).extracting("left")
        //                                  .contains(FTS2.formula.getChildren().stream().map(FormulaDependencyPair::getLeft).toArray());
    }

    /**
     * Test for {@link CalcService#eval(Formula, Map)}.
     * @throws Exception if error rise
     */
    @Test
    public void test04Eval() throws Exception {
        EvalResult result = calcService.eval(formula, null);
        Assert.assertEquals("test", result.getOutput().trim());

        formula.getFormula().setData("print1('test');");
        result = calcService.eval(formula, null);
        Assert.assertEquals(result.getException().getCause().getClass(), ScriptException.class);

        formula.getFormula().setData("print(v1 + v2); v1 + v2;");
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("v1", 1);
        attrs.put("v2", 2);
        result = calcService.eval(formula, attrs);
        Assert.assertEquals("3", result.getOutput().trim());
        Assert.assertEquals(Integer.parseInt("3"), ((Number) result.getResult()).intValue());
    }

    /**
     * Test for {@link CalcService#eval(Calculation)}.
     * @throws Exception if error rise
     */
    @Test
    public void test05Eval() throws Exception {
        /* FIRST CHECK: SIMPLE CHAIN OF FUNCTIONS */
        childFormula2.getFormula().setData("print('executing formula F_KEY3 resulting its name'); 'F_KEY3';");
        childFormula2.setResultType(FormulaResultType.STRING);
        calcService.persistFormula(childFormula2);

        childFormula1.getFormula().setData("print('executing formula F_KEY2 resulting its name, input attribute: ' + childFormula2); 'F_KEY2';");
        childFormula1.setResultType(FormulaResultType.STRING);
        calcService.persistFormula(childFormula1);

        formula = calcService.getFormulaByKey(formula.getKey(), metadataDao.getSysTimestamp());
        formula.getFormula().setData("print('executing root formula F_KEY, input attribute: ' + childFormula1);");
        calcService.persistFormula(formula);

        /* initialize model and calculation: actuality date must be greater than formula persist date */
        Calculation calculation = (Calculation) entityService.newEmptyEntity(Calculation.METADATA_KEY);
        calculation.setProfiles(Collections.singletonList(profile));
        LocalDateTime actuality = metadataDao.getSysTimestamp().plusMinutes(1);
        calculation.setActuality(actuality.toLocalDate());
        calculation.setDataActuality(actuality);
        calculation.setName("formula calc test");
        calculation.setAuthor(securityService.getCurrentUser());

        calculation.setModel((Model) entityService.newEmptyEntity(Model.METADATA_KEY));
        calculation.getModel().setFormulas(Arrays.asList(formula, childFormula1));
        calculation.getModel().setActuality(actuality);
        calculation.getModel().setName(new MultilangDescription("черновик тест", "draft test"));

        calcService.persistModel(calculation.getModel());
        calcService.persistCalculation(calculation);

        assertThat(catchThrowable(() -> calcService.eval(calculation).get())).isNull();

        /* results validation */
        Criteria criteria = new Criteria();
        criteria.setHubIds(Arrays.asList(calculation.getHubId()));
        List<Formula> formulas = calcService.getCalculations(criteria, null, null).get(0).getModel().getFormulas();
        Assert.assertEquals(2, formulas.size());
        formulas.forEach(formula -> {
            if (formula.getKey().equals("F_KEY2")) {
                Assert.assertEquals("F_KEY2", formula.getFormulaResult(calculation, profile).getStringResult());
            }
            if (formula.getKey().equals("F_KEY")) {
                Assert.assertNull(formula.getFormulaResult(calculation, profile).getStringResult());
            }
        });

        /* SECOND CHECK: FIRST FUNCTION IS LIBRARY ONE */
        childFormula2.getFormula().setData("function fn(x) {print(x); return x * 2;};");
        childFormula2.setResultType(FormulaResultType.NUMBER);
        childFormula2.setType(FormulaType.LIBRARY);
        calcService.persistFormula(childFormula2);

        childFormula1.getFormula().setData("print('executing formula F_KEY2 using library formula F_KEY3.'); fn(5);");
        childFormula1.setResultType(FormulaResultType.NUMBER);
        childFormula1.setType(FormulaType.FORMULA);
        calcService.persistFormula(childFormula1);

        formula.getFormula().setData("print('executing root formula F_KEY, input attribute: ' + childFormula1);");
        formula.setResultType(FormulaResultType.NUMBER);
        formula.setType(FormulaType.FORMULA);
        calcService.persistFormula(formula);

        /* re-initialize model and calculation: actuality date must be greater than formula persist date */
        actuality = metadataDao.getSysTimestamp().plusMinutes(1);
        calculation.setActuality(actuality.toLocalDate());
        calculation.getModel().setActuality(actuality);
        calculation.getModel().setFormulas(Arrays.asList(formula));
        calcService.persistModel(calculation.getModel());
        calcService.persistCalculation(calculation);

        /* results validation */
        assertThat(catchThrowable(() -> calcService.eval(calculation).get())).isNull();
        formulas = calcService.getCalculations(criteria, null, null).get(0).getModel().getFormulas();
        Assert.assertEquals(1, formulas.size());
        Assert.assertNull(formulas.get(0).getFormulaResult(calculation, profile).getNumberResult());
        Assert.assertNotNull(formulas.get(0).getFormulaResult(calculation, profile).getOutput());

        /* THIRD CHECK: CHAIN OF LIBRARY FUNCTIONS */
        childFormula1.getFormula().setData(
                "print('executing library formula F_KEY2 using library formula F_KEY3.'); function fn2(x) {print('fn2: ' + x); return 2 * fn(x);};"
        );
        childFormula1.setResultType(FormulaResultType.NUMBER);
        childFormula1.setType(FormulaType.LIBRARY);
        calcService.persistFormula(childFormula1);

        formula.getFormula().setData("print('executing root formula F_KEY, using chain of library functions.'); fn2(2);");
        formula.setResultType(FormulaResultType.NUMBER);
        formula.setType(FormulaType.FORMULA);
        calcService.persistFormula(formula);

        /* re-initialize model and calculation: actuality date must be greater than formula persist date */
        actuality = metadataDao.getSysTimestamp().plusMinutes(1);
        calculation.setActuality(actuality.toLocalDate());
        calculation.getModel().setActuality(actuality);
        calculation.getModel().setFormulas(Arrays.asList(formula));
        calcService.persistModel(calculation.getModel());
        calcService.persistCalculation(calculation);

        /* results validation */
        assertThat(catchThrowable(() -> calcService.eval(calculation).get())).isNull();
        formulas = calcService.getCalculations(criteria, null, null).get(0).getModel().getFormulas();
        Assert.assertEquals(1, formulas.size());
        Assert.assertEquals("8", String.valueOf(formulas.get(0).getFormulaResult(calculation, profile).getNumberResult().intValue()));

        /* reset library flag for future tests */
        childFormula1.setType(FormulaType.FORMULA);
        calcService.persistFormula(childFormula1);
    }

    /**
     * Test for {@link CalcService#eval(Calculation)}.
     * @throws Exception if error rise
     */
    @Test
    public void test050EvalPublished() throws Exception {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        Calculation calculation = (Calculation) entityService.newEmptyEntity(Calculation.METADATA_KEY);
        calculation.setProfiles(Collections.singletonList(profile));
        calculation.setActuality(ldts.toLocalDate());
        calculation.setDataActuality(ldts);
        calculation.setName("formula calc test");
        calculation.setAuthor(securityService.getCurrentUser());
        calculation.setPublished(true);
        calculation.setModel((Model) entityService.newEmptyEntity(Model.METADATA_KEY));
        //calculation.getModel().setActuality(metadataDao.getSysTimestamp());
        //calculation.getModel().setFormulas(Arrays.asList(formula, childFormula1));
        calculation.getModel().setActuality(ldts);
        calculation.getModel().setName(new MultilangDescription("черновик тест", "draft test"));
        calcService.persistModel(calculation.getModel());
        calcService.persistCalculation(calculation);

        assertThat(catchThrowable(() -> calcService.eval(calculation).get())).isInstanceOf(ExecutionException.class)
                                                                             .hasCauseInstanceOf(CalculationException.class)
                                                                             .hasFieldOrPropertyWithValue("cause.errorCode",
                                                                                                          CalculationErrorCode.CALCULATE_PUBLISHED);
    }

    /**
     * Test for {@link CalcService#getFormulaTrees(Criteria, LocalDateTime)}.
     */
    @Test
    public void test06GetFilteredFormulas() {
        LocalDateTime ldts = metadataDao.getSysTimestamp();

        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(formulaMetadata.getKeyAttribute(), Operator.IN, "F_KEY", "F_KEY2", "F_KEY3"));

        List<Formula> trees = calcService.getFormulaTrees(criteria, ldts);
        Assert.assertEquals(1, trees.size());
        Assert.assertEquals(1, trees.get(0).getChildren().size());
        Assert.assertEquals(1, trees.get(0).getChildren().get(0).getRight().getChildren().size());

        AttributeMeta metaLibrary = formulaMetadata.getAttributeMetadata(Formula.FormulaAttributeMeta.TYPE.getKey());
        criteria.getWhere().addItem(new WhereItem(metaLibrary, Operator.NOT_IN, FormulaType.LIBRARY));
        trees = calcService.getFormulaTrees(criteria, ldts);
        Assert.assertEquals(1, trees.get(0).getChildren().size());
        Assert.assertEquals(0, trees.get(0).getChildren().get(0).getRight().getChildren().size());

        criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(formulaMetadata.getKeyAttribute(), Operator.IN, "F_KEY", "F_KEY2", "F_KEY3"));
        AttributeMeta metaEvalLang = formulaMetadata.getAttributeMetadata(Formula.FormulaAttributeMeta.EVAL_LANG.getKey());
        WhereItem evalLangWhereItem = new WhereItem(metaEvalLang, Operator.EQ, "mvel");
        criteria.getWhere().addItem(evalLangWhereItem);
        trees = calcService.getFormulaTrees(criteria, ldts);
        Assert.assertEquals(0, trees.size());
        evalLangWhereItem.setValues(new Object[] {"nashorn"});
        trees = calcService.getFormulaTrees(criteria, ldts);
        Assert.assertEquals(1, trees.size());
        Assert.assertEquals(EvalLang.NASHORN.name().toLowerCase(), trees.get(0).getEvalLang());

        AttributeMeta metaName = formulaMetadata.getAttributeMetadata(Formula.FormulaAttributeMeta.NAME_EN.getKey());
        WhereItem metaNameWhereItem = new WhereItem(metaName, Operator.LIKE, "not found");
        criteria.getWhere().addItem(metaNameWhereItem);
        trees = calcService.getFormulaTrees(criteria, ldts);
        Assert.assertEquals(0, trees.size());
        metaNameWhereItem.setValues(new Object[] {"name"});
        trees = calcService.getFormulaTrees(criteria, ldts);
        Assert.assertEquals(1, trees.size());

        AttributeMeta metaKey = formulaMetadata.getKeyAttribute();
        WhereItem metaKeyWhereItem = new WhereItem(metaKey, Operator.EQ, "F_KEY");
        criteria.getWhere().addItem(metaKeyWhereItem);
        trees = calcService.getFormulaTrees(criteria, ldts);
        Assert.assertEquals("F_KEY", trees.get(0).getKey());

        criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(metaKey, Operator.IN, "FCYT1F_KEY", "FCYT1F_KEY2", "FCYT1F_KEY3",
                                                  "FTS2F_KEY", "FTS2F_KEY3", "FTS2F_KEY2"));
        criteria.getOrder().addItem(metaKey, false);
        trees = calcService.getFormulaTrees(criteria, ldts);
        Assert.assertEquals("FCYT1F_KEY", trees.get(0).getKey());
        Assert.assertEquals("FCYT1F_KEY2", trees.get(0).getChildren().get(0).getRight().getKey());
        Assert.assertEquals("FCYT1F_KEY3", trees.get(0).getChildren().get(1).getRight().getKey());

        criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(metaKey, Operator.IN, "FCYT1F_KEY", "FCYT1F_KEY2", "FCYT1F_KEY3",
                                                  "FTS2F_KEY", "FTS2F_KEY3", "FTS2F_KEY2"));
        criteria.getOrder().addItem(metaKey, true);
        trees = calcService.getFormulaTrees(criteria, ldts);
        Assert.assertEquals("FTS2F_KEY", trees.get(0).getKey());
        Assert.assertEquals("FTS2F_KEY3", trees.get(0).getChildren().get(0).getRight().getKey());
        Assert.assertEquals("FTS2F_KEY2", trees.get(0).getChildren().get(1).getRight().getKey());
    }

    /**
     * Test for {@link CalcService#getFormulaTrees(Criteria, Calculation, LocalDateTime)}.
     */
    @Test
    public void test06x02GetCalculationFormulaTree() {
        childFormula2.getFormula().setData("print('test3'); 1234567;");
        childFormula2.setType(FormulaType.FORMULA);
        childFormula2.getChildren().clear();
        calcService.persistFormula(childFormula2);

        childFormula1.setType(FormulaType.FORMULA);
        childFormula1.getFormula().setData("print('test2'); 123456;");
        childFormula1.getChildren().clear();
        childFormula1.addChild("childFormula2", childFormula2);
        calcService.persistFormula(childFormula1);

        Formula f = calcService.getFormulaByKey(formula.getKey(), metadataDao.getSysTimestamp());
        if (f == null) {
            calcService.persistFormula(formula);
            formula = calcService.getFormulaByKey(formula.getKey(), metadataDao.getSysTimestamp());
        } else {
            formula = f;
        }
        formula.setResultType(FormulaResultType.NUMBER);
        formula.getFormula().setData("print('test'); 12345;");
        formula.getChildren().clear();
        formula.addChild("childFormula1", childFormula1);
        calcService.persistFormula(formula);

        /* initialize model and calculation: actuality date must be greater than formula persist date */
        Calculation calculation = (Calculation) entityService.newEmptyEntity(Calculation.METADATA_KEY);
        calculation.setProfiles(Collections.singletonList(profile));
        LocalDateTime actuality = metadataDao.getSysTimestamp().plusMinutes(1);
        calculation.setActuality(actuality.toLocalDate());
        calculation.setDataActuality(actuality);
        calculation.setName("formula calc test");
        calculation.setAuthor(securityService.getCurrentUser());

        calculation.setModel((Model) entityService.newEmptyEntity(Model.METADATA_KEY));
        calculation.getModel().setFormulas(Arrays.asList(formula));
        calculation.getModel().setActuality(actuality);
        calculation.getModel().setName(new MultilangDescription("черновик тест", "draft test"));

        calcService.persistModel(calculation.getModel());
        calcService.persistCalculation(calculation);

        assertThat(catchThrowable(() -> calcService.eval(calculation).get())).isNull();

        Criteria criteria = new Criteria();
        criteria.getOrder().addItem(formulaMetadata.getKeyAttribute(), false);
        List<Formula> formulaTrees = calcService.getFormulaTrees(criteria, calculation, null);
        assertThat(formulaTrees.get(0).getFormulaResult(calculation, profile).getException()).isNull();
        assertThat(formulaTrees.get(0).getFormulaResult(calculation, profile).getOutput()).contains("test");
        assertThat(formulaTrees.get(0).getFormulaResult(calculation, profile).getNumberResult()).isEqualTo(new BigDecimal("12345"));
        assertThat(formulaTrees.get(0).getChildren().get(0).getRight().getKey()).isEqualTo("F_KEY2");
        assertThat(formulaTrees.get(0).getChildren().get(0).getRight().getFormulaResult(calculation, profile).getNumberResult())
                .isEqualTo(new BigDecimal("123456"));
        assertThat(formulaTrees.get(0).getChildren().get(0).getRight().getFormulaResult(calculation, profile).getOutput()).contains("test2");

        formula.getFormula().setData("wrongFormula");
        calcService.persistFormula(formula);

        assertThat(catchThrowable(() -> calcService.eval(calculation).get())).isNull();
        criteria.getWhere().addItem(new WhereItem(formulaMetadata.getKeyAttribute(), Operator.EQ, "F_KEY"));
        formulaTrees = calcService.getFormulaTrees(criteria, calculation, null);
        assertThat(formulaTrees.get(0).getFormulaResult(calculation, profile).getException()).contains("wrongFormula");
    }

    /**
     * Test for {@link CalcService#removeFormula(Formula)}.
     */
    @Test
    public void test07RemoveFormula() {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        AttributeLocale locale = AttributeLocale.RU;

        Assert.assertNotEquals(0, calcService.getFormulaByKey(childFormula1.getKey(), ldts).getChildren().size());

        calcService.removeFormula(childFormula1);

        Assert.assertTrue(childFormula1.isRemoved());
        Assert.assertEquals(0, calcService.getFormulaByKey(childFormula1.getKey(), ldts).getChildren().size());
    }

    /**
     * Make cycle at formulas forest test.
     */
    @Test
    public void test08CycleTest01() {
        fcyt2.childFormula1 = calcService.getFormulaByKey(fcyt2.childFormula1.getKey(), metadataDao.getSysTimestamp());
        fcyt1.childFormula1 = calcService.getFormulaByKey(fcyt1.childFormula1.getKey(), metadataDao.getSysTimestamp());
        fcyt2.childFormula1.addChild("test_1", fcyt1.childFormula1);
        calcService.persistFormula(fcyt2.childFormula1);

        fcyt1.childFormula1 = calcService.getFormulaByKey(fcyt1.childFormula1.getKey(), metadataDao.getSysTimestamp());
        fcyt2.formula = calcService.getFormulaByKey(fcyt2.formula.getKey(), metadataDao.getSysTimestamp());
        fcyt1.childFormula1.addChild("test_1_1", fcyt2.formula);
        assertThatThrownBy(() -> calcService.persistFormula(fcyt1.childFormula1)).isInstanceOf(FormulaCyclicDependencyException.class)
                                                                                 .extracting("formulas")
                                                                                 // FCYT1F_KEY2, FCYT2F_KEY2, FCYT2F_KEY
                                                                                 .contains(Arrays.asList(fcyt1.childFormula1, fcyt2.childFormula1,
                                                                                                         fcyt2.formula));
    }

    /**
     * Test usage of data access library.
     * @throws Exception throw error by json reader
     */
    @Test
    public void test09AccessData() throws Exception {
        Formula formulaParent = createFormulaWithLibraryChild("test09AccessData.js", "formula.json", "FL09");

        Entity classifierValue0101, classifierValue0102;
        final BigDecimal numValue1 = new BigDecimal(10);
        classifierValue0101 = createClassifierValue(entityMetaClassifier01, "DATAACC0101", numValue1, null);
        assertThat(classifierValue0101).isNotNull();
        final BigDecimal numValue2 = new BigDecimal(20);
        classifierValue0102 = createClassifierValue(entityMetaClassifier01, "DATAACC0102", numValue2, null);
        assertThat(classifierValue0102).isNotNull();

        Calculation calculation = createCalculationTestDataIfNotexists("FORMCALC09", false);
        calcService.persistsClassifierValues(calculation, Arrays.asList(classifierValue0101, classifierValue0102));
        LocalDateTime now = metadataDao.getSysTimestamp().plusMinutes(1L);
        Model model = createModelTestIfNotExists("FORMMOD09", now);
        model.setActuality(now);
        calculation.setDataActuality(now);
        calculation.setActuality(now.toLocalDate());
        calculation.setModel(model);

        EvalResult result = calcService.eval(formulaParent, formulaService.buildBindings(null, calculation, profile));
        assertThat(result.getException()).isNull();
        final Double evalResult = new Double(30.0);
        assertThat(result.getResult()).isEqualTo(evalResult);
    }

    /**
     * Test usage of data access library with date parameters.
     * @throws Exception throw error by json reader
     */
    @Test
    public void test10AccessDataWithDate() throws Exception {
        Formula formulaParent = createFormulaWithLibraryChild("test10AccessDataWithDate.js", "formula.json", "FL10");

        Entity classifierValue0101, classifierValue0102;
        LocalDate date = LocalDate.parse("2017-01-01");
        final BigDecimal numValue1 = new BigDecimal(10);
        classifierValue0101 = createClassifierValue(entityMetaClassifier01, "DATAACCD0101", numValue1, date.atStartOfDay());
        assertThat(classifierValue0101).isNotNull();
        final BigDecimal numValue2 = new BigDecimal(20);
        classifierValue0102 = createClassifierValue(entityMetaClassifier01, "DATAACCD0102", numValue2, date.atStartOfDay());
        assertThat(classifierValue0102).isNotNull();

        Calculation calculation = createCalculationTestDataIfNotexists("FORMCALC10", false);
        calcService.persistsClassifierValues(calculation, Arrays.asList(classifierValue0101, classifierValue0102));
        LocalDateTime now = metadataDao.getSysTimestamp().plusMinutes(1L);
        Model model = createModelTestIfNotExists("FORMMOD10", now);
        calculation.setDataActuality(now);
        calculation.setActuality(date);
        calculation.setModel(model);

        EvalResult result = calcService.eval(formulaParent, formulaService.buildBindings(null, calculation, profile));
        assertThat(result.getException()).isNull();
        final Double evalResult = new Double(30.0);
        assertThat(result.getResult()).isEqualTo(evalResult);
    }

    /**
     * Test usage of data access library with date parameters.
     * @throws Exception throw error by json reader
     */
    @Test
    public void test11AccessDataWithReference() throws Exception {
        Formula formulaParent = createFormulaWithLibraryChild("test11AccessDataWithReference.js", "formula.json", "FL11");

        final BigDecimal numValue1 = new BigDecimal(10);
        Entity classifierValue0201 = createClassifierValue(entityMetaClassifier02, "DATAACCR0201", numValue1, null);

        Entity classifierValue01R01, classifierValue01R02;
        classifierValue01R01 = createClassifierValue(entityMetaClassifier01R, "DATAACCR01R01", numValue1, null);
        assertThat(classifierValue01R01).isNotNull();
        final BigDecimal numValue2 = new BigDecimal(20);
        classifierValue01R02 = createClassifierValue(entityMetaClassifier01R, "DATAACCR01R02", numValue2, null);
        assertThat(classifierValue01R02).isNotNull();

        ((LinkedEntityAttribute<Entity>) classifierValue01R01.getAttribute("CLASSIFIER01R#CLASS02")).add(classifierValue0201);
        entityService.persistEntity(classifierValue01R01);
        ((LinkedEntityAttribute<Entity>) classifierValue01R02.getAttribute("CLASSIFIER01R#CLASS02")).add(classifierValue0201);
        entityService.persistEntity(classifierValue01R02);

        Calculation calculation = createCalculationTestDataIfNotexists("FORMCALC11", false);
        calcService.persistsClassifierValues(calculation, Arrays.asList(classifierValue01R01, classifierValue01R02));
        LocalDateTime now = metadataDao.getSysTimestamp().plusMinutes(1L);
        Model model = createModelTestIfNotExists("FORMMOD11", now);
        model.setActuality(now);
        calculation.setDataActuality(now);
        calculation.setActuality(now.toLocalDate());
        calculation.setModel(model);

        EvalResult result = calcService.eval(formulaParent, formulaService.buildBindings(null, calculation, profile));
        assertThat(result.getException()).isNull();
        final Double evalResult = new Double(30.0);
        assertThat(result.getResult()).isEqualTo(evalResult);
    }

    /**
     * Load formulas by query criteria.
     */
    @Test
    public void test11GetFormulas() {
        Criteria criteria = new Criteria();
        criteria.getWhere()
                .addItem(new WhereItem(formulaMetadata.getKeyAttribute(), Operator.IN, fts1.childFormula1.getKey(), fts1.childFormula2.getKey()));
        List<Formula> formulas = calcService.getFormulas(criteria, null);
        assertThat(formulas).hasSize(2);
        assertThat(formulas).containsExactlyInAnyOrder(fts1.childFormula1, fts1.childFormula2);

        criteria = new Criteria();
        criteria.setHubIds(Arrays.asList(fts1.childFormula1.getHubId(), fts1.childFormula2.getHubId()));
        formulas = calcService.getFormulas(criteria, null); // hub id really is null
        assertThat(formulas).hasSize(0);

        LocalDateTime ldts = metadataDao.getSysTimestamp();
        criteria = new Criteria();
        criteria.setHubIdsAndLdts(
                Arrays.asList(new ImmutablePair(fts1.childFormula1.getHubId(), ldts), new ImmutablePair(fts1.childFormula2.getHubId(), ldts)));
        formulas = calcService.getFormulas(criteria, null);
        assertThat(formulas).hasSize(0);
    }

    /**
     * Test for adjust date.
     * @throws Exception throw error by json reader
     */
    @Test
    public void test12DataAccessAdjustDate() throws Exception {
        Formula formulaParent = createFormulaWithLibraryChild("test12DataAccessAdjustDate.js", "formula.json", "FL12");
        Calculation calculation = createCalculationTestDataIfNotexists("FORMCALC12", false);
        Map<String, Object> attrs = formulaService.buildBindings(null, calculation, null);
        LocalDateTime sourceDate = metadataDao.getSysTimestamp();
        attrs.put("sourceDate", sourceDate);
        attrs.put("years", 1);
        attrs.put("months", 1);
        attrs.put("days", 1);
        EvalResult result = calcService.eval(formulaParent, attrs);
        LocalDateTime targetDate = sourceDate.plusYears(1);
        targetDate = targetDate.plusMonths(1);
        targetDate = targetDate.plusDays(1);
        assertThat(result.getResult()).isEqualTo(targetDate);

        attrs.put("years", null);
        attrs.put("months", null);
        attrs.put("days", null);
        result = calcService.eval(formulaParent, attrs);
        assertThat(result.getResult()).isEqualTo(sourceDate);
    }

    /**
     * Test access to calculation and model object.
     * @throws Exception on JSON error
     */
    @Test
    public void test13AccessCalcAndModel() throws Exception {
        Formula formulaParent = createFormulaWithLibraryChild("test13AccessCalcAndModel.js", "formula-string.json", "FL13");

        Calculation calculation = createCalculationTestDataIfNotexists("FORMCALC13", false);
        LocalDateTime now = metadataDao.getSysTimestamp().plusMinutes(1L);
        Model model = createModelTestIfNotExists("FORMMOD13", now);
        model.setActuality(now);
        calculation.setDataActuality(now);
        calculation.setActuality(now.toLocalDate());
        calculation.setModel(model);

        EvalResult result = calcService.eval(formulaParent, formulaService.buildBindings(null, calculation, profile));
        assertThat(result.getException()).isNull();
        final String evalResult = "draft test " + calculation.getKey() + "published test " + model.getKey();
        assertThat(result.getResult()).isEqualTo(evalResult);
    }

    /**
     * Test for getClassifierValue library JavaScript function.
     * @throws IOException if error rise
     */
    @Test
    public void test14DataAccessGetClassifierValue() throws IOException {
        final BigDecimal numValue = new BigDecimal(10);

        Formula formulaParent = createFormulaWithLibraryChild("test14DataAccessGetClassifierValue_1.js", "formula.json", "FL14_1");

        Entity classifierValue = createClassifierValue(entityMetaClassifier03, entityMetaClassifier03.getKey(), numValue, null);
        classifierValue.setAttributeValue(
                entityMetaService.getAttributeMetaKey(entityMetaClassifier03, ClassifierAttributeMeta.CLASSIFIER_TYPE.name()), numValue
        );
        entityService.persistEntity(classifierValue);

        Calculation calculation = createCalculationTestDataIfNotexists("FORMCALC14", false);
        calcService.persistsClassifierValues(calculation, Arrays.asList(classifierValue));
        LocalDateTime now = metadataDao.getSysTimestamp().plusDays(1L);
        Model model = createModelTestIfNotExists("FORMMOD14", now);
        model.setActuality(now);
        calculation.setDataActuality(now);
        calculation.setActuality(now.toLocalDate());
        calculation.setModel(model);

        Map<String, Object> attrs = formulaService.buildBindings(null, calculation, null);
        attrs.put("classifierKey", entityMetaClassifier03.getKey());
        attrs.put("refClassifierAttributeKey", null);
        EvalResult result = calcService.eval(formulaParent, attrs);
        assertThat(result.getException()).isNull();
        assertThat(result.getResult()).isEqualTo(numValue);

        formulaParent = createFormulaWithLibraryChild("test14DataAccessGetClassifierValue_2.js", "formula.json", "FL14_2");
        attrs.put("dataActuality", calculation.getDataActuality());
        result = calcService.eval(formulaParent, attrs);
        assertThat(result.getException()).isNull();
        assertThat(result.getResult()).isEqualTo(numValue);

        attrs.put("dataActuality", calculation.getDataActuality().minusDays(2L));
        result = calcService.eval(formulaParent, attrs);
        assertThat(result.getException()).isNull();
        assertThat(result.getResult()).isNull();
    }

    /**
     * Test for getFormValue library JavaScript function.
     * @throws IOException if error rise
     */
    @Test
    public void test15DataAccessGetFormValue() throws IOException {
        final BigDecimal numValue = new BigDecimal(10);

        Formula formulaParent = createFormulaWithLibraryChild("test15DataAccessGetFormValue_1.js", "formula.json", "FL15_1");

        Entity formValue = createClassifierValue(entityMetaClassifier03, entityMetaClassifier03.getKey(), numValue, null);
        formValue.setAttributeValue(
                entityMetaService.getAttributeMetaKey(entityMetaClassifier03, ClassifierAttributeMeta.CLASSIFIER_TYPE.name()), numValue
        );
        entityService.persistEntity(formValue);

        Calculation calculation = createCalculationTestDataIfNotexists("FORMCALC15", false);
        calcService.persistsClassifierValues(calculation, Arrays.asList(formValue));
        LocalDateTime now = metadataDao.getSysTimestamp().plusDays(1L);
        Model model = createModelTestIfNotExists("FORMMOD15", now);
        model.setActuality(now);
        calculation.setDataActuality(now);
        calculation.setActuality(now.toLocalDate());
        calculation.setModel(model);

        Map<String, Object> attrs = formulaService.buildBindings(null, calculation, null);
        attrs.put("formKey", entityMetaClassifier03.getKey());
        attrs.put("formAttributeKey", entityMetaClassifier03.getKey() + "#" + ClassifierAttributeMeta.CLASSIFIER_TYPE.name());
        EvalResult result = calcService.eval(formulaParent, attrs);
        assertThat(result.getException()).isNull();
        assertThat(result.getResult()).isEqualTo(numValue.intValue());

        formulaParent = createFormulaWithLibraryChild("test15DataAccessGetFormValue_2.js", "formula.json", "FL15_2");
        attrs.put("dataActuality", calculation.getDataActuality());
        result = calcService.eval(formulaParent, attrs);
        assertThat(result.getException()).isNull();
        assertThat(result.getResult()).isEqualTo(numValue.intValue());

        attrs.put("dataActuality", calculation.getDataActuality().minusDays(2L));
        result = calcService.eval(formulaParent, attrs);
        assertThat(result.getException()).isNull();
        assertThat(result.getResult()).isNull();
    }

    /**
     * Test for getDictionaryValue library JavaScript function.
     * @throws IOException if error rise
     */
    @Test
    public void test16DataAccessGetDictionaryValue() throws IOException {
        final BigDecimal numValue = new BigDecimal(10);

        Formula formulaParent = createFormulaWithLibraryChild("test16DataAccessGetDictionaryValue_1.js", "formula.json", "FL16_1");

        Entity formValue = createClassifierValue(entityMetaClassifier03, entityMetaClassifier03.getKey(), numValue, null);
        formValue.setAttributeValue(
                entityMetaService.getAttributeMetaKey(entityMetaClassifier03, ClassifierAttributeMeta.CLASSIFIER_TYPE.name()), numValue
        );
        entityService.persistEntity(formValue);

        Calculation calculation = createCalculationTestDataIfNotexists("FORMCALC16", false);
        calcService.persistsClassifierValues(calculation, Arrays.asList(formValue));
        LocalDateTime now = metadataDao.getSysTimestamp().plusDays(1L);
        Model model = createModelTestIfNotExists("FORMMOD16", now);
        model.setActuality(now);
        calculation.setDataActuality(now);
        calculation.setActuality(now.toLocalDate());
        calculation.setModel(model);

        Map<String, Object> attrs = formulaService.buildBindings(null, calculation, null);
        attrs.put("dictionaryKey", entityMetaClassifier03.getKey());
        attrs.put("dictionaryAttributeKey", entityMetaClassifier03.getKey() + "#" + ClassifierAttributeMeta.CLASSIFIER_TYPE.name());
        EvalResult result = calcService.eval(formulaParent, attrs);
        assertThat(result.getException()).isNull();
        assertThat(result.getResult()).isEqualTo(numValue.intValue());

        formulaParent = createFormulaWithLibraryChild("test16DataAccessGetDictionaryValue_2.js", "formula.json", "FL16_2");
        attrs.put("dataActuality", calculation.getDataActuality());
        result = calcService.eval(formulaParent, attrs);
        assertThat(result.getException()).isNull();
        assertThat(result.getResult()).isEqualTo(numValue.intValue());

        attrs.put("dataActuality", null);
        result = calcService.eval(formulaParent, attrs);
        assertThat(result.getException()).isNull();
        assertThat(result.getResult()).isEqualTo(numValue.intValue());
    }

/*
    @Test
    public void testLCHERNAYA() throws Exception {
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(calculationEntityMeta.getKeyAttribute(), Operator.EQ, "260"));
        List<Calculation> calculations = calcService.getCalculations(criteria, null, null);
        assertThat(calculations).hasSize(1);
        Calculation calculation = calculations.get(0);

        //Formula formulaParent = createFormulaWithLibraryChild("testONBASE.js", "FL_LCHERNAYA");
        assertThat(catchThrowable(() -> calcService.eval(calculation).get())).isNull();
    }
*/

    /**
     * Utility to create formula with library.
     * @param parentJavaScriptName formula resource file name
     * @param formulaMetaFile formula object json definition file
     * @param formulaKey formula key to create
     * @return formula object
     * @throws java.io.IOException when cant read resource files
     */
    private Formula createFormulaWithLibraryChild(String parentJavaScriptName, String formulaMetaFile, String formulaKey) throws java.io.IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Formula formulaChild = calcService.getFormulaByKey("LIB_MD_DATA_ACCESS", null);
        assertThat(formulaChild).isNotNull();
        String parentJavaScript = IOUtils.toString(FormulaServiceTest.class.getResourceAsStream(parentJavaScriptName), StandardCharsets.UTF_8);

        Formula formulaParent = calcService.getFormulaByKey(formulaKey, null);
        if (formulaParent == null) {
            formulaParent = objectMapper.readValue(FormulaServiceTest.class.getResourceAsStream(formulaMetaFile), Formula.class);
            formulaParent.setKey(formulaKey);
            formulaParent.getFormula().setData(parentJavaScript);
            formulaParent.addChild(null, formulaChild);
            formulaParent.setEvalLang("nashorn");
            calcService.persistFormula(formulaParent);
        } else {
            formulaParent = formulaService.getFormulaTree(formulaParent, metadataDao.getSysTimestamp());
            formulaParent.getFormula().setData(parentJavaScript);
        }
        return formulaParent;
    }

    /**
     * Create classifier value instance.
     * @param entityMeta classifier metadata
     * @param key classifier instance key
     * @param num value for NUMBER attribute
     * @param dateTime date and time
     * @return classifier value entity
     */
    private Entity createClassifierValue(EntityMeta entityMeta, String key, BigDecimal num, LocalDateTime dateTime) {
        Entity entity = entityService.getEntity(entityMeta, key, null);
        if (entity == null) {
            entity = entityService.newEmptyEntity(entityMeta);
            entity.setKey(key);
        }
        entity.setAttributeValue(entityMetaService.getAttributeMetaKey(entityMeta, "STRING"), "string value " + key);
        entity.setAttributeValue(entityMetaService.getAttributeMetaKey(entityMeta, "NUMBER"), num);
        if (entity.getMeta().getAttributeMetadata(entityMetaService.getAttributeMetaKey(entityMeta, "DATETIME")) != null)
            entity.setAttributeValue(entityMetaService.getAttributeMetaKey(entityMeta, "DATETIME"), dateTime);

        LinkedEntityAttribute calcProfile = (LinkedEntityAttribute) entity
                .getAttribute(entityMetaService.getAttributeMetaKey(entityMeta, ClassifierAttributeMeta.CALC_PROFILE.name()));
        if (calcProfile.getEntityList().isEmpty())
            calcProfile.getEntityList().add(profile);

        entityService.persistEntity(entity);
        return entity;
    }

    /**
     * Create test data at database.
     * @param key test object key
     * @param calculated calculated flag
     * @return calculation with key
     */
    private Calculation createCalculationTestDataIfNotexists(String key, boolean calculated) {
        if (entityService.getEntityIdByKey(calculationEntityMeta, key) == null) {
            Calculation calculation = (Calculation) entityService.newEmptyEntity(Calculation.METADATA_KEY);
            calculation.setProfiles(Collections.singletonList(profile));
            calculation.setKey(key);
            calculation.setName("draft test " + key);
            LocalDateTime now = LocalDateTime.now();
            calculation.setActuality(now.toLocalDate());
            calculation.setDataActuality(now);
            calculation.setAuthor(securityService.getCurrentUser());
            calculation.setCalculated(calculated);

            calcService.persistCalculation(calculation);
            return calculation;
        } else {
            Criteria criteria = new Criteria();
            criteria.getWhere().addItem(new WhereItem(calculationEntityMeta.getKeyAttribute(), Operator.EQ, key));
            Calculation calculation = calcService.getCalculations(criteria, null, null).get(0);
            return calculation;
        }
    }

    /**
     * Create test data at database.
     * @param key test object key
     * @param actuality actuality date
     * @return return created or found model
     */
    private Model createModelTestIfNotExists(String key, LocalDateTime actuality) {
        Model model;
        if (entityService.getEntityIdByKey(modelEntityMeta, key) == null) {
            model = (Model) entityService.newEmptyEntity(Model.METADATA_KEY);
            model.setKey(key);
            model.setName(new MultilangDescription("чистовик тест " + key, "published test " + key));
            model.setPublished(true);
            model.setActuality(actuality);
            calcService.persistModel(model);
        } else {
            Criteria criteria = new Criteria();
            criteria.getWhere().addItem(new WhereItem(modelEntityMeta.getKeyAttribute(), Operator.EQ, key));
            model = calcService.getModels(criteria, null, null).get(0);
        }
        return model;
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
     * Prepares initial test data.
     * @throws Exception exception
     */
    @BeforeClass
    public static void setupClass() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        formula = objectMapper.readValue(FormulaServiceTest.class.getResourceAsStream("formula.json"), Formula.class);
        childFormula1 = objectMapper.readValue(FormulaServiceTest.class.getResourceAsStream("formula_child_1.json"), Formula.class);
        childFormula2 = objectMapper.readValue(FormulaServiceTest.class.getResourceAsStream("formula_child_2.json"), Formula.class);
        formula.addChild("childFormula1", childFormula1);
    }

    /**
     * Setup object instance data.
     * @throws Exception any exception
     */
    @Before
    public void setupObject() throws Exception {
        securityService.defineSecurityContext(ADMINWF_LOGIN);
        if (!setupDone) {
            LocalDateTime ldts = metadataDao.getSysTimestamp();
            formulaMetadata = entityMetaService.getEntityMetaByKey(Formula.METADATA_KEY, ldts);
            calculationEntityMeta = entityMetaService.getEntityMetaByKey(Calculation.METADATA_KEY, ldts);
            modelEntityMeta = entityMetaService.getEntityMetaByKey(Model.METADATA_KEY, ldts);

            fts1 = createFormulaBundle(FTS_1);
            fts2 = createFormulaBundle(FTS_2);
            fts1.formula.addChild("FTS2_childFormula1", fts2.childFormula1);
            fts2.persists();
            fts1.persists();

            fcyt1 = createFormulaBundle(FCYT_1);
            fcyt2 = createFormulaBundle(FCYT_2);
            fcyt1.persists();
            fcyt2.persists();

            ObjectMapper objectMapper = new ObjectMapper();
            entityMetaClassifier02 = objectMapper.readValue(CalculationServiceTest.class.getResourceAsStream("Classifier02.json"),
                                                            EntityMeta.class);
            entityMetaClassifier02 = createEntityMeta(entityMetaClassifier02);

            entityMetaClassifier03 = objectMapper.readValue(CalculationServiceTest.class.getResourceAsStream("Classifier03.json"),
                                                            EntityMeta.class);
            entityMetaClassifier03 = createEntityMeta(entityMetaClassifier03);

            entityMetaClassifier01 = objectMapper.readValue(CalculationServiceTest.class.getResourceAsStream("Classifier01.json"),
                                                            EntityMeta.class);
            entityMetaClassifier01 = createEntityMeta(entityMetaClassifier01);

            entityMetaClassifier01R = objectMapper.readValue(CalculationServiceTest.class.getResourceAsStream("Classifier01R.json"),
                                                             EntityMeta.class);
            entityMetaClassifier01R = createEntityMeta(entityMetaClassifier01R);

            profileEntityMeta = entityMetaService.getEntityMetaByKey(CalculationProfileAttributeMeta.METADATA_KEY, null);

            profile = entityService.getEntity(profileEntityMeta, DEFAULT_CALC_PROFILE, null);
            assertThat(profile).isNotNull();

            setupDone = true;
        }
    }

    /**
     * Create trinity formula bundle. One parent, tow children
     * @param keyPrefix key prefix for bundle
     * @return formulas bundle
     * @throws Exception any exception
     */
    private FormulaBundle createFormulaBundle(String keyPrefix) throws Exception {
        FormulaBundle fb = new FormulaBundle();
        ObjectMapper objectMapper = new ObjectMapper();
        fb.formula = objectMapper.readValue(FormulaServiceTest.class.getResourceAsStream("formula.json"), Formula.class);
        fb.formula.setKey(keyPrefix + fb.formula.getKey());
        fb.childFormula1 = objectMapper.readValue(FormulaServiceTest.class.getResourceAsStream("formula_child_1.json"), Formula.class);
        fb.childFormula1.setKey(keyPrefix + fb.childFormula1.getKey());
        fb.childFormula2 = objectMapper.readValue(FormulaServiceTest.class.getResourceAsStream("formula_child_2.json"), Formula.class);
        fb.childFormula2.setKey(keyPrefix + fb.childFormula2.getKey());
        fb.formula.addChild(keyPrefix + "childFormula1", fb.childFormula1);
        fb.formula.addChild(keyPrefix + "childFormula2", fb.childFormula2);
        return fb;
    }
}
