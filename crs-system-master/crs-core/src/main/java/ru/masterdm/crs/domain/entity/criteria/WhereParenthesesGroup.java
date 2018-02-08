package ru.masterdm.crs.domain.entity.criteria;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Container for statement elements (item or other /child/ groups).
 * <pre>
 * where
 *     (F1=V1 and F2 like 'V2%') -- first top-level group with elements
 *     or
 *     (F1=V3 and F2 is null) -- second top-level group with elements
 *     or
 *     (F1 is null and (F2='V3' or F2='V4')) -- third group with element an one child group
 * </pre>
 * Group should have connectable.conjunction to join with prev item at statement.
 * @author Pavel Masalov
 */
public class WhereParenthesesGroup extends Connectable {

    private List<Connectable> content = new ArrayList<>();

    /**
     * Create single (..) group.
     */
    public WhereParenthesesGroup() {
        super();
    }

    /**
     * Create conjuncted (non single) group.
     * @param conjunction conjunction
     */
    public WhereParenthesesGroup(Conjunction conjunction) {
        super(conjunction);
    }

    @Override
    public String getText() {
        return getConjunction() != null
               ? getConjunction().getMeaning() + SPACE + content.stream().map(Connectable::getText).collect(Collectors.joining(" ", "(", ")"))
               : content.stream().map(Connectable::getText).collect(Collectors.joining(" ", "(", ")"));
    }

    /**
     * Add child (..) group.
     * @param whereParenthesesGroup (..) group
     */
    public void addChildGroup(WhereParenthesesGroup whereParenthesesGroup) {
        content.add(whereParenthesesGroup);
    }

    /**
     * Add field-operator-value item to group.
     * @param whereItem field-operator-value item
     */
    public void addItem(WhereItem whereItem) {
        if (content.size() > 0 && whereItem.getConjunction() == null)
            whereItem.setConjunction(Conjunction.AND);
        content.add(whereItem);
    }

}
