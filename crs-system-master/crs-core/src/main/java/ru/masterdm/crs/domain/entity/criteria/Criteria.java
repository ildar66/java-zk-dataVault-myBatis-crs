package ru.masterdm.crs.domain.entity.criteria;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;

import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;

/**
 * Set of filter and sort query criteria.
 * @author Pavel Masalov
 */
public class Criteria implements Cloneable {

    private boolean strictLatestActualRecord = true;
    private Set<AttributeMeta> addPartition;
    private Where where;
    private Order order;
    private Collection<Long> hubIds;
    private Collection<Entity> referencedEntities;
    private Collection<Pair<Long, LocalDateTime>> hubIdsAndLdts;
    private boolean resultCache;
    private User user;

    /**
     * Get filter (SQL WHERE) part of criteria.
     * @return filter criteria
     */
    public Where getWhere() {
        if (where == null)
            where = new Where();
        return where;
    }

    /**
     * Return WHERE part existence in criteria.
     * @return true is WHERE part exists
     */
    public boolean isWhereDefined() {
        return where != null && where.isDefined();
    }

    /**
     * Get sort (SQL ORDER BY) part of criteria.
     * @return sort part of criteria
     */
    public Order getOrder() {
        if (order == null)
            order = new Order();
        return order;
    }

    /**
     * Return ORDER part existence in criteria.
     * @return true is ORDER part exists
     */
    public boolean isOrderDefined() {
        return order != null && order.isDefined();
    }

    /**
     * Returns flag to manage retrieve only technically last record by criteria strictly.
     * @return strict flag
     */
    public boolean isStrictLatestActualRecord() {
        return strictLatestActualRecord;
    }

    /**
     * Sets flag to manage retrieve only technically last record by criteria strictly.
     * @param strictLatestActualRecord strict flag
     */
    public void setStrictLatestActualRecord(boolean strictLatestActualRecord) {
        this.strictLatestActualRecord = strictLatestActualRecord;
    }

    /**
     * Returns hub ids used to filter result.
     * @return hub ids
     */
    public Collection<Long> getHubIds() {
        return hubIds;
    }

    /**
     * Sets hub ids used to filter result.
     * @param hubIds hub ids
     */
    public void setHubIds(Collection<Long> hubIds) {
        this.hubIds = hubIds;
    }

    /**
     * Returns referenced hubs.
     * @return referenced hubs
     */
    public Collection<Entity> getReferencedEntities() {
        if (referencedEntities == null)
            referencedEntities = new ArrayList<>();
        return referencedEntities;
    }

    /**
     * Adds referenced entity.
     * @param referencedEntity referenced entity
     */
    public void addReferencedEntity(Entity referencedEntity) {
        getReferencedEntities().add(referencedEntity);
    }

    /**
     * Returns list of id and ldts pairs.
     * @return list of id and ldts pairs
     */
    public Collection<Pair<Long, LocalDateTime>> getHubIdsAndLdts() {
        return hubIdsAndLdts;
    }

    /**
     * Sets list of id and ldts pairs.
     * @param hubIdsAndLdts list of id and ldts pairs
     */
    public void setHubIdsAndLdts(Collection<Pair<Long, LocalDateTime>> hubIdsAndLdts) {
        this.hubIdsAndLdts = hubIdsAndLdts;
    }

    /**
     * Returns set of attributes added into "row_number over partition".
     * @return set of attributes
     */
    public Set<AttributeMeta> getAddPartition() {
        if (addPartition == null)
            addPartition = new HashSet<>();
        return addPartition;
    }

    /**
     * Add attribute into "row_number over partition".
     * @param attributeMeta attribute meta
     */
    public void addPartitionAttribute(AttributeMeta attributeMeta) {
        getAddPartition().add(attributeMeta);
    }

    /**
     * Detect if additional partition attributes are defined.
     * @return true if attributes defined
     */
    public boolean isAddPartitionDefined() {
        return !CollectionUtils.isEmpty(addPartition);
    }

    /**
     * Returns usage of result_cache in base request.
     * @return usage of result_cache in base request
     */
    public boolean isResultCache() {
        return resultCache;
    }

    /**
     * Sets usage of result_cache in base request.
     * @param resultCache usage of result_cache in base request
     */
    public void setResultCache(boolean resultCache) {
        this.resultCache = resultCache;
    }

    /**
     * Returns secured user.
     * @return secured user
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets secured user.
     * @param user secured user
     */
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        // make a shallow copy of criteria and shallow copy of where
        Criteria copy = (Criteria) super.clone();
        copy.where = ObjectUtils.clone(this.where);
        return copy;
    }
}
