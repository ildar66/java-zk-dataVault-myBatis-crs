package ru.masterdm.crs.domain.entity.meta;

/**
 * Available entity types.
 * @author Sergey Valiev
 */
public enum EntityType {
    /** Dictionary. */
    DICTIONARY,
    /** Predefined dictionary. */
    PREDEFINED_DICTIONARY,
    /** Input form, e.g. balance. */
    INPUT_FORM,
    /** Classifier. */
    CLASSIFIER,
    /** Embedded object. */
    EMBEDDED_OBJECT
}
