package ru.masterdm.crs.web.domain.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ru.masterdm.crs.domain.entity.Entity;

/**
 * Abstract attribute class.
 * @author Igor Matushak
 */
public class EntityStatus implements Serializable {

    private Entity entity;
    private boolean editingStatus;
    private List<EntityStatus> children;

    /**
     * Constructor.
     * @param entity entity
     * @param editingStatus editing status
     */
    public EntityStatus(Entity entity, boolean editingStatus) {
        this.entity = entity;
        this.editingStatus = editingStatus;
    }

    /**
     * Returns entity.
     * @return entity
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Sets entity.
     * @param entity entity
     */
    public void setEntity(Entity entity) {
        this.entity = entity;
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
    public List<EntityStatus> getChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }

    /**
     * Sets children.
     * @param children children
     */
    public void setChildren(List<EntityStatus> children) {
        this.children = children;
    }
}
