package ru.masterdm.crs.service.security;

import static ru.masterdm.crs.domain.entity.BusinessAction.Action;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.SecurityService;
import ru.masterdm.crs.service.UserRoleService;

/**
 * Plugable expression handler permission evaluator.
 * Check permission to make {@link ru.masterdm.crs.domain.entity.BusinessAction.Action Action} on
 * {@link ru.masterdm.crs.domain.entity.Entity entity concrete object}, {@link ru.masterdm.crs.domain.entity.meta.EntityMeta entity} or
 * {@link ru.masterdm.crs.domain.entity.meta.EntityType entity type}.
 * @author Pavel Masalov
 */
@Validated
@Service("authorizationEvaluatorService")
public class AuthorizationEvaluatorServiceImpl implements PermissionEvaluator {

    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private EntityMetaService entityMetaService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        // only entity are controlled for now
        if (targetDomainObject instanceof Entity) {
            return hasEntityPermission(getUser(authentication), (Entity) targetDomainObject, permission);
        } else if (targetDomainObject instanceof EntityMeta) {
            return hasEntityMetaPermission(getUser(authentication), (EntityMeta) targetDomainObject, permission);
        }
        return true;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        // if target is string this is string containing key of checked metadata
        // then replace it by real metadata
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey((String) targetType, null);
        return hasPermission(authentication, entityMeta, permission);
    }

    /**
     * Check permission on entity object level.
     * @param user current user
     * @param entity entity object
     * @param permission actions to check
     * @return true if permission granted
     */
    private boolean hasEntityPermission(User user, Entity entity, Object permission) {
        boolean permit = false;
        if (entity.getHubId() == null && hasAction(permission, Action.CREATE_NEW)) { // this is create new case
            return securityService.isPermitted(user, entity.getMeta(), Action.CREATE_NEW);
        } else {
            for (Action action : asList(permission)) {
                permit |= securityService.isPermitted(user, entity.getMeta(), action);
                if (permit)
                    return permit;
            }
        }
        return permit;
    }

    /**
     * Check permission on entity metadata level.
     * @param user current user
     * @param entityMeta entity metadata
     * @param permission actions to check
     * @return true if permission granted
     */
    private boolean hasEntityMetaPermission(User user, EntityMeta entityMeta, Object permission) {
        boolean permit = false;
        for (Action action : asList(permission)) {
            permit |= securityService.isPermitted(user, entityMeta, action);
            if (permit)
                return permit;
        }
        return permit;
    }

    /**
     * Check if permission object has permissions.
     * @param permission list or instance of {@link Action}
     * @param action action to find
     * @return true if list contains action of action is it
     */
    private boolean hasAction(Object permission, Action action) {
        if (permission instanceof Action) {
            return ((Action) permission) == action;
        } else { // is List<BusinessAction.Action>
            for (Action a : (List<Action>) permission) {
                if (a == action)
                    return true;
            }
        }
        return false;
    }

    /**
     * Represent actions as list.
     * @param permission actions
     * @return list of actions
     */
    private List<Action> asList(Object permission) {
        if (permission instanceof Action) {
            return Collections.singletonList((Action) permission);
        } else { // is List<BusinessAction.Action>
            return (List<Action>) permission;
        }
    }

    /**
     * Get user associated with auth.
     * @param authentication security authentication
     * @return user object
     */
    private User getUser(Authentication authentication) {
        String login = authentication.getName().trim().toUpperCase();
        User user = userRoleService.getUser(authentication.getName().trim().toUpperCase());
        if (user == null) {
            throw new CrsException("Can't find by login '" + login + "'");
        }
        return user;
    }
}
