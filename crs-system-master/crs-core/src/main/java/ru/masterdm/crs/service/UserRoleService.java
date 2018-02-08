package ru.masterdm.crs.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.Permission;
import ru.masterdm.crs.domain.entity.Role;
import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;

/**
 * Operations with user's roles.
 * @author Pavel Masalov
 */
public interface UserRoleService {

    /**
     * Get list of users.
     * @param criteria filter object
     * @param rowRange paging object
     * @param ldts load datetime
     * @return list of users
     */
    List<User> getUsers(Criteria criteria, RowRange rowRange, LocalDateTime ldts);

    /**
     * Get user by key.
     * @param key user key
     * @return user object
     */
    User getUser(String key);

    /**
     * Save changes for user.
     * @param user user object
     */
    void persistUser(User user);

    /**
     * Remove user.
     * @param user user object
     */
    void removeUser(User user);

    /**
     * Get list of roles.
     * @param criteria filter criteria
     * @param rowRange paging object
     * @param ldts load datetime
     * @return list of roles
     */
    List<Role> getRoles(Criteria criteria, RowRange rowRange, LocalDateTime ldts);

    /**
     * Save new or changed role.
     * Forbidden to save {@link Role#embedded embedded roles}.
     * @param role role object
     */
    void persistRole(Role role);

    /**
     * Remove role.
     * Forbidden to remove {@link Role#embedded embedded roles}.
     * @param role role object
     */
    void removeRole(Role role);

    /**
     * Get list of permissions defined for entity and role.
     * Return empty list if no permission are defined.
     * Data are cached at "entity-meta-role-permission" cache.
     * @param entityMeta entity meta
     * @param role role
     * @param ldts load datetime
     * @return list of permissions
     */
    List<Permission> getEntityMetaPermissions(EntityMeta entityMeta, Role role, LocalDateTime ldts);

    /**
     * Get list of permissions defined for entity and role.
     * Return empty list if no permission are defined.
     * Data are cached at "entity-meta-role-permission" cache.
     * @param entityMetaKey entity meta key
     * @param role role
     * @param ldts load datetime
     * @return list of permissions
     */
    List<Permission> getEntityMetaPermissions(String entityMetaKey, Role role, LocalDateTime ldts);

    /**
     * Get list of entitytype's permission for role.
     * @param entityType entity type
     * @param role role
     * @param ldts load datetime
     * @return list of permissions for role and entity type
     */
    List<Permission> getEntityTypePermissions(EntityType entityType, Role role, LocalDateTime ldts);

    /**
     * Get list of entitytype's permission for role.
     * @param entityType entity type as entity
     * @param role role
     * @param ldts load datetime
     * @return list of permissions for role and entity type
     */
    List<Permission> getEntityTypePermissions(Entity entityType, Role role, LocalDateTime ldts);

    /**
     * Get list of permissions defined for entity meta and role.
     * Return empty list if no permission are defined.
     * Returns data directly from storage without caching.
     * This method must be used for setup utility.
     * @param entityMeta entity meta
     * @param role role
     * @param ldts load datetime
     * @return list of permissions
     */
    List<Permission> getEntityMetaPermissionsNoCache(EntityMeta entityMeta, Role role, LocalDateTime ldts);

    /**
     * Get list of permissions defined for entity meta and role.
     * Return empty list if no permission are defined.
     * Returns data directly from storage without caching.
     * This method must be used for setup utility.
     * @param entityMetaKey entity meta key
     * @param role role
     * @param ldts load datetime
     * @return list of permissions
     */
    List<Permission> getEntityMetaPermissionsNoCache(String entityMetaKey, Role role, LocalDateTime ldts);

    /**
     * Get list of permissions defined for entity and role.
     * Return empty list if no permission are defined.
     * Returns data directly from storage without caching.
     * This method must be used for setup utility.
     * @param entity instance of entity
     * @param role role
     * @param ldts load datetime
     * @return list of permission
     */
    List<Permission> getEntityPermissionsNoCache(Entity entity, Role role, LocalDateTime ldts);

    /**
     * Get list of permissions defined for entity type and role.
     * Return empty list if no permission are defined.
     * Returns data directly from storage without caching.
     * This method must be used for setup utility.
     * @param entityType entity type
     * @param role role
     * @param ldts load datetime
     * @return list of permission
     */
    List<Permission> getEntityTypePermissionsNoCache(Entity entityType, Role role, LocalDateTime ldts);

    /**
     * Get list of permissions defined for entity type and role.
     * Return empty list if no permission are defined.
     * Returns data directly from storage without caching.
     * This method must be used for setup utility.
     * @param entityType entity type as {@link EntityType enum}
     * @param role role
     * @param ldts load datetime
     * @return list of permission
     */
    List<Permission> getEntityTypePermissionsNoCache(EntityType entityType, Role role, LocalDateTime ldts);

    /**
     * Save permissions defined for meta and role.
     * @param permissions list of current permissions
     */
    void persistsRolePermissions(Collection<Permission> permissions);
}
