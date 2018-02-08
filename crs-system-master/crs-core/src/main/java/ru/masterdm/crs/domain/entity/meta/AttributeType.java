package ru.masterdm.crs.domain.entity.meta;

/**
 * Available attribute types.
 * @author Sergey Valiev
 */
public enum AttributeType {
    /** Number(1). */
    BOOLEAN,
    /** Varchar. */
    STRING,
    /** Clob. */
    TEXT,
    /** Blob. */
    FILE,
    /** Number. */
    NUMBER,
    /** Date. */
    DATE,
    /** Date. */
    DATETIME,
    /** Link to another entity. */
    REFERENCE
}
