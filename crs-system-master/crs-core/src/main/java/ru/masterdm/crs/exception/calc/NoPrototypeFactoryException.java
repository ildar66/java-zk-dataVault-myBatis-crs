package ru.masterdm.crs.exception.calc;

import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.exception.CrsException;

/**
 * Raised if {@link ru.masterdm.crs.service.EntityMetaService#getEntityMetaPrototypeFactory(EntityType)}
 * cant return instance of {@link ru.masterdm.crs.service.calc.EntityMetaPrototypeFactory}.
 * @author Pavel Masalov
 */
public class NoPrototypeFactoryException extends CrsException {

    private EntityType entityType;

    /**
     * Construct exception with error entity type.
     * @param entityType entity type
     */
    public NoPrototypeFactoryException(EntityType entityType) {
        super("No metadata prototype factory for entity type " + entityType);
        this.entityType = entityType;
    }

    /**
     * Returns error entity type.
     * @return entity type
     */
    public EntityType getEntityType() {
        return entityType;
    }
}
