package ru.masterdm.crs.test.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.masterdm.crs.domain.calc.Calculation.CalculationAttributeMeta;
import static ru.masterdm.crs.domain.calc.FormulaResult.FormulaResultAttributeMeta;
import static ru.masterdm.crs.domain.calc.Model.ModelAttributeMeta;
import static ru.masterdm.crs.domain.entity.User.UserAttributeMeta;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.calc.FormulaResult;
import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.meta.CalculationProfileAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientGroupAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.test.service.calc.CalculationServiceTest;

/**
 * Tests embedded object data sync.
 * @author Sergey Valiev
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EntityServiceEmbeddedTest {

    @Autowired
    private EntityService entityService;

    private EntityMeta entityMetaClassifier01;
    private EntityMeta entityMetaClassifier02;
    private EntityMeta entityMetaInputForm01;
    private EntityMeta entityMetaInputForm02;

    /**
     * User embedded object set/get attribute test.
     */
    @Test
    public void test01EmbeddedUser() {
        User user1 = (User) entityService.newEmptyEntity(User.METADATA_KEY); // set attribute, read member
        user1.setAttributeValue(UserAttributeMeta.NAME.getKey(), "name");
        assertThat(user1.getName()).isEqualTo("name");
        user1.setAttributeValue(UserAttributeMeta.PATRONYMIC.getKey(), "Patronymic");
        assertThat(user1.getPatronymic()).isEqualTo("Patronymic");
        user1.setAttributeValue(UserAttributeMeta.SURNAME.getKey(), "Surname");
        assertThat(user1.getSurname()).isEqualTo("Surname");

        User user2 = (User) entityService.newEmptyEntity(User.METADATA_KEY); // set mebber, read attribute
        user2.setName("name");
        assertThat(user2.getAttributeValue(UserAttributeMeta.NAME.getKey())).isEqualTo("name");
        user2.setPatronymic("Patronymic");
        assertThat(user2.getAttributeValue(UserAttributeMeta.PATRONYMIC.getKey())).isEqualTo("Patronymic");
        user2.setSurname("Surname");
        assertThat(user2.getAttributeValue(UserAttributeMeta.SURNAME.getKey())).isEqualTo("Surname");
    }

    /**
     * Formula result embedded object set/get attribute test.
     */
    @Test
    public void test02EmbeddedFormulaResult() {
        final int t123 = 123;
        final int t321 = 321;
        FormulaResult fr1 = (FormulaResult) entityService.newEmptyEntity(FormulaResult.METADATA_KEY); // set attribute, read member
        fr1.setAttributeValue(FormulaResultAttributeMeta.STRING_RESULT.getKey(), "string");
        assertThat(fr1.getStringResult()).isEqualTo("string");
        fr1.setAttributeValue(FormulaResultAttributeMeta.NUMBER_RESULT.getKey(), new BigDecimal(t123));
        assertThat(fr1.getNumberResult()).isEqualTo(new BigDecimal(t123));
        fr1.setAttributeValue(FormulaResultAttributeMeta.EXCEPTION.getKey(), "exception");
        assertThat(fr1.getException()).isEqualTo("exception");

        FormulaResult fr2 = (FormulaResult) entityService.newEmptyEntity(FormulaResult.METADATA_KEY); // set member, get attribute
        fr2.setStringResult("string");
        assertThat(fr2.getAttributeValue(FormulaResultAttributeMeta.STRING_RESULT.getKey())).isEqualTo("string");
        fr2.setNumberResult(new BigDecimal(t321));
        assertThat(fr2.getAttributeValue(FormulaResultAttributeMeta.NUMBER_RESULT.getKey())).isEqualTo(new BigDecimal(t321));
        fr2.setException("exception");
        assertThat(fr2.getAttributeValue(FormulaResultAttributeMeta.EXCEPTION.getKey())).isEqualTo("exception");
    }

    /**
     * Model embedded object set/get attribute test.
     */
    @Test
    public void test03EmbeddedModel() {
        Model model1 = (Model) entityService.newEmptyEntity(Model.METADATA_KEY); // with linked objects set attribute, check member
        Formula formula1 = new Formula();
        formula1.setKey("FORMULA1");
        ((LinkedEntityAttribute<Formula>) model1.getAttribute(ModelAttributeMeta.FORMULAS.getKey())).add(formula1);
        assertThat(model1.getFormulas()).containsExactlyInAnyOrder(formula1);

        LinkedEntityAttribute<EntityMeta> inputForms = (LinkedEntityAttribute<EntityMeta>) model1
                .getAttribute(ModelAttributeMeta.INPUT_FORMS.getKey());
        inputForms.add(entityMetaClassifier01);
        inputForms.add(entityMetaClassifier02);
        assertThat(model1.getInputForms()).containsExactlyInAnyOrder(entityMetaClassifier01, entityMetaClassifier02);

        LinkedEntityAttribute<EntityMeta> classifiers = (LinkedEntityAttribute<EntityMeta>) model1
                .getAttribute(ModelAttributeMeta.CLASSIFIERS.getKey());
        classifiers.add(entityMetaInputForm01);
        classifiers.add(entityMetaInputForm02);
        assertThat(model1.getClassifiers()).containsExactlyInAnyOrder(entityMetaInputForm01, entityMetaInputForm02);

        model1.setAttributeValue(ModelAttributeMeta.NAME_RU.getKey(), "name ru");
        assertThat(model1.getName().getDescriptionRu()).isEqualTo("name ru");
        model1.setAttributeValue(ModelAttributeMeta.NAME_EN.getKey(), "name en");
        assertThat(model1.getName().getDescriptionEn()).isEqualTo("name en");

        Model model2 = (Model) entityService.newEmptyEntity(Model.METADATA_KEY); // with linked objects set member, check attribute
        Formula formula2 = new Formula();
        formula1.setKey("FORMULA2");
        model2.setFormulas(Collections.singletonList(formula2));
        assertThat(((LinkedEntityAttribute<Formula>) model2.getAttribute(ModelAttributeMeta.FORMULAS.getKey())).getEntityList())
                .containsExactlyInAnyOrder(formula2);

        model2.setInputForms(Arrays.asList(entityMetaClassifier01, entityMetaClassifier02));
        assertThat(((LinkedEntityAttribute<EntityMeta>) model2.getAttribute(ModelAttributeMeta.INPUT_FORMS.getKey())).getEntityList())
                .containsExactlyInAnyOrder(entityMetaClassifier01, entityMetaClassifier02);
        model2.setClassifiers(Arrays.asList(entityMetaInputForm01, entityMetaInputForm02));
        assertThat(((LinkedEntityAttribute<EntityMeta>) model2.getAttribute(ModelAttributeMeta.CLASSIFIERS.getKey())).getEntityList())
                .containsExactlyInAnyOrder(entityMetaInputForm01, entityMetaInputForm02);
        model2.setName(new MultilangDescription("nameRu", "nameEn"));
        assertThat(model2.getAttributeValue(ModelAttributeMeta.NAME_RU.getKey())).isEqualTo("nameRu");
        assertThat(model2.getAttributeValue(ModelAttributeMeta.NAME_EN.getKey())).isEqualTo("nameEn");
    }

    /**
     * Calculation embedded object set/get attribute test.
     */
    @Test
    public void test03EmbeddedCalculation() {
        Calculation calculation1 = (Calculation) entityService
                .newEmptyEntity(Calculation.METADATA_KEY); // with linked objects set attribute, check member
        User user = (User) entityService.newEmptyEntity(User.METADATA_KEY);
        user.setKey("USER1");
        ((LinkedEntityAttribute<User>) calculation1.getAttribute(CalculationAttributeMeta.AUTHOR.getKey())).add(user);
        assertThat(calculation1.getAuthor()).isEqualTo(user);
        Model model = (Model) entityService.newEmptyEntity(Model.METADATA_KEY);
        model.setKey("MODEL1");
        ((LinkedEntityAttribute<Model>) calculation1.getAttribute(CalculationAttributeMeta.MODEL.getKey())).add(model);
        assertThat(calculation1.getModel()).isEqualTo(model);
        Entity client = entityService.newEmptyEntity(ClientAttributeMeta.METADATA_KEY);
        client.setKey("CLIENT1");
        ((LinkedEntityAttribute<Entity>) calculation1.getAttribute(CalculationAttributeMeta.CLIENT.getKey())).add(client);
        assertThat(calculation1.getClient()).isEqualTo(client);
        Entity clientGroup = entityService.newEmptyEntity(ClientGroupAttributeMeta.METADATA_KEY);
        clientGroup.setKey("CLIENT_GROUP1");
        ((LinkedEntityAttribute<Entity>) calculation1.getAttribute(CalculationAttributeMeta.CLIENT_GROUP.getKey())).add(clientGroup);
        assertThat(calculation1.getClient()).isEqualTo(client);
        Entity profile = entityService.newEmptyEntity(CalculationProfileAttributeMeta.METADATA_KEY);
        profile.setKey("CALC_PROFILE1");
        ((LinkedEntityAttribute<Entity>) calculation1.getAttribute(CalculationAttributeMeta.CALC_PROFILE.getKey())).add(profile);
        assertThat(calculation1.getProfiles()).containsExactlyInAnyOrder(profile);

        Calculation calculation2 = (Calculation) entityService
                .newEmptyEntity(Calculation.METADATA_KEY); // with linked objects set member, check attribute
        calculation2.setAuthor(user);
        assertThat(((LinkedEntityAttribute<User>) calculation2.getAttribute(CalculationAttributeMeta.AUTHOR.getKey())).getEntityList())
                .containsExactly(user);
        calculation2.setModel(model);
        assertThat(((LinkedEntityAttribute<Model>) calculation2.getAttribute(CalculationAttributeMeta.MODEL.getKey())).getEntityList())
                .containsExactly(model);
        Entity wrongClient = entityService.newEmptyEntity(User.METADATA_KEY);
        assertThatThrownBy(() -> calculation2.setClient(wrongClient)).isInstanceOf(CrsException.class)
                                                                     .hasMessageContaining("Wrong entity with type=" + User.METADATA_KEY);
        calculation2.setClient(client);
        assertThat(((LinkedEntityAttribute<Entity>) calculation2.getAttribute(CalculationAttributeMeta.CLIENT.getKey())).getEntityList())
                .containsExactly(client);
        Entity wrongClientGroup = entityService.newEmptyEntity(User.METADATA_KEY);
        assertThatThrownBy(() -> calculation2.setClientGroup(wrongClientGroup)).isInstanceOf(CrsException.class)
                                                                               .hasMessageContaining("Wrong entity with type=" + User.METADATA_KEY);
        calculation2.setClientGroup(clientGroup);
        assertThat(((LinkedEntityAttribute<Entity>) calculation2.getAttribute(CalculationAttributeMeta.CLIENT_GROUP.getKey())).getEntityList())
                .containsExactly(clientGroup);
        calculation2.setProfiles(Collections.singletonList(profile));
        assertThat(((LinkedEntityAttribute<Entity>) calculation2.getAttribute(CalculationAttributeMeta.CALC_PROFILE.getKey())).getEntityList())
                .containsExactly(profile);
    }

    /**
     * Prepares initial test data.
     * @throws Exception exception
     */
    @Before
    public void setup() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        entityMetaClassifier01 = objectMapper.readValue(CalculationServiceTest.class.getResourceAsStream("Classifier01.json"),
                                                        EntityMeta.class);
        entityMetaClassifier02 = objectMapper.readValue(CalculationServiceTest.class.getResourceAsStream("Classifier02.json"),
                                                        EntityMeta.class);
        entityMetaInputForm01 = objectMapper.readValue(CalculationServiceTest.class.getResourceAsStream("input-form01.json"),
                                                       EntityMeta.class);
        entityMetaInputForm02 = objectMapper.readValue(CalculationServiceTest.class.getResourceAsStream("input-form02.json"),
                                                       EntityMeta.class);
    }
}
