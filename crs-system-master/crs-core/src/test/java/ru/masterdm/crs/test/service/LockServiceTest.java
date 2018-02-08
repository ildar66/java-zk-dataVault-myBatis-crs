package ru.masterdm.crs.test.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import ru.masterdm.crs.domain.Lockable;
import ru.masterdm.crs.exception.LockErrorCode;
import ru.masterdm.crs.exception.LockException;
import ru.masterdm.crs.service.LockService;
import ru.masterdm.crs.service.LockTestService;

/**
 * Tests lock service.
 * @author Pavel Masalov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/spring/crs-core-config.xml",
                                   "classpath:META-INF/spring/crs-datasource-config-test.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LockServiceTest {

    @Autowired
    private LockService lockService;
    @Autowired
    private LockTestService lockTestService;

    /**
     * Lockable object test implementation.
     */
    public static class LockableObj implements Lockable {

        private String key;

        /**
         * Default constructor with locable value.
         * @param key loack key
         */
        public LockableObj(String key) {
            this.key = key;
        }

        @Override
        public String getLockName() {
            return key;
        }
    }

    /**
     * Simple single lock allocation release.
     */
    @Transactional
    @Test
    public void test01LockUnlock() {
        LockableObj o = new LockableObj("L1");
        lockService.lock(o);
        boolean locked = lockService.isLocked(o);
        assertThat(locked).isTrue();
        lockService.unlock(o);
        locked = lockService.isLocked(o);
        assertThat(locked).isFalse();
    }

    /**
     * Concurrent lock allocation.
     */
    @Transactional
    @Test
    public void test02LockAndConcurrentLock() {
        LockableObj o = new LockableObj("L2");
        lockService.lock(o);
        boolean locked = lockService.isLocked(o);
        assertThat(locked).isTrue();

        // try to get locked lock
        assertThatThrownBy(() -> lockTestService.tryToLockLock(o))
                .isInstanceOf(LockException.class)
                .hasFieldOrPropertyWithValue("errorCode", LockErrorCode.TIMEOUT);

        // unlock lock
        lockService.unlock(o);
        locked = lockService.isLocked(o);
        assertThat(locked).isFalse();

        lockTestService.tryToLockLock(o); // lock and release lock after exit method
    }

}
