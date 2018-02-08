package ru.masterdm.crs.web.domain.entity.form;

import java.time.LocalDate;
import java.util.List;

import org.zkoss.zss.api.model.Book;

import ru.masterdm.crs.domain.form.mapping.ImportObject;

/**
 * Instance of the form object for single date.
 * @author Vladimir Shvets
 */
public class UiFormInstance {

    private Book book;
    private List<ImportObject> importObjects;
    private LocalDate date;
    private boolean changed;

    /**
     * Constructor.
     * @param date date
     * @param importObjects list of import objects
     */
    public UiFormInstance(LocalDate date, List<ImportObject> importObjects) {
        this.date = date;
        this.importObjects = importObjects;
    }

    /**
     * Returns book.
     * @return book
     */
    public Book getBook() {
        return book;
    }

    /**
     * Sets book.
     * @param book book
     */
    public void setBook(Book book) {
        this.book = book;
    }

    /**
     * Returns list of import objects.
     * @return list of import objects
     */
    public List<ImportObject> getImportObjects() {
        return importObjects;
    }

    /**
     * Sets list of import objects.
     * @param importObjects list of import objects
     */
    public void setImportObjects(List<ImportObject> importObjects) {
        this.importObjects = importObjects;
    }

    /**
     * Returns date.
     * @return date
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Returns is from was changed.
     * @return true if from was changed
     */
    public boolean isChanged() {
        return changed;
    }

    /**
     * Sets is from was changed.
     * @param changed is changed
     */
    public void setChanged(boolean changed) {
        this.changed = changed;
    }
}
