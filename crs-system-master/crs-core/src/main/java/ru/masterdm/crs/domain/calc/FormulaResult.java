package ru.masterdm.crs.domain.calc;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

import java.math.BigDecimal;

import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.attribute.AbstractAttribute;
import ru.masterdm.crs.domain.entity.meta.AttributeMeta;
import ru.masterdm.crs.domain.entity.meta.EmbeddedAttributeMeta;
import ru.masterdm.crs.service.entity.AttributeFactory;

/**
 * Formula result.
 * @author Sergey Valiev
 */
public class FormulaResult extends Entity {

    /**
     * Formula Result model's meta attributes.
     * @author Pavel Masalov
     */
    public enum FormulaResultAttributeMeta implements EmbeddedAttributeMeta {
        /** Result as number. */
        STRING_RESULT,
        /** Result as string. */
        NUMBER_RESULT,
        /** Exception log. */
        EXCEPTION,
        /** Output data. */
        OUTPUT;

        @Override
        public String getKey() {
            return METADATA_KEY + KEY_DELIMITER + this.name();
        }
    }

    /**
     * Model metadata key.
     */
    public static final String METADATA_KEY = "CALC_FORMULA_RESULT";

    private String stringResult;
    private BigDecimal numberResult;
    private String exception;
    private String output;

    @Override
    public String calcDigest() {
        return calcDigest(stringResult, numberResult, exception, output);
    }

    /**
     * Sets output.
     * @param output output
     */
    public void setOutput(String output) {
        this.output = output;
    }

    /**
     * Gets output.
     * @return output
     */
    public String getOutput() {
        return this.output;
    }

    /**
     * Returns string result.
     * @return string result
     */
    public String getStringResult() {
        return stringResult;
    }

    /**
     * Sets string result.
     * @param stringResult string result
     */
    public void setStringResult(String stringResult) {
        this.stringResult = stringResult;
    }

    /**
     * Returns number result.
     * @return number result
     */
    public BigDecimal getNumberResult() {
        return numberResult;
    }

    /**
     * Sets number result.
     * @param numberResult number result
     */
    public void setNumberResult(BigDecimal numberResult) {
        this.numberResult = numberResult;
    }

    /**
     * Returns calculation exception.
     * @return calculation exception
     */
    public String getException() {
        return exception;
    }

    /**
     * Sets calculation exception.
     * @param exception calculation exception
     */
    public void setException(String exception) {
        this.exception = exception;
    }

    /**
     * Returns result.
     * @param formula formula
     * @return result
     */
    public Object getResult(Formula formula) {
        FormulaResultType resultType = (formula != null) ? formula.getResultType() : null;
        if (resultType == null) {
            return null;
        }
        switch (resultType) {
            case NUMBER:
                return numberResult;
            case STRING:
                return stringResult;
            default:
                return null;
        }
    }

    @Override
    public AbstractAttribute newAttribute(AttributeMeta attributeMeta) {
        // TODO create annotation based attribute value accessors creation
        if (attributeMeta.getKey().equals(FormulaResultAttributeMeta.STRING_RESULT.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null, createValueAccessor((String v) -> setStringResult(v), () -> getStringResult()));
        } else if (attributeMeta.getKey().equals(FormulaResultAttributeMeta.NUMBER_RESULT.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null,
                                                 createValueAccessor((BigDecimal v) -> setNumberResult(v), () -> getNumberResult()));
        } else if (attributeMeta.getKey().equals(FormulaResultAttributeMeta.EXCEPTION.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null, createValueAccessor((String v) -> setException(v), () -> getException()));
        } else if (attributeMeta.getKey().equals(FormulaResultAttributeMeta.OUTPUT.getKey())) {
            return AttributeFactory.newAttribute(attributeMeta, null, createValueAccessor((String v) -> setOutput(v), () -> getOutput()));
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
