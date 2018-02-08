package ru.masterdm.crs.web.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.util.resource.Labels;

import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.meta.AttributeLocale;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.web.domain.ReferencedAttributeMetaPair;

/**
 * Entity filter class.
 * @author Igor Matushak
 */
public class EntityFilter {

    private ReferencedAttributeMetaPair referencedAttributeMetaPair;
    private Operator operator;
    private Object value;
    private String dateFormat;
    private String dateTimeFormat;
    private AttributeLocale locale;

    /**
     * Returns referenced attribute meta pair.
     * @return referenced attribute meta pair
     */
    public ReferencedAttributeMetaPair getReferencedAttributeMetaPair() {
        return referencedAttributeMetaPair;
    }

    /**
     * Sets referenced attribute meta pair.
     * @param referencedAttributeMetaPair referenced attribute meta pair
     */
    public void setReferencedAttributeMetaPair(ReferencedAttributeMetaPair referencedAttributeMetaPair) {
        this.referencedAttributeMetaPair = referencedAttributeMetaPair;
    }

    /**
     * Returns operator.
     * @return operator
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * Sets operator.
     * @param operator operator
     */
    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    /**
     * Returns value.
     * @return value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets value.
     * @param value value
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Sets date format.
     * @param dateFormat date format
     */
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * Sets date time format.
     * @param dateTimeFormat date time format
     */
    public void setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }

    /**
     * Sets locale.
     * @param locale locale
     */
    public void setLocale(AttributeLocale locale) {
        this.locale = locale;
    }

    /**
     * Returns attribute meta.
     * @return attribute meta
     */
    public AttributeMeta getAttributeMeta() {
        return getReferencedAttributeMetaPair().getReferencedAttributeMeta() == null
               ? getReferencedAttributeMetaPair().getAttributeMeta()
               : getReferencedAttributeMetaPair().getReferencedAttributeMeta();
    }

    @Override
    public String toString() {
        String result;
        if (value == null) {
            result = StringUtils.EMPTY;
        } else {
            if (value instanceof LocalDate) {
                result = ((LocalDate) value).format(DateTimeFormatter.ofPattern(dateFormat));
            } else if (value instanceof LocalDateTime) {
                result = ((LocalDateTime) value).format(DateTimeFormatter.ofPattern(dateTimeFormat));
            } else if (value instanceof Enum) {
                String name = Labels.getLabel(String.valueOf(value));
                result = name != null ? name : String.valueOf(value);
            } else {
                result = String.valueOf(value);
            }
        }
        if (referencedAttributeMetaPair.getReferencedAttributeMeta() == null) {
            return String.format("%s %s %s", referencedAttributeMetaPair.getAttributeMeta().getName().getDescription(locale),
                                 Labels.getLabel(operator.name()), result);
        } else {
            return String.format("%s (%s) %s %s", referencedAttributeMetaPair.getAttributeMeta().getName().getDescription(locale),
                                 referencedAttributeMetaPair.getReferencedAttributeMeta().getName().getDescription(locale),
                                 Labels.getLabel(operator.name()), result);
        }
    }
}
