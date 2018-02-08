package ru.masterdm.crs.test.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;
import static ru.masterdm.crs.SecurityContextBeanPostProcessor.ADMINWF_LOGIN;
import static ru.masterdm.crs.domain.entity.BusinessAction.Action;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.masterdm.crs.dao.entity.meta.MetadataDao;
import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.entity.BusinessAction;
import ru.masterdm.crs.domain.entity.Department;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.Permission;
import ru.masterdm.crs.domain.entity.Role;
import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.attribute.EntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.MultilangAttribute;
import ru.masterdm.crs.domain.entity.criteria.Conjunction;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.OrderItem;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.criteria.Where;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.ClientAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientGroupAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.domain.entity.meta.EntityTypeAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.UserEmailAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.UserTelAttributeMeta;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.service.CalcService;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.SecurityService;
import ru.masterdm.crs.service.UserRoleService;

/**
 * {@link SecurityService} test collection.
 * @author Alexey Kirilchev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml",
                                   "classpath:META-INF/spring/crs-security-test.xml",
                                   "classpath:META-INF/spring/crs-security-config.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SecurityServiceTest {

    public static final Long ADMINWF_ID = 27L;
    public static final String FULL_NAME = "Иванов Иван Иванович";
    public static final String DEPARTMENT_KEY = "46";
    public static final String DEPARTMENT_ONE_KEY = "1";
    public static final String DEPARTMENT_KBZ_KEY = "206";
    public static final String ROLE_KEY = "SA";
    public static final String ADMINWF_SHORT_DEPARTMENT = "000 - Головная организация";
    public static final String ADMINWF_FULL_DEPARTMENT = "Головная организация ОАО Банк ВТБ";
    public static final String LIPETSK_SHORT_DEPARTMENT = "Ф-л Липецк";
    public static final String LIPETSK_FULL_DEPARTMENT = "Филиал в г. Липецке";
    public static final String TEST_ROLE_2 = "TEST_ROLE2";
    public static final String TEST_ROLE = "TEST_ROLE";
    public static final String TEST_ROLE3 = "TEST_ROLE3";
    public static final String TEST_ROLE4 = "TEST_ROLE4";
    public static final String USER_EMAIL = "testmailssco@masterdm.ru";
    public static final String USER_TEL_NUMBER = "1000027";

    private static final String TEST_13_USER_KEY = "TEST13";

    @Autowired
    private SecurityService securityService;
    @Autowired
    private EntityService entityService;
    @Autowired
    private EntityMetaService entityMetaService;
    @Autowired
    private UserRoleService userRoleService;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private MetadataDao metadataDao;
    @Autowired
    private CalcService calcService;

    private EntityMeta permissionTestEntityMeta;
    private EntityMeta userEntityMeta;
    private EntityMeta roleEntityMeta;
    private EntityMeta baEntityMeta;
    private EntityMeta calculationEntityMeta;

    private BusinessAction baEdit;
    private BusinessAction baView;
    private BusinessAction baRemove;
    private BusinessAction baUseAtCalc;
    private BusinessAction baCreateNew;
    private BusinessAction baPublish;

    /**
     * Check if default user has calc permissions (by role АДМ).
     */
    @Test
    public void test00DefaultUserAdmPermission() {
        User user = securityService.getCurrentUser();
        assertThat(securityService.isPermitted(user, calculationEntityMeta, Action.CREATE_NEW)).isTrue();
        assertThat(securityService.isPermitted(user, calculationEntityMeta, Action.VIEW)).isTrue();
        assertThat(securityService.isPermitted(user, calculationEntityMeta, Action.EDIT)).isTrue();
        assertThat(securityService.isPermitted(user, calculationEntityMeta, Action.REMOVE)).isTrue();
        assertThat(securityService.isPermitted(user, calculationEntityMeta, Action.EXECUTE)).isTrue();
        assertThat(securityService.isPermitted(user, calculationEntityMeta, Action.PUBLISH)).isTrue();
        assertThat(securityService.isPermitted(user, calculationEntityMeta, Action.CREATE_COPY)).isTrue();
    }

    /**
     * Test for {@link SecurityService#defineSecurityContext(String)}}.
     */
    @Test
    public void test01CreateDummySecurityContext() {
        securityService.defineSecurityContext(ADMINWF_LOGIN);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(ADMINWF_LOGIN);
    }

    /**
     * Test for {@link SecurityService#getCurrentUser()}.
     */
    @Test
    public void test02GetUser() {
        securityService.defineSecurityContext(ADMINWF_LOGIN);
        User user = securityService.getCurrentUser();
        assertThat(ADMINWF_LOGIN).isEqualToIgnoringCase(user.getLogin());
        assertThat(ADMINWF_ID).isEqualTo(user.getHubId());
        assertThat(user.getFullName()).isEqualToIgnoringCase(FULL_NAME);
        List<Entity> tels = ((LinkedEntityAttribute<Entity>) user.getAttribute(User.UserAttributeMeta.TEL_NUMBER.getKey())).getEntityList();
        assertThat(tels).hasSize(1);
        assertThat(tels.get(0).getAttribute(UserTelAttributeMeta.TEL_NUMBER.getKey()).getValue()).isEqualTo(USER_TEL_NUMBER);
        List<Entity> emails = ((LinkedEntityAttribute<Entity>) user.getAttribute(User.UserAttributeMeta.EMAIL.getKey())).getEntityList();
        assertThat(emails).hasSize(1);
        assertThat(emails.get(0).getAttribute(UserEmailAttributeMeta.EMAIL.getKey()).getValue()).isEqualTo(USER_EMAIL);

        Department department = user.getDepartment();
        assertThat(department).isNotNull();
        assertThat(department.getName().getDescriptionRu()).isEqualTo(ADMINWF_SHORT_DEPARTMENT);
        assertThat(department.getFullName().getDescriptionRu()).isEqualTo(ADMINWF_FULL_DEPARTMENT);
        assertThat(department.isChildrenExists()).isEqualTo(true);
        assertThat(department.isParentExists()).isEqualTo(false);
    }

    /**
     * Get user by name test.
     */
    @Test
    public void test02x2getUserByName() {
        assertThat(getUser("NO_SUCH_DUMMY_USER")).isEmpty();
        assertThat(getUser(ADMINWF_LOGIN)).hasSize(1);
        assertThat(getUser("Иванович")).isNotEmpty();
        assertThat(getUser("Иванов")).isNotEmpty();
    }

    /**
     * Returns user.
     * @param searchValue search value
     * @return user
     */
    private List<User> getUser(String searchValue) {
        searchValue = (!searchValue.contains("%")) ? ("%" + searchValue + "%") : searchValue;
        Criteria criteria = new Criteria();
        Where where = criteria.getWhere();
        where.addItem(new WhereItem(userEntityMeta.getKeyAttribute(),
                                    Operator.LIKE, searchValue));
        where.addItem(new WhereItem(Conjunction.OR, userEntityMeta.getAttributeMetadata(User.UserAttributeMeta.SURNAME.getKey()),
                                    Operator.LIKE, searchValue));
        where.addItem(new WhereItem(Conjunction.OR, userEntityMeta.getAttributeMetadata(User.UserAttributeMeta.NAME.getKey()),
                                    Operator.LIKE, searchValue));
        where.addItem(new WhereItem(Conjunction.OR, userEntityMeta.getAttributeMetadata(User.UserAttributeMeta.PATRONYMIC.getKey()),
                                    Operator.LIKE, searchValue));
        return userRoleService.getUsers(criteria, null, null);
    }

    /**
     * Get user department test.
     */
    @Test
    public void test03GetDepartment() {
        EntityMeta departmentMeta = entityMetaService.getEntityMetaByKey(Department.METADATA_KEY, null);
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(departmentMeta.getKeyAttribute(), Operator.EQ, DEPARTMENT_KEY));
        List<Department> departments = (List<Department>) entityService.getEntities(departmentMeta, criteria, null, null);
        assertThat(departments).isNotEmpty();
        Department department = departments.get(0);
        assertThat(department.getName().getDescriptionRu()).isEqualTo(LIPETSK_SHORT_DEPARTMENT);
        assertThat(department.getFullName().getDescriptionRu()).isEqualTo(LIPETSK_FULL_DEPARTMENT);
        assertThat(department.isChildrenExists()).isEqualTo(true);
        assertThat(department.isParentExists()).isEqualTo(true);
    }

    /**
     * Test for user roles.
     */
    @Test
    public void test04GetUserRoles() {
        User user = createTestUser("SECU04", null, null);
        securityService.defineSecurityContext(user.getLogin());

        List<Role> roles = userRoleService.getRoles(null, null, null);
        assertThat(roles).isNotEmpty();

        Role role = roles.get(0);
        user.getRoles().clear();
        userRoleService.persistUser(user);
        user.getRoles().add(role);
        userRoleService.persistUser(user);

        user = securityService.getCurrentUser();
        assertThat(user.getRoles()).size().isEqualTo(1);
        assertThat(user.getRoles().get(0).getKey()).isEqualTo(role.getKey());
    }

    /**
     * Test errors on save embedded role TEST_ROLE2.
     */
    @Test
    public void test04x2PersistsEmbeddedRole() {
        LocalDateTime ldts = metadataDao.getSysTimestamp();

        Criteria criteria = new Criteria();
        Where where = criteria.getWhere();
        where.addItem(new WhereItem(roleEntityMeta.getAttributeMetadata(Role.RoleAttributeMeta.EMBEDDED.getKey()), Operator.EQ, true));
        List<Role> roleList = userRoleService.getRoles(criteria, null, ldts);
        assertThat(roleList).isNotEmpty();
        Role role = roleList.get(0);
        assertThatThrownBy(() -> userRoleService.persistRole(role)).isInstanceOf(CrsException.class)
                                                                   .hasMessageContaining("Can't save embedded role");
        assertThatThrownBy(() -> userRoleService.removeRole(role)).isInstanceOf(CrsException.class)
                                                                  .hasMessageContaining("Can't remove embedded role");

        Role role2 = (Role) entityService.newEmptyEntity(Role.METADATA_KEY);
        role2.setKey(TEST_ROLE_2);
        role2.setName(new MultilangDescription("тест", "test"));
        role2.setDescription(new MultilangDescription("тест", "test"));
        role2.setEmbedded(true);
        assertThatThrownBy(() -> userRoleService.persistRole(role2)).isInstanceOf(CrsException.class)
                                                                    .hasMessageContaining("Can't create new embedded role");
    }

    /**
     * Test getting allowed actions.
     */
    @Test
    public void test05AllowableBusinessActions() {
        // get actions for dictionary  this.permissionTestEntityMeta
        List<BusinessAction> actions = securityService.getEntityMetaBusinessActions(this.permissionTestEntityMeta, null);
        assertThat(actions).extracting("key").containsExactlyInAnyOrder(Action.VIEW.name(),
                                                                        Action.EDIT.name(),
                                                                        Action.REMOVE.name());

        // get actions for CALC
        actions = securityService.getEntityMetaBusinessActions(entityMetaService.getEntityMetaByKey(Calculation.METADATA_KEY, null), null);
        assertThat(actions).extracting("key").containsExactlyInAnyOrder(Action.CREATE_NEW.name(),
                                                                        Action.VIEW.name(),
                                                                        Action.EDIT.name(),
                                                                        Action.REMOVE.name(),
                                                                        Action.EXECUTE.name(),
                                                                        Action.PUBLISH.name(),
                                                                        Action.CREATE_COPY.name());

        BusinessAction view = actions.stream().filter(ba -> ba.getKey().equals(Action.VIEW.name())).findFirst().get();
        assertThat(view.isCancelSecextRuleAvailable()).isTrue();
        BusinessAction edit = actions.stream().filter(ba -> ba.getKey().equals(Action.EDIT.name())).findFirst().get();
        assertThat(edit.isCancelSecextRuleAvailable()).isFalse();
    }

    /**
     * Test setting remove permissions for test entity on role TEST_ROLE.
     */
    @Test
    public void test06RolePermissions() {
        Role role = createTestRole(TEST_ROLE);

        List<BusinessAction> availableActions = securityService.getEntityMetaBusinessActions(this.permissionTestEntityMeta, null);
        List<Permission> rolePermissions = userRoleService.getEntityMetaPermissions(permissionTestEntityMeta, role, null);
        assertThat(rolePermissions).isEmpty();

        for (BusinessAction action : availableActions) {
            Permission perm = (Permission) entityService.newEmptyEntity(Permission.METADATA_KEY);
            perm.setAction(action);
            perm.setPermit(true);
            perm.setRole(role);
            perm.setEntityMeta(permissionTestEntityMeta);
            perm.setKey(role.getKey() + ":" + permissionTestEntityMeta.getKey() + ":" + action.getKey()); // just for test compare at assert
            rolePermissions.add(perm);
        }
        persistsRolePermissions(permissionTestEntityMeta, role, rolePermissions);

        List<Permission> rolePermissions2 = userRoleService.getEntityMetaPermissions(permissionTestEntityMeta, role, null);
        assertThat(rolePermissions2).containsExactlyInAnyOrder(rolePermissions.toArray(new Permission[] {}));

        // no EDIT
        Permission editPermission = rolePermissions2.stream().filter(p -> p.getAction().getKey().equals(Action.EDIT.name())).findFirst().get();
        assertThat(editPermission.isCancelSecextRule()).isFalse();
        editPermission.setPermit(false);
        editPermission.setCancelSecextRule(true);
        persistsRolePermissions(permissionTestEntityMeta, role, rolePermissions2);
        rolePermissions2 = userRoleService.getEntityMetaPermissions(permissionTestEntityMeta, role, null);
        Permission editPermission2 = rolePermissions2.stream().filter(p -> p.getAction().getKey().equals(Action.EDIT.name())).findFirst().get();
        assertThat(editPermission2.isPermit()).isFalse();
        assertThat(editPermission2.isCancelSecextRule()).isTrue();
    }

    /**
     * Test user permission getting.
     */
    @Test
    public void test07UserPermission() {
        User user = createTestUser("SECU07", null, null);
        // set roles TEST_ROLE2   TEST_ROLE
        Role role1;
        Role role2;
        if (!user.getRoles().stream().anyMatch(r -> r.getKey().equals(TEST_ROLE))) {
            role1 = createTestRole(TEST_ROLE);
            role2 = createTestRole(TEST_ROLE_2);
            user.setRoles(Arrays.asList(role1, role2));
            userRoleService.persistUser(user);
        } else {
            role1 = user.getRoles().stream().filter(r -> r.getKey().equals(TEST_ROLE)).findFirst().get();
            role2 = user.getRoles().stream().filter(r -> r.getKey().equals(TEST_ROLE_2)).findFirst().get();
        }

        //             EDIT, VIEW, REMOVE, USE_AT_CALC
        // TEST_ROLE   Y     N        Y       N
        // TEST_ROLE2  Y     N        N       Y

        // for TEST_ROLE
        List<Permission> rolePermissions = Arrays.asList(createTestPermission(permissionTestEntityMeta, role1, true, baEdit),
                                                         createTestPermission(permissionTestEntityMeta, role1, false, baView),
                                                         createTestPermission(permissionTestEntityMeta, role1, true, baRemove),
                                                         createTestPermission(permissionTestEntityMeta, role1, false, baUseAtCalc));
        persistsRolePermissions(permissionTestEntityMeta, role1, rolePermissions);

        // for TEST_ROLE2
        rolePermissions = Arrays.asList(createTestPermission(permissionTestEntityMeta, role2, true, baEdit),
                                        createTestPermission(permissionTestEntityMeta, role2, false, baView),
                                        createTestPermission(permissionTestEntityMeta, role2, false, baRemove),
                                        createTestPermission(permissionTestEntityMeta, role2, true, baUseAtCalc));
        persistsRolePermissions(permissionTestEntityMeta, role2, rolePermissions);

        assertThat(securityService.isPermitted(user, permissionTestEntityMeta, Action.EDIT)).isTrue();
        assertThat(securityService.isPermitted(user, permissionTestEntityMeta, Action.EXECUTE)).isFalse();
        assertThat(securityService.isPermitted(user, permissionTestEntityMeta, Action.VIEW)).isFalse();
        assertThat(securityService.isPermitted(user, permissionTestEntityMeta, Action.REMOVE)).isTrue();
        assertThat(securityService.isPermitted(user, permissionTestEntityMeta, Action.USE_AT_CALC)).isTrue();

        //             EDIT, VIEW, REMOVE, USE_AT_CALC
        // TEST_ROLE   Y     N        Y       N
        // TEST_ROLE2  N     N        N       N
        // for TEST_ROLE2
        rolePermissions = Arrays.asList(createTestPermission(permissionTestEntityMeta, role2, false, baEdit),
                                        createTestPermission(permissionTestEntityMeta, role2, false, baView),
                                        createTestPermission(permissionTestEntityMeta, role2, false, baRemove),
                                        createTestPermission(permissionTestEntityMeta, role2, false, baUseAtCalc));
        persistsRolePermissions(permissionTestEntityMeta, role2, rolePermissions);
        assertThat(securityService.isPermitted(user, permissionTestEntityMeta, Action.EDIT)).isTrue();
        assertThat(securityService.isPermitted(user, permissionTestEntityMeta, Action.EXECUTE)).isFalse();
        assertThat(securityService.isPermitted(user, permissionTestEntityMeta, Action.VIEW)).isFalse();
        assertThat(securityService.isPermitted(user, permissionTestEntityMeta, Action.REMOVE)).isTrue();
        assertThat(securityService.isPermitted(user, permissionTestEntityMeta, Action.USE_AT_CALC)).isFalse();
    }

    /**
     * Test call level authorizations.
     */
    @Test
    public void test08AuthorizationAnnotated() {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        User user = createTestUser("SECU08", null, null);
        user.getRoles().clear();
        // set roles TEST_ROLE2   TEST_ROLE
        Role role = createTestRole(TEST_ROLE);
        user.getRoles().add(role);
        userRoleService.persistUser(user);
        securityService.defineSecurityContext(user.getLogin());
        EntityMeta calcEntityMeta = entityMetaService.getEntityMetaByKey(Calculation.METADATA_KEY, ldts);
        List<Permission> rolePermissions = Arrays.asList(createTestPermission(calcEntityMeta, role, true, baEdit),
                                                         createTestPermission(calcEntityMeta, role, true, baView),
                                                         createTestPermission(calcEntityMeta, role, true, baRemove),
                                                         createTestPermission(calcEntityMeta, role, false, baUseAtCalc));
        persistsRolePermissions(calcEntityMeta, role, rolePermissions);
        assertThat(securityService.isPermitted(user, calcEntityMeta, Action.VIEW)).isTrue();
        final int five = 5;
        List<Calculation> calculations = calcService.getCalculations(null, RowRange.newAsPageAndSize(1, five), ldts);

        rolePermissions = Arrays.asList(createTestPermission(calcEntityMeta, role, true, baEdit),
                                        createTestPermission(calcEntityMeta, role, false, baView),
                                        createTestPermission(calcEntityMeta, role, true, baRemove),
                                        createTestPermission(calcEntityMeta, role, false, baUseAtCalc));
        persistsRolePermissions(calcEntityMeta, role, rolePermissions);
        assertThat(securityService.isPermitted(user, calcEntityMeta, Action.VIEW)).isFalse();
        assertThatThrownBy(() -> calcService.getCalculations(null, RowRange.newAsPageAndSize(1, five), ldts))
                .isInstanceOf(AccessDeniedException.class);
    }

    /**
     * Test fine grained access to calculations by reading.
     */
    @Test
    public void test09ViewAuthorisationBackend() {

        User user1 = securityService.getCurrentUser();
        Role role = createTestRole(TEST_ROLE3);
        List<Permission> rolePermissions = Arrays.asList(createTestPermission(calculationEntityMeta, role, true, baEdit),
                                                         createTestPermission(calculationEntityMeta, role, true, baView),
                                                         createTestPermission(calculationEntityMeta, role, true, baRemove),
                                                         createTestPermission(calculationEntityMeta, role, true, baCreateNew),
                                                         createTestPermission(calculationEntityMeta, role, true, baPublish),
                                                         createTestPermission(calculationEntityMeta, role, false, baUseAtCalc));
        persistsRolePermissions(calculationEntityMeta, role, rolePermissions);
        Department dep = createTestDepartment(DEPARTMENT_KEY);
        Department depOne = createTestDepartment(DEPARTMENT_ONE_KEY);
        User user2 = createTestUser("SECU0902", role, dep);
        User user3 = createTestUser("SECU0903", role, dep);
        User user4 = createTestUser("SECU0904", role, depOne);

        // as user2
        securityService.defineSecurityContext(user2.getLogin());
        assertThat(securityService.isPermitted(user2, calculationEntityMeta, Action.VIEW)).isTrue();
        Calculation calc = createTestCalculationIfNotexists("SEC0901", true, true);
        Calculation calc2 = createTestCalculationIfNotexists("SEC0902", true, true);

        // published, no client
        securityService.defineSecurityContext(user2.getLogin());
        assertThat(securityService.isPermitted(user2, calculationEntityMeta, Action.PUBLISH)).isTrue();

        assertThat(calc.getModel()).isNull();
        calc.setModel(createTestModelIfNotexists("modelsst0901"));
        assertThat(calc.getClientGroup()).isNull();
        calc.setClientGroup(createTestClientGroup("clientgroupsst09"));
        calcService.publishCalculation(calc);

        // as user1
        securityService.defineSecurityContext(user1.getLogin());
        assertThat(getCalculation(calc.getKey())).hasSize(1);
        // as user2
        securityService.defineSecurityContext(user2.getLogin());
        assertThat(getCalculation(calc.getKey())).hasSize(1);

        // as user2
        securityService.defineSecurityContext(user2.getLogin());
        Entity client = createTestClient("SECCL0901", dep);
        calc2.setClient(client);

        assertThat(calc2.getModel()).isNull();
        calc2.setModel(createTestModelIfNotexists("modelsst0902"));
        calcService.publishCalculation(calc2);

        // published has client
        // as user1
        securityService.defineSecurityContext(user1.getLogin());
        assertThat(getCalculation(calc2.getKey())).hasSize(0);
        // as user2
        securityService.defineSecurityContext(user2.getLogin());
        assertThat(getCalculation(calc2.getKey())).hasSize(1);

        // draft
        securityService.defineSecurityContext(user2.getLogin());
        Calculation calc3 = createTestCalculationIfNotexists("SEC0903", true, true);

        // user2
        assertThat(getCalculation(calc3.getKey())).hasSize(1);
        // as user4
        securityService.defineSecurityContext(user4.getLogin());
        assertThat(getCalculation(calc3.getKey())).hasSize(1);
        // user3
        securityService.defineSecurityContext(user3.getLogin());
        assertThat(getCalculation(calc3.getKey())).hasSize(1);
        // user1
        securityService.defineSecurityContext(user1.getLogin());
        assertThat(getCalculation(calc3.getKey())).hasSize(0);

    }

    /**
     * Test for testVEFGRAFOV.
     */
    @Test
    @Ignore
    public void testVEFGRAFOV() {
        User user = userRoleService.getUser("psidorov");
        assertThat(user).isNotNull();
        securityService.defineSecurityContext(user.getLogin());
        assertThat(securityService.isPermitted(user, EntityMeta.METADATA_KEY, Action.CREATE_NEW)).isFalse();
    }

    /**
     * Test call level authorizations.
     */
    @Test
    public void test10WrongPermission() {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        Role role = createTestRole(TEST_ROLE);
        User user = createTestUser("SECU10", role, null);
        securityService.defineSecurityContext(user.getLogin());
        Permission permission = createTestPermission(calculationEntityMeta, role, true, baEdit);
        List<Permission> rolePermissions = Arrays.asList(permission);
        EntityMeta entityTypeMeta = entityMetaService.getEntityMetaByKey(EntityTypeAttributeMeta.METADATA_KEY, ldts);
        Entity entityType = entityService.getEntity(entityTypeMeta, EntityType.CLASSIFIER.name(), ldts);
        permission.setEntityType(entityType);
        assertThatThrownBy(() -> userRoleService.persistsRolePermissions(rolePermissions))
                .isInstanceOf(CrsException.class)
                .hasMessageContaining("Only one of entity meta, entity type or model may be defined for permission");
    }

    /**
     * Check permission settings for entity meta and entity type.
     */
    @Test
    public void test11EntityMetaAndTypePermission() {
        Role role = createTestRole(TEST_ROLE4);
        User user = createTestUser("SECU11", role, null);
        securityService.defineSecurityContext(user.getLogin());

        // none of permission -> FALSE
        assertThat(securityService.isPermitted(user, calculationEntityMeta.getType(), Action.EDIT)).isFalse();
        assertThat(securityService.isPermitted(user, calculationEntityMeta, Action.EDIT)).isFalse();

        // enable entity type permission, null entity type -> TRUE
        Permission permissionEntityType = createTestPermission(calculationEntityMeta.getType(), role, true, baEdit);
        userRoleService.persistsRolePermissions(Collections.singletonList(permissionEntityType));
        assertThat(securityService.isPermitted(user, calculationEntityMeta.getType(), Action.EDIT)).isTrue();
        assertThat(securityService.isPermitted(user, calculationEntityMeta, Action.EDIT)).isTrue();

        // enable entity meta permission -> TRUE
        Permission permissionEntityMeta = createTestPermission(calculationEntityMeta, role, true, baEdit);
        userRoleService.persistsRolePermissions(Collections.singletonList(permissionEntityMeta));
        assertThat(securityService.isPermitted(user, calculationEntityMeta.getType(), Action.EDIT)).isTrue();
        assertThat(securityService.isPermitted(user, calculationEntityMeta, Action.EDIT)).isTrue();

        // disable entity type permission --> TRUE
        permissionEntityType.setPermit(false);
        userRoleService.persistsRolePermissions(Collections.singletonList(permissionEntityType));
        assertThat(securityService.isPermitted(user, calculationEntityMeta.getType(), Action.EDIT)).isFalse();
        assertThat(securityService.isPermitted(user, calculationEntityMeta, Action.EDIT)).isTrue();

        // disable entity meta permission --> FALSE
        permissionEntityMeta.setPermit(false);
        userRoleService.persistsRolePermissions(Collections.singletonList(permissionEntityMeta));
        assertThat(securityService.isPermitted(user, calculationEntityMeta.getType(), Action.EDIT)).isFalse();
        assertThat(securityService.isPermitted(user, calculationEntityMeta, Action.EDIT)).isFalse();

        // enable entity type permission --> FALSE
        permissionEntityType.setPermit(true);
        userRoleService.persistsRolePermissions(Collections.singletonList(permissionEntityType));
        assertThat(securityService.isPermitted(user, calculationEntityMeta.getType(), Action.EDIT)).isTrue();
        assertThat(securityService.isPermitted(user, calculationEntityMeta, Action.EDIT)).isFalse();
    }

    /**
     * Test departments fields.
     */
    @Test
    public void test11DepartmentFields() {
        EntityMeta departmentMeta = entityMetaService.getEntityMetaByKey(Department.METADATA_KEY, null);
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(departmentMeta.getKeyAttribute(), Operator.EQ, DEPARTMENT_KBZ_KEY));
        List<Department> departments = (List<Department>) entityService.getEntities(departmentMeta, criteria, null, null);
        assertThat(departments).isNotEmpty();
        Department department = departments.get(0);
        department.setBranch(true);
        department.setOrgUnit(true);
        entityService.persistEntity(department);
        List<Department> reqDepartments = (List<Department>) entityService.getEntities(departmentMeta, criteria, null, null);
        Department reqDepartment = reqDepartments.get(0);
        assertThat(reqDepartment.isBranch()).isTrue();
        assertThat(reqDepartment.isOrgUnit()).isTrue();
    }

    /**
     * Test roles fields.
     */
    @Test
    public void test12RoleFields() {
        EntityMeta roleMeta = entityMetaService.getEntityMetaByKey(Role.METADATA_KEY, null);
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(roleMeta.getKeyAttribute(), Operator.EQ, ROLE_KEY));
        List<Role> roles = (List<Role>) entityService.getEntities(roleMeta, criteria, null, null);
        assertThat(roles).isNotEmpty();
        Role role = roles.get(0);
        role.setBranch(true);
        role.setApproved(true);
        entityService.persistEntity(role);
        List<Role> reqRoles = (List<Role>) entityService.getEntities(roleMeta, criteria, null, null);
        Role reqRole = reqRoles.get(0);
        assertThat(reqRole.isBranch()).isTrue();
        assertThat(reqRole.isApproved()).isTrue();
    }

    /**
     * Test user roles approved flag.
     */
    @Test
    public void test13UserRoleApproved() {
        final boolean approved = true;

        // List of Roles
        List<Role> roles = userRoleService.getRoles(null, null, null);
        assertThat(roles).isNotEmpty();
        Role role = roles.get(0);

        // Create user
        User user = (User) entityService.newEmptyEntity(User.METADATA_KEY);
        String userKey = TEST_13_USER_KEY + UUID.randomUUID();
        user.setKey(userKey);
        user.setName(userKey);
        user.getRoles().add(role);

        // Add approved attribute to User Role
        LinkedEntityAttribute<EntityMeta> userRoleAttribute =
                (LinkedEntityAttribute<EntityMeta>) user.getAttribute(User.UserAttributeMeta.ROLES.getKey());
        assertThat(userRoleAttribute.getEntityList().size()).isEqualTo(1);
        assertThat(userRoleAttribute.getEntityAttributeList()).isNotEmpty();
        assertThat(userRoleAttribute.getEntityAttributeList().size()).isEqualTo(1);
        EntityAttribute<EntityMeta> entityAttribute = userRoleAttribute.getEntityAttributeList().get(0);
        entityAttribute.getSatellite().setAttributeValue(User.UserRolesAttributeMeta.APPROVED.getKey(), approved);
        userRoleService.persistUser(user);

        // Get approved attribute from User Role
        User newUser = userRoleService.getUser(userKey);
        assertThat(newUser.getRoles()).isNotNull();
        LinkedEntityAttribute<EntityMeta> newUserRoleAttribute =
                (LinkedEntityAttribute<EntityMeta>) newUser.getAttribute(User.UserAttributeMeta.ROLES.getKey());
        EntityAttribute<EntityMeta> newEntityAttribute = newUserRoleAttribute.getEntityAttributeList().get(0);
        assertThat(newEntityAttribute.isSatelliteDefined()).isTrue();
        assertThat(newEntityAttribute.getSatellite().getAttributeValue(User.UserRolesAttributeMeta.APPROVED.getKey())).isEqualTo(approved);
    }

    /**
     * save permission for entity meta and role.
     * @param entityMeta entity meta
     * @param role role
     * @param permissions permissions
     */
    private void persistsRolePermissions(EntityMeta entityMeta, Role role, Collection<Permission> permissions) {
        for (Permission p : permissions) {
            p.setEntityMeta(entityMeta);
            p.setRole(role);
        }
        userRoleService.persistsRolePermissions(permissions);
    }

    /**
     * Read calculation by key.
     * @param key calc key
     * @return list of calculation
     */
    private List<Calculation> getCalculation(String key) {
        assertThat(securityService.isPermitted(securityService.getCurrentUser(), calculationEntityMeta, Action.VIEW))
                .as("VIEW For user " + securityService.getCurrentUser().getLogin()).isTrue();
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(calculationEntityMeta.getKeyAttribute(), Operator.EQ, key));
        return calcService.getCalculations(criteria, null, null);
    }

    /**
     * Create test permission object.
     * @param entityMeta entity meta for permission
     * @param role role to assign
     * @param permit permission flag
     * @param action action to assign
     * @return permission object
     */
    private Permission createTestPermission(EntityMeta entityMeta, Role role, boolean permit, BusinessAction action) {
        Permission perm = (Permission) entityService.newEmptyEntity(Permission.METADATA_KEY);
        perm.setAction(action);
        perm.setPermit(permit);
        perm.setRole(role);
        perm.setEntityMeta(entityMeta);
        return perm;
    }

    /**
     * Create test permission for entity type.
     * @param entityType entity type
     * @param role role
     * @param permit permission flag
     * @param action business action
     * @return permission
     */
    private Permission createTestPermission(EntityType entityType, Role role, boolean permit, BusinessAction action) {
        Entity entityTypeEntity = getEntityType(entityType);
        Permission perm = (Permission) entityService.newEmptyEntity(Permission.METADATA_KEY);
        perm.setAction(action);
        perm.setPermit(permit);
        perm.setRole(role);
        perm.setEntityType(entityTypeEntity);
        return perm;
    }

    /**
     * Setup test instance data.
     * @throws IOException on JSON error
     */
    @Before
    public void setup() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        EntityMeta entityMeta = objectMapper.readValue(EntityServicePersistTest.class.getResourceAsStream("entity_meta_permission_test.json"),
                                                       EntityMeta.class);
        assertNotNull(entityMeta);
        this.permissionTestEntityMeta = createEntityMeta(entityMeta);
        userEntityMeta = entityMetaService.getEntityMetaByKey(User.METADATA_KEY, null);
        roleEntityMeta = entityMetaService.getEntityMetaByKey(Role.METADATA_KEY, null);
        baEntityMeta = entityMetaService.getEntityMetaByKey(BusinessAction.METADATA_KEY, null);
        calculationEntityMeta = entityMetaService.getEntityMetaByKey(Calculation.METADATA_KEY, null);

        Criteria baCriteria = new Criteria();
        baCriteria.getWhere().addItem(
                new WhereItem(baEntityMeta.getKeyAttribute(), Operator.IN, "EDIT", "VIEW", "REMOVE", "PUBLISH", "CREATE_NEW", "USE_AT_CALC"));
        baCriteria.getOrder().addItem(new OrderItem(baEntityMeta.getKeyAttribute()));
        List<BusinessAction> businessActionList = (List<BusinessAction>) entityService.getEntities(baEntityMeta, baCriteria, null, null);
        final int six = 6;
        assertThat(businessActionList).hasSize(six);
        for (BusinessAction ba : businessActionList) {
            if (ba.getKey().equals(Action.VIEW.name())) {
                baView = ba;
            } else if (ba.getKey().equals(Action.EDIT.name())) {
                baEdit = ba;
            } else if (ba.getKey().equals(Action.REMOVE.name())) {
                baRemove = ba;
            } else if (ba.getKey().equals(Action.USE_AT_CALC.name())) {
                baUseAtCalc = ba;
            } else if (ba.getKey().equals(Action.CREATE_NEW.name())) {
                baCreateNew = ba;
            } else if (ba.getKey().equals(Action.PUBLISH.name())) {
                baPublish = ba;
            }
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
     * Create entity metadata.
     * @param entityMeta entity object
     * @return created/persisted metadata
     */
    private EntityMeta createEntityMeta(EntityMeta entityMeta) {
        EntityMeta m = entityMetaService.getEntityMetaByKey(entityMeta.getKey(), null);
        if (m == null) {
            entityMetaService.persistEntityMeta(entityMeta);
        } else {
            return m;
        }
        return entityMetaService.getEntityMetaByKey(entityMeta.getKey(), null);
    }

    /**
     * Create or load test role.
     * @param key role key
     * @return role
     */
    private Role createTestRole(String key) {
        Role role;
        if (entityService.getEntityIdByKey(roleEntityMeta, key) == null) {
            role = (Role) entityService.newEmptyEntity(Role.METADATA_KEY);
            role.setKey(key);
            role.setName(new MultilangDescription("тест " + key, "test " + key));
            role.setDescription(new MultilangDescription("тест " + key, "test " + key));
            userRoleService.persistRole(role);
        } else {
            Criteria criteria = new Criteria();
            criteria.getWhere().addItem(new WhereItem(roleEntityMeta.getKeyAttribute(), Operator.EQ, key));
            List<Role> roleList = userRoleService.getRoles(criteria, null, null);
            assertThat(roleList).hasSize(1);
            role = roleList.get(0);
        }
        return role;
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
            Calculation calculation = (Calculation) entityService.newEmptyEntity(Calculation.METADATA_KEY);
            calculation.setKey(key);
            calculation.setName("draft test " + key);
            LocalDateTime now = LocalDateTime.now();
            calculation.setActuality(now.toLocalDate());
            calculation.setDataActuality(now);
            calculation.setAuthor(securityService.getCurrentUser());
            calculation.setCalculated(calculated);

            calcService.persistCalculation(calculation);
            return calculation;
        } else if (doLoadExisted) {
            Criteria criteria = new Criteria();
            criteria.getWhere().addItem(new WhereItem(calculationEntityMeta.getKeyAttribute(), Operator.EQ, key));
            List<Calculation> calculationList = calcService.getCalculations(criteria, null, null);
            assertThat(calculationList).hasSize(1);
            return calculationList.get(0);
        }
        return null;
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
            return userRoleService.getUser(key);
        }
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

            ((LinkedEntityAttribute) client.getAttribute(ClientAttributeMeta.DEPARTMENT.getKey())).getEntityList().add(department);

            entityService.persistEntity(client);
        }
        return client;
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

    /**
     * Get entity type entity object by enum.
     * @param entityType entity type enum
     * @return entity type entity
     */
    private Entity getEntityType(EntityType entityType) {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        EntityMeta entityTypeMeta = entityMetaService.getEntityMetaByKey(EntityTypeAttributeMeta.METADATA_KEY, ldts);
        Entity entity = entityService.getEntity(entityTypeMeta, entityType.name(), ldts);
        assertThat(entity).isNotNull();
        return entity;
    }

    /**
     * Create test data at database.
     * @param key test object key
     * @return return created or found model
     */
    private Model createTestModelIfNotexists(String key) {
        EntityMeta modelEntityMeta = entityMetaService.getEntityMetaByKey(Model.METADATA_KEY, null);
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

}
