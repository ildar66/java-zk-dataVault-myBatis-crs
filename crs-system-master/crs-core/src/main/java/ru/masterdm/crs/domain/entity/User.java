package ru.masterdm.crs.domain.entity;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

import java.util.List;

import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EmbeddedAttributeMeta;
import ru.masterdm.crs.service.entity.AttributeFactory;

/**
 * User entity.
 * @author Sergey Valiev
 * @author Alexey Kirilchev
 */
public class User extends Entity {

    /**
     * User's meta attributes.
     * @author Alexey Kirilchev
     */
    public enum UserAttributeMeta implements EmbeddedAttributeMeta {
        /** Surname. */
        SURNAME,
        /** Name. */
        NAME,
        /** Patronymic. */
        PATRONYMIC,
        /** Favorites. */
        FAVORITES,
        /** Department. */
        DEPARTMENT,
        /** Roles. */
        ROLES,
        /** User tel. */
        TEL_NUMBER,
        /** User email. */
        EMAIL;

        @Override
        public String getKey() {
            return METADATA_KEY + KEY_DELIMITER + this.name();
        }
    }

    /**
     * User roles meta attributes.
     */
    public enum UserRolesAttributeMeta implements EmbeddedAttributeMeta {
        /** Is user role approved. */
        APPROVED;

        @Override
        public String getKey() {
            return UserAttributeMeta.ROLES.getKey() + KEY_DELIMITER + this.name();
        }
    }

    /**
     * User metadata key.
     */
    public static final String METADATA_KEY = "USER";

    private String surname;
    private String name;
    private String patronymic;
    private Department department;
    private List<Entity> favoritesList;
    private List<Role> roles;

    @Override
    public String calcDigest() {
        return calcDigest(surname, name, patronymic);
    }

    /**
     * Returns surname.
     * @return surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Sets surname.
     * @param surname surname
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * Returns name.
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns patronymic.
     * @return patronymic
     */
    public String getPatronymic() {
        return patronymic;
    }

    /**
     * Sets patronymic.
     * @param patronymic patronymic
     */
    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    /**
     * Returns login.
     * @return login
     */
    public String getLogin() {
        return getKey();
    }

    /**
     * Sets login.
     * @param login login
     */
    public void setLogin(String login) {
        setKey(login);
    }

    /**
     * Returns department.
     * @return department
     */
    public Department getDepartment() {
        if (department == null) {
            List<Department> departments = ((LinkedEntityAttribute) getAttribute(UserAttributeMeta.DEPARTMENT.getKey())).getEntityList();
            department = departments.isEmpty() ? null : departments.get(0);
        }
        return department;
    }

    /**
     * Sets department.
     * @param department department
     */
    public void setDepartment(Department department) {
        List<Department> departments = ((LinkedEntityAttribute) getAttribute(UserAttributeMeta.DEPARTMENT.getKey())).getEntityList();
        departments.clear();
        if (department != null)
            departments.add(department);
    }

    /**
     * Returns favorites.
     * @return favorites
     */
    public Entity getFavorites() {
        initFavorites();
        if (favoritesList.isEmpty())
            return null;
        return favoritesList.get(0);
    }

    /**
     * Sets favorites.
     * @param favorites favorites
     */
    public void setFavorites(Entity favorites) {
        initFavorites();
        this.favoritesList.clear();
        if (favorites != null)
            this.favoritesList.add(favorites);
    }

    /**
     * Init favorites single-instance array.
     */
    private void initFavorites() {
        if (favoritesList == null)
            favoritesList = ((LinkedEntityAttribute) getAttribute(UserAttributeMeta.FAVORITES.getKey())).getEntityList();
    }

    /**
     * Returns user roles.
     * @return user roles
     */
    public List<Role> getRoles() {
        if (roles == null) {
            roles = ((LinkedEntityAttribute) getAttribute(UserAttributeMeta.ROLES.getKey())).getEntityList();
        }
        return roles;
    }

    /**
     * Sets user roles.
     * @param roles user roles
     */
    public void setRoles(List<Role> roles) {
        getRoles().clear();
        this.roles.addAll(roles);
    }

    /**
     * Returns full name.
     * @return full name
     */
    public String getFullName() {
        StringBuilder result = new StringBuilder();
        if (getSurname() != null && !getSurname().isEmpty())
            result.append(getSurname()).append(" ");
        if (getName() != null && !getName().isEmpty())
            result.append(getName()).append(" ");
        if (getPatronymic() != null && !getPatronymic().isEmpty())
            result.append(getPatronymic()).append(" ");
        return result.toString().trim();
    }

    @Override
    public AbstractAttribute newAttribute(AttributeMeta attributeMeta) {
        // TODO create annotation based attribute value accessors creation
        if (attributeMeta.getKey().equals(UserAttributeMeta.SURNAME.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null, createValueAccessor(this::setSurname, this::getSurname));
        } else if (attributeMeta.getKey().equals(UserAttributeMeta.NAME.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null, createValueAccessor(this::setName, this::getName));
        } else if (attributeMeta.getKey().equals(UserAttributeMeta.PATRONYMIC.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null, createValueAccessor(this::setPatronymic, this::getPatronymic));
        } else {
            return super.newAttribute(attributeMeta);
        }
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
