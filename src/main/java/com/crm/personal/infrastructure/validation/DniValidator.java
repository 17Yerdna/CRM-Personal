package com.crm.personal.infrastructure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class DniValidator implements ConstraintValidator<ValidDni, String> {

    private static final Pattern DNI_PATTERN = Pattern.compile("^\\d{8}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return false;
        return DNI_PATTERN.matcher(value.trim()).matches();
    }
}
