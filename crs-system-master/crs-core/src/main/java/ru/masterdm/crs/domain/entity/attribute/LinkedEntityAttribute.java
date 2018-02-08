package ru.masterdm.crs.domain.entity.attribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import ru.masterdm.crs.domain.entity.AbstractDvEntity;
import ru.masterdm.crs.domain.entity.MetadataSupport;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.util.SynchronizedList;

/**
 * Container for linked entity attribute instances.
 * @param <T> types stored at link reference
 * @author Pavel Masalov
 */
public class LinkedEntityAttribute<T extends AbstractDvEntity> extends AbstractAttribute<List<EntityAttribute<T>>> {

    private static final long serialVersionUID = 1L;

    private transient SynchronizedList<EntityAttribute<T>, T> sync;

    /**
     * Override all list adds methods to set attribute metadata.
     */
    private class EntityAttributeList extends ArrayList<EntityAttribute> {

        /**
         * Added entity checker.
         * @param entityAttribute added entity attribute
         */
        private void checkMeta(EntityAttribute entityAttribute) {
            if (entityAttribute.getMeta() == null)
                entityAttribute.setMeta(getMeta());
            entityAttribute.setLinkedEntityAttribute(LinkedEntityAttribute.this);
        }

        @Override
        public EntityAttribute set(int index, EntityAttribute element) {
            checkMeta(element);
            return super.set(index, element);
        }

        @Override
        public boolean add(EntityAttribute entityAttribute) {
            checkMeta(entityAttribute);
            return super.add(entityAttribute);
        }

        @Override
        public void add(int index, EntityAttribute element) {
            checkMeta(element);
            super.add(index, element);
        }

        @Override
        public boolean addAll(Collection<? extends EntityAttribute> c) {
            for (EntityAttribute ea : c)
                checkMeta(ea);
            return super.addAll(c);
        }

        @Override
        public boolean addAll(int index, Collection<? extends EntityAttribute> c) {
            for (EntityAttribute ea : c)
                checkMeta(ea);
            return super.addAll(index, c);
        }
    }

    /**
     * Construct attribute with known meta and value (may be non empty).
     * Values are coped into internal array.
     * @param meta attribute meta
     * @param entities attribute value
     */
    public LinkedEntityAttribute(AttributeMeta meta, List<T> entities) {
        this(meta);
        sync.getListT2().addAll(entities);
    }

    /**
     * Construct attribute with known meta.
     * @param meta attribute meta
     */
    public LinkedEntityAttribute(AttributeMeta meta) {
        super(meta);
        sync = new SynchronizedList(new EntityAttributeList(), new ArrayList<AbstractDvEntity>(), // T1 - EntityAttribute, T2 - AbstractDvEntity
                                    (Function<EntityAttribute, AbstractDvEntity>) (EntityAttribute ea) -> ea.getEntity(),
                                    (Function<AbstractDvEntity, EntityAttribute>) (AbstractDvEntity e) -> new EntityAttribute(getMeta(), e),
                                    (Function<EntityAttribute, AbstractDvEntity>) (EntityAttribute ea) -> ea.getEntity(),
                                    (Function<AbstractDvEntity, EntityAttribute>) (AbstractDvEntity e) -> getAttributeByEntity(e));
    }

    /**
     * Append link to entity to attribute.
     * @param attribute appended link to entity
     */
    public void addAttribute(EntityAttribute attribute) {
        if (attribute.getLinkedEntityAttribute() != null) {
            throw new CrsException("Entity attribute " + attribute + " already added to link attribute " + attribute.getLinkedEntityAttribute());
        }
        sync.getListT1().add(attribute);
    }

    /**
     * Remove link to entity.
     * @param attribute removed link to entity
     */
    public void removeAttribute(EntityAttribute attribute) {
        if (sync.getListT1().remove(attribute))
            attribute.setLinkedEntityAttribute(null);
    }

    @Override
    public List<EntityAttribute<T>> getValue() {
        return getEntityAttributeList();
    }

    @Override
    public void setValue(List<EntityAttribute<T>> value) {
        throw new UnsupportedOperationException("Can't set value to linked entity list directly");
        // for  java.lang.ClassCastException: ... cannot be cast to ...
        //entityAttributeList = value;
    }

    /**
     * Check if entity are referenced by this reference.
     * @param entity entity object
     * @return true if entity are referenced
     */
    public boolean isExists(AbstractDvEntity entity) {
        if (entity instanceof MetadataSupport && !getMeta().getEntityKey().equals(((MetadataSupport) entity).getMeta().getKey()))
            return false;

        return sync.getListT2().contains(entity);
    }

    /**
     * Remove entity from link.
     * @param entity entity object
     * @return true if reference removed
     */
    public boolean remove(AbstractDvEntity entity) {
        if (entity instanceof MetadataSupport && !getMeta().getEntityKey().equals(((MetadataSupport) entity).getMeta().getKey()))
            return false;

        return sync.getListT2().remove(entity);
    }

    /**
     * Add entity to reference attribute.
     * @param entity entity object
     */
    public void add(T entity) {
        if (entity instanceof MetadataSupport && !getMeta().getEntityKey().equals(((MetadataSupport) entity).getMeta().getKey()))
            throw new IllegalArgumentException("Entity meta should be " + getMeta().getEntityKey());

        sync.getListT2().add(entity);
    }

    /**
     * Add entity to list.
     * Called only from {@link EntityAttribute#setEntity(AbstractDvEntity)} of attribute already at link.
     * @param entity entity object
     */
    void addEntityNotSync(T entity) {
        sync.getListT2InnerList().add(entity);
    }

    /**
     * Remove entity from list.
     * Called only from {@link EntityAttribute#setEntity(AbstractDvEntity)} of attribute already at link.
     * @param entity entity to remove
     * @return true if entity removed
     */
    boolean removeEntityNotSync(T entity) {
        return sync.getListT2InnerList().remove(entity);
    }

    /**
     * Returns entity list.
     * @return entity list
     */
    public List<T> getEntityList() {
        return sync.getListT2();
    }

    /**
     * Returns entity attribute list.
     * @return entity attribute list
     */
    public List<EntityAttribute<T>> getEntityAttributeList() {
        return sync.getListT1();
    }

    /**
     * Find {@link EntityAttribute} by entity.
     * @param entity entity object
     * @return attribute, null - if attribute not found
     */
    public EntityAttribute getAttributeByEntity(AbstractDvEntity entity) {
        for (EntityAttribute ea : getEntityAttributeList()) {
            if (ea.getEntity().equals(entity))
                return ea;
        }
        return null;
    }

    /**
     * Get size of link.
     * @return size of link
     */
    public int size() {
        return getEntityAttributeList().size();
    }

    /**
     * Detect if link is empty.
     * @return true is link contains no references
     */
    public boolean isEmpty() {
        return getEntityAttributeList().isEmpty();
    }
}
