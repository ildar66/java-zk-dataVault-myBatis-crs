package ru.masterdm.crs.service.calc;

import org.springframework.stereotype.Service;

import ru.masterdm.crs.domain.entity.meta.EntityMeta;

/**
 * Prototype metadata creator.
 * @author Pavel Masalov
 */
@Service
public interface EntityMetaPrototypeFactory {

    /**
     * Create entity meta prototype form.
     * @param key metadata key
     * @return entity metadata
     */
    EntityMeta create(String key);
}
