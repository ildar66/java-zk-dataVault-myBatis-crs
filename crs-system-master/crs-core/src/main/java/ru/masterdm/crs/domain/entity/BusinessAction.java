package ru.masterdm.crs.domain.entity;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.attribute.MultilangAttribute;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EmbeddedAttributeMeta;
import ru.masterdm.crs.service.entity.AttributeFactory;

/**
 * Business action class.
 * @author Pavel Masalov
 */
public class BusinessAction extends Entity {

    /**
     * Actions allowed in system.
     */
    public enum Action {
        CREATE_NEW,
        VIEW,
        EDIT,
        REMOVE,
        EXECUTE,
        PUBLISH,
        CREATE_COPY,
        USE_AT_CALC
    }

    /**
     * Business action attribute keys.
     */
    public enum BusinessActionAttributeMeta implements EmbeddedAttributeMeta {
        /** Name. */
        NAME;

        @Override
        public String getKey() {
            return METADATA_KEY + KEY_DELIMITER + this.name();
        }
    }

    public static final String METADATA_KEY = "BUSINESS_ACTION";

    private MultilangDescription name;

    // not own property
    private boolean cancelSecextRuleAvailable;

    /**
     * Action enum value of business action.
     * @return action value
     */
    public Action getActionEnum() {
        return Action.valueOf(getKey());
    }

    /**
     * Returns multilang name.
     * @return multilang name
     */

    public MultilangDescription getName() {
        if (name == null) {
            name = ((MultilangAttribute) getAttribute(BusinessActionAttributeMeta.NAME.getKey())).getMultilangDescription();
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

    @Override
    public AbstractAttribute newAttribute(AttributeMeta attributeMeta) {
        if (attributeMeta.getKey().equals(BusinessActionAttributeMeta.NAME.getKey())) {
            return AttributeFactory.newAttribute(
                    attributeMeta, null, createValueAccessor((MultilangDescription v) -> setName(v), () -> getName())
            );
        } else {
            return super.newAttribute(attributeMeta);
        }
    }

    /**
     * Returns availability of additional secure rule cancellation flag.
     * @return availability flag
     */
    public boolean isCancelSecextRuleAvailable() {
        return cancelSecextRuleAvailable;
    }

    /**
     * Sets availability additional secure rule cancellation flag.
     * @param cancelSecextRuleAvailable availability flag
     */
    public void setCancelSecextRuleAvailable(boolean cancelSecextRuleAvailable) {
        this.cancelSecextRuleAvailable = cancelSecextRuleAvailable;
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