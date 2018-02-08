package ru.masterdm.crs.exception;

/**
 * Converter exception codes.
 * @author Pavel Masalov
 */
public enum ConverterErrorCode {
    /** Have not converter for value-attribute pair. */
    CANT_FIND_CONVERTER,
    /** Converter cant convert input value. */
    CONVERTER_CANT_CONVERT_VALUE
}
