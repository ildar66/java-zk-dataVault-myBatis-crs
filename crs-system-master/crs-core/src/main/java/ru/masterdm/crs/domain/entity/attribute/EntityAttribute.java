package ru.masterdm.crs.domain.entity.attribute;

import static ru.masterdm.crs.domain.entity.meta.CommonColumn.DIGEST;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.ID;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.LDTS;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.REMOVED;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import ru.masterdm.crs.domain.entity.AbstractDvEntity;
import ru.masterdm.crs.domain.entity.AttributeSupport;
import ru.masterdm.crs.domain.entity.DigestSupport;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.exception.CrsException;

/**
 * Attribute to store reference to linked entity.
 * @param <T> type of entity
 * @author Pavel Masalov
 */
public class EntityAttribute<T extends AbstractDvEntity> extends LinkAttribute<Object> implements Cloneable {

    private static final long serialVersionUID = 1L;

    private T entity;
    private LinkedEntityAttribute linkedEntityAttribute;
    private transient Satellite satellite;

    /**
     * Internal class to represent satellite object.
     */
    public class Satellite implements DigestSupport, AttributeSupport {

        private Long id;
        private LocalDateTime ldts;
        private boolean removed;
        private String digest;
        private Map<String, AbstractAttribute> attributes = new HashMap<>();

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
            return getMeta().getAttributeMetadata(attributeKey);
        }

        /**
         * Set system property into object from mybatis result map.
         * @param data result map
         */
        public void setSystemPropertyByMap(Map<String, Object> data) {
            setId((Long) data.get(ID.name()));
            setLdts((LocalDateTime) data.get(LDTS.name()));
            setRemoved((Boolean) data.get(REMOVED.name()));
            setDigest((String) data.get(DIGEST.name()));
        }

        @Override
        public String getDigest() {
            return digest;
        }

        @Override
        public void setDigest(String digest) {
            this.digest = digest;
        }

        /**
         * Returns satellite primary id.
         * @return satellite primary id
         */
        public Long getId() {
            return id;
        }

        /**
         * Sets satellite primary id.
         * @param id satellite primary id
         */
        public void setId(Long id) {
            this.id = id;
        }

        /**
         * Returns satellite load datetime.
         * @return satellite load datetime
         */
        public LocalDateTime getLdts() {
            return ldts;
        }

        /**
         * Sets satellite load datetime.
         * @param ldts satellite load datetime
         */
        public void setLdts(LocalDateTime ldts) {
            this.ldts = ldts;
        }

        /**
         * Returns satellite remove flag.
         * @return satellite remove flag
         */
        public boolean isRemoved() {
            return removed;
        }

        /**
         * Sets satellite remove flag.
         * @param removed satellite remove flag
         */
        public void setRemoved(boolean removed) {
            this.removed = removed;
        }

        @Override
        public String calcDigest() {
            return calcDigest(attributes.values()
                                        .stream()
                                        .sorted(Comparator.comparing((a) -> a.getMeta().getKey()))
                                        .map((a) -> a.getValue())
                                        .collect(Collectors.toList()).toArray());
        }
    }

    /**
     * Default constructor.
     */
    public EntityAttribute() {
        super();
    }

    /**
     * Construct attribute with entity.
     * @param attributeMeta link attribute metadata
     * @param entity entity object
     */
    public EntityAttribute(AttributeMeta attributeMeta, T entity) {
        super(attributeMeta);
        this.entity = entity;
        setLinkedHubId(entity.getHubId());
    }

    @Override
    public Object getValue() {
        if (entity == null || !(entity instanceof Entity))
            return null;
        return ((Entity) entity).getAttribute(getMeta().getAttributeKey()).getValue();
    }

    @Override
    public void setValue(Object value) {
        if (entity != null && entity instanceof Entity)
            ((Entity) entity).getAttribute(getMeta().getAttributeKey()).setValue(value);
    }

    /**
     * Returns linked entity instance.
     * @return linked entity instance
     */
    public T getEntity() {
        return entity;
    }

    /**
     * Sets linked entity instance.
     * @param newEntity linked entity instance
     */
    public void setEntity(T newEntity) {
        if (entity != null && entity.getHubId() == null)
            throw new CrsException("Cant add not persisted entity with key=" + entity.getKey() + " to REFERENCE attribute " + getMeta().getKey());

        AbstractDvEntity oldEntity = this.entity;
        this.entity = newEntity;

        if (this.entity != null)
            setLinkedHubId(this.entity.getHubId());
        else
            setLinkedHubId(null);

        if (linkedEntityAttribute != null) {
            if (oldEntity != null)
                linkedEntityAttribute.removeEntityNotSync(oldEntity);
            if (entity != null)
                linkedEntityAttribute.addEntityNotSync(entity);
        }
    }

    /**
     * Returns linked attribute that owns the entiry.
     * @return linked attribute that owns the entiry
     */
    public LinkedEntityAttribute getLinkedEntityAttribute() {
        return linkedEntityAttribute;
    }

    /**
     * Sets linked attribute that owns the entiry.
     * @param linkedEntityAttribute link attribute
     */
    public void setLinkedEntityAttribute(LinkedEntityAttribute linkedEntityAttribute) {
        this.linkedEntityAttribute = linkedEntityAttribute;
    }

    /**
     * Get satellite object for entiry link.
     * @return satellite
     */
    public Satellite getSatellite() {
        if (satellite == null)
            satellite = new Satellite();
        return satellite;
    }

    /**
     * Detect is satellite are defined for link.
     * @return true if satellite are defined
     */
    public boolean isSatelliteDefined() {
        return satellite != null;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        // make a shallow copy of EntityAttribute. Don't copy satellite information.
        EntityAttribute copy = (EntityAttribute) super.clone();
        return copy;
    }

}
