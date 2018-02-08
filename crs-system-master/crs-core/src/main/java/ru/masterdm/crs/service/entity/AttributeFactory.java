package ru.masterdm.crs.service.entity;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.type.JdbcType;
import org.jooq.DataType;
import org.jooq.impl.SQLDataType;
import org.springframework.util.NumberUtils;

import ru.masterdm.crs.domain.entity.AbstractDvEntity;
import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.attribute.BooleanAttribute;
import ru.masterdm.crs.domain.entity.attribute.DateAttribute;
import ru.masterdm.crs.domain.entity.attribute.DatetimeAttribute;
import ru.masterdm.crs.domain.entity.attribute.FileInfoAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.MultilangAttribute;
import ru.masterdm.crs.domain.entity.attribute.NumberAttribute;
import ru.masterdm.crs.domain.entity.attribute.StringAttribute;
import ru.masterdm.crs.domain.entity.attribute.value.ValueAccessor;
import ru.masterdm.crs.domain.entity.attribute.value.ValueKeeper;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.exception.CrsException;

/**
 * Attribute factory.
 * @author Sergey Valiev
 */
public final class AttributeFactory {

    /**
     * Constructor.
     */
    private AttributeFactory() {
    }

    /**
     * Create new empty attribute.
     * @param attributeMeta attribute metadata
     * @return new attribute
     */
    public static AbstractAttribute newAttribute(AttributeMeta attributeMeta) {
        return newAttribute(attributeMeta, null, null);
    }

    /**
     * Create new attribute with value.
     * @param attributeMeta attribute metadata
     * @param value attribute value
     * @return new attribute
     */
    public static AbstractAttribute newAttribute(AttributeMeta attributeMeta, Object value) {
        return newAttribute(attributeMeta, value, null);
    }

    /**
     * Creates new attribute with kind of value.
     * @param attributeMeta attribute metadata
     * @param value concrete value
     * @param valueAccessor value accessor for member
     * @return new empty attribute
     */
    public static AbstractAttribute newAttribute(AttributeMeta attributeMeta, Object value, ValueAccessor valueAccessor) {
        switch (attributeMeta.getType()) {
            case BOOLEAN:
                return new BooleanAttribute(attributeMeta, buildValueAccessor(value, valueAccessor));
            case STRING:
            case TEXT:
                if (attributeMeta.isMultilang())
                    return valueAccessor == null ? new MultilangAttribute(attributeMeta) : new MultilangAttribute(attributeMeta, valueAccessor);
                return new StringAttribute(attributeMeta, buildValueAccessor(value, valueAccessor));
            case NUMBER:
                if (value != null)
                    value = (BigDecimal) NumberUtils.convertNumberToTargetClass((Number) value, BigDecimal.class);
                return new NumberAttribute(attributeMeta, buildValueAccessor(value, valueAccessor));
            case DATE:
                return new DateAttribute(attributeMeta, buildValueAccessor(value, valueAccessor));
            case DATETIME:
                return new DatetimeAttribute(attributeMeta, buildValueAccessor(value, valueAccessor));
            case FILE:
                return valueAccessor == null ? new FileInfoAttribute(attributeMeta) : new FileInfoAttribute(attributeMeta, valueAccessor);
            case REFERENCE:
                return value == null ? new LinkedEntityAttribute(attributeMeta)
                                     : new LinkedEntityAttribute(attributeMeta, (List<AbstractDvEntity>) value);
            default:
                throw new IllegalArgumentException("Wrong attribute:type = " + attributeMeta.getKey() + ":" + attributeMeta.getType());
        }
    }

    /**
     * Build proper value accessor from input.
     * @param value concrete value
     * @param valueAccessor value assessor for field
     * @return value accessor
     */
    private static ValueAccessor buildValueAccessor(Object value, ValueAccessor valueAccessor) {
        if (valueAccessor == null) {
            valueAccessor = new ValueKeeper<>(value);
        } else if (value != null) {
            valueAccessor.set(value);
        }

        return valueAccessor;
    }

    /**
     * Decodes {@link DataType} from {@link AttributeMeta} instance.
     * @param attributeMeta {@link AttributeMeta} instance
     * @return {@link DataType} constant
     */
    public static DataType<?> newDataType(AttributeMeta attributeMeta) {
        AttributeType dataType = attributeMeta.getType();
        switch (dataType) {
            case BOOLEAN:
                return SQLDataType.NUMERIC.precision(1, 0);
            case DATE:
            case DATETIME:
                return SQLDataType.DATE;
            case NUMBER:
                return SQLDataType.NUMERIC;
            case STRING:
                return attributeMeta.isMultilang() ? null : SQLDataType.VARCHAR;
            case TEXT:
                return attributeMeta.isMultilang() ? null : SQLDataType.CLOB;
            default:
                return null;
        }
    }

    /**
     * Get Mybatis Jdbc type by attribute type.
     * @param attributeMeta attribute metadata
     * @return Mybatis Jdbc type, null if attribute not stored at SQL data directly
     */
    public static JdbcType getMybatisJdbcType(AttributeMeta attributeMeta) {
        switch (attributeMeta.getType()) {
            case BOOLEAN:
                return JdbcType.NUMERIC;
            case STRING:
                return JdbcType.VARCHAR;
            case TEXT:
                return JdbcType.CLOB;
            case FILE:
                return JdbcType.BLOB;
            case NUMBER:
                return JdbcType.NUMERIC;
            case DATE:
            case DATETIME:
                return JdbcType.DATE;
            default:
                return null;
        }
    }

    /**
     * Get Java class used to store attribute value.
     * @param attributeMeta attribute metadata
     * @return Java class, null if attribute does not stored at separate Java type
     */
    public static Class getJavaClass(AttributeMeta attributeMeta) {
        switch (attributeMeta.getType()) {
            case BOOLEAN:
                return Boolean.class;
            case STRING:
            case TEXT:
                return String.class;
            case FILE:
                return InputStream.class;
            case NUMBER:
                return BigDecimal.class;
            case DATE:
                return LocalDate.class;
            case DATETIME:
                return LocalDateTime.class;
            default:
                throw new CrsException("No Java type associated for attribute type " + attributeMeta.getType().name());
        }
    }
}
