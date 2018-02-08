package ru.masterdm.crs.dao;

import org.springframework.cache.annotation.Cacheable;

/**
 * Takes random number from oracle database.
 * @author Igor Darovskikh
 */
public interface RandomDao {

    /**
     * Returns random number.
     * @return random number.
     */
    @Cacheable("randoms")
    Long getRandom();
}
