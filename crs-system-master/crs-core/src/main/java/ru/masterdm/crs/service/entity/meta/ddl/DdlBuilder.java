package ru.masterdm.crs.service.entity.meta.ddl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.ParamType;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;

/**
 * Common superclass for DDL statement builders.
 * @author Alexey Chalov
 */
public abstract class DdlBuilder {

    private static Settings settings;

    private static final String PK_POSTFIX = "_PK";
    private static final String UK_POSTFIX = "_UK";
    private static final String FK_POSTFIX = "_FK";
    private static final String CK_POSTFIX = "_CK";

    private static final String INDEX_POSTFIX = "_I";

    static final String TABLESPACE = " TABLESPACE SPOINDX";
    static final String COMPRESS1 = " COMPRESS 1";

    static final List<AttributeType> STRING_DATA_TYPES = Arrays.asList(AttributeType.STRING, AttributeType.TEXT);

    static {
        settings = new Settings();
        settings.setParamType(ParamType.NAMED);
    }

    @Autowired
    protected DsqlNamesService dsqlNamesService;

    @Value("#{config['db.dialect']}")
    protected String databaseDialect;

    /**
     * Builder method.
     * @param entityMeta {@link EntityMeta} instance
     * @return pair or DDL create and drop scripts
     */
    public abstract List<Pair<String, String>> build(EntityMeta entityMeta);

    /**
     * Builds update scripts for newly added attributes.
     * @param entityMeta {@link EntityMeta} instance
     * @param attributeMeta {@link AttributeMeta} instance
     * @return pair of create and drop scripts
     */
    public List<Pair<String, String>> buildUpdate(EntityMeta entityMeta, AttributeMeta attributeMeta) {
        throw new UnsupportedOperationException("No default implementation is provided. Subclasses must implement this method.");
    }

    /**
     * Creates and returns new {@link DSLContext} instance.
     * @return {@link DSLContext} instance
     */
    protected DSLContext newDslContext() {
        return DSL.using(SQLDialect.valueOf(databaseDialect), settings);
    }

    /**
     * Builds primary key constraint name.
     * @param tableName table name
     * @return primary key constraint name
     */
    protected String buildPkCountraintName(String tableName) {
        return tableName + PK_POSTFIX;
    }

    /**
     * Builds foreign key constraint name.
     * @param tableName table name
     * @param constraintIndex constraint index
     * @return foreign key constraint name
     */
    protected String buildFkConstraintName(String tableName, int constraintIndex) {
        return tableName + FK_POSTFIX + String.format("%02d", constraintIndex);
    }

    /**
     * Builds unique key constraint name.
     * @param tableName table name
     * @param constraintIndex constraint index
     * @return unique key constraint name
     */
    protected String buildUkConstraintName(String tableName, int constraintIndex) {
        return tableName + UK_POSTFIX + String.format("%02d", constraintIndex);
    }

    /**
     * Builds check constraint name.
     * @param tableName table name
     * @param constraintIndex constraint index
     * @return check constraint name
     */
    protected String buildCkConstraintName(String tableName, int constraintIndex) {
        return tableName + CK_POSTFIX + String.format("%02d", constraintIndex);
    }

    /**
     * Builds index name.
     * @param tableName table name
     * @param indexNumber index number
     * @return index name
     */
    protected String buildIndexName(String tableName, int indexNumber) {
        return tableName + INDEX_POSTFIX + String.format("%02d", indexNumber);
    }
}
