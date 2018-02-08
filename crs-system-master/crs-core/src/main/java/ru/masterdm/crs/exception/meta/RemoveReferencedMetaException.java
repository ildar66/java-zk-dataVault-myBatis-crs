package ru.masterdm.crs.exception.meta;

import java.util.List;
import java.util.stream.Collectors;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.exception.CrsException;

/**
 * Raised if removed metadata are referenced by other references attributes.
 * @author Pavel Masalov
 */
public class RemoveReferencedMetaException extends CrsException {

    private EntityMeta referencedEntityMeta;
    private List<EntityMeta> referenceByEntityMetas;

    /**
     * Construct exception with data.
     * @param referencedEntityMeta referenced entity meta
     * @param referenceByEntityMetas list of reference entity meta
     */
    public RemoveReferencedMetaException(EntityMeta referencedEntityMeta, List<EntityMeta> referenceByEntityMetas) {
        super("Entity " + referencedEntityMeta.getKey() + " referenced by " + (referenceByEntityMetas.size() == 1
                                                                               ? referenceByEntityMetas.get(0).getKey() + " entity"
                                                                               : referenceByEntityMetas.size() + " entities"));
        this.referencedEntityMeta = referencedEntityMeta;
        this.referenceByEntityMetas = referenceByEntityMetas;
    }

    /**
     * Returns referenced entity meta.
     * @return entity meta
     */
    public EntityMeta getReferencedEntityMeta() {
        return referencedEntityMeta;
    }

    /**
     * Returns list of reference entity meta.
     * @return list of entity meta
     */
    public List<EntityMeta> getReferenceByEntityMetas() {
        return referenceByEntityMetas;
    }

    /**
     * List of attributes at {@link #getReferenceByEntityMetas()} referenced to .
     * @return list of reference attribute
     */
    public List<AttributeMeta> getReferencedByAttributes() {
        return referenceByEntityMetas.stream().flatMap(
                (em) -> em.getAttributes().stream()) // few attributes at single entity meta may reference to same other meta
                                     .filter((am) -> am.getType() == AttributeType.REFERENCE
                                                     && referencedEntityMeta.getKey().equals(am.getEntityKey()))
                                     .collect(Collectors.toList());
    }
}
