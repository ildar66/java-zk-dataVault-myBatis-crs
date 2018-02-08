package ru.masterdm.crs.service.entity.meta.ddl;

import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.constraint;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.ID;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.LDTS;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.REMOVED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Component;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.DsqlNames;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;

/**
 * 'create link' DDL statement builder.
 * @author Alexey Chalov
 */
@Component
public class CreateLinkDdlBuilder extends DdlBuilder {

    private static final String STORAGE = "STORAGE";
    private static final String LOCALIZATION = "LOCALIZATION";

    @Override
    public List<Pair<String, String>> build(EntityMeta entityMeta) {
        DSLContext ctx = newDslContext();
        DsqlNames dsqlNames = dsqlNamesService.getNames(entityMeta);
        String hubTableName = dsqlNames.getHubTableName();
        List<Pair<String, String>> result = new ArrayList<>();
        for (AttributeMeta attributeMeta : entityMeta.getAttributes()) {
            AttributeType dataType = attributeMeta.getType();
            if (dataType == AttributeType.FILE) {
                result.addAll(buildLink(entityMeta, attributeMeta, ctx, hubTableName, dsqlNames.getHubSysPrefix() + STORAGE));
            }
            if (attributeMeta.isMultilang() && STRING_DATA_TYPES.contains(dataType)) {
                result.addAll(buildLink(entityMeta, attributeMeta, ctx, hubTableName, dsqlNames.getHubSysPrefix() + LOCALIZATION));
            }
            if (dataType == AttributeType.REFERENCE) {
                result.addAll(buildLink(entityMeta, attributeMeta, ctx, hubTableName, dsqlNames.getHubPrefix() + attributeMeta.getEntityKey()));
            }
        }
        return result;
    }

    @Override
    public List<Pair<String, String>> buildUpdate(EntityMeta entityMeta, AttributeMeta attributeMeta) {
        EntityMeta surrogate = new EntityMeta();
        surrogate.setKey(entityMeta.getKey());
        surrogate.setAttributes(Arrays.asList(attributeMeta));
        return build(surrogate);
    }

    /**
     * Builds link.
     * @param entityMeta entity metadata
     * @param attributeMeta {@link AttributeMeta} instance
     * @param ctx {@link DSLContext} instance
     * @param hubTableName hub table name
     * @param anotherHubTableName another hub table name
     * @return list of {@link Pair} of create/drop scripts
     */
    private List<Pair<String, String>> buildLink(EntityMeta entityMeta, AttributeMeta attributeMeta, DSLContext ctx, String hubTableName,
                                                 String anotherHubTableName) {
        DsqlNames dsqlNames = dsqlNamesService.getNames(entityMeta, attributeMeta);
        String hubReferenceColumnName = dsqlNames.getLinkParentHubIdColumnName();
        String anotherHubReferenceColumnName = dsqlNames.getLinkChildHubIdColumnName();
        String create = ctx.createTable(attributeMeta.getLinkTable())
                           .column(ID.name(), SQLDataType.NUMERIC.nullable(false))
                           .column(LDTS.name(), SQLDataType.TIMESTAMP.nullable(false))
                           .column(REMOVED.name(), SQLDataType.NUMERIC(1, 0).nullable(false))
                           .column(hubReferenceColumnName, SQLDataType.NUMERIC.nullable(false))
                           .column(anotherHubReferenceColumnName, SQLDataType.NUMERIC.nullable(false))
                           .constraints(constraint(buildPkCountraintName(attributeMeta.getLinkTable())).primaryKey(ID.name()),
                                        constraint(buildFkConstraintName(attributeMeta.getLinkTable(), 1))
                                                .foreignKey(hubReferenceColumnName)
                                                .references(hubTableName, ID.name()),
                                        constraint(buildFkConstraintName(attributeMeta.getLinkTable(), 2))
                                                .foreignKey(anotherHubReferenceColumnName)
                                                .references(anotherHubTableName, ID.name()),
                                        constraint(buildCkConstraintName(attributeMeta.getLinkTable(), 1))
                                                .check(condition(REMOVED.name() + " in (0, 1)")))
                           .getSQL().toUpperCase();
        String drop = ctx.dropTable(attributeMeta.getLinkTable()).getSQL().toUpperCase();

        int indexNumber = 1;
        String indexNameHub = buildIndexName(attributeMeta.getLinkTable(), indexNumber++);
        String createIndexHub =
                ctx.createIndex(indexNameHub).on(attributeMeta.getLinkTable(), hubReferenceColumnName, LDTS.name()).getSQL().toUpperCase()
                + COMPRESS1 + TABLESPACE;
        String dropIndexHub = ctx.dropIndex(indexNameHub).getSQL().toUpperCase();

        String indexNameRefHub = buildIndexName(attributeMeta.getLinkTable(), indexNumber++);
        String createIndexRefHub =
                ctx.createIndex(indexNameRefHub).on(attributeMeta.getLinkTable(), anotherHubReferenceColumnName, LDTS.name()).getSQL().toUpperCase()
                + COMPRESS1 + TABLESPACE;
        String dropIndexRefHub = ctx.dropIndex(indexNameRefHub).getSQL().toUpperCase();

        return Arrays.asList(Pair.of(create, drop), Pair.of(createIndexHub, dropIndexHub), Pair.of(createIndexRefHub, dropIndexRefHub));
    }
}
