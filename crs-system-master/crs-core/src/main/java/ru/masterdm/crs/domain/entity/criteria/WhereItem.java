package ru.masterdm.crs.domain.entity.criteria;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;

/**
 * Column element of WHERE.
 * This is column-operator-value(s) element.
 * Item should have connectable.conjunction to join with prev item at statement.
 * @author Pavel Masalov
 */
public class WhereItem extends Connectable {

    private AttributeMeta searchAttribute;
    private Object[] values;
    private Operator operator;

    private static final DateTimeFormatter SQL_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter SQL_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String KEY_COLUMN = "KEY";

    /**
     * Create element with values.
     * It should be second or grater element at group connected with previous by conjunction.
     * Values can be used for {@link Operator#IN IN (...)} SQL operator.
     * @param conjunction logic conjunction
     * @param attributeMeta search attribute
     * @param operator one of WHERE operator
     * @param values values used with operator
     */
    public WhereItem(Conjunction conjunction, AttributeMeta attributeMeta, Operator operator, Object... values) {
        super(conjunction);
        this.searchAttribute = attributeMeta;
        this.operator = operator;
        setValues(values);
    }

    /**
     * Create first element at group with values.
     * Values can be used for {@link Operator#IN IN (...)} SQL operator.
     * @param attributeMeta search attribute
     * @param operator one of WHERE operator
     * @param values values used with operator
     */
    public WhereItem(AttributeMeta attributeMeta, Operator operator, Object... values) {
        super();
        this.searchAttribute = attributeMeta;
        this.operator = operator;
        setValues(values);
    }

    /**
     * Create element without value.
     * It should be second or grater element at group connected with previous by conjunction.
     * It may be {@link Operator#IS_NULL IS NULL} query operator.
     * @param conjunction logic conjunction
     * @param attributeMeta search attribute
     * @param operator one of WHERE operator
     */
    public WhereItem(Conjunction conjunction, AttributeMeta attributeMeta, Operator operator) {
        this(conjunction, attributeMeta, operator, (Object[]) null);
    }

    /**
     * Create first element at group without values.
     * @param attributeMeta attribute meta
     * @param operator one of WHERE operator
     */
    public WhereItem(AttributeMeta attributeMeta, Operator operator) {
        this(attributeMeta, operator, (Object[]) null);
    }

    /**
     * Returns attribute that element based on.
     * @return attribute
     */
    public AttributeMeta getSearchAttribute() {
        return searchAttribute;
    }

    /**
     * Sets values.
     * @param values values
     */
    public void setValues(Object[] values) {
        this.values = (values != null && values.length == 0) ? null : values;
        if (operator == Operator.LIKE
            && values[0] instanceof String
            && !values[0].toString().contains("%")
            && searchAttribute.getType() != AttributeType.TEXT) {

            values[0] = values[0].toString() + "%";
        }
    }

    /**
     * Return one value.
     * It's helper method
     * @return value
     */
    public Object getValue() {
        if (values != null && values.length > 0)
            return values[0];
        return null;
    }

    /**
     * Returns comparison operator.
     * @return operator
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * Format parameter value to inject into DSQL text.
     * @param value value of parameter
     * @return formatted string
     */
    private String formatValue(Object value) {
        if (value == null)
            return "null";

        if (value instanceof LocalDate) {
            return "date '" + ((LocalDate) value).format(SQL_DATE) + "'";
        } else if (value instanceof LocalDateTime) {
            return "to_date('" + ((LocalDateTime) value).format(SQL_DATE_TIME) + "','YYYY-MM-DD HH24:MI:SS')";
        } else if (value instanceof Boolean) {
            return (Boolean.FALSE.equals(value)) ? "0" : "1";
        } else if (value instanceof String) {
            return "upper('" + value + "')";
        } else if (value instanceof Enum) {
            return "'" + value + "'";
        }
        return value.toString();
    }

    @Override
    public String getText() {
        String text = (getConjunction() != null) ? getConjunction().getMeaning() + SPACE : "";

        // TODO: for CLOB columns may be use Oracle Text (?)
        String nativeColumn = searchAttribute.getNativeColumn();
        if (searchAttribute.getType() == AttributeType.TEXT && operator == Operator.LIKE && values != null && values.length == 1) {
            text += "dbms_lob.instr(upper(\"" + nativeColumn + "\")," + formatValue(values[0]) + ")!=0";
        } else {
            switch (searchAttribute.getType()) {
                case TEXT:
                    text += "upper(dbms_lob.substr(\"" + nativeColumn + "\",4000,1))";
                    break;
                case STRING:
                    if (KEY_COLUMN.equalsIgnoreCase(nativeColumn))
                        text += nativeColumn;
                    else
                        text += "upper(\"" + nativeColumn + "\")";
                    break;
                default:
                    text += "\"" + nativeColumn + "\"";
            }
            text += SPACE + operator.getMeaning();
            if (values != null && operator != Operator.IS_NOT_NULL && operator != Operator.IS_NULL) {
                if (values.length == 1 && values[0] != null)
                    text += SPACE + formatValue(values[0]);
                else if (values.length > 1)
                    text += SPACE + Arrays.stream(values).map(this::formatValue).collect(Collectors.joining(",", "(", ")"));
            }
        }
        return text;
    }
}