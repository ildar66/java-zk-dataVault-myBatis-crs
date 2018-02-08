package ru.masterdm.crs.domain.calc;

import ru.masterdm.crs.domain.entity.AbstractDvEntity;
import ru.masterdm.crs.domain.entity.DigestSupport;

/**
 * Formula data content container.
 * @author Alexey Chalov
 */
public class FormulaData extends AbstractDvEntity implements DigestSupport {

    private String digest;
    private String data;

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
        return calcDigest(data);
    }

    /**
     * Returns formula content.
     * @return formula content
     */
    public String getData() {
        return data;
    }

    /**
     * Sets formula content.
     * @param data formula content
     */
    public void setData(String data) {
        this.data = data;
    }
}
