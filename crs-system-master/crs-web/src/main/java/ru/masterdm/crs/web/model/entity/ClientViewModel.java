package ru.masterdm.crs.web.model.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Default;
import org.zkoss.bind.annotation.ExecutionParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import ru.masterdm.crs.domain.FavoritesChecker;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.meta.ClientAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.FavoritesService;
import ru.masterdm.crs.web.domain.UserProfile;

/**
 * Client view model class.
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ClientViewModel {

    @WireVariable
    protected EntityService entityService;
    @WireVariable
    protected EntityMetaService entityMetaService;
    @WireVariable("userProfile")
    private UserProfile userProfile;
    @WireVariable("pages")
    private Properties pages;
    @WireVariable
    private FavoritesService favoritesService;

    private Entity client;
    private Entity group;
    private FavoritesChecker favoritesChecker;
    private boolean calcMode;

    /**
     * Initiates context.
     * @param client client
     * @param mode calculation mode
     */
    @Init
    public void initSetup(@ExecutionParam("client") Entity client, @ExecutionParam("mode") @Default("false") Boolean mode) {
        if (client == null) {
            String key = (String) Executions.getCurrent().getAttribute("key");
            if (key != null) {
                EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(ClientAttributeMeta.METADATA_KEY, null);
                this.client = entityService.getEntity(entityMeta, key, null);
            }
        } else
            this.client = client;
        calcMode = mode;
    }

    /**
     * Returns client.
     * @return client
     */
    public Entity getClient() {
        return client;
    }

    /**
     * Navigates group.
     * @param clientGroup Group
     */
    @Command
    public void navigateClientGroup(@BindingParam("clientGroup") Entity clientGroup) {
        Executions.getCurrent().setAttribute("clientGroup", clientGroup);
        Executions.getCurrent().setAttribute("key", clientGroup.getKey());
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "entity.client_group");
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Returns group.
     * @return group
     */
    public Entity getGroup() {
        if (group != null)
            return group;
        LinkedEntityAttribute groupAttribute = (LinkedEntityAttribute) client.getAttribute(ClientAttributeMeta.CLIENT_GROUP.getKey());
        List<Entity> entities = (List<Entity>) groupAttribute.getEntityList();
        if (entities.size() == 0)
            return null;
        group = entityService.getEntity(entities.get(0).getMeta(), entities.get(0).getKey(), null);
        return group;
    }

    /**
     * Returns true if entity added in favorites, false otherwise.
     * @return true if entity added in favorites, false otherwise
     */
    public boolean isFavorite() {
        if (favoritesChecker == null)
            favoritesChecker = new FavoritesChecker(favoritesService.getFavorites());
        return favoritesChecker.isFavorite(client);
    }

    /**
     * Adds favorite.
     */
    @Command
    @SmartNotifyChange("favorite")
    public void addFavorite() {
        favoritesService.addFavorite(client);
        favoritesChecker = null;
    }

    /**
     * Removes favorite.
     */
    @Command
    @SmartNotifyChange("favorite")
    public void removeFavorite() {
        favoritesService.removeFavorite(client);
        favoritesChecker = null;
    }

    /**
     * Returns calculation mode.
     * @return calculation mode
     */
    public boolean isCalcMode() {
        return calcMode;
    }
}
