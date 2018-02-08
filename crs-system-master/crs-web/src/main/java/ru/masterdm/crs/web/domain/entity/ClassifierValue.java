package ru.masterdm.crs.web.domain.entity;

import java.util.List;
import java.util.Objects;

import ru.masterdm.crs.domain.entity.AbstractDvEntity;
import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;

/**
 * Classifier value class.
 * @author Igor Matushak
 * @author Alexey Kirilchev
 */
public class ClassifierValue {

    /**
     * Constructor.
     * @param type type
     * @param comment comment
     * @param profile profile
     */
    public ClassifierValue(AbstractAttribute type, AbstractAttribute comment, AbstractAttribute profile) {
        this.type = type;
        this.comment = comment;
        this.profile = profile;
    }

    private AbstractAttribute type;
    private AbstractAttribute comment;
    private AbstractAttribute profile;
    private boolean changed;

    /**
     * Returns type.
     * @return type
     */
    public AbstractAttribute getType() {
        return type;
    }

    /**
     * Returns comment.
     * @return comment
     */
    public AbstractAttribute getComment() {
        return comment;
    }

    /**
     * Returns profile.
     * @return profile
     */
    public AbstractAttribute getProfile() {
        return profile;
    }

    /**
     * Returns comment value.
     * @return comment value
     */
    public String getCommentValue() {
        return (String) getComment().getValue();
    }

    /**
     * Sets comment value.
     * @param commentValue comment value
     */
    public void setCommentValue(String commentValue) {
        setChanged(isChanged() || !Objects.equals(commentValue, getComment().getValue()));
        getComment().setValue(commentValue);
    }

    /**
     * Returns classifier value.
     * @return classifier value
     */
    public Object getClassifierValue() {
        return getType().getValue();
    }

    /**
     * Sets classifier value.
     * @param classifierValue classifier value
     */
    public void setClassifierValue(Object classifierValue) {
        setChanged(isChanged() || !Objects.equals(classifierValue, getType().getValue()));
        getType().setValue(classifierValue);
    }

    /**
     * Returns classifier reference value.
     * @return classifier reference value
     */
    public AbstractDvEntity getClassifierReferenceValue() {
        List<AbstractDvEntity> entityList = ((LinkedEntityAttribute<AbstractDvEntity>) getType()).getEntityList();
        return entityList.isEmpty() ? null : entityList.get(0);
    }

    /**
     * Sets classifier reference value.
     * @param entity classifier reference value
     */
    public void setClassifierReferenceValue(AbstractDvEntity entity) {
        List<AbstractDvEntity> entityList = ((LinkedEntityAttribute<AbstractDvEntity>) getType()).getEntityList();
        boolean changed = (entity != null
                           ? (entityList.size() != 1 || !Objects.equals(entity, entityList.get(0)))
                           : !entityList.isEmpty());
        setChanged(isChanged() || changed);
        if (changed) {
            entityList.clear();
            if (entity != null)
                entityList.add(entity);
        }
    }

    /**
     * Returns is changed and not yet saved.
     * @return is changed and not yet saved
     */
    public boolean isChanged() {
        return changed;
    }

    /**
     * Sets is changed and not yet saved.
     * @param changed is changed and not yet saved
     */
    public void setChanged(boolean changed) {
        this.changed = changed;
    }
}
