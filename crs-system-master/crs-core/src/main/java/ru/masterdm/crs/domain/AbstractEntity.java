package ru.masterdm.crs.domain;

import java.io.Serializable;

/**
 * Abstract entity interface.
 * @param <T> id type
 * @author Sergey Valiev
 */
public interface AbstractEntity<T> extends Serializable {

    /**
     * Returns id.
     * @return id
     */
    T getId();

    /**
     * Sets id.
     * @param id id
     */
    void setId(T id);
}
