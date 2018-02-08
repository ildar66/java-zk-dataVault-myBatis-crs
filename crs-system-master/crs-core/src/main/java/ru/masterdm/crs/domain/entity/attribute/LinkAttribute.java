package ru.masterdm.crs.domain.entity.attribute;

import java.time.LocalDateTime;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;

/**
 * Base class to store attribute accessed by any kind of link table.
 * @param <V> Value Java type to be visible at attribute
 * @author Pavel Masalov
 */
public abstract class LinkAttribute<V> extends AbstractAttribute<V> {

    private Long linkId;
    private LocalDateTime linkLdts;
    private Long mainHubId;
    private Long linkedHubId;
    private boolean linkRemoved;

    /**
     * Default constructor.
     */
    public LinkAttribute() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinkAttribute)) return false;

        LinkAttribute<?> that = (LinkAttribute<?>) o;

        // metadata should be defined both
        if (that.getMeta() == null || this.getMeta() == null)
            return false;

        if (!getMeta().equals(that.getMeta()))
            return false;

        if (this.mainHubId == null || that.mainHubId == null
            || this.linkedHubId == null || that.linkedHubId == null)
            return super.equals(o);

        if (!mainHubId.equals(that.mainHubId)) return false;
        return linkedHubId.equals(that.linkedHubId);
    }

    @Override
    public int hashCode() {
        if (mainHubId == null || linkedHubId == null)
            return super.hashCode();

        int result = mainHubId.hashCode();
        result = 31 * result + linkedHubId.hashCode();
        return result;
    }

    /**
     * construct attribute with known meta.
     * @param meta attribute meta
     */
    public LinkAttribute(AttributeMeta meta) {
        super(meta);
    }

    /**
     * Returns link table primary id.
     * @return link table primary id
     */
    public Long getLinkId() {
        return linkId;
    }

    /**
     * Sets link table primary id.
     * @param linkId link table primary id
     */
    public void setLinkId(Long linkId) {
        this.linkId = linkId;
    }

    /**
     * Returns link table load datetime.
     * @return link table load datetime
     */
    public LocalDateTime getLinkLdts() {
        return linkLdts;
    }

    /**
     * Sets link table load datetime.
     * @param linkLdts link table load datetime
     */
    public void setLinkLdts(LocalDateTime linkLdts) {
        this.linkLdts = linkLdts;
    }

    /**
     * Returns main entity hub table primary id.
     * @return main entity hub table primary id
     */
    public Long getMainHubId() {
        return mainHubId;
    }

    /**
     * Sets main entity hub table primary id.
     * @param mainHubId main entity hub table primary id
     */
    public void setMainHubId(Long mainHubId) {
        this.mainHubId = mainHubId;
    }

    /**
     * Returns linked entity hub table primary id.
     * @return linked entity hub table primary id
     */
    public Long getLinkedHubId() {
        return linkedHubId;
    }

    /**
     * Sets linked entity hub table primary id.
     * @param linkedHubId linked entity hub table primary id
     */
    public void setLinkedHubId(Long linkedHubId) {
        this.linkedHubId = linkedHubId;
    }

    /**
     * Returns value for REMOVED column of link table.
     * @return removed value
     */
    public boolean isLinkRemoved() {
        return linkRemoved;
    }

    /**
     * Sets value for REMOVED column of link table.
     * @param linkRemoved removed value
     */
    public void setLinkRemoved(boolean linkRemoved) {
        this.linkRemoved = linkRemoved;
    }

    @Override
    public String toString() {
        return super.toString() + " mainHubId=" + mainHubId + " linkedHubId=" + linkedHubId;
    }
}
