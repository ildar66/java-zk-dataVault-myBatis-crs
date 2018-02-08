package ru.masterdm.crs.domain.form;

/**
 * File formats.
 * @author Vladimir Shvets
 */
public enum ExcelFormat {
    /** XLS. */
    XLS,
    /** XLSX. */
    XLSX,
    /** XML. */
    XML,
    /** XLSM. */
    XLSM;

    private static final String FORMAT_WITH_MACRO = "vnd.ms-excel.sheet.macroenabled.12";

    /**
     * Gets file format.
     * @return string format
     */
    public String getFormat() {
        if (this.equals(XLSM)) {
            return FORMAT_WITH_MACRO;
        } else {
            return this.name().toLowerCase();
        }
    }
}
