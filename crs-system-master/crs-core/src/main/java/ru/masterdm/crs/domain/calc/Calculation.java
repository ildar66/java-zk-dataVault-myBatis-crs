package ru.masterdm.crs.domain.calc;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import ru.masterdm.crs.domain.Lockable;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.User;
import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.ClientGroupAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EmbeddedAttributeMeta;
import ru.masterdm.crs.exception.CrsException;
import ru.masterdm.crs.service.entity.AttributeFactory;

/**
 * User's calculation.
 * @author Sergey Valiev
 */
public class Calculation extends Entity implements Lockable {

    /**
     * Calculation's meta attributes.
     * @author Pavel Masalov
     */
    public enum CalculationAttributeMeta implements EmbeddedAttributeMeta {
        /** Calculation name. */
        NAME,
        /** Published flag. */
        PUBLISHED,
        /** Calculated flag. */
        CALCULATED,
        /** Point in time the current model. */
        ACTUALITY,
        /** Point in time the data actuality. */
        DATA_ACTUALITY,
        /** Calculation's author. */
        AUTHOR,
        /** Calculation's model. */
        MODEL,
        /** Formula results linked to calculation. */
        FORMULA_RESULTS,
        /** Client. */
        CLIENT,
        /** Client group. */
        CLIENT_GROUP,
        /** Calculation profiles. */
        CALC_PROFILE;

        @Override
        public String getKey() {
            return METADATA_KEY + KEY_DELIMITER + this.name();
        }
    }

    /**
     * Model reference attribute link's attributes.
     */
    public enum CalculationModelAttributeMeta implements EmbeddedAttributeMeta {
        /** Model version. */
        VERSION;

        @Override
        public String getKey() {
            return CalculationAttributeMeta.MODEL.getKey() + KEY_DELIMITER + this.name();
        }
    }

    /**
     * Calculation client reference attribute link's attributes.
     */
    public enum CalculationClientAttributeMeta implements EmbeddedAttributeMeta {
        /** Participant. */
        PARTICIPANT,
        /** Status. */
        STATUS,
        /** Comment. */
        COMMENT,
        /** Excluded. */
        EXCLUDED;

        @Override
        public String getKey() {
            return CalculationAttributeMeta.CLIENT.getKey() + KEY_DELIMITER + this.name();
        }
    }

    /**
     * Calculation client status.
     */
    public enum CalculationClientStatus {
        /** Base. */
        BASE,
        /** Motherhood. */
        MOTHERHOOD,
        /** Other. */
        OTHER;
    }

    /**
     * Calculation metadata key.
     */
    public static final String METADATA_KEY = "CALC";

    // sync arrays attributes
    private List<User> authors;
    private List<Model> models;
    private List<Entity> clients;
    private List<Entity> clientGroups;
    private List<Entity> profiles;

    // accessor attributes
    private String name;
    private LocalDate actuality;
    private LocalDateTime dataActuality;
    private boolean published;
    private boolean calculated;

    /**
     * Returns author.
     * @return author
     */
    public User getAuthor() {
        initAuthor();
        if (authors.isEmpty())
            return null;
        return authors.get(0);
    }

    /**
     * Sets author.
     * @param author author
     */
    public void setAuthor(User author) {
        initAuthor();
        this.authors.clear();
        if (author != null)
            this.authors.add(author);
    }

    /**
     * Init author single-instance array.
     */
    private void initAuthor() {
        if (authors == null)
            authors = ((LinkedEntityAttribute) getAttribute(CalculationAttributeMeta.AUTHOR.getKey())).getEntityList();
    }

    /**
     * Returns calculation model.
     * @return calculation model
     */
    public Model getModel() {
        initModel();
        if (models.isEmpty())
            return null;
        return models.get(0);
    }

    /**
     * Sets calculation model.
     * @param model calculation model
     */
    public void setModel(Model model) {
        initModel();
        this.models.clear();
        if (model != null)
            this.models.add(model);
    }

    /**
     * Init model single-instance array.
     */
    private void initModel() {
        if (models == null)
            models = ((LinkedEntityAttribute) getAttribute(CalculationAttributeMeta.MODEL.getKey())).getEntityList();
    }

    /**
     * Get client entity object.
     * @return client entity object
     */
    public Entity getClient() {
        initClient();
        if (clients.isEmpty())
            return null;
        return clients.get(0);
    }

    /**
     * Set client entity object.
     * @param client client entity object
     */
    public void setClient(Entity client) {
        if (client != null && !client.getMeta().getKey().equals(ClientAttributeMeta.METADATA_KEY)) {
            throw new CrsException("Wrong entity with type=" + client.getMeta().getKey() + " trying to set as client. Type should be "
                                   + ClientAttributeMeta.METADATA_KEY);
        }

        initClient();
        this.clients.clear();
        if (client != null)
            this.clients.add(client);
    }

    /**
     * Init client entity objects store.
     */
    private void initClient() {
        if (clients == null)
            clients = ((LinkedEntityAttribute) getAttribute(CalculationAttributeMeta.CLIENT.getKey())).getEntityList();
    }

