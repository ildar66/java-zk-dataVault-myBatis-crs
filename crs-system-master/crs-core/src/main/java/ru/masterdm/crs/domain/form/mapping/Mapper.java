package ru.masterdm.crs.domain.form.mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Mapping settings for importer or exporter.
 * @author Vladimir Shvets
 */
public class Mapper implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private transient Mapper parent;

    private List<MappingObject> objects;

    /**
     * Constructor.
     */
    public Mapper() {
        objects = new ArrayList<>();
    }

    /**
     * Returns objects of map.
     * @return mapping objects
     */
    public List<MappingObject> getObjects() {
        return objects;
    }

    /**
     * Sets objects of map.
     * @param objects mapping object
     */
    public void setObjects(List<MappingObject> objects) {
        this.objects = objects;
    }

    /**
     * Adds new object to map or object.
     * @param newObject new object
     */
    public void addObject(MappingObject newObject) {
        newObject.setParent(this);
        this.getObjects().add(newObject);
    }

    /**
     * Returns parent object.
     * @return parent object
     */
    public Mapper getParent() {
        return parent;
    }

    /**
     * Sets parent object.
     * @param parent parent object
     */
    public void setParent(Mapper parent) {
        this.parent = parent;
    }

}
