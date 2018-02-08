package ru.masterdm.crs.domain.entity.meta;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.entity.AbstractDvEntity;
import ru.masterdm.crs.domain.entity.BusinessAction;
import ru.masterdm.crs.util.CollectorUtils;

/**
 * Entity metadata description.
 * @author Sergey Valiev
 */
public class EntityMeta extends AbstractDvEntity {

    /**
     * Entity meta model's meta attributes.
     * @author Alexey Kirilchev
     */
    public enum EntityMetaAttributeMeta implements EmbeddedAttributeMeta {
        /** Form. */
        FORM,
        /** Russian name. */
        NAME_RU,
        /** English name. */
        NAME_EN,
        /** Link table. */
        LINK_TABLE,
        /** Attribute key. */
        ATTRIBUTE_KEY,
        /** View order. */
        VIEW_ORDER,
        /** Hierarchical flag. */
        HIERARCHICAL,
        /** Allowed business actions. */
        BUSINESS_ACTION;

        @Override
        public String getKey() {
            return METADATA_KEY + KEY_DELIMITER + this.name();
        }
    }

    /**
     * Entity meta model metadata key.
     */
    public static final String METADATA_KEY = "ENTITY";

    private MultilangDescription name;
    private MultilangDescription comment;
    private String form;
    private Long viewOrder = 0L;
    private List<EntityType> types;
    private List<AttributeMeta> attributes;
    private String linkTable;
    private String attributeKey;
    private boolean hierarchical;
    private MultilangDescription keyName;

    // system attributes
    private AttributeMeta keyAttribute;
    private AttributeMeta hubIdAttribute;
    private AttributeMeta childrenReferenceAttribute;
    private AttributeMeta parentReferenceAttribute;
    private AttributeMeta hubLdtsAttribute;

    /**
     * Create key attribute for this entity meta.
     * @return key attribute metadata
     */
    private AttributeMeta createKeyAttribute() {
        AttributeMeta keyAttribute = new AttributeMeta();
        keyAttribute.setKey(CommonAttribute.KEY.name());
        keyAttribute.setType(AttributeType.STRING);
        keyAttribute.setNullable(false);
        keyAttribute.setMultilang(false);
        keyAttribute.setLink(false);
        keyAttribute.setFilterAvailable(true);
        keyAttribute.setNativeColumn(CommonColumn.KEY.name());
        keyAttribute.setName(keyName != null ? keyName : new MultilangDescription(CommonAttribute.KEY.name(), CommonAttribute.KEY.name()));
        return keyAttribute;
    }

    /**
     * Create hub id attribute for this entity meta.
     * @return hub id attribute metadata
     */
    private AttributeMeta createHubIdAttribute() {
        AttributeMeta keyAttribute = new AttributeMeta();
        keyAttribute.setKey(CommonAttribute.H_ID.name());
        keyAttribute.setType(AttributeType.NUMBER);
        keyAttribute.setNullable(false);
        keyAttribute.setMultilang(false);
        keyAttribute.setLink(false);
        keyAttribute.setFilterAvailable(true);
        keyAttribute.setNativeColumn(CommonColumn.H_ID.name());
        keyAttribute.setName(new MultilangDescription(CommonAttribute.H_ID.name(), CommonAttribute.H_ID.name()));
        return keyAttribute;
    }

    /**
     * Create hub ldts attribute meta.
     * @return hub ldts attribute meta
     */
    private AttributeMeta createHubLdtsAttribute() {
        AttributeMeta keyAttribute = new AttributeMeta();
        keyAttribute.setKey(CommonAttribute.HUB_LDTS.name());
        keyAttribute.setType(AttributeType.DATETIME);
        keyAttribute.setNullable(false);
        keyAttribute.setMultilang(false);
        keyAttribute.setLink(false);
        keyAttribute.setFilterAvailable(true);
        keyAttribute.setNativeColumn(CommonColumn.HUB_LDTS.name());
        keyAttribute.setName(new MultilangDescription(CommonAttribute.HUB_LDTS.name(), CommonAttribute.HUB_LDTS.name()));
        return keyAttribute;
    }

    /**
     * Create self reference attribute meta by one of the type.
     * @param commonAttribute type of reference {@link CommonAttribute#CHILDREN} or {@link CommonAttribute#PARENT}
     * @return new attribute meta
     */
    private AttributeMeta createSelfReferenceAttribute(CommonAttribute commonAttribute) {
        AttributeMeta childrenAttribute = new AttributeMeta();
        childrenAttribute.setKey(commonAttribute.name());
        childrenAttribute.setType(AttributeType.REFERENCE);
        childrenAttribute.setNullable(false);
        childrenAttribute.setMultilang(false);
        childrenAttribute.setLink(true);
        childrenAttribute.setFilterAvailable(false); // ???
        childrenAttribute.setLinkTable(getLinkTable());
        childrenAttribute.setEntityKey(getKey());
        childrenAttribute.setAttributeKey(getAttributeKey());
        childrenAttribute.setName(new MultilangDescription(commonAttribute.name(), commonAttribute.name()));
        return childrenAttribute;
    }

