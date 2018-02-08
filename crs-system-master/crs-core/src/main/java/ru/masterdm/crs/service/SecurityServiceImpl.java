package ru.masterdm.crs.service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import ru.masterdm.crs.dao.SecurityDao;
import ru.masterdm.crs.dao.entity.meta.MetadataDao;
import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.entity.BusinessAction;
import ru.masterdm.crs.domain.entity.Department;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.Permission;
import ru.masterdm.crs.domain.entity.Role;
import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.meta.ClientAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.domain.entity.meta.EntityTypeAttributeMeta;
import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.util.annotation.CurrentTimeStamp;

/**
 * Security service implementation.
 * @author Sergey Valiev
 */
@Validated
@Service("securityService")
public class SecurityServiceImpl implements SecurityService {

    @Autowired
    private EntityService entityService;
    @Autowired
    private EntityMetaService entityMetaService;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private MetadataDao metadataDao;
    @Autowired
    private SecurityDao securityDao;

    //TODO remove within (at finish) VTBCRS-612
    private static final String TEMPORALLY_FOR_TEST = "PERMISSION_TEST";

    @Override
    public User getCurrentUser() {
        String login = getLogin();
        User user = userRoleService.getUser(login);
        if (user == null) {
            throw new CrsException("Can't find by login '" + login + "'");
        }
        return user;
    }

    @Override
    @Cacheable(cacheNames = "entity-type-action-user-permission", key = "#p0.key + #p1 + #p2")
    public Boolean isPermitted(@NotNull User user, @NotNull EntityType entityType, @NotNull BusinessAction.Action action) {
        return Optional.ofNullable(isPermittedInternal(user, entityType, action, metadataDao.getSysTimestamp())).orElse(Boolean.FALSE);
    }

    @Override
    @Cacheable(cacheNames = "entity-type-action-user-permission", key = "#p0.key + #p1.key + #p2")
    public Boolean isPermittedForEntityType(@NotNull User user, @NotNull Entity entityType, @NotNull BusinessAction.Action action) {
        return isPermitted(user, EntityType.valueOf(entityType.getKey()), action);
    }

    @Override
    @Cacheable(cacheNames = "entity-meta-action-user-permission", key = "#p0.key + #p1.key + #p2")
    public Boolean isPermitted(@NotNull User user, @NotNull EntityMeta entityMeta, @NotNull BusinessAction.Action action) {
        // TODO check now only for CALC and CLIENT and test  remove it after VTBCRS-612
        if (!(entityMeta.getKey().equals(Calculation.METADATA_KEY) || entityMeta.getKey().equals(ClientAttributeMeta.METADATA_KEY)
              || entityMeta.getKey().equals(EntityMeta.METADATA_KEY)
              || entityMeta.getKey().equals(Model.METADATA_KEY)
              || entityMeta.getKey().equals(Formula.METADATA_KEY)
              || entityMeta.getKey().equals(FormTemplate.METADATA_KEY)
              || entityMeta.getKey().equals(TEMPORALLY_FOR_TEST)))
            return Boolean.TRUE;

        return Optional.ofNullable(isPermittedInternal(user, entityMeta, action, metadataDao.getSysTimestamp())).orElse(Boolean.FALSE);
    }

    @Override
    @Cacheable(cacheNames = "entity-meta-action-user-permission", key = "#p0.key + #p1 + #p2")
    public Boolean isPermitted(@NotNull User user, @NotNull String entityMetaKey, @NotNull BusinessAction.Action action) {
        EntityMeta entityMeta = entityMetaService.getEntityMetaByKey(entityMetaKey, null);
        return isPermitted(user, entityMeta, action);
    }

    /**
     * Internal implementation of permission detection for entity type.
     * @param user user
     * @param entityType entity type enum
     * @param action business action
     * @param ldts load datetime
     * @return permission flag, null if permission not existed
     */
    private Boolean isPermittedInternal(User user, EntityType entityType, BusinessAction.Action action, LocalDateTime ldts) {
        return consolidatedUserPermission(user, action, (r) -> userRoleService.getEntityTypePermissions(entityType, r, ldts));
    }

