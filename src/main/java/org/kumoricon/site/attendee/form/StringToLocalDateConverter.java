package org.kumoricon.site.attendee.form;

import com.vaadin.data.Converter;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class StringToLocalDateConverter implements Converter<String, LocalDate> {
    private static final DateTimeFormatter DEFAULT = DateTimeFormatter.ofPattern("MMddyyyy");
    private static final DateTimeFormatter DASHES = DateTimeFormatter.ofPattern("M-d-yyyy");
    private static final DateTimeFormatter SLASHES = DateTimeFormatter.ofPattern("M/d/yyyy");
    private final String message;

    public StringToLocalDateConverter(String message) {
        this.message = message;
    }


    @Override
    public Result<LocalDate> convertToModel(String fieldValue, ValueContext context) {
        Result result = Result.error(message);
        try {
            result = Result.ok(LocalDate.parse(fieldValue, DEFAULT));
        } catch (DateTimeParseException ignored) {
            try {
                result = Result.ok(LocalDate.parse(fieldValue, SLASHES));
            } catch (DateTimeParseException ignored2) {
                try {
                    result = Result.ok(LocalDate.parse(fieldValue, DASHES));
                } catch (DateTimeParseException ignored3) {

                }
            }
        }
        return result;
    }

    @Override
    public String convertToPresentation(LocalDate date, ValueContext context) {
        if (date != null) {
            return date.format(SLASHES);
        } else {
            return "";
        }
    }
}
