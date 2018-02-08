package ru.masterdm.crs.web.model.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.SmartNotifyChange;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;

import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.calc.Formula;
import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.entity.BusinessAction;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.Permission;
import ru.masterdm.crs.domain.entity.Role;
import ru.masterdm.crs.domain.entity.criteria.Criteria;
import ru.masterdm.crs.domain.entity.criteria.Operator;
import ru.masterdm.crs.domain.entity.criteria.WhereItem;
import ru.masterdm.crs.domain.entity.meta.AttributeLocale;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.domain.entity.meta.EntityTypeAttributeMeta;
import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.service.CalcService;
import ru.masterdm.crs.service.EntityMetaService;
import ru.masterdm.crs.service.EntityService;
import ru.masterdm.crs.service.SecurityService;
import ru.masterdm.crs.service.UserRoleService;
import ru.masterdm.crs.web.domain.UserProfile;

/**
 * Edit permissions view model class.
 * @author Igor Matushak
 */
@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class EditPermissionsViewModel {

    @WireVariable
    private EntityMetaService entityMetaService;
    @WireVariable
    private EntityService entityService;
    @WireVariable("userProfile")
    private UserProfile userProfile;
    @WireVariable("roleService")
    private UserRoleService userRoleService;
    @WireVariable
    private CalcService calcService;
    @WireVariable
    private SecurityService securityService;
    private List<Role> roles;
    private Role selectedRole;
    private Map<BusinessAction, Map<Object, Permission>> permissionMap;
    private List<BusinessAction> businessActions;
    private List<Object> systemObjects;

    /**
     * Initiates context.
     */
    @Init
    public void initSetup() {
        if (!getRoles().isEmpty()) {
            selectedRole = getRoles().get(0);
        }
    }

    /**
     * Returns roles.
     * @return roles
     */
    public List<Role> getRoles() {
        if (roles == null) {
            roles = (List<Role>) entityService.getEntities(entityMetaService.getEntityMetaByKey(Role.METADATA_KEY, null), null, null, null);
        }
        return roles;
    }

    /**
     * Returns permissions.
     * @param object object
     * @return permissions
     */
    public List<Permission> getPermissions(Object object) {
        if (object instanceof EntityMeta) {
            return userRoleService.getEntityMetaPermissionsNoCache((EntityMeta) object, selectedRole, null);
        } else if (object instanceof Entity) {
            return userRoleService.getEntityTypePermissionsNoCache((Entity) object, selectedRole, null);
        }
        return null;
    }

    /**
     * Returns permission map.
     * @return permission map
     */
    public Map<BusinessAction, Map<Object, Permission>> getPermissionMap() {
        if (permissionMap == null) {
            permissionMap = new HashMap<>();
            for (BusinessAction businessAction : getBusinessActions()) {
                permissionMap.put(businessAction, new HashMap());
                for (Object object : getSystemObjects()) {
                    Permission newPermission = (Permission) entityService.newEmptyEntity(Permission.METADATA_KEY);
                    newPermission.setAction(businessAction);
                    newPermission.setRole(selectedRole);
                    if (object instanceof EntityMeta) {
                        newPermission.setEntityMeta((EntityMeta) object);
                    } else if (object instanceof Entity) {
                        newPermission.setEntityType((Entity) object);
                    }
                    permissionMap.get(businessAction).put(object, newPermission);
                    List<Permission> permissions = getPermissions(object);
                    for (Permission permission : permissions) {
                        if (permission.getAction().equals(businessAction)) {
                            permissionMap.get(businessAction).put(object, permission);
                        }
                    }
                }
            }
        }
        return permissionMap;
    }

    /**
     * Returns selected role.
     * @return selected role
     */
    public Role getSelectedRole() {
        return selectedRole;
    }

    /**
     * Sets selected role.
     * @param selectedRole selected role
     */
    public void setSelectedRole(Role selectedRole) {
        this.selectedRole = selectedRole;
    }

    /**
     * Returns business actions.
     * @return business actions
     */
    public List<BusinessAction> getBusinessActions() {
        if (businessActions == null) {
            EntityMeta businessActionMeta = entityMetaService.getEntityMetaByKey(BusinessAction.METADATA_KEY, null);
            businessActions = (List<BusinessAction>) entityService.getEntities(businessActionMeta, null, null, null);
        }
        return businessActions;
    }

    /**
     * Returns business actions.
     * @param object object
     * @return business actions
     */
    public List<BusinessAction> getBusinessActions(Object object) {
        if (object instanceof EntityMeta) {
            return securityService.getEntityMetaBusinessActions((EntityMeta) object, null);
        } else if (object instanceof Entity) {
            return securityService.getEntityTypeBusinessActions((Entity) object, null);
        }
        return null;
    }

    /**
     * Returns system objects.
     * @return system objects
     */
    public List<Object> getSystemObjects() {
        if (systemObjects == null) {
            systemObjects = new ArrayList<>();
            systemObjects.add(entityMetaService.getEntityMetaByKey(FormTemplate.METADATA_KEY, null));
            systemObjects.add(entityMetaService.getEntityMetaByKey(Model.METADATA_KEY, null));
            systemObjects.add(entityMetaService.getEntityMetaByKey(Formula.METADATA_KEY, null));
            systemObjects.add(entityMetaService.getEntityMetaByKey(Calculation.METADATA_KEY, null));
            systemObjects.add(entityMetaService.getEntityMetaByKey(EntityMeta.METADATA_KEY, null));

            EntityMeta entityTypeMeta = entityMetaService.getEntityMetaByKey(EntityTypeAttributeMeta.METADATA_KEY, null);
            Criteria criteria = new Criteria();
            criteria.getWhere().addItem(
                    new WhereItem(entityTypeMeta.getKeyAttribute(), Operator.IN,
                                  EntityType.CLASSIFIER.name(),
                                  EntityType.INPUT_FORM.name(),
                                  EntityType.DICTIONARY.name()));

            List<Entity> entityTypes = (List<Entity>) entityService.getEntities(entityTypeMeta, criteria, null, null);

            systemObjects.add(entityTypes.stream().filter(et -> et.getKey().equals(EntityType.CLASSIFIER.name())).findFirst().get());
            systemObjects.add(entityTypes.stream().filter(et -> et.getKey().equals(EntityType.INPUT_FORM.name())).findFirst().get());
            systemObjects.add(entityTypes.stream().filter(et -> et.getKey().equals(EntityType.DICTIONARY.name())).findFirst().get());
        }
        return systemObjects;
    }

    /**
     * Returns entity meta name.
     * @param object object
     * @return entity meta name
     */
    public String getSystemObjectName(Object object) {
        if (object instanceof EntityMeta) {
            return ((EntityMeta) object).getName().getDescription(userProfile.getLocale());
        } else if (object instanceof Entity) {
            String nameKey = userProfile.getLocale().equals(AttributeLocale.RU) ? EntityTypeAttributeMeta.NAME_RU.getKey()
                                                                                : EntityTypeAttributeMeta.NAME_EN.getKey();
            return (String) ((Entity) object).getAttributeValue(nameKey);
        }
        return null;
    }

    /**
     * Selects role.
     * @param role role
     */
    @Command
    @SmartNotifyChange("permissionMap")
    public void selectRole(@BindingParam("role") Role role) {
        permissionMap = null;
    }

    /**
     * Persists permissions.
     */
    @Command
    @SmartNotifyChange("*")
    public void persistPermissions() {
        List<Permission> permissions = new ArrayList<>();
        getBusinessActions().stream().forEach(businessAction -> {
            getSystemObjects().stream().forEach(entityMeta -> {
                Permission permission = permissionMap.get(businessAction).get(entityMeta);
                if (permission != null) {
                    permissions.add(permission);
                }
            });
        });
        userRoleService.persistsRolePermissions(permissions);
        permissionMap = null;
    }
}
