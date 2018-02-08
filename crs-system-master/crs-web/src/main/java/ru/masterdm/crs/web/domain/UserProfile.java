package ru.masterdm.crs.web.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.zkoss.util.Locales;

import ru.masterdm.crs.domain.entity.meta.AttributeLocale;
import ru.masterdm.crs.service.SecurityService;
import ru.masterdm.crs.web.domain.entity.EntityFilter;

/**
 * User profile class.
 * @author Igor Matushak
 */
@Component
@Scope(WebApplicationContext.SCOPE_SESSION)
public class UserProfile {

    @Autowired
    private SecurityService securityService;

    private Map<String, List<EntityFilter>> filterMap;
    private Map<String, String> entityMetaFilterMap;

    /**
     * Initiates.
     */
    @PostConstruct
    private void init() {
        filterMap = new HashMap<>();
        entityMetaFilterMap = new HashMap<>();
    }

    /**
     * Returns filters by key.
     * @param key key
     * @return filters by key
     */
    public List<EntityFilter> getFiltersByKey(String key) {
        filterMap.putIfAbsent(key, new ArrayList<>());
        return filterMap.get(key);
    }

    /**
     * Sets filter by key.
     * @param key key
     * @param filters filters
     */
    public void setFiltersByKey(String key, List<EntityFilter> filters) {
        filterMap.put(key, filters);
    }

    /**
     * Returns entity meta filter by key.
     * @param key key
     * @return entity meta filter by key
     */
    public String getEntityMetaFilterByKey(String key) {
        entityMetaFilterMap.putIfAbsent(key, null);
        return entityMetaFilterMap.get(key);
    }

    /**
     * Sets entity meta filter by key.
     * @param key key
     * @param filter filter
     */
    public void setEntityMetaFilterByKey(String key, String filter) {
        entityMetaFilterMap.put(key, filter);
    }

    /**
     * Returns current locale.
     * @return current locale
     */
    public AttributeLocale getLocale() {
        return AttributeLocale.getLocale(Locales.getCurrent());
    }

    /**
     * Returns current user login.
     * @return current user login
     */
    public String getLogin() {
        return securityService.getLogin();
    }
}
