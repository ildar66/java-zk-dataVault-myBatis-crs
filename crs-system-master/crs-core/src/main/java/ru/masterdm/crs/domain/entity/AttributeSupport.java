package ru.masterdm.crs.domain.entity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.service.entity.AttributeFactory;

/**
 * Objects that support attributes.
 * @author Pavel Masalov
 */
public interface AttributeSupport {
    /**
     * Returns entity attributes value objects.
     * @return attribute object
     */
    Map<String, AbstractAttribute> getAttributes();

    /**
     * Sets entity attributes value objects.
     * @param attributes attribute object
     */
    void setAttributes(Map<String, AbstractAttribute> attributes);

    /**
     * Get metadata for attribute.
     * @param attributeKey attribute key
     * @return attribute metadata
     */
    AttributeMeta getAttributeMetadata(String attributeKey);

    /**
     * Returns attributes are sorted by metadata.
     * @return attributes
     */
    default List<AbstractAttribute> getSortedAttributes() {
        if (getAttributes() == null || getAttributes().size() == 0) {
            return Collections.EMPTY_LIST;
        }

        return getAttributes().values()
                              .stream()
                              .sorted(Comparator.comparingLong(a -> a.getMeta().getViewOrder()))
                              .collect(Collectors.toList());
    }

    /**
     * Add attribute entry to entity.
     * @param attribute attribute to be added
     */
    default void setAttribute(AbstractAttribute attribute) {
        getAttributes().put(attribute.getMeta().getKey(), attribute);
    }

    /**
     * Get attribute by its key.
     * Attribute created on demand to implement novalue-emptycell concept.
     * @param attributeKey attribute key
     * @return {@code null} if entity contains no attribute for the key
     */
    default AbstractAttribute getAttribute(String attributeKey) {
        AbstractAttribute attribute = getAttributes().get(attributeKey);
        if (attribute == null) {
            attribute = newAttribute(attributeKey);
            getAttributes().put(attributeKey, attribute);
        }
        return attribute;
    }

    /**
     * Check if attribute exists in entity.
     * @param attributeKey attribute key
     * @return true is attribute exists
     */
    default boolean isAttributeExists(String attributeKey) {
        return getAttributes().containsKey(attributeKey);
    }
    /**
     * Get viewable value of attribute.
     * For detail see {@link AbstractAttribute#getValue()}
     * @param attributeKey key of attribute
     * @return viewable value of attribute
     */
    default Object getAttributeValue(String attributeKey) {
        return getAttribute(attributeKey).getValue();
    }

    /**
     * Set viewable value for attribute.
     * For detail see {@link AbstractAttribute#setValue(Object)}
     * @param attributeKey key of attribute
     * @param value viewable value of attribute
     * @throws IllegalArgumentException when key not exists in entity metadata
     */
    default void setAttributeValue(String attributeKey, Object value) {
        AbstractAttribute attribute = getAttribute(attributeKey);
        attribute.setValue(value);
    }

    /**
     * Create empty attribute and return it.
     * @param attributeKey attribute key
     * @return attribute object
     */
    default AbstractAttribute newAttribute(String attributeKey) {
        AttributeMeta attributeMeta = getAttributeMetadata(attributeKey);
        if (attributeMeta != null) {
            return newAttribute(attributeMeta);
        } else {
            throw new IllegalArgumentException("Wrong attribute key " + attributeKey);
        }
    }

    /**
     * Create empty attribute and return it.
     * @param attributeMeta attribute meta
     * @return attribute object
     */
    default AbstractAttribute newAttribute(AttributeMeta attributeMeta) {
        return AttributeFactory.newAttribute(attributeMeta);
    }
}
