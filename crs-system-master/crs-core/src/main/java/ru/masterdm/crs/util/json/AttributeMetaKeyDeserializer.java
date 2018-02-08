package ru.masterdm.crs.util.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;

/**
 * Deserialize attribute meta from key string to almost empty {@link AttributeMeta attribute meta}.
 * Only key value entered into object.
 * @author Pavel Masalov
 */
public class AttributeMetaKeyDeserializer extends StdDeserializer<AttributeMeta> {

    /**
     * Default constructor.
     */
    public AttributeMetaKeyDeserializer() {
        this(null);
    }

    /**
     * Override super constructor.
     * @param vc class type
     */
    protected AttributeMetaKeyDeserializer(Class vc) {
        super(vc);
    }

    @Override
    public AttributeMeta deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        AttributeMeta attributeMeta = new AttributeMeta();
        attributeMeta.setKey(p.getText());
        return attributeMeta;
    }
}
