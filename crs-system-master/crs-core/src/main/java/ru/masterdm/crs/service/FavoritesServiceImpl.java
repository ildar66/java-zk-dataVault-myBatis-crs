package ru.masterdm.crs.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import ru.masterdm.crs.domain.FavoritesChecker;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.FavoritesAttributeMeta;
import ru.masterdm.crs.exception.CrsException;

/**
 * Favorites interaction service implementation.
 * @author Alexey Kirilchev
 */
@Validated
@Service("favoritesService")
public class FavoritesServiceImpl implements FavoritesService {

    @Autowired
    private SecurityService securityService;
    @Autowired
    private EntityService entityService;

    @Transactional
    @Override
    public Entity getFavorites() {
        User currentUser = securityService.getCurrentUser();
        Entity favorites = currentUser.getFavorites();
        if (favorites == null) {
            favorites = entityService.newEmptyEntity(FavoritesAttributeMeta.METADATA_KEY);
            entityService.persistEntity(favorites);

            currentUser.setFavorites(favorites);
            entityService.persistEntity(currentUser);
        }
        return entityService.getEntity(favorites.getMeta(), favorites.getHubId(), null);
    }

    @Override
    public FavoritesChecker findFavorites(@NotNull List<Entity> entities) {
        Entity favorites = getFavorites();
        List<Entity> results = entities.stream()
                                       .filter(e -> getFavoritesEntityList(e, favorites)
                                               .stream()
                                               .anyMatch(t -> t.getHubId().equals(e.getHubId()) && t.getMeta().getKey().equals(e.getMeta().getKey())))
                                       .collect(Collectors.toList());
        return new FavoritesChecker(results);
    }

    @Transactional
    @Override
    public void addFavorite(@NotNull Entity entity) {
        Entity favorites = getFavorites();
        List<Entity> entities = getFavoritesEntityList(entity, favorites);
        if (!entities.stream().anyMatch(e -> e.getHubId().equals(entity.getHubId()))) {
            entities.add(entity);
            entityService.persistEntity(favorites);
        }
    }

    @Transactional
    @Override
    public void removeFavorite(@NotNull Entity entity) {
        Entity favorites = getFavorites();
        List<Entity> entities = getFavoritesEntityList(entity, favorites);
        if (entities.removeAll(entities.stream().filter(e -> e.getHubId().equals(entity.getHubId())).collect(Collectors.toSet())))
            entityService.persistEntity(favorites);
    }

    @Transactional
    @Override
    public void addFavorite(@NotNull Entity entity, @NotNull FavoritesChecker favoritesChecker) {
        addFavorite(entity);
        favoritesChecker.addToFavoritesChecker(entity);
    }

    @Transactional
    @Override
    public void removeFavorite(@NotNull Entity entity, @NotNull FavoritesChecker favoritesChecker) {
        removeFavorite(entity);
        favoritesChecker.removeFromFavoritesChecker(entity);
    }

    /**
     * Returns favorites entity list.
     * @param entity entity
     * @param favorites favorites
     * @return favorites entity list
     */
    private List<Entity> getFavoritesEntityList(Entity entity, Entity favorites) {
        String entityKey = entity.getMeta().getKey();
        AttributeMeta attributeMeta = favorites.getMeta().getAttributes().stream()
                                               .filter(e -> e.getType() == AttributeType.REFERENCE && entityKey.equals(e.getEntityKey()))
                                               .findFirst().orElse(null);
        if (attributeMeta == null)
            throw new CrsException("not supported entity key '" + entityKey + "'");
        return ((LinkedEntityAttribute) favorites.getAttribute(attributeMeta.getKey())).getEntityList();
    }

    @Override
    public boolean isEntitySupportsFavorites(EntityMeta entityMeta) {
        return entityMeta.getAttributes().stream().anyMatch(e -> e.getType() == AttributeType.REFERENCE
                                                                 && FavoritesAttributeMeta.METADATA_KEY.equals(e.getEntityKey()));
    }
}