package ru.masterdm.crs.domain.entity;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EmbeddedAttributeMeta;
import ru.masterdm.crs.service.entity.AttributeFactory;

/**
 * Role entity.
 * @author Alexey Chalov
 */
public class Role extends Entity {

    private MultilangDescription name;
    private MultilangDescription description;
    private boolean embedded;
    private boolean branch;
    private boolean approved;

    public static final String METADATA_KEY = "ROLE";

    /**
     * Role entity metadata attributes.
     * @author Alexey Chalov
     */
    public enum RoleAttributeMeta implements EmbeddedAttributeMeta {
        /** Name. */
        NAME,
        /** Description. */
        DESCRIPTION,
        /** Embedded role. */
        EMBEDDED,
        /** Is branch role. */
        BRANCH,
        /** Is role approved. */
        APPROVED;

        @Override
        public String getKey() {
            return METADATA_KEY + KEY_DELIMITER + name();
        }
    }

    @Override
    public AbstractAttribute newAttribute(AttributeMeta attributeMeta) {
        if (attributeMeta.getKey().equals(RoleAttributeMeta.NAME.getKey())) {
            return AttributeFactory.newAttribute(
                    attributeMeta, null, createValueAccessor(this::setName, this::getName)
            );
        } else if (attributeMeta.getKey().equals(RoleAttributeMeta.DESCRIPTION.getKey())) {
            return AttributeFactory.newAttribute(
                    attributeMeta, null, createValueAccessor(this::setDescription, this::getDescription)
            );
        } else if (attributeMeta.getKey().equals(RoleAttributeMeta.EMBEDDED.getKey())) {
            return AttributeFactory.newAttribute(
                    attributeMeta, null, createValueAccessor(this::setEmbedded, this::isEmbedded)
            );
        } else if (attributeMeta.getKey().equals(RoleAttributeMeta.BRANCH.getKey())) {
            return AttributeFactory.newAttribute(
                    attributeMeta, null, createValueAccessor(this::setBranch, this::isBranch)
            );
        } else if (attributeMeta.getKey().equals(RoleAttributeMeta.APPROVED.getKey())) {
            return AttributeFactory.newAttribute(
                    attributeMeta, null, createValueAccessor(this::setApproved, this::isApproved)
            );
        } else {
            return super.newAttribute(attributeMeta);
        }
    }

    /**
     * Returns multilang name.
     * @return multilang name
     */

    public MultilangDescription getName() {
        if (name == null) {
            name = new MultilangDescription();
        }
        return name;
    }

    /**
     * Sets multilang name.
     * @param name multilang name
     */
    public void setName(MultilangDescription name) {
        this.name = name;
    }

    /**
     * Returns multilang description.
     * @return multilang description
     */
    public MultilangDescription getDescription() {
        if (description == null) {
            description = new MultilangDescription();
        }
        return description;
    }

    /**
     * Sets multilang description.
     * @param description multilang description
     */
    public void setDescription(MultilangDescription description) {
        this.description = description;
    }

    /**
     * Returns embedded flag.
     * @return embedded flag
     */
    public boolean isEmbedded() {
        return embedded;
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
     * Returns approved.
     * @return approved
     */
    public boolean isApproved() {
        return approved;
    }

    /**
     * Sets approved.
     * @param approved approved
     */
    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    /**
     * Sets embedded flag.
     * @param embedded embedded flag
     */
    public void setEmbedded(boolean embedded) {
        this.embedded = embedded;
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
