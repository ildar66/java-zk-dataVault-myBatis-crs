package ru.masterdm.crs.web.domain.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMetaGroup;

/**
 * Entity meta tree element.
 * @author Alexey Kirilchev
 */
public class EntityMetaStatus implements Serializable {

    private EntityMetaGroup group;
    private EntityMeta entityMeta;
    private boolean editingStatus;
    private List<EntityMetaStatus> children;

    /**
     * Constructor.
     * @param entityMeta entity meta
     * @param editingStatus editing status
     */
    public EntityMetaStatus(EntityMeta entityMeta, boolean editingStatus) {
        this.entityMeta = entityMeta;
        this.editingStatus = editingStatus;
    }

    /**
     * Constructor.
     * @param group group
     * @param editingStatus editing status
     */
    public EntityMetaStatus(EntityMetaGroup group, boolean editingStatus) {
        this.group = group;
        this.editingStatus = editingStatus;
    }

    /**
     * Returns group.
     * @return group
     */
    public EntityMetaGroup getGroup() {
        return group;
    }

    /**
     * Sets group.
     * @param group group
     */
    public void setGroup(EntityMetaGroup group) {
        this.group = group;
    }

    /**
     * Returns entity meta.
     * @return entity meta
     */
    public EntityMeta getEntityMeta() {
        return entityMeta;
    }

    /**
     * Sets entity meta.
     * @param entityMeta entity meta
     */
    public void setEntityMeta(EntityMeta entityMeta) {
        this.entityMeta = entityMeta;
    }

    /**
     * Returns editing status.
     * @return editing status
     */
    public boolean isEditingStatus() {
        return editingStatus;
    }

    /**
     * Sets editing status.
     * @param editingStatus editing status
     */
    public void setEditingStatus(boolean editingStatus) {
        this.editingStatus = editingStatus;
    }

    /**
     * Returns children.
     * @return children
     */
    public List<EntityMetaStatus> getChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }

    /**
     * Sets children.
     * @param children children
     */
    public void setChildren(List<EntityMetaStatus> children) {
        this.children = children;
    }
}
