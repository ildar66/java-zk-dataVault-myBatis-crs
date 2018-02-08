package ru.masterdm.crs.domain;

import java.io.InputStream;
import java.io.Serializable;

/**
 * File information for file info attribute.
 * @author Pavel Masalov
 */
public class FileInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String mimeType;
    private String name;
    private transient InputStream content;
    private String description;

    /**
     * Default constructor.
     */
    public FileInfo() {
    }

    /**
     * Construct object with file name and mme type.
     * @param name file name
     * @param mimeType mime type
     */
    public FileInfo(String name, String mimeType) {
        this.mimeType = mimeType;
        this.name = name;
    }

    /**
     * Construct object with file name.
     * @param name file name
     */
    public FileInfo(String name) {
        this.name = name;
    }

    /**
     * Returns file mime type.
     * @return file mime type
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Sets file mime type.
     * @param mimeType file mime type
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Returns file name.
     * @return file name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets file name.
     * @param name file name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns content.
     * @return content
     */
    public InputStream getContent() {
        return content;
    }

    /**
     * Sets content.
     * @param content content
     */
    public void setContent(InputStream content) {
        this.content = content;
    }

    /**
     * Sets description.
     * @param description description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets description.
     * @return description
     */
    public String getDescription() {
        return description;
    }
}
