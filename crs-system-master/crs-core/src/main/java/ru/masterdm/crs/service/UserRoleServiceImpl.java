package ru.masterdm.crs.service;

import static ru.masterdm.crs.domain.entity.Permission.PermissionAttributeMeta;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import ru.masterdm.crs.dao.entity.meta.MetadataDao;
import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.entity.AbstractDvEntity;
import ru.masterdm.crs.domain.entity.BusinessAction;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.Permission;
import ru.masterdm.crs.domain.entity.Role;
import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.criteria.Conjunction;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.RowRange;
import ru.masterdm.crs.domain.entity.criteria.Where;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.domain.entity.meta.EntityTypeAttributeMeta;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.util.annotation.CurrentTimeStamp;

/**
 * Operations with user's roles.
 * @author Pavel Masalov
 */
@Validated
@Service("roleService")
public class UserRoleServiceImpl implements UserRoleService {

    @Autowired
    private EntityService entityService;
    @Autowired
    private EntityMetaService entityMetaService;
    @Autowired
    private SecurityService securityService;
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private MetadataDao metadataDao;

    @Override
    public List<User> getUsers(Criteria criteria, RowRange rowRange, LocalDateTime ldts) {
        return (List<User>) entityService.getEntities(entityMetaService.getEntityMetaByKey(User.METADATA_KEY, ldts), criteria, rowRange, ldts);
    }

