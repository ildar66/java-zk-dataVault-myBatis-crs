package ru.masterdm.crs.web.model.entity;

import org.zkoss.zul.AbstractTreeModel;

import ru.masterdm.crs.web.domain.entity.EntityMetaStatus;

/**
 * Entity meta tree model class.
 * @author Alexey Kirilchev
 */
public class EntityMetaTreeModel extends AbstractTreeModel<EntityMetaStatus> {

    /**
     * Constructor.
     * @param root entity meta status.
     */
    public EntityMetaTreeModel(EntityMetaStatus root) {
        super(root);
    }

    @Override
    public boolean isLeaf(EntityMetaStatus node) {
        return node.getChildren().isEmpty();
    }

    @Override
    public EntityMetaStatus getChild(EntityMetaStatus parent, int index) {
        return parent.getChildren().get(index);
    }

    @Override
    public int getChildCount(EntityMetaStatus parent) {
        return parent != null ? parent.getChildren().size() : 0;
    }
}
