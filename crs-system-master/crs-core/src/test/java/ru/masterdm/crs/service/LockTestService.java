package ru.masterdm.crs.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import ru.masterdm.crs.domain.Lockable;

/**
 * Its helper test to organize autonomous transactions.
 * @author Pavel Masalov
 */
@Validated
@Component
public class LockTestService {

    @Autowired
    private LockService lockService;

    /**
     * Try to get lock in another transaction.
     * @param lockable locable object
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void tryToLockLock(Lockable lockable) {
        lockService.lock(lockable);
    }

}
