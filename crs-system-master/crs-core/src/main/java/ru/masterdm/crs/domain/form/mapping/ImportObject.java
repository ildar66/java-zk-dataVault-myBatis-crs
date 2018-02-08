package ru.masterdm.crs.domain.form.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.masterdm.crs.domain.entity.Entity;

/**
 * Link between entity and row in file.
 * @author Vladimir Shvets
 */
public class ImportObject {

    private MappingObject mappingObject;
    private Entity entity;
    private Map<String, String> keysMap = new HashMap<>();
    private Map<String, Range> valuesMap = new HashMap<>();
    private ImportObject parent;
    private boolean modified;
    private List<Integer> rows = new ArrayList<>();
    private List<Integer> columns = new ArrayList<>();

    /**
     * Constructor.
     * @param mappingObject mapping object
     */
    public ImportObject(MappingObject mappingObject) {
        this.mappingObject = mappingObject;
    }

    /**
     * Constructor.
     * @param mappingObject mapping object
     * @param parent parent import object
     */
    public ImportObject(MappingObject mappingObject, ImportObject parent) {
        this(mappingObject);
        this.parent = parent;
    }

    /**
     * Returns entity.
     * @return entity
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Sets entity.
     * @param entity entity
     */
    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    /**
     * Returns mapping object.
     * @return mapping object
     */
    public MappingObject getMappingObject() {
        return mappingObject;
    }

    /**
     * Sets  mapping object.
     * @param mappingObject mapping object
     */
    public void setMappingObject(MappingObject mappingObject) {
        this.mappingObject = mappingObject;
    }

    /**
     * Returns key map.
     * @return key map
     */
    public Map<String, String> getKeysMap() {
        return keysMap;
    }

    /**
     * Sets  key map.
     * @param keysMap key map
     */
    public void setKeysMap(Map<String, String> keysMap) {
        this.keysMap = keysMap;
    }

    /**
     * Returns values map.
     * @return values map
     */
    public Map<String, Range> getValuesMap() {
        return valuesMap;
    }

    /**
     * Sets  values map.
     * @param valuesMap values map
     */
    public void setValuesMap(Map<String, Range> valuesMap) {
        this.valuesMap = valuesMap;
    }

    /**
     * Returns parent.
     * @return parent
     */
    public ImportObject getParent() {
        return parent;
    }

    /**
     * Sets parent.
     * @param parent parent
     */
    public void setParent(ImportObject parent) {
        this.parent = parent;
    }

    /**
     * Returns is modified.
     * @return is modified
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Sets is modified.
     * @param modified is modified
     */
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * Returns object's rows in multiline import.
     * @return rows
     */
    public List<Integer> getRows() {
        return rows;
    }

    /**
     * Sets object's rows in multiline import .
     * @param rows rows
     */
    public void setRows(List<Integer> rows) {
        this.rows = rows;
    }

    /**
     * Returns object's columns in multiline import.
     * @return columns
     */
    public List<Integer> getColumns() {
        return columns;
    }

    /**
     * Sets object's columns in multiline import .
     * @param columns columns
     */
    public void setColumns(List<Integer> columns) {
        this.columns = columns;
    }
}
