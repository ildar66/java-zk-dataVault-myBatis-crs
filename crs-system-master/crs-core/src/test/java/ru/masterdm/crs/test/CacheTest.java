package ru.masterdm.crs.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ru.masterdm.crs.dao.RandomDao;

/**
 * Tests GuavaCache.
 * @author Igor Darovskikh
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml"})
public class CacheTest {

    @Autowired
    private RandomDao randomDao;

    /**
     * Tests guava cache.
     */
    @Test
    public void testCache() {
        assertEquals(randomDao.getRandom(), randomDao.getRandom());
    }
}
