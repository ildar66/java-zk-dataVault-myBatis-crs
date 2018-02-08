package ru.masterdm.crs.web.domain.entity.form;

import java.time.LocalDate;
import java.util.List;

import ru.masterdm.crs.domain.form.FormTemplate;
import ru.masterdm.crs.domain.form.mapping.MappingField;

/**
 * Excel form object in calculation card.
 * @author Vladimir Shvets
 */
public class UiForm {

    private FormTemplate formTemplate;
    private List<UiFormInstance> formInstances;
    private List<MappingField> dateFields;
    private LocalDate selectedDate;

    /**
     * Constructor.
     * @param formTemplate form template
     * @param dateFields date fields
     * @param formInstances form instance list
     * @param selectedDate selected date
     */
    public UiForm(FormTemplate formTemplate, List<MappingField> dateFields, List<UiFormInstance> formInstances, LocalDate selectedDate) {
        this.formTemplate = formTemplate;
        this.dateFields = dateFields;
        this.formInstances = formInstances;
        this.selectedDate = selectedDate;
    }

    /**
     * Returns form template.
     * @return form template
     */
    public FormTemplate getFormTemplate() {
        return formTemplate;
    }

    /**
     * Returns list of mapping fields.
     * @return list of mapping field
     */
    public List<MappingField> getDateFields() {
        return dateFields;
    }

    /**
     * Sets date fields.
     * @param dateFields date fields
     */
    public void setDateFields(List<MappingField> dateFields) {
        this.dateFields = dateFields;
    }

    /**
     * Returns selected date.
     * @return selected date
     */
    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    /**
     * Sets selected date.
     * @param selectedDate selected date
     */
    public void setSelectedDate(LocalDate selectedDate) {
        this.selectedDate = selectedDate;
    }

    /**
     * Returns form instance list.
     * @return form instance list
     */
    public List<UiFormInstance> getFormInstances() {
        return formInstances;
    }

    /**
     * Returns form instance on selected date.
     * @return form instance
     */
    public UiFormInstance getFormInstance() {
        return formInstances.stream().filter(formInstance -> formInstance.getDate().equals(selectedDate)).findFirst().orElse(null);
    }
}
