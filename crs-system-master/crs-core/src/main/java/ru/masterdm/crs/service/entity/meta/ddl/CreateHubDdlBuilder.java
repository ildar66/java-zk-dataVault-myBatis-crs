package ru.masterdm.crs.service.entity.meta.ddl;

import static org.jooq.impl.DSL.constraint;
import static org.jooq.impl.DSL.field;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.ID;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.KEY;
import static ru.masterdm.crs.domain.entity.meta.CommonColumn.LDTS;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Component;

import ru.masterdm.crs.domain.entity.meta.EntityMeta;

/**
 * 'create hub' DDL statement builder.
 * @author Alexey Chalov
 */
@Component
public class CreateHubDdlBuilder extends DdlBuilder {

    private static final int KEY_LENGTH = 100;

    @Override
    public List<Pair<String, String>> build(EntityMeta entityMeta) {
        DSLContext ctx = newDslContext();
        int constraintCounter = 1;
        String tableName = dsqlNamesService.getNames(entityMeta).getHubTableName();
        String create = ctx.createTable(tableName)
                           .column(ID.name(), SQLDataType.NUMERIC.nullable(false))
                           .column(KEY.name(), SQLDataType.VARCHAR.length(KEY_LENGTH).nullable(false))
                           .column(LDTS.name(), SQLDataType.TIMESTAMP.nullable(false))
                           .constraints(constraint(buildPkCountraintName(tableName))
                                                .primaryKey(ID.name()),
                                        constraint(buildUkConstraintName(tableName, constraintCounter))
                                                .unique(KEY.name()),
                                        constraint(buildCkConstraintName(tableName, constraintCounter))
                                                .check(field(KEY.name()).eq(field(KEY.name(), String.class).trim().upper())))
                           .getSQL().toUpperCase();

        String drop = ctx.dropTable(tableName).getSQL().toUpperCase();

        return Arrays.asList(Pair.of(create, drop));
    }
}
