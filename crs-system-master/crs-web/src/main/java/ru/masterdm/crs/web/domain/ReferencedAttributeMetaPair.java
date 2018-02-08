package ru.masterdm.crs.web.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.MutablePair;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;

/**
 * Referenced attribute meta pair class.
 * @author Igor Matushak
 */
public class ReferencedAttributeMetaPair extends MutablePair<AttributeMeta, AttributeMeta> {

    /**
     * Constructor.
     * @param left attribute meta
     * @param right referenced attribute meta
     */
    public ReferencedAttributeMetaPair(AttributeMeta left, AttributeMeta right) {
        super(left, right);
    }

    /**
     * Returns attribute meta.
     * @return attribute meta
     */
    public AttributeMeta getAttributeMeta() {
        return getLeft();
    }

    /**
     * Returns referenced attribute meta.
     * @return referenced attribute meta
     */
    public AttributeMeta getReferencedAttributeMeta() {
        return getRight();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ReferencedAttributeMetaPair that = (ReferencedAttributeMetaPair) o;

        String referencedAttributeMetaKey = getRight() == null ? null : getRight().getKey();
        String thatReferencedAttributeMetaKey = that.getRight() == null ? null : that.getRight().getKey();

        return new EqualsBuilder()
                .append(getLeft().getKey(), that.getLeft().getKey())
                .append(referencedAttributeMetaKey, thatReferencedAttributeMetaKey)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getLeft().getKey())
                .append(getRight() == null ? null : getRight().getKey())
                .toHashCode();
    }

    /**
     * <p>Obtains a pair of from two objects inferring the generic types.</p>
     * <p>This factory allows the pair to be created using inference to
     * obtain the generic types.</p>
     * @param left attribute meta, may be null
     * @param right referenced attribute meta, may be null
     * @return a pair formed from the two parameters, not null
     */
    public static ReferencedAttributeMetaPair of(final AttributeMeta left, final AttributeMeta right) {
        return new ReferencedAttributeMetaPair(left, right);
    }

}
