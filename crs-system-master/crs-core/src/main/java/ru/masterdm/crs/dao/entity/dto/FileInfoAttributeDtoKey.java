package ru.masterdm.crs.dao.entity.dto;

import ru.masterdm.crs.domain.entity.attribute.FileInfoAttribute;

/**
 * Store part of file data for {@code attributeMetaKey attribute of entity}.
 * @author Pavel Masalov
 */
public class FileInfoAttributeDtoKey {

    private String attributeMetaKey;
    private FileInfoAttribute fileInfoAttribute;

    /**
     * Returns attribute metakey value.
     * @return attribute metakey value
     */
    public String getAttributeMetaKey() {
        return attributeMetaKey;
    }

    /**
     * Sets attribute metakey value.
     * @param attributeMetaKey attribute metakey value
     */
    public void setAttributeMetaKey(String attributeMetaKey) {
        this.attributeMetaKey = attributeMetaKey;
    }

    /**
     * Returns file attribute.
     * @return file attribute
     */
    public FileInfoAttribute getFileInfoAttribute() {
        return fileInfoAttribute;
    }

    /**
     * Sets file attribute.
     * @param fileInfoAttribute file attribute
     */
    public void setFileInfoAttribute(FileInfoAttribute fileInfoAttribute) {
        this.fileInfoAttribute = fileInfoAttribute;
    }
}
