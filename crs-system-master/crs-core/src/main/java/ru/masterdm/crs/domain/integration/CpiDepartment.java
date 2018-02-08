package ru.masterdm.crs.domain.integration;

import ru.masterdm.crs.domain.AbstractEntity;
import ru.masterdm.crs.domain.MultilangDescription;

/**
 * Client portal integration class for department.
 * @author Alexey Chalov
 */
public class CpiDepartment implements AbstractEntity<Long> {

    private Long id;
    private MultilangDescription name;
    private MultilangDescription fullName;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns department name.
     * @return department name
     */
    public MultilangDescription getName() {
        return name;
    }

    /**
     * Sets department name.
     * @param name department name
     */
    public void setName(MultilangDescription name) {
        this.name = name;
    }

    /**
     * Returns department full name.
     * @return department full name
     */
    public MultilangDescription getFullName() {
        return fullName;
    }

    /**
     * Sets department full name.
     * @param fullName department full name
     */
    public void setFullName(MultilangDescription fullName) {
        this.fullName = fullName;
    }
}
