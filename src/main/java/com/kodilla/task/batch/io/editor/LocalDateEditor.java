package com.kodilla.task.batch.io.editor;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateEditor extends PropertyEditorSupport {
    private final DateTimeFormatter formatter;

    public LocalDateEditor(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public String getAsText() {
        return getValue().toString();
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(LocalDate.parse(text,  formatter));
    }
}
