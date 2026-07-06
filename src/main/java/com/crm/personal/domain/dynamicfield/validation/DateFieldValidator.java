package com.crm.personal.domain.dynamicfield.validation;

import com.crm.personal.domain.dynamicfield.model.DynamicFieldType;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class DateFieldValidator implements FieldValidator {

    @Override
    public DynamicFieldType supports() {
        return DynamicFieldType.DATE;
    }

    @Override
    public void validate(String fieldName, String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return;
        }

        try {
            LocalDate.parse(rawValue);
        } catch (DateTimeParseException ex) {
            throw new FieldValidationException(
                    "El campo \"%s\" debe tener formato ISO yyyy-MM-dd".formatted(fieldName),
                    ex
            );
        }
    }
}
