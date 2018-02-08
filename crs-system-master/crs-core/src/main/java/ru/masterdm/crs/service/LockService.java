package ru.masterdm.crs.service;

import ru.masterdm.crs.domain.Lockable;

/**
 * Manage named lock.
 * @author Pavel Masalov
 */
public interface LockService {

    /**
     * Try to get lock.
     * @param lockable lockable object
     */
    void lock(Lockable lockable);

    /**
     * Try to get lock.
     * @param lockable lockable object
     * @param timeout timeout in seconds to wait lock
     */
    void lock(Lockable lockable, int timeout);

    /**
     * Release lock.
     * @param lockable lockable object
     */
    void unlock(Lockable lockable);

    /**
     * Check if lock locked.
     * @param lockable lockable object
     * @return true if lock is locked
     */
    boolean isLocked(Lockable lockable);

}
