package ru.masterdm.crs.service.entity.meta.ddl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.AttributeType;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;

/**
 * 'create link sequence' DDL statement builder.
 * @author Alexey Chalov
 */
@Component
public class CreateLinkSequenceDdlBuilder extends DdlBuilder {

    @Override
    public List<Pair<String, String>> build(EntityMeta entityMeta) {
        DSLContext ctx = newDslContext();
        List<Pair<String, String>> result = new ArrayList<>();
        for (AttributeMeta attributeMeta : entityMeta.getAttributes()) {
            AttributeType dataType = attributeMeta.getType();
            boolean createSequence = dataType == AttributeType.FILE
                                     || dataType == AttributeType.REFERENCE
                                     || (attributeMeta.isMultilang() && STRING_DATA_TYPES.contains(dataType));
            if (createSequence) {
                String sequenceName = dsqlNamesService.getNames(entityMeta, attributeMeta).getLinkSequenceName();
                String create = ctx.createSequence(sequenceName).getSQL().toUpperCase();
                String drop = ctx.dropSequence(sequenceName).getSQL().toUpperCase();
                result.add(Pair.of(create, drop));
            }
        }
        return result;
    }

    @Override
    public List<Pair<String, String>> buildUpdate(EntityMeta entityMeta, AttributeMeta attributeMeta) {
        EntityMeta surrogate = new EntityMeta();
        surrogate.setAttributes(Arrays.asList(attributeMeta));
        return build(surrogate);
    }
}
