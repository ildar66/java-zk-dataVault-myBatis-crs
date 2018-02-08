package ru.masterdm.crs.domain.entity.meta;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.ID;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * DDL/DSQL names container.
 * @author Pavel Masalov
 */
public class DsqlNames {

    private static final String LINK_LOCALIZATION_COLUMN = "localization_id";
    private static final String LINK_STORAGE_COLUMN = "storage_id";

    private String hubSysPrefix = "";
    private String satelliteSysPrefix = "";
    private String hubPrefix = "";
    private String satellitePrefix = "";
    private String referencedHubPrefix = "";
    private String referencedSatellitePrefix = "";
    private String sequencePostfix = "";
    private String linkPrefix = "";
    private String linkSatellitePrefix = "";
    private String linkPrefixRegExp;

    private EntityMeta entityMeta;
    private AttributeMeta attributeMeta;

    private EntityMeta referencedEntityMeta;

    /**
     * Create instance to make only entity level names.
     * @param entityMeta entity metadata
     */
    public DsqlNames(EntityMeta entityMeta) {
        this.entityMeta = entityMeta;
    }

    /**
     * Create instance to make entity and referenced attribute names.
     * @param entityMeta entity metadata
     * @param attributeMeta reference attribute metadata
     */
    public DsqlNames(EntityMeta entityMeta, AttributeMeta attributeMeta) {
        this(entityMeta);
        this.attributeMeta = attributeMeta;
    }

    /**
     * Create instance to make entity, referenced attribute and referenced entity names.
     * @param entityMeta entity metadata
     * @param attributeMeta reference attribute metadata
     * @param referencedEntityMeta referenced entity metadata
     */
    public DsqlNames(EntityMeta entityMeta, AttributeMeta attributeMeta, EntityMeta referencedEntityMeta) {
        this(entityMeta);
        this.attributeMeta = attributeMeta;
        this.referencedEntityMeta = referencedEntityMeta; // TODO make it load on demand
    }

    /**
     * Returns attribute key.
     * @param partialAttributeKey partial attribute key
     * @return attribute key
     */
    public String getAttributeKey(String partialAttributeKey) {
        if (StringUtils.isEmpty(entityMeta.getKey())) {
            return partialAttributeKey;
        }
        return entityMeta.getKey() + KEY_DELIMITER + partialAttributeKey;
    }

    /**
     * Returns hub system table prefix.
     * @return hub system table prefix
     */
    public String getHubSysPrefix() {
        return hubSysPrefix;
    }

    /**
     * Sets hub system table prefix.
     * @param hubSysPrefix hub system table prefix
     */
    public void setHubSysPrefix(String hubSysPrefix) {
        this.hubSysPrefix = hubSysPrefix;
    }

    /**
     * Returns satellite system table prefix.
     * @return satellite system table prefix
     */
    public String getSatelliteSysPrefix() {
        return satelliteSysPrefix;
    }

    /**
     * Sets satellite system table prefix.
     * @param satelliteSysPrefix satellite system table prefix
     */
    public void setSatelliteSysPrefix(String satelliteSysPrefix) {
        this.satelliteSysPrefix = satelliteSysPrefix;
    }

    /**
     * Returns hub table prefix.
     * @return hub table prefix
     */
    public String getHubPrefix() {
        return hubPrefix;
    }

    /**
     * Sets hub table prefix.
     * @param hubPrefix hub table prefix
     */
    public void setHubPrefix(String hubPrefix) {
        this.hubPrefix = hubPrefix;
    }

    /**
     * Returns satellite table prefix.
     * @return satellite table prefix
     */
    public String getSatellitePrefix() {
        return satellitePrefix;
    }

    /**
     * Sets satellite table prefix.
     * @param satellitePrefix satellite table prefix
     */
    public void setSatellitePrefix(String satellitePrefix) {
        this.satellitePrefix = satellitePrefix;
    }

    /**
     * Returns sequence object postfix.
     * @return sequence object postfix
     */
    public String getSequencePostfix() {
        return sequencePostfix;
    }

    /**
     * Sets sequence object postfix.
     * @param sequencePostfix sequence object postfix
     */
    public void setSequencePostfix(String sequencePostfix) {
        this.sequencePostfix = sequencePostfix;
    }

    /**
     * Make parent(main) hub table name.
     * @return hub table name
     */
    public String getHubTableName() {
        return hubPrefix + entityMeta.getKey();
    }

    /**
     * Make parent satellite table name.
     * @return satellite table name
     */
    public String getSatelliteTableName() {
        return satellitePrefix + entityMeta.getKey();
    }

    /**
     * Satellite table name for referenced table if names are prepared to serve REFERENCE attribute.
     * @return table name
     */
    public String getReferencedSatelliteTableName() {
        return referencedSatellitePrefix + referencedEntityMeta.getKey();
    }

    /**
     * Hub table name for referenced table if names are prepared to serve REFERENCE attribute.
     * @return table name
     */
    public String getReferencedHubTableName() {
        return referencedHubPrefix + referencedEntityMeta.getKey();
    }

