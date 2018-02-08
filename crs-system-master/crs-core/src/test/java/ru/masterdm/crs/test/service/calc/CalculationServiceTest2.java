package ru.masterdm.crs.test.service.calc;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.entity.BusinessAction;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.Permission;
import ru.masterdm.crs.domain.entity.Role;
import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.service.CalcService;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.SecurityService;
import ru.masterdm.crs.service.UserRoleService;

/**
 * Calculation tests.
 * @author Pavel Masalov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml",
                                   "classpath:META-INF/spring/crs-security-test.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CalculationServiceTest2 {

    private boolean setupDone = false;

    @Autowired
    private CalcService calcService;
    @Autowired
    private EntityMetaService entityMetaService;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private SecurityService securityService;

    private EntityMeta calculationEntityMeta;

    /**
     * Test of reading concrete calculations.
     */
    @Test
    @Ignore
    public void testLCHERNAYA() {
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(
                new WhereItem(calculationEntityMeta.getKeyAttribute(), Operator.IN, "483"));
        List<Calculation> calculationList = calcService.getDraftCalculations(criteria, null, null);

        criteria = new Criteria();
        criteria.getWhere().addItem(
                new WhereItem(calculationEntityMeta.getKeyAttribute(), Operator.IN, "482"));
        List<Calculation> calculationList2 = calcService.getPublishedCalculations(criteria, null, null);
        System.out.println(calculationList);
    }

    /**
     * Test of reading concrete calculations.
     */
    @Test
    @Ignore
    public void testLCHERNAYA482483() {
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(calculationEntityMeta.getKeyAttribute(), Operator.EQ, "482"));
        List<Calculation> calculationList = calcService.getPublishedCalculations(criteria, null, null);
        Calculation calc482 = calculationList.get(0);
        Model model482 = calc482.getModel();
        criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(calculationEntityMeta.getKeyAttribute(), Operator.EQ, "483"));
        calculationList = calcService.getDraftCalculations(criteria, null, null);
        Calculation calc483 = calculationList.get(0);
        Model model483 = calc483.getModel();

        assertThat(model482.getClassifiers()).containsExactlyInAnyOrder(model483.getClassifiers().toArray(new EntityMeta[] {}));

        List<Entity> classVal482 = calcService.getClassifierValues(calc482, null);
        List<Entity> classVal483 = calcService.getClassifierValues(calc483, null);
        System.out.println("eeee");
    }

    /**
     * Check if use OBUMASH has only one role RA and RA role has EXECUTE permission on CALC.
     */
    @Test
    @Ignore
    public void testHasExecute() {
        EntityMeta roleEntityMeta = entityMetaService.getEntityMetaByKey(Role.METADATA_KEY, null);
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(roleEntityMeta.getKeyAttribute(), Operator.EQ, "RA"));
        List<Role> raRoleList = userRoleService.getRoles(criteria, null, null);
        assertThat(raRoleList).isNotEmpty();
        Role ra = raRoleList.get(0);
        List<Permission> permissionList = userRoleService.getEntityMetaPermissions(calculationEntityMeta, ra, null);
        assertThat(permissionList).extracting("action").extracting("key").contains("EXECUTE");
        Permission execute = permissionList.stream().filter(p -> p.getAction().getKey().equals(BusinessAction.Action.EXECUTE.name())).findFirst()
                                           .get();
        assertThat(execute.isPermit()).isFalse();

        User obumash = userRoleService.getUser("OBUMASH");
        assertThat(obumash.getRoles()).extracting("key").containsExactly("RA");
        assertThat(securityService.isPermitted(obumash, calculationEntityMeta, BusinessAction.Action.EXECUTE)).isFalse();
    }

    /**
     * Prepares initial test data.
     * @throws Exception exception
     */
    @Before
    public void setup() throws Exception {
        if (!setupDone) {
            calculationEntityMeta = entityMetaService.getEntityMetaByKey(Calculation.METADATA_KEY, null);
            setupDone = true;
        }
    }
}
