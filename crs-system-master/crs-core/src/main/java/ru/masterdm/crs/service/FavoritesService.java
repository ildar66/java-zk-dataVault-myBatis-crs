package ru.masterdm.crs.service;

import java.util.List;

import ru.masterdm.crs.domain.FavoritesChecker;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;

/**
 * Favorites interaction service.
 * @author Alexey Kirilchev
 */
public interface FavoritesService {

    /**
     * Returns favorites of current user.
     * @return favorites of current user
     */
    Entity getFavorites();

    /**
     * Returns container of favorite flags for entities.
     * @param entities entities
     * @return container of favorite flags for entities
     */
    FavoritesChecker findFavorites(List<Entity> entities);

    /**
     * Adds favorite.
     * @param entity entity
     */
    void addFavorite(Entity entity);

    /**
     * Removes favorite.
     * @param entity entity
     */
    void removeFavorite(Entity entity);

    /**
     * Adds favorite.
     * @param entity entity
     * @param favoritesChecker favorites checker
     */
    void addFavorite(Entity entity, FavoritesChecker favoritesChecker);

    /**
     * Removes favorite.
     * @param entity entity
     * @param favoritesChecker favorites checker
     */
    void removeFavorite(Entity entity, FavoritesChecker favoritesChecker);

    /**
     * Returns is entity meta supports favorites.
     * @param entityMeta entity meta
     * @return true if entity meta supports favorites, false otherwise
     */
    boolean isEntitySupportsFavorites(EntityMeta entityMeta);
}
