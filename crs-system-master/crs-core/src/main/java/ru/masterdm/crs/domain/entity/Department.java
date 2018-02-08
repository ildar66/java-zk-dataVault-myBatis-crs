package ru.masterdm.crs.domain.entity;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EmbeddedAttributeMeta;
import ru.masterdm.crs.service.entity.AttributeFactory;

/**
 * Department entity.
 * @author Alexey Kirilchev
 */
public class Department extends Entity {

    /**
     * Department's meta attributes.
     * @author Alexey Kirilchev
     */
    public enum DepartmentAttributeMeta implements EmbeddedAttributeMeta {
        /** Name. */
        NAME,
        /** Full name. */
        FULL_NAME,
        /** Comment. */
        COMMENT,
        /** Branch. */
        BRANCH,
        /** Show Branch Short Name. */
        ORG_UNIT;

        @Override
        public String getKey() {
            return METADATA_KEY + KEY_DELIMITER + this.name();
        }
    }

    /**
     * Model metadata key.
     */
    public static final String METADATA_KEY = "DEPARTMENT";

    private MultilangDescription name;
    private MultilangDescription fullName;
    private MultilangDescription comment;
    private boolean branch;
    private boolean orgUnit;

    @Override
    public AbstractAttribute newAttribute(AttributeMeta attributeMeta) {
        // TODO create annotation based attribute value accessors creation
        if (attributeMeta.getKey().equals(DepartmentAttributeMeta.NAME.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor(this::setName, this::getName));
        } else if (attributeMeta.getKey().equals(DepartmentAttributeMeta.FULL_NAME.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor(this::setFullName, this::getFullName));
        } else if (attributeMeta.getKey().equals(DepartmentAttributeMeta.COMMENT.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor(this::setComment, this::getComment));
        } else if (attributeMeta.getKey().equals(DepartmentAttributeMeta.BRANCH.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor(this::setBranch, this::isBranch));
        } else if (attributeMeta.getKey().equals(DepartmentAttributeMeta.ORG_UNIT.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor(this::setOrgUnit, this::isOrgUnit));
        } else {
            return super.newAttribute(attributeMeta);
        }
    }

    /**
     * Returns name.
     * @return name
     */

    public MultilangDescription getName() {
        if (name == null)
            name = new MultilangDescription();
        return name;
    }

    /**
     * Sets name.
     * @param name name
     */
    public void setName(MultilangDescription name) {
        this.name = name;
    }

    /**
     * Returns full name.
     * @return full name
     */
    public MultilangDescription getFullName() {
        if (fullName == null)
            fullName = new MultilangDescription();
        return fullName;
    }

    /**
     * Sets full name.
     * @param fullName full name
     */
    public void setFullName(MultilangDescription fullName) {
        this.fullName = fullName;
    }

    /**
     * Returns comment.
     * @return comment
     */
    public MultilangDescription getComment() {
        if (comment == null)
            comment = new MultilangDescription();
        return comment;
    }

    /**
     * Sets comment.
     * @param comment comment
     */
    public void setComment(MultilangDescription comment) {
        this.comment = comment;
    }

    /**
     * Returns branch.
     * @return branch
     */
    public boolean isBranch() {
        return branch;
    }

    /**
     * Sets branch.
     * @param branch branch
     */
    public void setBranch(boolean branch) {
        this.branch = branch;
    }

    /**
     * Returns ShowBranchShortName.
     * @return ShowBranchShortName
     */
    public boolean isOrgUnit() {
        return orgUnit;
    }

    /**
     * Sets orgUnit.
     * @param orgUnit orgUnit
     */
    public void setOrgUnit(boolean orgUnit) {
        this.orgUnit = orgUnit;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
