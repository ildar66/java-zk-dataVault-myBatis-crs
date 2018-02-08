package ru.masterdm.crs.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.FavoritesAttributeMeta;
import ru.masterdm.crs.exception.CrsException;

/**
 * Favorites checker business entity.
 * @author Alexey Kirilchev
 */
public class FavoritesChecker implements Serializable {

    private Set<Pair<Long, String>> pairs;

    /**
     * Constructor.
     * @param favorites favorites
     */
    public FavoritesChecker(Entity favorites) {
        String entityKey = favorites.getMeta().getKey();
        if (!FavoritesAttributeMeta.METADATA_KEY.equals(entityKey))
            throw new CrsException("wrong entity meta key '" + entityKey + "'");
        List entities = favorites.getAttributes().values()
                                 .stream()
                                 .filter(e -> e.getMeta().getType() == AttributeType.REFERENCE)
                                 .flatMap(e -> {
                                     List<Entity> entityList = ((LinkedEntityAttribute) e).getEntityList();
                                     return entityList.stream();
                                 })
                                 .collect(Collectors.toList());
        initPairs(entities);
    }

    /**
     * Adds entity to favorites checker.
     * @param entity entity
     */
    public void addToFavoritesChecker(Entity entity) {
        pairs.add(Pair.of(entity.getHubId(), entity.getMeta().getKey()));
    }

    /**
     * Removes entity from favorites checker.
     * @param entity entity
     */
    public void removeFromFavoritesChecker(Entity entity) {
        pairs.remove(Pair.of(entity.getHubId(), entity.getMeta().getKey()));
    }

    /**
     * Constructor.
     * @param favoritesEntities favorites entities
     */
    public FavoritesChecker(List<Entity> favoritesEntities) {
        initPairs(favoritesEntities);
    }

    /**
     * Initialize pairs of hub identifier and entity meta key.
     * @param favoritesEntities favorites entities
     */
    private void initPairs(List<Entity> favoritesEntities) {
        pairs = favoritesEntities.stream().map(e -> Pair.of(e.getHubId(), e.getMeta().getKey())).collect(Collectors.toSet());
    }

    /**
     * Returns true if entity added in favorites, false otherwise.
     * @param entity entity
     * @return true if entity added in favorites, false otherwise
     */
    public boolean isFavorite(Entity entity) {
        if (entity == null)
            throw new CrsException("entity is null");
        return pairs.contains(Pair.of(entity.getHubId(), entity.getMeta().getKey()));
    }
}
