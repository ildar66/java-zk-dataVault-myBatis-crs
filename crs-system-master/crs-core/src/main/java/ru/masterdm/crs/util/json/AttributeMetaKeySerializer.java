package ru.masterdm.crs.util.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;

/**
 * Serialise {@link AttributeMeta attribute meta} to single string containing key.
 * @author Pavel Masalov
 */
public class AttributeMetaKeySerializer extends StdSerializer<AttributeMeta> {

    /**
     * Default constructor.
     */
    public AttributeMetaKeySerializer() {
        this(null);
    }

    /**
     * Override super constructor.
     * @param t type class
     */
    protected AttributeMetaKeySerializer(Class<AttributeMeta> t) {
        super(t);
    }

    @Override
    public void serialize(AttributeMeta value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.getKey());
    }
}
