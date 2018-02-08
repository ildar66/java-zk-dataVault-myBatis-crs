package ru.masterdm.crs.domain;

/**
 * Object that require lock should implements this.
 * @author Pavel Masalov
 */
public interface Lockable {

    /**
     * Get lock's namespace.
     * @return namespace
     */
    default String getLockNamespace() {
        return this.getClass().getSimpleName();
    }

    /**
     * Get lock name.
     * @return lock name
     */
    String getLockName();
}
