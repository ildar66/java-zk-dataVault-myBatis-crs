package ru.masterdm.crs.domain.form.mapping;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.entity.meta.EntityType;
import ru.masterdm.crs.domain.form.CreateOption;
import ru.masterdm.crs.domain.form.RepeatType;
import ru.masterdm.crs.util.json.EntityMetaKeyDeserializer;
import ru.masterdm.crs.util.json.EntityMetaKeySerializer;

/**
 * Object of map.
 * @author Vladimir Shvets
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MappingObject extends Mapper {

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = EntityMetaKeySerializer.class)
    @JsonDeserialize(using = EntityMetaKeyDeserializer.class)
    @JsonProperty("entityMetaKey")
    private EntityMeta entityMeta;

    private RepeatType repeat = RepeatType.ONE;
    private boolean updateOption;
    private CreateOption createOption = CreateOption.NEVER;
    private String name;
    private boolean context;
    private List<MappingField> fields;

    /**
     * Constructor.
     */
    public MappingObject() {
        super();
        fields = new ArrayList<>();
    }

    /**
     * Constructor.
     * @param entityMeta entity meta
     */
    public MappingObject(EntityMeta entityMeta) {
        this();
        setEntityMeta(entityMeta);
        fields.add(new MappingField(this, entityMeta.getKeyAttribute()));
        entityMeta.getAttributes().forEach(this::addField);
        if (entityMeta.getType().equals(EntityType.EMBEDDED_OBJECT)) {
            setUpdateOption(false);
            setCreateOption(CreateOption.NEVER);
        }
    }

    /**
     * Returns fields.
     * @return fields
     */
    public List<MappingField> getFields() {
        return fields;
    }

    /**
     * Sets fields.
     * @param fields fields
     */
    public void setFields(List<MappingField> fields) {
        this.fields = fields;
    }

    /**
     * Returns repeat.
     * @return repeat
     */
    public RepeatType getRepeat() {
        return repeat;
    }

    /**
     * Sets repeat.
     * @param repeat repeat
     */
    public void setRepeat(RepeatType repeat) {
        this.repeat = repeat;
    }

    /**
     * Returns update option.
     * @return update option
     */
    public boolean getUpdateOption() {
        return updateOption;
    }

    /**
     * Sets update option.
     * @param updateOption update option
     */
    public void setUpdateOption(boolean updateOption) {
        this.updateOption = updateOption;
    }

    /**
     * Returns create option.
     * @return create option
     */
    public CreateOption getCreateOption() {
        return createOption;
    }

    /**
     * Sets create option.
     * @param createOption create option
     */
    public void setCreateOption(CreateOption createOption) {
        this.createOption = createOption;
    }

    /**
     * Returns entity meta.
     * @return entity meta
     */
    public EntityMeta getEntityMeta() {
        return entityMeta;
    }

    /**
     * Sets entity meta.
     * @param entityMeta entity meta
     */
    public void setEntityMeta(EntityMeta entityMeta) {
        this.entityMeta = entityMeta;
    }

    /**
     * Returns name.
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets size of the largest range.
     * @return int size
     */
    @JsonIgnore
    public int getRangeSize() {
        MappingField mappingField = fields.stream()
                                          .filter(f -> f.getRange() != null && f.getRange().getSize() > 1)
                                          .findFirst().orElse(null);
        if (mappingField != null)
            return mappingField.getRange().getSize();
        return 1;
    }

    /**
     * Returns is context object.
     * @return is context object
     */
    public boolean isContext() {
        return context;
    }

    /**
     * Sets is context object.
     * @param context is context object
     */
    public void setContext(boolean context) {
        this.context = context;
    }

    /**
     * Adds new field.
     * @param attributeMeta attribute meta
     */
    public void addField(AttributeMeta attributeMeta) {
        fields.add(new MappingField(this, attributeMeta));
    }
}
