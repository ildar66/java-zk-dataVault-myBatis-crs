package ru.masterdm.crs.domain.entity.criteria;

/**
 * Used to select range or output rows.
 * Useful for data pagination.
 * @author Pavel Masalov
 */
public class RowRange {

    private int startRow;
    private int endRow;
    private Long totalCount;

    /**
     * Construct simple start-end row range.
     * @param startRow number of first row
     * @param endRow number of last row
     */
    public RowRange(int startRow, int endRow) {
        this.startRow = startRow;
        this.endRow = endRow;
    }

    /**
     * Factory method to create range by page number and page size.
     * @param pageNumber page number, starts from 0
     * @param sizeOfPage page size
     * @return range object
     */
    public static RowRange newAsPageAndSize(int pageNumber, int sizeOfPage) {
        int s = pageNumber * sizeOfPage + 1;
        return new RowRange(s, s + sizeOfPage - 1);
    }

    /**
     * Factory method to create range from shift.
     * @param shift shift of start row from beginning of result set. starts from 0
     * @param sizeOfPage page size
     * @return range object
     */
    public static RowRange newAsShiftAndSize(int shift, int sizeOfPage) {
        return new RowRange(shift + 1, shift + sizeOfPage);
    }

    /**
     * Returns start row.
     * @return start row
     */
    public int getStartRow() {
        return startRow;
    }

    /**
     * Returns end row.
     * @return end row
     */
    public int getEndRow() {
        return endRow;
    }

    /**
     * Returns total rows count that can be retrieved.
     * Get value only after request.
     * @return total rows count
     */
    public Long getTotalCount() {
        return totalCount;
    }

    /**
     * Sets total rows count.
     * @param totalCount total rows count
     */
    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }
}