    /**
     * Reference table should be checked.
     * @return true if should be checked
     */
    public boolean isReferenceEntity() {
        return referencedEntityMeta != null;
    }

    /**
     * Make hub sequence name.
     * @return hub sequence name
     */
    public String getHubSequenceName() {
        return getHubTableName() + sequencePostfix;
    }

    /**
     * Make hub self reference sequence name.
     * @return self reference sequence name
     */
    public String getHubSelfRefLinkSequenceName() {
        return entityMeta.getLinkTable() + sequencePostfix;
    }

    /**
     * Make satellite sequence name.
     * @return satellite sequence name
     */
    public String getSatelliteSequenceName() {
        return getSatelliteTableName() + sequencePostfix;
    }

    /**
     * Make column name that used to reference parent hub at link tables.
     * @return column name
     */
    public String getLinkParentHubIdColumnName() {
        checkAttributeMeta();
        return calcLinkToHubColumnName(attributeMeta.getKey().equals(CommonAttribute.CHILDREN.name())
                                       ? entityMeta.getKey() + "_P"
                                       : entityMeta.getKey());
    }

    /**
     * Make column name that used to reference child hubs at link tables.
     * @return column name
     */
    public String getLinkChildHubIdColumnName() {
        checkAttributeMeta();
        if (attributeMeta.isMultilang())
            return LINK_LOCALIZATION_COLUMN;
        else if (attributeMeta.getType() == AttributeType.FILE)
            return LINK_STORAGE_COLUMN;

        return calcLinkToHubColumnName(attributeMeta.getKey().equals(CommonAttribute.PARENT.name())
                                       ? attributeMeta.getEntityKey() + "_P"
                                       : attributeMeta.getEntityKey());
    }

    /**
     * Make link sequence name.
     * @return link sequence name
     */
    public String getLinkSequenceName() {
        checkAttributeMeta();
        return attributeMeta.getLinkTable() + sequencePostfix;
    }

    /**
     * Get link satellite table name.
     * @return table name
     */
    public String getLinkSatelliteTableName() {
        checkAttributeMeta();
        return attributeMeta.getLinkTable().replaceAll(linkPrefixRegExp, linkSatellitePrefix);
    }

    /**
     * Get link satellite table sequence.
     * @return sequence name
     */
    public String getLinkSatelliteSequenceName() {
        return getLinkSatelliteTableName() + sequencePostfix;
    }

    /**
     * Helper to make column name to reference hub.
     * @param entityKeyPrefix entity key prefix
     * @return column name
     */
    private String calcLinkToHubColumnName(String entityKeyPrefix) {
        return entityKeyPrefix + "_" + ID.name();
    }

    /**
     * Check existance if attribute metadata.
     */
    private void checkAttributeMeta() {
        if (attributeMeta == null)
            throw new IllegalStateException("Name object has no attribute metadata");
    }

    /**
     * Returns attribute metadata used to generate DSQL names.
     * @return attribute metadata
     */
    public AttributeMeta getAttributeMeta() {
        return attributeMeta;
    }

    /**
     * Returns entity metadata used to generate DSQL names.
     * @return entity metadata
     */
    public EntityMeta getEntityMeta() {
        return entityMeta;
    }

    /**
     * Returns referenced entity meta.
     * @return referenced entity meta
     */
    public EntityMeta getReferencedEntityMeta() {
        return referencedEntityMeta;
    }

    /**
     * Returns link prefix.
     * @return link prefix
     */
    public String getLinkPrefix() {
        return linkPrefix;
    }

    /**
     * Sets link prefix.
     * @param linkPrefix link prefix
     */
    public void setLinkPrefix(String linkPrefix) {
        this.linkPrefix = linkPrefix;
        linkPrefixRegExp = "(?i)" + Pattern.quote(linkPrefix);
    }

    /**
     * Returns link satellite prefix.
     * @return link satellite prefix
     */
    public String getLinkSatellitePrefix() {
        return linkSatellitePrefix;
    }

    /**
     * Sets link satellite prefix.
     * @param linkSatellitePrefix link satellite prefix
     */
    public void setLinkSatellitePrefix(String linkSatellitePrefix) {
        this.linkSatellitePrefix = linkSatellitePrefix;
    }

    /**
     * Returns reference hub table prefix.
     * @return hub table prefix
     */
    public String getReferencedHubPrefix() {
        return referencedHubPrefix;
    }

    /**
     * Sets reference hub table prefix.
     * @param referencedHubPrefix hub table prefix
     */
    public void setReferencedHubPrefix(String referencedHubPrefix) {
        this.referencedHubPrefix = referencedHubPrefix;
    }

    /**
     * Returns reference satellite table prefix.
     * @return satellite table prefix
     */
    public String getReferencedSatellitePrefix() {
        return referencedSatellitePrefix;
    }

    /**
     * Sets reference satellite table prefix.
     * @param referencedSatellitePrefix satellite table prefix
     */
    public void setReferencedSatellitePrefix(String referencedSatellitePrefix) {
        this.referencedSatellitePrefix = referencedSatellitePrefix;
    }
}
