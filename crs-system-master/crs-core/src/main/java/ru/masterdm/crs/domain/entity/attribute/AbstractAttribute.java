package ru.masterdm.crs.domain.entity.attribute;

import java.io.Serializable;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;

/**
 * Base class for any type of attribute.
 * @param <V> Java type to be stored at attribute
 * @author Pavel Masalov
 */
public abstract class AbstractAttribute<V> implements Serializable {

    private AttributeMeta meta;

    /**
     * Default constructor.
     */
    public AbstractAttribute() {
    }

    /**
     * Create attribute with known metadata.
     * @param meta attribute type metadata
     */
    public AbstractAttribute(AttributeMeta meta) {
        this.meta = meta;
    }

    /**
     * Method should be implemented at child to get simple scalar value (that can be viewed at UI).
     * Viewable value is a scalar or collection of attributes (with valid setValue method). As example get {@link LinkedEntityAttribute} that get
     * collection of {@link EntityAttribute}).
     * <p>If attribute type does not support viewable value then raise {@link UnsupportedOperationException}</p>
     * @return viewable value. May be scalar or collection of Attributes with valid
     * @throws UnsupportedOperationException if attribute cant operate with viewable value
     */
    public abstract V getValue();

    /**
     * Method that should be implemented to set simple viewable value to attribute (that can be viewed at UI).
     * Viewable value is a scalar or collection of attributes (with valid setValue method).
     * <p>If attribute type does not support viewable value then raise {@link UnsupportedOperationException}</p>
     * @param value scalar value
     * @throws UnsupportedOperationException if attribute cant operate with viewable value
     * @throws IllegalArgumentException if type pf value not supported for attribute
     */
    public abstract void setValue(V value);

    /**
     * Returns attribute metadata.
     * @return attribute metadata
     */
    public AttributeMeta getMeta() {
        return meta;
    }

    /**
     * Sets attribute metadata.
     * @param meta attribute metadata
     */
    public void setMeta(AttributeMeta meta) {
        this.meta = meta;
    }

    @Override
    public String toString() {
        return super.toString() + " meta " + getMeta();
    }
}
