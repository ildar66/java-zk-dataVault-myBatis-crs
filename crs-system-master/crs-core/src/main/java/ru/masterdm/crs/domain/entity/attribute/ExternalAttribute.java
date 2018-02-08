package ru.masterdm.crs.domain.entity.attribute;

import java.time.LocalDateTime;

import ru.masterdm.crs.domain.entity.DigestSupport;
import ru.masterdm.crs.domain.entity.attribute.value.ValueAccessor;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;

/**
 * Base class for attributes technically stored at other DV structure.
 * Foundation class for attributes stored at other DV structure (hub-satellite) and referenced by
 * link ({@link AttributeType#FILE}, {@link AttributeType#TEXT} (multilang), {@link AttributeType#STRING} (multilang)).
 * @param <T> Java type to be stored at view attribute
 * @param <V> type of real value object
 * @author Pavel Masalov
 */
public abstract class ExternalAttribute<T, V> extends LinkAttribute<T> implements DigestSupport {

    private Long satelliteId;
    private LocalDateTime satelliteLdts;
    private String digest;

    private ValueAccessor<V> valueAccessor;

    /**
     * Construct attribute with value accessor.
     * @param valueAccessor value accessor
     */
    public ExternalAttribute(ValueAccessor valueAccessor) {
        this.valueAccessor = valueAccessor;
    }

    /**
     * Constructor for known attribute meta.
     * @param meta attribute meta
     * @param valueAccessor value accessor
     */
    public ExternalAttribute(AttributeMeta meta, ValueAccessor valueAccessor) {
        super(meta);
        this.valueAccessor = valueAccessor;
    }

    /**
     * Returns linked satellite table primary id.
     * @return linked satellite table primary id
     */
    public Long getSatelliteId() {
        return satelliteId;
    }

    /**
     * Sets linked satellite table primary id.
     * @param satelliteId linked satellite table primary id
     */
    public void setSatelliteId(Long satelliteId) {
        this.satelliteId = satelliteId;
    }

    /**
     * Returns linked satellite table load datetime.
     * @return linked satellite table load datetime
     */
    public LocalDateTime getSatelliteLdts() {
        return satelliteLdts;
    }

    /**
     * Sets linked satellite table load datetime.
     * @param satelliteLdts linked satellite table load datetime
     */
    public void setSatelliteLdts(LocalDateTime satelliteLdts) {
        this.satelliteLdts = satelliteLdts;
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
     * Returns value accessor.
     * @return value accessor
     */
    public ValueAccessor<V> getValueAccessor() {
        return valueAccessor;
    }

    /**
     * Sets value accessor.
     * @param valueAccessor value accessor
     */
    public void setValueAccessor(ValueAccessor<V> valueAccessor) {
        this.valueAccessor = valueAccessor;
    }
}
