package ru.masterdm.crs.util.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import ru.masterdm.crs.domain.entity.meta.EntityMeta;

/**
 * Serialise {@link EntityMeta entity meta} object to single string containing key.
 * @author Pavel Masalov
 */
public class EntityMetaKeySerializer extends StdSerializer<EntityMeta> {

    /**
     * Default constructor.
     */
    public EntityMetaKeySerializer() {
        this(null);
    }

    /**
     * Override super constructor.
     * @param t class type
     */
    public EntityMetaKeySerializer(Class<EntityMeta> t) {
        super(t);
    }

    @Override
    public void serialize(EntityMeta value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.getKey());
    }
}
