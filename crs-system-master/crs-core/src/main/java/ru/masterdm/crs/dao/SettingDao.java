package ru.masterdm.crs.dao;

import org.apache.ibatis.annotations.Param;

/**
 * Setting DAO.
 * @author Alexey Chalov
 */
public interface SettingDao {

    /**
     * Setting mnemo codes enumeration.
     * @author Alexey Chalov
     */
    enum SettingMnemo {
        INTEGRATION_MODULE_URL
    }

    /**
     * Returns setting value.
     * @param mnemo {@link SettingMnemo} constant
     * @return seting value
     */
    String getSettingValue(@Param("mnemo") SettingMnemo mnemo);
}