    @Override
    @Cacheable(cacheNames = "user-entity", key = "#p0")
    public User getUser(@NotNull String key) {
        LocalDateTime ldts = metadataDao.getSysTimestamp();
        EntityMeta userMeta = entityMetaService.getEntityMetaByKey(User.METADATA_KEY, null);
        Criteria criteria = new Criteria();
        criteria.getWhere().addItem(new WhereItem(userMeta.getKeyAttribute(), Operator.EQ, key));
        List<User> userList = getUsers(criteria, null, ldts);
        if (userList.isEmpty())
            return null;
        return userList.get(0);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "entity-meta-action-user-permission", allEntries = true),
            @CacheEvict(cacheNames = "entity-type-action-user-permission", allEntries = true),
            @CacheEvict(cacheNames = "user-entity", key = "#p0.key")
    })
    public void persistUser(User user) {
        entityService.persistEntity(user);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "entity-meta-action-user-permission", allEntries = true),
            @CacheEvict(cacheNames = "entity-user-action-user-permission", allEntries = true),
            @CacheEvict(cacheNames = "user-entity", key = "#p0.key")
    })
    public void removeUser(User user) {
        entityService.removeEntity(user);
    }

    @Override
    public List<Role> getRoles(Criteria criteria, RowRange rowRange, @CurrentTimeStamp LocalDateTime ldts) {
        return (List<Role>) entityService.getEntities(entityMetaService.getEntityMetaByKey(Role.METADATA_KEY, ldts), criteria, rowRange, ldts);
    }

    @Override
    @Transactional
    public void persistRole(@NotNull Role role) {
        if (role.isEmbedded()) {
            if (role.getHubId() == null)
                throw new CrsException("Can't create new embedded role");
            throw new CrsException("Can't save embedded role");
        }
        entityService.persistEntity(role);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(cacheNames = "entity-meta-role-permission", allEntries = true),
                    @CacheEvict(cacheNames = "entity-type-role-permission", allEntries = true),
                    @CacheEvict(cacheNames = "entity-meta-action-user-permission", allEntries = true),
                    @CacheEvict(cacheNames = "entity-type-action-user-permission", allEntries = true),
                    @CacheEvict(cacheNames = "user-entity", allEntries = true)
            }
    )
    public void removeRole(@NotNull Role role) {
        if (role.isEmbedded()) {
            throw new CrsException("Can't remove embedded role");
        }
        entityService.removeEntity(role);
    }

    @Override
    @Cacheable(cacheNames = "entity-meta-role-permission", key = "#p0.key + #p1.key")
    public List<Permission> getEntityMetaPermissions(@NotNull EntityMeta entityMeta, @NotNull Role role, @CurrentTimeStamp LocalDateTime ldts) {
        return getEntityMetaPermissionsNoCache(entityMeta, role, ldts);
    }

    @Override
    @Cacheable(cacheNames = "entity-meta-role-permission", key = "#p0 + #p1.key")
    public List<Permission> getEntityMetaPermissions(@NotNull String entityMetaKey, @NotNull Role role, @CurrentTimeStamp LocalDateTime ldts) {
        return getEntityMetaPermissionsNoCache(entityMetaKey, role, ldts);
    }

    @Override
    @Cacheable(cacheNames = "entity-type-role-permission", key = "#p0 + #p1.key")
    public List<Permission> getEntityTypePermissions(EntityType entityType, Role role, LocalDateTime ldts) {
        return getEntityTypePermissionsNoCache(entityType, role, ldts);
    }

    @Override
    @Cacheable(cacheNames = "entity-type-role-permission", key = "#p0.key + #p1.key")
    public List<Permission> getEntityTypePermissions(Entity entityType, Role role, LocalDateTime ldts) {
        return getEntityTypePermissions(EntityType.valueOf(entityType.getKey()), role, ldts);
    }

    @Override
    public List<Permission> getEntityMetaPermissionsNoCache(@NotNull EntityMeta entityMeta, @NotNull Role role,
                                                            @CurrentTimeStamp LocalDateTime ldts) {
        return getEntityMetaPermissionsNoCache(entityMeta.getKey(), role, ldts);
    }

    @Override
    public List<Permission> getEntityMetaPermissionsNoCache(@NotNull String entityMetaKey, @NotNull Role role,
                                                            @CurrentTimeStamp LocalDateTime ldts) {
        EntityMeta permissionMeta = entityMetaService.getEntityMetaByKey(Permission.METADATA_KEY, ldts);
        EntityMeta entityEntityMeta = entityMetaService.getEntityMetaByKey(EntityMeta.METADATA_KEY, ldts);
        Criteria criteria = new Criteria();
        Where where = criteria.getWhere();
        where.addReferenceItem(permissionMeta.getAttributeMetadata(PermissionAttributeMeta.ENTITY.getKey()),
                               new WhereItem(entityEntityMeta.getKeyAttribute(), Operator.EQ, entityMetaKey));

        return loadPermissionsForNoCache(permissionMeta, criteria, role, ldts);
    }

    @Override
    public List<Permission> getEntityPermissionsNoCache(@NotNull Entity entity, @NotNull Role role, @CurrentTimeStamp LocalDateTime ldts) {
        EntityMeta permissionMeta = entityMetaService.getEntityMetaByKey(Permission.METADATA_KEY, ldts);
        Criteria criteria = new Criteria();
        criteria.addReferencedEntity(entity);

        return loadPermissionsForNoCache(permissionMeta, criteria, role, ldts);
    }

    @Override
    public List<Permission> getEntityTypePermissionsNoCache(@NotNull Entity entityType, @NotNull Role role, @CurrentTimeStamp LocalDateTime ldts) {
        return getEntityTypePermissionsNoCache(EntityType.valueOf(entityType.getKey()), role, ldts);
    }

    @Override
    public List<Permission> getEntityTypePermissionsNoCache(@NotNull EntityType entityType, @NotNull Role role,
                                                            @CurrentTimeStamp LocalDateTime ldts) {
        EntityMeta permissionMeta = entityMetaService.getEntityMetaByKey(Permission.METADATA_KEY, ldts);
        EntityMeta entityTypeEntityMeta = entityMetaService.getEntityMetaByKey(EntityTypeAttributeMeta.METADATA_KEY, ldts);
        Criteria criteria = new Criteria();
        Where where = criteria.getWhere();
        where.addReferenceItem(permissionMeta.getAttributeMetadata(PermissionAttributeMeta.ENTITY_TYPE.getKey()),
                               new WhereItem(entityTypeEntityMeta.getKeyAttribute(), Operator.EQ, entityType.name()));

        return loadPermissionsForNoCache(permissionMeta, criteria, role, ldts);
    }

    /**
     * Complete permission loading.
     * Utility method
     * @param permissionMeta permission object mets
     * @param criteria filter criteria
     * @param role role
     * @param ldts load datetime
     * @return list of permissions
     */
    private List<Permission> loadPermissionsForNoCache(EntityMeta permissionMeta, Criteria criteria, Role role, LocalDateTime ldts) {
        criteria.addReferencedEntity(role);
        criteria.getWhere()
                .setReferenceExists(permissionMeta.getAttributeMetadata(PermissionAttributeMeta.BUSINESS_ACTION.getKey()), Conjunction.AND);
        List<Permission> permissions = (List<Permission>) entityService.getEntities(permissionMeta, criteria, null, ldts);
        for (Permission p : permissions) {
            replaceEntityMetaByObject(p, ldts);
        }
        return permissions;
    }

    @Override
    @Transactional
    @Caching(evict = {@CacheEvict(cacheNames = "entity-meta-role-permission", allEntries = true),
                      @CacheEvict(cacheNames = "entity-type-role-permission", allEntries = true),
                      @CacheEvict(cacheNames = "entity-meta-action-user-permission", allEntries = true),
                      @CacheEvict(cacheNames = "entity-type-action-user-permission", allEntries = true)})
    public void persistsRolePermissions(@NotNull Collection<Permission> permissions) {
        LocalDateTime ldts = metadataDao.getSysTimestamp();

        for (Permission permission : permissions) {
            if (permission.getEntityMeta() == null && permission.getEntityType() == null && permission.getModel() == null)
                throw new CrsException("One of the entity, entitytype or model should be defined for permission");
            if (permission.getRole() == null)
                throw new CrsException("Role should be defined for permission");
            if (permission.getRole().isEmbedded())
                throw new CrsException("Can't change permissions for embedded role");
            if (permission.getAction() == null)
                throw new CrsException("Action should be defined for permission");

            if ((permission.getEntityMeta() != null && permission.getEntityType() != null)
                || (permission.getEntityMeta() != null && permission.getModel() != null)
                || (permission.getEntityType() != null && permission.getModel() != null))
                throw new CrsException("Only one of entity meta, entity type or model may be defined for permission");

            // TODO get old permition by entityt0ype and model as well
            if (permission.getHubId() == null) {
                // may be same permission already defined at database
                Permission oldPermission = null;
                if (permission.getEntityType() != null) {
                    oldPermission = getOldPermissionForEntityType(permission.getEntityType(), permission.getRole(), permission.getAction(), ldts);
                } else if (permission.getEntityMeta() != null) {
                    oldPermission = getOldPermissionForEntityMeta(permission.getEntityMeta(), permission.getRole(), permission.getAction(), ldts);
                }

                if (oldPermission == null) {
                    entityService.persistEntity(permission, ldts);
                } else {
                    oldPermission.setPermit(permission.isPermit());
                    entityService.persistEntity(oldPermission, ldts);
                    permission.setSystemProperty(oldPermission);
                }
            } else {
                entityService.persistEntity(permission, ldts);
            }
        }

        permissions.stream().map(p -> p.getRole()).distinct().forEach(role -> securityService.pendingSecureChange(Calculation.METADATA_KEY, role));
    }

    /**
     * Get existed permission from database by entity meta, role and action.
     * @param entityMeta entity meta
     * @param role role
     * @param businessAction business action
     * @param ldts load datetime
     * @return permission if exists else null
     */
    private Permission getOldPermissionForEntityMeta(EntityMeta entityMeta, Role role, BusinessAction businessAction, LocalDateTime ldts) {
        EntityMeta permissionMeta = entityMetaService.getEntityMetaByKey(Permission.METADATA_KEY, ldts);
        EntityMeta entityEntityMeta = entityMetaService.getEntityMetaByKey(EntityMeta.METADATA_KEY, ldts);

        Criteria criteria = new Criteria();
        Where where = criteria.getWhere();
        where.addReferenceItem(permissionMeta.getAttributeMetadata(PermissionAttributeMeta.ENTITY.getKey()),
                               new WhereItem(entityEntityMeta.getKeyAttribute(), Operator.EQ, entityMeta.getKey()));
        return getPermissionCompleteDo(role, businessAction, permissionMeta, criteria, ldts);
    }

    /**
     * Get existed permission from database by entity type, role and action.
     * @param entityType entity type represented by {@link Entity}
     * @param role role
     * @param businessAction business action
     * @param ldts load datetime
     * @return permission if exists else null
     */
    private Permission getOldPermissionForEntityType(Entity entityType, Role role, BusinessAction businessAction, LocalDateTime ldts) {
        EntityMeta permissionMeta = entityMetaService.getEntityMetaByKey(Permission.METADATA_KEY, ldts);

        Criteria criteria = new Criteria();
        criteria.addReferencedEntity(entityType);
        return getPermissionCompleteDo(role, businessAction, permissionMeta, criteria, ldts);
    }

    /**
     * Complete operation to load existed permission.
     * @param role role
     * @param businessAction business action
     * @param permissionMeta permission metadata
     * @param criteria criteria
     * @param ldts load date time
     * @return permission object
     */
    private Permission getPermissionCompleteDo(Role role, BusinessAction businessAction, EntityMeta permissionMeta,
                                               Criteria criteria, LocalDateTime ldts) {
        criteria.addReferencedEntity(role);
        criteria.addReferencedEntity(businessAction);
        List<Permission> permissions = (List<Permission>) entityService.getEntities(permissionMeta, criteria, null, ldts);
        if (permissions.isEmpty())
            return null;

        Permission p = permissions.get(0);
        replaceEntityMetaByObject(p, ldts);
        return p;
    }

    /**
     * Replace loaded entity object by real entity meta object.
     * @param p permission
     * @param ldts load datetime
     */
    private void replaceEntityMetaByObject(Permission p, LocalDateTime ldts) {
        if (p.isAttributeExists(PermissionAttributeMeta.ENTITY.getKey())) {
            LinkedEntityAttribute<AbstractDvEntity> entityMetaLink = (LinkedEntityAttribute<AbstractDvEntity>) p
                    .getAttribute(PermissionAttributeMeta.ENTITY.getKey());
            // replace loaded meta as entity to real entity meta
            AbstractDvEntity ae = entityMetaLink.getEntityList().get(0);
            EntityMeta em = entityMetaService.getEntityMetaByKey(ae.getKey(), ldts);
            entityMetaLink.getEntityAttributeList().get(0).setEntity(em);
        }
    }

}
