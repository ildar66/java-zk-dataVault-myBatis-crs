package ru.masterdm.crs.domain.form;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

import ru.masterdm.crs.domain.FileInfo;
import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.attribute.FileInfoAttribute;
import ru.masterdm.crs.domain.entity.attribute.MultilangAttribute;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EmbeddedAttributeMeta;
import ru.masterdm.crs.domain.form.mapping.Mapper;
import ru.masterdm.crs.service.entity.AttributeFactory;

/**
 * Form template entity.
 * @author Eugene Melnikov
 */
public class FormTemplate extends Entity {

    /**
     * Form template attributes.
     */
    public enum FormTemplateAttributeMeta implements EmbeddedAttributeMeta {
        /** Template data. */
        BOOK,
        /** Draft template. */
        DRAFT,
        /** Template format. */
        FORMAT,
        /** JSON representation of Mapper object. */
        MAPPER_CONFIG,
        /** Template name. */
        NAME,
        /** Template type. */
        TYPE;

        @Override
        public String getKey() {
            return METADATA_KEY + KEY_DELIMITER + this.name();
        }
    }

    /**
     * Form template metadata key.
     */
    public static final String METADATA_KEY = "FORM_TEMPLATE";

    private MultilangDescription name;
    private FileFormat format;
    private FileInfo book;
    private boolean draft = true;
    private TemplateType type;
    private Mapper mapper;
    private String mapperConfig;

    /**
     * Constructor.
     */
    public FormTemplate() {
        setMapper(new Mapper());
    }

    /**
     * Returns template name.
     * @return name
     */
    public MultilangDescription getName() {
        if (name == null)
            name = new MultilangDescription();
        return name;
    }

    /**
     * Sets template name.
     * @param name name
     */
    public void setName(MultilangDescription name) {
        this.name = name;
    }

    /**
     * Returns template format.
     * @return format
     */
    public FileFormat getFormat() {
        return format;
    }

    /**
     * Sets template format.
     * @param format format
     */
    public void setFormat(FileFormat format) {
        this.format = format;
    }

    /**
     * Returns is draft.
     * @return draft
     */
    public boolean isDraft() {
        return draft;
    }

    /**
     * Sets is draft.
     * @param draft draft
     */
    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    /**
     * Returns type.
     * @return type
     */
    public TemplateType getType() {
        return type;
    }

    /**
     * Sets type.
     * @param type type
     */
    public void setType(TemplateType type) {
        this.type = type;
    }

    /**
     * Returns mapper.
     * @return mapper
     */
    public Mapper getMapper() {
        return mapper;
    }

    /**
     * Sets mapper.
     * @param mapper mapper
     */
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Returns book.
     * @return book
     */
    public FileInfo getBook() {
        if (book == null)
            book = new FileInfo();
        return book;
    }

    /**
     * Sets book.
     * @param book book
     */
    public void setBook(FileInfo book) {
        this.book = book;
    }

    /**
     * Returns mapper json representation.
     * @return mapper json representation
     */
    public String getMapperConfig() {
        return mapperConfig;
    }

    /**
     * Sets mapper json representation.
     * @param mapperConfig mapper json representation
     */
    public void setMapperConfig(String mapperConfig) {
        this.mapperConfig = mapperConfig;
    }

    @Override
    public void setAttribute(AbstractAttribute attribute) {
        if (attribute.getMeta().getKey().equals(FormTemplateAttributeMeta.NAME.getKey()))
            setName(((MultilangAttribute) attribute).getMultilangDescription());
        else if (attribute.getMeta().getKey().equals(FormTemplateAttributeMeta.BOOK.getKey()))
            setBook(((FileInfoAttribute) attribute).getFileInfo());

        super.setAttribute(attribute);
    }

    @Override
    public AbstractAttribute newAttribute(AttributeMeta attributeMeta) {
        if (attributeMeta.getKey().equals(FormTemplateAttributeMeta.BOOK.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor((FileInfo v) -> setBook(v), () -> getBook()));
        } else if (attributeMeta.getKey().equals(FormTemplateAttributeMeta.DRAFT.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor((Boolean v) -> setDraft(v), () -> isDraft()));
        } else if (attributeMeta.getKey().equals(FormTemplateAttributeMeta.FORMAT.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor((String v) -> setFormat(FileFormat.valueOf(v)), () -> getFormat().name()));
        } else if (attributeMeta.getKey().equals(FormTemplateAttributeMeta.MAPPER_CONFIG.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor((String v) -> setMapperConfig(v), () -> getMapperConfig()));
        } else if (attributeMeta.getKey().equals(FormTemplateAttributeMeta.NAME.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor((MultilangDescription v) -> setName(v), () -> getName()));
        } else if (attributeMeta.getKey().equals(FormTemplateAttributeMeta.TYPE.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor((String v) -> setType(TemplateType.valueOf(v)), () -> getType().name()));

        } else {
            return super.newAttribute(attributeMeta);
        }
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