    /**
     * Internal implementation of permission detection for entity meta.
     * @param user user
     * @param entityMeta entity meta
     * @param action business action
     * @param ldts load datetime
     * @return permission flag, null if permission not existed
     */
    private Boolean isPermittedInternal(User user, EntityMeta entityMeta, BusinessAction.Action action, LocalDateTime ldts) {
        LocalDateTime ldts2 = ldts == null ? metadataDao.getSysTimestamp() : ldts;
        Boolean entityTypePermit = isPermittedInternal(user, entityMeta.getType(), action, ldts);
        Boolean permit = consolidatedUserPermission(user, action, (r) -> userRoleService.getEntityMetaPermissions(entityMeta, r, ldts2));

        if (permit != null)
            return permit;
        return entityTypePermit;
    }

    /**
     * Role's consolidated permission loader.
     * @param user user
     * @param action business action
     * @param rolePermissionProducer functor to get permission for role
     * @return collection of permission object
     */
    private Boolean consolidatedUserPermission(User user, BusinessAction.Action action,
                                               Function<Role, Collection<Permission>> rolePermissionProducer) {
        Boolean permit = null;
        for (Role role : user.getRoles()) {
            Permission nextPerm;
            if ((nextPerm = findPermissionForAction(rolePermissionProducer.apply(role), action)) != null) {
                // the best elected by OR
                permit = nullableOr(permit, nextPerm.isPermit());
                // do not needed better then true
                if (permit != null && permit)
                    break;
            }
        }
        return permit;
    }

    /**
     * Helper function to implement logic according table.
     * <table border="1">
     * <tr>
     * <th>A</th><th>B</th><th>result</th>
     * </tr>
     * <tr>
     * <td>null</td><td>null</td><td>null</td>
     * </tr>
     * <tr>
     * <td>null</td><td>false</td><td>false</td>
     * </tr>
     * <tr>
     * <td>null</td><td>true</td><td>true</td>
     * </tr>
     * <tr>
     * <td>false</td><td>null</td><td>false</td>
     * </tr>
     * <tr>
     * <td>true</td><td>null</td><td>true</td>
     * </tr>
     * <tr>
     * <td>false</td><td>false</td><td>false</td>
     * </tr>
     * <tr>
     * <td>true</td><td>true</td><td>true</td>
     * </tr>
     * </table>
     * @param a left parameter
     * @param b right parameter
     * @return result (see table)
     */
    @SuppressFBWarnings(value = "NP_BOOLEAN_RETURN_NULL", justification = "Caller use null result")
    private Boolean nullableOr(Boolean a, Boolean b) {
        if (a == null && b == null)
            return null;
        return Optional.ofNullable(a).orElse(Boolean.FALSE) || Optional.ofNullable(b).orElse(Boolean.FALSE);
    }

    /**
     * Helper method to find permission associated with action.
     * @param perms permission list
     * @param action action to find
     * @return permission for action
     */
    private Permission findPermissionForAction(Collection<Permission> perms, BusinessAction.Action action) {
        for (Permission perm : perms) {
            BusinessAction businessAction;
            if ((businessAction = perm.getAction()) != null && businessAction.getKey().equals(action.name()))
                return perm;
        }
        return null;
    }

    @Override
    @Cacheable(cacheNames = "entity-meta-business-actions", key = "#p0.key")
    public List<BusinessAction> getEntityMetaBusinessActions(@NotNull EntityMeta entityMeta, @CurrentTimeStamp LocalDateTime ldts) {
        List<BusinessAction> actions = loadEntityMetaActions(entityMeta, ldts);
        if (CollectionUtils.isEmpty(actions)) {
            actions = getEntityTypeBusinessActions(entityMeta.getType(), ldts);
        }
        return actions;
    }

