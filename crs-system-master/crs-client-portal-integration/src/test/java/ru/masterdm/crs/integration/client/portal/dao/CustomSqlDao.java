package ru.masterdm.crs.integration.client.portal.dao;

import org.apache.ibatis.annotations.Param;

/**
 * DAO supports custom sql.
 * @author Sergey Valiev
 */
public interface CustomSqlDao {

    /**
     * Get string value as result of executing SQL code.
     * @param sql SQL string
     * @return string value
     */
    String getValue(@Param("sql") String sql);
}
