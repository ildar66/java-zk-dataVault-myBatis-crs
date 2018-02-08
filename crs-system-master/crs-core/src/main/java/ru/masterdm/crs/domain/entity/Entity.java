package ru.masterdm.crs.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.attribute.NumberAttribute;
import ru.masterdm.crs.domain.entity.attribute.StringAttribute;
import ru.masterdm.crs.domain.entity.attribute.value.ValueAccessor;
import ru.masterdm.crs.domain.entity.attribute.value.ValueAccessorImpl;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.CommonAttribute;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.service.entity.AttributeFactory;

/**
 * Data Vault business entity.
 * @author Sergey Valiev
 */
public class Entity extends AbstractDvEntity implements DigestSupport, MetadataSupport, AttributeSupport {

    private EntityMeta meta;
    private Map<String, AbstractAttribute> attributes = new HashMap<>();
    private String digest;
    private LinkedEntityAttribute<Entity> childReferenceAttribute;
    private LinkedEntityAttribute<Entity> parentReferenceAttribute;
    private NumberAttribute hubIdAttribute;
    private StringAttribute keyAttribute;

    @Override
    public String getDigest() {
        return digest;
    }

    @Override
    public void setDigest(String digest) {
        this.digest = digest;
    }

    @Override
    public String calcDigest() {
        return calcDigest(meta.getInTableAttribute()
                              .stream()
                              .map((am) -> getAttributeValue(am.getKey()))
                              .collect(Collectors.toList()).toArray());
    }

    /**
     * Get children reference attribute.
     * @return reference attribute
     */
    public LinkedEntityAttribute<Entity> getChildrenReferenceAttribute() {
        if (childReferenceAttribute == null)
            childReferenceAttribute = new LinkedEntityAttribute<>(getMeta().getChildrenReferenceAttribute());
        return childReferenceAttribute;
    }

    /**
     * Get link system attribute for reference to parent.
     * This attribute for read only. Changes at this attribute will not be saved.
     * Use parent's {@link #getChildrenReferenceAttribute child attribute} to persists parent-child relations.
     * @return reference to parent attribute
     */
    public LinkedEntityAttribute<Entity> getParentReferenceAttribute() {
        if (parentReferenceAttribute == null)
            parentReferenceAttribute = new LinkedEntityAttribute<>(getMeta().getParentReferenceAttribute());
        return parentReferenceAttribute;
    }

    /**
     * Get system hub attribute.
     * @return nub attribute
     */
    public NumberAttribute getHubIdAttribute() {
        if (hubIdAttribute == null)
            hubIdAttribute = (NumberAttribute) AttributeFactory
                    .newAttribute(getMeta().getHubIdAttribute(), null,
                                  createValueAccessor((v) -> setHubId(v.longValue()), () -> new BigDecimal(getHubId())));
        return hubIdAttribute;
    }

    /**
     * Get key system attribute.
     * @return key attribute
     */
    public StringAttribute getKeyAttribute() {
        if (keyAttribute == null)
            keyAttribute = (StringAttribute) AttributeFactory
                    .newAttribute(getMeta().getKeyAttribute(), null, createValueAccessor((v) -> setKey(v), () -> getKey()));
        return keyAttribute;
    }

    /**
     * Detect if entity has children.
     * Children entity should be loaded by {@link ru.masterdm.crs.service.EntityService#loadEntityChildren(Entity, LocalDateTime)}
     * or by  {@link ru.masterdm.crs.service.EntityService#loadEntityChildren(List, LocalDateTime)}.
     * @return true if entity has children
     */
    public boolean isChildrenExists() {
        return childReferenceAttribute != null && !childReferenceAttribute.isEmpty();
    }

    /**
     * Detect if entity has parent.
     * Parent entity should be loaded by {@link ru.masterdm.crs.service.EntityService#loadEntityParent(Entity, LocalDateTime)}
     * or by  {@link ru.masterdm.crs.service.EntityService#loadEntityParent(List, LocalDateTime)}.
     * @return true if entity has parent
     */
    public boolean isParentExists() {
        return parentReferenceAttribute != null && !parentReferenceAttribute.isEmpty();
    }

    @Override
    public AbstractAttribute getAttribute(String attributeKey) {
        if (attributeKey.equals(CommonAttribute.CHILDREN.name()))
            return getChildrenReferenceAttribute();
        if (attributeKey.equals(CommonAttribute.PARENT.name()))
            return getParentReferenceAttribute();
        if (attributeKey.equals(CommonAttribute.H_ID.name()))
            return getHubIdAttribute();
        if (attributeKey.equals(CommonAttribute.KEY.name()))
            return getKeyAttribute();

        return AttributeSupport.super.getAttribute(attributeKey);
    }

    @Override
    public boolean isAttributeExists(String attributeKey) {
        if (attributeKey.equals(CommonAttribute.CHILDREN.name()))
            return isChildrenExists();
        if (attributeKey.equals(CommonAttribute.PARENT.name()))
            return isParentExists();
        return AttributeSupport.super.isAttributeExists(attributeKey);
    }

    /**
     * Returns entity attributes value objects.
     * @return attribute object
     */
    @Override
    public Map<String, AbstractAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public void setAttributes(Map<String, AbstractAttribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public AttributeMeta getAttributeMetadata(String attributeKey) {
        return meta.getAttributeMetadata(attributeKey);
    }

    /**
     * Returns entity metadata.
     * @return entity metadata
     */
    @Override
    public EntityMeta getMeta() {
        return meta;
    }

    /**
     * Sets entity metadata.
     * @param meta entity metadata
     */
    @Override
    public void setMeta(EntityMeta meta) {
        this.meta = meta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null)
            return false;

        if (o instanceof Entity && meta != null) {
            return meta.equals(((Entity) o).getMeta()) && super.equals(o);
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(super.hashCode())
                .append(meta.hashCode())
                .toHashCode();
    }

    /**
     * Helper method to capture accessor type.
     * Used only at overriders of {@link AbstractDvEntity}.newAttribute({@link AttributeMeta}) at subclasses.
     * @param setter value setter
     * @param getter value getter
     * @param <T> type of value
     * @return value
     */
    protected <T> ValueAccessor<T> createValueAccessor(Consumer<T> setter, Supplier<T> getter) {
        return new ValueAccessorImpl<>(setter, getter);
    }
}
