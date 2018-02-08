package ru.masterdm.crs.test.service.calc;

import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.masterdm.crs.domain.calc.EvalResult;
import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.entity.meta.FormulaType;
import ru.masterdm.crs.service.CalcService;

/**
 * {@link CalcService} test collection.
 * @author Alexey Chalov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-system-config-test.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FormulaServiceTimeoutTest {

    @Autowired
    private CalcService calcService;

    private Formula formula;
    private Formula childFormula1;

    /**
     * Test for formula time out.
     * @throws Exception if error rise
     */
    @Test
    public void test01EvalTimeout() throws Exception {
        formula.getFormula().setData("while (true) {}");
        EvalResult result = calcService.eval(formula, null);
        Assert.assertEquals(TimeoutException.class, result.getException().getClass());
    }

    /**
     * Prepares initial test data.
     * @throws Exception exception
     */
    @Before
    public void setup() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        formula = objectMapper.readValue(FormulaServiceTimeoutTest.class.getResourceAsStream("formula.json"), Formula.class);
        formula.setType(FormulaType.FORMULA);
        formula.setKey(formula.getKey() + "TO");
        formula.setEvalLang("nashorn");
        childFormula1 = objectMapper.readValue(FormulaServiceTimeoutTest.class.getResourceAsStream("formula_child_1.json"), Formula.class);
        childFormula1.setType(FormulaType.FORMULA);
        childFormula1.setKey(childFormula1.getKey() + "TO");
        formula.addChild("childFormula1", childFormula1);
    }
}
