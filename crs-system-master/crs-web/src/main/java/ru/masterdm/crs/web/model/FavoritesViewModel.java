package ru.masterdm.crs.web.model;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.select.annotation.VariableResolver;

import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;

/**
 * Favorites view model.
 * @author Alexey Kirilchev
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class FavoritesViewModel extends SearchResultViewModel {

    /**
     * ViewModel initialization method.
     * @param searchString search string
     */
    @Init
    @Override
    public void init(@BindingParam("searchString") String searchString) {
        pageSize = Integer.parseInt(webConfig.getProperty("pageSize"));
        searchResultPageSize = Integer.parseInt(webConfig.getProperty("searchResultPageSize"));
        this.searchString = searchString;
        this.searchCategory = StringUtils.EMPTY;
        getClients();
        getClientGroups();
        getUsers();
    }

    @Override
    protected List<Long> getFavoritesEntitiesHubIds(String favoritesAttributeKey) {
        return ((LinkedEntityAttribute<Entity>) getFavorites().getAttribute(favoritesAttributeKey))
                .getEntityList().stream().map(Entity::getHubId).collect(Collectors.toList());
    }

    @Override
    public Boolean getFavoritesMode() {
        return true;
    }

}