    /**
     * Returns multilang name.
     * @return multilang name
     */
    public MultilangDescription getName() {
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
     * Returns multilang comment.
     * @return multilang comment
     */
    public MultilangDescription getComment() {
        return comment;
    }

    /**
     * Sets multilang comment.
     * @param comment multilang comment
     */
    public void setComment(MultilangDescription comment) {
        this.comment = comment;
    }

    /**
     * Returns key name multilang.
     * @return key name multilang
     */
    public MultilangDescription getKeyName() {
        return keyName;
    }

    /**
     * Sets key name multilang.
     * @param keyName key name multilang
     */
    public void setKeyName(MultilangDescription keyName) {
        this.keyName = keyName;
    }

    /**
     * Returns KEY default attribute for entity.
     * @return KEY default attribute for entity
     */
    public AttributeMeta getKeyAttribute() {
        if (keyAttribute == null)
            keyAttribute = createKeyAttribute();
        return keyAttribute;
    }

    /**
     * Return HUB_ID default attribute for entity.
     * @return HUB_ID default attribute for entity
     */
    public AttributeMeta getHubIdAttribute() {
        if (hubIdAttribute == null)
            hubIdAttribute = createHubIdAttribute();
        return hubIdAttribute;
    }

    /**
     * Return HUB_LDTS default attribute for entity.
     * @return HUB_LDTS default attribute for entity
     */
    public AttributeMeta getHubLdtsAttribute() {
        if (hubLdtsAttribute == null)
            hubLdtsAttribute = createHubLdtsAttribute();
        return hubLdtsAttribute;
    }

    /**
     * Get children self reference attribute meta.
     * @return attribute meta
     */
    public AttributeMeta getChildrenReferenceAttribute() {
        if (childrenReferenceAttribute == null)
            childrenReferenceAttribute = createSelfReferenceAttribute(CommonAttribute.CHILDREN);
        return childrenReferenceAttribute;
    }

    /**
     * Get parent self reference attribute meta.
     * @return attribute meta
     */
    public AttributeMeta getParentReferenceAttribute() {
        if (parentReferenceAttribute == null)
            parentReferenceAttribute = createSelfReferenceAttribute(CommonAttribute.PARENT);
        return parentReferenceAttribute;
    }

    /**
     * Returns UI form.
     * @return UI form
     */
    public String getForm() {
        return form;
    }

    /**
     * Sets UI form.
     * @param form UI form
     */
    public void setForm(String form) {
        this.form = form;
    }

    /**
     * Returns view order.
     * @return view order
     */
    public Long getViewOrder() {
        return viewOrder;
    }

    /**
     * Sets view order.
     * @param viewOrder view order
     */
    public void setViewOrder(Long viewOrder) {
        this.viewOrder = viewOrder;
    }

    /**
     * Returns types.
     * @return types
     */
    public List<EntityType> getTypes() {
        return types;
    }

    /**
     * Returns first type.
     * @return first type
     */
    public EntityType getType() {
        return types != null && !types.isEmpty() ? types.get(0) : null;
    }

    /**
     * Sets types.
     * @param types types
     */
    public void setTypes(List<EntityType> types) {
        this.types = types;
    }

    /**
     * Returns setup attributes description.
     * @return attributes description
     */
    public List<AttributeMeta> getAttributes() {
        if (attributes == null)
            attributes = new ArrayList<>();
        return attributes;
    }

    /**
     * Sets setup attributes description.
     * @param attributes attributes description
     */
    public void setAttributes(List<AttributeMeta> attributes) {
        this.attributes = attributes;
    }

    /**
     * Returns link table.
     * @return link table
     */
    public String getLinkTable() {
        return linkTable;
    }

    /**
     * Sets link table.
     * @param linkTable link table
     */
    public void setLinkTable(String linkTable) {
        this.linkTable = linkTable;
    }

    /**
     * Returns attribute key.
     * @return attribute key
     */
    public String getAttributeKey() {
        return attributeKey;
    }

    /**
     * Sets attribute key.
     * @param attributeKey attribute key
     */
    public void setAttributeKey(String attributeKey) {
        this.attributeKey = attributeKey;
    }

    /**
     * Returns hierarchical flag.
     * @return <code>true</code> if hierarchical entity
     */
    public boolean isHierarchical() {
        return hierarchical;
    }

    /**
     * Sets hierarchical flag.
     * @param hierarchical hierarchical flag
     */
    public void setHierarchical(boolean hierarchical) {
        this.hierarchical = hierarchical;
    }

    /**
     * Get list of attributes stored at entity satellite table ("intable" attributes).
     * @return list of attributes
     */
    public List<AttributeMeta> getInTableAttribute() {
        return getAttributes().stream()  // not FILE, REFERENCE, STRING ML, TEXT ML
                              .filter(AttributeMeta::isInTable)
                              .sorted(Comparator.comparing(AttributeMeta::getKey))
                              .collect(Collectors.toList());
    }

    /**
     * Get attribute metadata for key.
     * @param key attribute key
     * @return attribute metadata, null if attribute not in entity
     */
    public AttributeMeta getAttributeMetadata(String key) {
        return getAttributes().stream()
                              .filter((attribute) -> attribute.getKey().equals(key))
                              .sorted(Comparator.comparing(AttributeMeta::getKey))
                              .collect(CollectorUtils.singletonCollector());
    }

    /**
     * Return is system object.
     * @return is system object
     */
    public boolean isSystemObject() {
        return METADATA_KEY.equals(getKey())
               || EntityMetaGroup.METADATA_KEY.equals(getKey())
               || EntityTypeAttributeMeta.METADATA_KEY.equals(getKey())
               || BusinessAction.METADATA_KEY.equals(getKey());
    }
}
