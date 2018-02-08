package ru.masterdm.crs.domain.entity;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Digest support.
 * Digest calculation is only for not null properties.
 */
public interface DigestSupport {

    /**
     * Returns digest.
     * @return digest
     */
    String getDigest();

    /**
     * Sets digest.
     * @param digest digest
     */
    void setDigest(String digest);

    /**
     * Calculates digest for particular object.
     * @return digest
     */
    String calcDigest();

    /**
     * Calculates digest from passed object array.
     * @param in object array, its allowed to pass array of null or empty array
     * @return digest
     */
    default String calcDigest(Object... in) {
        String concat = Arrays.stream(in).filter(Objects::nonNull).map(p -> {
            if (p instanceof Date) {
                return ((Date) p).toInstant().toString();
            }
            return p.toString();
        }).collect(Collectors.joining());
        return DigestUtils.sha256Hex(concat);
    }
}
