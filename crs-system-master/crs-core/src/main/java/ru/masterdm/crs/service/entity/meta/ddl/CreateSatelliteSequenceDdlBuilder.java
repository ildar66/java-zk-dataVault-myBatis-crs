package ru.masterdm.crs.service.entity.meta.ddl;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import ru.masterdm.crs.domain.entity.meta.EntityMeta;

/**
 * 'create satellite sequence' DDL statement builder.
 * @author Alexey Chalov
 */
@Component
public class CreateSatelliteSequenceDdlBuilder extends DdlBuilder {

    @Override
    public List<Pair<String, String>> build(EntityMeta entityMeta) {
        DSLContext ctx = newDslContext();
        String sequenceName = dsqlNamesService.getNames(entityMeta).getSatelliteSequenceName();
        String create = ctx.createSequence(sequenceName).getSQL().toUpperCase();
        String drop = ctx.dropSequence(sequenceName).getSQL().toUpperCase();
        return Arrays.asList(Pair.of(create, drop));
    }
}