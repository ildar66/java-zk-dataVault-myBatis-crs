package ru.masterdm.crs.domain.calc;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import ru.masterdm.crs.domain.Lockable;
import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.attribute.LinkedEntityAttribute;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EmbeddedAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EntityMeta;
import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.service.entity.AttributeFactory;

/**
 * Calculation model.
 * @author Sergey Valiev
 */
public class Model extends Entity implements Lockable {

    /**
     * Calculation model's meta attributes.
     * @author Sergey Valiev
     */
    public enum ModelAttributeMeta implements EmbeddedAttributeMeta {
        /** Model name (ru). */
        NAME_RU,
        /** Model name (en). */
        NAME_EN,
        /** Published flag. */
        PUBLISHED,
        /** Periodicity. */
        PERIODICITY,
        /** Point in time the current model. */
        ACTUALITY,
        /** Version. */
        VERSION,
        /** Comment (ru). */
        COMMENT_RU,
        /** Comment (en). */
        COMMENT_EN,
        /** Model's formulas. */
        FORMULAS,
        /** Model's forms. */
        INPUT_FORMS,
        /** Classifiers. */
        CLASSIFIERS,
        /** Form templates. */
        FORM_TEMPLATES;

        @Override
        public String getKey() {
            return METADATA_KEY + KEY_DELIMITER + this.name();
        }
    }

    /**
     * Periodicity.
     * @author Alexey Kirilchev
     */
    public enum Periodicity {
        /** Year. */
        YEAR,
        /** Quarter. */
        QUARTER,
        /** Month. */
        MONTH,
        /** Day. */
        DAY
    }

    /**
     * Model input form reference attribute link's attributes.
     */
    public enum ModelInputFormAttributeMeta implements EmbeddedAttributeMeta {
        /** Period count. */
        PERIOD_COUNT,
        /** Input form date attribute key. */
        INPUT_FORM_DATE_ATTR_KEY;

        @Override
        public String getKey() {
            return ModelAttributeMeta.INPUT_FORMS.getKey() + KEY_DELIMITER + this.name();
        }
    }

    /**
     * Model metadata key.
     */
    public static final String METADATA_KEY = "CALC_MODEL";

    private MultilangDescription name;
    private String digest;
    private boolean published;
    private LocalDateTime actuality;
    private Long version;
    private MultilangDescription comment;
    private List<Formula> formulas;
    private List<EntityMeta> inputForms;
    private List<EntityMeta> classifiers;
    private List<FormTemplate> formTemplates;
    private Periodicity periodicity = Periodicity.QUARTER;

    /**
     * Returns multilang name.
     * @return multilang name
     */
    public MultilangDescription getName() {
        if (name == null) {
            name = new MultilangDescription();
        }
        return name;
    }

    /**
     * Sets multilang name.
     * @param name multilang name
     */
    public void setName(MultilangDescription name) {
        // only copy values
        this.getName().setDescriptionEn(name.getDescriptionEn());
        this.name.setDescriptionRu(name.getDescriptionRu());
    }

    @Override
    public String getDigest() {
        return digest;
    }

    @Override
    public void setDigest(String digest) {
        this.digest = digest;
    }

    @Override
    public String calcDigest() {
        return calcDigest(
                published, actuality, version,
                name != null ? name.getDescriptionRu() : null,
                name != null ? name.getDescriptionEn() : null,
                comment != null ? comment.getDescriptionRu() : null,
                comment != null ? comment.getDescriptionEn() : null,
                periodicity != null ? periodicity.name() : null
        );
    }

    /**
     * Returns published flag.
     * @return <code>true</code> if calculation model is published
     */
    public boolean isPublished() {
        return published;
    }

    /**
     * Sets published flag.
     * @param published published flag
     */
    public void setPublished(boolean published) {
        this.published = published;
    }

    /**
     * Returns calculation model actuality date.
     * @return calculation model actuality date
     */
    public LocalDateTime getActuality() {
        return actuality;
    }

    /**
     * Sets calculation model actuality date.
     * @param actuality calculation model actuality date
     */
    public void setActuality(LocalDateTime actuality) {
        this.actuality = actuality.truncatedTo(ChronoUnit.SECONDS);
    }

    /**
     * Returns version of published model.
     * @return version of published model
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Sets version of published model.
     * @param version version of published model
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Returns multilang comment.
     * @return multilang comment
     */
    public MultilangDescription getComment() {
        if (comment == null) {
            comment = new MultilangDescription();
        }
        return comment;
    }

    /**
     * Sets multilang comment.
     * @param comment multilang comment
     */
    public void setComment(MultilangDescription comment) {
        // only copy values
        this.getComment().setDescriptionEn(comment.getDescriptionEn());
        this.comment.setDescriptionRu(comment.getDescriptionRu());
    }

    /**
     * Returns formulas.
     * @return formulas
     */
    public List<Formula> getFormulas() {
        if (formulas == null)
            formulas = ((LinkedEntityAttribute) getAttribute(ModelAttributeMeta.FORMULAS.getKey())).getEntityList();
        return formulas;
    }

    /**
     * Sets formulas.
     * @param formulas formulas
     */
    public void setFormulas(List<Formula> formulas) {
        this.getFormulas().clear();
        this.formulas.addAll(formulas);
    }

