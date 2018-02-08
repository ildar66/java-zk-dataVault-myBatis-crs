package ru.masterdm.crs.domain.form.mapping;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.form.FormDateType;
import ru.masterdm.crs.util.json.AttributeMetaKeyDeserializer;
import ru.masterdm.crs.util.json.AttributeMetaKeySerializer;

/**
 * Field of mapping mappingObject.
 * @author Vladimir Shvets
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MappingField implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private transient MappingObject mappingObject;

    @JsonDeserialize(using = AttributeMetaKeyDeserializer.class)
    @JsonSerialize(using = AttributeMetaKeySerializer.class)
    @JsonProperty("attributeMetaKey")
    private AttributeMeta attributeMeta;

    private boolean key;
    private boolean mapped;
    private Range range;
    private boolean write;
    private FormDateType formDateType;
    private FormDateType.OffsetType dateOffsetType;
    private int dateOffset;
    private MappingObject object;

    /**
     * One of the {@link ru.masterdm.crs.service.entity.AttributeFactory#getJavaClass(AttributeMeta)} types instance used in {@link
     * ru.masterdm.crs.domain.entity.criteria.WhereItem query criteria item} as a value.
     * For multilang we store one {@link String}.
     */
    private Object value;

    /**
     * Default constructor.
     * Used at deserialization process.
     */
    public MappingField() {
    }

    /**
     * Constructor.
     * @param mappingObject mapping object
     * @param attributeMeta attribute meta
     */
    MappingField(MappingObject mappingObject, AttributeMeta attributeMeta) {
        this.setMappingObject(mappingObject);
        this.setAttributeMeta(attributeMeta);
        this.setFormDateType(FormDateType.CURRENT);
        this.setDateOffsetType(FormDateType.OffsetType.MONTHS);
    }

    /**
     * Returns is key.
     * @return is key
     */
    public boolean isKey() {
        return key;
    }

    /**
     * Sets is key.
     * @param key is key
     */
    public void setKey(boolean key) {
        this.key = key;
    }

    /**
     * Returns value.
     * @return value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets value.
     * @param value value
     */

    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Returns true if mapped.
     * @return if mapped
     */
    public boolean isMapped() {
        return mapped;
    }

    /**
     * Sets if mapped.
     * @param mapped if mapped
     */
    public void setMapped(boolean mapped) {
        this.mapped = mapped;
    }

    /**
     * Returns range.
     * @return range
     */
    public Range getRange() {
        return range;
    }

    /**
     * Sets range.
     * @param range range
     */
    public void setRange(Range range) {
        this.range = range;
    }

    /**
     * Returns mapping mappingObject.
     * @return mapping mappingObject
     */
    public MappingObject getMappingObject() {
        return mappingObject;
    }

    /**
     * Sets mapping mappingObject.
     * @param mappingObject mapping mappingObject
     */
    public void setMappingObject(MappingObject mappingObject) {
        this.mappingObject = mappingObject;
    }

    /**
     * Returns write option.
     * @return write option
     */
    public boolean isWrite() {
        return write;
    }

    /**
     * Sets write option.
     * @param write write option
     */
    public void setWrite(boolean write) {
        this.write = write;
    }

    /**
     * Returns attribute meta.
     * @return attribute meta
     */
    public AttributeMeta getAttributeMeta() {
        return attributeMeta;
    }

    /**
     * Sets attribute meta.
     * @param attributeMeta attribute meta
     */
    public void setAttributeMeta(AttributeMeta attributeMeta) {
        this.attributeMeta = attributeMeta;
    }

    /**
     * Returns date type.
     * @return date type
     */
    public FormDateType getFormDateType() {
        return formDateType;
    }

    /**
     * Sets  date type.
     * @param formDateType date type
     */
    public void setFormDateType(FormDateType formDateType) {
        this.formDateType = formDateType;
    }

    /**
     * Returns offset type.
     * @return offset type
     */
    public FormDateType.OffsetType getDateOffsetType() {
        return dateOffsetType;
    }

    /**
     * Sets  offset type.
     * @param dateOffsetType offset type
     */
    public void setDateOffsetType(FormDateType.OffsetType dateOffsetType) {
        this.dateOffsetType = dateOffsetType;
    }

    /**
     * Returns date offset.
     * @return date offset
     */
    public int getDateOffset() {
        return dateOffset;
    }

    /**
     * Sets date offset.
     * @param dateOffset date offset
     */
    public void setDateOffset(int dateOffset) {
        this.dateOffset = dateOffset;
    }

    /**
     * Returns object.
     * @return object
     */
    public MappingObject getObject() {
        return object;
    }

    /**
     * Sets object.
     * @param object object
     */
    public void setObject(MappingObject object) {
        this.object = object;
    }
}
