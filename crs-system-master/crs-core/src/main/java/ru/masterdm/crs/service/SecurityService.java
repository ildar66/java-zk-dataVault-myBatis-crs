package ru.masterdm.crs.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import ru.masterdm.crs.domain.entity.BusinessAction;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.exception.CrsException;

/**
 * Security service.
 * @author Sergey Valiev
 */
public interface SecurityService {

    /**
     * Returns current user.
     * @return current user
     */
    User getCurrentUser();

    /**
     * Detect if user has right to execute action on entity meta.
     * @param user user object
     * @param entityMeta entity metadata
     * @param action action
     * @return true if user has right
     */
    Boolean isPermitted(User user, EntityMeta entityMeta, BusinessAction.Action action);

    /**
     * Detect if user has right to execute action on entity meta.
     * @param user user object
     * @param entityMetaKey entity metadata key
     * @param action action
     * @return true if user has right
     */
    Boolean isPermitted(User user, String entityMetaKey, BusinessAction.Action action);

    /**
     * Detect if user has right to execute action on entity type.
     * @param user user object
     * @param entityType entity type
     * @param action action
     * @return true if user has right
     */
    Boolean isPermitted(User user, EntityType entityType, BusinessAction.Action action);

    /**
     * Detect if user has right to execute action on entity type.
     * @param user user object
     * @param entityType entity type as {@link Entity}
     * @param action action
     * @return true if user has right
     */
    Boolean isPermittedForEntityType(User user, Entity entityType, BusinessAction.Action action);

    /**
     * Get allowed business actions for entity.
     * If allowing defined for entity type it returned first.
     * @param entityMeta entity meta
     * @param ldts load datetime
     * @return list of actions
     */
    List<BusinessAction> getEntityMetaBusinessActions(EntityMeta entityMeta, LocalDateTime ldts);

    /**
     * Get allowed business actions for entity type.
     * @param entityType entity type
     * @param ldts load datetime
     * @return list of actions
     */
    List<BusinessAction> getEntityTypeBusinessActions(Entity entityType, LocalDateTime ldts);

    /**
     * Get allowed business actions for entity type.
     * @param entityType entity type
     * @param ldts load datetime
     * @return list of actions
     */
    List<BusinessAction> getEntityTypeBusinessActions(EntityType entityType, LocalDateTime ldts);

    /**
     * Get allowed business actions for concrete entity object.
     * @param entity entity object
     * @param ldts load datetime
     * @return list of actions
     */
    List<BusinessAction> getEntityBusinessActions(Entity entity, LocalDateTime ldts);

    /**
     * Returns current user login.
     * @return current user login
     */
    default String getLogin() {
        String login = SecurityContextHolder.getContext().getAuthentication().getName();
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        return login != null ? login.trim().toUpperCase() : null;
    }

    /**
     * Define security context.
     * @param login user login
     */
    default void defineSecurityContext(String login) {
        if (login == null) {
            throw new CrsException("login is null");
        }
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(login));
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(login, login, authorities);

        Authentication authentication = new AnonymousAuthenticationToken(login, userDetails, authorities);
        authentication.setAuthenticated(true);

        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * Mark changes of access rights for type of entities.
     * @param forEntityMetaKey entity meta key to mark
     * @param entity reason to change the tag
     */
    void pendingSecureChange(String forEntityMetaKey, Entity entity);

    /**
     * Run rebuilds of security tags.
     */
    void rebuildAllSecurity();
}
