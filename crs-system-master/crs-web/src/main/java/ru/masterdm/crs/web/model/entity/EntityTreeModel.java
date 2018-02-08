package ru.masterdm.crs.web.model.entity;

import org.zkoss.zul.AbstractTreeModel;

import ru.masterdm.crs.web.domain.entity.EntityStatus;

/**
 * Entity tree model class.
 * @author Igor Matushak
 */
public class EntityTreeModel extends AbstractTreeModel<EntityStatus> {

    /**
     * Constructor.
     * @param entityStatus entity status.
     */
    public EntityTreeModel(EntityStatus entityStatus) {
        super(entityStatus);
    }

    @Override
    public EntityStatus getChild(EntityStatus parent, int index) {
        return parent.getChildren().get(index);
    }

    @Override
    public int getChildCount(EntityStatus parent) {
        return parent.getChildren().size();
    }

    @Override
    public boolean isLeaf(EntityStatus node) {
        return node.getChildren().isEmpty();
    }
}