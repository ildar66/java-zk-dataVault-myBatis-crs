package ru.masterdm.crs.domain.entity.criteria;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;

/**
 * Used to create WHERE.
 * Predicate are based on entity attribute.
 * <pre>
 * where
 *     F1=V0 -- first simple top-level field-operator-value(s) item
 *     or
 *     (F1=V1 and F2 like 'V2%') -- first top-level group with elements
 *     or
 *     (F1=V3 and F2 is null) -- second top-level group with elements
 *     or
 *     (F1 is null and (F2='V3' or F2='V4')) -- third group with element an one child group
 * </pre>
 * @author Pavel Masalov
 */
public class Where implements Cloneable {

    private List<Connectable> content = new ArrayList<>();
    private Map<AttributeMeta, ReferenceWhere> referenceWheres;
    private Map<AttributeMeta, Where> multilangWheres;

    /**
     * Get top level groups or items that used to construct predicate.
     * @return list of top level groups
     */
    public List<Connectable> getContent() {
        return content;
    }

    /**
     * Add top level <code>(..)</code> group to predicate.
     * @param whereParenthesesGroup <code>(..)</code> element
     */
    public void addGroup(WhereParenthesesGroup whereParenthesesGroup) {
        content.add(whereParenthesesGroup);
    }

    /**
     * Add top level field-operator-value element.
     * @param whereItem item of filter
     */
    public void addItem(WhereItem whereItem) {
        AttributeMeta searchAttribute = whereItem.getSearchAttribute();
        if (searchAttribute.isMultilang() && (searchAttribute.getType() == AttributeType.TEXT || searchAttribute.getType() == AttributeType.STRING)) {
            if (whereItem.getConjunction() == null)
                whereItem.setConjunction(Conjunction.AND);
            getMultilangWhere(searchAttribute).getContent().add(whereItem);
        } else {
            if (!content.isEmpty() && whereItem.getConjunction() == null)
                whereItem.setConjunction(Conjunction.AND);
            if (content.isEmpty())
                whereItem.setConjunction(null);
            content.add(whereItem);
        }
    }

    /**
     * Add top level referencesd subquery field-operator-value element.
     * @param referenceAttribute REFERENCE attribute of main object
     * @param whereItemAtReferencedEntity where item to operate over child entity
     */
    public void addReferenceItem(AttributeMeta referenceAttribute, WhereItem whereItemAtReferencedEntity) {
        ReferenceWhere referencedWhere = getReferenceWhere(referenceAttribute);
        if (referencedWhere.getConjunction() == null)
            referencedWhere.setConjunction(whereItemAtReferencedEntity.getConjunction() != null
                                           ? whereItemAtReferencedEntity.getConjunction() : Conjunction.AND);
        referencedWhere.addItem(whereItemAtReferencedEntity);
    }

    /**
     * Set reference exists or not exists option.
     * @param referenceAttribute REFERENCE attribute of main object
     * @param conjunction conjunction
     */
    public void setReferenceExists(AttributeMeta referenceAttribute, Conjunction conjunction) {
        getReferenceWhere(referenceAttribute).setConjunction(conjunction);
    }

    /**
     * Remove top level element.
     * @param whereItem where element
     * @return true if item removed
     */
    public boolean removeItem(WhereItem whereItem) {
        return content.remove(whereItem);
    }

    /**
     * Generate text for WHERE to inject it into DSQL.
     * @return generated text
     */
    public String getText() {
        return content.stream().map(Connectable::getText).collect(Collectors.joining(" "));
    }

    /**
     * Detect if WHERE statement defined.
     * @return true if where ready to generate text
     */
    public boolean isDefined() {
        return !content.isEmpty();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        // make a shallow copy of where and shallow copy of its content. Items itself are not cloning
        Where copy = (Where) super.clone();
        copy.content = ObjectUtils.clone(this.content);
        return copy;
    }

    /**
     * Returns map of reference wheres linked to reference attributes.
     * @return reference where map
     */
    public Map<AttributeMeta, ReferenceWhere> getReferenceWheres() {
        if (referenceWheres == null)
            referenceWheres = new HashMap<>();
        return referenceWheres;
    }

    /**
     * Reference where for REFERENCE attribute to use it over referenced entity in main entity filter.
     * @param attributeMeta REFERENCE attribute metadata
     * @return where object
     */
    public ReferenceWhere getReferenceWhere(AttributeMeta attributeMeta) {
        ReferenceWhere ret = getReferenceWheres().get(attributeMeta);
        if (ret == null) {
            ret = new ReferenceWhere();
            getReferenceWheres().put(attributeMeta, ret);
        }
        return ret;
    }

    /**
     * Is has referenced wheres defined.
     * @return true if one where defined at least
     */
    public boolean isHasReferenceWhere() {
        return referenceWheres != null && !referenceWheres.isEmpty();
    }

    /**
     * Returns map of multilang wheres linked to multilang attributes.
     * @return multilang where map
     */
    public Map<AttributeMeta, Where> getMultilangWheres() {
        if (multilangWheres == null)
            multilangWheres = new HashMap<>();
        return multilangWheres;
    }

    /**
     * Where for multilang attribute to use it over multilang entity in main entity filter.
     * @param attributeMeta multilang attribute metadata
     * @return where object
     */
    public Where getMultilangWhere(AttributeMeta attributeMeta) {
        Where ret = getMultilangWheres().get(attributeMeta);
        if (ret == null) {
            ret = new Where();
            getMultilangWheres().put(attributeMeta, ret);
        }
        return ret;
    }

    /**
     * Is has multilang wheres defined.
     * @return true if one where defined at least
     */
    public boolean isHasMultilangWhere() {
        return multilangWheres != null && !multilangWheres.isEmpty();
    }
}
