package ru.masterdm.crs.web.model;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Default;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import ru.masterdm.crs.domain.FavoritesChecker;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.Role;
import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.criteria.Conjunction;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.criteria.Where;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.ClientAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientGroupAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientTypeAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.FavoritesAttributeMeta;
import ru.masterdm.crs.exception.TooShortSearchStringException;
import ru.masterdm.crs.service.ClientService;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.FavoritesService;
import ru.masterdm.crs.service.SecurityService;
import ru.masterdm.crs.service.UserRoleService;
import ru.masterdm.crs.web.domain.UserProfile;
import ru.masterdm.crs.web.domain.entity.EntityStatus;

/**
 * Search result view model.
 * @author Alexey Kirilchev
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SearchResultViewModel {

    @WireVariable("config")
    protected Properties config;
    @WireVariable("pages")
    protected Properties pages;
    @WireVariable
    protected EntityService entityService;
    @WireVariable
    protected EntityMetaService entityMetaService;
    @WireVariable
    protected SecurityService securityService;
    @WireVariable
    protected FavoritesService favoritesService;
    @WireVariable("userProfile")
    protected UserProfile userProfile;
    @WireVariable("webConfig")
    protected Properties webConfig;
    @WireVariable
    protected ClientService clientService;
    @WireVariable("roleService")
    protected UserRoleService userRoleService;

    protected EntityMeta clientEntityMeta;
    protected EntityMeta clientGroupEntityMeta;
    protected EntityMeta userEntityMeta;
    protected EntityMeta clientTypeEntityMeta;
    protected List<EntityStatus> clients;
    protected List<EntityStatus> clientGroups;
    protected List<EntityStatus> users;
    protected String searchString;
    protected String searchCategory;

    protected Entity favorites;

    protected int pageSize;
    protected int searchResultPageSize;

    protected FavoritesChecker favoritesChecker;

    protected int clientActivePage = 0;
    protected long clientTotalSize;
    protected int clientGroupActivePage = 0;
    protected long clientGroupTotalSize;
    protected int userActivePage = 0;
    protected long userTotalSize;

    /**
     * ViewModel initialization method.
     * @param searchString search string
     */
    @Init
    public void init(@BindingParam("searchString") String searchString) {
        pageSize = Integer.parseInt(webConfig.getProperty("pageSize"));
        searchResultPageSize = Integer.parseInt(webConfig.getProperty("searchResultPageSize"));
        this.searchString = searchString;
        this.searchCategory = StringUtils.EMPTY;
        getClients();
        getClientGroups();
        getUsers();
    }

    /**
     * Navigates client.
     * @param client Client
     * @param mode mode
     */
    @Command
    public void navigateClient(@BindingParam("client") Entity client, @BindingParam("mode") @Default("false") Boolean mode) {
        Executions.getCurrent().setAttribute("client", client);
        Executions.getCurrent().setAttribute("key", client.getKey());
        Executions.getCurrent().setAttribute("mode", mode);
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "entity.client");
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Navigates group.
     * @param clientGroup Group
     * @param mode mode
     */
    @Command
    public void navigateClientGroup(@BindingParam("clientGroup") Entity clientGroup, @BindingParam("mode") @Default("false") Boolean mode) {
        Executions.getCurrent().setAttribute("clientGroup", clientGroup);
        Executions.getCurrent().setAttribute("key", clientGroup.getKey());
        Executions.getCurrent().setAttribute("mode", mode);
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "entity.client_group");
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Returns client entity meta.
     * @return client entity meta
     */
    public EntityMeta getClientEntityMeta() {
        if (clientEntityMeta == null) {
            clientEntityMeta = entityMetaService.getEntityMetaByKey(ClientAttributeMeta.METADATA_KEY, null);
        }
        return clientEntityMeta;
    }

    /**
     * Returns client group entity meta.
     * @return client group entity meta
     */
    public EntityMeta getClientGroupEntityMeta() {
        if (clientGroupEntityMeta == null) {
            clientGroupEntityMeta = entityMetaService.getEntityMetaByKey(ClientGroupAttributeMeta.METADATA_KEY, null);
        }
        return clientGroupEntityMeta;
    }

    /**
     * Returns user entity meta.
     * @return user entity meta
     */
    public EntityMeta getUserEntityMeta() {
        if (userEntityMeta == null) {
            userEntityMeta = entityMetaService.getEntityMetaByKey(User.METADATA_KEY, null);
        }
        return userEntityMeta;
    }

    /**
     * Returns client type entity meta.
     * @return client type entity meta
     */
    public EntityMeta getClientTypeEntityMeta() {
        if (clientTypeEntityMeta == null) {
            clientTypeEntityMeta = entityMetaService.getEntityMetaByKey(ClientTypeAttributeMeta.METADATA_KEY, null);
        }
        return clientTypeEntityMeta;
    }

    /**
     * Returns clients.
     * @return clients
     */
    public List<EntityStatus> getClients() {
        if (clients == null) {
            RowRange rowRange = RowRange.newAsPageAndSize(clientActivePage, getPageSize());
            if (searchString == null || searchString.trim().isEmpty() || searchString.equals("%")) {
                Criteria criteria = new Criteria();
                if (getFavoritesMode()) {
                    List<Long> favoritesEntitiesHubIds = getFavoritesEntitiesHubIds(FavoritesAttributeMeta.CLIENT.getKey());
                    if (favoritesEntitiesHubIds.isEmpty())
                        return new ArrayList<>();
                    criteria.setHubIds(favoritesEntitiesHubIds);
                }
                clients = entityService.getEntities(getClientEntityMeta(), criteria, rowRange, clientService.getClientsEntityRequestLdts())
                                       .stream().map(entity -> {
                            return new EntityStatus(entity, false);
                        }).collect(Collectors.toList());
                clientTotalSize = rowRange.getTotalCount();
            } else {
                LocalDateTime clientLdts = clientService.getClientsEntityRequestLdts();
                List<Long> clientHubIds = null;
                try {
                    List<Long> favoritesEntitiesHubIds = getFavoritesEntitiesHubIds(FavoritesAttributeMeta.CLIENT.getKey());
                    clientHubIds = clientService.getClientIdsBySearchString(searchString, rowRange, favoritesEntitiesHubIds);
                } catch (TooShortSearchStringException e) {
                    return Collections.emptyList();
                }
                clientTotalSize = rowRange.getTotalCount();
                if (clientHubIds.isEmpty()) {
                    clients = Collections.emptyList();
                } else {
                    Criteria criteria = new Criteria();
                    criteria.setResultCache(true);
                    criteria.setHubIds(clientHubIds);
                    clients = entityService.getEntities(getClientEntityMeta(), criteria, null, clientLdts)
                                           .stream().map(entity -> {
                                return new EntityStatus(entity, false);
                            }).collect(Collectors.toList());
                }
            }
        }
        return clients;
    }

    /**
     * Returns client groups.
     * @return client groups
     */
    public List<EntityStatus> getClientGroups() {
        if (clientGroups == null) {
            RowRange rowRange = RowRange.newAsPageAndSize(clientGroupActivePage, getPageSize());
            if (searchString == null || searchString.trim().isEmpty() || searchString.equals("%")) {
                Criteria criteria = new Criteria();
                if (getFavoritesMode()) {
                    List<Long> favoritesEntitiesHubIds = getFavoritesEntitiesHubIds(FavoritesAttributeMeta.CLIENT_GROUP.getKey());
                    if (favoritesEntitiesHubIds.isEmpty())
                        return new ArrayList<>();
                    criteria.setHubIds(favoritesEntitiesHubIds);
                }
                clientGroups = entityService.getEntities(getClientGroupEntityMeta(), criteria, rowRange, null).stream().map(entity -> {
                    return new EntityStatus(entity, false);
                }).collect(Collectors.toList());
                clientGroupTotalSize = rowRange.getTotalCount();
            } else {
                LocalDateTime clientLdts = clientService.getClientsEntityRequestLdts();
                List<Long> clientGroupHubIds = null;
                try {
                    List<Long> favoritesEntitiesHubIds = getFavoritesEntitiesHubIds(FavoritesAttributeMeta.CLIENT_GROUP.getKey());
                    clientGroupHubIds = clientService.getClientGroupIdsBySearchString(searchString, rowRange, favoritesEntitiesHubIds);
                } catch (TooShortSearchStringException e) {
                    return Collections.emptyList();
                }
                clientGroupTotalSize = rowRange.getTotalCount();
                if (clientGroupHubIds.isEmpty()) {
                    clientGroups = Collections.emptyList();
                } else {
                    Criteria criteria = new Criteria();
                    criteria.setResultCache(true);
                    criteria.setHubIds(clientGroupHubIds);
                    clientGroups = entityService.getEntities(getClientGroupEntityMeta(), criteria, null, clientLdts)
                                                .stream().map(entity -> {
                                return new EntityStatus(entity, false);
                            }).collect(Collectors.toList());
                }
            }
        }
        return clientGroups;
    }

    /**
     * Returns client groups.
     * @return client groups
     */
    public List<EntityStatus> getUsers() {
        if (users == null) {
            String searchValue = (searchString != null ? searchString : "%");
            searchValue = (!searchValue.contains("%")) ? ("%" + this.searchString.trim() + "%") : searchValue.trim();
            RowRange rowRange = RowRange.newAsPageAndSize(userActivePage, getPageSize());
            Criteria criteria = new Criteria();
            if (getFavoritesMode()) {
                List<Long> favoritesEntitiesHubIds = getFavoritesEntitiesHubIds(FavoritesAttributeMeta.USER.getKey());
                if (favoritesEntitiesHubIds.isEmpty())
                    return new ArrayList<>();
                criteria.setHubIds(favoritesEntitiesHubIds);
            }
            Where where = criteria.getWhere();
            where.addItem(new WhereItem(getUserEntityMeta().getKeyAttribute(),
                                        Operator.LIKE, searchValue));
            where.addItem(new WhereItem(Conjunction.OR, getUserEntityMeta().getAttributeMetadata(User.UserAttributeMeta.SURNAME.getKey()),
                                        Operator.LIKE, searchValue));
            where.addItem(new WhereItem(Conjunction.OR, getUserEntityMeta().getAttributeMetadata(User.UserAttributeMeta.NAME.getKey()),
                                        Operator.LIKE, searchValue));
            where.addItem(new WhereItem(Conjunction.OR, getUserEntityMeta().getAttributeMetadata(User.UserAttributeMeta.PATRONYMIC.getKey()),
                                        Operator.LIKE, searchValue));
            users = userRoleService.getUsers(criteria, rowRange, null).stream().map(entity -> {
                return new EntityStatus(entity, false);
            }).collect(Collectors.toList());
            userTotalSize = rowRange.getTotalCount();
        }
        return users;
    }

    /**
     * Returns favorites entities hub identifiers.
     * @param favoritesAttributeKey favorites attribute key
     * @return favorites entities hub identifiers. Returns null if not favorites view else returns favorites hub identifiers
     */
    protected List<Long> getFavoritesEntitiesHubIds(String favoritesAttributeKey) {
        return null;
    }

    /**
     * Returns search string.
     * @return search string
     */
    public String getSearchString() {
        return searchString;
    }

    /**
     * Sets search string.
     * @param searchString search string
     */
    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    /**
     * Searches.
     */
    @Command
    public void search() {
        refreshResults();
        searchCategory = StringUtils.EMPTY;
    }

    /**
     * Refreshes results.
     */
    private void refreshResults() {
        clients = null;
        clientGroups = null;
        users = null;
        clientActivePage = 0;
        clientGroupActivePage = 0;
        userActivePage = 0;
        BindUtils.postNotifyChange(null, null, this, "clients");
        BindUtils.postNotifyChange(null, null, this, "clientGroups");
        BindUtils.postNotifyChange(null, null, this, "users");
        BindUtils.postNotifyChange(null, null, this, "clientTotalSize");
        BindUtils.postNotifyChange(null, null, this, "clientGroupTotalSize");
        BindUtils.postNotifyChange(null, null, this, "userTotalSize");
        BindUtils.postNotifyChange(null, null, this, "clientsTitle");
        BindUtils.postNotifyChange(null, null, this, "clientGroupsTitle");
        BindUtils.postNotifyChange(null, null, this, "usersTitle");
        BindUtils.postNotifyChange(null, null, this, "allTitle");
        BindUtils.postNotifyChange(null, null, this, "foundTitle");
    }

    /**
     * Returns clients title.
     * @return clients title
     */
    public String getClientsTitle() {
        return MessageFormat.format(Labels.getLabel("search_result_clients"), getClientTotalSize());
    }

    /**
     * Returns client groups title.
     * @return client groups title
     */
    public String getClientGroupsTitle() {
        return MessageFormat.format(Labels.getLabel("search_result_client_groups"), getClientGroupTotalSize());
    }

    /**
     * Returns client groups title.
     * @return client groups title
     */
    public String getUsersTitle() {
        return MessageFormat.format(Labels.getLabel("search_result_users"), getUserTotalSize());
    }

    /**
     * Returns All title.
     * @return All title
     */
    public String getAllTitle() {
        return MessageFormat.format(Labels.getLabel("search_result_all"),
                                    getClientTotalSize() + getClientGroupTotalSize() + getUserTotalSize());
    }

    /**
     * Returns Found title.
     * @return Found title
     */
    public String getFoundTitle() {
        return MessageFormat.format(Labels.getLabel("search_result_found"),
                                    getClientTotalSize() + getClientGroupTotalSize() + getUserTotalSize());
    }

    /**
     * Sets single category in search results.
     * @param category category
     */
    @Command
    @SmartNotifyChange("*")
    public void setSearchCategory(@BindingParam("category") String category) {
        searchCategory = category;
        clients = null;
        clientGroups = null;
        users = null;
        clientActivePage = 0;
        clientGroupActivePage = 0;
        userActivePage = 0;
    }

    /**
     * Gets search category.
     * @return search category
     */
    public String getSearchCategory() {
        return searchCategory;
    }

    /**
     * Returns true if entity added in favorites, false otherwise.
     * @param entity entity
     * @return true if entity added in favorites, false otherwise
     */
    public boolean isFavorite(Entity entity) {
        return getFavoritesChecker().isFavorite(entity);
    }

    /**
     * Returns favorites checker.
     * @return favorites checker
     */
    protected FavoritesChecker getFavoritesChecker() {
        if (favoritesChecker == null)
            favoritesChecker = new FavoritesChecker(getFavorites());
        return favoritesChecker;
    }

    /**
     * Returns favorites.
     * @return favorites
     */
    protected Entity getFavorites() {
        if (favorites == null) {
            favorites = favoritesService.getFavorites();
        }
        return favorites;
    }

    /**
     * Adds favorite.
     * @param entityStatus entity status
     */
    @Command
    public void addFavorite(@BindingParam("entityStatus") EntityStatus entityStatus) {
        favoritesService.addFavorite(entityStatus.getEntity(), getFavoritesChecker());
        BindUtils.postNotifyChange(null, null, entityStatus, "entity");
    }

    /**
     * Removes favorite.
     * @param entityStatus entity status
     */
    @Command
    public void removeFavorite(@BindingParam("entityStatus") EntityStatus entityStatus) {
        favoritesService.removeFavorite(entityStatus.getEntity(), getFavoritesChecker());
        BindUtils.postNotifyChange(null, null, entityStatus, "entity");
    }

    /**
     * Returns true if favorites mode, false otherwise.
     * @return true if favorites mode, false otherwise
     */
    public Boolean getFavoritesMode() {
        return false;
    }

    /**
     * Returns client total size.
     * @return client total size
     */
    public long getClientTotalSize() {
        return clientTotalSize;
    }

    /**
     * Returns client group total size.
     * @return client group total size
     */
    public long getClientGroupTotalSize() {
        return clientGroupTotalSize;
    }

    /**
     * Returns user total size.
     * @return user total size
     */
    public long getUserTotalSize() {
        return userTotalSize;
    }

    /**
     * Returns client active page.
     * @return client active page
     */
    public int getClientActivePage() {
        return clientActivePage;
    }

    /**
     * Sets client active page.
     * @param clientActivePage client active page
     */
    @SmartNotifyChange("*")
    public void setClientActivePage(int clientActivePage) {
        this.clientActivePage = clientActivePage;
        clients = null;
    }

    /**
     * Returns client group active page.
     * @return client group active page
     */
    public int getClientGroupActivePage() {
        return clientGroupActivePage;
    }

    /**
     * Sets client group active page.
     * @param clientGroupActivePage client group active page
     */
    @SmartNotifyChange("*")
    public void setClientGroupActivePage(int clientGroupActivePage) {
        this.clientGroupActivePage = clientGroupActivePage;
        clientGroups = null;
    }

    /**
     * Returns user active page.
     * @return user active page
     */
    public int getUserActivePage() {
        return userActivePage;
    }

    /**
     * Sets user active page.
     * @param userActivePage user active page
     */
    @SmartNotifyChange("*")
    public void setUserActivePage(int userActivePage) {
        this.userActivePage = userActivePage;
        users = null;
    }

    /**
     * Returns page size.
     * @return page size
     */
    public int getPageSize() {
        if (searchCategory.equals(StringUtils.EMPTY)) {
            return searchResultPageSize;
        }
        return pageSize;
    }

    /**
     * Navigates user's calculations.
     * @param user USer
     */
    @Command
    public void navigateUser(@BindingParam("user") User user) {
        Executions.getCurrent().setAttribute("entity", user);
        Map<String, Object> map = new HashMap<>();
        map.put("targetPage", "calc.calculations");
        BindUtils.postGlobalCommand(null, null, "navigateGlobal", map);
    }

    /**
     * Concatenates Roles.
     * @param roles List of roles
     * @return concatenated Roles
     */
    public String getRolesName(List<Role> roles) {
        return roles.stream()
                    .map(entity -> entity.getName().getDescription(userProfile.getLocale()))
                    .collect(Collectors.joining(", "));
    }
}
