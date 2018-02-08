package ru.masterdm.crs.web.model.calc.formula;

import org.zkoss.zul.AbstractTreeModel;

import ru.masterdm.crs.domain.calc.Formula;

/**
 * Formula tree model class.
 * @author Mikhail Kuzmin
 */

public class FormulaTreeModel extends AbstractTreeModel<Formula> {

    /**
     * Root formula tree model.
     * @param root formula root
     */
    public FormulaTreeModel(Formula root) {
        super(root);
    }

    @Override
    public boolean isLeaf(Formula node) {
        return node.getChildren().size() == 0;
    }

    @Override
    public Formula getChild(Formula parent, int nth) {
        return parent.getChildren().get(nth).getRight();
    }

    @Override
    public int getChildCount(Formula parent) {
        int count = 0;
        if (parent != null) {
            count = parent.getChildren().size();
        }
        return count;
    }
}