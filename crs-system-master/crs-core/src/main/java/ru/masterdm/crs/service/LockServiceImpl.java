package ru.masterdm.crs.service;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import ru.masterdm.crs.dao.LockDao;
import ru.masterdm.crs.domain.Lockable;
import ru.masterdm.crs.exception.LockErrorCode;
import ru.masterdm.crs.exception.LockException;

/**
 * Manage named lock implementation.
 * @author Pavel Masalov
 */
@Validated
@Service("lockService")
public class LockServiceImpl implements LockService {

    @Autowired
    private LockDao lockDao;

    @Value("#{config['system.prefix']}")
    private String lockPrefix;

    @Transactional
    @Override
    public void lock(@NotNull Lockable lockable) {
        lock(lockable, 0);
    }

    @Transactional
    @Override
    public void lock(Lockable lockable, int timeout) {
        try {
            lockDao.requestLock(makeLockGlobalName(lockable), timeout);
        } catch (UncategorizedSQLException e) {
            throw new LockException(LockErrorCode.getByCode(Long.valueOf(e.getSQLException().getErrorCode())), e);
        }
    }

    @Transactional
    @Override
    public void unlock(@NotNull Lockable lockable) {
        try {
            lockDao.releaseLock(makeLockGlobalName(lockable));
        } catch (UncategorizedSQLException e) {
            throw new LockException(LockErrorCode.getByCode(Long.valueOf(e.getSQLException().getErrorCode())), e);
        }
    }

    @Override
    public boolean isLocked(@NotNull Lockable lockable) {
        Long sessionId = lockDao.getLockSessionId(makeLockGlobalName(lockable));
        return sessionId != null;
    }

    /**
     * Utility function to constract global lock name.
     * @param lockable locable object
     * @return global lock name
     */
    private String makeLockGlobalName(Lockable lockable) {
        return lockPrefix + KEY_DELIMITER + lockable.getLockNamespace() + KEY_DELIMITER + lockable.getLockName();
    }
}
