package ru.masterdm.crs.util.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import ru.masterdm.crs.domain.entity.meta.EntityMeta;

/**
 * Deserialize {@link EntityMeta entity meta} object from single string containing key.
 * Create almost empty object. Only key member be defined.
 * @author Pavel Masalov
 */
public class EntityMetaKeyDeserializer extends StdDeserializer<EntityMeta> {

    /**
     * Default constructor.
     */
    public EntityMetaKeyDeserializer() {
        this(null);
    }

    /**
     * Override super constructor.
     * @param vc class type
     */
    public EntityMetaKeyDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public EntityMeta deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        EntityMeta entityMeta = new EntityMeta();
        entityMeta.setKey(p.getText());
        return entityMeta;
    }
}
