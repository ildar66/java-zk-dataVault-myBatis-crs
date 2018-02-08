package ru.masterdm.crs.domain.form;

/**
 * Create option enum.
 * @author Eugene Melnikov
 */
public enum CreateOption {
    /** Never creates target object. */
    NEVER,
    /** Creates target object if not exists. */
    IF_NOT_EXISTS,
    /** Always creates target object. */
    ALWAYS
}
