package ru.masterdm.crs.domain.calc;

import org.apache.commons.lang3.tuple.MutablePair;

/**
 * Formula dependency.
 * @author Sergey Valiev
 */
public class FormulaDependencyPair extends MutablePair<String, Formula> {

    private Long id;

    /**
     * Constructor.
     */
    public FormulaDependencyPair() {
    }

    /**
     * Constructor.
     * @param left left value
     * @param right right value
     */
    public FormulaDependencyPair(String left, Formula right) {
        super(left, right);
        id = right.getId();
    }

    /**
     * Returns id.
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets id.
     * @param id id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * <p>Obtains an immutable pair of from two objects inferring the generic types.</p>
     * <p>This factory allows the pair to be created using inference to
     * obtain the generic types.</p>
     * @param left the left element, may be null
     * @param right the right element, may be null
     * @return a pair formed from the two parameters, not null
     */
    public static FormulaDependencyPair of(final String left, final Formula right) {
        return new FormulaDependencyPair(left, right);
    }
}
