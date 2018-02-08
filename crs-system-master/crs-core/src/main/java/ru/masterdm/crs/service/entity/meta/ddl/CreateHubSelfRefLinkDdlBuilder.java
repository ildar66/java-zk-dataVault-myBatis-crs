package ru.masterdm.crs.service.entity.meta.ddl;

import static org.jooq.impl.DSL.condition;
import static org.jooq.impl.DSL.constraint;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.ID;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.LDTS;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.REMOVED;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Component;

import ru.masterdm.crs.domain.entity.meta.DsqlNames;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;

/**
 * 'create hub self reference link' DDL statement builder.
 * @author Alexey Chalov
 */
@Component
public class CreateHubSelfRefLinkDdlBuilder extends DdlBuilder {

    @Override
    public List<Pair<String, String>> build(EntityMeta entityMeta) {
        DSLContext ctx = newDslContext();
        DsqlNames dsqlNames = dsqlNamesService.getNames(entityMeta);
        String hubTableName = dsqlNames.getHubTableName();
        String colNameSelf = entityMeta.getKey() + "_ID";
        String colNameParent = entityMeta.getKey() + "_P_ID";
        String create = ctx.createTable(entityMeta.getLinkTable())
                           .column(ID.name(), SQLDataType.NUMERIC.nullable(false))
                           .column(LDTS.name(), SQLDataType.TIMESTAMP.nullable(false))
                           .column(REMOVED.name(), SQLDataType.NUMERIC(1, 0).nullable(false))
                           .column(colNameSelf, SQLDataType.NUMERIC.nullable(false))
                           .column(colNameParent, SQLDataType.NUMERIC.nullable(false))
                           .constraints(constraint(buildPkCountraintName(entityMeta.getLinkTable()))
                                                .primaryKey(ID.name()),
                                        constraint(buildFkConstraintName(entityMeta.getLinkTable(), 1))
                                                .foreignKey(colNameSelf)
                                                .references(hubTableName, ID.name()),
                                        constraint(buildFkConstraintName(entityMeta.getLinkTable(), 2))
                                                .foreignKey(colNameParent)
                                                .references(hubTableName, ID.name()),
                                        constraint(buildCkConstraintName(entityMeta.getLinkTable(), 1))
                                                .check(condition(REMOVED.name() + " in (0, 1)")))
                           .getSQL().toUpperCase();
        String drop = ctx.dropTable(entityMeta.getLinkTable()).getSQL().toUpperCase();

        int indexNumber = 1;
        String indexNameSelf = buildIndexName(entityMeta.getLinkTable(), indexNumber++);
        String createIndexSelf =
                ctx.createIndex(indexNameSelf).on(entityMeta.getLinkTable(), colNameSelf, LDTS.name()).getSQL().toUpperCase() + COMPRESS1
                + TABLESPACE;
        String dropIndexSelf = ctx.dropIndex(indexNameSelf).getSQL().toUpperCase();

        String indexNameParent = buildIndexName(entityMeta.getLinkTable(), indexNumber++);
        String createIndexParent =
                ctx.createIndex(indexNameParent).on(entityMeta.getLinkTable(), colNameParent, LDTS.name()).getSQL().toUpperCase() + COMPRESS1
                + TABLESPACE;
        String dropIndexParent = ctx.dropIndex(indexNameParent).getSQL().toUpperCase();

        return Arrays.asList(Pair.of(create, drop), Pair.of(createIndexSelf, dropIndexSelf), Pair.of(createIndexParent, dropIndexParent));
    }
}