    @Override
    public List<BusinessAction> getEntityBusinessActions(Entity entity, LocalDateTime ldts) {
        return getEntityMetaBusinessActions(entity.getMeta(), ldts);
    }

    @Override
    public List<BusinessAction> getEntityTypeBusinessActions(Entity entityType, LocalDateTime ldts) {
        return getEntityTypeBusinessActions(EntityType.valueOf(entityType.getKey()), ldts);
    }

    /**
     * Get list of allowable actions for entity type.
     * @param entityTypeE entity type
     * @param ldts load datetime
     * @return list of actions
     */
    @Override
    public List<BusinessAction> getEntityTypeBusinessActions(EntityType entityTypeE, LocalDateTime ldts) {
        EntityMeta entityTypeMeta = entityMetaService.getEntityMetaByKey(EntityTypeAttributeMeta.METADATA_KEY, ldts);
        Entity entityType = entityService.getEntity(entityTypeMeta, entityTypeE.name(), ldts);
        LinkedEntityAttribute<BusinessAction> link =
                (LinkedEntityAttribute<BusinessAction>) entityType.getAttribute(EntityTypeAttributeMeta.BUSINESS_ACTION.getKey());
        return link.getEntityList();
    }

    /**
     * Get list of allowable actions for entity meta.
     * @param entityMeta entity meta
     * @param ldts load datetime
     * @return list of actions
     */
    private List<BusinessAction> loadEntityMetaActions(EntityMeta entityMeta, LocalDateTime ldts) {
        List<Pair<Long, Boolean>> actionHubIds = metadataDao.getEntityMetaActionsIds(entityMeta, ldts);
        if (CollectionUtils.isEmpty(actionHubIds))
            return Collections.emptyList();

        // load actions by link ids
        EntityMeta businessActionMeta = entityMetaService.getEntityMetaByKey(BusinessAction.METADATA_KEY, ldts);
        Criteria criteria = new Criteria();
        criteria.setHubIds(actionHubIds.stream().map(p -> p.getLeft()).collect(Collectors.toList()));
        List<BusinessAction> businessActionList = (List<BusinessAction>) entityService.getEntities(businessActionMeta, criteria, null, ldts);
        for (BusinessAction ba : businessActionList) {
            ba.setCancelSecextRuleAvailable(findCancelSecextRuleAvailable(actionHubIds, ba.getHubId()));
        }
        return businessActionList;
    }

    /**
     * Internal utility function to find element in short list.
     * @param pairs list of pairs
     * @param hubId action hub ad to find in pair
     * @return value if cancel_secext_rule_available
     */
    private boolean findCancelSecextRuleAvailable(List<Pair<Long, Boolean>> pairs, Long hubId) {
        for (Pair<Long, Boolean> p : pairs) {
            if (hubId.equals(p.getLeft()) && p.getRight() != null)
                return p.getRight();
        }
        return false;
    }

    @Override
    public void pendingSecureChange(String forEntityMetaKey, Entity entity) {
        if (forEntityMetaKey.equals(Calculation.METADATA_KEY)) {
            if (entity instanceof Calculation)
                securityDao.pendingCalcSecureChangeByCalc((Calculation) entity);
            else if (entity instanceof User)
                securityDao.pendingCalcSecureChangeByUser((User) entity);
            else if (entity instanceof Department)
                securityDao.pendingCalcSecureChangeByDepartment((Department) entity);
            else if (entity instanceof Role)
                securityDao.pendingCalcSecureChangeByRole((Role) entity);
            else if (entity.getMeta().getKey().equals(ClientAttributeMeta.METADATA_KEY))
                securityDao.pendingCalcSecureChangeByClient(entity);
        }
    }

    @Override
    @Transactional
    public void rebuildAllSecurity() {
        securityDao.rebuildSecurityTags();
    }
}
