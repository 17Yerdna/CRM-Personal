package com.crm.personal.domain.dynamicfield.validation;

import com.crm.personal.domain.dynamicfield.model.DynamicFieldType;

public final class TextFieldValidator implements FieldValidator {

    private static final int MAX_LENGTH = 1000;

    @Override
    public DynamicFieldType supports() {
        return DynamicFieldType.TEXT;
    }

    @Override
    public void validate(String fieldName, String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return;
        }

        if (rawValue.length() > MAX_LENGTH) {
            throw new FieldValidationException(
                    "El campo \"%s\" excede el máximo de %d caracteres".formatted(fieldName, MAX_LENGTH)
            );
        }
    }
}
