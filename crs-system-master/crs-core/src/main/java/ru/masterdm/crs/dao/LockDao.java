package ru.masterdm.crs.dao;

import org.apache.ibatis.annotations.Param;

/**
 * Access to database named locks.
 * @author Pavel Masalov
 */
public interface LockDao {

    /**
     * Request database lock.
     * @param lockGlobalName unique global lock name
     * @param timeout lock wait timeout
     */
    void requestLock(@Param("lockGlobalName") String lockGlobalName, @Param("timeout") int timeout);

    /**
     * Release database lock.
     * @param lockGlobalName unique global lock name
     */
    void releaseLock(@Param("lockGlobalName") String lockGlobalName);

    /**
     * Get database session id that hold lock.
     * @param lockGlobalName unique global lock name
     * @return session id
     */
    Long getLockSessionId(@Param("lockGlobalName") String lockGlobalName);
}
