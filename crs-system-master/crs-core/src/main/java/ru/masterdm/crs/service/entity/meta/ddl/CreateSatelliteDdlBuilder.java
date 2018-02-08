package ru.masterdm.crs.service.entity.meta.ddl;

import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.constraint;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.DIGEST;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.H_ID;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.ID;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.LDTS;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.REMOVED;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.jooq.Context;
import org.jooq.CreateTableColumnStep;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.impl.CustomField;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Component;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.DsqlNames;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.service.entity.AttributeFactory;

/**
 * 'create satellite' DDL statement builder.
 * @author Alexey Chalov
 */
@Component
public class CreateSatelliteDdlBuilder extends DdlBuilder {

    /**
     * Wrapping sql field.
     * @param <T> field data type
     */
    private static class WrapField<T> extends CustomField<T> {

        /**
         * Constructor.
         * @param name field name
         * @param dataType data type
         */
        WrapField(String name, DataType<T> dataType) {
            super(name, dataType);
        }

        @Override
        public void accept(Context<?> ctx) {
            switch (ctx.configuration().dialect()) {
                case ORACLE:
                case ORACLE10G:
                case ORACLE11G:
                case ORACLE12C:
                    ctx.sql(String.format("\"%s\"", getName()), true);
                    break;
                default:
                    ctx.sql(getName(), true);
            }
        }
    }

    private static final int DIGEST_LENGTH = 100;

    @Override
    public List<Pair<String, String>> build(EntityMeta entityMeta) {
        DSLContext ctx = newDslContext();
        DsqlNames dsqlNames = dsqlNamesService.getNames(entityMeta);
        String tableName = dsqlNames.getSatelliteTableName();
        String hubTableName = dsqlNames.getHubTableName();

        CreateTableColumnStep createTable = ctx.createTable(tableName)
                                           .column(ID.name(), SQLDataType.NUMERIC.nullable(false))
                                           .column(H_ID.name(), SQLDataType.NUMERIC.nullable(false))
                                           .column(LDTS.name(), SQLDataType.TIMESTAMP.nullable(false))
                                           .column(REMOVED.name(), SQLDataType.NUMERIC(1, 0).nullable(false))
                                           .column(DIGEST.name(), SQLDataType.VARCHAR(DIGEST_LENGTH).nullable(false));
        /* add columns from metadata */
        for (AttributeMeta attributeMeta : entityMeta.getAttributes()) {
            DataType dataType = AttributeFactory.newDataType(attributeMeta);
            if (dataType != null) {
                createTable.column(new WrapField(attributeMeta.getNativeColumn(), dataType));
            }
        }

        /* add constraints */
        createTable.constraints(constraint(buildPkCountraintName(tableName)).primaryKey(ID.name()),
                            constraint(buildFkConstraintName(tableName, 1)).foreignKey(H_ID.name()).references(hubTableName, ID.name()),
                            constraint(buildCkConstraintName(tableName, 1)).check(condition(REMOVED.name() + " in (0, 1)"))
        );

        String create = createTable.getSQL().toUpperCase();
        String drop = ctx.dropTable(tableName).getSQL().toUpperCase();

        String indexName = buildIndexName(tableName, 1);
        String createIndex = ctx.createUniqueIndex(indexName).on(tableName, H_ID.name(), LDTS.name()).getSQL().toUpperCase() + COMPRESS1 + TABLESPACE;
        String dropIndex = ctx.dropIndex(indexName).getSQL().toUpperCase();

        return Arrays.asList(Pair.of(create, drop), Pair.of(createIndex, dropIndex));
    }

    @Override
    public List<Pair<String, String>> buildUpdate(EntityMeta entityMeta, AttributeMeta attributeMeta) {
        DataType<?> dataType = AttributeFactory.newDataType(attributeMeta);
        if (dataType != null) {
            DSLContext ctx = newDslContext();
            String tableName = dsqlNamesService.getNames(entityMeta, attributeMeta).getSatelliteTableName();
            String create = ctx.alterTable(tableName)
                               .addColumn(new WrapField(attributeMeta.getNativeColumn(), dataType), dataType)
                               .getSQL().toUpperCase();
            String drop = ctx.alterTable(tableName).dropColumn(new WrapField(attributeMeta.getNativeColumn(), dataType)).getSQL().toUpperCase();
            return Arrays.asList(Pair.of(create, drop));
        }
        return Collections.emptyList();
    }
}