    /**
     * Returns input forms.
     * @return input forms
     */
    public List<EntityMeta> getInputForms() {
        if (inputForms == null)
            inputForms = ((LinkedEntityAttribute) getAttribute(ModelAttributeMeta.INPUT_FORMS.getKey())).getEntityList();
        return inputForms;
    }

    /**
     * Sets input forms.
     * @param inputForms input forms
     */
    public void setInputForms(List<EntityMeta> inputForms) {
        this.getInputForms().clear();
        this.inputForms.addAll(inputForms);
    }

    /**
     * Returns classifiers.
     * @return classifiers
     */
    public List<EntityMeta> getClassifiers() {
        if (classifiers == null)
            classifiers = ((LinkedEntityAttribute) getAttribute(ModelAttributeMeta.CLASSIFIERS.getKey())).getEntityList();
        return classifiers;
    }

    /**
     * Sets classifiers.
     * @param classifiers classifiers
     */
    public void setClassifiers(List<EntityMeta> classifiers) {
        this.getClassifiers().clear();
        this.classifiers.addAll(classifiers);
    }

    /**
     * Returns form templates.
     * @return form templates
     */
    public List<FormTemplate> getFormTemplates() {
        if (formTemplates == null)
            formTemplates = ((LinkedEntityAttribute) getAttribute(ModelAttributeMeta.FORM_TEMPLATES.getKey())).getEntityList();
        return formTemplates;
    }

    /**
     * Sets form templates.
     * @param formTemplates form templates
     */
    public void setFormTemplates(List<FormTemplate> formTemplates) {
        getFormTemplates().clear();
        this.formTemplates.addAll(formTemplates);
    }

    /**
     * Returns periodicity.
     * @return periodicity
     */
    public Periodicity getPeriodicity() {
        return periodicity;
    }

    /**
     * Sets periodicity.
     * @param periodicity periodicity
     */
    public void setPeriodicity(Periodicity periodicity) {
        this.periodicity = periodicity;
    }

    @Override
    public String getLockName() {
        return getKey();
    }

    @Override
    public AbstractAttribute newAttribute(AttributeMeta attributeMeta) {
        // TODO create annotation based attribute value accessors creation
        if (attributeMeta.getKey().equals(ModelAttributeMeta.NAME_EN.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor((String v) -> getName().setDescriptionEn(v),
                                                                     () -> getName().getDescriptionEn()));
        } else if (attributeMeta.getKey().equals(ModelAttributeMeta.NAME_RU.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor((String v) -> getName().setDescriptionRu(v),
                                                                     () -> getName().getDescriptionRu()));
        } else if (attributeMeta.getKey().equals(ModelAttributeMeta.COMMENT_RU.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor((String v) -> getComment().setDescriptionRu(v),
                                                                     () -> getComment().getDescriptionRu()));
        } else if (attributeMeta.getKey().equals(ModelAttributeMeta.COMMENT_EN.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor((String v) -> getComment().setDescriptionEn(v),
                                                                     () -> getComment().getDescriptionEn()));
        } else if (attributeMeta.getKey().equals(ModelAttributeMeta.PERIODICITY.getKey())) {
            return AttributeFactory
                    .newAttribute(attributeMeta, null,
                                  createValueAccessor((String v) -> setPeriodicity(Periodicity.valueOf(v)), () -> getPeriodicity().name()));
        } else if (attributeMeta.getKey().equals(ModelAttributeMeta.PUBLISHED.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null, createValueAccessor((Boolean v) -> setPublished(v), () -> isPublished()));
        } else if (attributeMeta.getKey().equals(ModelAttributeMeta.ACTUALITY.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor((LocalDateTime v) -> setActuality(v), () -> getActuality()));
        } else if (attributeMeta.getKey().equals(ModelAttributeMeta.VERSION.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor((BigDecimal v) -> setVersion(v == null ? null : v.longValue()),
                                                                     () -> getVersion() == null ? null : new BigDecimal(getVersion())));
        } else if (attributeMeta.getKey().equals(ModelAttributeMeta.FORMULAS.getKey())) {
            return new LinkedEntityAttribute<>(attributeMeta);
        } else if (attributeMeta.getKey().equals(ModelAttributeMeta.INPUT_FORMS.getKey())) {
            return new LinkedEntityAttribute<>(attributeMeta);
        } else if (attributeMeta.getKey().equals(ModelAttributeMeta.CLASSIFIERS.getKey())) {
            return new LinkedEntityAttribute<>(attributeMeta);
        } else if (attributeMeta.getKey().equals(ModelAttributeMeta.FORM_TEMPLATES.getKey())) {
            return new LinkedEntityAttribute<>(attributeMeta);

        } else {
            return super.newAttribute(attributeMeta);
        }
    }

    @Override
    public int hashCode() {
        if (getKey() == null) {
            return super.hashCode();
        }

        return new HashCodeBuilder(17, 37)
                .append(getKey())
                .append(version)
                .toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Model that = (Model) o;
        return new EqualsBuilder()
                .append(getKey(), that.getKey())
                .append(version, that.getVersion())
                .isEquals();
    }
}
