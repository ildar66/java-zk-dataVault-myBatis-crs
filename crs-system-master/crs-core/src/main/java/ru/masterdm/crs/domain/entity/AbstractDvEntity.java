package ru.masterdm.crs.domain.entity;

import static ru.masterdm.crs.domain.entity.meta.CommonColumn.H_ID;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.ID;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.KEY;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.LDTS;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.REMOVED;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.HUB_LDTS;

import java.time.LocalDateTime;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ru.masterdm.crs.domain.AbstractEntity;

/**
 * Abstract Data Vault entity.
 * @author Sergey Valiev
 */
public abstract class AbstractDvEntity implements AbstractEntity<Long> {

    private Long id;
    private Long hubId;
    private String key;
    private LocalDateTime ldts;
    private LocalDateTime hubLdts;
    private boolean removed;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns hub id.
     * @return hub id
     */
    public Long getHubId() {
        return hubId;
    }

    /**
     * Sets hub id.
     * @param hubId hub id
     */
    public void setHubId(Long hubId) {
        this.hubId = hubId;
    }

    /**
     * Returns business key.
     * @return business key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets business key.
     * @param key business key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns load date timestamp.
     * @return load date timestamp
     */
    public LocalDateTime getLdts() {
        return ldts;
    }

    /**
     * Sets load date timestamp.
     * @param ldts load date timestamp
     */
    public void setLdts(LocalDateTime ldts) {
        this.ldts = ldts;
    }

    /**
     * Returns hub load datetime.
     * @return hub load datetime
     */
    public LocalDateTime getHubLdts() {
        return hubLdts;
    }

    /**
     * Sets hub load datetime.
     * @param hubLdts hub load datetime
     */
    public void setHubLdts(LocalDateTime hubLdts) {
        this.hubLdts = hubLdts;
    }

    /**
     * Returns removed flag.
     * @return <code>true</code> if entity is removed
     */
    public boolean isRemoved() {
        return removed;
    }

    /**
     * Sets removed flag.
     * @param removed removed flag
     */
    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    /**
     * Set system properties from map loaded from database.
     * @param data column data loaded from database
     */
    public void setSystemPropertyByMap(Map<String, Object> data) {
        this.setId((Long) data.get(ID.name()));
        this.setHubId((Long) data.get(H_ID.name()));
        this.setKey((String) data.get(KEY.name()));
        this.setLdts((LocalDateTime) data.get(LDTS.name()));
        this.setRemoved((Boolean) data.get(REMOVED.name()));
        this.setHubLdts((LocalDateTime) data.get(HUB_LDTS.name()));
    }

    /**
     * Set system properties from other datavalue entity.
     * @param source datavalued entity
     */
    public void setSystemProperty(AbstractDvEntity source) {
        this.setId(source.getId());
        this.setHubId(source.getHubId());
        this.setKey(source.getKey());
        this.setLdts(source.getLdts());
        this.setRemoved(source.isRemoved());
        this.setHubLdts(source.getHubLdts());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractDvEntity that = (AbstractDvEntity) o;
        return new EqualsBuilder()
                .append(key, that.key)
                .isEquals();
    }

    @Override
    public int hashCode() {
        if (key == null) {
            return super.hashCode();
        }

        return new HashCodeBuilder(17, 37)
                .append(key)
                .toHashCode();
    }

    @Override
    public String toString() {
        return super.toString() + " key = " + key;
    }
}