    /**
     * Get client group object.
     * @return client group object
     */
    public Entity getClientGroup() {
        initClientGroup();
        if (clientGroups.isEmpty())
            return null;
        return clientGroups.get(0);
    }

    /**
     * Set client group object.
     * @param clientGroup client group object
     */
    public void setClientGroup(Entity clientGroup) {
        if (clientGroup != null && !clientGroup.getMeta().getKey().equals(ClientGroupAttributeMeta.METADATA_KEY)) {
            throw new CrsException("Wrong entity with type=" + clientGroup.getMeta().getKey() + " trying to set as client. Type should be "
                                   + ClientGroupAttributeMeta.METADATA_KEY);
        }

        initClientGroup();
        this.clientGroups.clear();
        if (clientGroup != null)
            this.clientGroups.add(clientGroup);
    }

    /**
     * Init client group object store.
     */
    private void initClientGroup() {
        if (clientGroups == null)
            clientGroups = ((LinkedEntityAttribute) getAttribute(CalculationAttributeMeta.CLIENT_GROUP.getKey())).getEntityList();
    }

    /**
     * Returns calculation profiles.
     * @return calculation profiles
     */
    public List<Entity> getProfiles() {
        if (profiles == null)
            profiles = ((LinkedEntityAttribute) getAttribute(CalculationAttributeMeta.CALC_PROFILE.getKey())).getEntityList();
        return profiles;
    }

    /**
     * Sets calculation profiles.
     * @param profiles calculation profiles
     */
    public void setProfiles(List<Entity> profiles) {
        getProfiles().clear();
        this.profiles.addAll(profiles);
    }

    /**
     * Returns calc name.
     * @return calc name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets calc name.
     * @param name calc name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns calc actual date.
     * @return calc actual date
     */
    public LocalDate getActuality() {
        return actuality;
    }

    /**
     * Sets calc actual date.
     * @param actuality calc actual date
     */
    public void setActuality(LocalDate actuality) {
        this.actuality = actuality;
    }

    /**
     * Returns data actuality.
     * @return data actuality
     */
    public LocalDateTime getDataActuality() {
        return dataActuality;
    }

    /**
     * Sets data actuality.
     * @param dataActuality data actuality
     */
    public void setDataActuality(LocalDateTime dataActuality) {
        this.dataActuality = dataActuality.truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * Returns published flag.
     * @return <code>true</code> if published calculation
     */
    public boolean isPublished() {
        return published;
    }

    /**
     * Sets published flag.
     * @param published published flag
     */
    public void setPublished(boolean published) {
        this.published = published;
    }

    /**
     * Returns calculated flag.
     * @return <code>true</code> if calculations completed
     */
    public boolean isCalculated() {
        return calculated;
    }

    /**
     * Sets calculated flag.
     * @param calculated calculated flag
     */
    public void setCalculated(boolean calculated) {
        this.calculated = calculated;
    }

    @Override
    public String calcDigest() {
        return calcDigest(name, published, calculated, actuality, dataActuality);
    }

    @Override
    public String getLockName() {
        return getKey();
    }

    @Override
    public AbstractAttribute newAttribute(AttributeMeta attributeMeta) {
        // TODO create annotation based attribute value accessors creation
        if (attributeMeta.getKey().equals(CalculationAttributeMeta.AUTHOR.getKey())) {
            return new LinkedEntityAttribute<>(attributeMeta);

        } else if (attributeMeta.getKey().equals(CalculationAttributeMeta.MODEL.getKey())) {
            return new LinkedEntityAttribute<>(attributeMeta);

        } else if (attributeMeta.getKey().equals(CalculationAttributeMeta.CLIENT.getKey())) {
            return new LinkedEntityAttribute<>(attributeMeta);

        } else if (attributeMeta.getKey().equals(CalculationAttributeMeta.CLIENT_GROUP.getKey())) {
            return new LinkedEntityAttribute<>(attributeMeta);

        } else if (attributeMeta.getKey().equals(CalculationAttributeMeta.CALC_PROFILE.getKey())) {
            return new LinkedEntityAttribute<>(attributeMeta);

        } else if (attributeMeta.getKey().equals(CalculationAttributeMeta.NAME.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null, createValueAccessor((String v) -> setName(v), () -> getName()));

        } else if (attributeMeta.getKey().equals(CalculationAttributeMeta.ACTUALITY.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor((LocalDate v) -> setActuality(v), () -> getActuality()));

        } else if (attributeMeta.getKey().equals(CalculationAttributeMeta.PUBLISHED.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null, createValueAccessor((Boolean v) -> setPublished(v), () -> isPublished()));

        } else if (attributeMeta.getKey().equals(CalculationAttributeMeta.CALCULATED.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null, createValueAccessor((Boolean v) -> setCalculated(v), () -> isCalculated()));

        } else if (attributeMeta.getKey().equals(CalculationAttributeMeta.DATA_ACTUALITY.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor((LocalDateTime v) -> setDataActuality(v), () -> getDataActuality()));

        } else if (attributeMeta.getKey().equals(CalculationAttributeMeta.FORMULA_RESULTS.getKey())) {
            return new LinkedEntityAttribute<>(attributeMeta);

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
