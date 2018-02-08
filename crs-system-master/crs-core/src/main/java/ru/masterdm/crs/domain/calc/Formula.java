package ru.masterdm.crs.domain.calc;

import static ru.masterdm.crs.domain.entity.meta.AttributeMeta.KEY_DELIMITER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.tuple.Pair;

import ru.masterdm.crs.domain.MultilangDescription;
import ru.masterdm.crs.domain.entity.AbstractDvEntity;
import ru.masterdm.crs.domain.entity.DigestSupport;
import ru.masterdm.crs.domain.entity.Entity;
import ru.masterdm.crs.domain.entity.meta.EmbeddedAttributeMeta;
import ru.masterdm.crs.domain.entity.meta.FormulaType;

/**
 * Calculation formula.
 * @author Sergey Valiev
 */
public class Formula extends AbstractDvEntity implements DigestSupport, BiConsumer<String, FormulaVisitor> {

    /**
     * Calculation formula's meta attributes.
     * @author Sergey Valiev
     */
    public enum FormulaAttributeMeta implements EmbeddedAttributeMeta {
        /** Formula name (ru). */
        NAME_RU,
        /** Formula name (en). */
        NAME_EN,
        /** Comment (ru). */
        COMMENT_RU,
        /** Comment (en). */
        COMMENT_EN,
        /** Evaluation language. */
        EVAL_LANG,
        /** Result type. */
        RESULT_TYPE,
        /** Formula type. */
        TYPE;

        @Override
        public String getKey() {
            return METADATA_KEY + KEY_DELIMITER + this.name();
        }
    }

    /**
     * Formula metadata key.
     */
    public static final String METADATA_KEY = "CALC_FORMULA";

    private MultilangDescription name;
    private String digest;
    private FormulaData formula;
    private MultilangDescription comment;
    private String evalLang;
    private FormulaResultType resultType;
    private Map<Pair<Calculation, Entity>, FormulaResult> formulaResults;
    private FormulaResult tempFormulaResult; // crutch filed used only to fill map after tree select
    private FormulaType type;
    private List<FormulaDependencyPair> children;

    /**
     * Constructor.
     */
    public Formula() {
        type = FormulaType.FORMULA;
    }

    /**
     * Returns multilang name.
     * @return multilang name
     */
    public MultilangDescription getName() {
        return name;
    }

    /**
     * Sets multilang name.
     * @param name multilang name
     */
    public void setName(MultilangDescription name) {
        this.name = name;
    }

    @Override
    public String calcDigest() {
        return calcDigest(
                type, evalLang, resultType,
                name != null ? name.getDescriptionRu() : null,
                name != null ? name.getDescriptionEn() : null,
                comment != null ? comment.getDescriptionRu() : null,
                comment != null ? comment.getDescriptionEn() : null
        );
    }

    /**
     * Visitor pattern accept method.
     * @param attributeName name of result attribute
     * @param visitor {@link FormulaVisitor} instance
     */
    @Override
    public void accept(String attributeName, FormulaVisitor visitor) {
        visitor.visit(attributeName, this);
    }

    @Override
    public String getDigest() {
        return digest;
    }

    @Override
    public void setDigest(String digest) {
        this.digest = digest;
    }

    /**
     * Returns formula data.
     * @return formula data
     */
    public FormulaData getFormula() {
        return formula;
    }

    /**
     * Sets formula data.
     * @param formula formula data
     */
    public void setFormula(FormulaData formula) {
        this.formula = formula;
    }

    /**
     * Returns multilang comment.
     * @return multilang comment
     */
    public MultilangDescription getComment() {
        return comment;
    }

    /**
     * Sets multilang comment.
     * @param comment multilang comment
     */
    public void setComment(MultilangDescription comment) {
        this.comment = comment;
    }

    /**
     * Returns library flag.
     * @return library flag
     */
    public boolean isLibrary() {
        return getType() != null
               && (getType() == FormulaType.LIBRARY || getType() == FormulaType.SYS_LIBRARY);
    }

    /**
     * Returns system library flag.
     * @return system library flag
     */
    public boolean isSysLibrary() {
        return getType() != null && getType() == FormulaType.SYS_LIBRARY;
    }

    /**
     * Returns precalculated formula flag.
     * @return precalculated formula flag
     */
    public boolean isPrecalculatedFormula() {
        return getType() != null && getType() == FormulaType.PRECALCULATED_FORMULA;
    }

    /**
     * Returns eval lang.
     * @return eval lang
     */
    public String getEvalLang() {
        return evalLang;
    }

    /**
     * Sets eval lang.
     * @param evalLang eval lang
     */
    public void setEvalLang(String evalLang) {
        this.evalLang = evalLang;
    }

    /**
     * Returns result type.
     * @return result type
     */
    public FormulaResultType getResultType() {
        return resultType;
    }

    /**
     * Sets result type.
     * @param resultType result type
     */
    public void setResultType(FormulaResultType resultType) {
        this.resultType = resultType;
    }

    /**
     * Add formula, this one depends on, to children list.
     * @param resultAttributeName formula result context attribute name
     * @param formula {@link Formula} instance
     */
    public void addChild(String resultAttributeName, Formula formula) {
        if (children == null) {
            this.children = new ArrayList<>();
        }
        children.add(FormulaDependencyPair.of(resultAttributeName, formula));
    }

    /**
     * Returns formula result.
     * @param calculation calculation associated with result
     * @param profile calculation profile
     * @return formula result
     */
    public FormulaResult getFormulaResult(Calculation calculation, Entity profile) {
        if (formulaResults == null)
            return null;
        return formulaResults.get(Pair.of(calculation, profile));
    }

    /**
     * Sets formula result.
     * @param formulaResult formula result
     * @param calculation calculation associated with result
     * @param profile calculation profile
     */
    public void setFormulaResult(FormulaResult formulaResult, Calculation calculation, Entity profile) {
        if (formulaResults == null)
            formulaResults = new HashMap<>();

        tempFormulaResult = formulaResult;
        this.formulaResults.put(Pair.of(calculation, profile), formulaResult);
    }

    /**
     * Returns formula type.
     * @return formula type
     */
    public FormulaType getType() {
        return type;
    }

    /**
     * Sets formula type.
     * @param type formula type
     */
    public void setType(FormulaType type) {
        this.type = type;
    }

    /**
     * Returns list of formulas, this one depends on.
     * @return list of formulas, this one depends on
     */
    public List<FormulaDependencyPair> getChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }

    /**
     * Sets list of formulas, this one depends on.
     * @param children list of formulas, this one depends on
     */
    public void setChildren(List<FormulaDependencyPair> children) {
        this.children = children;
    }
}
