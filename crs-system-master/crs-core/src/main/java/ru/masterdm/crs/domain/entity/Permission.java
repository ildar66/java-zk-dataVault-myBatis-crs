package ru.masterdm.crs.domain.entity;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

import java.util.List;

import ru.masterdm.crs.domain.calc.Model;
import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EmbeddedAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.service.entity.AttributeFactory;

/**
 * Permission domain object.
 * @author Pavel Masalov
 */
public class Permission extends Entity {

    /**
     * Permission entity metadata attributes.
     * @author Pavel Masalov
     */
    public enum PermissionAttributeMeta implements EmbeddedAttributeMeta {
        /** Action to permit. */
        BUSINESS_ACTION,
        /** Entity to permit. */
        ENTITY,
        /** Role to permit. */
        ROLE,
        /** Permission value. */
        PERMIT,
        /** Cancel security extended rule. */
        CANCEL_SECEXT_RULE,
        /** Entity type. */
        ENTITY_TYPE,
        /** Model. */
        CALC_MODEL;

        @Override
        public String getKey() {
            return METADATA_KEY + KEY_DELIMITER + name();
        }
    }

    public static final String METADATA_KEY = "PERMISSION";

    private boolean permit;
    private boolean cancelSecextRule;
    private List<BusinessAction> actions;
    private List<Role> roles;
    private List<EntityMeta> entityMetas;
    private List<Entity> entityTypes;
    private List<Model> models;

    @Override
    public AbstractAttribute newAttribute(AttributeMeta attributeMeta) {
        if (attributeMeta.getKey().equals(PermissionAttributeMeta.PERMIT.getKey())) {
            return AttributeFactory.newAttribute(
                    attributeMeta, null, createValueAccessor((Boolean v) -> setPermit(v), () -> isPermit())
            );
        } else if (attributeMeta.getKey().equals(PermissionAttributeMeta.CANCEL_SECEXT_RULE.getKey())) {
            return AttributeFactory.newAttribute(
                    attributeMeta, null, createValueAccessor((Boolean v) -> setCancelSecextRule(v), () -> isCancelSecextRule())
            );
        } else {
            return super.newAttribute(attributeMeta);
        }
    }

    /**
     * Returns permit flag.
     * @return permit flag
     */
    public boolean isPermit() {
        return permit;
    }

    /**
     * Sets permit flag.
     * @param permit permit flag
     */
    public void setPermit(boolean permit) {
        this.permit = permit;
    }

    /**
     * Returns additional secure rule cancellation flag.
     * @return additional secure rule cancellation flag
     */
    public boolean isCancelSecextRule() {
        return cancelSecextRule;
    }

    /**
     * Sets additional secure rule cancellation flag.
     * @param cancelSecextRule additional secure rule cancellation flag
     */
    public void setCancelSecextRule(boolean cancelSecextRule) {
        this.cancelSecextRule = cancelSecextRule;
    }

    /**
     * Associate business action associated with permission.
     * @param action business action
     */
    public void setAction(BusinessAction action) {
        getAction();
        actions.clear();
        actions.add(action);
    }

    /**
     * Get business action associated with permission.
     * @return business action
     */
    public BusinessAction getAction() {
        if (actions == null)
            actions = ((LinkedEntityAttribute) getAttribute(PermissionAttributeMeta.BUSINESS_ACTION.getKey())).getEntityList();
        if (actions.isEmpty())
            return null;
        return actions.get(0);
    }

    /**
     * Returns role associated with permission.
     * @return role
     */
    public Role getRole() {
        if (roles == null)
            roles = ((LinkedEntityAttribute) getAttribute(PermissionAttributeMeta.ROLE.getKey())).getEntityList();
        if (roles.isEmpty())
            return null;
        return roles.get(0);
    }

    /**
     * Associate role with permission.
     * @param role role
     */
    public void setRole(Role role) {
        getRole();
        this.roles.clear();
        this.roles.add(role);
    }

    /**
     * Returns meta for which permission are defined.
     * @return entity meta
     */
    public EntityMeta getEntityMeta() {
        if (entityMetas == null)
            entityMetas = ((LinkedEntityAttribute) getAttribute(PermissionAttributeMeta.ENTITY.getKey())).getEntityList();
        if (entityMetas.isEmpty())
            return null;
        return entityMetas.get(0);
    }

    /**
     * Sets meta for which permission are defined.
     * @param entityMeta entity meta
     */
    public void setEntityMeta(EntityMeta entityMeta) {
        getEntityMeta();
        this.entityMetas.clear();
        this.entityMetas.add(entityMeta);
    }

    /**
     * Returns permission entity type.
     * @return entity type
     */
    public Entity getEntityType() {
        if (entityTypes == null)
            entityTypes = ((LinkedEntityAttribute) getAttribute(PermissionAttributeMeta.ENTITY_TYPE.getKey())).getEntityList();
        if (entityTypes.isEmpty())
            return null;
        return entityTypes.get(0);
    }

    /**
     * Sets permission entity type.
     * @param entityType entity type
     */
    public void setEntityType(Entity entityType) {
        getEntityType();
        this.entityTypes.clear();
        this.entityTypes.add(entityType);
    }

    /**
     * Returns permission model.
     * @return permission model
     */
    public Model getModel() {
        if (models == null)
            models = ((LinkedEntityAttribute) getAttribute(PermissionAttributeMeta.CALC_MODEL.getKey())).getEntityList();
        if (models.isEmpty())
            return null;
        return models.get(0);
    }

    /**
     * Sets permission model.
     * @param model permission model
     */
    public void setModel(Model model) {
        getModel();
        this.models.clear();
        this.models.add(model);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString() + " action=" + (getAction() == null ? "null" : getAction().getKey())
               + (getEntityMeta() != null ? " entityMeta=" + getEntityMeta().getKey() : "")
               + (getEntityType() != null ? " entityType=" + getEntityType().getKey() : "")
               + (getModel() != null ? " model=" + getModel().getKey() : "");
    }
}
