package ru.masterdm.crs.test.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ru.masterdm.crs.domain.FavoritesChecker;
import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.FormulaResult;
import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.ClientAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientGroupAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.FavoritesAttributeMeta;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.FavoritesService;
import ru.masterdm.crs.service.SecurityService;

/**
 * Tests favorites interaction service.
 * @author Alexey Kirilchev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FavoritesServiceTest {

    private static final String ADMINWF_LOGIN = "adminwf";
    private static final String NO_FAVORITES_LOGIN = "wasadmin";

    @Autowired
    private FavoritesService favoritesService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private EntityService entityService;
    @Autowired
    private EntityMetaService entityMetaService;

    private static boolean setupDone = false;

    private static User user;
    private static User newUser;
    private static User userNotInFavorites;
    private static Entity newClient;
    private static Entity clientNotInFavorites;
    private static Entity newClientGroup;
    private static Entity clientGroupNotInFavorites;
    private static Model newModel;
    private static Model modelNotInFavorites;
    private static Calculation newCalculation;
    private static Calculation calculationNotInFavorites;

    private Entity favorites;

    /**
     * Test get favorites from new user.
     */
    @Test
    public void test01FavoriteExistsForNewUser() {
        favorites = favoritesService.getFavorites();
        assertThat(favorites).isNotNull();

        favorites.getAttributes().clear();
        entityService.persistEntity(favorites);
    }

    /**
     * Tests add favorites.
     */
    @Test
    public void test02AddFavorites() {
        favoritesService.addFavorite(user);
        favoritesService.addFavorite(newUser);
        favoritesService.addFavorite(newClient);
        favoritesService.addFavorite(newClientGroup);
        favoritesService.addFavorite(newModel);
        favoritesService.addFavorite(newCalculation);

        favorites = favoritesService.getFavorites();
        assertThat(((LinkedEntityAttribute) favorites.getAttribute(FavoritesAttributeMeta.USER.getKey())).getEntityList())
                .containsExactlyInAnyOrder(user, newUser);
        assertThat(((LinkedEntityAttribute) favorites.getAttribute(FavoritesAttributeMeta.CLIENT.getKey())).getEntityList())
                .containsExactlyInAnyOrder(newClient);
        assertThat(((LinkedEntityAttribute) favorites.getAttribute(FavoritesAttributeMeta.CLIENT_GROUP.getKey())).getEntityList())
                .containsExactlyInAnyOrder(newClientGroup);
        assertThat(((LinkedEntityAttribute) favorites.getAttribute(FavoritesAttributeMeta.CALC_MODEL.getKey())).getEntityList())
                .containsExactlyInAnyOrder(newModel);
        assertThat(((LinkedEntityAttribute) favorites.getAttribute(FavoritesAttributeMeta.CALC.getKey())).getEntityList())
                .containsExactlyInAnyOrder(newCalculation);
    }

    /**
     * Tests finding favorites in entity list.
     */
    @Test
    public void test03FindFavorites() {
        FavoritesChecker favoritesChecker = favoritesService.findFavorites(Arrays.asList(new Entity[] {newUser, newClient, newClientGroup,
                                                                                                       newModel, newCalculation}));
        assertThat(favoritesChecker.isFavorite(newUser)).isTrue();
        assertThat(favoritesChecker.isFavorite(newClient)).isTrue();
        assertThat(favoritesChecker.isFavorite(newClientGroup)).isTrue();
        assertThat(favoritesChecker.isFavorite(newModel)).isTrue();
        assertThat(favoritesChecker.isFavorite(newCalculation)).isTrue();

        assertThat(favoritesChecker.isFavorite(user)).isFalse();
        assertThat(favoritesChecker.isFavorite(userNotInFavorites)).isFalse();
        assertThat(favoritesChecker.isFavorite(clientNotInFavorites)).isFalse();
        assertThat(favoritesChecker.isFavorite(clientGroupNotInFavorites)).isFalse();
        assertThat(favoritesChecker.isFavorite(modelNotInFavorites)).isFalse();
        assertThat(favoritesChecker.isFavorite(calculationNotInFavorites)).isFalse();
    }

    /**
     * Tests remove favorites.
     */
    @Test
    public void test04RemoveFavorites() {
        favoritesService.removeFavorite(user);
        favoritesService.removeFavorite(newUser);
        favoritesService.removeFavorite(newClient);
        favoritesService.removeFavorite(newClientGroup);
        favoritesService.removeFavorite(newModel);
        favoritesService.removeFavorite(newCalculation);

        favorites = favoritesService.getFavorites();
        assertThat(favorites.getAttributes()).hasSize(0);
    }

    /**
     * Tests not supported entity type.
     */
    @Test
    public void test05CheckNotSupportedEntity() {
        FormulaResult formulaResult = (FormulaResult) entityService.newEmptyEntity(FormulaResult.METADATA_KEY);
        assertThat(catchThrowable(() -> favoritesService.addFavorite(formulaResult)))
                .hasMessageContaining("not supported");
        assertThat(catchThrowable(() -> favoritesService.findFavorites(Arrays.asList(new Entity[] {formulaResult}))))
                .hasMessageContaining("not supported");
    }

    /**
     * Prepares initial test data.
     * @throws Exception exception
     */
    @Before
    public void setup() throws Exception {
        securityService.defineSecurityContext(NO_FAVORITES_LOGIN);
        if (!setupDone) {

            EntityMeta userMeta = entityMetaService.getEntityMetaByKey(User.METADATA_KEY, null);
            Criteria criteria = new Criteria();
            criteria.getWhere().addItem(new WhereItem(userMeta.getKeyAttribute(), Operator.EQ, ADMINWF_LOGIN.toUpperCase()));
            List<User> userList = (List<User>) entityService.getEntities(userMeta, criteria, null, null);
            user = userList.get(0);

            newUser = (User) entityService.newEmptyEntity(User.METADATA_KEY);
            entityService.persistEntity(newUser);

            newClient = entityService.newEmptyEntity(ClientAttributeMeta.METADATA_KEY);
            entityService.persistEntity(newClient);

            newClientGroup = entityService.newEmptyEntity(ClientGroupAttributeMeta.METADATA_KEY);
            entityService.persistEntity(newClientGroup);

            newModel = (Model) entityService.newEmptyEntity(Model.METADATA_KEY);
            newModel.setName(new MultilangDescription("testRu", "testEn"));
            LocalDateTime currentTime = entityMetaService.getSysTimestamp();
            newModel.setActuality(currentTime);
            entityService.persistEntity(newModel);

            newCalculation = (Calculation) entityService.newEmptyEntity(Calculation.METADATA_KEY);
            newCalculation.setName("testName");
            newCalculation.setActuality(currentTime.toLocalDate());
            newCalculation.setDataActuality(currentTime);
            entityService.persistEntity(newCalculation);

            // add entities not in favorites
            userNotInFavorites = (User) entityService.newEmptyEntity(User.METADATA_KEY);
            entityService.persistEntity(userNotInFavorites);

            clientNotInFavorites = entityService.newEmptyEntity(ClientAttributeMeta.METADATA_KEY);
            entityService.persistEntity(clientNotInFavorites);

            clientGroupNotInFavorites = entityService.newEmptyEntity(ClientGroupAttributeMeta.METADATA_KEY);
            entityService.persistEntity(clientGroupNotInFavorites);

            modelNotInFavorites = (Model) entityService.newEmptyEntity(Model.METADATA_KEY);
            modelNotInFavorites.setName(new MultilangDescription("testRu", "testEn"));
            modelNotInFavorites.setActuality(currentTime);
            entityService.persistEntity(modelNotInFavorites);

            calculationNotInFavorites = (Calculation) entityService.newEmptyEntity(Calculation.METADATA_KEY);
            calculationNotInFavorites.setName("testName");
            calculationNotInFavorites.setActuality(currentTime.toLocalDate());
            calculationNotInFavorites.setDataActuality(currentTime);
            entityService.persistEntity(calculationNotInFavorites);

            setupDone = true;
        }
    }

    /**
     * Restore secure state.
     */
    @After
    public void finish() {
        securityService.defineSecurityContext(ADMINWF_LOGIN);
    }
}
