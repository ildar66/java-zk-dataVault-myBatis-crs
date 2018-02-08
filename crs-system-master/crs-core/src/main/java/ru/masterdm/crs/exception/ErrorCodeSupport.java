package ru.masterdm.crs.exception;

/**
 * Error code support.
 * @param <T> type of codifying kinds of error
 * @author Sergey Valiev
 */
public interface ErrorCodeSupport<T> {

    /**
     * Returns error code type.
     * @return error code type
     */
    T getErrorCode();
}
