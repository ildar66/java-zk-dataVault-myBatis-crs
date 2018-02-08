package ru.masterdm.crs.dao;

import org.apache.ibatis.annotations.Param;

import ru.masterdm.crs.domain.entity.meta.EntityType;

/**
 * Metadata checking dao.
 * @author Sergey Valiev
 */
public interface MetadataCheckingDao {

    /**
     * Returns existence flag.
     * @param type type
     * @return <code>true</code> if type exists
     */
    boolean isEntityTypeExists(@Param("type") EntityType type);

    /**
     * Get scalar long value from arbitrary SQL code.
     * @param sql SQL query string
     * @return single result
     */
    Long selectLong(@Param("sql") String sql);
}
