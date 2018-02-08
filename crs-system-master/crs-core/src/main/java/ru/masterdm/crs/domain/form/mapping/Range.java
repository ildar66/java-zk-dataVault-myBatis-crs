package ru.masterdm.crs.domain.form.mapping;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Excel sheet range.
 * @author Vladimir Shvets
 */
public class Range implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sheet;
    private int row;
    private int column;
    private int lastRow;
    private int lastColumn;
    private String ref;
    private long sheetIndex;

    /**
     * Constructor.
     */
    public Range() {

    }

    /**
     * Constructor.
     * @param row row
     * @param column column
     * @param lastRow last row
     * @param lastColumn last column
     */
    public Range(int row, int column, int lastRow, int lastColumn) {
        this();
        this.row = row;
        this.column = column;
        this.lastRow = lastRow;
        this.lastColumn = lastColumn;
    }

    /**
     * Constructor.
     * @param sheet sheet name
     * @param row row
     * @param column column
     * @param lastRow last row
     * @param lastColumn last column
     */
    public Range(String sheet, int row, int column, int lastRow, int lastColumn) {
        this(row, column, lastRow, lastColumn);
        this.sheet = sheet;
    }

    /**
     * Constructor.
     * @param sheet sheet name
     * @param row row
     * @param column column
     */
    public Range(String sheet, int row, int column) {
        this(sheet, row, column, row, column);
    }

    /**
     * Constructor.
     * @param row row
     * @param column column
     * @param lastRow last row
     * @param lastColumn last column
     * @param ref ref
     */
    public Range(int row, int column, int lastRow, int lastColumn, String ref) {
        this(row, column, lastRow, lastColumn);
        this.ref = ref;
    }

    /**
     * Constructor.
     * @param sheet sheet name
     * @param row row
     * @param column column
     * @param lastRow last row
     * @param lastColumn last column
     * @param ref ref
     */
    public Range(String sheet, int row, int column, int lastRow, int lastColumn, String ref) {
        this(sheet, row, column, lastRow, lastColumn);
        this.ref = ref;
    }

    /**
     * Returns first row.
     * @return first row
     */
    public int getRow() {
        return row;
    }

    /**
     * Sets first row.
     * @param row first row
     */
    public void setRow(int row) {
        this.row = row;
    }

    /**
     * Returns last row.
     * @return last row
     */
    public int getLastRow() {
        return lastRow;
    }

    /**
     * Sets last row.
     * @param lastRow last row
     */
    public void setLastRow(int lastRow) {
        this.lastRow = lastRow;
    }

    /**
     * Returns first column.
     * @return first column
     */
    public int getColumn() {
        return column;
    }

    /**
     * Sets first column.
     * @param column first column
     */
    public void setColumn(int column) {
        this.column = column;
    }

    /**
     * Returns last column.
     * @return last column
     */
    public int getLastColumn() {
        return lastColumn;
    }

    /**
     * Sets last column.
     * @param lastColumn last column
     */
    public void setLastColumn(int lastColumn) {
        this.lastColumn = lastColumn;
    }

    /**
     * Returns area reference..
     * @return ref
     */
    public String getRef() {
        return ref;
    }

    /**
     * Sets area reference.
     * @param ref ref
     */
    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     * Returns sheet name.
     * @return sheet name
     */
    public String getSheet() {
        return sheet;
    }

    /**
     * Sets sheet name.
     * @param sheet sheet name
     */
    public void setSheet(String sheet) {
        this.sheet = sheet;
    }

    /**
     * Returns sheet index.
     * @return sheet index
     */
    public long getSheetIndex() {
        return sheetIndex;
    }

    /**
     * Sets sheet index.
     * @param sheetIndex sheetIndex
     */
    public void setSheetIndex(long sheetIndex) {
        this.sheetIndex = sheetIndex;
    }

    /**
     * Gets range's square.
     * @return square
     */
    @JsonIgnore
    public int getSize() {
        return (1 + lastColumn - column) * (1 + lastRow - row);
    }
}
