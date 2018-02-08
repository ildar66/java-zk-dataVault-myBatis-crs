package ru.masterdm.crs.domain.entity.criteria;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;

/**
 * Used to create ORDER BY at query.
 * @author Pavel Masalov
 */
public class Order {

    private List<OrderItem> orderItemList = new ArrayList<>();

    /**
     * Add new sort element.
     * @param orderItem element
     */
    public void addItem(OrderItem orderItem) {
        orderItemList.add(orderItem);
    }

    /**
     * Add attribute as sort element.
     * @param attributeMeta attribute metadata
     * @param descending order direction
     */
    public void addItem(AttributeMeta attributeMeta, boolean descending) {
        addItem(new OrderItem(attributeMeta, descending));
    }

    /**
     * Generate text for ORDER to inject it into DSQL.
     * @return generated text
     */
    public String getText() {
        return orderItemList.stream().map(OrderItem::getText).collect(Collectors.joining(","));
    }

    /**
     * Detect if ORDER statement defined.
     * @return true if order ready to generate text
     */
    public boolean isDefined() {
        return orderItemList.size() > 0;
    }
}
