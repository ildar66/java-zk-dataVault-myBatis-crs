package ru.masterdm.crs.domain.entity.criteria;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;

/**
 * Item of ORDER BY.
 * Item are based on entity attribute.
 * @author Pavel Masalov
 */
public class OrderItem {

    private AttributeMeta sortedAttribute;
    private boolean descending;

    /**
     * Create item.
     * @param attributeMeta attribute used to sort.
     */
    public OrderItem(AttributeMeta attributeMeta) {
        this(attributeMeta, false);
    }

    /**
     * Create item.
     * @param attributeMeta attribute used to sort.
     * @param descending direction of sort
     */
    public OrderItem(AttributeMeta attributeMeta, boolean descending) {
        this.sortedAttribute = attributeMeta;
        this.descending = descending;
    }

    /**
     * Returns column name.
     * @return column name
     */
    public String getColumnName() {
        return sortedAttribute.getNativeColumn();
    }

    /**
     * Returns direction of order.
     * @return direction of order
     */
    public boolean isDescending() {
        return descending;
    }

    /**
     * Returns attribute used to sort.
     * @return attribute metadata
     */
    public AttributeMeta getSortedAttribute() {
        return sortedAttribute;
    }

    /**
     * Generate text for ORDER item.
     * @return generated text
     */
    public String getText() {
        String columnName = AttributeType.STRING == sortedAttribute.getType() ? "upper(\"" + getColumnName() + "\")" : "\"" + getColumnName() + "\"";
        return columnName + (descending ? " desc" : "");
    }
}
