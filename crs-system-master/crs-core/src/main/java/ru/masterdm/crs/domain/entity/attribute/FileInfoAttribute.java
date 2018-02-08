package ru.masterdm.crs.domain.entity.attribute;

import java.io.InputStream;

import ru.masterdm.crs.domain.FileInfo;
import ru.masterdm.crs.domain.entity.attribute.value.ValueAccessor;
import ru.masterdm.crs.domain.entity.attribute.value.ValueKeeper;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;

/**
 * Store information about file.
 * <br>
 * <i>(Do not store content of file)</i>
 * @author Pavel Masalov
 */
public class FileInfoAttribute extends ExternalAttribute<String, FileInfo> {

    /**
     * Default file attribute constructor.
     */
    public FileInfoAttribute() {
        super(new ValueKeeper(new FileInfo()));
    }

    /**
     * Construct file info attribute with metadata.
     * @param meta attribute metadata
     */
    public FileInfoAttribute(AttributeMeta meta) {
        super(meta, new ValueKeeper(new FileInfo()));
    }

    /**
     * Construct file attribute with known meta type.
     * @param meta attribute metadata
     * @param valueAccessor value accessor
     */
    public FileInfoAttribute(AttributeMeta meta, ValueAccessor valueAccessor) {
        super(meta, valueAccessor);
    }

    /**
     * Get file info.
     * @return file info
     */
    public FileInfo getFileInfo() {
        return getValueAccessor().get();
    }

    @Override
    public String getValue() {
        return getName();
    }

    @Override
    public void setValue(String value) {
        // for  java.lang.ClassCastException: ... cannot be cast to ...
        setName(value);
    }

    /**
     * Returns mime type of file.
     * @return mime type of file
     */
    public String getMimeType() {
        return getFileInfo().getMimeType();
    }

    /**
     * Sets mime type of file.
     * @param mimeType mime type of file
     */
    public void setMimeType(String mimeType) {
        getFileInfo().setMimeType(mimeType);
    }

    /**
     * Returns name of file.
     * @return name of file
     */
    public String getName() {
        return getFileInfo().getName();
    }

    /**
     * Sets name of file.
     * @param name name of file
     */
    public void setName(String name) {
        getFileInfo().setName(name);
    }

    /**
     * Returns content.
     * @return content
     */
    public InputStream getContent() {
        return getFileInfo().getContent();
    }

    /**
     * Sets content.
     * @param content content
     */
    public void setContent(InputStream content) {
        getFileInfo().setContent(content);
    }

    /**
     * Sets description.
     * @param description description
     */
    public void setDescription(String description) {
        getFileInfo().setDescription(description);
    }

    /**
     * Gets description.
     * @return description
     */
    public String getDescription() {
        return getFileInfo().getDescription();
    }

    @Override
    public String calcDigest() {
        return calcDigest(getMimeType(), getName(), getDescription());
    }
}