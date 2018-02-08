package ru.masterdm.crs.exception;

import java.util.stream.Stream;

/**
 * Lock error codes.
 * @author Sergey Valiev
 */
public enum LockErrorCode implements ErrorCode {
    CUSTOM(20002L),
    TIMEOUT(20003L);

    private Long code;

    /**
     * Constructor.
     * @param code error code
     */
    LockErrorCode(Long code) {
        this.code = code;
    }

    @Override
    public Long getCode() {
        return code;
    }

    /**
     * Finds {@link LockErrorCode} by error code.
     * @param code error code
     * @return {@link LockErrorCode}
     */
    public static LockErrorCode getByCode(Long code) {
        return Stream.of(LockErrorCode.values()).filter(ec -> ec.getCode().equals(code)).findFirst().orElse(null);
    }
}
