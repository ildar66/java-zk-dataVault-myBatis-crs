package ru.masterdm.crs.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.entity.Department;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.Role;
import ru.masterdm.crs.domain.entity.User;

/**
 * Utility operations to operate with securities functionality on backend.
 * @author Pavel Masalov
 */
public interface SecurityDao {

    /**
     * Mark calculations for change security tag by calculation.
     * @param calculation calculation
     */
    @Transactional
    void pendingCalcSecureChangeByCalc(@Param("calculation") Calculation calculation);

    /**
     * Mark calculations for change security tag by user.
     * @param user user
     */
    @Transactional
    void pendingCalcSecureChangeByUser(@Param("user") User user);

    /**
     * Mark calculations for change security tag by department.
     * @param department department
     */
    @Transactional
    void pendingCalcSecureChangeByDepartment(@Param("department") Department department);

    /**
     * Mark calculations for change security tag by role.
     * @param role role
     */
    @Transactional
    void pendingCalcSecureChangeByRole(@Param("role") Role role);

    /**
     * Mark calculations for change security tag by client.
     * @param client client
     */
    @Transactional
    void pendingCalcSecureChangeByClient(@Param("client") Entity client);

    /**
     * Rebuild security tag on calculations.
     */
    @Transactional
    void rebuildSecurityTags();
}
