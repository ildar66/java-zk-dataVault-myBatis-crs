package ru.masterdm.crs.domain.entity.meta;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

import java.math.BigDecimal;
import java.util.List;

import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.service.entity.AttributeFactory;

/**
 * Entity group system entity.
 * @author Pavel Masalov
 */
public class EntityMetaGroup extends Entity {

    /**
     * Entity meta model's meta attributes.
     * @author Alexey Kirilchev
     */
    public enum EntityMetaGroupAttributeMeta implements EmbeddedAttributeMeta {
        /** Russian name. */
        NAME_RU,
        /** English name. */
        NAME_EN,
        /** View order. */
        VIEW_ORDER,
        /** Reference to entities. */
        ENTITY,
        /** Entity type. */
        ENTITY_TYPE;

        @Override
        public String getKey() {
            return METADATA_KEY + KEY_DELIMITER + this.name();
        }
    }

    /**
     * Default entity group.
     */
    public enum DefaultGroup {
        /** Default group for dictionary. */
        DEFAULT_DICTIONARY_GROUP(EntityType.DICTIONARY),
        /** Default group for input form. */
        DEFAULT_INPUT_FORM_GROUP(EntityType.INPUT_FORM),
        /** Default group for classifier. */
        DEFAULT_CLASSIFIER_GROUP(EntityType.CLASSIFIER);

        private EntityType entityType;

        /**
         * Construct enum with entity type associated.
         * @param entityType entity type
         */
        DefaultGroup(EntityType entityType) {
            this.entityType = entityType;
        }

        /**
         * Grouo's entity type.
         * @return entity type
         */
        public EntityType getEntityType() {
            return entityType;
        }

        /**
         * Check if key is in default groups.
         * @param key group key
         * @return true if key is default
         */
        public static boolean isHasValue(String key) {
            for (DefaultGroup v : values()) {
                if (v.name().equals(key))
                    return true;
            }
            return false;
        }

        /**
         * Get group enum for entity type.
         * @param entityType entity type
         * @return group enum
         */
        public static DefaultGroup getDefaultGroupKey(EntityType entityType) {
            for (DefaultGroup v : values()) {
                if (v.getEntityType().equals(entityType))
                    return v;
            }
            return null;
        }

        /**
         * Get group enum for entity type.
         * @param entityType entity type key
         * @return group enum
         */
        public static DefaultGroup getDefaultGroupKey(String entityType) {
            for (DefaultGroup v : values()) {
                if (v.getEntityType().name().equals(entityType))
                    return v;
            }
            return null;
        }
    }

    /**
     * Entity group metadata key.
     */
    public static final String METADATA_KEY = "ENTITY_GROUP";

    private EntityType type;
    private List<EntityMeta> elements;
    private MultilangDescription name;
    private Long viewOrder = 0L;

    @Override
    public String calcDigest() {
        return calcDigest(type, viewOrder, name != null ? name.getDescriptionRu() : null, name != null ? name.getDescriptionEn() : null);
    }

    /**
     * Returns entity type which types can be entered into group.
     * @return entity type
     */
    public EntityType getType() {
        return type;
    }

    /**
     * Sets entity type which types can be entered into group.
     * @param type entity type
     */
    public void setType(EntityType type) {
        this.type = type;
    }

    /**
     * Returns entity group member entity metadatas.
     * @return entity metadatas
     */
    public List<EntityMeta> getElements() {
        if (elements == null)
            elements = ((LinkedEntityAttribute) getAttribute(EntityMetaGroupAttributeMeta.ENTITY.getKey())).getEntityList();
        return elements;
    }

    /**
     * Sets entity group member entity metadatas.
     * @param elements entity metadatas
     */
    public void setElements(List<EntityMeta> elements) {
        getElements().clear();
        getElements().addAll(elements);
    }

    /**
     * Returns group name.
     * @return name
     */
    public MultilangDescription getName() {
        if (name == null)
            name = new MultilangDescription();
        return name;
    }

    /**
     * Sets group name.
     * @param name name
     */
    public void setName(MultilangDescription name) {
        MultilangDescription n = this.getName();
        n.setDescriptionEn(name.getDescriptionEn());
        n.setDescriptionRu(name.getDescriptionRu());
    }

    /**
     * Returns group view order.
     * @return group view order
     */
    public Long getViewOrder() {
        return viewOrder;
    }

    /**
     * Sets group view order.
     * @param viewOrder group view order
     */
    public void setViewOrder(Long viewOrder) {
        this.viewOrder = viewOrder;
    }

    @Override
    public AbstractAttribute newAttribute(AttributeMeta attributeMeta) {
        // TODO create annotation based attribute value accessors creation
        if (attributeMeta.getKey().equals(EntityMetaGroupAttributeMeta.NAME_EN.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor((String v) -> getName().setDescriptionEn(v),
                                                                     () -> getName().getDescriptionEn()));
        } else if (attributeMeta.getKey().equals(EntityMetaGroupAttributeMeta.NAME_RU.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor((String v) -> getName().setDescriptionRu(v),
                                                                     () -> getName().getDescriptionRu()));
        } else if (attributeMeta.getKey().equals(EntityMetaGroupAttributeMeta.VIEW_ORDER.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor((BigDecimal v) -> setViewOrder(v == null ? null : v.longValue()),
                                                                     () -> getViewOrder() == null ? null : new BigDecimal(getViewOrder())));
        } else if (attributeMeta.getKey().equals(EntityMetaGroupAttributeMeta.ENTITY.getKey())) {
            return new LinkedEntityAttribute<>(attributeMeta);
        } else if (attributeMeta.getKey().equals(EntityMetaGroupAttributeMeta.ENTITY_TYPE.getKey())) {
            return new LinkedEntityAttribute<>(attributeMeta);

        } else {
            return super.newAttribute(attributeMeta);
        }
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
