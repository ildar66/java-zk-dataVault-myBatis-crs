package ru.masterdm.crs.web.service;

import java.util.List;

import org.zkoss.zss.api.model.Book;
import org.zkoss.zul.ListModelList;

import ru.masterdm.crs.domain.calc.Calculation;
import ru.masterdm.crs.domain.form.FormDateType;
import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.domain.form.TemplateType;
import ru.masterdm.crs.domain.form.mapping.ImportObject;
import ru.masterdm.crs.domain.form.mapping.Mapper;

/**
 * Form templates service.
 * @author Vladimir Shvets
 */
public interface FormTemplateUiService {

    /**
     * Prepares form map linking entities and cells.
     * @param book spreadsheet book
     * @param mapper mapper
     * @param type type
     * @return form map
     */
    List<ImportObject> prepareFormMap(Book book, Mapper mapper, TemplateType type);

    /**
     * Prepares form map linking entities and cells for current calculation.
     * @param book spreadsheet book
     * @param mapper mapper
     * @param context context calculation
     * @param type type
     * @return form map
     */
    List<ImportObject> prepareFormMap(Book book, Mapper mapper, Calculation context, TemplateType type);

    /**
     * Export data to Excel.
     * @param book book
     * @param importObjects list of import objects
     */
    void exportForm(Book book, List<ImportObject> importObjects);

    /**
     * Imports data from Excel.
     * @param book book
     * @param importObjects list of import objects
     */
    void importForm(Book book, List<ImportObject> importObjects);

    /**
     * Process template with parameters dialog.
     * @param template template
     * @param command global command that starts after modal dialog
     */
    void process(FormTemplate template, String command);

    /**
     * Form's date type description.
     * @param dateType date type
     * @return date type description
     */
    String getDateTypeDescription(FormDateType dateType);

    /**
     * Date's offset type description.
     * @param offsetType offset type
     * @return offset type description
     */
    String getOffsetTypeDescription(FormDateType.OffsetType offsetType);

    /**
     * Gets form's date type.
     * @return date types
     */
    ListModelList<FormDateType> getDateTypes();

    /**
     * Gets offset type.
     * @return offset types
     */
    ListModelList<FormDateType.OffsetType> getOffsetTypes();

    /**
     * Exports book to Excel file.
     * @param book book
     */
    void exportFile(Book book);

    /**
     * Imports template with file uploaded.
     * @param template form template
     */
    void importFile(FormTemplate template);

    /**
     * Returns max row count.
     * @param book book
     * @return max row
     */
    int getMaxRows(Book book);

    /**
     * Returns max column count.
     * @param book book
     * @return max column
     */
    int getMaxColumns(Book book);

}
