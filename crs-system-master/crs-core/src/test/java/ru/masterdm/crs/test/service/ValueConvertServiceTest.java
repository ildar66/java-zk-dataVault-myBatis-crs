package ru.masterdm.crs.test.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;
import static ru.masterdm.crs.exception.ConverterErrorCode.CANT_FIND_CONVERTER;
import static ru.masterdm.crs.exception.ConverterErrorCode.CONVERTER_CANT_CONVERT_VALUE;
import static ru.masterdm.crs.test.service.PersistTestConstant.ConvertTestFields;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.exception.ConverterErrorCode;
import ru.masterdm.crs.exception.ConverterException;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.ValueConvertService;
import ru.masterdm.crs.util.converter.StringToLocalDateConverter;
import ru.masterdm.crs.util.converter.StringToLocalDateTimeConverter;

/**
 * Value convert service tests.
 * @author Pavel Masalov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml",
                                   "classpath:META-INF/spring/crs-security-test.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ValueConvertServiceTest {

    private EntityMeta entityMeta;

    @Autowired
    private EntityMetaService entityMetaService;
    @Autowired
    private ValueConvertService valueConvertService;

    /**
     * Test converter for BOOLEAN type attribute.
     */
    @Test
    public void test01BooleanConverter() {
        AttributeMeta attributeMeta = entityMeta.getAttributeMetadata(ConvertTestFields.BOOLEAN);
        Pair<Object, Boolean>[] booleanValues = new Pair[] {pair(true, Boolean.TRUE),
                                                            pair(false, Boolean.FALSE),
                                                            //pair(1, Boolean.TRUE),
                                                            //pair(0, Boolean.FALSE),
                                                            pair("FalSe", Boolean.FALSE),
                                                            pair("tRuE", Boolean.TRUE),
                                                            pair("0", Boolean.FALSE),
                                                            pair("1", Boolean.TRUE)};

        testScalarConvert(attributeMeta, booleanValues);

        Pair<Object, ConverterErrorCode>[] wrongBooleanValues = new Pair[] {pair(new Date(), CANT_FIND_CONVERTER)};
        testWrongScalarConvert(attributeMeta, wrongBooleanValues);
    }

    /**
     * Test converter for DATE type attribute.
     */
    @Test
    public void test02LocalDateConverter() {
        AttributeMeta attributeMeta = entityMeta.getAttributeMetadata(ConvertTestFields.DATE);
        LocalDate now = LocalDate.now();
        Date dNow = Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant());

        Pair<Object, LocalDate>[] dateValues = new Pair[] {pair(dNow, now),
                                                           pair(now.format(DateTimeFormatter.ofPattern(StringToLocalDateConverter.FORMAT)), now)};
        testScalarConvert(attributeMeta, dateValues);

        Pair<Object, ConverterErrorCode>[] wrongDateValues = new Pair[] {
                pair("12-12-2017", CONVERTER_CANT_CONVERT_VALUE)}; // TODO create various string converter
        testWrongScalarConvert(attributeMeta, wrongDateValues);
    }

    /**
     * Test converter for DATETIME attribute.
     */
    @Test
    public void test03LocalDateTimeConverter() {
        AttributeMeta attributeMeta = entityMeta.getAttributeMetadata(ConvertTestFields.DATETIME);
        LocalDateTime now = LocalDateTime.now();
        Date dtNow = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());

        Pair<Object, LocalDateTime>[] dateTimeValues = new Pair[] {pair(dtNow, now),
                                                                   pair(now.format(
                                                                           DateTimeFormatter.ofPattern(StringToLocalDateTimeConverter.FORMAT)),
                                                                        now.truncatedTo(ChronoUnit.MINUTES))};
        testScalarConvert(attributeMeta, dateTimeValues);

        Pair<Object, ConverterErrorCode>[] wrongDateTimeValues = new Pair[] {
                pair("12-12-2017 12:12:12", CONVERTER_CANT_CONVERT_VALUE)}; // TODO create various string converter
        testWrongScalarConvert(attributeMeta, wrongDateTimeValues);
    }

    /**
     * Test converter for NUMBER attribute.
     */
    @Test
    public void test04BidDecimalConverter() {
        AttributeMeta attributeMeta = entityMeta.getAttributeMetadata(ConvertTestFields.NUMBER);
        final Double d = new Double(1.2);
        final BigDecimal bd = new BigDecimal("1.2");
        final BigDecimal bdBig = new BigDecimal(123123123.5);
        final Integer i = new Integer(2);
        final BigDecimal bdInt = new BigDecimal(2);

        Pair<Object, BigDecimal>[] bigDecimalValues = new Pair[] {pair(d, bd),
                                                                  pair(i, bdInt),
                                                                  //123,123,113.33     111 111 111.33
                                                                  pair("1.2", bd),
                                                                  pair("1,2", bd)};
        testScalarConvert(attributeMeta, bigDecimalValues);

        assertThat(valueConvertService.convert(attributeMeta, "")).isNull();

        Pair<Object, ConverterErrorCode>[] wrongBigDecimalValues = new Pair[] {
                pair("123 123 123,5", CONVERTER_CANT_CONVERT_VALUE)}; // TODO create various string converter
        testWrongScalarConvert(attributeMeta, wrongBigDecimalValues);
    }

    /**
     * Init external data for test.
     * @throws IOException on JSON error
     */
    @Before
    public void setup() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        EntityMeta entityMeta = objectMapper.readValue(EntityServicePersistTest.class.getResourceAsStream("entity_meta_converter_test.json"),
                                                       EntityMeta.class);
        assertNotNull(entityMeta);
        this.entityMeta = createEntityMeta(entityMeta);
    }

    /**
     * Run converters for values pairs.
     * @param attributeMeta attribute meta
     * @param values values to convert and test
     */
    private void testScalarConvert(AttributeMeta attributeMeta, Pair[] values) {
        for (Pair v : values) {
            Object c = valueConvertService.convert(attributeMeta, v.getLeft());
            assertThat(c).as("Attribute type=%s, src type=%s src=%s tgt=%s conv:%s", attributeMeta.getType().name(), v.getLeft().getClass().getName(),
                             v.getLeft().toString(), v.getRight().toString(), c.toString())
                         .isEqualTo(v.getRight());
        }
    }

    /**
     * Run convert that should produce errors.
     * @param attributeMeta attribute meta
     * @param values values to convert
     */
    private void testWrongScalarConvert(AttributeMeta attributeMeta, Pair[] values) {
        for (Pair v : values) {
            assertThatThrownBy(() -> valueConvertService.convert(attributeMeta, v.getLeft()))
                    .as("Attribute type=%s, src type=%s src=%s", attributeMeta.getType().name(), v.getLeft().getClass().getName(),
                        v.getLeft().toString())
                    .isInstanceOf(ConverterException.class)
                    .hasFieldOrPropertyWithValue("errorCode", v.getRight());
        }
    }

    /**
     * Shortcut method to create pair of objects.
     * @param left left
     * @param right right
     * @return pair object
     */
    private static Pair pair(Object left, Object right) {
        return new ImmutablePair(left, right);
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
}