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
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import ru.masterdm.crs.domain.FavoritesChecker;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.meta.ClientAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientGroupAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.FavoritesService;
import ru.masterdm.crs.web.domain.UserProfile;

/**
 * Client group view model class.
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class ClientGroupViewModel {

    @WireVariable
    protected EntityService entityService;
    @WireVariable
    protected EntityMetaService entityMetaService;
    @WireVariable("webConfig")
    private Properties webConfig;
    @WireVariable("userProfile")
    private UserProfile userProfile;
    @WireVariable("pages")
    private Properties pages;
    @WireVariable
    private FavoritesService favoritesService;

    private Entity clientGroup;
    private List<Entity> clients;
    private FavoritesChecker favoritesChecker;
    private boolean calcMode;
    private int pageSize;

    /**
     * Initiates context.
     * @param clientGroup client group
     * @param mode mode
     */
    @Init
    public void initSetup(@ExecutionParam("clientGroup") Entity clientGroup, @ExecutionParam("mode") @Default("false") Boolean mode) {
        pageSize = Integer.parseInt(webConfig.getProperty("pageSize"));
        if (clientGroup == null) {
            String key = (String) Executions.getCurrent().getAttribute("key");
            if (key != null) {
                EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(ClientGroupAttributeMeta.METADATA_KEY, null);
                this.clientGroup = entityService.getEntity(entityMeta, key, null);
            }
        } else
            this.clientGroup = clientGroup;
        calcMode = mode;
    }

    /**
     * Returns client group.
     * @return client group
     */
    public Entity getClientGroup() {
        return clientGroup;
    }

    /**
     * Navigates client.
     * @param client Client
     */
    @Command
    public void navigateClient(@BindingParam("client") Entity client) {
        Executions.getCurrent().setAttribute("client", client);
        Executions.getCurrent().setAttribute("key", client.getKey());
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "entity.client");
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Inits clients.
     */
    @Command
    public void initClients() {
        EntityMeta clientEntityMeta = entityMetaService.getEntityMetaByKey(ClientAttributeMeta.METADATA_KEY, null);
        Criteria criteria = new Criteria();
        criteria.addReferencedEntity(clientGroup);
        clients = (List<Entity>) entityService.getEntities(clientEntityMeta, criteria, null, null);
        BindUtils.postNotifyChange(null, null, this, "clients");
        BindUtils.postNotifyChange(null, null, this, "clientsListTitle");
    }

    /**
     * Returns clients.
     * @return clients
     */
    public List<Entity> getClients() {
        return clients;
    }

    /**
     * Returns true if entity added in favorites, false otherwise.
     * @return true if entity added in favorites, false otherwise
     */
    public boolean isFavorite() {
        if (favoritesChecker == null)
            favoritesChecker = new FavoritesChecker(favoritesService.getFavorites());
        return favoritesChecker.isFavorite(clientGroup);
    }

    /**
     * Adds favorite.
     */
    @Command
    @SmartNotifyChange("favorite")
    public void addFavorite() {
        favoritesService.addFavorite(clientGroup);
        favoritesChecker = null;
    }

    /**
     * Removes favorite.
     */
    @Command
    @SmartNotifyChange("favorite")
    public void removeFavorite() {
        favoritesService.removeFavorite(clientGroup);
        favoritesChecker = null;
    }

    /**
     * Returns calculation mode.
     * @return calculation mode
     */
    public boolean isCalcMode() {
        return calcMode;
    }

    /**
     * Returns clients list title.
     * @return clients list title
     */
    public String getClientsListTitle() {
        return String.format("%s (%d)", Labels.getLabel("client_group_clients"), (clients == null) ? 0 : clients.size());
    }

    /**
     * Returns page size.
     * @return page size
     */
    public Integer getPageSize() {
        return pageSize;
    }

}
