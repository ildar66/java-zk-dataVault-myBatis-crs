package ru.masterdm.crs.domain.entity.meta;

import java.util.ArrayList;
import java.util.List;

import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.entity.AbstractDvEntity;
import ru.masterdm.crs.util.CollectorUtils;

/**
 * Entity attribute description.
 * @author Sergey Valiev
 */
public class AttributeMeta extends AbstractDvEntity {

    /** Key delimiter. */
    public static final String KEY_DELIMITER = "#";

    private MultilangDescription name;
    private Long viewOrder = 0L;
    private String linkTable;
    private String entityKey;
    private String attributeKey;
    private AttributeType type;
    private boolean nullable;
    private boolean multilang;
    private boolean link;
    private boolean filterAvailable;
    private String nativeColumn;
    private String defaultValue;

    private List<AttributeMeta> attributeAttributes;

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
     * Returns entity key.
     * @return entity key
     */
    public String getEntityKey() {
        return entityKey;
    }

    /**
     * Sets entity key.
     * @param entityKey entity key
     */
    public void setEntityKey(String entityKey) {
        this.entityKey = entityKey;
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
     * Returns type.
     * @return type
     */
    public AttributeType getType() {
        return type;
    }

    /**
     * Sets type.
     * @param type type
     */
    public void setType(AttributeType type) {
        this.type = type;
    }

    /**
     * Returns nullable flag.
     * @return <code><b>true</b></code> if column can contain null value
     */
    public boolean isNullable() {
        return nullable;
    }

    /**
     * Sets nullable flag.
     * @param nullable nullable flag
     */
    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    /**
     * Returns multilang flag.
     * @return <code><b>true</b></code> if column contains multilang value
     */
    public boolean isMultilang() {
        return multilang;
    }

    /**
     * Sets multilang flag.
     * @param multilang multilang flag
     */
    public void setMultilang(boolean multilang) {
        this.multilang = multilang;
    }

    /**
     * Returns link flag.
     * @return <code><b>true</b></code> if the column is a reference
     */
    public boolean isLink() {
        return link;
    }

    /**
     * Sets link flag.
     * @param link link flag
     */
    public void setLink(boolean link) {
        this.link = link;
    }

    /**
     * Returns real column name.
     * @return real column name
     */
    public String getNativeColumn() {
        return nativeColumn;
    }

    /**
     * Sets real column name.
     * @param nativeColumn real column name
     */
    public void setNativeColumn(String nativeColumn) {
        this.nativeColumn = nativeColumn;
    }

    /**
     * Returns filter available flag.
     * @return <code><b>true</b></code> if column filtering is available
     */
    public boolean isFilterAvailable() {
        return filterAvailable;
    }

    /**
     * Sets filter available flag.
     * @param filterAvailable filter available flag
     */
    public void setFilterAvailable(boolean filterAvailable) {
        this.filterAvailable = filterAvailable;
    }

    /**
     * Returns default value.
     * @return default value
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets default value.
     * @param defaultValue defaultValue
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Attribute data stored in satellite table column.
     * @return true data stored at satellite column
     */
    public boolean isInTable() {
        return !(getType() == AttributeType.FILE
                 || getType() == AttributeType.REFERENCE
                 || (getType() == AttributeType.STRING && isMultilang())
                 || (getType() == AttributeType.TEXT && isMultilang()));
    }

    /**
     * Returns list of attribute's attributes.
     * @return list of attribute's attributes
     */
    public List<AttributeMeta> getAttributeAttributes() {
        if (attributeAttributes == null)
            attributeAttributes = new ArrayList<>();
        return attributeAttributes;
    }

    /**
     * Sets attribute' attributes.
     * @param attributeAttributes list of attribute' attributes
     */
    public void setAttributeAttributes(List<AttributeMeta> attributeAttributes) {
        this.attributeAttributes = attributeAttributes;
    }

    /**
     * Detect if attribute has attributes.
     * @return true if it has attributes
     */
    public boolean isHasAttributeAttributes() {
        return attributeAttributes != null && !attributeAttributes.isEmpty();
    }

    /**
     * Get attribute metadata for key.
     * @param key attribute key
     * @return attribute metadata, null if attribute not in entity
     */
    public AttributeMeta getAttributeMetadata(String key) {
        if (isHasAttributeAttributes())
            return attributeAttributes.stream()
                                      .filter((attribute) -> attribute.getKey().equals(key))
                                      .collect(CollectorUtils.singletonCollector());
        return null;
    }
}
