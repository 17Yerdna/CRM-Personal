package com.crm.personal.domain.dynamicfield.validation;

import com.crm.personal.domain.shared.DomainException;

public final class FieldValidationException extends DomainException {

    public FieldValidationException(String message) {
        super(message);
    }

    public FieldValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
